/**
 * Hoja de Carga y Remitos de Entrega para el Distribuidor / Repartidor.
 * Conectado con la ruta diaria /api/v1/rutas-distribuidor/mi-ruta-del-dia.
 */
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { rutaService } from '../../services/rutaService';
import { recorridoService, esParadaEfectivo } from '../../services/recorridoService';

function formatearDireccion(dir) {
  if (!dir) return 'Mendoza';
  const calleAltura = [dir.calle, dir.numeracion && dir.numeracion !== 'S/N' ? dir.numeracion : null]
    .filter(Boolean)
    .join(' ');
  const zona = dir.localidadNombre || dir.departamentoNombre || 'Mendoza';
  return calleAltura ? `${calleAltura}, ${zona}` : zona;
}

export default function Pedidos() {
  const [ruta, setRuta] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [generando, setGenerando] = useState(false);
  const [procesando, setProcesando] = useState(false);
  const [error, setError] = useState(null);
  const [paradaCobroModal, setParadaCobroModal] = useState(null);
  const [reciboNumero, setReciboNumero] = useState('REC-1001');
  const [observacionesCobro, setObservacionesCobro] = useState('');
  const [toastMensaje, setToastMensaje] = useState(null);

  const cargarEntregas = async () => {
    try {
      setCargando(true);
      setError(null);
      const data = await rutaService.obtenerMiRutaDistribuidorDelDia();
      setRuta(data);
      recorridoService.sincronizarRuta(data);
    } catch (err) {
      console.error('Error al cargar entregas de distribuidor:', err);
      setError(
        err.code === 'CAMBIO_CLAVE_REQUERIDO' || err.status === 403
          ? 'Cambie su clave para acceder'
          : err.message || 'No tenés una ruta de reparto asignada para hoy.'
      );
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    cargarEntregas();
  }, []);

  const handleGenerarRuta = async () => {
    try {
      setGenerando(true);
      setError(null);
      const data = await rutaService.generarRutaDistribuidor();
      setRuta(data);
      recorridoService.sincronizarRuta(data);
    } catch (err) {
      console.error('Error al armar entregas de distribuidor:', err);
      setError(err.message || 'No hay pedidos facturados pendientes para armar una ruta de reparto.');
    } finally {
      setGenerando(false);
    }
  };

  const handleIniciarEntrega = (parada) => {
    if (!parada) return;
    if (esParadaEfectivo(parada)) {
      const nroRef = parada.numeroPedido || parada.orden || '1001';
      setReciboNumero(`REC-${nroRef}`);
      setObservacionesCobro('');
      setParadaCobroModal(parada);
    } else {
      ejecutarEntrega(parada, 'Entrega confirmada por distribuidor');
    }
  };

  const handleConfirmarCobroSubmit = async (e) => {
    e.preventDefault();
    if (!paradaCobroModal) return;
    const obsTexto = [
      `Cobro en efectivo contra entrega (Recibo ${reciboNumero.trim() || 'S/N'})`,
      observacionesCobro.trim() ? observacionesCobro.trim() : null,
    ]
      .filter(Boolean)
      .join(' - ');
    await ejecutarEntrega(paradaCobroModal, obsTexto);
  };

  const ejecutarEntrega = async (parada, observacion) => {
    if (!ruta?.id || !parada?.pedidoId) return;
    const eraEfectivo = esParadaEfectivo(parada);
    const montoCobrado = Number(parada.totalPedido || 0);

    try {
      setProcesando(true);
      const avance = await rutaService.registrarEntregaDistribuidor(ruta.id, {
        pedidoId: parada.pedidoId,
        observacion: observacion || 'Entrega confirmada por distribuidor',
      });

      let rutaActualizada = avance?.ruta;
      if (!rutaActualizada) {
        const nextParadas = (ruta.paradas || []).map((p) =>
          p.orden === parada.orden ? { ...p, completada: true } : p
        );
        const nuevoRecaudado = nextParadas
          .filter((p) => p.completada && esParadaEfectivo(p))
          .reduce((acc, p) => acc + Number(p.totalPedido || 0), 0);
        const nuevoPendiente = nextParadas
          .filter((p) => !p.completada && esParadaEfectivo(p))
          .reduce((acc, p) => acc + Number(p.totalPedido || 0), 0);
        rutaActualizada = {
          ...ruta,
          paradas: nextParadas,
          dineroRecaudadoEfectivo: nuevoRecaudado,
          dineroPendienteEfectivo: nuevoPendiente,
        };
      }

      setRuta(rutaActualizada);
      const nuevoResumen = recorridoService.sincronizarRuta(rutaActualizada);
      setParadaCobroModal(null);

      if (eraEfectivo) {
        setToastMensaje(
          `✓ Cobro en efectivo de $${montoCobrado.toLocaleString('es-AR')} registrado y facturado para "${
            parada.puntoDeVenta?.nombreComercio || 'Comercio'
          }". Total recaudado: $${Number(nuevoResumen.recaudadoEfectivo || 0).toLocaleString('es-AR')}.`
        );
      } else {
        setToastMensaje(
          `✓ Entrega a "${parada.puntoDeVenta?.nombreComercio || 'Comercio'}" registrada con éxito.`
        );
      }
    } catch (err) {
      alert(err.message || 'Error al registrar la entrega.');
    } finally {
      setProcesando(false);
    }
  };

  const paradas = ruta?.paradas || [];
  const resumen = recorridoService.calcularResumenDesdeRuta(ruta);
  const {
    entregadosCount,
    pendientesCount,
    recaudadoEfectivo,
    pendienteEfectivo,
    entregasEfectivoCount,
    pendientesEfectivoCount,
  } = resumen;
  const siguienteParadaPendiente = paradas.find((p) => !p.completada) || null;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#3fb8d9]">
            Hoja de Ruta del Repartidor
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a] mt-0.5">
            {paradas.length} paradas asignadas
          </h1>
          <p className="text-[12px] text-[#6b6b80] mt-0.5">
            {ruta?.responsable ? `Distribuidor: ${ruta.responsable.nombre || ''}` : 'Reparto programado'} · {entregadosCount} entregadas, {pendientesCount} pendientes
          </p>
        </div>

        <Link to="/recorrido">
          <Button className="text-[12px]">
            <Icon name="route" className="w-4 h-4" />
            <span>Ver Mapa de Ruta</span>
          </Button>
        </Link>
      </div>

      {/* Contador de Dinero Recaudado en Efectivo */}
      {!cargando && !error && (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <div className="sm:col-span-2 bg-gradient-to-br from-[#f2fdf7] to-[#e6f9ef] border border-[#bbf7d0] rounded-[20px] p-4 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <div className="flex items-center gap-1.5 text-[#15803d]">
                <span className="w-2.5 h-2.5 rounded-full bg-[#22c55e] animate-pulse" />
                <span className="text-[10px] font-bold uppercase tracking-wider">
                  Dinero Recaudado en Efectivo (Ruta Actual)
                </span>
              </div>
              <strong className="text-3xl font-extrabold text-[#166534] font-mono block mt-1">
                ${recaudadoEfectivo.toLocaleString('es-AR')}
              </strong>
              <p className="text-[11px] text-[#3d7a55] mt-0.5">
                {entregasEfectivoCount} pedido{entregasEfectivoCount === 1 ? '' : 's'} entregado{entregasEfectivoCount === 1 ? '' : 's'} y facturado{entregasEfectivoCount === 1 ? '' : 's'} en efectivo.
              </p>
            </div>

            <div className="sm:text-right border-t sm:border-t-0 pt-2 sm:pt-0 border-[#bbf7d0]/70">
              <span className="text-[10px] font-bold uppercase tracking-wider text-[#92400e] bg-[#fef3c7] px-2.5 py-1 rounded-full inline-block">
                Por cobrar en efectivo
              </span>
              <strong className="text-lg font-extrabold text-[#92400e] font-mono block mt-1">
                ${pendienteEfectivo.toLocaleString('es-AR')}
              </strong>
              <span className="text-[11px] text-[#2c6343]">
                {pendientesEfectivoCount} parada{pendientesEfectivoCount === 1 ? '' : 's'} en efectivo pendiente{pendientesEfectivoCount === 1 ? '' : 's'}
              </span>
            </div>
          </div>

          <div className="bg-white border border-[#ececf2] rounded-[20px] p-4 shadow-xs flex items-center justify-around">
            <div className="text-center">
              <strong className="text-2xl font-bold text-[#39855b] block">{entregadosCount}</strong>
              <span className="text-[11px] text-[#6b6b80]">Entregados</span>
            </div>
            <div className="h-10 w-px bg-[#ececf2]" />
            <div className="text-center">
              <strong className="text-2xl font-bold text-[#3fb8d9] block">{pendientesCount}</strong>
              <span className="text-[11px] text-[#6b6b80]">Pendientes</span>
            </div>
          </div>
        </div>
      )}

      {toastMensaje && (
        <div className="p-3.5 rounded-[16px] bg-[#f0fdf4] border border-[#bbf7d0] text-[#166534] text-[12px] font-medium flex items-center justify-between gap-3 shadow-xs">
          <span>{toastMensaje}</span>
          <button
            type="button"
            onClick={() => setToastMensaje(null)}
            className="text-[11px] font-bold text-[#15803d] hover:underline shrink-0 cursor-pointer"
          >
            Cerrar
          </button>
        </div>
      )}

      {cargando && (
        <div className="flex justify-center items-center py-16">
          <div className="w-8 h-8 border-3 border-[#3fb8d9] border-t-transparent rounded-full animate-spin"></div>
        </div>
      )}

      {error && (
        error === 'Cambie su clave para acceder' ? (
          <div className="max-w-md mx-auto my-8 p-8 bg-white rounded-[24px] border border-[#ececf2] text-center space-y-4 shadow-sm">
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
          <div className="p-8 bg-white rounded-[20px] border border-[#ececf2] text-center space-y-4 max-w-md mx-auto">
            <span className="text-4xl">🚚</span>
            <div>
              <h3 className="font-bold text-[#2b2b3a] text-lg">Sin entregas pendientes</h3>
              <p className="text-[12px] text-[#6b6b80] mt-1">{error}</p>
            </div>
            <Button
              onClick={handleGenerarRuta}
              disabled={generando}
              className="w-full justify-center py-3 text-[13px] cursor-pointer bg-[#3fb8d9] hover:bg-[#2fa3c2]"
            >
              {generando ? 'Armando hoja de reparto...' : 'Armar mi hoja de reparto'}
            </Button>
          </div>
        )
      )}

      {!cargando && !error && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {paradas.map((p) => {
            const pdv = p.puntoDeVenta;
            const dir = formatearDireccion(pdv?.direccion);
            const esEfectivo = esParadaEfectivo(p);
            const montoNum = Number(p.totalPedido || 0);
            const esSiguienteParada =
              !p.completada &&
              siguienteParadaPendiente &&
              p.orden === siguienteParadaPendiente.orden;

            return (
              <div
                key={p.orden}
                className="bg-white border border-[#ececf2] rounded-[18px] p-4 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow"
              >
                <div>
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <span className="text-[10px] font-bold text-[#3fb8d9] tracking-wider uppercase">
                      Parada #{p.orden}{' '}
                      {p.numeroPedido
                        ? `· Pedido #DM-${p.numeroPedido}`
                        : p.pedidoId
                        ? `· Pedido #${p.pedidoId.slice(0, 8)}`
                        : ''}
                    </span>
                    <strong
                      className={`text-[10px] font-bold px-2.5 py-1 rounded-full ${
                        p.completada
                          ? 'bg-[#dff8e9] text-[#39855b]'
                          : 'bg-[#fff0d3] text-[#986b16]'
                      }`}
                    >
                      {p.completada ? 'Entregado ✓' : 'Pendiente'}
                    </strong>
                  </div>

                  <h3 className="font-bold text-[15px] text-[#2b2b3a] leading-tight">
                    {pdv?.nombreComercio}
                  </h3>
                  <p className="text-[11px] text-[#6b6b80] mt-0.5">{dir}</p>

                  <div className="mt-3 p-2.5 bg-[#fafafd] border border-[#ececf2] rounded-[12px] flex items-center justify-between text-[11px]">
                    <span
                      className={`font-semibold px-2 py-0.5 rounded-full text-[10px] ${
                        esEfectivo
                          ? p.completada
                            ? 'bg-[#dcfce7] text-[#166534]'
                            : 'bg-[#fef3c7] text-[#92400e]'
                          : 'bg-[#eefaff] text-[#247f99]'
                      }`}
                    >
                      {esEfectivo
                        ? p.completada
                          ? 'Efectivo · Facturado y Cobrado'
                          : 'Efectivo contra entrega'
                        : p.tipoPago === 'BILLETERA_VIRTUAL'
                        ? 'Mercado Pago QR · Facturado'
                        : 'Transferencia · Facturado'}
                    </span>
                    <strong className="text-[13px] font-extrabold font-mono text-[#2b2b3a]">
                      ${montoNum.toLocaleString('es-AR')}
                    </strong>
                  </div>
                </div>

                <div className="flex items-center justify-between gap-2 mt-3 pt-3 border-t border-[#f0f0f4] text-[11px]">
                  {esSiguienteParada ? (
                    <button
                      type="button"
                      disabled={procesando}
                      onClick={() => handleIniciarEntrega(p)}
                      className={`px-3 py-1.5 rounded-[10px] font-bold text-[11px] transition-colors cursor-pointer ${
                        esEfectivo
                          ? 'bg-[#16a34a] hover:bg-[#15803d] text-white'
                          : 'bg-[#3fb8d9] hover:bg-[#2fa3c2] text-white'
                      }`}
                    >
                      {procesando
                        ? 'Procesando...'
                        : esEfectivo
                        ? `Cobrar en Efectivo ($${montoNum.toLocaleString('es-AR')}) y Entregar`
                        : 'Marcar Entregado'}
                    </button>
                  ) : (
                    <span className="text-[#aaa9ba]">Secuencia de parada #{p.orden}</span>
                  )}

                  <Link
                    to="/recorrido"
                    className="text-[#3fb8d9] font-semibold hover:underline flex items-center gap-1"
                  >
                    <span>Ver en mapa</span>
                    <span>→</span>
                  </Link>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Modal de Cobro en Efectivo desde Hoja de Ruta */}
      <Modal
        isOpen={Boolean(paradaCobroModal)}
        onClose={() => !procesando && setParadaCobroModal(null)}
        tag="RENDICIÓN DE COBRO CONTRA ENTREGA"
        title={`Cobro en Efectivo - Pedido ${
          paradaCobroModal?.numeroPedido ? `#DM-${paradaCobroModal.numeroPedido}` : ''
        }`}
      >
        <form onSubmit={handleConfirmarCobroSubmit} className="space-y-4">
          <div className="p-3.5 bg-[#fafafd] rounded-[14px] border border-[#ececf2] text-[12px] space-y-1.5">
            <p>
              <strong>Comercio:</strong> {paradaCobroModal?.puntoDeVenta?.nombreComercio}
            </p>
            <p>
              <strong>Dirección:</strong>{' '}
              {formatearDireccion(paradaCobroModal?.puntoDeVenta?.direccion)}
            </p>
            <p className="pt-1 text-[13px] flex items-center justify-between border-t border-[#ececf2]">
              <strong>Monto en efectivo a recibir:</strong>
              <span className="text-xl font-extrabold font-mono text-[#166534]">
                ${Number(paradaCobroModal?.totalPedido || 0).toLocaleString('es-AR')}
              </span>
            </p>
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              N° de Recibo Oficial / Remito Entregado *
            </label>
            <input
              type="text"
              required
              value={reciboNumero}
              onChange={(e) => setReciboNumero(e.target.value)}
              className="w-full border border-[#ececf2] rounded-[10px] p-2.5 text-[12px] font-mono text-[#2b2b3a] focus:outline-none focus:border-[#3fb8d9]"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Observaciones del distribuidor (opcional)
            </label>
            <input
              type="text"
              value={observacionesCobro}
              onChange={(e) => setObservacionesCobro(e.target.value)}
              placeholder="Ej: Cobrado en mano por titular del comercio"
              className="w-full border border-[#ececf2] rounded-[10px] p-2.5 text-[12px] text-[#2b2b3a] focus:outline-none focus:border-[#3fb8d9]"
            />
          </div>

          <div className="pt-2 flex justify-end gap-2.5">
            <Button
              secondary
              type="button"
              disabled={procesando}
              onClick={() => setParadaCobroModal(null)}
            >
              Cancelar
            </Button>
            <Button
              type="submit"
              disabled={procesando}
              className="bg-[#16a34a] hover:bg-[#15803d] text-white cursor-pointer"
            >
              {procesando ? 'Facturando y cobrando...' : 'Confirmar Cobro y Entregar'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
