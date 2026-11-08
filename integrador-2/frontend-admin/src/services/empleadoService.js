import { apiFetch } from './api';

export const empleadoService = {
  async listar(filtros = {}) {
    const params = new URLSearchParams();
    if (filtros.rol) params.append('rol', filtros.rol);
    if (filtros.legajo) params.append('legajo', filtros.legajo);
    if (filtros.q) params.append('q', filtros.q);
    if (filtros.page !== undefined) params.append('page', filtros.page);
    if (filtros.size !== undefined) params.append('size', filtros.size);

    const query = params.toString() ? `?${params.toString()}` : '';
    return apiFetch(`/empleados${query}`, { method: 'GET' });
  },

  async obtener(id) {
    return apiFetch(`/empleados/${id}`, { method: 'GET' });
  },

  async crear(empleadoData) {
    return apiFetch('/empleados', {
      method: 'POST',
      body: JSON.stringify(empleadoData),
    });
  },

  async modificar(id, empleadoData) {
    return apiFetch(`/empleados/${id}`, {
      method: 'PUT',
      body: JSON.stringify(empleadoData),
    });
  },

  async eliminar(id) {
    return apiFetch(`/empleados/${id}`, {
      method: 'DELETE',
    });
  },
};
