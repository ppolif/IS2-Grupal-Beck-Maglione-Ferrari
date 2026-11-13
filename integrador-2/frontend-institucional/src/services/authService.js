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

  async loginExterno(proveedor, tokenExterno) {
    return apiFetch('/auth/login-externo', {
      method: 'POST',
      body: JSON.stringify({ proveedor, tokenExterno }),
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

  async completarDni(numeroDocumento, confirmarNumeroDocumento) {
    return apiFetch('/mi-cuenta/completar-dni', {
      method: 'POST',
      body: JSON.stringify({ numeroDocumento, confirmarNumeroDocumento }),
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

