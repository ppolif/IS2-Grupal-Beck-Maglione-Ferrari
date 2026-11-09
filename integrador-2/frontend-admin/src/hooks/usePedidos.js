import { useState, useEffect, useCallback } from 'react';
import { pedidoService } from '../services/pedidoService';
import { facturaService } from '../services/facturaService';
import { ordersMock } from '../mocks/orders';

/**
 * Custom hook managing reception orders, invoice state and detail modal.
 * Se conecta con la API del backend y recurre al mock si la API no está disponible o no hay datos.
 */
export function usePedidos() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);

  const cargarPedidos = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await pedidoService.listar({ size: 50 });
      const listaApi = data.elementos || data.content || data;

      if (Array.isArray(listaApi) && listaApi.length > 0) {
        const formateados = listaApi.map((p) => {
          const comercio = p.puntoDeVenta?.nombreComercio || 'Punto de Venta';
          const preventorNombre = p.preventor
            ? `${p.preventor.nombre || ''} ${p.preventor.apellido || ''}`.trim()
            : 'Venta Directa / Web';
          const facturado = p.estado === 'FACTURADO' || p.estado === 'EN_REPARTO' || p.estado === 'ENTREGADO';

          const items = (p.detalles || []).map((d) => ({
            nombre: d.productoNombre || d.producto?.nombre || 'Producto',
            cant: d.cantidad || 1,
            sub: `$${(d.subtotal || 0).toLocaleString('es-AR')}`,
          }));

          return {
            id: p.id,
            numeroPedido: p.numeroPedido,
            comercio,
            preventor: preventorNombre,
            monto: `$${(p.total || 0).toLocaleString('es-AR')}`,
            montoNum: p.total || 0,
            estado: p.estado,
            facturado,
            facturaId: p.facturaId,
            fecha: p.fechaHoraCarga ? new Date(p.fechaHoraCarga).toLocaleString('es-AR') : '',
            items,
          };
        });
        setOrders(formateados);
      } else {
        // Fallback a mocks si la BD aún está limpia
        setOrders(ordersMock);
      }
    } catch (err) {
      console.warn('No se pudieron obtener pedidos de la API, usando mock:', err);
      setOrders(ordersMock);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarPedidos();
  }, [cargarPedidos]);

  const handleOpenDetail = (order) => {
    setSelectedOrder(order);
    setIsDetailModalOpen(true);
  };

  const handleInvoiceOrder = async (orderId) => {
    setActionLoading(true);
    try {
      // Intentar procesar en backend si es un UUID o pedido real
      await pedidoService.procesar(orderId);
      alert(`¡Pedido #${orderId} FACTURADO con éxito! Se despachó la factura en PDF al correo del cliente.`);
      await cargarPedidos();
      setIsDetailModalOpen(false);
    } catch (err) {
      console.warn('Error al procesar en API:', err);
      // Actualización local de contingencia
      setOrders((prev) =>
        prev.map((o) =>
          o.id === orderId ? { ...o, facturado: true, estado: 'FACTURADO' } : o
        )
      );
      if (selectedOrder && selectedOrder.id === orderId) {
        setSelectedOrder({ ...selectedOrder, facturado: true, estado: 'FACTURADO' });
      }
      alert(`Pedido #${orderId} facturado (Aviso: ${err.message || 'operación local'})`);
    } finally {
      setActionLoading(false);
    }
  };

  const handleReenviarFactura = async (facturaId) => {
    if (!facturaId) {
      alert('Este pedido no tiene aún una factura vinculada para reenviar.');
      return;
    }
    try {
      await facturaService.reenviarCorreo(facturaId);
      alert('¡Factura reenviada por correo electrónico con éxito!');
    } catch (err) {
      alert(`Error al reenviar factura: ${err.message}`);
    }
  };

  return {
    orders,
    loading,
    error,
    selectedOrder,
    isDetailModalOpen,
    actionLoading,
    setIsDetailModalOpen,
    handleOpenDetail,
    handleInvoiceOrder,
    handleReenviarFactura,
    recargar: cargarPedidos,
  };
}
