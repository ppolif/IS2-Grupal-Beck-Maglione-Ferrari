import { apiFetch } from './api';

const STORAGE_KEY = 'dm_cliente_pedidos';

export const pedidoClienteService = {
  /**
   * Obtiene la lista de pedidos persistidos localmente del cliente minorista.
   */
  obtenerPedidos() {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (!stored) {
        return [];
      }
      const parsed = JSON.parse(stored);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  },

  /**
   * Registra y persiste un pedido confirmado por el cliente.
   */
  guardarPedido(nuevoPedido) {
    try {
      const pedidosActuales = this.obtenerPedidos();
      const actualizados = [
        nuevoPedido,
        ...pedidosActuales.filter(
          (p) => p.id !== nuevoPedido.id && (!nuevoPedido.rawId || p.rawId !== nuevoPedido.rawId)
        ),
      ];
      localStorage.setItem(STORAGE_KEY, JSON.stringify(actualizados));
      window.dispatchEvent(new CustomEvent('dm-pedido-actualizado', { detail: nuevoPedido }));
      return nuevoPedido;
    } catch (err) {
      console.error('Error al persistir pedido localmente:', err);
      return nuevoPedido;
    }
  },

  /**
   * Elimina un pedido del almacenamiento local por id o rawId.
   */
  eliminarPedidoLocal(pedidoId) {
    if (!pedidoId) return;
    try {
      const pedidosActuales = this.obtenerPedidos();
      const actualizados = pedidosActuales.filter(
        (p) => p.id !== pedidoId && p.rawId !== pedidoId && p.orderId !== pedidoId
      );
      localStorage.setItem(STORAGE_KEY, JSON.stringify(actualizados));
      window.dispatchEvent(new CustomEvent('dm-pedido-actualizado'));
    } catch (err) {
      console.error('Error al eliminar pedido localmente:', err);
    }
  },

  /**
   * Consulta los pedidos del cliente en el backend si hay sesión activa.
   */
  async sincronizarConBackend() {
    try {
      const response = await apiFetch('/mi-cuenta/pedidos');
      if (response && response.content && Array.isArray(response.content)) {
        return response.content;
      }
    } catch {
      // Fallback silencioso a almacenamiento local
    }
    return this.obtenerPedidos();
  },
};
