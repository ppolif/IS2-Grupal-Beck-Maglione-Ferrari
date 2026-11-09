import { apiFetch } from './api';

export const facturaService = {
  listar: async (params = {}) => {
    const query = new URLSearchParams();
    if (params.estado) query.append('estado', params.estado);
    if (params.minoristaId) query.append('minoristaId', params.minoristaId);
    if (params.page !== undefined) query.append('page', params.page);
    if (params.size !== undefined) query.append('size', params.size);

    const qs = query.toString() ? `?${query.toString()}` : '';
    return apiFetch(`/facturas${qs}`, { method: 'GET' });
  },

  buscarPorId: async (id) => {
    return apiFetch(`/facturas/${id}`, { method: 'GET' });
  },

  reenviarCorreo: async (id) => {
    return apiFetch(`/facturas/${id}/reenviar-correo`, { method: 'POST' });
  },
};
