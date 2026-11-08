import { apiFetch, ApiError } from './api';

export const authService = {
  async login(nombreUsuario, clave) {
    const response = await apiFetch('/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        nombreUsuario: nombreUsuario.trim(),
        clave,
      }),
    });

    const rol = response?.usuario?.rol;
    if (rol !== 'JEFE' && rol !== 'ADMINISTRATIVO') {
      throw new ApiError(
        403,
        'ROL_NO_AUTORIZADO',
        `Acceso denegado: El usuario "${nombreUsuario}" tiene rol "${rol}". Este panel es exclusivo para Jefatura y Personal Administrativo.`
      );
    }

    return response;
  },

  async obtenerPerfil() {
    return apiFetch('/auth/perfil', {
      method: 'GET',
    });
  },

  async cambiarClave(claveActual, nuevaClave, confirmarClave) {
    return apiFetch('/auth/cambiar-clave', {
      method: 'POST',
      body: JSON.stringify({ claveActual, nuevaClave, confirmarClave }),
    });
  },
};
