/**
 * Servicio para creación, consulta y seguimiento de pedidos en el backend.
 * Conecta con los endpoints /pedidos y sus transiciones de estado.
 */
import { apiFetch } from './api';

export const pedidoService = {
  /**
   * Crea un nuevo pedido en el sistema.
   * @param {Object} data - { puntoDeVentaId, preventorId, codigoPromocion, detalles: [{ productoId, cantidad }] }
   * @returns {Promise<Object>} Pedido creado con su ID y total
   */
  async crearPedido(data) {
    return apiFetch('/pedidos', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  /**
   * Lista los pedidos con filtros opcionales de estado, preventor o distribuidor.
   * @param {Object} params - { estado, preventorId, distribuidorId, page, size }
   * @returns {Promise<Object>}
   */
  async listarPedidos(params = {}) {
    const query = new URLSearchParams();
    if (params.estado) query.append('estado', params.estado);
    if (params.preventorId) query.append('preventorId', params.preventorId);
    if (params.distribuidorId) query.append('distribuidorId', params.distribuidorId);
    if (params.puntoDeVentaId) query.append('puntoDeVentaId', params.puntoDeVentaId);
    if (params.page !== undefined) query.append('page', params.page);
    if (params.size !== undefined) query.append('size', params.size || 50);

    const qs = query.toString();
    const endpoint = `/pedidos${qs ? `?${qs}` : ''}`;
    return apiFetch(endpoint);
  },

  /**
   * Obtiene el detalle completo de un pedido específico.
   * @param {string} id
   * @returns {Promise<Object>}
   */
  async obtenerPedido(id) {
    return apiFetch(`/pedidos/${id}`);
  },

  /**
   * Cancela y elimina lógicamente un pedido no facturado en el backend.
   * @param {string} id
   * @returns {Promise<void>}
   */
  async eliminarPedido(id) {
    return apiFetch(`/pedidos/${id}`, {
      method: 'DELETE',
    });
  },
};
