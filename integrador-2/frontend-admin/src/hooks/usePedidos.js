import { useState, useEffect, useCallback } from 'react';
import { pedidoService } from '../services/pedidoService';
import { facturaService } from '../services/facturaService';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

/**
 * Custom hook managing reception orders, invoice state and detail modal.
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
      const listaApi = data.elementos || data.content || (Array.isArray(data) ? data : []);

      if (Array.isArray(listaApi) && listaApi.length > 0) {
        const formateados = listaApi.map((p) => {
          const comercio = p.puntoDeVenta?.nombreComercio || 'Punto de Venta';
          const preventorNombre = p.preventor
            ? `${p.preventor.nombre || ''} ${p.preventor.apellido || ''}`.trim()
            : 'Venta Directa / Web';
          const esEfectivo = p.tipoPago === 'EFECTIVO';
          const facturado = Boolean(p.facturaId) || p.estado === 'FACTURADO' || p.estado === 'ENTREGADO';
          const medioPagoLabel =
            p.tipoPago === 'EFECTIVO'
              ? 'Efectivo contra entrega'
              : p.tipoPago === 'BILLETERA_VIRTUAL'
              ? 'Billetera Virtual (QR)'
              : p.tipoPago === 'TRANSFERENCIA'
              ? 'Transferencia Bancaria'
              : 'Sin especificar';

          const items = (p.detalles || []).map((d) => ({
            nombre: d.productoNombre || d.producto?.nombre || 'Producto',
            cant: d.cantidad || 1,
            sub: `$${(d.subtotal || 0).toLocaleString('es-AR')}`,
          }));

          return {
            id: p.id,
            numeroPedido: p.numeroPedido || p.id,
            comercio,
            preventor: preventorNombre,
            monto: `$${(p.total || 0).toLocaleString('es-AR')}`,
            montoNum: p.total || 0,
            estado: p.estado,
            tipoPago: p.tipoPago || null,
            medioPago: medioPagoLabel,
            esEfectivo,
            facturado,
            facturaId: p.facturaId,
            fecha: p.fechaHoraCarga ? new Date(p.fechaHoraCarga).toLocaleString('es-AR') : '',
            items,
          };
        });
        setOrders(formateados);
      } else {
        setOrders([]);
      }
    } catch (err) {
      console.error('Error al obtener pedidos de la API:', err);
      setError(err.message);
      setOrders([]);
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
      await pedidoService.procesar(orderId);
      alert(`¡Pedido facturado con éxito! Se generó la factura y se despachó el PDF al correo del cliente.`);
      await cargarPedidos();
      setIsDetailModalOpen(false);
    } catch (err) {
      alert(`No se pudo facturar el pedido: ${err.message}`);
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

  const handleDescargarFacturaPdf = async (facturaId) => {
    if (!facturaId) {
      alert('Este pedido no posee factura asociada.');
      return;
    }
    try {
      const token = localStorage.getItem('dm_admin_token');
      const res = await fetch(`${API_BASE_URL}/facturas/${facturaId}/pdf`, {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `factura-${facturaId}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert(`No se pudo descargar el PDF de la factura: ${err.message}`);
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
    handleDescargarFacturaPdf,
    recargar: cargarPedidos,
  };
}
