import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import Button from '../components/Button';
import Icon from '../components/Icon';
import { cartMock } from '../mocks/products';
import { puntosDeVentaMock, datosBancariosMock } from '../mocks/checkoutMock';

export default function Checkout() {
  const navigate = useNavigate();

  // Estados de entrega
  const [puntosDeVenta] = useState(puntosDeVentaMock);
  const [selectedPdvId, setSelectedPdvId] = useState(puntosDeVentaMock[0]?.id || '');
  const [horarioEntrega, setHorarioEntrega] = useState('mañana');
  const [observacionEntrega, setObservacionEntrega] = useState('');

  // Método de pago: 'mercadopago' | 'transferencia' | 'efectivo'
  const [paymentMethod, setPaymentMethod] = useState('mercadopago');

  // Estado de Transferencia
  const [transferenciaData, setTransferenciaData] = useState({
    numeroComprobante: '',
    bancoEmisor: 'Banco Galicia',
    comprobanteArchivo: null
  });
  const [copiadoKey, setCopiadoKey] = useState(null);

  // Estado de Mercado Pago QR y temporizador
  const [timeLeft, setTimeLeft] = useState(900); // 15 minutos en segundos
  const [simulatedOutcome, setSimulatedOutcome] = useState('exito'); // 'exito' | 'fallo' | 'pendiente'
  const [isProcessing, setIsProcessing] = useState(false);

  // Contador regresivo para QR
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
          size: (file.size / 1024).toFixed(1) + ' KB'
        }
      }));
    }
  };

  // Totales calculados
  const subtotal = cartMock.reduce((acc, it) => acc + it.producto.precioNum * it.cantidad, 0);
  const descuento = Math.round(subtotal * 0.10); // Promo DULCE10 simulada
  const total = Math.max(0, subtotal - descuento);
  const selectedPdv = puntosDeVenta.find((p) => p.id === selectedPdvId) || puntosDeVenta[0];

  const handleProcessOrder = (e) => {
    e.preventDefault();
    setIsProcessing(true);

    setTimeout(() => {
      setIsProcessing(false);
      const orderPayload = {
        orderId: `DM-${Math.floor(3000 + Math.random() * 900)}`,
        comercio: selectedPdv?.nombre,
        direccion: `${selectedPdv?.direccion}, ${selectedPdv?.localidad}`,
        total,
        subtotal,
        descuento,
        metodoPago:
          paymentMethod === 'mercadopago'
            ? 'Billetera Virtual (Mercado Pago QR)'
            : paymentMethod === 'transferencia'
            ? 'Transferencia Bancaria'
            : 'Efectivo contra entrega',
        comprobante: transferenciaData.numeroComprobante || null,
        fecha: new Date().toLocaleDateString('es-AR', {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        })
      };

      if (paymentMethod === 'mercadopago') {
        if (simulatedOutcome === 'fallo') {
          navigate('/pago/fallo', { state: orderPayload });
        } else if (simulatedOutcome === 'pendiente') {
          navigate('/pago/pendiente', { state: orderPayload });
        } else {
          navigate('/pago/exito', { state: orderPayload });
        }
      } else if (paymentMethod === 'transferencia') {
        // Transferencia va a pendiente de validación bancaria
        navigate('/pago/pendiente', { state: orderPayload });
      } else {
        // Efectivo contra entrega
        navigate('/pago/exito', { state: orderPayload });
      }
    }, 1000);
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
                    <p className="text-[11px] text-[#aaa9ba] mt-1">
                      Horario: {pdv.horarioAtencion}
                    </p>
                  </div>
                );
              })}
            </div>

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
                  placeholder="Ej: Tocar timbre en depósito / Recibe Juan"
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

            {/* Selector de métodos (Tabs/Cards) */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              {/* Opción 1: Mercado Pago */}
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

              {/* Opción 2: Transferencia Bancaria */}
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

              {/* Opción 3: Efectivo */}
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

            {/* CONTENIDO ESPECÍFICO DEL MÉTODO SELECCIONADO */}

            {/* Caso 1: MERCADO PAGO QR */}
            {paymentMethod === 'mercadopago' && (
              <div className="p-5 rounded-[18px] bg-gradient-to-br from-[#f8fcff] to-[#eef7fd] border border-[#d2ecfa] space-y-4">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-[#d2ecfa] pb-3">
                  <div className="flex items-center gap-2.5">
                    <div className="w-9 h-9 rounded-xl bg-[#009ee3] text-white flex items-center justify-center font-bold text-sm shadow-sm">
                      MP
                    </div>
                    <div>
                      <h3 className="text-[14px] font-bold text-[#0f344d]">
                        Escaneo de QR Dinámico (Preferencia Mercado Pago)
                      </h3>
                      <p className="text-[11px] text-[#55819e]">
                        Acreditación en tiempo real sin esperas
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-1.5 bg-white px-3 py-1.5 rounded-full border border-[#d2ecfa] text-[11px] text-[#009ee3] font-semibold self-start sm:self-auto">
                    <Icon name="clock" className="w-3.5 h-3.5" />
                    <span>Expira en: <strong>{formatTimer(timeLeft)}</strong></span>
                  </div>
                </div>

                <div className="flex flex-col md:flex-row items-center gap-6 py-2">
                  {/* QR Box */}
                  <div className="relative p-4 bg-white border-2 border-[#009ee3]/30 rounded-[20px] shadow-sm flex flex-col items-center">
                    <div className="w-44 h-44 bg-white p-2 border border-[#ececf2] rounded-[14px] flex items-center justify-center">
                      {/* Simulación visual de código QR */}
                      <svg className="w-full h-full text-[#2b2b3a]" viewBox="0 0 100 100" fill="currentColor">
                        {/* Marcadores de esquina */}
                        <rect x="5" y="5" width="25" height="25" rx="3" />
                        <rect x="9" y="9" width="17" height="17" fill="white" />
                        <rect x="13" y="13" width="9" height="9" />

                        <rect x="70" y="5" width="25" height="25" rx="3" />
                        <rect x="74" y="9" width="17" height="17" fill="white" />
                        <rect x="78" y="13" width="9" height="9" />

                        <rect x="5" y="70" width="25" height="25" rx="3" />
                        <rect x="9" y="74" width="17" height="17" fill="white" />
                        <rect x="13" y="78" width="9" height="9" />

                        {/* Patrones de datos simulados */}
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

                        {/* Centro con isotipo de Dulce Mayor */}
                        <circle cx="50" cy="50" r="10" fill="white" />
                        <circle cx="50" cy="50" r="7" fill="#009ee3" />
                      </svg>
                    </div>
                    <span className="text-[10px] text-[#55819e] font-semibold mt-2">
                      Código QR interoperable
                    </span>
                  </div>

                  {/* Pasos para el cliente */}
                  <div className="space-y-3 flex-1 text-[12px] text-[#426c88]">
                    <div className="flex items-start gap-2.5">
                      <span className="w-5 h-5 rounded-full bg-[#009ee3]/15 text-[#009ee3] font-bold text-[11px] flex items-center justify-center shrink-0 mt-0.5">
                        1
                      </span>
                      <p>Abrí la app de <strong>Mercado Pago</strong> o tu aplicación bancaria preferida con lector QR.</p>
                    </div>
                    <div className="flex items-start gap-2.5">
                      <span className="w-5 h-5 rounded-full bg-[#009ee3]/15 text-[#009ee3] font-bold text-[11px] flex items-center justify-center shrink-0 mt-0.5">
                        2
                      </span>
                      <p>Escaneá este código QR con la cámara de tu celular.</p>
                    </div>
                    <div className="flex items-start gap-2.5">
                      <span className="w-5 h-5 rounded-full bg-[#009ee3]/15 text-[#009ee3] font-bold text-[11px] flex items-center justify-center shrink-0 mt-0.5">
                        3
                      </span>
                      <p>Confirmá el pago por <strong>${total.toLocaleString('es-AR')}</strong> con dinero en cuenta.</p>
                    </div>

                    {/* Selector de Simulación para testing */}
                    <div className="pt-2 border-t border-[#d2ecfa]/80 mt-3">
                      <label className="block text-[11px] font-bold text-[#0f344d] mb-1.5">
                        🛠️ Modo Demostración (Simular respuesta de Mercado Pago):
                      </label>
                      <div className="flex flex-wrap gap-2">
                        {[
                          { id: 'exito', label: '✓ Aprobado', color: 'bg-[#dff8e9] text-[#39855b]' },
                          { id: 'pendiente', label: '⏳ En proceso', color: 'bg-[#faf0d9] text-[#b88014]' },
                          { id: 'fallo', label: '✕ Rechazado', color: 'bg-[#ffe2e7] text-[#c95569]' }
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
                </div>
              </div>
            )}

            {/* Caso 2: TRANSFERENCIA BANCARIA */}
            {paymentMethod === 'transferencia' && (
              <div className="p-5 rounded-[18px] bg-[#fafafd] border border-[#ececf2] space-y-4">
                <div className="flex items-center justify-between border-b border-[#f0f0f4] pb-3">
                  <div>
                    <h3 className="text-[14px] font-bold text-[#2b2b3a]">
                      Datos Bancarios para Transferencia Directa
                    </h3>
                    <p className="text-[11px] text-[#6b6b80]">
                      Cuenta Corriente oficial de Dulce Mayor S.R.L.
                    </p>
                  </div>
                  <span className="text-[10px] font-semibold bg-white border border-[#ececf2] text-[#6b6b80] px-2.5 py-1 rounded-md">
                    Sin comisión adicional
                  </span>
                </div>

                {/* Grilla de datos para copiar */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-[12px]">
                  <div className="p-3 bg-white rounded-[12px] border border-[#ececf2] flex items-center justify-between">
                    <div>
                      <span className="text-[10px] text-[#aaa9ba] block uppercase font-bold">Alias CBU</span>
                      <strong className="text-[#2b2b3a] font-mono text-[13px]">{datosBancariosMock.alias}</strong>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleCopy(datosBancariosMock.alias, 'alias')}
                      className="text-[11px] font-bold text-[#e86bc4] hover:underline flex items-center gap-1"
                    >
                      <Icon name="copy" className="w-3.5 h-3.5" />
                      {copiadoKey === 'alias' ? '¡Copiado!' : 'Copiar'}
                    </button>
                  </div>

                  <div className="p-3 bg-white rounded-[12px] border border-[#ececf2] flex items-center justify-between">
                    <div>
                      <span className="text-[10px] text-[#aaa9ba] block uppercase font-bold">CBU</span>
                      <strong className="text-[#2b2b3a] font-mono text-[11px]">{datosBancariosMock.cbu}</strong>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleCopy(datosBancariosMock.cbu, 'cbu')}
                      className="text-[11px] font-bold text-[#e86bc4] hover:underline flex items-center gap-1"
                    >
                      <Icon name="copy" className="w-3.5 h-3.5" />
                      {copiadoKey === 'cbu' ? '¡Copiado!' : 'Copiar'}
                    </button>
                  </div>

                  <div className="p-3 bg-white rounded-[12px] border border-[#ececf2]">
                    <span className="text-[10px] text-[#aaa9ba] block uppercase font-bold">Banco & Tipo</span>
                    <span className="text-[#2b2b3a] font-medium">{datosBancariosMock.banco} ({datosBancariosMock.tipoCuenta})</span>
                  </div>

                  <div className="p-3 bg-white rounded-[12px] border border-[#ececf2]">
                    <span className="text-[10px] text-[#aaa9ba] block uppercase font-bold">Titular & CUIT</span>
                    <span className="text-[#2b2b3a] font-medium">{datosBancariosMock.titular} (CUIT {datosBancariosMock.cuit})</span>
                  </div>
                </div>

                {/* Declaración de Comprobante */}
                <div className="pt-3 border-t border-[#f0f0f4] space-y-3">
                  <h4 className="text-[13px] font-bold text-[#2b2b3a]">
                    Informar Comprobante de Transferencia
                  </h4>
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
                        placeholder="Ej: Banco Macro, Santander, etc."
                        className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                      />
                    </div>
                  </div>

                  {/* Dropzone de comprobante */}
                  <div>
                    <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                      Adjuntar captura o comprobante (Opcional)
                    </label>
                    <label className="border-2 border-dashed border-[#ececf2] hover:border-[#e86bc4] rounded-[12px] p-4 bg-white flex flex-col items-center justify-center cursor-pointer transition-colors text-center">
                      <Icon name="box" className="w-6 h-6 text-[#aaa9ba] mb-1" />
                      <span className="text-[12px] text-[#2b2b3a] font-medium">
                        {transferenciaData.comprobanteArchivo
                          ? `${transferenciaData.comprobanteArchivo.name} (${transferenciaData.comprobanteArchivo.size})`
                          : 'Hacé clic acá para adjuntar tu comprobante (PNG, JPG o PDF)'}
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

            {/* Caso 3: EFECTIVO CONTRA ENTREGA */}
            {paymentMethod === 'efectivo' && (
              <div className="p-5 rounded-[18px] bg-[#f9fdfa] border border-[#d2f3e2] space-y-3">
                <div className="flex items-center gap-2 text-[#39855b]">
                  <Icon name="check" className="w-5 h-5" />
                  <h3 className="text-[14px] font-bold">
                    Cobro en Efectivo contra Entrega
                  </h3>
                </div>
                <p className="text-[12px] text-[#4a6b57]">
                  El distribuidor de Dulce Mayor cobrará el importe exacto en efectivo al momento de entregar los bultos en tu comercio. Te entregará el remito firmado y la factura correspondiente.
                </p>
                <div className="p-3 bg-white rounded-[12px] border border-[#d2f3e2] text-[11px] text-[#6b6b80] flex items-center justify-between">
                  <span>Monto exacto a preparar:</span>
                  <strong className="text-[14px] text-[#2b2b3a] font-bold">
                    ${total.toLocaleString('es-AR')}
                  </strong>
                </div>
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

            {/* Lista resumida de ítems */}
            <div className="space-y-3 max-h-56 overflow-y-auto pr-1">
              {cartMock.map((item) => (
                <div key={item.producto.id} className="flex items-center gap-3 text-[12px]">
                  <img
                    src={item.producto.imagen}
                    alt={item.producto.nombre}
                    className="w-10 h-10 object-cover rounded-[8px] border border-[#ececf2] shrink-0"
                  />
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-[#2b2b3a] truncate">{item.producto.nombre}</p>
                    <span className="text-[11px] text-[#aaa9ba]">
                      {item.cantidad} bultos × ${item.producto.precioNum.toLocaleString('es-AR')}
                    </span>
                  </div>
                  <strong className="font-bold text-[#2b2b3a]">
                    ${(item.producto.precioNum * item.cantidad).toLocaleString('es-AR')}
                  </strong>
                </div>
              ))}
            </div>

            {/* Desglose de totales */}
            <div className="space-y-2.5 pt-3 border-t border-[#f0f0f4] text-[13px]">
              <div className="flex justify-between text-[#6b6b80]">
                <span>Subtotal mercadería</span>
                <strong className="text-[#2b2b3a]">${subtotal.toLocaleString('es-AR')}</strong>
              </div>

              {descuento > 0 && (
                <div className="flex justify-between text-[#39855b]">
                  <span>Cupón DULCE10 (10%)</span>
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

            {/* Botón de Confirmación Principal */}
            <Button
              type="submit"
              disabled={isProcessing}
              className="w-full py-3.5 text-[14px] justify-center cursor-pointer shadow-md"
            >
              {isProcessing ? (
                <span className="flex items-center gap-2">
                  <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  Procesando pedido...
                </span>
              ) : paymentMethod === 'mercadopago' ? (
                'Confirmar y Pagar con Mercado Pago'
              ) : paymentMethod === 'transferencia' ? (
                'Confirmar Transferencia Bancaria'
              ) : (
                'Confirmar Pedido en Efectivo'
              )}
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
