/**
 * Carrito de Compras y Cierre de Pedido para el Preventista.
 * Conectado con CartContext, /api/v1/pedidos y /api/v1/pedidos/{id}/pagos/qr/iniciar.
 */
import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import MercadoPagoQrPanel from '../../components/MercadoPagoQrPanel';
import { useCart } from '../../context/CartContext';
import { pagoService } from '../../services/pagoService';
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

  // Estados del flujo: 'CART' | 'WAITING_QR' | 'CONFIRMED'
  const [flowStep, setFlowStep] = useState('CART');
  const [confirmedOrderData, setConfirmedOrderData] = useState(null);
  const [initialQrData, setInitialQrData] = useState(null);
  const [promoCodeInput, setPromoCodeInput] = useState(codigoPromo || '');
  const [appliedPromo, setAppliedPromo] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);
  const navigate = useNavigate();

  // Método de pago: 'mercadopago' | 'transferencia' | 'efectivo'
  const [paymentMethod, setPaymentMethod] = useState('efectivo');

  // Datos bancarios oficiales
  const [datosBancarios, setDatosBancarios] = useState(DATOS_BANCARIOS);
  const [transferenciaData, setTransferenciaData] = useState({
    numeroComprobante: '',
    bancoEmisor: 'Banco Santander',
  });
  const [copiadoKey, setCopiadoKey] = useState(null);

  useEffect(() => {
    pagoService
      .obtenerDatosBancarios()
      .then((res) => {
        if (res && res.cbu) setDatosBancarios(res);
      })
      .catch(() => null);
  }, []);

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

      // 1. Registrar pedido en el backend (deja estado CARGADO y vacía el carrito)
      const nuevoPedido = await confirmarPedido();

      const dir = clienteActual.direccion;
      const direccionStr = dir
        ? `${dir.calle || ''} ${dir.numeracion || dir.numero || ''}`.trim() || 'Mendoza'
        : 'Mendoza';

      const baseOrderPayload = {
        orderId: nuevoPedido?.id,
        numeroPedido: nuevoPedido?.numeroPedido,
        etiquetaPedido: nuevoPedido?.numeroPedido
          ? `DM-${nuevoPedido.numeroPedido}`
          : `#${String(nuevoPedido?.id || '').slice(-6)}`,
        cliente: clienteActual.nombreComercio || 'Comercio Minorista',
        direccion: direccionStr,
        total: Number(nuevoPedido?.total ?? total),
        subtotal: Number(nuevoPedido?.subtotal ?? subtotal),
        descuento: Number(nuevoPedido?.descuento ?? 0),
        comprobante: transferenciaData.numeroComprobante || null,
        fecha: new Date().toLocaleString('es-AR'),
      };

      // 2. Bifurcar según modalidad de cobro
      if (paymentMethod === 'mercadopago' && nuevoPedido?.id) {
        const qrResp = await pagoService.iniciarPagoQr(nuevoPedido.id).catch(() => null);
        setInitialQrData(qrResp);
        setConfirmedOrderData({
          ...baseOrderPayload,
          medioPago: 'Mercado Pago QR',
          estadoPago: 'Aguardando acreditación de pago',
          pagoAprobado: false,
        });
        setFlowStep('WAITING_QR');
        return;
      }

      if (paymentMethod === 'transferencia' && nuevoPedido?.id) {
        await pagoService
          .informarTransferencia(nuevoPedido.id, {
            comprobante: transferenciaData.numeroComprobante.trim(),
            bancoEmisor: transferenciaData.bancoEmisor || 'Transferencia CBU',
            observacion: 'Comprobante informado por preventor en visita comercial',
          })
          .catch(() => null);
      }

      setConfirmedOrderData({
        ...baseOrderPayload,
        medioPago:
          paymentMethod === 'transferencia' ? 'Transferencia Bancaria' : 'Efectivo contra entrega',
        estadoPago:
          paymentMethod === 'transferencia'
            ? 'Informado por preventor (pendiente de verificación administrativa)'
            : 'Pendiente de cobro por distribuidor en entrega',
        pagoAprobado: false,
      });
      setFlowStep('CONFIRMED');
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
        {flowStep === 'WAITING_QR' && confirmedOrderData ? (
          <div className="space-y-4">
            <div className="p-3.5 bg-[#eaf8fc] border border-[#3fb8d9] rounded-[14px] text-[12px] text-[#0f344d]">
              <strong className="block">
                Pedido {confirmedOrderData.etiquetaPedido} registrado para {confirmedOrderData.cliente}
              </strong>
              <span className="text-[#3a6888] text-[11px]">
                Mostrá el QR al comerciante para completar el cobro en el acto.
              </span>
            </div>

            <MercadoPagoQrPanel
              pedidoId={confirmedOrderData.orderId}
              numeroPedido={confirmedOrderData.numeroPedido}
              nombreComercio={confirmedOrderData.cliente}
              monto={confirmedOrderData.total}
              qrInicial={initialQrData}
              onPagoAprobado={() => {
                setConfirmedOrderData((prev) => ({
                  ...prev,
                  estadoPago: 'Cobro acreditado por Mercado Pago',
                  pagoAprobado: true,
                }));
                setFlowStep('CONFIRMED');
              }}
              onPosponerPago={() => {
                setConfirmedOrderData((prev) => ({
                  ...prev,
                  estadoPago: 'Pendiente de cobro QR (podés reabrirlo en Mis Pedidos)',
                  pagoAprobado: false,
                }));
                setFlowStep('CONFIRMED');
              }}
            />
          </div>
        ) : flowStep === 'CONFIRMED' && confirmedOrderData ? (
          <div className="p-6 bg-[#dff8e9] rounded-[20px] text-[#235839] space-y-4">
            <div className="w-12 h-12 rounded-full bg-white text-[#39855b] flex items-center justify-center mx-auto shadow-sm">
              <Icon name="check" className="w-6 h-6" />
            </div>

            <div className="text-center">
              <h2 className="text-xl font-bold text-[#1f4e33]">
                ¡Pedido {confirmedOrderData.etiquetaPedido} Confirmado!
              </h2>
              <p className="text-[12px] text-[#2e744c] mt-1">
                {confirmedOrderData.pagoAprobado
                  ? 'El pago con Mercado Pago fue acreditado y el pedido avanzó a preparación en depósito.'
                  : 'El pedido fue enviado exitosamente al sistema central.'}
              </p>
            </div>

            <div className="bg-white/80 rounded-[14px] p-3 text-[11px] space-y-1.5 border border-[#bfeecf]">
              <div className="flex justify-between">
                <span className="text-[#6b6b80]">Cliente:</span>
                <strong className="text-[#2b2b3a]">{confirmedOrderData.cliente}</strong>
              </div>
              <div className="flex justify-between">
                <span className="text-[#6b6b80]">Total del Pedido:</span>
                <strong className="text-[#2b2b3a] font-bold">
                  ${confirmedOrderData.total?.toLocaleString('es-AR')}
                </strong>
              </div>
              <div className="flex justify-between">
                <span className="text-[#6b6b80]">Medio de Cobro:</span>
                <span className="font-medium text-[#2b2b3a]">{confirmedOrderData.medioPago}</span>
              </div>
              <div className="flex justify-between gap-2">
                <span className="text-[#6b6b80]">Estado de Pago:</span>
                <span className="font-semibold text-[#1f4e33] text-right">
                  {confirmedOrderData.estadoPago}
                </span>
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
                  onClick={() => {
                    setFlowStep('CART');
                    setInitialQrData(null);
                    setConfirmedOrderData(null);
                  }}
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
              submitError === 'Cambie su clave para acceder' ? (
                <div className="max-w-md mx-auto my-4 p-8 bg-white rounded-[24px] border border-[#ececf2] text-center space-y-4 shadow-sm">
                  <div className="w-16 h-16 rounded-full bg-[#eefaff] text-[#247f99] flex items-center justify-center mx-auto text-3xl font-bold">
                    🔒
                  </div>
                  <h2 className="text-xl font-bold text-[#2b2b3a]">Cambie su clave para acceder</h2>
                  <p className="text-[13px] text-[#6b6b80] leading-relaxed">
                    Su usuario posee una contraseña temporal por defecto. Por razones de seguridad, debe actualizar su clave antes de operar en esta sección.
                  </p>
                  <Link
                    to="/cambiar-password"
                    className="inline-flex items-center justify-center w-full py-3 px-4 rounded-[14px] bg-[#247f99] text-white font-bold text-[13px] hover:bg-[#1c667b] transition-colors shadow-sm"
                  >
                    Cambiar contraseña ahora →
                  </Link>
                </div>
              ) : (
                <div className="p-3 bg-[#ffe8ed] border border-[#ffccd5] rounded-[12px] text-[12px] text-[#e5484d]">
                  {submitError}
                </div>
              )
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
            <div className="space-y-2">
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
              {appliedPromo && (
                <div className="flex items-center justify-between text-[11px] bg-[#fff0fb] border border-[#f8c6ea] text-[#9b2c7c] px-3 py-1.5 rounded-[10px]">
                  <span>{appliedPromo.descripcion}</span>
                  <button
                    type="button"
                    onClick={handleRemovePromo}
                    className="font-bold underline cursor-pointer"
                  >
                    Quitar
                  </button>
                </div>
              )}
            </div>

            {/* Totales */}
            <div className="bg-[#f8f9fc] rounded-[16px] p-4 border border-[#ececf2] space-y-2">
              <div className="flex justify-between text-[12px] text-[#6b6b80]">
                <span>Subtotal ({items.reduce((a, b) => a + b.cantidad, 0)} unidades)</span>
                <span className="font-mono font-medium">${subtotal.toLocaleString('es-AR')}</span>
              </div>
              <div className="flex justify-between text-[15px] font-bold text-[#2b2b3a] pt-2 border-t border-[#ececf2]">
                <span>Total del Pedido</span>
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

              {/* Previsualización informativa Efectivo */}
              {paymentMethod === 'efectivo' && (
                <div className="p-3.5 rounded-[14px] bg-[#f9fdfa] border border-[#d2f3e2] text-[11px] text-[#356649]">
                  El preventista no recibe dinero en efectivo. El cobro se diferirá al chofer distribuidor contra entrega en el comercio.
                </div>
              )}

              {/* Previsualización informativa Mercado Pago QR */}
              {paymentMethod === 'mercadopago' && (
                <div className="p-4 rounded-[14px] bg-[#f4fbfe] border border-[#cdecfb] flex items-start gap-3 text-[11px] text-[#0f344d]">
                  <div className="w-9 h-9 rounded-[10px] bg-[#009ee3]/15 text-[#009ee3] flex items-center justify-center shrink-0">
                    <Icon name="qr" className="w-5 h-5" />
                  </div>
                  <div className="space-y-1">
                    <strong className="block text-[12px]">
                      QR Dinámico oficial de Mercado Pago
                    </strong>
                    <p className="text-[#426c88]">
                      Al confirmar el pedido se generará el código QR oficial para que el comerciante lo escanee en el mostrador por <strong>${total.toLocaleString('es-AR')}</strong> (vigencia de 15 minutos con verificación automática).
                    </p>
                  </div>
                </div>
              )}

              {/* Detalle Transferencia */}
              {paymentMethod === 'transferencia' && (
                <div className="p-4 rounded-[14px] bg-[#f8f9fc] border border-[#ececf2] space-y-3 text-[11px]">
                  <div className="flex justify-between items-center">
                    <span className="text-[#6b6b80]">Alias CBU:</span>
                    <strong className="font-mono text-[#2b2b3a]">{datosBancarios.alias}</strong>
                    <button
                      type="button"
                      onClick={() => handleCopy(datosBancarios.alias, 'alias')}
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
              {isSubmitting
                ? 'Enviando Pedido al Servidor...'
                : paymentMethod === 'mercadopago'
                ? 'Confirmar Pedido y Generar QR'
                : 'Confirmar Pedido de Preventa'}
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
