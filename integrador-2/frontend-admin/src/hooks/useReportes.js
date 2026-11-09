import { useState } from 'react';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

/**
 * Custom hook managing executive reports selection, period state and real binary downloads.
 */
export function useReportes() {
  const [activeReport, setActiveReport] = useState('ventas');
  const [periodo, setPeriodo] = useState('Historico');
  const [isDownloading, setIsDownloading] = useState(false);

  const getReportEndpoint = () => {
    if (activeReport === 'ventas') return '/reportes/ventas-historico';
    if (activeReport === 'preventores') return '/reportes/rendimiento-preventores';
    return '/reportes/productos-mas-vendidos';
  };

  const getReportFilename = (formato) => {
    const ext = formato.toLowerCase() === 'excel' ? 'xlsx' : 'pdf';
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
          ...(token ? { Authorization: `Bearer ${token}` } : {})
        }
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
    descargarArchivo('PDF');
  };

  const handleDownloadExcel = () => {
    descargarArchivo('EXCEL');
  };

  return {
    activeReport,
    setActiveReport,
    periodo,
    setPeriodo,
    isDownloading,
    handleDownloadPDF,
    handleDownloadExcel,
  };
}
