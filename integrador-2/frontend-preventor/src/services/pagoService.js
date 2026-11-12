/**
 * Servicio de operaciones de pago para el portal de Preventores.
 * Conecta con PagoOperacionController (/api/v1/pedidos/... y /api/v1/pagos/...).
 */
import { apiFetch } from './api';

export const pagoService = {
  /**
   * Inicia o regenera el código QR dinámico de Mercado Pago para un pedido.
   * @param {string} pedidoId
   * @returns {Promise<Object>} IniciarPagoQrResponse
   */
  async iniciarPagoQr(pedidoId) {
    return apiFetch(`/pedidos/${pedidoId}/pagos/qr/iniciar`, {
      method: 'POST',
    });
  },

  /**
   * Consulta el estado de acreditación del pago QR de un pedido.
   * @param {string} pedidoId
   * @returns {Promise<Object>} IniciarPagoQrResponse
   */
  async consultarEstadoQr(pedidoId) {
    return apiFetch(`/pedidos/${pedidoId}/pagos/qr/estado`);
  },

  /**
   * Obtiene los datos bancarios oficiales de la empresa para transferencias.
   * @returns {Promise<Object>}
   */
  async obtenerDatosBancarios() {
    return apiFetch('/pagos/datos-bancarios');
  },

  /**
   * Informa el comprobante de una transferencia bancaria realizada por el comercio.
   * @param {string} pedidoId
   * @param {Object} data - { comprobante, bancoEmisor, observacion }
   * @returns {Promise<void>}
   */
  async informarTransferencia(pedidoId, data) {
    return apiFetch(`/pedidos/${pedidoId}/pagos/transferencia/informar`, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },
};
