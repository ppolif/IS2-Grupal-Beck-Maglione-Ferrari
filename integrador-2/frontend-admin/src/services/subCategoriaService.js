import { apiFetch } from './api';

export const subCategoriaService = {
  async listar(categoriaId = null) {
    const query = categoriaId ? `?categoriaId=${encodeURIComponent(categoriaId)}` : '';
    return apiFetch(`/subcategorias${query}`, { method: 'GET' });
  },

  async obtener(id) {
    return apiFetch(`/subcategorias/${id}`, { method: 'GET' });
  },

  async crear(datos) {
    return apiFetch('/subcategorias', {
      method: 'POST',
      body: JSON.stringify(datos),
    });
  },

  async modificar(id, datos) {
    return apiFetch(`/subcategorias/${id}`, {
      method: 'PUT',
      body: JSON.stringify(datos),
    });
  },

  async eliminar(id) {
    return apiFetch(`/subcategorias/${id}`, {
      method: 'DELETE',
    });
  },
};
