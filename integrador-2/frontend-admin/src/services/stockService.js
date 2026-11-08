import { apiFetch } from './api';

export const stockService = {
  async buscarPorProducto(productoId) {
    return apiFetch(`/productos/${productoId}/stock`, { method: 'GET' });
  },

  async reponerStock(productoId, cantidad, observacion = '') {
    const payload = {
      cantidad: parseInt(cantidad, 10),
      observacion: observacion.trim() || 'Reposición desde panel administrativo',
    };

    return apiFetch(`/productos/${productoId}/stock/reposiciones`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  async modificarStock(productoId, cantidadActual, observacion = '') {
    const payload = {
      cantidadActual: parseInt(cantidadActual, 10),
      observacion: observacion.trim() || 'Ajuste de inventario',
    };

    return apiFetch(`/productos/${productoId}/stock`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },

  async listarStock(filtros = {}) {
    const params = new URLSearchParams();
    if (filtros.q) params.append('q', filtros.q);
    if (filtros.soloSinStock) params.append('soloSinStock', filtros.soloSinStock);
    if (filtros.page !== undefined) params.append('page', filtros.page);
    if (filtros.size !== undefined) params.append('size', filtros.size);

    const query = params.toString() ? `?${params.toString()}` : '';
    return apiFetch(`/stock${query}`, { method: 'GET' });
  },
};
