import { useState, useEffect, useCallback } from 'react';
import { apiFetch } from '../services/api';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

/**
 * Custom hook managing executive reports selection, real data fetching and binary downloads.
 */
export function useReportes() {
  const [activeReport, setActiveReport] = useState('ventas');
  const [periodo, setPeriodo] = useState('Histórico');
  const [isDownloading, setIsDownloading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [ventasRows, setVentasRows] = useState([]);
  const [preventoresRows, setPreventoresRows] = useState([]);
  const [golosinasRows, setGolosinasRows] = useState([]);

  const cargarReportes = useCallback(async () => {
    setLoading(true);
    try {
      const [ventasRes, prevRes, golRes] = await Promise.all([
        apiFetch('/reportes/ventas-historico?formato=json', { method: 'GET' }).catch(() => ({ filas: [] })),
        apiFetch('/reportes/rendimiento-preventores?formato=json', { method: 'GET' }).catch(() => ({ filas: [] })),
        apiFetch('/reportes/productos-mas-vendidos?formato=json', { method: 'GET' }).catch(() => ({ filas: [] })),
      ]);

      setVentasRows(Array.isArray(ventasRes?.filas) ? ventasRes.filas : []);
      setPreventoresRows(Array.isArray(prevRes?.filas) ? prevRes.filas : []);
      setGolosinasRows(Array.isArray(golRes?.filas) ? golRes.filas : []);
    } catch (err) {
      console.error('Error al cargar reportes:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarReportes();
  }, [cargarReportes]);

  const getReportEndpoint = () => {
    if (activeReport === 'ventas') return '/reportes/ventas-historico';
    if (activeReport === 'preventores') return '/reportes/rendimiento-preventores';
    return '/reportes/productos-mas-vendidos';
  };

  const getReportFilename = (formato) => {
    const ext = formato.toLowerCase() === 'xlsx' || formato.toLowerCase() === 'excel' ? 'xlsx' : 'pdf';
    return `reporte-${activeReport}-${Date.now()}.${ext}`;
  };

  const descargarArchivo = async (formato) => {
    const token = localStorage.getItem('dm_admin_token');
    const endpoint = getReportEndpoint();
    const url = `${API_BASE_URL}${endpoint}?formato=${encodeURIComponent(formato)}`;

    setIsDownloading(true);
    try {
      const response = await fetch(url, {
        headers: {
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status} al generar el reporte`);
      }

      const blob = await response.blob();
      const downloadUrl = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = downloadUrl;
      link.download = getReportFilename(formato);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(downloadUrl);
    } catch (err) {
      alert(`No se pudo descargar el reporte: ${err.message}`);
    } finally {
      setIsDownloading(false);
    }
  };

  const handleDownloadPDF = () => {
    descargarArchivo('pdf');
  };

  const handleDownloadExcel = () => {
    descargarArchivo('xlsx');
  };

  const totalFacturadoVentas = ventasRows.reduce((acc, r) => acc + Number(r.total || 0), 0);

  return {
    activeReport,
    setActiveReport,
    periodo,
    setPeriodo,
    loading,
    ventasRows,
    preventoresRows,
    golosinasRows,
    totalFacturadoVentas,
    isDownloading,
    handleDownloadPDF,
    handleDownloadExcel,
  };
}
