export const deliveriesMock = [
  {
    id: "DM-2084",
    comercio: "Kiosco El Sol",
    responsable: "Roberto Gómez",
    direccion: "Av. San Martín 1842, Godoy Cruz",
    distancia: "Godoy Cruz · 2,4 km",
    tiempoEstimado: "12 min",
    orden: 4,
    totalEntregas: 10,
    monto: "$45.200",
    medioPago: "Efectivo",
    estadoPago: "cash", // 'cash' -> 'Cobrar $45.200', 'paid' -> 'Transferencia · pagado'
    estadoEntrega: "En curso",
    items: "3 cajas alfajores · 2 bolsas gomitas",
    bultos: 5
  },
  {
    id: "DM-2083",
    comercio: "Almacén Don Pepe",
    responsable: "José Fernández",
    direccion: "Belgrano 450, Godoy Cruz",
    distancia: "Godoy Cruz · 3,2 km",
    tiempoEstimado: "20 min",
    orden: 5,
    totalEntregas: 10,
    monto: "$81.450",
    medioPago: "Transferencia",
    estadoPago: "paid",
    estadoEntrega: "Pendiente",
    items: "5 cajas alfajores · 4 displays chupetines",
    bultos: 9
  },
  {
    id: "DM-2082",
    comercio: "Drugstore Mitre",
    responsable: "Carlos Mitre",
    direccion: "Mitre 920, Godoy Cruz",
    distancia: "Godoy Cruz · 4,8 km",
    tiempoEstimado: "35 min",
    orden: 6,
    totalEntregas: 10,
    monto: "$32.800",
    medioPago: "Efectivo",
    estadoPago: "cash",
    estadoEntrega: "Pendiente",
    items: "2 cajas chocolates · 1 display turrones",
    bultos: 3
  },
  {
    id: "DM-2081",
    comercio: "Kiosco Central",
    responsable: "Mariana Paz",
    direccion: "Rivadavia 110, Godoy Cruz",
    distancia: "Godoy Cruz · 6,1 km",
    tiempoEstimado: "50 min",
    orden: 7,
    totalEntregas: 10,
    monto: "$64.120",
    medioPago: "Transferencia",
    estadoPago: "paid",
    estadoEntrega: "Pendiente",
    items: "4 cajas alfajores · 3 bolsas gomitas",
    bultos: 7
  }
];

