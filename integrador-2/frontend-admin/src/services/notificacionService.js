import { apiFetch } from './api';

export const notificacionService = {
  enviarNewsletter: async () => {
    return apiFetch('/notificaciones/newsletter/enviar', { method: 'POST' });
  },
};
