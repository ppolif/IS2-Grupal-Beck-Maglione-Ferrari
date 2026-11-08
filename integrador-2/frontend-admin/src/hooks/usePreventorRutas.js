import { useState } from 'react';

export function usePreventorRutas() {
  const [selectedVisit, setSelectedVisit] = useState({
    comercio: 'Kiosco El Sol',
    responsable: 'Roberto Gómez',
    direccion: 'Av. San Martín 1842, Godoy Cruz',
    distancia: '8 min · 2,4 km',
    preventor: 'Lucía Martínez',
    estado: 'Visitado',
    ultimoPedido: '$45.200 (hace 1 hora)'
  });

  const visits = [
    { comercio: 'Kiosco El Sol', direccion: 'Av. San Martín 1842, Godoy Cruz', preventor: 'Lucía Martínez', estado: 'Visitado', hora: '10:15' },
    { comercio: 'Almacén Don Pepe', direccion: 'Belgrano 450, Godoy Cruz', preventor: 'Lucía Martínez', estado: 'En camino', hora: '11:00' },
    { comercio: 'Drugstore Mitre', direccion: 'Mitre 920, Godoy Cruz', preventor: 'Lucía Martínez', estado: 'Pendiente', hora: '11:45' },
    { comercio: 'Kiosco Central', direccion: 'Rivadavia 110, Godoy Cruz', preventor: 'Lucía Martínez', estado: 'Pendiente', hora: '12:30' }
  ];

  const handleOpenHistorial = () => {
    alert('Abriendo historial comercial del cliente... (Simulado)');
  };

  return {
    selectedVisit,
    setSelectedVisit,
    visits,
    handleOpenHistorial
  };
}
