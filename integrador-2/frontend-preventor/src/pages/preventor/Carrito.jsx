import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import { cartItemsMock } from '../../mocks/products';
import { datosBancariosMock } from '../../mocks/checkoutMock';

export default function Carrito() {
  const [cart, setCart] = useState(cartItemsMock);
  const [confirmed, setConfirmed] = useState(false);
  const [confirmedOrderData, setConfirmedOrderData] = useState(null);
  const [promoCodeInput, setPromoCodeInput] = useState('');
  const [appliedPromo, setAppliedPromo] = useState({
    code: 'MAYORISTA20',
    descripcion: '20% descuento por acuerdo comercial',
    pct: 0.20
  });
  const [promoError, setPromoError] = useState('');
  const navigate = useNavigate();

  // Método de pago: 'mercadopago' | 'transferencia' | 'efectivo'
  const [paymentMethod, setPaymentMethod] = useState('mercadopago');

  // Estado para Transferencia Bancaria
  const [transferenciaData, setTransferenciaData] = useState({
    numeroComprobante: '',
    bancoEmisor: 'Banco Santander',
    confirmadoPorCliente: true
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

  const PROMO_CODES = {
    'MAYORISTA20': { code: 'MAYORISTA20', descripcion: '20% descuento por acuerdo comercial', pct: 0.20 },
    'DULCE10': { code: 'DULCE10', descripcion: '10% bonificación de preventa', pct: 0.10 },
    'PROMOALFAJORES': { code: 'PROMOALFAJORES', descripcion: 'Bonificación fija de $4.900', fijo: 4900 }
  };

  const updateQuantity = (id, delta) => {
    setCart((prev) =>
      prev.map((item) => {
        if (item.id === id) {
          const newQ = Math.max(1, item.cantidad + delta);
          return { ...item, cantidad: newQ };
        }
        return item;
      })
    );
  };

  const handleApplyPromo = (e) => {
    e.preventDefault();
    const clean = promoCodeInput.trim().toUpperCase();
    if (!clean) return;

    if (PROMO_CODES[clean]) {
      setAppliedPromo(PROMO_CODES[clean]);
      setPromoError('');
      setPromoCodeInput('');
    } else {
      setPromoError('Código inválido o sin vigencia para este comercio.');
    }
  };

  const handleRemovePromo = () => {
    setAppliedPromo(null);
    setPromoError('');
  };

  const subtotal = cart.reduce((acc, it) => acc + it.precioNum * it.cantidad, 0);

  let descuento = 0;
  if (appliedPromo) {
    if (appliedPromo.pct) {
      descuento = Math.round(subtotal * appliedPromo.pct);
    } else if (appliedPromo.fijo) {
      descuento = Math.min(subtotal, appliedPromo.fijo);
    }
  }

  const total = Math.max(0, subtotal - descuento);

  const handleConfirmOrder = () => {
    if (paymentMethod === 'transferencia' && !transferenciaData.numeroComprobante.trim()) {
      alert('Por favor ingresá el número de comprobante o transacción de la transferencia bancaria realizada por el cliente.');
      return;
    }

    const orderPayload = {
      orderId: 'DM-2085',
      cliente: 'Kiosco El Sol',
      direccion: 'Av. San Martín 1842, Godoy Cruz',
      subtotal,
      descuento,
      total,
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
      fecha: new Date().toLocaleDateString('es-AR') + ' ' + new Date().toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' })
    };

    setConfirmedOrderData(orderPayload);
    setConfirmed(true);
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
          Comercio: <strong>Kiosco El Sol</strong> (Av. San Martín 1842, Godoy Cruz)
        </p>
      </div>

      <div className="bg-white rounded-[22px] p-5 sm:p-6 border border-[#ececf2] shadow-sm">
        {confirmed ? (
          <div className="p-6 bg-[#dff8e9] rounded-[20px] text-[#235839] space-y-4">
            <div className="w-12 h-12 rounded-full bg-white text-[#39855b] flex items-center justify-center mx-auto shadow-sm">
              <Icon name="check" className="w-6 h-6" />
            </div>

            <div className="text-center">
              <h3 className="text-xl font-bold text-[#1c4b30]">¡Pedido registrado exitosamente!</h3>
              <p className="text-[12px] text-[#3d7a55] mt-1">
                El pedido <strong>#{confirmedOrderData?.orderId}</strong> fue transmitido al sistema central de Dulce Mayor.
              </p>
            </div>

            <div className="bg-white/90 rounded-[14px] p-4 text-[12px] text-[#2b2b3a] space-y-2 border border-[#bbf7d0]">
              <div className="flex justify-between">
                <span className="text-[#6b6b80]">Total Facturado:</span>
                <strong className="text-[14px] font-bold text-[#166534]">
                  ${confirmedOrderData?.total.toLocaleString('es-AR')}
                </strong>
              </div>
              <div className="flex justify-between">
                <span className="text-[#6b6b80]">Forma de Pago:</span>
                <span className="font-semibold">{confirmedOrderData?.medioPago}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-[#6b6b80]">Estado del Pago:</span>
                <span className="font-semibold text-[#166534]">{confirmedOrderData?.estadoPago}</span>
              </div>
              {confirmedOrderData?.comprobante && (
                <div className="flex justify-between">
                  <span className="text-[#6b6b80]">N° Comprobante:</span>
                  <span className="font-mono font-semibold">{confirmedOrderData?.comprobante}</span>
                </div>
              )}
            </div>

            <div className="pt-2 flex flex-col gap-2">
              <Button onClick={() => navigate('/recorrido')} className="w-full justify-center">
                Continuar con el recorrido de visitas
              </Button>
            </div>
          </div>
        ) : (
          <div className="space-y-6">
            {/* Lista de productos en el carrito */}
            <div>
              <h2 className="text-[14px] font-bold text-[#2b2b3a] mb-2">
                Mercadería seleccionada
              </h2>
              <div className="divide-y divide-[#f0f0f4]">
                {cart.map((p) => (
                  <div
                    key={p.id}
                    className="py-3 first:pt-0 last:pb-0 grid grid-cols-[58px_1fr_auto] gap-3 items-center"
                  >
                    <img
                      src={p.imagen}
                      alt={p.nombre}
                      className="w-[58px] h-[58px] rounded-[10px] object-cover border border-[#ececf2]"
                    />
                    <div className="flex flex-col text-[11px]">
                      <b className="text-[13px] text-[#2b2b3a] leading-tight font-semibold">
                        {p.nombre}
                      </b>
                      <small className="text-[#6b6b80] text-[10px]">{p.presentacion}</small>
                      <div className="flex items-center gap-2 mt-1.5 text-[#e86bc4] font-bold">
                        <button
                          type="button"
                          onClick={() => updateQuantity(p.id, -1)}
                          className="w-5 h-5 rounded bg-[#ffe2f7] flex items-center justify-center text-[12px] cursor-pointer"
                        >
                          −
                        </button>
                        <span className="text-[12px] text-[#2b2b3a]">{p.cantidad}</span>
                        <button
                          type="button"
                          onClick={() => updateQuantity(p.id, 1)}
                          className="w-5 h-5 rounded bg-[#ffe2f7] flex items-center justify-center text-[12px] cursor-pointer"
                        >
                          +
                        </button>
                      </div>
                    </div>
                    <strong className="text-[13px] font-bold text-[#2b2b3a]">
                      ${(p.precioNum * p.cantidad).toLocaleString('es-AR')}
                    </strong>
                  </div>
                ))}
              </div>
            </div>

            {/* Código de Promoción Input */}
            <div className="pt-3 border-t border-[#f0f0f4] space-y-2">
              <label className="block text-[11px] font-semibold text-[#2b2b3a]">
                Código de Promoción / Acuerdo de Preventa
              </label>

              {appliedPromo ? (
                <div className="p-3 rounded-[12px] bg-[#dff8e9] border border-[#7ed6a5]/50 flex items-center justify-between">
                  <div>
                    <div className="flex items-center gap-1.5">
                      <span className="text-[11px] font-bold text-[#39855b] bg-white px-2 py-0.5 rounded-md border border-[#7ed6a5]/60">
                        {appliedPromo.code}
                      </span>
                      <span className="text-[10px] text-[#39855b] font-semibold">
                        Aplicado
                      </span>
                    </div>
                    <small className="text-[10px] text-[#39855b] block mt-0.5">
                      {appliedPromo.descripcion}
                    </small>
                  </div>
                  <button
                    type="button"
                    onClick={handleRemovePromo}
                    className="text-[10px] text-[#c95569] font-bold hover:underline ml-2 cursor-pointer"
                  >
                    Quitar
                  </button>
                </div>
              ) : (
                <form onSubmit={handleApplyPromo} className="space-y-1.5">
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={promoCodeInput}
                      onChange={(e) => {
                        setPromoCodeInput(e.target.value);
                        if (promoError) setPromoError('');
                      }}
                      placeholder="Ej. MAYORISTA20 o DULCE10"
                      className="flex-1 border border-[#ececf2] rounded-[10px] px-3 py-2 text-[11px] text-[#2b2b3a] uppercase font-mono bg-[#f8f9fc] focus:bg-white focus:outline-none focus:border-[#e86bc4]"
                    />
                    <Button type="submit" className="px-3.5 py-2 text-[11px]">
                      Aplicar
                    </Button>
                  </div>
                  {promoError && (
                    <p className="text-[10px] text-[#ff8fa3] font-medium">{promoError}</p>
                  )}
                </form>
              )}
            </div>

            {/* Total summary */}
            <div className="bg-[#fafafd] rounded-[14px] p-4 border border-[#ececf2] space-y-2 text-[12px]">
              <div className="flex justify-between text-[#6b6b80]">
                <span>Subtotal mercadería</span>
                <b className="text-[#2b2b3a]">${subtotal.toLocaleString('es-AR')}</b>
              </div>

              {descuento > 0 && (
                <div className="flex justify-between text-[#39855b]">
                  <span>Descuento aplicado</span>
                  <b>−${descuento.toLocaleString('es-AR')}</b>
                </div>
              )}

              <div className="flex justify-between text-[15px] pt-2 border-t border-[#ececf2] text-[#2b2b3a]">
                <strong>Total a Facturar</strong>
                <strong className="text-[#e86bc4] text-[18px]">
                  ${total.toLocaleString('es-AR')}
                </strong>
              </div>
            </div>

            {/* SELECCIÓN Y REGISTRO DE LA FORMA DE PAGO */}
            <div className="pt-2 border-t border-[#f0f0f4] space-y-4">
              <div>
                <h2 className="text-[14px] font-bold text-[#2b2b3a]">
                  Registrar Forma de Pago
                </h2>
                <p className="text-[11px] text-[#6b6b80]">
                  Seleccioná cómo abonará el minorista en esta visita.
                </p>
              </div>

              {/* Selector de métodos (3 cards) */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5">
                {/* Opción 1: QR Mercado Pago */}
                <button
                  type="button"
                  onClick={() => setPaymentMethod('mercadopago')}
                  className={`p-3 rounded-[14px] border text-left cursor-pointer transition-all ${
                    paymentMethod === 'mercadopago'
                      ? 'border-[#009ee3] bg-[#f0faff] shadow-sm ring-1 ring-[#009ee3]'
                      : 'border-[#ececf2] bg-white hover:bg-[#fafafd]'
                  }`}
                >
                  <div className="flex items-center justify-between mb-1.5">
                    <span className="text-[14px]">📱</span>
                    <span className="text-[9px] font-bold px-1.5 py-0.5 rounded bg-[#dff8e9] text-[#39855b]">
                      En vivo
                    </span>
                  </div>
                  <strong className="block text-[12px] text-[#2b2b3a]">Mercado Pago QR</strong>
                  <p className="text-[10px] text-[#6b6b80] mt-0.5">Mostrar en pantalla al cliente</p>
                </button>

                {/* Opción 2: Transferencia */}
                <button
                  type="button"
                  onClick={() => setPaymentMethod('transferencia')}
                  className={`p-3 rounded-[14px] border text-left cursor-pointer transition-all ${
                    paymentMethod === 'transferencia'
                      ? 'border-[#e86bc4] bg-[#fff5fc] shadow-sm ring-1 ring-[#e86bc4]'
                      : 'border-[#ececf2] bg-white hover:bg-[#fafafd]'
                  }`}
                >
                  <div className="flex items-center justify-between mb-1.5">
                    <span className="text-[14px]">🏦</span>
                    <span className="text-[9px] font-bold px-1.5 py-0.5 rounded bg-[#faf0d9] text-[#b88014]">
                      CBU / Alias
                    </span>
                  </div>
                  <strong className="block text-[12px] text-[#2b2b3a]">Transferencia</strong>
                  <p className="text-[10px] text-[#6b6b80] mt-0.5">Mostrar datos y marcar pago</p>
                </button>

                {/* Opción 3: Efectivo */}
                <button
                  type="button"
                  onClick={() => setPaymentMethod('efectivo')}
                  className={`p-3 rounded-[14px] border text-left cursor-pointer transition-all ${
                    paymentMethod === 'efectivo'
                      ? 'border-[#7ed6a5] bg-[#f2fdf7] shadow-sm ring-1 ring-[#7ed6a5]'
                      : 'border-[#ececf2] bg-white hover:bg-[#fafafd]'
                  }`}
                >
                  <div className="flex items-center justify-between mb-1.5">
                    <span className="text-[14px]">🚚</span>
                    <span className="text-[9px] font-bold px-1.5 py-0.5 rounded bg-[#f0f0f4] text-[#6b6b80]">
                      En reparto
                    </span>
                  </div>
                  <strong className="block text-[12px] text-[#2b2b3a]">Efectivo</strong>
                  <p className="text-[10px] text-[#6b6b80] mt-0.5">Cobra el distribuidor</p>
                </button>
              </div>

              {/* PANEL ESPECÍFICO SEGÚN MÉTODO */}

              {/* CASO 1: QR DE MERCADO PAGO EN EL MOSTRADOR */}
              {paymentMethod === 'mercadopago' && (
                <div className="p-4 rounded-[16px] bg-[#f8fcff] border border-[#d2ecfa] space-y-3">
                  <div className="flex items-center justify-between border-b border-[#d2ecfa] pb-2">
                    <div className="flex items-center gap-2">
                      <span className="w-6 h-6 rounded-md bg-[#009ee3] text-white flex items-center justify-center font-bold text-[10px]">
                        MP
                      </span>
                      <strong className="text-[12px] text-[#0f344d]">
                        QR Oficial para Escaneo In Situ
                      </strong>
                    </div>
                    <div className="text-[10px] font-semibold text-[#009ee3] bg-white px-2 py-0.5 rounded-full border border-[#d2ecfa]">
                      Expira: {formatTimer(qrTimeLeft)}
                    </div>
                  </div>

                  <div className="flex flex-col sm:flex-row items-center gap-4 py-1">
                    {/* Caja del QR interactivo */}
                    <div className="p-2.5 bg-white border border-[#009ee3]/30 rounded-[14px] shadow-xs flex flex-col items-center shrink-0">
                      <div className="w-36 h-36 bg-white p-1 rounded-[10px] flex items-center justify-center relative">
                        {/* Simulación visual de código QR */}
                        <svg className="w-full h-full text-[#2b2b3a]" viewBox="0 0 100 100" fill="currentColor">
                          <rect x="5" y="5" width="25" height="25" rx="2" />
                          <rect x="9" y="9" width="17" height="17" fill="white" />
                          <rect x="13" y="13" width="9" height="9" />

                          <rect x="70" y="5" width="25" height="25" rx="2" />
                          <rect x="74" y="9" width="17" height="17" fill="white" />
                          <rect x="78" y="13" width="9" height="9" />

                          <rect x="5" y="70" width="25" height="25" rx="2" />
                          <rect x="9" y="74" width="17" height="17" fill="white" />
                          <rect x="13" y="78" width="9" height="9" />

                          <rect x="35" y="10" width="6" height="6" />
                          <rect x="45" y="15" width="6" height="6" />
                          <rect x="55" y="8" width="6" height="6" />
                          <rect x="35" y="25" width="6" height="6" />
                          <rect x="48" y="32" width="6" height="6" />
                          <rect x="15" y="45" width="6" height="6" />
                          <rect x="25" y="52" width="6" height="6" />
                          <rect x="35" y="45" width="6" height="6" />
                          <rect x="45" y="48" width="6" height="6" />
                          <rect x="55" y="42" width="6" height="6" />
                          <rect x="70" y="40" width="6" height="6" />
                          <rect x="80" y="48" width="6" height="6" />
                          <rect x="88" y="38" width="6" height="6" />
                          <rect x="40" y="65" width="6" height="6" />
                          <rect x="52" y="72" width="6" height="6" />
                          <rect x="62" y="65" width="6" height="6" />
                          <rect x="75" y="75" width="6" height="6" />
                          <rect x="85" y="85" width="6" height="6" />
                          <rect x="40" y="85" width="6" height="6" />
                          <rect x="58" y="88" width="6" height="6" />

                          <circle cx="50" cy="50" r="9" fill="white" />
                          <circle cx="50" cy="50" r="6" fill="#009ee3" />
                        </svg>

                        {qrTimeLeft === 0 && (
                          <div className="absolute inset-0 bg-white/95 backdrop-blur-[2px] rounded-[10px] flex flex-col items-center justify-center p-2 text-center">
                            <span className="text-[10px] font-bold text-red-500 mb-1">QR Expirado</span>
                            <button
                              type="button"
                              onClick={() => setQrTimeLeft(900)}
                              className="px-2 py-1 rounded bg-[#009ee3] text-white text-[9px] font-bold cursor-pointer"
                            >
                              ↻ Regenerar
                            </button>
                          </div>
                        )}
                      </div>
                      <span className="text-[9px] text-[#55819e] font-semibold mt-1">
                        Monto: ${total.toLocaleString('es-AR')}
                      </span>
                    </div>

                    <div className="text-[11px] text-[#426c88] space-y-2 flex-1">
                      <p>
                        1. <strong>Mostrale tu pantalla al comerciante</strong> para que escanee con la app de Mercado Pago o cualquier banco con interoperabilidad QR.
                      </p>
                      <p>
                        2. El importe total exacto de <strong>${total.toLocaleString('es-AR')}</strong> aparecerá en su teléfono.
                      </p>
                      <div className="pt-2">
                        <button
                          type="button"
                          onClick={() => setQrSimuladoAprobado(!qrSimuladoAprobado)}
                          className={`px-3 py-1.5 rounded-[10px] text-[11px] font-bold border cursor-pointer transition-all ${
                            qrSimuladoAprobado
                              ? 'bg-[#dff8e9] text-[#39855b] border-[#39855b]'
                              : 'bg-white text-[#009ee3] border-[#009ee3]/40 hover:bg-[#009ee3]/10'
                          }`}
                        >
                          {qrSimuladoAprobado ? '✓ Pago Acreditado en Mercado Pago' : 'Simular escaneo y acreditación'}
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* CASO 2: TRANSFERENCIA BANCARIA */}
              {paymentMethod === 'transferencia' && (
                <div className="p-4 rounded-[16px] bg-[#fafafd] border border-[#ececf2] space-y-3">
                  <div className="flex items-center justify-between border-b border-[#f0f0f4] pb-2">
                    <strong className="text-[12px] text-[#2b2b3a]">
                      Datos Bancarios Oficiales para Mostrar al Cliente
                    </strong>
                    <span className="text-[9px] font-bold px-2 py-0.5 rounded bg-white border border-[#ececf2] text-[#6b6b80]">
                      Cuenta Corriente
                    </span>
                  </div>

                  {/* Cuadrícula de CBU y Alias */}
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-[11px]">
                    <div className="p-2.5 bg-white rounded-[10px] border border-[#ececf2] flex items-center justify-between">
                      <div>
                        <span className="text-[9px] uppercase font-bold text-[#aaa9ba] block">Alias CBU</span>
                        <strong className="text-[#2b2b3a] font-mono text-[12px]">{datosBancariosMock.alias}</strong>
                      </div>
                      <button
                        type="button"
                        onClick={() => handleCopy(datosBancariosMock.alias, 'alias')}
                        className="text-[10px] font-bold text-[#e86bc4] hover:underline cursor-pointer"
                      >
                        {copiadoKey === 'alias' ? '¡Copiado!' : 'Copiar'}
                      </button>
                    </div>

                    <div className="p-2.5 bg-white rounded-[10px] border border-[#ececf2] flex items-center justify-between">
                      <div>
                        <span className="text-[9px] uppercase font-bold text-[#aaa9ba] block">CBU</span>
                        <strong className="text-[#2b2b3a] font-mono text-[11px]">{datosBancariosMock.cbu}</strong>
                      </div>
                      <button
                        type="button"
                        onClick={() => handleCopy(datosBancariosMock.cbu, 'cbu')}
                        className="text-[10px] font-bold text-[#e86bc4] hover:underline cursor-pointer"
                      >
                        {copiadoKey === 'cbu' ? '¡Copiado!' : 'Copiar'}
                      </button>
                    </div>

                    <div className="p-2.5 bg-white rounded-[10px] border border-[#ececf2] sm:col-span-2">
                      <span className="text-[9px] uppercase font-bold text-[#aaa9ba] block">Titular & Banco</span>
                      <span className="text-[#2b2b3a] font-medium">
                        {datosBancariosMock.titular} (CUIT {datosBancariosMock.cuit}) · {datosBancariosMock.banco}
                      </span>
                    </div>
                  </div>

                  {/* Formulario para que el preventor marque el pedido como pago */}
                  <div className="pt-2 border-t border-[#f0f0f4] space-y-2.5">
                    <strong className="text-[11px] font-bold text-[#2b2b3a] block">
                      Marcar Pago por Transferencia Realizada
                    </strong>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                      <div>
                        <label className="block text-[10px] font-semibold text-[#2b2b3a] mb-1">
                          N° Comprobante / Transacción *
                        </label>
                        <input
                          type="text"
                          required
                          value={transferenciaData.numeroComprobante}
                          onChange={(e) => setTransferenciaData({ ...transferenciaData, numeroComprobante: e.target.value })}
                          placeholder="Ej: TRF-903421"
                          className="w-full border border-[#ececf2] rounded-[8px] px-2.5 py-1.5 text-[11px] font-mono text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                        />
                      </div>

                      <div>
                        <label className="block text-[10px] font-semibold text-[#2b2b3a] mb-1">
                          Banco Emisor del Cliente
                        </label>
                        <input
                          type="text"
                          value={transferenciaData.bancoEmisor}
                          onChange={(e) => setTransferenciaData({ ...transferenciaData, bancoEmisor: e.target.value })}
                          placeholder="Ej: Macro, Santander, etc."
                          className="w-full border border-[#ececf2] rounded-[8px] px-2.5 py-1.5 text-[11px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                        />
                      </div>
                    </div>

                    <label className="flex items-center gap-2 p-2 bg-[#f4fbf7] border border-[#d2f3e2] rounded-[10px] cursor-pointer">
                      <input
                        type="checkbox"
                        checked={transferenciaData.confirmadoPorCliente}
                        onChange={(e) => setTransferenciaData({ ...transferenciaData, confirmadoPorCliente: e.target.checked })}
                        className="rounded text-[#39855b] focus:ring-0"
                      />
                      <span className="text-[10px] text-[#2c6343] font-medium">
                        El comerciante exhibió el comprobante de transferencia y la operación fue efectuada.
                      </span>
                    </label>
                  </div>
                </div>
              )}

              {/* CASO 3: EFECTIVO CONTRA ENTREGA (NO PUEDE COBRAR EL PREVENTOR) */}
              {paymentMethod === 'efectivo' && (
                <div className="p-4 rounded-[16px] bg-[#fffaf0] border border-[#ffe1a6] space-y-2">
                  <div className="flex items-center gap-2 text-[#986b16]">
                    <span className="text-base">⚠️</span>
                    <strong className="text-[12px] font-bold">
                      Cobro Diferido en Reparto
                    </strong>
                  </div>
                  <p className="text-[11px] text-[#7d5707] leading-relaxed">
                    <strong>El preventor no recibe dinero en efectivo.</strong> Esta modalidad deja el pedido asentado para que el distribuidor descargue los bultos y recaude los <strong>${total.toLocaleString('es-AR')}</strong> exactos contra remito físico al momento de la entrega.
                  </p>
                  <div className="p-2.5 bg-white/80 rounded-[10px] border border-[#ffe1a6] text-[11px] flex justify-between items-center text-[#7d5707]">
                    <span>Importe exacto a preparar por el comercio:</span>
                    <strong className="text-[13px] font-bold text-[#2b2b3a] font-mono">
                      ${total.toLocaleString('es-AR')}
                    </strong>
                  </div>
                </div>
              )}
            </div>

            {/* Botón de Confirmación Principal */}
            <div className="pt-2">
              <Button
                onClick={handleConfirmOrder}
                className="w-full py-3.5 text-[14px] justify-center cursor-pointer shadow-md"
              >
                {paymentMethod === 'mercadopago'
                  ? 'Confirmar Pedido y Cobro con QR'
                  : paymentMethod === 'transferencia'
                  ? 'Confirmar y Marcar Transferencia'
                  : 'Confirmar Pedido para Cobro en Reparto'}
              </Button>
            </div>

            <div className="text-center pt-1">
              <Link to="/tienda" className="text-[11px] text-[#3fb8d9] font-medium hover:underline">
                + Agregar más productos desde la tienda
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
