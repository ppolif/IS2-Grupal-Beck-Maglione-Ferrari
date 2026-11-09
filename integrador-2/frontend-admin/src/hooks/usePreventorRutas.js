import { useState } from 'react';

export function usePreventorRutas() {
  const visits = [
    {
      id: 1,
      comercio: 'Kiosco El Sol',
      responsable: 'Roberto Gómez',
      direccion: 'Av. San Martín 1842, Godoy Cruz',
      distancia: '8 min · 2,4 km',
      preventor: 'Lucía Martínez',
      estado: 'Visitado',
      ultimoPedido: '$45.200 (hace 1 hora)',
      hora: '10:15',
      orden: 1,
      lat: -32.9238,
      lng: -68.8423
    },
    {
      id: 2,
      comercio: 'Almacén Don Pepe',
      responsable: 'José Fernández',
      direccion: 'Belgrano 450, Godoy Cruz',
      distancia: '12 min · 3,2 km',
      preventor: 'Lucía Martínez',
      estado: 'En camino',
      ultimoPedido: '$81.450 (hace 3 horas)',
      hora: '11:00',
      orden: 2,
      lat: -32.9180,
      lng: -68.8465
    },
    {
      id: 3,
      comercio: 'Drugstore Mitre',
      responsable: 'Carlos Mitre',
      direccion: 'Mitre 920, Godoy Cruz',
      distancia: '18 min · 4,8 km',
      preventor: 'Lucía Martínez',
      estado: 'Pendiente',
      ultimoPedido: '$32.800 (hace 1 día)',
      hora: '11:45',
      orden: 3,
      lat: -32.9140,
      lng: -68.8390
    },
    {
      id: 4,
      comercio: 'Kiosco Central',
      responsable: 'Mariana Paz',
      direccion: 'Rivadavia 110, Godoy Cruz',
      distancia: '25 min · 6,1 km',
      preventor: 'Lucía Martínez',
      estado: 'Pendiente',
      ultimoPedido: '$64.120 (hace 2 días)',
      hora: '12:30',
      orden: 4,
      lat: -32.9265,
      lng: -68.8495
    }
  ];

  const [selectedVisit, setSelectedVisit] = useState(visits[0]);

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
