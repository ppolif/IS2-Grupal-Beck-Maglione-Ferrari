import { apiFetch } from './api';

export const productoService = {
  async listar(filtros = {}) {
    const params = new URLSearchParams();
    if (filtros.q) params.append('q', filtros.q);
    if (filtros.categoriaId) params.append('categoriaId', filtros.categoriaId);
    if (filtros.subCategoriaId) params.append('subCategoriaId', filtros.subCategoriaId);
    if (filtros.enOferta !== undefined && filtros.enOferta !== '') params.append('enOferta', filtros.enOferta);
    if (filtros.soloConStock !== undefined && filtros.soloConStock !== '') params.append('soloConStock', filtros.soloConStock);
    if (filtros.page !== undefined) params.append('page', filtros.page);
    if (filtros.size !== undefined) params.append('size', filtros.size);

    const query = params.toString() ? `?${params.toString()}` : '';
    return apiFetch(`/productos${query}`, { method: 'GET' });
  },

  async obtener(id) {
    return apiFetch(`/productos/${id}`, { method: 'GET' });
  },

  async crear(productoData) {
    return apiFetch('/productos', {
      method: 'POST',
      body: JSON.stringify(productoData),
    });
  },

  async modificar(id, productoData) {
    return apiFetch(`/productos/${id}`, {
      method: 'PUT',
      body: JSON.stringify(productoData),
    });
  },

  async eliminar(id) {
    return apiFetch(`/productos/${id}`, {
      method: 'DELETE',
    });
  },
};
