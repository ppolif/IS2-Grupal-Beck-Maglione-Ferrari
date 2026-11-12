const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

export class ApiError extends Error {
  constructor(status, code, message, path, errors = []) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.path = path;
    this.errors = errors;
  }
}

export async function apiFetch(endpoint, options = {}) {
  const token = localStorage.getItem('dm_preventor_token');
  const isFormData = options.body instanceof FormData;

  const headers = {
    ...(!isFormData && { 'Content-Type': 'application/json' }),
    ...(options.headers || {}),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  // Normalizar endpoint asegurando prefijo /api/v1 si no viene incluido
  let url = endpoint;
  if (!url.startsWith('http')) {
    if (!url.startsWith('/api/v1') && !url.startsWith('/auth')) {
      url = `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`;
    } else if (url.startsWith('/auth')) {
      url = `/api/v1${url}`;
    }
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type') || '';
  const isJson = contentType.includes('application/json');
  const data = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    if (response.status === 401) {
      window.dispatchEvent(new CustomEvent('dm-preventor-unauthorized'));
    }

    if (isJson && data) {
      const code = data.codigo || data.code || 'ERROR';
      let message = data.mensaje || data.message || 'Error en la solicitud';
      if (code === 'CAMBIO_CLAVE_REQUERIDO' || (response.status === 403 && typeof message === 'string' && message.toLowerCase().includes('clave'))) {
        message = 'Cambie su clave para acceder';
        window.dispatchEvent(new CustomEvent('dm-preventor-cambio-clave-requerido'));
      }
      const path = data.path || endpoint;
      const errors = data.errores || data.errors || [];
      throw new ApiError(response.status, code, message, path, errors);
    }

    throw new ApiError(
      response.status,
      'HTTP_ERROR',
      typeof data === 'string' && data ? data : response.statusText,
      endpoint
    );
  }

  return data;
}
