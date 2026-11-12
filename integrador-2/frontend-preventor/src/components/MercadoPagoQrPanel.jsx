/**
 * Panel reutilizable para generar, mostrar, compartir y monitorear en tiempo real
 * el cobro mediante QR dinámico de Mercado Pago (In-Store QR o respaldo Checkout Preferences).
 */
import { useState, useEffect, useCallback, useRef } from 'react';
import Icon from './Icon';
import { pagoService } from '../services/pagoService';

export default function MercadoPagoQrPanel({
  pedidoId,
  numeroPedido,
  nombreComercio,
  monto,
  qrInicial = null,
  onPagoAprobado,
  onPosponerPago,
}) {
  const [qrData, setQrData] = useState(qrInicial);
  const [timeLeft, setTimeLeft] = useState(
    qrInicial?.tiempoRestanteSegundos ?? qrInicial?.expiraEnSegundos ?? 900
  );
  const [generando, setGenerando] = useState(false);
  const [errorQr, setErrorQr] = useState(null);
  const [copiado, setCopiado] = useState(false);
  const pagoNotificadoRef = useRef(false);
  const onPagoAprobadoRef = useRef(onPagoAprobado);

  useEffect(() => {
    onPagoAprobadoRef.current = onPagoAprobado;
  }, [onPagoAprobado]);

  const generarQr = useCallback(async () => {
    if (!pedidoId) return;
    try {
      setGenerando(true);
      setErrorQr(null);
      const res = await pagoService.iniciarPagoQr(pedidoId);
      setQrData(res);
      setTimeLeft(res?.tiempoRestanteSegundos ?? res?.expiraEnSegundos ?? 900);
    } catch (err) {
      setErrorQr(err.message || 'No se pudo generar el código QR con Mercado Pago.');
    } finally {
      setGenerando(false);
    }
  }, [pedidoId]);

  useEffect(() => {
    pagoNotificadoRef.current = false;
    if (qrInicial?.qrImagenBase64) {
      setQrData(qrInicial);
      setTimeLeft(qrInicial?.tiempoRestanteSegundos ?? qrInicial?.expiraEnSegundos ?? 900);
      setErrorQr(null);
    } else if (pedidoId) {
      generarQr();
    }
  }, [pedidoId, qrInicial, generarQr]);

  // Cuenta regresiva del QR
  useEffect(() => {
    if (!pedidoId) return;
    const timer = setInterval(() => {
      setTimeLeft((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => clearInterval(timer);
  }, [pedidoId]);

  const qrExpirado = timeLeft === 0;

  // Polling de acreditación cada 3 segundos
  useEffect(() => {
    if (!pedidoId || qrExpirado || pagoNotificadoRef.current) return;

    const interval = setInterval(async () => {
      try {
        const estado = await pagoService.consultarEstadoQr(pedidoId);
        if (
          estado &&
          (estado.qrData === 'APROBADO' ||
            estado.estadoPago === 'APROBADO' ||
            estado.estado === 'APROBADO' ||
            estado.estado === 'FACTURADO')
        ) {
          if (!pagoNotificadoRef.current) {
            pagoNotificadoRef.current = true;
            clearInterval(interval);
            onPagoAprobadoRef.current?.(estado);
          }
        }
      } catch {
        // Silencioso mientras el cliente finaliza el pago
      }
    }, 3000);

    return () => clearInterval(interval);
  }, [pedidoId, qrExpirado]);

  const formatTimer = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const handleCopyLink = () => {
    if (!qrData?.qrData) return;
    navigator.clipboard?.writeText(qrData.qrData);
    setCopiado(true);
    setTimeout(() => setCopiado(false), 2000);
  };

  const isCheckoutLink = Boolean(qrData?.qrData && qrData.qrData.startsWith('http'));
  const montoFormateado = Number(monto || qrData?.monto || 0).toLocaleString('es-AR');
  const etiquetaPedido = numeroPedido ? `DM-${numeroPedido}` : `#${String(pedidoId || '').slice(-6)}`;
  const whatsappHref = isCheckoutLink
    ? `https://wa.me/?text=${encodeURIComponent(
        `Hola! Te comparto el enlace de Mercado Pago para abonar el pedido ${etiquetaPedido}${
          nombreComercio ? ` de ${nombreComercio}` : ''
        } por $${montoFormateado}: ${qrData.qrData}`
      )}`
    : null;

  return (
    <div className="p-4 sm:p-5 rounded-[18px] bg-[#f4fbfe] border border-[#cdecfb] space-y-4">
      {/* Cabecera */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 border-b border-[#d2ecfa]">
        <div className="flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-[10px] bg-[#009ee3] text-white font-extrabold text-xs flex items-center justify-center shrink-0">
            MP
          </div>
          <div>
            <h3 className="text-[14px] font-bold text-[#0f344d]">
              Cobro QR Mercado Pago
            </h3>
            <p className="text-[11px] text-[#426c88]">
              Pedido {etiquetaPedido} · Total: <strong>${montoFormateado}</strong>
            </p>
          </div>
        </div>

        <div className="flex items-center gap-1.5 bg-white px-3 py-1.5 rounded-full border border-[#d2ecfa] text-[11px] text-[#009ee3] font-semibold self-start sm:self-auto">
          <Icon name="clock" className="w-3.5 h-3.5" />
          <span>
            Expira en: <strong>{formatTimer(timeLeft)}</strong>
          </span>
        </div>
      </div>

      {/* Estado de Error */}
      {errorQr && (
        <div className="p-3 bg-[#ffe8ed] border border-[#ffccd5] rounded-[12px] text-[12px] text-[#e5484d] flex items-center justify-between gap-2">
          <span>{errorQr}</span>
          <button
            type="button"
            onClick={generarQr}
            disabled={generando}
            className="px-2.5 py-1 rounded-[8px] bg-white border border-[#ffccd5] text-[#e5484d] font-bold text-[11px] cursor-pointer shrink-0"
          >
            Reintentar
          </button>
        </div>
      )}

      {/* Contenido Principal QR + Pasos */}
      <div className="flex flex-col sm:flex-row items-center gap-5">
        <div className="flex flex-col items-center shrink-0">
          <div className="w-52 h-52 bg-white p-2.5 border-2 border-[#009ee3]/30 rounded-[18px] shadow-sm flex items-center justify-center relative overflow-hidden">
            {generando ? (
              <div className="flex flex-col items-center gap-2 text-center p-4">
                <div className="w-7 h-7 border-3 border-[#009ee3] border-t-transparent rounded-full animate-spin" />
                <span className="text-[11px] text-[#55819e] font-semibold">
                  Generando QR oficial...
                </span>
              </div>
            ) : qrData?.qrImagenBase64 ? (
              <img
                src={
                  qrData.qrImagenBase64.startsWith('data:')
                    ? qrData.qrImagenBase64
                    : `data:image/png;base64,${qrData.qrImagenBase64}`
                }
                alt="Código QR Mercado Pago"
                className="w-full h-full object-contain"
              />
            ) : (
              <div className="text-center p-3 text-[11px] text-[#55819e]">
                QR no disponible todavía.
              </div>
            )}

            {timeLeft === 0 && !generando && (
              <div className="absolute inset-0 bg-white/95 backdrop-blur-[2px] flex flex-col items-center justify-center p-3 text-center z-10">
                <p className="text-[11px] font-bold text-red-500 mb-2">
                  QR Expirado (15 min)
                </p>
                <button
                  type="button"
                  onClick={generarQr}
                  className="px-3 py-1.5 rounded-[8px] bg-[#009ee3] text-white text-[11px] font-bold hover:bg-[#0089c7] transition-colors cursor-pointer flex items-center gap-1 shadow-sm"
                >
                  <Icon name="refresh" className="w-3.5 h-3.5" />
                  <span>Regenerar QR</span>
                </button>
              </div>
            )}
          </div>

          <span className="text-[10px] text-[#55819e] font-semibold mt-2 text-center">
            {isCheckoutLink
              ? 'QR / Link de Checkout Mercado Pago'
              : 'Código QR EMVCo Interoperable Oficial'}
          </span>

          <button
            type="button"
            onClick={generarQr}
            disabled={generando}
            className="mt-1 text-[11px] text-[#009ee3] hover:underline font-bold cursor-pointer flex items-center gap-1"
          >
            <Icon name="refresh" className="w-3 h-3" />
            <span>{generando ? 'Actualizando...' : 'Regenerar QR'}</span>
          </button>
        </div>

        {/* Instrucciones y acciones de campo */}
        <div className="flex-1 w-full space-y-3 text-[12px] text-[#426c88]">
          <div className="p-3 bg-white rounded-[12px] border border-[#d2ecfa] space-y-1.5">
            <div className="flex items-center gap-2 text-[#39855b] font-semibold text-[11px]">
              <span className="w-2 h-2 rounded-full bg-[#39855b] animate-ping" />
              <span>Verificación en tiempo real activa</span>
            </div>
            <p className="text-[11px] text-[#6b6b80]">
              Mostrá este QR al comerciante. Apenas confirme el pago, la orden pasará automáticamente a facturación.
            </p>
          </div>

          {isCheckoutLink && (
            <div className="p-3 bg-white rounded-[12px] border border-[#d2ecfa] space-y-2">
              <span className="text-[10px] font-bold uppercase tracking-wider text-[#009ee3] block">
                Opciones de enlace directo (Checkout Pro)
              </span>
              <div className="flex flex-wrap gap-2">
                <button
                  type="button"
                  onClick={handleCopyLink}
                  className="px-2.5 py-1.5 rounded-[8px] bg-[#eaf8fc] hover:bg-[#d7f1fa] text-[#0f344d] text-[11px] font-bold flex items-center gap-1 cursor-pointer transition-colors"
                >
                  <Icon name="copy" className="w-3.5 h-3.5" />
                  <span>{copiado ? '¡Copiado!' : 'Copiar link'}</span>
                </button>

                {whatsappHref && (
                  <a
                    href={whatsappHref}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="px-2.5 py-1.5 rounded-[8px] bg-[#dff8e9] hover:bg-[#ccf3dd] text-[#235839] text-[11px] font-bold flex items-center gap-1 transition-colors"
                  >
                    <Icon name="share" className="w-3.5 h-3.5" />
                    <span>Enviar por WhatsApp</span>
                  </a>
                )}

                <a
                  href={qrData.qrData}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="px-2.5 py-1.5 rounded-[8px] bg-[#009ee3] hover:bg-[#0089c7] text-white text-[11px] font-bold flex items-center gap-1 transition-colors"
                >
                  <Icon name="externalLink" className="w-3.5 h-3.5" />
                  <span>Pagar en Mercado Pago</span>
                </a>
              </div>
            </div>
          )}

          <div className="space-y-1.5 text-[11px]">
            <p>
              <strong>1.</strong> El comercio escanea el QR con <strong>Mercado Pago</strong> o su billetera bancaria.
            </p>
            <p>
              <strong>2.</strong> Confirma el importe de <strong>${montoFormateado}</strong>.
            </p>
            <p>
              <strong>3.</strong> Esta pantalla detectará la acreditación automáticamente cada 4 segundos.
            </p>
          </div>

          {onPosponerPago && (
            <div className="pt-1">
              <button
                type="button"
                onClick={onPosponerPago}
                className="w-full py-2 px-3 rounded-[10px] bg-white hover:bg-slate-50 border border-[#d2ecfa] text-[#426c88] text-[11px] font-bold transition-colors cursor-pointer"
              >
                Cobrar más tarde (dejar pedido en CARGADO)
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
