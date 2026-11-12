import { useState, useEffect, useCallback } from 'react';
import { apiFetch } from '../services/api';

export function usePreventorRutas() {
  const [visits, setVisits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedVisit, setSelectedVisit] = useState(null);
  const [preventorNombre, setPreventorNombre] = useState('Sin ruta activa');
  const [zonaNombre, setZonaNombre] = useState('Gran Mendoza');

  const cargarVisitas = useCallback(async () => {
    setLoading(true);
    try {
      const rutasRes = await apiFetch('/rutas-preventor', { method: 'GET' }).catch(() => []);
      const lista = Array.isArray(rutasRes) ? rutasRes : rutasRes.elementos || rutasRes.content || [];

      if (lista.length > 0) {
        const ruta = lista[0];
        const resp = ruta.responsable
          ? `${ruta.responsable.nombre || ''} ${ruta.responsable.apellido || ''}`.trim()
          : 'Preventor';
        setPreventorNombre(resp);

        const sucursalNombre =
          ruta.sucursalOrigenNombre ||
          ruta.responsable?.mayoristaRazonSocial ||
          'Dulce Mayor - Sucursal';
        const sucDir = ruta.sucursalOrigenDireccion || {};
        const sucursalItem = {
          id: 'origen-sucursal',
          comercio: `Salida: ${sucursalNombre}`,
          responsable: resp,
          direccion: `${sucDir.calle || 'Av. San Martín'} ${sucDir.numeracion || '1020'}, ${sucDir.localidadNombre || sucDir.departamentoNombre || 'Mendoza'}`.trim(),
          preventor: resp,
          estado: 'Sucursal Origen',
          hora: 'Punto de Partida (S)',
          orden: 'S',
          color: '#3fb8d9',
          lat: Number(sucDir.latitud) || -32.889458,
          lng: Number(sucDir.longitud) || -68.845839,
          esSucursal: true,
        };

        const paradas = Array.isArray(ruta.paradas) ? ruta.paradas : [];
        const mapped = paradas.map((p, idx) => {
          const pdv = p.puntoDeVenta || {};
          const dir = pdv.direccion || {};
          return {
            id: pdv.id || idx + 1,
            comercio: pdv.nombreComercio || `Parada #${p.orden || idx + 1}`,
            responsable: pdv.minorista
              ? `${pdv.minorista.nombre || ''} ${pdv.minorista.apellido || ''}`.trim()
              : '-',
            direccion: `${dir.calle || ''} ${dir.numeracion || ''}, ${dir.localidadNombre || dir.departamentoNombre || 'Mendoza'}`.trim(),
            preventor: resp,
            estado: p.completada ? 'Visitado' : ruta.paradaActual?.id === pdv.id ? 'En camino' : 'Pendiente',
            hora: `Parada #${p.orden || idx + 1}`,
            orden: p.orden || idx + 1,
            lat: Number(dir.latitud) || -32.8895,
            lng: Number(dir.longitud) || -68.8458,
          };
        });

        const itemsConSucursal = [sucursalItem, ...mapped];
        setVisits(itemsConSucursal);
        if (mapped.length > 0) {
          setSelectedVisit(mapped[0]);
          const firstDir = paradas[0]?.puntoDeVenta?.direccion;
          if (firstDir?.departamentoNombre) {
            setZonaNombre(firstDir.departamentoNombre);
          }
        } else {
          setSelectedVisit(sucursalItem);
        }
      } else {
        setVisits([]);
        setSelectedVisit(null);
      }
    } catch (err) {
      console.error('Error al cargar paradas de preventor:', err);
      setVisits([]);
      setSelectedVisit(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarVisitas();
  }, [cargarVisitas]);

  return {
    loading,
    preventorNombre,
    zonaNombre,
    selectedVisit,
    setSelectedVisit,
    visits,
  };
}
