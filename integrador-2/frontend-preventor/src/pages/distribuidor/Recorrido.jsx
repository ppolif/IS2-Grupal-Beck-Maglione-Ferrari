/**
 * Recorrido y Mapa Interactivo de Reparto para Distribuidores.
 * Conectado con /api/v1/rutas-distribuidor/mi-ruta-del-dia y /entregas del backend.
 */
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import OSMMap from '../../components/OSMMap';
import Button from '../../components/Button';
import Chip from '../../components/Chip';
import Modal from '../../components/Modal';
import Icon from '../../components/Icon';
import { colors } from '../../theme/colors';
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

function formatearMedioPago(parada) {
  if (esParadaEfectivo(parada)) {
    return parada.completada ? 'Efectivo (Facturado y Cobrado)' : 'Efectivo contra entrega';
  }
  if (parada?.tipoPago === 'BILLETERA_VIRTUAL') {
    return 'Mercado Pago QR (Facturado)';
  }
  if (parada?.tipoPago === 'TRANSFERENCIA') {
    return 'Transferencia Bancaria (Facturado)';
  }
  return 'Facturado previamente';
}

export default function Recorrido() {
  const [ruta, setRuta] = useState(null);
  const [selectedParada, setSelectedParada] = useState(null);
  const [isCashModalOpen, setIsCashModalOpen] = useState(false);
  const [reciboNumero, setReciboNumero] = useState('REC-1001');
  const [observacionesCobro, setObservacionesCobro] = useState('');
  const [cargando, setCargando] = useState(true);
  const [generando, setGenerando] = useState(false);
  const [error, setError] = useState(null);
  const [procesando, setProcesando] = useState(false);
  const [toastMensaje, setToastMensaje] = useState(null);

  const cargarRutaDistribuidor = async () => {
    try {
      setCargando(true);
      setError(null);
      const data = await rutaService.obtenerMiRutaDistribuidorDelDia();
      setRuta(data);
      recorridoService.sincronizarRuta(data);
      if (data?.paradas && data.paradas.length > 0) {
        const pendiente = data.paradas.find((p) => !p.completada) || data.paradas[0];
        setSelectedParada(pendiente);
      }
    } catch (err) {
      console.error('Error al cargar la ruta del repartidor:', err);
      setError(
        err.code === 'CAMBIO_CLAVE_REQUERIDO' || err.status === 403
          ? 'Cambie su clave para acceder'
          : err.message || 'No tenés una ruta de reparto asignada para el día de hoy.'
      );
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    cargarRutaDistribuidor();
  }, []);

  const handleGenerarRuta = async () => {
    try {
      setGenerando(true);
      setError(null);
      const data = await rutaService.generarRutaDistribuidor();
      setRuta(data);
      recorridoService.sincronizarRuta(data);
      if (data?.paradas && data.paradas.length > 0) {
        const pendiente = data.paradas.find((p) => !p.completada) || data.paradas[0];
        setSelectedParada(pendiente);
      }
    } catch (err) {
      console.error('Error al armar ruta de distribuidor:', err);
      setError(err.message || 'No hay pedidos facturados pendientes para armar una ruta de reparto.');
    } finally {
      setGenerando(false);
    }
  };

  const handleAbrirAccionEntrega = (parada) => {
    if (!parada) return;
    if (esParadaEfectivo(parada)) {
      const nroRef = parada.numeroPedido || parada.orden || '1001';
      setReciboNumero(`REC-${nroRef}`);
      setObservacionesCobro('');
      setIsCashModalOpen(true);
    } else {
      handleConfirmarEntrega('Entrega confirmada por distribuidor');
    }
  };

  const handleConfirmarCobroEfectivoSubmit = async (e) => {
    e.preventDefault();
    const obsTexto = [
      `Cobro en efectivo contra entrega (Recibo ${reciboNumero.trim() || 'S/N'})`,
      observacionesCobro.trim() ? observacionesCobro.trim() : null,
    ]
      .filter(Boolean)
      .join(' - ');
    await handleConfirmarEntrega(obsTexto);
  };

  const handleConfirmarEntrega = async (observacion = '') => {
    if (!ruta?.id || !selectedParada?.pedidoId) return;
    const paradaActualEntregada = selectedParada;
    const eraEfectivo = esParadaEfectivo(paradaActualEntregada);
    const montoCobrado = Number(paradaActualEntregada.totalPedido || 0);

    try {
      setProcesando(true);
      const avance = await rutaService.registrarEntregaDistribuidor(ruta.id, {
        pedidoId: paradaActualEntregada.pedidoId,
        observacion: observacion || 'Entrega confirmada por distribuidor',
      });

      let rutaActualizada = avance?.ruta;
      if (!rutaActualizada) {
        const nextParadas = (ruta.paradas || []).map((p) =>
          p.orden === paradaActualEntregada.orden ? { ...p, completada: true } : p
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
      const resumenNuevo = recorridoService.sincronizarRuta(rutaActualizada);

      const siguiente =
        avance?.siguienteParada ||
        (rutaActualizada.paradas || []).find((p) => !p.completada) ||
        (rutaActualizada.paradas || []).find((p) => p.orden === paradaActualEntregada.orden) ||
        null;
      setSelectedParada(siguiente);
      setIsCashModalOpen(false);

      if (eraEfectivo) {
        setToastMensaje(
          `✓ Cobro en efectivo de $${montoCobrado.toLocaleString('es-AR')} registrado y facturado para "${
            paradaActualEntregada.puntoDeVenta?.nombreComercio || 'Comercio'
          }". Total recaudado hoy: $${Number(resumenNuevo.recaudadoEfectivo || 0).toLocaleString('es-AR')}.`
        );
      } else {
        setToastMensaje(
          `✓ Entrega a "${
            paradaActualEntregada.puntoDeVenta?.nombreComercio || 'Comercio'
          }" registrada con éxito.`
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

  const sucursalNombre =
    ruta?.sucursalOrigenNombre ||
    ruta?.responsable?.mayoristaRazonSocial ||
    'Dulce Mayor - Casa Central';
  const sucursalDir = ruta?.sucursalOrigenDireccion;
  const sucursalDireccionTexto = sucursalDir
    ? formatearDireccion(sucursalDir)
    : 'Av. San Martín 1020, Ciudad de Mendoza';
  const sucursalMarker = {
    id: 'origen-sucursal',
    lat: Number(sucursalDir?.latitud) || -32.889458,
    lng: Number(sucursalDir?.longitud) || -68.845839,
    nombre: `Salida: ${sucursalNombre}`,
    comercio: sucursalNombre,
    direccion: sucursalDireccionTexto,
    estado: 'Punto de Partida (Sede Central)',
    orden: 'S',
    color: '#2b2b3a',
    esSucursal: true,
  };

  const paradasMarkers = paradas.map((p) => {
    const pdv = p.puntoDeVenta;
    const lat = Number(pdv?.direccion?.latitud) || -32.89;
    const lng = Number(pdv?.direccion?.longitud) || -68.845;
    const direccionLegible = formatearDireccion(pdv?.direccion);
    const nombreComercio = pdv?.nombreComercio || `Parada #${p.orden}`;
    return {
      id: `parada-${p.orden}`,
      pdvId: pdv?.id,
      lat,
      lng,
      nombre: nombreComercio,
      comercio: nombreComercio,
      direccion: direccionLegible,
      estado: p.completada ? 'Entregado' : 'Pendiente',
      label: nombreComercio,
      sublabel: direccionLegible,
      status: p.completada ? 'completed' : 'pending',
      orden: p.orden,
    };
  });

  const markers = [sucursalMarker, ...paradasMarkers];

  return (
    <div className="space-y-6">
      {/* Header & Contadores de Recaudación */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-3">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#3fb8d9]">
            Itinerario de Entrega
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a]">
            Ruta y Paradas de Reparto
          </h1>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {/* Contador en tiempo real de Dinero Recaudado en Efectivo */}
          <div className="flex items-center gap-2 bg-[#f0fdf4] border border-[#bbf7d0] px-3.5 py-1.5 rounded-full text-[12px] text-[#15803d] font-bold shadow-xs">
            <span className="w-2.5 h-2.5 rounded-full bg-[#22c55e] animate-pulse" />
            <span>Dinero recaudado en efectivo:</span>
            <strong className="text-[14px] text-[#166534] font-extrabold font-mono">
              ${recaudadoEfectivo.toLocaleString('es-AR')}
            </strong>
          </div>

          {pendienteEfectivo > 0 && (
            <div className="flex items-center gap-1.5 bg-[#fffbeb] border border-[#fde68a] px-3 py-1.5 rounded-full text-[11px] text-[#92400e] font-semibold">
              <span>Por cobrar en efectivo:</span>
              <strong className="font-mono font-bold">
                ${pendienteEfectivo.toLocaleString('es-AR')}
              </strong>
            </div>
          )}

          <Chip variant="blue">
            {entregadosCount} de {paradas.length} paradas realizadas
          </Chip>

          <span className="text-[11px] font-medium bg-white text-[#6b6b80] px-2.5 py-1 rounded-full border border-[#ececf2]">
            {pendientesCount} pendientes
          </span>
        </div>
      </div>

      {/* Notificación de entrega y cobro */}
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
            <div className="w-16 h-16 rounded-full bg-[#eefaff] text-[#247f99] flex items-center justify-center mx-auto shadow-xs">
              <Icon name="key" className="w-8 h-8" />
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
            <div className="w-16 h-16 rounded-full bg-[#f0f4f8] text-[#3fb8d9] flex items-center justify-center mx-auto">
              <Icon name="truck" className="w-8 h-8" />
            </div>
            <div>
              <h3 className="font-bold text-[#2b2b3a] text-lg">Sin ruta de reparto</h3>
              <p className="text-[12px] text-[#6b6b80] mt-1">{error}</p>
            </div>
            <Button
              onClick={handleGenerarRuta}
              disabled={generando}
              className="w-full justify-center py-3 text-[13px] cursor-pointer bg-[#3fb8d9] hover:bg-[#2fa3c2]"
            >
              {generando ? 'Armando ruta de entrega...' : 'Armar mi ruta de reparto'}
            </Button>
          </div>
        )
      )}

      {!cargando && !error && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          {/* Listado de paradas */}
          <div className="lg:col-span-5 space-y-3 order-2 lg:order-1">
            {/* Tarjeta de Caja / Recaudación en Efectivo */}
            <div className="bg-gradient-to-br from-[#f2fdf7] to-[#e6f9ef] border border-[#bbf7d0] rounded-[20px] p-4 shadow-xs">
              <div className="flex items-center justify-between gap-2">
                <div>
                  <span className="text-[10px] font-bold uppercase tracking-wider text-[#15803d] block">
                    Caja del Distribuidor · Efectivo Recaudado
                  </span>
                  <strong className="text-2xl font-extrabold text-[#166534] font-mono mt-0.5 block">
                    ${recaudadoEfectivo.toLocaleString('es-AR')}
                  </strong>
                </div>
                <div className="text-right text-[11px] text-[#2c6343]">
                  <span className="block font-semibold">
                    {entregasEfectivoCount} cobro{entregasEfectivoCount === 1 ? '' : 's'} en efectivo
                  </span>
                  <span className="text-[10px] opacity-85">
                    {pendientesEfectivoCount > 0
                      ? `${pendientesEfectivoCount} pendiente${pendientesEfectivoCount === 1 ? '' : 's'} ($${pendienteEfectivo.toLocaleString('es-AR')})`
                      : 'Sin cobros pendientes'}
                  </span>
                </div>
              </div>
            </div>

            <div className="bg-white rounded-[20px] p-4 border border-[#ececf2] shadow-sm">
              <h2 className="text-[14px] font-bold text-[#2b2b3a] mb-3 pb-2 border-b border-[#f0f0f4] flex items-center justify-between">
                <span>Itinerario de Entrega ({paradas.length})</span>
                <span className="text-[11px] text-[#6b6b80] font-normal">Sede Central → Destino</span>
              </h2>

              <div className="space-y-2">
                {/* Tarjeta de Sede Central de Salida */}
                <div
                  onClick={() => setSelectedParada({ id: 'origen-sucursal', esSucursal: true })}
                  className={`p-3.5 rounded-[16px] border transition-all cursor-pointer mb-2.5 ${
                    selectedParada?.id === 'origen-sucursal'
                      ? 'bg-[#eaf8fc] border-[#3fb8d9] shadow-sm ring-1 ring-[#3fb8d9]/30'
                      : 'bg-[#f4fbfe] border-[#cdecfb] hover:bg-[#edf8fd]'
                  }`}
                >
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex items-start gap-2.5">
                      <span className="w-6 h-6 rounded-full bg-[#3fb8d9] text-white font-bold text-[11px] flex items-center justify-center shrink-0 shadow-xs">
                        S
                      </span>
                      <div>
                        <span className="text-[10px] font-bold text-[#3fb8d9] uppercase tracking-wider block">
                          Punto de Partida
                        </span>
                        <h3 className="font-bold text-[13px] text-[#2b2b3a] leading-tight">
                          {sucursalNombre}
                        </h3>
                        <p className="text-[11px] text-[#6b6b80] mt-0.5">
                          {sucursalDireccionTexto}
                        </p>
                      </div>
                    </div>
                    <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-[#dff8e9] text-[#39855b] shrink-0">
                      Sede Central
                    </span>
                  </div>
                </div>

                {paradas.map((p) => {
                  const isSelected = selectedParada?.orden === p.orden;
                  const pdv = p.puntoDeVenta;
                  const esEfectivo = esParadaEfectivo(p);
                  const montoNum = Number(p.totalPedido || 0);

                  return (
                    <div
                      key={p.orden}
                      onClick={() => setSelectedParada(p)}
                      className={`p-3.5 rounded-[16px] border transition-all cursor-pointer ${
                        isSelected
                          ? 'bg-[#eaf8fc] border-[#3fb8d9] shadow-sm'
                          : 'bg-[#fafafd] border-[#ececf2] hover:bg-white'
                      }`}
                    >
                      <div className="flex items-start justify-between gap-2">
                        <div>
                          <span className="text-[10px] font-bold text-[#3fb8d9] uppercase">
                            Parada #{p.orden}{' '}
                            {p.numeroPedido ? `· Pedido #DM-${p.numeroPedido}` : ''}
                          </span>
                          <h3 className="font-bold text-[14px] text-[#2b2b3a] leading-tight">
                            {pdv?.nombreComercio}
                          </h3>
                          <p className="text-[11px] text-[#6b6b80] mt-0.5">
                            {formatearDireccion(pdv?.direccion)}
                          </p>
                        </div>
                        <span
                          className={`text-[10px] font-semibold px-2 py-0.5 rounded-full shrink-0 ${
                            p.completada ? 'bg-[#dff8e9] text-[#39855b]' : 'bg-[#fff0d3] text-[#986b16]'
                          }`}
                        >
                          {p.completada ? 'Entregado' : 'Pendiente'}
                        </span>
                      </div>

                      <div className="mt-2 pt-2 border-t border-[#ececf2]/70 flex items-center justify-between text-[11px]">
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
                              ? `Efectivo Recaudado: +$${montoNum.toLocaleString('es-AR')}`
                              : `Cobrar en Efectivo: $${montoNum.toLocaleString('es-AR')}`
                            : `${formatearMedioPago(p)}`}
                        </span>

                        {montoNum > 0 && (
                          <strong className="font-mono text-[#2b2b3a]">
                            ${montoNum.toLocaleString('es-AR')}
                          </strong>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Mapa OSM interactivo */}
          <div className="lg:col-span-7 space-y-4 order-1 lg:order-2">
            <div className="bg-white rounded-[24px] p-3 border border-[#ececf2] shadow-sm overflow-hidden">
              <div className="h-[380px] sm:h-[460px] rounded-[18px] overflow-hidden">
                <OSMMap
                  markers={markers}
                  activeMarkerId={
                    selectedParada?.id === 'origen-sucursal'
                      ? 'origen-sucursal'
                      : `parada-${selectedParada?.orden}`
                  }
                  showRoute={true}
                  routeColor={colors.secondary.DEFAULT}
                  onMarkerClick={(m) => {
                    if (m.id === 'origen-sucursal' || m.esSucursal) {
                      setSelectedParada({ id: 'origen-sucursal', esSucursal: true });
                    } else {
                      const found = paradas.find(
                        (p) => `parada-${p.orden}` === String(m.id) || String(p.orden) === String(m.orden)
                      );
                      if (found) setSelectedParada(found);
                    }
                  }}
                />
              </div>
            </div>

            {selectedParada?.esSucursal ? (
              <div className="bg-white rounded-[20px] p-4 border border-[#ececf2] shadow-sm flex items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                  <span className="w-8 h-8 rounded-full bg-[#3fb8d9] text-white font-bold text-[13px] flex items-center justify-center shrink-0">
                    S
                  </span>
                  <div>
                    <span className="text-[10px] font-bold text-[#3fb8d9] uppercase tracking-wider block">
                      Centro de Expedición y Logística
                    </span>
                    <h4 className="font-bold text-[15px] text-[#2b2b3a]">
                      {sucursalNombre}
                    </h4>
                    <p className="text-[11px] text-[#6b6b80]">
                      {sucursalDireccionTexto}
                    </p>
                  </div>
                </div>
                <span className="text-[11px] font-semibold text-[#1c667b] bg-[#eefaff] px-3 py-1.5 rounded-full border border-[#d2f3fc] shrink-0">
                  Punto de Salida
                </span>
              </div>
            ) : selectedParada && (() => {
              const esSiguienteParada =
                !selectedParada.completada &&
                siguienteParadaPendiente &&
                selectedParada.orden === siguienteParadaPendiente.orden;
              const esEfectivo = esParadaEfectivo(selectedParada);
              const montoNum = Number(selectedParada.totalPedido || 0);

              return (
                <div className="bg-white rounded-[20px] p-5 border border-[#ececf2] shadow-sm space-y-4">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                    <div>
                      <span className="text-[10px] font-bold text-[#3fb8d9] uppercase">
                        Parada #{selectedParada.orden} en destino
                        {esSiguienteParada ? ' · Siguiente Entrega' : ''}
                      </span>
                      <h4 className="font-bold text-[16px] text-[#2b2b3a]">
                        {selectedParada.puntoDeVenta?.nombreComercio}
                      </h4>
                      <p className="text-[12px] text-[#6b6b80]">
                        {formatearDireccion(selectedParada.puntoDeVenta?.direccion)}
                      </p>
                    </div>

                    {selectedParada.completada ? (
                      <span className="px-3.5 py-2 rounded-[12px] bg-[#dff8e9] text-[#39855b] text-[12px] font-bold self-start sm:self-auto">
                        {esEfectivo
                          ? `Entregado y Cobrado ($${montoNum.toLocaleString('es-AR')}) ✓`
                          : 'Entregado ✓'}
                      </span>
                    ) : esSiguienteParada ? (
                      <Button
                        onClick={() => handleAbrirAccionEntrega(selectedParada)}
                        disabled={procesando}
                        className={`text-[12px] cursor-pointer ${
                          esEfectivo
                            ? 'bg-[#16a34a] hover:bg-[#15803d] text-white'
                            : 'bg-[#3fb8d9] hover:bg-[#2fa3c2]'
                        }`}
                      >
                        {procesando
                          ? 'Registrando...'
                          : esEfectivo
                          ? `Cobrar en Efectivo ($${montoNum.toLocaleString('es-AR')}) y Entregar`
                          : 'Marcar Entregado'}
                      </Button>
                    ) : (
                      <span className="px-3 py-2 rounded-[12px] bg-[#fff0d3] text-[#986b16] text-[11px] font-semibold">
                        Entregá primero la Parada #{siguienteParadaPendiente?.orden}
                      </span>
                    )}
                  </div>

                  {/* Detalle financiero del pedido en la parada */}
                  <div className="bg-[#fafafd] border border-[#ececf2] rounded-[14px] p-3 text-[12px] flex flex-wrap items-center justify-between gap-2">
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-[#6b6b80]">
                        Pedido {selectedParada.numeroPedido ? `#DM-${selectedParada.numeroPedido}` : `#${selectedParada.pedidoId?.slice(0, 8) || selectedParada.orden}`}
                      </span>
                      <span className="text-[#aaa9ba]">·</span>
                      <span
                        className={`font-semibold ${
                          esEfectivo ? 'text-[#15803d]' : 'text-[#247f99]'
                        }`}
                      >
                        {formatearMedioPago(selectedParada)}
                      </span>
                    </div>
                    <strong className="text-[14px] font-extrabold font-mono text-[#2b2b3a]">
                      Total: ${montoNum.toLocaleString('es-AR')}
                    </strong>
                  </div>
                </div>
              );
            })()}
          </div>
        </div>
      )}

      {/* Modal de Confirmación de Cobro en Efectivo contra Entrega */}
      <Modal
        isOpen={isCashModalOpen}
        onClose={() => !procesando && setIsCashModalOpen(false)}
        tag="RENDICIÓN DE COBRO CONTRA ENTREGA"
        title={`Cobro en Efectivo - Pedido ${
          selectedParada?.numeroPedido ? `#DM-${selectedParada.numeroPedido}` : ''
        }`}
      >
        <form onSubmit={handleConfirmarCobroEfectivoSubmit} className="space-y-4">
          <div className="p-3.5 bg-[#fafafd] rounded-[14px] border border-[#ececf2] text-[12px] space-y-1.5">
            <p>
              <strong>Comercio:</strong> {selectedParada?.puntoDeVenta?.nombreComercio}
            </p>
            <p>
              <strong>Dirección:</strong>{' '}
              {formatearDireccion(selectedParada?.puntoDeVenta?.direccion)}
            </p>
            <p className="pt-1 text-[13px] flex items-center justify-between border-t border-[#ececf2]">
              <strong>Monto en efectivo a recibir:</strong>
              <span className="text-xl font-extrabold font-mono text-[#166534]">
                ${Number(selectedParada?.totalPedido || 0).toLocaleString('es-AR')}
              </span>
            </p>
            <p className="text-[11px] text-[#15803d] flex items-center justify-between pt-1">
              <span>Nuevo total recaudado en tu caja tras cobrar:</span>
              <strong className="font-mono">
                $
                {(
                  recaudadoEfectivo + Number(selectedParada?.totalPedido || 0)
                ).toLocaleString('es-AR')}
              </strong>
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

          <div className="p-2.5 bg-[#f0fdf4] border border-[#bbf7d0] rounded-[10px] text-[11px] text-[#166534] flex items-center gap-2">
            <Icon name="check" className="w-4 h-4 text-[#16a34a] shrink-0" />
            <span>
              Al confirmar el cobro, el monto se sumará al contador de dinero recaudado, el backend emitirá la Factura, descontará el stock y enviará el comprobante por correo al cliente.
            </span>
          </div>

          <div className="pt-2 flex justify-end gap-2.5">
            <Button
              secondary
              type="button"
              disabled={procesando}
              onClick={() => setIsCashModalOpen(false)}
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
