import { useState } from 'react';
import { paymentsMock } from '../mocks/payments';

export function usePagos() {
  const [payments, setPayments] = useState(paymentsMock);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [periodo, setPeriodo] = useState('Junio 2025');
  const [medio, setMedio] = useState('Todos');

  const [formData, setFormData] = useState({
    pedidoId: 'DM-2084',
    comercio: 'Kiosco El Sol',
    monto: '$45.200',
    medioPago: 'Transferencia Bancaria',
    comprobante: 'TRF-909988'
  });

  const handleRegisterPayment = (e) => {
    e.preventDefault();
    const newPayment = {
      id: `PAG-${Math.floor(4092 + Math.random() * 100)}`,
      pedidoId: formData.pedidoId,
      comercio: formData.comercio,
      monto: formData.monto,
      medioPago: formData.medioPago,
      fecha: '14/06/2025 11:40',
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
    isModalOpen,
    setIsModalOpen,
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
