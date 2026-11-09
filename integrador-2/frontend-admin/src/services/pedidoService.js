import { apiFetch } from './api';

export const pedidoService = {
  listar: async (params = {}) => {
    const query = new URLSearchParams();
    if (params.estado) query.append('estado', params.estado);
    if (params.page !== undefined) query.append('page', params.page);
    if (params.size !== undefined) query.append('size', params.size);

    const qs = query.toString() ? `?${query.toString()}` : '';
    return apiFetch(`/pedidos${qs}`, { method: 'GET' });
  },

  buscarPorId: async (id) => {
    return apiFetch(`/pedidos/${id}`, { method: 'GET' });
  },

  procesar: async (id) => {
    return apiFetch(`/pedidos/${id}/procesar`, { method: 'POST' });
  },
};
