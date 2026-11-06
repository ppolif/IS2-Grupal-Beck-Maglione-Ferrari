export const invoicesMock = [
  {
    id: "inv-3041",
    numeroFactura: "FAC-A-0001-00003041",
    cae: "74391829481920",
    fechaVencimientoCae: "24/06/2025",
    pedidoId: "DM-2083",
    fechaEmision: "14/06/2025 10:10",
    cliente: "Almacén Don Pepe",
    cuit: "20-22948172-8",
    tipoFactura: "Factura A",
    total: 81450,
    totalPagado: 81450,
    saldo: 0,
    estado: "PAGADA",
    items: [
      { producto: "Alfajor triple chocolate (Caja x24)", cantidad: 3, precio: 18900, subtotal: 56700 },
      { producto: "Chupetines surtidos (Display x50)", cantidad: 3, precio: 8250, subtotal: 24750 }
    ],
    pagos: [
      { id: "PAG-4091", fecha: "14/06/2025 10:15", medio: "Transferencia Bancaria", comprobante: "TRF-908123", monto: 81450 }
    ]
  },
  {
    id: "inv-3042",
    numeroFactura: "FAC-B-0001-00003042",
    cae: "74391829481935",
    fechaVencimientoCae: "24/06/2025",
    pedidoId: "DM-2084",
    fechaEmision: "14/06/2025 11:30",
    cliente: "Kiosco El Sol",
    cuit: "20-28491823-4",
    tipoFactura: "Factura B",
    total: 45200,
    totalPagado: 0,
    saldo: 45200,
    estado: "PENDIENTE_PAGO",
    items: [
      { producto: "Alfajor triple chocolate (Caja x24)", cantidad: 2, precio: 18900, subtotal: 37800 },
      { producto: "Gomitas frutales (Bolsa x1 kg)", cantidad: 1, precio: 7400, subtotal: 7400 }
    ],
    pagos: []
  },
  {
    id: "inv-3040",
    numeroFactura: "FAC-A-0001-00003040",
    cae: "74391829481901",
    fechaVencimientoCae: "23/06/2025",
    pedidoId: "DM-2081",
    fechaEmision: "13/06/2025 17:40",
    cliente: "Kiosco Central",
    cuit: "30-71829481-2",
    tipoFactura: "Factura A",
    total: 64120,
    totalPagado: 30000,
    saldo: 34120,
    estado: "PENDIENTE_PAGO",
    items: [
      { producto: "Chocolate con maní (Caja x20)", cantidad: 4, precio: 16030, subtotal: 64120 }
    ],
    pagos: [
      { id: "PAG-4090", fecha: "13/06/2025 18:30", medio: "Efectivo (Repartidor)", comprobante: "REC-0012", monto: 30000 }
    ]
  },
  {
    id: "inv-3038",
    numeroFactura: "FAC-B-0001-00003038",
    cae: "74391829481877",
    fechaVencimientoCae: "22/06/2025",
    pedidoId: "DM-2076",
    fechaEmision: "12/06/2025 09:20",
    cliente: "Drugstore Mitre",
    cuit: "20-33491820-9",
    tipoFactura: "Factura B",
    total: 28500,
    totalPagado: 0,
    saldo: 0,
    estado: "ANULADA",
    motivoAnulacion: "Pedido devuelto por cierre temporal del local.",
    items: [
      { producto: "Turrón con oblea (Display x30)", cantidad: 3, precio: 9500, subtotal: 28500 }
    ],
    pagos: []
  }
];
