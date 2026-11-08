import { apiFetch } from './api';

export const vigenciaPrecioService = {
  async listarPorProducto(productoId) {
    return apiFetch(`/productos/${productoId}/vigencias-precio`, { method: 'GET' });
  },

  async registrarPrecio(productoId, precio, fechaDesde = null) {
    const payload = {
      precio: parseFloat(precio),
      fechaDesde: fechaDesde || new Date().toISOString().split('T')[0],
      fechaHasta: null,
    };

    return apiFetch(`/productos/${productoId}/vigencias-precio`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
};
