import { apiFetch } from './api';

export const authService = {
  async registrarMinorista(datos) {
    return apiFetch('/auth/registro', {
      method: 'POST',
      body: JSON.stringify(datos),
    });
  },

  async login(nombreUsuario, clave) {
    return apiFetch('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ nombreUsuario, clave }),
    });
  },

  async obtenerMiPerfil() {
    return apiFetch('/mi-cuenta/perfil', {
      method: 'GET',
    });
  },

  async actualizarMiPerfil(datos) {
    return apiFetch('/mi-cuenta/perfil', {
      method: 'PUT',
      body: JSON.stringify(datos),
    });
  },

  async cambiarClave(claveActual, nuevaClave, confirmarClave) {
    return apiFetch('/auth/cambiar-clave', {
      method: 'POST',
      body: JSON.stringify({ claveActual, nuevaClave, confirmarClave }),
    });
  },

  async eliminarMiCuenta() {
    return apiFetch('/mi-cuenta/perfil', {
      method: 'DELETE',
    });
  },

  async listarMisPuntosDeVenta() {
    return apiFetch('/puntos-de-venta?size=50', {
      method: 'GET',
    });
  },

  async crearPuntoDeVenta(datos) {
    return apiFetch('/puntos-de-venta', {
      method: 'POST',
      body: JSON.stringify(datos),
    });
  },

  async listarLocalidades(departamentoId = '') {
    const q = departamentoId ? `?departamentoId=${encodeURIComponent(departamentoId)}` : '';
    return apiFetch(`/localidades${q}`, {
      method: 'GET',
    });
  },
};

