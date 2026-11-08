import { useState } from 'react';
import { preventorRankingMock } from '../mocks/reports';

export function useRendimiento() {
  const [zonaFilter, setZonaFilter] = useState('Todas');

  const filtered = preventorRankingMock.filter((p) => {
    if (zonaFilter === 'Todas') return true;
    return p.zona === zonaFilter;
  });

  const handleDownloadPdf = () => {
    alert('Descargando ranking en PDF... (Simulado)');
  };

  return {
    zonaFilter,
    setZonaFilter,
    filtered,
    handleDownloadPdf
  };
}
