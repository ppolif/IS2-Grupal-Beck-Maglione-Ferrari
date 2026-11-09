import { useEffect, useRef } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix generic Leaflet marker icon paths in Vite bundles
const createCustomIcon = (color = '#e86bc4', number = null) => {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 42" width="32" height="42">
      <path d="M16 0C7.16 0 0 7.16 0 16c0 10.5 16 26 16 26s16-15.5 16-26c0-8.84-7.16-16-16-16z" fill="${color}" stroke="#ffffff" stroke-width="2"/>
      <circle cx="16" cy="16" r="10" fill="#ffffff"/>
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
  routeColor = '#e86bc4',
  className = 'h-full w-full',
  center = [-32.8895, -68.8458], // Mendoza Centro default
  zoom = 13,
  onMarkerClick = null
}) {
  const mapContainerRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const layerGroupRef = useRef(null);
  const polylineRef = useRef(null);

  // Initialize Map once
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

  // Update Markers, Route and Active focus
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
      const markerColor = isSelected ? '#ff3b30' : (m.color || routeColor);
      const icon = createCustomIcon(markerColor, m.orden || idx + 1);

      const marker = L.marker([m.lat, m.lng], { icon });

      let popupContent = `
        <div style="font-family: inherit; font-size: 12px; line-height: 1.4; padding: 2px;">
          <strong style="font-size: 13px; color: #2b2b3a; display: block; margin-bottom: 2px;">${m.nombre || m.comercio || 'Punto'}</strong>
          ${m.direccion ? `<div style="color: #6b6b80; margin-bottom: 4px;">${m.direccion}</div>` : ''}
          ${m.zona ? `<span style="display: inline-block; background: #d9f7ff; color: #247f99; font-size: 10px; font-weight: bold; padding: 2px 6px; border-radius: 4px;">${m.zona}</span>` : ''}
          ${m.estado ? `<span style="display: inline-block; background: #ffe2f7; color: #e86bc4; font-size: 10px; font-weight: bold; padding: 2px 6px; border-radius: 4px; margin-left: 4px;">${m.estado}</span>` : ''}
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

    // Draw route line if requested
    if (showRoute && latLngs.length > 1) {
      polylineRef.current = L.polyline(latLngs, {
        color: routeColor,
        weight: 4,
        opacity: 0.8,
        dashArray: '6, 8',
        smoothFactor: 1
      }).addTo(map);
    }

    // Auto center
    const active = validMarkers.find((m) => String(m.id) === String(activeMarkerId));
    if (active) {
      map.flyTo([active.lat, active.lng], 15, { animate: true, duration: 0.8 });
    } else if (latLngs.length > 1) {
      const bounds = L.latLngBounds(latLngs);
      map.fitBounds(bounds, { padding: [40, 40] });
    } else if (latLngs.length === 1) {
      map.setView(latLngs[0], 14);
    }

    // Trigger a resize check to avoid grey tiles in responsive layouts
    setTimeout(() => {
      map.invalidateSize();
    }, 200);
  }, [markers, activeMarkerId, showRoute, routeColor]);

  return (
    <div className={`relative overflow-hidden rounded-[24px] border border-[#ececf2] shadow-sm ${className}`}>
      <div ref={mapContainerRef} className="h-full w-full z-0" />
    </div>
  );
}
