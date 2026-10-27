export const ordersMock = [
  {
    id: 'DM-2084',
    comercio: 'Kiosco El Sol',
    preventor: 'Lucía Martínez',
    monto: '$45.200',
    montoNum: 45200,
    estado: 'En camino',
    facturado: false,
    fecha: '14/06/2025 09:30',
    items: [
      { nombre: 'Alfajor triple chocolate', cant: 3, sub: '$56.700' },
      { nombre: 'Gomitas frutales', cant: 2, sub: '$12.900' }
    ]
  },
  {
    id: 'DM-2083',
    comercio: 'Almacén Don Pepe',
    preventor: 'Martín Flores',
    monto: '$81.450',
    montoNum: 81450,
    estado: 'Facturado',
    facturado: true,
    fecha: '14/06/2025 09:10',
    items: [
      { nombre: 'Chocolate con maní', cant: 5, sub: '$78.000' }
    ]
  },
  {
    id: 'DM-2082',
    comercio: 'Drugstore Mitre',
    preventor: 'Sofía Vidal',
    monto: '$32.800',
    montoNum: 32800,
    estado: 'Pendiente',
    facturado: false,
    fecha: '14/06/2025 08:45',
    items: [
      { nombre: 'Chupetines surtidos', cant: 4, sub: '$32.800' }
    ]
  },
  {
    id: 'DM-2081',
    comercio: 'Kiosco Central',
    preventor: 'Lucía Martínez',
    monto: '$64.120',
    montoNum: 64120,
    estado: 'Entregado',
    facturado: true,
    fecha: '13/06/2025 17:20',
    items: [
      { nombre: 'Alfajor triple chocolate', cant: 2, sub: '$37.800' },
      { nombre: 'Caramelos masticables', cant: 5, sub: '$26.000' }
    ]
  },
  {
    id: 'DM-2080',
    comercio: 'Minimarket San Juan',
    preventor: 'Martín Flores',
    monto: '$52.300',
    montoNum: 52300,
    estado: 'Entregado',
    facturado: true,
    fecha: '13/06/2025 16:15',
    items: [
      { nombre: 'Turrón de maní crocante', cant: 4, sub: '$39.200' }
    ]
  }
];

