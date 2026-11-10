import { apiFetch } from './api';

export const pagoAdminService = {
  /**
   * Obtiene la lista de transferencias bancarias pendientes de conciliación.
   */
  async listarTransferenciasPendientes() {
    return apiFetch('/pagos/transferencias-pendientes', { method: 'GET' });
  },

  /**
   * Aprueba manualmente un pago por transferencia bancaria (disparando facturación y descuento de stock).
   */
  async aprobarPago(pagoId, observaciones = '') {
    return apiFetch(`/pagos/${pagoId}/aprobar`, {
      method: 'POST',
      body: JSON.stringify({ observaciones }),
    });
  },

  /**
   * Rechaza o anula un pago con motivo explícito.
   */
  async rechazarPago(pagoId, motivo) {
    return apiFetch(`/pagos/${pagoId}/rechazar`, {
      method: 'POST',
      body: JSON.stringify({ motivo }),
    });
  },

  /**
   * Obtiene el historial de pagos asociados a un pedido específico.
   */
  async listarPagosPorPedido(pedidoId) {
    return apiFetch(`/pedidos/${pedidoId}/pagos`, { method: 'GET' });
  },
};
