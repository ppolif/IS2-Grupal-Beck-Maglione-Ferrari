const RAW_API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';
const API_BASE_URL = RAW_API_BASE_URL.replace(/\/+$/, '');

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
  const token = localStorage.getItem('dm_admin_token');
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
    let cleanEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
    if (cleanEndpoint.startsWith('/auth')) {
      cleanEndpoint = `/api/v1${cleanEndpoint}`;
    }
    if (API_BASE_URL.endsWith('/api/v1') && cleanEndpoint.startsWith('/api/v1')) {
      url = `${API_BASE_URL}${cleanEndpoint.substring(7)}`;
    } else {
      url = `${API_BASE_URL}${cleanEndpoint}`;
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
      window.dispatchEvent(new CustomEvent('dm-admin-unauthorized'));
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
