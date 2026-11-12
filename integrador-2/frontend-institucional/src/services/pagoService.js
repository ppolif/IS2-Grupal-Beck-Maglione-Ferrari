import { apiFetch } from './api';

export const pagoService = {
  /**
   * Inicia o regenera el código QR dinámico de Mercado Pago para un pedido.
   */
  async iniciarPagoQr(pedidoId) {
    return apiFetch(`/pedidos/${pedidoId}/pagos/qr/iniciar`, {
      method: 'POST',
    });
  },

  /**
   * Consulta el estado de pago del código QR de un pedido.
   */
  async consultarEstadoQr(pedidoId) {
    return apiFetch(`/pedidos/${pedidoId}/pagos/qr/estado`);
  },

  /**
   * Obtiene los datos oficiales de la cuenta bancaria de la empresa para transferencias.
   */
  async obtenerDatosBancarios() {
    return apiFetch('/pagos/datos-bancarios');
  },

  /**
   * El cliente informa los datos del comprobante de transferencia bancaria realizada.
   */
  async informarTransferencia(pedidoId, data) {
    return apiFetch(`/pedidos/${pedidoId}/pagos/transferencia/informar`, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  /**
   * Cancela y elimina lógicamente un pedido no abonado al salir del checkout o descartar el QR.
   */
  async cancelarPedido(pedidoId, options = {}) {
    return apiFetch(`/pedidos/${pedidoId}`, {
      method: 'DELETE',
      ...options,
    });
  },
};
