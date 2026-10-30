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
  const token = localStorage.getItem('dm_institucional_token');
  const headers = {
    'Content-Type': 'application/json',
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
      // Disparar evento para que la sesión local se limpie si expiró el token
      window.dispatchEvent(new CustomEvent('dm-auth-unauthorized'));
    }

    if (isJson && data) {
      throw new ApiError(
        data.status || response.status,
        data.code || 'ERROR',
        data.message || 'Ha ocurrido un error en la solicitud',
        data.path,
        data.errors || []
      );
    }
    throw new ApiError(response.status, 'HTTP_ERROR', response.statusText || 'Error en la solicitud');
  }

  return data;
}

