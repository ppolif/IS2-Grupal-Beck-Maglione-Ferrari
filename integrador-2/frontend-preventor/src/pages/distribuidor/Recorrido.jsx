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
import { colors } from '../../theme/colors';
import { rutaService } from '../../services/rutaService';

function formatearDireccion(dir) {
  if (!dir) return 'Mendoza';
  const calleAltura = [dir.calle, dir.numeracion && dir.numeracion !== 'S/N' ? dir.numeracion : null]
    .filter(Boolean)
    .join(' ');
  const zona = dir.localidadNombre || dir.departamentoNombre || 'Mendoza';
  return calleAltura ? `${calleAltura}, ${zona}` : zona;
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

  const cargarRutaDistribuidor = async () => {
    try {
      setCargando(true);
      setError(null);
      const data = await rutaService.obtenerMiRutaDistribuidorDelDia();
      setRuta(data);
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

  const handleConfirmarEntrega = async (observacion = '') => {
    if (!ruta?.id || !selectedParada?.pedidoId) return;
    try {
      setProcesando(true);
      await rutaService.registrarEntregaDistribuidor(ruta.id, {
        pedidoId: selectedParada.pedidoId,
        observacion: observacion || 'Entrega confirmada por distribuidor',
      });

      setRuta((prev) => {
        if (!prev) return prev;
        const nextParadas = prev.paradas.map((p) =>
          p.orden === selectedParada.orden ? { ...p, completada: true } : p
        );
        return { ...prev, paradas: nextParadas };
      });

      setIsCashModalOpen(false);
      alert(
        `✓ Entrega a "${selectedParada.puntoDeVenta?.nombreComercio}" registrada con éxito.\n` +
        `✓ Si el pedido tenía pago en efectivo, se generó la factura, pasó a estado FACTURADO y luego a ENTREGADO, y se envió automáticamente por correo al cliente.`
      );
    } catch (err) {
      alert(err.message || 'Error al registrar la entrega.');
    } finally {
      setProcesando(false);
    }
  };

  const paradas = ruta?.paradas || [];
  const entregadosCount = paradas.filter((p) => p.completada).length;
  const pendientesCount = paradas.filter((p) => !p.completada).length;
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
    const lat = Number(pdv?.direccion?.latitud) || -32.8900;
    const lng = Number(pdv?.direccion?.longitud) || -68.8450;
    const direccionLegible = formatearDireccion(pdv?.direccion);
    const nombreComercio = pdv?.nombreComercio || `Parada #${p.orden}`;
    return {
      id: pdv?.id || p.orden,
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
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#3fb8d9]">
            Itinerario de Entrega
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a]">
            Ruta y Paradas de Reparto
          </h1>
        </div>
        <div className="flex items-center gap-2">
          <Chip variant="blue">
            {entregadosCount} de {paradas.length} paradas realizadas
          </Chip>
          <span className="text-[12px] text-[#6b6b80] hidden sm:inline">
            {pendientesCount} pendientes
          </span>
        </div>
      </div>

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
                            Parada #{p.orden}
                          </span>
                          <h3 className="font-bold text-[14px] text-[#2b2b3a] leading-tight">
                            {pdv?.nombreComercio}
                          </h3>
                          <p className="text-[11px] text-[#6b6b80] mt-0.5">
                            {formatearDireccion(pdv?.direccion)}
                          </p>
                        </div>
                        <span
                          className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${
                            p.completada ? 'bg-[#dff8e9] text-[#39855b]' : 'bg-[#fff0d3] text-[#986b16]'
                          }`}
                        >
                          {p.completada ? 'Entregado' : 'Pendiente'}
                        </span>
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
                      : selectedParada?.puntoDeVenta?.id || selectedParada?.orden
                  }
                  showRoute={true}
                  routeColor={colors.secondary.DEFAULT}
                  onMarkerClick={(m) => {
                    if (m.id === 'origen-sucursal' || m.esSucursal) {
                      setSelectedParada({ id: 'origen-sucursal', esSucursal: true });
                    } else {
                      const found = paradas.find((p) => String(p.puntoDeVenta?.id) === String(m.id));
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

              return (
                <div className="bg-white rounded-[20px] p-4 border border-[#ececf2] shadow-sm flex items-center justify-between gap-4">
                  <div>
                    <span className="text-[10px] font-bold text-[#3fb8d9] uppercase">
                      Parada #{selectedParada.orden} en destino
                      {esSiguienteParada ? ' · Siguiente Entrega' : ''}
                    </span>
                    <h4 className="font-bold text-[15px] text-[#2b2b3a]">
                      {selectedParada.puntoDeVenta?.nombreComercio}
                    </h4>
                    <p className="text-[11px] text-[#6b6b80]">
                      {formatearDireccion(selectedParada.puntoDeVenta?.direccion)}
                    </p>
                  </div>

                  {selectedParada.completada ? (
                    <span className="px-3.5 py-2 rounded-[12px] bg-[#dff8e9] text-[#39855b] text-[12px] font-bold">
                      Entregado ✓
                    </span>
                  ) : esSiguienteParada ? (
                    <Button
                      onClick={() => handleConfirmarEntrega()}
                      disabled={procesando}
                      className="text-[12px] bg-[#3fb8d9] hover:bg-[#2fa3c2] cursor-pointer"
                    >
                      {procesando ? 'Registrando...' : 'Marcar Entregado'}
                    </Button>
                  ) : (
                    <span className="px-3 py-2 rounded-[12px] bg-[#fff0d3] text-[#986b16] text-[11px] font-semibold">
                      Entregá primero la Parada #{siguienteParadaPendiente?.orden}
                    </span>
                  )}
                </div>
              );
            })()}
          </div>
        </div>
      )}
    </div>
  );
}
