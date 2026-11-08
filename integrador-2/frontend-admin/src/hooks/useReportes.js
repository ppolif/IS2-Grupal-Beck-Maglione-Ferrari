import { useState } from 'react';

/**
 * Custom hook managing executive reports selection, period state and simulated downloads.
 */
export function useReportes() {
  const [activeReport, setActiveReport] = useState('ventas');
  const [periodo, setPeriodo] = useState('Junio 2025');

  const getReportTitle = () => {
    if (activeReport === 'ventas') return 'Reporte Global de Ventas por Período';
    if (activeReport === 'preventores') return 'Análisis de Rendimiento de Preventores';
    return 'Análisis del Volumen de Golosinas Más Vendidas';
  };

  const handleDownloadPDF = () => {
    alert(`Descargando "${getReportTitle()}" en formato PDF... (Simulado)`);
  };

  const handleDownloadExcel = () => {
    alert(`Descargando "${getReportTitle()}" en formato Excel (.xlsx)... (Simulado)`);
  };

  return {
    activeReport,
    setActiveReport,
    periodo,
    setPeriodo,
    handleDownloadPDF,
    handleDownloadExcel,
    getReportTitle,
  };
}
