import { useState } from 'react';
import { ordersMock } from '../mocks/orders';

/**
 * Custom hook managing reception orders, invoice state and detail modal.
 */
export function usePedidos() {
  const [orders, setOrders] = useState(ordersMock);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);

  const handleOpenDetail = (order) => {
    setSelectedOrder(order);
    setIsDetailModalOpen(true);
  };

  const handleInvoiceOrder = (orderId) => {
    setOrders((prev) =>
      prev.map((o) =>
        o.id === orderId ? { ...o, facturado: true, estado: 'Facturado' } : o
      )
    );
    if (selectedOrder && selectedOrder.id === orderId) {
      setSelectedOrder({ ...selectedOrder, facturado: true, estado: 'Facturado' });
    }
    alert(`Pedido #${orderId} FACTURADO y preparado para despacho.`);
  };

  return {
    orders,
    selectedOrder,
    isDetailModalOpen,
    setIsDetailModalOpen,
    handleOpenDetail,
    handleInvoiceOrder,
  };
}
