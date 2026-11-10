/**
 * Servicio para consulta y registro de Clientes Minoristas y Puntos de Venta.
 * Conecta con los endpoints /puntos-de-venta y /minoristas del backend.
 */
import { apiFetch } from './api';

export const comercioService = {
  /**
   * Lista los puntos de venta (comercios físicos).
   * Permite filtrar opcionalmente por búsqueda de texto o preventor asignado.
   * @param {Object} params - { q, preventorId, page, size }
   * @returns {Promise<Object>} Respuesta paginada con items en content
   */
  async listarPuntosDeVenta(params = {}) {
    const query = new URLSearchParams();
    if (params.q) query.append('q', params.q);
    if (params.preventorId) query.append('preventorId', params.preventorId);
    if (params.page !== undefined) query.append('page', params.page);
    if (params.size !== undefined) query.append('size', params.size || 50);

    const qs = query.toString();
    const endpoint = `/puntos-de-venta${qs ? `?${qs}` : ''}`;
    return apiFetch(endpoint);
  },

  /**
   * Obtiene el detalle de un punto de venta por ID.
   * @param {string} id
   * @returns {Promise<Object>}
   */
  async obtenerPuntoDeVenta(id) {
    return apiFetch(`/puntos-de-venta/${id}`);
  },

  /**
   * Registra un nuevo comercio minorista junto a sus direcciones y contactos.
   * @param {Object} minoristaData - Datos del formulario de alta
   * @returns {Promise<Object>}
   */
  async registrarMinorista(minoristaData) {
    return apiFetch('/minoristas', {
      method: 'POST',
      body: JSON.stringify(minoristaData),
    });
  },

  /**
   * Registra un punto de venta asociado a un cliente minorista existente.
   * @param {Object} pdvData - { nombreComercio, minoristaId, direccion, preventorId }
   * @returns {Promise<Object>}
   */
  async crearPuntoDeVenta(pdvData) {
    return apiFetch('/puntos-de-venta', {
      method: 'POST',
      body: JSON.stringify(pdvData),
    });
  }
};
