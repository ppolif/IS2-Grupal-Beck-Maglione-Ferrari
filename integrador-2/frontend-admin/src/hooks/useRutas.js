import { useState } from 'react';

export function useRutas() {
  const [tab, setTab] = useState('preventores');

  const preventores = [
    {
      id: 'prev-1',
      name: 'Lucía Martínez',
      stops: '8 de 12 visitas',
      pct: 67,
      zona: 'Godoy Cruz',
      lat: -32.9238,
      lng: -68.8423,
      estado: 'En calle'
    },
    {
      id: 'prev-2',
      name: 'Martín Flores',
      stops: '5 de 10 visitas',
      pct: 50,
      zona: 'Capital',
      lat: -32.8895,
      lng: -68.8458,
      estado: 'En calle'
    },
    {
      id: 'prev-3',
      name: 'Sofía Vidal',
      stops: '7 de 9 visitas',
      pct: 78,
      zona: 'Guaymallén',
      lat: -32.8980,
      lng: -68.8050,
      estado: 'En calle'
    }
  ];

  const distribuidores = [
    {
      id: 'dist-1',
      name: 'Unidad 08 (Reparto)',
      stops: '6 de 10 entregas',
      pct: 60,
      zona: 'Godoy Cruz / Maipú',
      lat: -32.9350,
      lng: -68.8250,
      estado: 'En ruta'
    },
    {
      id: 'dist-2',
      name: 'Unidad 03 (Reparto)',
      stops: '8 de 12 entregas',
      pct: 66,
      zona: 'Ciudad / Las Heras',
      lat: -32.8750,
      lng: -68.8350,
      estado: 'En ruta'
    },
    {
      id: 'dist-3',
      name: 'Unidad 05 (Reparto)',
      stops: '4 de 8 entregas',
      pct: 50,
      zona: 'Guaymallén Este',
      lat: -32.9050,
      lng: -68.7900,
      estado: 'En ruta'
    }
  ];

  {/* estado de seleccion para centrado del mapa*/}
  const [selectedAgentId, setSelectedAgentId] = useState('prev-1');

  const agents = tab === 'preventores' ? preventores : distribuidores;

  const handleTabChange = (newTab) => {
    setTab(newTab);
    setSelectedAgentId(newTab === 'preventores' ? 'prev-1' : 'dist-1');
  };

  return {
    tab,
    setTab: handleTabChange,
    agents,
    selectedAgentId,
    setSelectedAgentId
  };
}
