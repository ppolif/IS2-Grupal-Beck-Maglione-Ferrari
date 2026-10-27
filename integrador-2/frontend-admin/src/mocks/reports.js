// Datos para gráficos de Recharts imitando fielmente el prototipo
// Colores de prototipo: Rosa fuerte (#E86BC4), Celeste fuerte (#3FB8D9), Amarillo (#FFD98A)

export const monthlySalesChart = [
  { dia: '01', ventas: 340000, previo: 280000 },
  { dia: '05', ventas: 520000, previo: 410000 },
  { dia: '10', ventas: 480000, previo: 490000 },
  { dia: '15', ventas: 790000, previo: 610000 },
  { dia: '20', ventas: 640000, previo: 580000 },
  { dia: '25', ventas: 920000, previo: 720000 },
  { dia: '30', ventas: 880000, previo: 760000 }
];

export const weeklyActivityChart = [
  { dia: 'L', pedidos: 55 },
  { dia: 'M', pedidos: 78 },
  { dia: 'X', pedidos: 46 },
  { dia: 'J', pedidos: 88 },
  { dia: 'V', pedidos: 64 },
  { dia: 'S', pedidos: 92 },
  { dia: 'D', pedidos: 70 }
];

export const categoryDonutChart = [
  { name: 'Chocolates', value: 42, color: '#E86BC4' },
  { name: 'Golosinas', value: 34, color: '#3FB8D9' },
  { name: 'Otros', value: 24, color: '#FFD98A' }
];

export const preventorRankingMock = [
  {
    preventor: 'Lucía Martínez',
    zona: 'Godoy Cruz',
    ventasTotal: '$4.850.000',
    pedidosConcretados: 48,
    visitasEfectuadas: 54,
    efectividad: '88.8%',
    ranking: 1
  },
  {
    preventor: 'Martín Flores',
    zona: 'Capital',
    ventasTotal: '$4.120.000',
    pedidosConcretados: 42,
    visitasEfectuadas: 50,
    efectividad: '84.0%',
    ranking: 2
  },
  {
    preventor: 'Sofía Vidal',
    zona: 'Guaymallén',
    ventasTotal: '$3.830.000',
    pedidosConcretados: 36,
    visitasEfectuadas: 46,
    efectividad: '78.2%',
    ranking: 3
  }
];

export const topCandiesMock = [
  {
    posicion: 1,
    producto: 'Alfajor triple chocolate',
    categoria: 'Alfajores',
    unidadesVendidas: 4280,
    totalFacturado: '$3.370.500',
    crecimiento: '+18%'
  },
  {
    posicion: 2,
    producto: 'Gomitas frutales 1kg',
    categoria: 'Golosinas',
    unidadesVendidas: 3150,
    totalFacturado: '$2.031.750',
    crecimiento: '+12%'
  },
  {
    posicion: 3,
    producto: 'Chocolate con maní',
    categoria: 'Chocolates',
    unidadesVendidas: 2400,
    totalFacturado: '$1.872.000',
    crecimiento: '+8%'
  },
  {
    posicion: 4,
    producto: 'Chupetines surtidos',
    categoria: 'Chupetines',
    unidadesVendidas: 1980,
    totalFacturado: '$1.623.600',
    crecimiento: '+15%'
  }
];

