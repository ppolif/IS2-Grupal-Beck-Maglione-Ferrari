import { useState } from 'react';

export function useRutas() {
  const [tab, setTab] = useState('preventores');

  const agents =
    tab === 'preventores'
      ? [
          { name: 'Lucía Martínez', stops: '8 de 12 visitas', pct: 67, zona: 'Godoy Cruz' },
          { name: 'Martín Flores', stops: '5 de 10 visitas', pct: 50, zona: 'Capital' },
          { name: 'Sofía Vidal', stops: '7 de 9 visitas', pct: 78, zona: 'Guaymallén' }
        ]
      : [
          { name: 'Unidad 08 (Reparto)', stops: '6 de 10 entregas', pct: 60, zona: 'Godoy Cruz / Maipú' },
          { name: 'Unidad 03 (Reparto)', stops: '8 de 12 entregas', pct: 66, zona: 'Ciudad / Las Heras' },
          { name: 'Unidad 05 (Reparto)', stops: '4 de 8 entregas', pct: 50, zona: 'Guaymallén Este' }
        ];

  return {
    tab,
    setTab,
    agents
  };
}
