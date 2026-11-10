import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import Button from '../components/Button';
import Icon from '../components/Icon';
import { apiFetch } from '../services/api';
import { cartStorage } from '../utils/cartStorage';

const DATOS_BANCARIOS = {
  banco: 'Banco Nación Argentina',
  tipoCuenta: 'Cuenta Corriente en Pesos',
  titular: 'Dulce Mayor S.R.L.',
  cuit: '30-71549982-9',
  cbu: '0110345920034500987612',
  alias: 'DULCE.MAYOR.MZA',
};

export default function Checkout() {
  const navigate = useNavigate();

  const [cartItems, setCartItems] = useState(() => cartStorage.getItems());
  const [appliedPromo] = useState(() => cartStorage.getPromo());
  const [puntosDeVenta, setPuntosDeVenta] = useState([]);
  const [selectedPdvId, setSelectedPdvId] = useState('');
  const [horarioEntrega, setHorarioEntrega] = useState('mañana');
  const [observacionEntrega, setObservacionEntrega] = useState('');

  const [paymentMethod, setPaymentMethod] = useState('mercadopago');
  const [transferenciaData, setTransferenciaData] = useState({
    numeroComprobante: '',
    bancoEmisor: '',
    comprobanteArchivo: null,
  });
  const [copiadoKey, setCopiadoKey] = useState(null);
  const [timeLeft, setTimeLeft] = useState(900);
  const [simulatedOutcome, setSimulatedOutcome] = useState('exito');
  const [isProcessing, setIsProcessing] = useState(false);

  useEffect(() => {
    const sync = () => setCartItems(cartStorage.getItems());
    window.addEventListener('dm_cart_updated', sync);
    return () => window.removeEventListener('dm_cart_updated', sync);
  }, []);

  useEffect(() => {
    let cancel = false;
    apiFetch('/puntos-de-venta?size=50')
      .then((res) => {
        if (cancel) return;
        const list = res?.elementos || res?.content || (Array.isArray(res) ? res : []);
        const mapped = list.map((p, idx) => ({
          id: p.id,
          nombre: p.nombreComercio || `Comercio #${idx + 1}`,
          direccion: p.direccion
            ? `${p.direccion.calle || ''} ${p.direccion.numeracion || ''}`.trim()
            : 'Sin dirección',
          localidad:
            p.direccion?.localidadNombre || p.direccion?.departamentoNombre || 'Mendoza',
          horarioAtencion: '09:00 a 18:00 hs',
          esPrincipal: idx === 0,
        }));
        setPuntosDeVenta(mapped);
        if (mapped.length > 0) setSelectedPdvId(mapped[0].id);
      })
      .catch(() => {
        if (!cancel) setPuntosDeVenta([]);
      });
    return () => {
      cancel = true;
    };
  }, []);

  useEffect(() => {
    if (paymentMethod !== 'mercadopago') return;
    const timer = setInterval(() => {
      setTimeLeft((prev) => (prev > 0 ? prev - 1 : 900));
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

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      setTransferenciaData((prev) => ({
        ...prev,
        comprobanteArchivo: {
          name: file.name,
          size: (file.size / 1024).toFixed(1) + ' KB',
        },
      }));
    }
  };

  const subtotal = cartItems.reduce(
    (acc, it) => acc + Number(it.producto.precioNum || 0) * Number(it.cantidad || 1),
    0
  );
  const descuento = appliedPromo?.pct ? Math.round(subtotal * appliedPromo.pct) : 0;
  const total = Math.max(0, subtotal - descuento);
  const selectedPdv = puntosDeVenta.find((p) => p.id === selectedPdvId) || puntosDeVenta[0] || null;

  if (cartItems.length === 0) {
    return (
      <div className="bg-white rounded-[22px] p-12 text-center border border-[#ececf2] max-w-2xl mx-auto">
        <h2 className="text-xl font-bold text-[#2b2b3a]">Tu carrito está vacío</h2>
        <p className="text-[13px] text-[#6b6b80] mt-1 mb-6">
          Agregá productos con stock desde la tienda antes de continuar al pago.
        </p>
        <Link to="/tienda">
          <Button>Ir a la tienda</Button>
        </Link>
      </div>
    );
  }

  const handleProcessOrder = async (e) => {
    e.preventDefault();
    setIsProcessing(true);

    if (paymentMethod === 'mercadopago' && simulatedOutcome === 'fallo') {
      setIsProcessing(false);
      navigate('/pago/fallo', {
        state: {
          orderId: 'SIN-PROCESAR',
          total,
          metodoPago: 'Billetera Virtual (Mercado Pago QR)',
        },
      });
      return;
    }

    try {
      const requestBody = {
        puntoDeVentaId: selectedPdv?.id || null,
        preventorId: null,
        codigoPromocion: appliedPromo?.codigo || null,
        detalles: cartItems.map((it) => ({
          productoId: it.producto.id,
          cantidad: Number(it.cantidad || 1),
        })),
      };

      const creado = await apiFetch('/pedidos', {
        method: 'POST',
        body: JSON.stringify(requestBody),
      });

      const pdvResp = creado?.puntoDeVenta;
      const dirResp = pdvResp?.direccion;
      const direccionStr = dirResp
        ? `${dirResp.calle || ''} ${dirResp.numeracion || ''}, ${dirResp.localidadNombre || dirResp.departamentoNombre || 'Mendoza'}`.trim()
        : selectedPdv
        ? `${selectedPdv.direccion}, ${selectedPdv.localidad}`
        : 'Gran Mendoza';

      const orderPayload = {
        id: creado?.id,
        orderId: creado?.numeroPedido ? `DM-${creado.numeroPedido}` : creado?.id || 'DM-NUEVO',
        estado: creado?.estado || 'CARGADO',
        facturaId: creado?.facturaId || null,
        comercio: pdvResp?.nombreComercio || selectedPdv?.nombre || 'Mi Comercio',
        direccion: direccionStr,
        total: Number(creado?.total ?? total),
        subtotal: Number(creado?.subtotal ?? subtotal),
        descuento: Number(creado?.descuento ?? descuento),
        metodoPago:
          paymentMethod === 'mercadopago'
            ? 'Billetera Virtual (Mercado Pago QR)'
            : paymentMethod === 'transferencia'
            ? 'Transferencia Bancaria'
            : 'Efectivo contra entrega',
        comprobante: transferenciaData.numeroComprobante || null,
        fecha: creado?.fechaHoraCarga
          ? new Date(creado.fechaHoraCarga).toLocaleString('es-AR')
          : new Date().toLocaleString('es-AR'),
      };

      cartStorage.clear();

      if (paymentMethod === 'mercadopago' && simulatedOutcome === 'pendiente') {
        navigate('/pago/pendiente', { state: orderPayload });
      } else if (paymentMethod === 'transferencia') {
        navigate('/pago/pendiente', { state: orderPayload });
      } else {
        navigate('/pago/exito', { state: orderPayload });
      }
    } catch (err) {
      alert(err.message || 'No se pudo registrar el pedido. Verificá tu sesión e intentá nuevamente.');
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Header y Pasos */}
      <div className="border-b border-[#ececf2] pb-5">
        <div className="flex items-center gap-2 text-[12px] text-[#6b6b80] mb-2">
          <Link to="/carrito" className="hover:text-[#e86bc4]">Carrito</Link>
          <span>›</span>
          <span className="font-semibold text-[#2b2b3a]">Proceso de Pago & Entrega</span>
        </div>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div>
            <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
              Finalizar Pedido
            </span>
            <h1 className="text-2xl sm:text-3xl font-bold text-[#2b2b3a] mt-0.5">
              Confirmación y Pago
            </h1>
          </div>
          <div className="flex items-center gap-2 text-[12px] font-medium bg-[#f8f9fc] border border-[#ececf2] px-3.5 py-1.5 rounded-full">
            <span className="w-2.5 h-2.5 rounded-full bg-[#7ed6a5] animate-pulse" />
            <span>Zona de Entrega: <strong>Gran Mendoza</strong></span>
          </div>
        </div>
      </div>

      <form onSubmit={handleProcessOrder} className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Columna Principal: Entrega y Pagos (8 cols) */}
        <div className="lg:col-span-8 space-y-6">
          {/* PASO 1: Selección de Punto de Venta */}
          <section className="bg-white rounded-[22px] p-6 border border-[#ececf2] shadow-sm space-y-4">
            <div className="flex items-center gap-3">
              <span className="w-7 h-7 rounded-full bg-[#ffe2f7] text-[#e86bc4] font-bold text-[13px] flex items-center justify-center">
                1
              </span>
              <div>
                <h2 className="text-[16px] font-bold text-[#2b2b3a]">
                  Punto de Venta y Entrega
                </h2>
                <p className="text-[12px] text-[#6b6b80]">
                  Seleccioná el local comercial donde el distribuidor descargará los bultos.
                </p>
              </div>
            </div>

            {puntosDeVenta.length === 0 ? (
              <div className="p-4 rounded-[14px] bg-[#fafafd] border border-[#ececf2] text-[12px] text-[#6b6b80]">
                No hay puntos de venta registrados para tu cuenta aún. La entrega se coordinará con tu preventor asignado.
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
                {puntosDeVenta.map((pdv) => {
                  const isSelected = pdv.id === selectedPdvId;
                  return (
                    <div
                      key={pdv.id}
                      onClick={() => setSelectedPdvId(pdv.id)}
                      className={`p-4 rounded-[16px] border cursor-pointer transition-all ${
                        isSelected
                          ? 'border-[#e86bc4] bg-[#fff5fc] shadow-[0_4px_16px_rgba(232,107,196,0.12)]'
                          : 'border-[#ececf2] hover:border-[#dcdce6] bg-[#fafafd]'
                      }`}
                    >
                      <div className="flex items-center justify-between mb-1.5">
                        <span className="font-bold text-[13px] text-[#2b2b3a]">
                          {pdv.nombre}
                        </span>
                        {pdv.esPrincipal && (
                          <span className="text-[10px] bg-[#d9f7ff] text-[#247f99] font-bold px-2 py-0.5 rounded-full">
                            Principal
                          </span>
                        )}
                      </div>
                      <p className="text-[12px] text-[#6b6b80] flex items-center gap-1.5">
                        <Icon name="mapPin" className="w-3.5 h-3.5 text-[#3fb8d9]" />
                        {pdv.direccion}, {pdv.localidad}
                      </p>
                    </div>
                  );
                })}
              </div>
            )}

            {/* Preferencias de entrega */}
            <div className="pt-3 border-t border-[#f0f0f4] grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                  Preferencia horaria de recepción
                </label>
                <div className="grid grid-cols-2 gap-2">
                  <button
                    type="button"
                    onClick={() => setHorarioEntrega('mañana')}
                    className={`py-2 px-3 rounded-[10px] text-[12px] font-medium border text-center transition-all ${
                      horarioEntrega === 'mañana'
                        ? 'bg-[#2b2b3a] text-white border-[#2b2b3a]'
                        : 'bg-white text-[#6b6b80] border-[#ececf2] hover:bg-[#fafafd]'
                    }`}
                  >
                    Mañana (09 - 13 hs)
                  </button>
                  <button
                    type="button"
                    onClick={() => setHorarioEntrega('tarde')}
                    className={`py-2 px-3 rounded-[10px] text-[12px] font-medium border text-center transition-all ${
                      horarioEntrega === 'tarde'
                        ? 'bg-[#2b2b3a] text-white border-[#2b2b3a]'
                        : 'bg-white text-[#6b6b80] border-[#ececf2] hover:bg-[#fafafd]'
                    }`}
                  >
                    Tarde (14 - 18 hs)
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                  Indicaciones para el distribuidor (Opcional)
                </label>
                <input
                  type="text"
                  value={observacionEntrega}
                  onChange={(e) => setObservacionEntrega(e.target.value)}
                  placeholder="Ej: Tocar timbre en depósito"
                  className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>
            </div>
          </section>

          {/* PASO 2: Selección de Método de Pago */}
          <section className="bg-white rounded-[22px] p-6 border border-[#ececf2] shadow-sm space-y-5">
            <div className="flex items-center gap-3">
              <span className="w-7 h-7 rounded-full bg-[#ffe2f7] text-[#e86bc4] font-bold text-[13px] flex items-center justify-center">
                2
              </span>
              <div>
                <h2 className="text-[16px] font-bold text-[#2b2b3a]">
                  Forma de Pago
                </h2>
                <p className="text-[12px] text-[#6b6b80]">
                  Elegí cómo abonarás tu pedido mayorista.
                </p>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <button
                type="button"
                onClick={() => setPaymentMethod('mercadopago')}
                className={`p-4 rounded-[16px] border text-left cursor-pointer transition-all ${
                  paymentMethod === 'mercadopago'
                    ? 'border-[#009ee3] bg-[#f0faff] shadow-[0_4px_16px_rgba(0,158,227,0.15)] ring-1 ring-[#009ee3]'
                    : 'border-[#ececf2] hover:border-[#dcdce6] bg-[#fafafd]'
                }`}
              >
                <div className="flex items-center justify-between mb-2">
                  <div className="w-8 h-8 rounded-lg bg-[#009ee3]/10 flex items-center justify-center text-[#009ee3]">
                    <Icon name="qrCode" className="w-5 h-5" />
                  </div>
                  <span className="text-[9px] font-bold px-2 py-0.5 rounded-full bg-[#dff8e9] text-[#39855b]">
                    Instantáneo
                  </span>
                </div>
                <strong className="block text-[13px] text-[#2b2b3a]">Mercado Pago QR</strong>
                <p className="text-[11px] text-[#6b6b80] mt-0.5">Billetera virtual o dinero en cuenta</p>
              </button>

              <button
                type="button"
                onClick={() => setPaymentMethod('transferencia')}
                className={`p-4 rounded-[16px] border text-left cursor-pointer transition-all ${
                  paymentMethod === 'transferencia'
                    ? 'border-[#e86bc4] bg-[#fff5fc] shadow-[0_4px_16px_rgba(232,107,196,0.15)] ring-1 ring-[#e86bc4]'
                    : 'border-[#ececf2] hover:border-[#dcdce6] bg-[#fafafd]'
                }`}
              >
                <div className="flex items-center justify-between mb-2">
                  <div className="w-8 h-8 rounded-lg bg-[#e86bc4]/10 flex items-center justify-center text-[#e86bc4]">
                    <Icon name="box" className="w-5 h-5" />
                  </div>
                  <span className="text-[9px] font-bold px-2 py-0.5 rounded-full bg-[#faf0d9] text-[#b88014]">
                    Conciliación
                  </span>
                </div>
                <strong className="block text-[13px] text-[#2b2b3a]">Transferencia</strong>
                <p className="text-[11px] text-[#6b6b80] mt-0.5">CBU o Alias directo del mayorista</p>
              </button>

              <button
                type="button"
                onClick={() => setPaymentMethod('efectivo')}
                className={`p-4 rounded-[16px] border text-left cursor-pointer transition-all ${
                  paymentMethod === 'efectivo'
                    ? 'border-[#7ed6a5] bg-[#f2fdf7] shadow-[0_4px_16px_rgba(126,214,165,0.2)] ring-1 ring-[#7ed6a5]'
                    : 'border-[#ececf2] hover:border-[#dcdce6] bg-[#fafafd]'
                }`}
              >
                <div className="flex items-center justify-between mb-2">
                  <div className="w-8 h-8 rounded-lg bg-[#7ed6a5]/15 flex items-center justify-center text-[#39855b]">
                    <Icon name="truck" className="w-5 h-5" />
                  </div>
                  <span className="text-[9px] font-bold px-2 py-0.5 rounded-full bg-[#f0f0f4] text-[#6b6b80]">
                    Al recibir
                  </span>
                </div>
                <strong className="block text-[13px] text-[#2b2b3a]">Efectivo</strong>
                <p className="text-[11px] text-[#6b6b80] mt-0.5">Contra entrega con el distribuidor</p>
              </button>
            </div>

            {paymentMethod === 'mercadopago' && (
              <div className="p-5 rounded-[18px] bg-gradient-to-br from-[#f8fcff] to-[#eef7fd] border border-[#d2ecfa] space-y-4">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-[#d2ecfa] pb-3">
                  <div className="flex items-center gap-2.5">
                    <div className="w-9 h-9 rounded-xl bg-[#009ee3] text-white flex items-center justify-center font-bold text-sm shadow-sm">
                      MP
                    </div>
                    <div>
                      <h3 className="text-[14px] font-bold text-[#0f344d]">
                        Escaneo de QR Dinámico
                      </h3>
                      <p className="text-[11px] text-[#55819e]">
                        Acreditación por ${total.toLocaleString('es-AR')}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-1.5 bg-white px-3 py-1.5 rounded-full border border-[#d2ecfa] text-[11px] text-[#009ee3] font-semibold self-start sm:self-auto">
                    <Icon name="clock" className="w-3.5 h-3.5" />
                    <span>Expira en: <strong>{formatTimer(timeLeft)}</strong></span>
                  </div>
                </div>

                <div className="pt-2">
                  <label className="block text-[11px] font-bold text-[#0f344d] mb-1.5">
                    Estado de respuesta de pasarela:
                  </label>
                  <div className="flex flex-wrap gap-2">
                    {[
                      { id: 'exito', label: '✓ Aprobado', color: 'bg-[#dff8e9] text-[#39855b]' },
                      { id: 'pendiente', label: '⏳ En proceso', color: 'bg-[#faf0d9] text-[#b88014]' },
                      { id: 'fallo', label: '✕ Rechazado', color: 'bg-[#ffe2e7] text-[#c95569]' },
                    ].map((s) => (
                      <button
                        key={s.id}
                        type="button"
                        onClick={() => setSimulatedOutcome(s.id)}
                        className={`text-[11px] font-bold px-3 py-1 rounded-[8px] border transition-all ${
                          simulatedOutcome === s.id
                            ? `${s.color} border-current shadow-sm`
                            : 'bg-white text-[#6b6b80] border-[#ececf2] hover:bg-slate-50'
                        }`}
                      >
                        {s.label}
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {paymentMethod === 'transferencia' && (
              <div className="p-5 rounded-[18px] bg-[#fafafd] border border-[#ececf2] space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-[12px]">
                  <div className="p-3 bg-white rounded-[12px] border border-[#ececf2] flex items-center justify-between">
                    <div>
                      <span className="text-[10px] text-[#aaa9ba] block uppercase font-bold">Alias CBU</span>
                      <strong className="text-[#2b2b3a] font-mono text-[13px]">{DATOS_BANCARIOS.alias}</strong>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleCopy(DATOS_BANCARIOS.alias, 'alias')}
                      className="text-[11px] font-bold text-[#e86bc4] hover:underline flex items-center gap-1"
                    >
                      <Icon name="copy" className="w-3.5 h-3.5" />
                      {copiadoKey === 'alias' ? '¡Copiado!' : 'Copiar'}
                    </button>
                  </div>

                  <div className="p-3 bg-white rounded-[12px] border border-[#ececf2] flex items-center justify-between">
                    <div>
                      <span className="text-[10px] text-[#aaa9ba] block uppercase font-bold">CBU</span>
                      <strong className="text-[#2b2b3a] font-mono text-[11px]">{DATOS_BANCARIOS.cbu}</strong>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleCopy(DATOS_BANCARIOS.cbu, 'cbu')}
                      className="text-[11px] font-bold text-[#e86bc4] hover:underline flex items-center gap-1"
                    >
                      <Icon name="copy" className="w-3.5 h-3.5" />
                      {copiadoKey === 'cbu' ? '¡Copiado!' : 'Copiar'}
                    </button>
                  </div>
                </div>

                <div className="pt-3 border-t border-[#f0f0f4] space-y-3">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    <div>
                      <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                        N° de Comprobante / Transacción *
                      </label>
                      <input
                        type="text"
                        required
                        value={transferenciaData.numeroComprobante}
                        onChange={(e) => setTransferenciaData({ ...transferenciaData, numeroComprobante: e.target.value })}
                        placeholder="Ej: TRF-9023418"
                        className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4] font-mono"
                      />
                    </div>

                    <div>
                      <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                        Banco emisor
                      </label>
                      <input
                        type="text"
                        value={transferenciaData.bancoEmisor}
                        onChange={(e) => setTransferenciaData({ ...transferenciaData, bancoEmisor: e.target.value })}
                        placeholder="Ej: Banco Nación, Macro..."
                        className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                      Adjuntar comprobante (Opcional)
                    </label>
                    <label className="border-2 border-dashed border-[#ececf2] hover:border-[#e86bc4] rounded-[12px] p-4 bg-white flex flex-col items-center justify-center cursor-pointer transition-colors text-center">
                      <Icon name="box" className="w-6 h-6 text-[#aaa9ba] mb-1" />
                      <span className="text-[12px] text-[#2b2b3a] font-medium">
                        {transferenciaData.comprobanteArchivo
                          ? `${transferenciaData.comprobanteArchivo.name} (${transferenciaData.comprobanteArchivo.size})`
                          : 'Hacé clic acá para adjuntar tu comprobante'}
                      </span>
                      <input
                        type="file"
                        accept="image/*,application/pdf"
                        onChange={handleFileChange}
                        className="hidden"
                      />
                    </label>
                  </div>
                </div>
              </div>
            )}

            {paymentMethod === 'efectivo' && (
              <div className="p-5 rounded-[18px] bg-[#f9fdfa] border border-[#d2f3e2] space-y-3">
                <div className="flex items-center gap-2 text-[#39855b]">
                  <Icon name="check" className="w-5 h-5" />
                  <h3 className="text-[14px] font-bold">
                    Cobro en Efectivo contra Entrega
                  </h3>
                </div>
                <p className="text-[12px] text-[#4a6b57]">
                  El distribuidor cobrará el importe en efectivo al momento de entregar los bultos en tu comercio.
                </p>
              </div>
            )}
          </section>
        </div>

        {/* Columna Lateral: Resumen del Pedido (4 cols) */}
        <div className="lg:col-span-4 space-y-5">
          <div className="bg-white rounded-[22px] p-6 border border-[#ececf2] shadow-sm space-y-5 sticky top-24">
            <h3 className="text-[16px] font-bold text-[#2b2b3a] pb-3 border-b border-[#f0f0f4]">
              Resumen del Pedido
            </h3>

            <div className="space-y-3 max-h-56 overflow-y-auto pr-1">
              {cartItems.map((item) => (
                <div key={item.producto.id} className="flex items-center gap-3 text-[12px]">
                  <img
                    src={item.producto.imagen}
                    alt={item.producto.nombre}
                    className="w-10 h-10 object-cover rounded-[8px] border border-[#ececf2] shrink-0"
                  />
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-[#2b2b3a] truncate">{item.producto.nombre}</p>
                    <span className="text-[11px] text-[#aaa9ba]">
                      {item.cantidad} bultos × ${Number(item.producto.precioNum || 0).toLocaleString('es-AR')}
                    </span>
                  </div>
                  <strong className="font-bold text-[#2b2b3a]">
                    ${(Number(item.producto.precioNum || 0) * item.cantidad).toLocaleString('es-AR')}
                  </strong>
                </div>
              ))}
            </div>

            <div className="space-y-2.5 pt-3 border-t border-[#f0f0f4] text-[13px]">
              <div className="flex justify-between text-[#6b6b80]">
                <span>Subtotal mercadería</span>
                <strong className="text-[#2b2b3a]">${subtotal.toLocaleString('es-AR')}</strong>
              </div>

              {descuento > 0 && (
                <div className="flex justify-between text-[#39855b]">
                  <span>Descuento ({appliedPromo?.code})</span>
                  <strong>−${descuento.toLocaleString('es-AR')}</strong>
                </div>
              )}

              <div className="flex justify-between text-[#6b6b80]">
                <span>Envío Gran Mendoza</span>
                <span className="text-[#39855b] font-semibold">Gratis</span>
              </div>

              <div className="pt-3 border-t border-[#ececf2] flex justify-between items-baseline">
                <span className="text-[15px] font-bold text-[#2b2b3a]">Total a Pagar</span>
                <strong className="text-2xl font-bold text-[#e86bc4]">
                  ${total.toLocaleString('es-AR')}
                </strong>
              </div>
            </div>

            <Button
              type="submit"
              disabled={isProcessing}
              className="w-full py-3.5 text-[14px] justify-center cursor-pointer shadow-md"
            >
              {isProcessing ? 'Procesando pedido...' : 'Confirmar Pedido'}
            </Button>

            <div className="text-center">
              <Link to="/carrito" className="text-[11px] text-[#6b6b80] hover:text-[#e86bc4] font-medium">
                ← Modificar bultos en el carrito
              </Link>
            </div>
          </div>
        </div>
      </form>
    </div>
  );
}
