/**
 * Carrito de Compras y Cierre de Pedido para el Preventista.
 * Conectado con CartContext y el endpoint /api/v1/pedidos del backend.
 */
import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import { useCart } from '../../context/CartContext';
import { DATOS_BANCARIOS } from '../../utils/paymentConstants';

export default function Carrito() {
  const {
    items,
    clienteActual,
    codigoPromo,
    setCodigoPromo,
    subtotal,
    actualizarCantidad,
    removerItem,
    confirmarPedido,
  } = useCart();

  const [confirmed, setConfirmed] = useState(false);
  const [confirmedOrderData, setConfirmedOrderData] = useState(null);
  const [promoCodeInput, setPromoCodeInput] = useState(codigoPromo || '');
  const [appliedPromo, setAppliedPromo] = useState(null);
  const [promoError, setPromoError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);
  const navigate = useNavigate();

  // Método de pago: 'mercadopago' | 'transferencia' | 'efectivo'
  const [paymentMethod, setPaymentMethod] = useState('efectivo');

  // Estado para Transferencia Bancaria
  const [transferenciaData, setTransferenciaData] = useState({
    numeroComprobante: '',
    bancoEmisor: 'Banco Santander',
    confirmadoPorCliente: true,
  });
  const [copiadoKey, setCopiadoKey] = useState(null);

  // Estado para QR de Mercado Pago
  const [qrTimeLeft, setQrTimeLeft] = useState(900); // 15 minutos en segundos
  const [qrSimuladoAprobado, setQrSimuladoAprobado] = useState(false);

  useEffect(() => {
    if (paymentMethod !== 'mercadopago') return;
    const timer = setInterval(() => {
      setQrTimeLeft((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => clearInterval(timer);
  }, [paymentMethod]);

  const formatTimer = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const handleCopy = (text, key) => {
    navigator.clipboard?.writeText(text);
    setCopiadoKey(key);
    setTimeout(() => setCopiadoKey(null), 2000);
  };

  const handleApplyPromo = (e) => {
    e.preventDefault();
    const clean = promoCodeInput.trim().toUpperCase();
    if (!clean) return;

    setCodigoPromo(clean);
    setAppliedPromo({ code: clean, descripcion: `Código aplicado: ${clean}` });
    setPromoError('');
  };

  const handleRemovePromo = () => {
    setCodigoPromo('');
    setAppliedPromo(null);
    setPromoCodeInput('');
  };

  const total = subtotal;

  const handleConfirmOrder = async () => {
    if (!clienteActual) {
      alert('Por favor seleccioná o verificá el comercio al que le estás tomando el pedido en la sección "Comercios".');
      navigate('/comercios');
      return;
    }

    if (items.length === 0) {
      alert('El carrito está vacío. Agregá productos antes de confirmar.');
      return;
    }

    if (paymentMethod === 'transferencia' && !transferenciaData.numeroComprobante.trim()) {
      alert('Por favor ingresá el número de comprobante o transacción de la transferencia bancaria realizada por el cliente.');
      return;
    }

    try {
      setIsSubmitting(true);
      setSubmitError(null);

      // Llamada real al backend para registrar la orden
      const nuevoPedido = await confirmarPedido();

      const orderPayload = {
        orderId: nuevoPedido?.id || `ORD-${Date.now().toString().slice(-4)}`,
        cliente: clienteActual.nombreComercio || 'Comercio Minorista',
        direccion: clienteActual.direccion
          ? `${clienteActual.direccion.calle || ''} ${clienteActual.direccion.numero || ''}`
          : 'Mendoza',
        total: nuevoPedido?.total || total,
        medioPago:
          paymentMethod === 'mercadopago'
            ? 'Mercado Pago QR (Escaneado en mostrador)'
            : paymentMethod === 'transferencia'
            ? 'Transferencia Bancaria'
            : 'Efectivo contra entrega',
        estadoPago:
          paymentMethod === 'mercadopago'
            ? 'Aprobado automáticamente'
            : paymentMethod === 'transferencia'
            ? 'Marcado como pago por preventor (Comprobante informado)'
            : 'Pendiente de cobro por distribuidor en entrega',
        comprobante: transferenciaData.numeroComprobante || null,
        fecha: new Date().toLocaleDateString('es-AR') + ' ' + new Date().toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' }),
      };

      setConfirmedOrderData(orderPayload);
      setConfirmed(true);
    } catch (err) {
      console.error('Error al registrar pedido:', err);
      setSubmitError(err.message || 'Error al procesar el pedido con el servidor.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-xl mx-auto space-y-6">
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Pedido en Curso
        </span>
        <h1 className="text-2xl font-bold text-[#2b2b3a] mt-1">
          Carga de Pedido de Preventa
        </h1>
        <p className="text-[12px] text-[#6b6b80] mt-0.5">
          Comercio:{' '}
          {clienteActual ? (
            <strong className="text-[#2b2b3a]">{clienteActual.nombreComercio}</strong>
          ) : (
            <Link to="/comercios" className="text-[#e86bc4] font-bold underline">
              Seleccionar comercio asignado →
            </Link>
          )}
        </p>
      </div>

      <div className="bg-white rounded-[22px] p-5 sm:p-6 border border-[#ececf2] shadow-sm">
        {confirmed ? (
          <div className="p-6 bg-[#dff8e9] rounded-[20px] text-[#235839] space-y-4">
            <div className="w-12 h-12 rounded-full bg-white text-[#39855b] flex items-center justify-center mx-auto shadow-sm">
              <Icon name="check" className="w-6 h-6" />
            </div>

            <div className="text-center">
              <h2 className="text-xl font-bold text-[#1f4e33]">
                ¡Pedido #{confirmedOrderData?.orderId} Confirmado!
              </h2>
              <p className="text-[12px] text-[#2e744c] mt-1">
                El pedido fue enviado exitosamente al sistema central y asignado para preparación en depósito.
              </p>
            </div>

            <div className="bg-white/80 rounded-[14px] p-3 text-[11px] space-y-1.5 border border-[#bfeecf]">
              <div className="flex justify-between">
                <span className="text-[#6b6b80]">Cliente:</span>
                <strong className="text-[#2b2b3a]">{confirmedOrderData?.cliente}</strong>
              </div>
              <div className="flex justify-between">
                <span className="text-[#6b6b80]">Total Facturado:</span>
                <strong className="text-[#2b2b3a] font-bold">
                  ${confirmedOrderData?.total?.toLocaleString('es-AR')}
                </strong>
              </div>
              <div className="flex justify-between">
                <span className="text-[#6b6b80]">Medio de Cobro:</span>
                <span className="font-medium text-[#2b2b3a]">{confirmedOrderData?.medioPago}</span>
              </div>
            </div>

            <div className="pt-2 flex flex-col sm:flex-row gap-2">
              <Link to="/pedidos" className="flex-1">
                <Button className="w-full justify-center text-[12px]">
                  Ver en Mis Pedidos
                </Button>
              </Link>
              <Link to="/tienda" className="flex-1">
                <button
                  type="button"
                  onClick={() => setConfirmed(false)}
                  className="w-full py-2.5 px-4 bg-white hover:bg-slate-50 text-[#2b2b3a] border border-[#bfeecf] rounded-[12px] text-[12px] font-bold transition-colors cursor-pointer"
                >
                  Nuevo Pedido
                </button>
              </Link>
            </div>
          </div>
        ) : items.length === 0 ? (
          <div className="text-center py-12 space-y-4">
            <div className="w-16 h-16 rounded-full bg-[#f8f9fc] flex items-center justify-center mx-auto text-[#aaa9ba]">
              <Icon name="bag" className="w-8 h-8" />
            </div>
            <h2 className="text-lg font-bold text-[#2b2b3a]">El carrito está vacío</h2>
            <p className="text-[12px] text-[#6b6b80] max-w-xs mx-auto">
              Todavía no agregaste golosinas al pedido de este comercio.
            </p>
            <Link to="/tienda">
              <Button className="text-[12px] cursor-pointer">
                Explorar Catálogo
              </Button>
            </Link>
          </div>
        ) : (
          <div className="space-y-6">
            {submitError && (
              <div className="p-3 bg-[#ffe8ed] border border-[#ffccd5] rounded-[12px] text-[12px] text-[#e5484d]">
                {submitError}
              </div>
            )}

            {/* Listado de Productos */}
            <div className="divide-y divide-[#ececf2]">
              {items.map((item) => (
                <div key={item.id} className="py-3 flex items-center justify-between gap-3">
                  <div className="flex items-center gap-3 flex-1 min-w-0">
                    <div className="w-12 h-12 rounded-[10px] bg-[#f8f9fc] border border-[#ececf2] flex items-center justify-center flex-shrink-0 overflow-hidden">
                      {item.img ? (
                        <img src={item.img} alt={item.nombre} className="w-full h-full object-cover" />
                      ) : (
                        <Icon name="box" className="w-5 h-5 text-[#aaa9ba]" />
                      )}
                    </div>
                    <div className="truncate">
                      <h4 className="text-[13px] font-bold text-[#2b2b3a] truncate">{item.nombre}</h4>
                      <span className="text-[11px] text-[#6b6b80] font-mono">
                        ${item.precio?.toLocaleString('es-AR')} c/u
                      </span>
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    <div className="flex items-center border border-[#ececf2] rounded-[10px] bg-[#f8f9fc]">
                      <button
                        type="button"
                        onClick={() => actualizarCantidad(item.id, -1)}
                        className="w-7 h-7 flex items-center justify-center text-[#6b6b80] hover:text-[#2b2b3a] font-bold cursor-pointer"
                      >
                        -
                      </button>
                      <span className="w-8 text-center text-[12px] font-bold text-[#2b2b3a]">
                        {item.cantidad}
                      </span>
                      <button
                        type="button"
                        onClick={() => actualizarCantidad(item.id, 1)}
                        className="w-7 h-7 flex items-center justify-center text-[#6b6b80] hover:text-[#2b2b3a] font-bold cursor-pointer"
                      >
                        +
                      </button>
                    </div>

                    <button
                      type="button"
                      onClick={() => removerItem(item.id)}
                      className="text-[#aaa9ba] hover:text-[#e5484d] p-1 cursor-pointer transition-colors"
                      title="Eliminar"
                    >
                      <Icon name="trash" className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>

            {/* Código Promocional */}
            <form onSubmit={handleApplyPromo} className="flex gap-2">
              <input
                type="text"
                value={promoCodeInput}
                onChange={(e) => setPromoCodeInput(e.target.value)}
                placeholder="Código promocional (opcional)"
                className="flex-1 border border-[#ececf2] rounded-[12px] px-3 py-2 text-[12px] uppercase focus:outline-none focus:border-[#e86bc4]"
              />
              <button
                type="submit"
                className="px-4 py-2 bg-[#f0f2f7] hover:bg-[#e4e7f0] text-[#2b2b3a] rounded-[12px] text-[12px] font-bold cursor-pointer transition-colors"
              >
                Aplicar
              </button>
            </form>

            {/* Totales */}
            <div className="bg-[#f8f9fc] rounded-[16px] p-4 border border-[#ececf2] space-y-2">
              <div className="flex justify-between text-[12px] text-[#6b6b80]">
                <span>Subtotal ({items.reduce((a, b) => a + b.cantidad, 0)} unidades)</span>
                <span className="font-mono font-medium">${subtotal.toLocaleString('es-AR')}</span>
              </div>
              <div className="flex justify-between text-[15px] font-bold text-[#2b2b3a] pt-2 border-t border-[#ececf2]">
                <span>Total a Facturar</span>
                <span className="font-mono text-[#e86bc4]">${total.toLocaleString('es-AR')}</span>
              </div>
            </div>

            {/* Medios de Pago */}
            <div className="space-y-3 pt-2 border-t border-[#ececf2]">
              <label className="block text-[12px] font-bold text-[#2b2b3a]">
                Modalidad de Cobro
              </label>

              <div className="grid grid-cols-3 gap-2">
                <button
                  type="button"
                  onClick={() => setPaymentMethod('efectivo')}
                  className={`p-3 rounded-[14px] border text-left cursor-pointer transition-all ${
                    paymentMethod === 'efectivo'
                      ? 'border-[#e86bc4] bg-[#ffe2f7]/20 font-bold text-[#2b2b3a]'
                      : 'border-[#ececf2] hover:bg-slate-50 text-[#6b6b80]'
                  }`}
                >
                  <span className="text-sm block mb-1">💵</span>
                  <span className="text-[11px] block">Contra Entrega</span>
                </button>

                <button
                  type="button"
                  onClick={() => setPaymentMethod('mercadopago')}
                  className={`p-3 rounded-[14px] border text-left cursor-pointer transition-all ${
                    paymentMethod === 'mercadopago'
                      ? 'border-[#3fb8d9] bg-[#d9f7ff]/20 font-bold text-[#2b2b3a]'
                      : 'border-[#ececf2] hover:bg-slate-50 text-[#6b6b80]'
                  }`}
                >
                  <span className="text-sm block mb-1">📱</span>
                  <span className="text-[11px] block">Mercado Pago QR</span>
                </button>

                <button
                  type="button"
                  onClick={() => setPaymentMethod('transferencia')}
                  className={`p-3 rounded-[14px] border text-left cursor-pointer transition-all ${
                    paymentMethod === 'transferencia'
                      ? 'border-[#5e35b1] bg-[#ede7f6]/40 font-bold text-[#2b2b3a]'
                      : 'border-[#ececf2] hover:bg-slate-50 text-[#6b6b80]'
                  }`}
                >
                  <span className="text-sm block mb-1">🏦</span>
                  <span className="text-[11px] block">Transferencia</span>
                </button>
              </div>

              {/* Detalle Transferencia */}
              {paymentMethod === 'transferencia' && (
                <div className="p-4 rounded-[14px] bg-[#f8f9fc] border border-[#ececf2] space-y-3 text-[11px]">
                  <div className="flex justify-between items-center">
                    <span className="text-[#6b6b80]">Alias CBU:</span>
                    <strong className="font-mono text-[#2b2b3a]">{DATOS_BANCARIOS.alias}</strong>
                    <button
                      type="button"
                      onClick={() => handleCopy(DATOS_BANCARIOS.alias, 'alias')}
                      className="text-[#e86bc4] font-bold ml-2 cursor-pointer"
                    >
                      {copiadoKey === 'alias' ? 'Copiado!' : 'Copiar'}
                    </button>
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-[#2b2b3a] mb-1">
                      N° Comprobante o Transacción *
                    </label>
                    <input
                      type="text"
                      required
                      value={transferenciaData.numeroComprobante}
                      onChange={(e) => setTransferenciaData({ ...transferenciaData, numeroComprobante: e.target.value })}
                      placeholder="Ej: TRF-903421"
                      className="w-full border border-[#ececf2] rounded-[8px] px-2.5 py-1.5 text-[11px] font-mono bg-white focus:outline-none focus:border-[#e86bc4]"
                    />
                  </div>
                </div>
              )}
            </div>

            {/* Botón Confirmar */}
            <Button
              onClick={handleConfirmOrder}
              disabled={isSubmitting}
              className="w-full py-3.5 text-[13px] justify-center shadow-md cursor-pointer"
            >
              {isSubmitting ? 'Enviando Pedido al Servidor...' : 'Confirmar Pedido de Preventa'}
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
