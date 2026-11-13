import { useEffect, useRef } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { colors } from '../theme/colors';

// Función para consultar a la API de OpenStreetMap (OSRM) la ruta real calle por calle
async function obtenerTrazoCallesOSM(puntos) {
  if (puntos.length < 2) return null;

  // En OSRM el orden de coordenadas es: longitud,latitud
  const coordString = puntos.map((p) => `${p.lng},${p.lat}`).join(';');
  const url = `https://router.project-osrm.org/route/v1/driving/${coordString}?overview=full&geometries=geojson`;

  try {
    const res = await fetch(url);
    if (!res.ok) return null;
    const data = await res.json();
    if (data.code === 'Ok' && data.routes && data.routes.length > 0) {
      // Invertir formato GeoJSON [lng, lat] al formato esperado por Leaflet [lat, lng]
      return data.routes[0].geometry.coordinates.map((c) => [c[1], c[0]]);
    }
  } catch (error) {
    console.warn('No se pudo obtener la ruta callejera de OpenStreetMap, usando fallback directo:', error);
  }
  return null;
}

// Marcador vectorial personalizado para evitar problemas de assets en Vite
const createCustomIcon = (color = colors.primary.DEFAULT, number = null) => {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 42" width="32" height="42">
      <path d="M16 0C7.16 0 0 7.16 0 16c0 10.5 16 26 16 26s16-15.5 16-26c0-8.84-7.16-16-16-16z" fill="${color}" stroke="${colors.surface.DEFAULT}" stroke-width="2"/>
      <circle cx="16" cy="16" r="10" fill="${colors.surface.DEFAULT}"/>
      ${
        number !== null
          ? `<text x="16" y="20" font-size="11" font-weight="bold" fill="${color}" text-anchor="middle" font-family="sans-serif">${number}</text>`
          : `<circle cx="16" cy="16" r="5" fill="${color}"/>`
      }
    </svg>
  `;
  return L.divIcon({
    className: 'custom-leaflet-marker',
    html: svg,
    iconSize: [32, 42],
    iconAnchor: [16, 42],
    popupAnchor: [0, -38]
  });
};

export default function OSMMap({
  markers = [],
  activeMarkerId = null,
  showRoute = false,
  routeColor = colors.primary.DEFAULT,
  className = 'h-full w-full',
  center = [-32.8895, -68.8458], // Mendoza Centro default
  zoom = 13,
  onMarkerClick = null
}) {
  const mapContainerRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const layerGroupRef = useRef(null);
  const polylineRef = useRef(null);

  // Inicializa el mapa una sola vez
  useEffect(() => {
    if (!mapContainerRef.current) return;

    if (!mapInstanceRef.current) {
      const map = L.map(mapContainerRef.current, {
        zoomControl: true,
        attributionControl: true
      }).setView(center, zoom);

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      }).addTo(map);

      layerGroupRef.current = L.layerGroup().addTo(map);
      mapInstanceRef.current = map;
    }

    return () => {
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove();
        mapInstanceRef.current = null;
      }
    };
  }, []);

  // Actualiza los marcadores, ruta callejera y foco activo
  useEffect(() => {
    const map = mapInstanceRef.current;
    if (!map || !layerGroupRef.current) return;

    layerGroupRef.current.clearLayers();
    if (polylineRef.current) {
      map.removeLayer(polylineRef.current);
      polylineRef.current = null;
    }

    const validMarkers = markers.filter(
      (m) => typeof m.lat === 'number' && typeof m.lng === 'number'
    );

    if (validMarkers.length === 0) return;

    const latLngs = [];

    validMarkers.forEach((m, idx) => {
      const isSelected = activeMarkerId !== null && String(m.id) === String(activeMarkerId);
      const markerColor = isSelected ? colors.status.danger.DEFAULT : (m.color || routeColor);
      const icon = createCustomIcon(markerColor, m.orden || idx + 1);

      const marker = L.marker([m.lat, m.lng], { icon });

      let popupContent = `
        <div style="font-family: inherit; font-size: 12px; line-height: 1.4; padding: 2px;">
          <strong style="font-size: 13px; color: ${colors.ink}; display: block; margin-bottom: 2px;">${m.nombre || m.comercio || 'Punto'}</strong>
          ${m.direccion ? `<div style="color: ${colors.muted.DEFAULT}; margin-bottom: 4px;">${m.direccion}</div>` : ''}
          ${m.distancia ? `<div style="color: ${colors.secondary.DEFAULT}; font-weight: 500; font-size: 11px;">Distancia: ${m.distancia}</div>` : ''}
          ${m.estado ? `<span style="display: inline-block; background: ${colors.primary.light}; color: ${colors.primary.DEFAULT}; font-size: 10px; font-weight: bold; padding: 2px 6px; border-radius: 4px; margin-top: 4px;">${m.estado}</span>` : ''}
        </div>
      `;

      marker.bindPopup(popupContent);

      if (onMarkerClick) {
        marker.on('click', () => onMarkerClick(m));
      }

      layerGroupRef.current.addLayer(marker);
      latLngs.push([m.lat, m.lng]);

      if (isSelected) {
        marker.openPopup();
      }
    });

    // Trazado de ruta callejera real con OpenStreetMap
    if (showRoute && latLngs.length > 1) {
      obtenerTrazoCallesOSM(validMarkers).then((caminoCallejero) => {
        if (!mapInstanceRef.current) return;
        if (polylineRef.current) {
          map.removeLayer(polylineRef.current);
        }

        // Si OSRM devuelve el trazado vial, dibuja sobre las calles reales; caso contrario usa línea directa
        const puntosFinales = caminoCallejero || latLngs;

        polylineRef.current = L.polyline(puntosFinales, {
          color: routeColor,
          weight: 5,
          opacity: 0.85,
          smoothFactor: 1
        }).addTo(map);
      });
    }

    // Centrado de cámara
    const active = validMarkers.find((m) => String(m.id) === String(activeMarkerId));
    if (active) {
      map.flyTo([active.lat, active.lng], 15, { animate: true, duration: 0.8 });
    } else if (latLngs.length > 1) {
      const bounds = L.latLngBounds(latLngs);
      map.fitBounds(bounds, { padding: [40, 40] });
    } else if (latLngs.length === 1) {
      map.setView(latLngs[0], 14);
    }

    setTimeout(() => {
      map.invalidateSize();
    }, 200);
  }, [markers, activeMarkerId, showRoute, routeColor]);

  return (
    <div className={`relative overflow-hidden rounded-[24px] border border-border-subtle shadow-sm ${className}`}>
      <div ref={mapContainerRef} className="h-full w-full z-0" />
    </div>
  );
}
