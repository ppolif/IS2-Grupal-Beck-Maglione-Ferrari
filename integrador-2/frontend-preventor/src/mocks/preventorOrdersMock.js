export const preventorOrdersMock = [
  {
    id: "DM-2095",
    comercio: "Kiosco Avenida",
    titular: "Mariana López",
    direccion: "Av. San Martín 1120, Godoy Cruz",
    fechaCarga: "Hoy, 14:30 hs",
    montoTotal: 64500,
    subtotal: 71667,
    descuento: 7167,
    codigoPromo: "DULCE10",
    estado: "CARGADO",
    puedeModificar: true,
    bultos: 6,
    detalles: [
      { producto: "Alfajor triple chocolate (Caja x24)", cantidad: 3, precio: 18900, subtotal: 56700 },
      { producto: "Gomitas frutales (Bolsa x1 kg)", cantidad: 3, precio: 6450, subtotal: 19350 }
    ],
    historial: [
      { fechaHora: "Hoy 14:30", estado: "CARGADO", usuario: "Lucía Martínez (Preventor)", observacion: "Pedido cargado en visita presencial." }
    ]
  },
  {
    id: "DM-2094",
    comercio: "Almacén Central",
    titular: "Gonzalo Benítez",
    direccion: "Rivadavia 430, Godoy Cruz",
    fechaCarga: "Hoy, 13:10 hs",
    montoTotal: 41200,
    subtotal: 41200,
    descuento: 0,
    codigoPromo: null,
    estado: "CARGADO",
    puedeModificar: true,
    bultos: 4,
    detalles: [
      { producto: "Chocolate con maní (Caja x20)", cantidad: 2, precio: 15600, subtotal: 31200 },
      { producto: "Chupetines surtidos (Display x50)", cantidad: 1, precio: 8200, subtotal: 8200 }
    ],
    historial: [
      { fechaHora: "Hoy 13:10", estado: "CARGADO", usuario: "Lucía Martínez (Preventor)", observacion: "Pedido registrado desde portal móvil." }
    ]
  },
  {
    id: "DM-2091",
    comercio: "Drugstore Godoy Cruz",
    titular: "Federico Rossi",
    direccion: "Perito Moreno 890, Godoy Cruz",
    fechaCarga: "Hoy, 11:15 hs",
    montoTotal: 94500,
    subtotal: 105000,
    descuento: 10500,
    codigoPromo: "MAYORISTA10",
    estado: "SIN_STOCK",
    puedeModificar: false,
    bultos: 8,
    motivoSinStock: "Faltan 5 bultos de Alfajor triple chocolate en almacén central.",
    detalles: [
      { producto: "Alfajor triple chocolate (Caja x24)", cantidad: 5, precio: 18900, subtotal: 94500 }
    ],
    historial: [
      { fechaHora: "Hoy 11:15", estado: "CARGADO", usuario: "Lucía Martínez (Preventor)", observacion: "Pedido cargado." },
      { fechaHora: "Hoy 11:45", estado: "SIN_STOCK", usuario: "Sistema Automático", observacion: "Stock insuficiente en depósito central." }
    ]
  },
  {
    id: "DM-2089",
    comercio: "Despensa Don Luis",
    titular: "Luis Alberto Castro",
    direccion: "Lavalle 210, Godoy Cruz",
    fechaCarga: "Hoy, 09:40 hs",
    montoTotal: 52400,
    subtotal: 52400,
    descuento: 0,
    codigoPromo: null,
    estado: "FACTURADO",
    puedeModificar: false,
    facturaNumero: "FAC-A-0001-00003042",
    bultos: 5,
    detalles: [
      { producto: "Caramelos ácidos surtidos (Bolsa x800g)", cantidad: 4, precio: 7800, subtotal: 31200 },
      { producto: "Turrón con oblea (Display x30)", cantidad: 2, precio: 10600, subtotal: 21200 }
    ],
    historial: [
      { fechaHora: "Hoy 09:40", estado: "CARGADO", usuario: "Lucía Martínez (Preventor)", observacion: "Pedido cargado." },
      { fechaHora: "Hoy 10:15", estado: "FACTURADO", usuario: "Esteban Quiroga (Almacén)", observacion: "Stock descontado y factura emitida." }
    ]
  },
  {
    id: "DM-2084",
    comercio: "Kiosco El Sol",
    titular: "Roberto Gómez",
    direccion: "Av. San Martín 1842, Godoy Cruz",
    fechaCarga: "Ayer, 16:20 hs",
    montoTotal: 45200,
    subtotal: 45200,
    descuento: 0,
    codigoPromo: null,
    estado: "EN_REPARTO",
    puedeModificar: false,
    distribuidor: "Martín Méndez (Unidad 08)",
    bultos: 5,
    detalles: [
      { producto: "Alfajor triple chocolate (Caja x24)", cantidad: 2, precio: 18900, subtotal: 37800 },
      { producto: "Gomitas frutales (Bolsa x1 kg)", cantidad: 1, precio: 7400, subtotal: 7400 }
    ],
    historial: [
      { fechaHora: "Ayer 16:20", estado: "CARGADO", usuario: "Lucía Martínez (Preventor)", observacion: "Pedido cargado." },
      { fechaHora: "Ayer 17:30", estado: "FACTURADO", usuario: "Esteban Quiroga (Almacén)", observacion: "Facturado con CAE." },
      { fechaHora: "Hoy 08:30", estado: "EN_REPARTO", usuario: "Jefe de Despacho", observacion: "Asignado a Ruta Distribuidor 08." }
    ]
  },
  {
    id: "DM-2080",
    comercio: "Minimarket Mitre",
    titular: "Carlos Mitre",
    direccion: "Mitre 920, Godoy Cruz",
    fechaCarga: "Ayer, 11:00 hs",
    montoTotal: 32800,
    subtotal: 32800,
    descuento: 0,
    codigoPromo: null,
    estado: "ENTREGADO",
    puedeModificar: false,
    fechaEntrega: "Hoy 11:20 hs",
    bultos: 3,
    detalles: [
      { producto: "Chocolate con maní (Caja x20)", cantidad: 2, precio: 16400, subtotal: 32800 }
    ],
    historial: [
      { fechaHora: "Ayer 11:00", estado: "CARGADO", usuario: "Lucía Martínez (Preventor)", observacion: "Pedido cargado." },
      { fechaHora: "Ayer 12:30", estado: "FACTURADO", usuario: "Almacén", observacion: "Facturado." },
      { fechaHora: "Hoy 08:30", estado: "EN_REPARTO", usuario: "Despacho", observacion: "En ruta." },
      { fechaHora: "Hoy 11:20", estado: "ENTREGADO", usuario: "Martín Méndez (Distribuidor)", observacion: "Entregado y cobrado en efectivo." }
    ]
  }
];

export const comerciosPreventorMock = [
  {
    id: "com-1",
    nombre: "Kiosco El Sol",
    titular: "Roberto Gómez",
    cuit: "20-28491823-4",
    direccion: "Av. San Martín 1842",
    localidad: "Godoy Cruz",
    telefono: "261 456 7890",
    ultimoPedido: "DM-2084 · Ayer",
    estadoCuenta: "Al día",
    diaVisita: "Lunes y Jueves"
  },
  {
    id: "com-2",
    nombre: "Kiosco Avenida",
    titular: "Mariana López",
    cuit: "27-31298451-2",
    direccion: "Av. San Martín 1120",
    localidad: "Godoy Cruz",
    telefono: "261 512 8840",
    ultimoPedido: "DM-2095 · Hoy",
    estadoCuenta: "Al día",
    diaVisita: "Martes y Viernes"
  },
  {
    id: "com-3",
    nombre: "Almacén Don Pepe",
    titular: "José Fernández",
    cuit: "20-22948172-8",
    direccion: "Belgrano 450",
    localidad: "Godoy Cruz",
    telefono: "261 498 1234",
    ultimoPedido: "DM-2083 · Hace 3 días",
    estadoCuenta: "Saldo pendiente $12.400",
    diaVisita: "Lunes y Miércoles"
  },
  {
    id: "com-4",
    nombre: "Drugstore Godoy Cruz",
    titular: "Federico Rossi",
    cuit: "20-33491820-9",
    direccion: "Perito Moreno 890",
    localidad: "Godoy Cruz",
    telefono: "261 678 9912",
    ultimoPedido: "DM-2091 · Hoy (Sin Stock)",
    estadoCuenta: "Al día",
    diaVisita: "Miércoles y Sábado"
  },
  {
    id: "com-5",
    nombre: "Minimarket Mitre",
    titular: "Carlos Mitre",
    cuit: "20-29182736-1",
    direccion: "Mitre 920",
    localidad: "Godoy Cruz",
    telefono: "261 334 1122",
    ultimoPedido: "DM-2080 · Entregado",
    estadoCuenta: "Al día",
    diaVisita: "Jueves"
  }
];
