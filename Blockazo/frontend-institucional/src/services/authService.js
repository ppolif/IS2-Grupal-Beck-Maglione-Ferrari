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

  async loginSocial(token) {
    return apiFetch('/auth/social', {
      method: 'POST',
      body: JSON.stringify({ token }),
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

  async obtenerPaises() {
    return apiFetch('/paises', { method: 'GET' });
  },

  async obtenerProvincias(paisId) {
    return apiFetch(`/provincias?paisId=${encodeURIComponent(paisId)}`, { method: 'GET' });
  },

  async obtenerDepartamentos(provinciaId) {
    return apiFetch(`/departamentos?provinciaId=${encodeURIComponent(provinciaId)}`, { method: 'GET' });
  },

  async obtenerLocalidades(departamentoId) {
    return apiFetch(`/localidades?departamentoId=${encodeURIComponent(departamentoId)}`, { method: 'GET' });
  },
};
