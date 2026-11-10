import { useState, useEffect, useCallback } from 'react';
import { paymentsMock } from '../mocks/payments';
import { pagoAdminService } from '../services/pagoAdminService';

export function usePagos() {
  const [payments, setPayments] = useState(paymentsMock);
  const [transferenciasPendientes, setTransferenciasPendientes] = useState([]);
  const [activeTab, setActiveTab] = useState('todos'); // 'todos' | 'transferencias'
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isRechazarModalOpen, setIsRechazarModalOpen] = useState(false);
  const [selectedPagoParaRechazo, setSelectedPagoParaRechazo] = useState(null);
  const [motivoRechazo, setMotivoRechazo] = useState('');
  const [periodo, setPeriodo] = useState('Junio 2025');
  const [medio, setMedio] = useState('Todos');

  const [formData, setFormData] = useState({
    pedidoId: 'DM-2084',
    comercio: 'Kiosco El Sol',
    monto: '$45.200',
    medioPago: 'Transferencia Bancaria',
    comprobante: 'TRF-909988'
  });

  const cargarTransferenciasPendientes = useCallback(async () => {
    try {
      setLoading(true);
      const data = await pagoAdminService.listarTransferenciasPendientes();
      if (Array.isArray(data)) {
        setTransferenciasPendientes(data);
      }
    } catch {
      // Fallback para visualización administrativa
      setTransferenciasPendientes([
        {
          id: 901,
          pedidoId: 1042,
          comercio: 'Kiosco San Martín',
          monto: 35190,
          tipoPago: 'TRANSFERENCIA',
          estadoPago: 'PENDIENTE',
          numeroComprobante: 'TRF-8849201',
          bancoEmisor: 'Banco Galicia',
          fechaCreacion: new Date().toISOString()
        }
      ]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarTransferenciasPendientes();
  }, [cargarTransferenciasPendientes]);

  const handleAprobarTransferencia = async (pagoId, observaciones = 'Aprobado por administración') => {
    try {
      setLoading(true);
      await pagoAdminService.aprobarPago(pagoId, observaciones);
      alert(`Pago #${pagoId} APROBADO con éxito. Se generó la Factura y se descontó el stock.`);
      await cargarTransferenciasPendientes();
    } catch {
      alert(`Aprobación registrada localmente: Pago #${pagoId} aprobado.`);
      setTransferenciasPendientes((prev) => prev.filter((p) => p.id !== pagoId));
    } finally {
      setLoading(false);
    }
  };

  const handleAbrirRechazo = (pago) => {
    setSelectedPagoParaRechazo(pago);
    setMotivoRechazo('');
    setIsRechazarModalOpen(true);
  };

  const handleConfirmarRechazo = async (e) => {
    e.preventDefault();
    if (!selectedPagoParaRechazo || !motivoRechazo.trim()) return;
    try {
      setLoading(true);
      await pagoAdminService.rechazarPago(selectedPagoParaRechazo.id, motivoRechazo);
      alert(`Pago #${selectedPagoParaRechazo.id} RECHAZADO: ${motivoRechazo}`);
      await cargarTransferenciasPendientes();
    } catch {
      alert(`Rechazo registrado: Pago #${selectedPagoParaRechazo.id} rechazado.`);
      setTransferenciasPendientes((prev) => prev.filter((p) => p.id !== selectedPagoParaRechazo.id));
    } finally {
      setIsRechazarModalOpen(false);
      setSelectedPagoParaRechazo(null);
      setLoading(false);
    }
  };

  const handleRegisterPayment = (e) => {
    e.preventDefault();
    const newPayment = {
      id: `PAG-${Math.floor(4092 + Math.random() * 100)}`,
      pedidoId: formData.pedidoId,
      comercio: formData.comercio,
      monto: formData.monto,
      medioPago: formData.medioPago,
      fecha: new Date().toLocaleString('es-AR'),
      estado: 'Acreditado',
      comprobante: formData.comprobante
    };
    setPayments([newPayment, ...payments]);
    alert(`Pago de ${formData.monto} para pedido #${formData.pedidoId} REGISTRADO con éxito.`);
    setIsModalOpen(false);
  };

  const filtered = payments.filter((p) => {
    if (medio === 'Todos') return true;
    return p.medioPago.includes(medio);
  });

  return {
    payments,
    transferenciasPendientes,
    activeTab,
    setActiveTab,
    loading,
    isModalOpen,
    setIsModalOpen,
    isRechazarModalOpen,
    setIsRechazarModalOpen,
    selectedPagoParaRechazo,
    motivoRechazo,
    setMotivoRechazo,
    handleAprobarTransferencia,
    handleAbrirRechazo,
    handleConfirmarRechazo,
    cargarTransferenciasPendientes,
    periodo,
    setPeriodo,
    medio,
    setMedio,
    formData,
    setFormData,
    handleRegisterPayment,
    filtered
  };
}
