import { apiFetch } from './api';

export const mayoristaService = {
  async listar() {
    return apiFetch('/mayoristas', { method: 'GET' });
  },

  async obtener(id) {
    return apiFetch(`/mayoristas/${id}`, { method: 'GET' });
  },
};
