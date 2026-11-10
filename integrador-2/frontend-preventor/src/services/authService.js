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
    // En Dulce Mayor, los roles son PREVENTISTA o DISTRIBUIDOR
    if (rol !== 'PREVENTISTA' && rol !== 'DISTRIBUIDOR') {
      throw new ApiError(
        403,
        'ROL_NO_AUTORIZADO',
        `Acceso denegado: El usuario "${nombreUsuario}" tiene rol "${rol}". Este portal es exclusivo para Preventistas y Distribuidores.`
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
