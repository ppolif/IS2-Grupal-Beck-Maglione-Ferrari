import { apiFetch } from './api';

export const imagenService = {
  async subirImagen(archivo, tipoImagen = 'PRODUCTO') {
    const formData = new FormData();
    formData.append('archivo', archivo);
    formData.append('tipoImagen', tipoImagen);

    return apiFetch('/imagenes', {
      method: 'POST',
      body: formData,
    });
  },

  obtenerUrlContenido(imagenId) {
    if (!imagenId) return null;
    return `/api/v1/imagenes/${imagenId}/contenido`;
  },

  async eliminar(id) {
    return apiFetch(`/imagenes/${id}`, {
      method: 'DELETE',
    });
  },
};
