export const promotionsMock = [
  {
    id: "promo-1",
    codigo: "DULCE10",
    descripcion: "10% de descuento en el total de la compra",
    porcentajeDescuento: 10,
    cantidadMinima: 0,
    productoDestacado: null,
    fechaDesde: "2025-06-01",
    fechaHasta: "2025-07-31",
    estado: "Vigente"
  },
  {
    id: "promo-2",
    codigo: "MAYORISTA20",
    descripcion: "20% de descuento por compra en volumen (mínimo 10 bultos)",
    porcentajeDescuento: 20,
    cantidadMinima: 10,
    productoDestacado: null,
    fechaDesde: "2025-06-01",
    fechaHasta: "2025-06-30",
    estado: "Vigente"
  },
  {
    id: "promo-3",
    codigo: "OFERTA_ALFAJOR",
    descripcion: "15% de descuento en Alfajor triple chocolate",
    porcentajeDescuento: 15,
    cantidadMinima: 3,
    productoDestacado: "Alfajor triple chocolate",
    fechaDesde: "2025-06-10",
    fechaHasta: "2025-06-25",
    estado: "Vigente"
  },
  {
    id: "promo-4",
    codigo: "BIENVENIDA",
    descripcion: "Cupón de bienvenida para nuevos clientes registrados",
    porcentajeDescuento: 5,
    cantidadMinima: 1,
    productoDestacado: null,
    fechaDesde: "2025-01-01",
    fechaHasta: "2025-12-31",
    estado: "Vigente"
  },
  {
    id: "promo-5",
    codigo: "INVIERNO15",
    descripcion: "Descuento de temporada invernal",
    porcentajeDescuento: 15,
    cantidadMinima: 5,
    productoDestacado: "Chocolate con maní",
    fechaDesde: "2025-05-01",
    fechaHasta: "2025-05-31",
    estado: "Vencida"
  }
];
