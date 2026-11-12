import { useState, useEffect, useCallback } from 'react';
import { apiFetch } from '../services/api';

export function useRutas() {
  const [tab, setTab] = useState('preventores');
  const [preventores, setPreventores] = useState([]);
  const [distribuidores, setDistribuidores] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedAgentId, setSelectedAgentId] = useState(null);

  const cargarRutas = useCallback(async () => {
    setLoading(true);
    try {
      const [rutasPrevRes, rutasDistRes] = await Promise.all([
        apiFetch('/rutas-preventor', { method: 'GET' }).catch(() => []),
        apiFetch('/rutas-distribuidor', { method: 'GET' }).catch(() => []),
      ]);

      const listaPrev = Array.isArray(rutasPrevRes)
        ? rutasPrevRes
        : rutasPrevRes.elementos || rutasPrevRes.content || [];
      const listaDist = Array.isArray(rutasDistRes)
        ? rutasDistRes
        : rutasDistRes.elementos || rutasDistRes.content || [];

      const mapRutaToAgent = (r, isDist) => {
        const paradas = Array.isArray(r.paradas) ? r.paradas : [];
        const completadas = paradas.filter((p) => p.completada).length;
        const total = paradas.length;
        const pct = total > 0 ? Math.round((completadas / total) * 100) : 0;
        const respNombre = r.responsable
          ? `${r.responsable.nombre || ''} ${r.responsable.apellido || ''}`.trim()
          : isDist
          ? 'Distribuidor'
          : 'Preventor';
        const dirActual =
          r.paradaActual?.direccion || paradas[0]?.puntoDeVenta?.direccion || null;
        const lat = Number(dirActual?.latitud) || -32.8895;
        const lng = Number(dirActual?.longitud) || -68.8458;
        const zona =
          dirActual?.departamentoNombre || dirActual?.localidadNombre || 'Gran Mendoza';

        const sucursalDir = r.sucursalOrigenDireccion || null;
        const sucursalNombre = r.sucursalOrigenNombre || (isDist ? 'Dulce Mayor - Sede Central' : 'Dulce Mayor - Sucursal');
        const sucursalDirTexto = sucursalDir
          ? `${sucursalDir.calle || 'Av. San Martín'} ${sucursalDir.numeracion || '1020'}, ${sucursalDir.localidadNombre || sucursalDir.departamentoNombre || 'Mendoza'}`.trim()
          : 'Av. San Martín 1020, Ciudad de Mendoza';

        return {
          id: r.id,
          name: respNombre,
          stops: `${completadas} de ${total} ${isDist ? 'entregas' : 'visitas'}`,
          pct,
          zona,
          lat,
          lng,
          estado: r.estado || 'EN_CURSO',
          sucursalNombre,
          sucursalDireccion: sucursalDirTexto,
          sucursalLat: Number(sucursalDir?.latitud) || -32.889458,
          sucursalLng: Number(sucursalDir?.longitud) || -68.845839,
          paradas,
        };
      };

      const mappedPrev = listaPrev.map((r) => mapRutaToAgent(r, false));
      const mappedDist = listaDist.map((r) => mapRutaToAgent(r, true));

      setPreventores(mappedPrev);
      setDistribuidores(mappedDist);
      if (mappedPrev.length > 0) {
        setSelectedAgentId(mappedPrev[0].id);
      } else if (mappedDist.length > 0) {
        setSelectedAgentId(mappedDist[0].id);
      }
    } catch (err) {
      console.error('Error al cargar rutas:', err);
      setPreventores([]);
      setDistribuidores([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarRutas();
  }, [cargarRutas]);

  const agents = tab === 'preventores' ? preventores : distribuidores;

  const handleTabChange = (newTab) => {
    setTab(newTab);
    const list = newTab === 'preventores' ? preventores : distribuidores;
    setSelectedAgentId(list.length > 0 ? list[0].id : null);
  };

  return {
    tab,
    setTab: handleTabChange,
    agents,
    loading,
    selectedAgentId,
    setSelectedAgentId,
  };
}
