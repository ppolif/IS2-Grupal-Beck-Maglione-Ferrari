import { useState, useEffect, useCallback } from 'react';
import { apiFetch } from '../services/api';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

export function useRendimiento() {
  const [rankingList, setRankingList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [zonaFilter, setZonaFilter] = useState('Todas');

  const cargarRendimiento = useCallback(async () => {
    setLoading(true);
    try {
      const res = await apiFetch('/reportes/rendimiento-preventores?formato=json', { method: 'GET' });
      const filas = Array.isArray(res?.filas) ? res.filas : [];
      const mapped = filas.map((r, index) => {
        const cantPedidos = Number(r.cantidadPedidos || 0);
        const entregados = Number(r.pedidosEntregados || 0);
        const pct = cantPedidos > 0 ? Math.round((entregados / cantPedidos) * 100) : 0;
        return {
          ranking: index + 1,
          legajo: r.legajo || '-',
          preventor: r.preventor || '-',
          zona: r.sucursal && r.sucursal !== '-' ? r.sucursal : 'General',
          ventasTotal: `$${Number(r.montoTotal || 0).toLocaleString('es-AR')}`,
          visitasEfectuadas: cantPedidos,
          pedidosConcretados: entregados,
          efectividad: `${pct}%`,
        };
      });
      setRankingList(mapped);
    } catch (err) {
      console.error('Error al cargar rendimiento de preventores:', err);
      setRankingList([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarRendimiento();
  }, [cargarRendimiento]);

  const filtered = rankingList.filter((p) => {
    if (zonaFilter === 'Todas') return true;
    return p.zona.toLowerCase().includes(zonaFilter.toLowerCase());
  });

  const handleDownloadPdf = async () => {
    try {
      const token = localStorage.getItem('dm_admin_token');
      const res = await fetch(`${API_BASE_URL}/reportes/rendimiento-preventores?formato=pdf`, {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'reporte-rendimiento-preventores.pdf';
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert(`No se pudo descargar el PDF: ${err.message}`);
    }
  };

  return {
    loading,
    zonaFilter,
    setZonaFilter,
    filtered,
    handleDownloadPdf,
  };
}
