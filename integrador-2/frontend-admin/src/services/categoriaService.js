import { apiFetch } from './api';

export const categoriaService = {
  async listar() {
    return apiFetch('/categorias', { method: 'GET' });
  },

  async obtener(id) {
    return apiFetch(`/categorias/${id}`, { method: 'GET' });
  },

  async crear(datos) {
    return apiFetch('/categorias', {
      method: 'POST',
      body: JSON.stringify(datos),
    });
  },

  async modificar(id, datos) {
    return apiFetch(`/categorias/${id}`, {
      method: 'PUT',
      body: JSON.stringify(datos),
    });
  },

  async eliminar(id) {
    return apiFetch(`/categorias/${id}`, {
      method: 'DELETE',
    });
  },
};
