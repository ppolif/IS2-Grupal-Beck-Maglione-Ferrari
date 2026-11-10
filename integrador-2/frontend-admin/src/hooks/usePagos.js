import { useState, useEffect, useCallback } from 'react';
import { facturaService } from '../services/facturaService';
import { pedidoService } from '../services/pedidoService';

export function usePagos() {
  const [payments, setPayments] = useState([]);
  const [facturasPendientes, setFacturasPendientes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [periodo, setPeriodo] = useState('Histórico');
  const [medio, setMedio] = useState('Todos');

  const [formData, setFormData] = useState({
    facturaId: '',
    monto: '',
    medioPago: 'Transferencia Bancaria',
    comprobante: '',
  });

  const cargarPagos = useCallback(async () => {
    setLoading(true);
    try {
      const [factData, pedData] = await Promise.all([
        facturaService.listar({ size: 100 }),
        pedidoService.listar({ size: 100 }).catch(() => ({ elementos: [] })),
      ]);

      const listaFact = factData.elementos || factData.content || (Array.isArray(factData) ? factData : []);
      const listaPed = pedData.elementos || pedData.content || (Array.isArray(pedData) ? pedData : []);
      const pedidosMap = {};
      listaPed.forEach((p) => {
        pedidosMap[p.id] = p;
      });

      const pendientes = [];
      const todosPagos = [];

      listaFact.forEach((f) => {
        const pedido = pedidosMap[f.pedidoId];
        const comercio =
          pedido?.puntoDeVenta?.nombreComercio ||
          (pedido?.puntoDeVenta?.minorista
            ? `${pedido.puntoDeVenta.minorista.nombre || ''} ${pedido.puntoDeVenta.minorista.apellido || ''}`.trim()
            : 'Cliente Mayorista');
        const numeroPedido = pedido?.numeroPedido || f.pedidoId || '-';
        const numeroFactura = f.numeroFactura ? `FA-${String(f.numeroFactura).padStart(8, '0')}` : f.id;

        if (f.estado === 'PENDIENTE_PAGO' && Number(f.saldo || 0) > 0) {
          pendientes.push({
            id: f.id,
            numeroFactura,
            numeroPedido,
            comercio,
            saldo: Number(f.saldo || 0),
          });
        }

        (f.pagos || []).forEach((pg) => {
          todosPagos.push({
            id: pg.referencia || pg.id,
            rawId: pg.id,
            pedidoId: numeroPedido,
            comercio,
            monto: `$${Number(pg.monto || 0).toLocaleString('es-AR')}`,
            medioPago: pg.observacion || pg.formaDePago?.tipoPago || 'Transferencia',
            fecha: pg.fechaPago ? new Date(pg.fechaPago).toLocaleString('es-AR') : '-',
            estado: 'Acreditado',
            comprobante: pg.referencia || 'S/N',
          });
        });
      });

      setFacturasPendientes(pendientes);
      setPayments(todosPagos);
      if (pendientes.length > 0) {
        setFormData((prev) => ({
          ...prev,
          facturaId: prev.facturaId || pendientes[0].id,
          monto: prev.monto || String(pendientes[0].saldo),
        }));
      }
    } catch (err) {
      console.error('Error al cargar pagos:', err);
      setPayments([]);
      setFacturasPendientes([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarPagos();
  }, [cargarPagos]);

  const handleRegisterPayment = async (e) => {
    e.preventDefault();
    if (!formData.facturaId) {
      alert('Seleccioná una factura pendiente para imputar el pago.');
      return;
    }
    const montoNum = parseFloat(String(formData.monto).replace(/[^0-9.]/g, '')) || 0;
    if (montoNum <= 0) {
      alert('Ingresá un monto mayor a 0.');
      return;
    }
    try {
      const formaDePagoId = await facturaService.obtenerOCrearFormaDePagoId(formData.medioPago);
      await facturaService.registrarPago(formData.facturaId, {
        formaDePagoId,
        monto: montoNum,
        referencia: formData.comprobante || 'S/N',
        observacion: formData.medioPago,
      });
      alert('Pago registrado con éxito.');
      setIsModalOpen(false);
      setFormData({
        facturaId: '',
        monto: '',
        medioPago: 'Transferencia Bancaria',
        comprobante: '',
      });
      await cargarPagos();
    } catch (err) {
      alert(`Error al registrar pago: ${err.message}`);
    }
  };

  const filtered = payments.filter((p) => {
    if (medio === 'Todos') return true;
    return p.medioPago.toLowerCase().includes(medio.toLowerCase());
  });

  return {
    payments,
    facturasPendientes,
    loading,
    isModalOpen,
    setIsModalOpen,
    periodo,
    setPeriodo,
    medio,
    setMedio,
    formData,
    setFormData,
    handleRegisterPayment,
    filtered,
  };
}
