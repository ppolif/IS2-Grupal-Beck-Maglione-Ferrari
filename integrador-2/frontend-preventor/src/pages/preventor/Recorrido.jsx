/**
 * Hoja de Ruta interactiva y Mapa OpenStreetMap del Preventor.
 * Conectado con /api/v1/rutas-preventor/mi-ruta-del-dia del backend.
 */
import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import OSMMap from '../../components/OSMMap';
import Button from '../../components/Button';
import Chip from '../../components/Chip';
import { colors } from '../../theme/colors';
import { rutaService } from '../../services/rutaService';
import { useCart } from '../../context/CartContext';

function formatearDireccion(dir) {
  if (!dir) return 'Mendoza';
  const calleAltura = [dir.calle, dir.numeracion && dir.numeracion !== 'S/N' ? dir.numeracion : null]
    .filter(Boolean)
    .join(' ');
  const zona = dir.localidadNombre || dir.departamentoNombre || 'Mendoza';
  return calleAltura ? `${calleAltura}, ${zona}` : zona;
}

export default function Recorrido() {
  const navigate = useNavigate();
  const { seleccionarCliente } = useCart();
  const [ruta, setRuta] = useState(null);
  const [selectedVisit, setSelectedVisit] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const [procesando, setProcesando] = useState(false);
  const [generando, setGenerando] = useState(false);

  // Carga la ruta de hoy desde el backend
  const cargarRuta = async () => {
    try {
      setCargando(true);
      setError(null);
      const data = await rutaService.obtenerMiRutaPreventorDelDia();
      setRuta(data);
      if (data?.paradas && data.paradas.length > 0) {
        const primeraIncompleta = data.paradas.find((p) => !p.completada) || data.paradas[0];
        setSelectedVisit(primeraIncompleta);
      }
    } catch (err) {
      console.error('Error al cargar la ruta del preventor:', err);
      if (err.code === 'CAMBIO_CLAVE_REQUERIDO' || err.status === 403) {
        setError('Cambie su clave para acceder');
      } else {
        setError('No tenés una ruta de preventa generada para hoy.');
      }
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    cargarRuta();
  }, []);

  const handleGenerarRuta = async () => {
    try {
      setGenerando(true);
      setError(null);
      const nuevaRuta = await rutaService.generarRutaPreventor();
      setRuta(nuevaRuta);
      if (nuevaRuta?.paradas && nuevaRuta.paradas.length > 0) {
        const primeraIncompleta = nuevaRuta.paradas.find((p) => !p.completada) || nuevaRuta.paradas[0];
        setSelectedVisit(primeraIncompleta);
      }
    } catch (err) {
      console.error('Error al generar la ruta:', err);
      if (err.code === 'CAMBIO_CLAVE_REQUERIDO' || err.status === 403) {
        setError('Cambie su clave para acceder');
      } else {
        setError(err.message || 'No se pudo generar la ruta. Verificá tener comercios en tu cartera.');
      }
    } finally {
      setGenerando(false);
    }
  };

  const handleMarcarParada = async (parada) => {
    if (!ruta?.id || !parada?.puntoDeVenta?.id) return;
    try {
      setProcesando(true);
      const avance = await rutaService.completarParadaPreventor(ruta.id, {
        puntoDeVentaId: parada.puntoDeVenta.id,
        pedidoId: null,
      });

      if (avance?.ruta) {
        setRuta(avance.ruta);
        if (avance.siguienteParada) {
          setSelectedVisit(avance.siguienteParada);
        } else {
          const actualizada = avance.ruta.paradas?.find(
            (p) => p.puntoDeVenta?.id === parada.puntoDeVenta.id
          );
          if (actualizada) setSelectedVisit(actualizada);
        }
      } else {
        setRuta((prev) => {
          if (!prev) return prev;
          const nextParadas = prev.paradas.map((p) =>
            p.puntoDeVenta?.id === parada.puntoDeVenta.id ? { ...p, completada: true } : p
          );
          const siguiente = nextParadas.find((p) => !p.completada);
          if (siguiente) setSelectedVisit(siguiente);
          return {
            ...prev,
            paradas: nextParadas,
          };
        });
      }

      alert(`Visita a "${parada.puntoDeVenta.nombreComercio}" registrada con éxito.`);
    } catch (err) {
      alert(err.message || 'Error al registrar visita');
    } finally {
      setProcesando(false);
    }
  };

  const paradas = ruta?.paradas || [];
  const completadas = paradas.filter((p) => p.completada).length;
  const total = paradas.length;
  const siguienteParadaPendiente = paradas.find((p) => !p.completada) || null;

  const sucursalNombre =
    ruta?.sucursalOrigenNombre ||
    ruta?.responsable?.mayoristaRazonSocial ||
    'Dulce Mayor - Sucursal de Salida';
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
    estado: 'Punto de Partida (Sucursal)',
    orden: 'S',
    color: '#3fb8d9',
    esSucursal: true,
  };

  // Adaptar marcadores al componente Leaflet OSMMap (iniciando en la Sucursal de salida)
  const paradasMarkers = paradas.map((p) => {
    const pdv = p.puntoDeVenta;
    const lat = Number(pdv?.direccion?.latitud) || -32.8895;
    const lng = Number(pdv?.direccion?.longitud) || -68.8458;
    const direccionLegible = formatearDireccion(pdv?.direccion);
    const nombreComercio = pdv?.nombreComercio || `Parada #${p.orden}`;
    return {
      id: pdv?.id || p.orden,
      lat,
      lng,
      nombre: nombreComercio,
      comercio: nombreComercio,
      direccion: direccionLegible,
      estado: p.completada ? 'Visitado' : 'Pendiente',
      label: nombreComercio,
      sublabel: direccionLegible,
      status: p.completada ? 'completed' : 'pending',
      orden: p.orden,
    };
  });

  const markers = [sucursalMarker, ...paradasMarkers];
  const activeMarkerId =
    selectedVisit?.id === 'origen-sucursal'
      ? 'origen-sucursal'
      : selectedVisit?.puntoDeVenta?.id || null;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Hoja de Ruta del Preventor
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a]">
            Recorrido y Visitas del Día
          </h1>
        </div>
        <div className="flex items-center gap-2 flex-wrap">
          <Chip variant="pink">
            {completadas} de {total} visitas completadas
          </Chip>
          <span className="text-[12px] text-[#6b6b80] hidden sm:inline">
            Estado: {ruta?.estado || 'En curso'}
          </span>
        </div>
      </div>

      {cargando && (
        <div className="flex justify-center items-center py-16">
          <div className="w-8 h-8 border-3 border-[#e86bc4] border-t-transparent rounded-full animate-spin"></div>
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
            <div className="w-16 h-16 rounded-full bg-[#fdf2f8] text-[#e86bc4] flex items-center justify-center mx-auto">
              <Icon name="mapPin" className="w-8 h-8" />
            </div>
            <div>
              <h3 className="font-bold text-[#2b2b3a] text-lg">Sin ruta asignada</h3>
              <p className="text-[12px] text-[#6b6b80] mt-1">{error}</p>
            </div>
            <Button
              onClick={handleGenerarRuta}
              disabled={generando}
              className="w-full justify-center py-3 text-[13px] cursor-pointer"
            >
              {generando ? 'Calculando ruta óptima...' : 'Generar mi ruta del día'}
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
                <span>Itinerario del Día ({paradas.length} comercios)</span>
                <span className="text-[11px] text-[#6b6b80] font-normal">Orden óptimo de visita</span>
              </h2>

              {/* Tarjeta de Sucursal de Salida */}
              <div
                onClick={() => setSelectedVisit({ id: 'origen-sucursal', esSucursal: true })}
                className={`mb-3 p-3.5 rounded-[16px] border transition-all cursor-pointer ${
                  selectedVisit?.id === 'origen-sucursal'
                    ? 'bg-[#eaf8fc] border-[#3fb8d9] shadow-sm'
                    : 'bg-[#f4fbfe] border-[#cdecfb] hover:bg-[#eaf8fc]'
                }`}
              >
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <span className="text-[10px] font-bold text-[#009ee3] uppercase">
                      Punto de Partida · Sucursal (S)
                    </span>
                    <h3 className="font-bold text-[14px] text-[#2b2b3a] leading-tight">
                      {sucursalNombre}
                    </h3>
                    <p className="text-[11px] text-[#6b6b80] mt-0.5">
                      {sucursalDireccionTexto}
                    </p>
                  </div>
                  <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-[#cdecfb] text-[#0077ad]">
                    Origen
                  </span>
                </div>
              </div>

              {paradas.length === 0 ? (
                <p className="text-[12px] text-[#6b6b80] text-center py-4">No hay comercios asignados para hoy.</p>
              ) : (
                <div className="space-y-2">
                  {paradas.map((p) => {
                    const isSelected = selectedVisit?.puntoDeVenta?.id === p.puntoDeVenta?.id;
                    const pdv = p.puntoDeVenta;
                    const esSiguiente =
                      !p.completada &&
                      siguienteParadaPendiente &&
                      String(siguienteParadaPendiente.puntoDeVenta?.id) === String(pdv?.id);

                    return (
                      <div
                        key={pdv?.id || p.orden}
                        onClick={() => setSelectedVisit(p)}
                        className={`p-3.5 rounded-[16px] border transition-all cursor-pointer ${
                          isSelected
                            ? 'bg-[#fff0fb] border-[#e86bc4] shadow-sm'
                            : 'bg-[#fafafd] border-[#ececf2] hover:bg-white'
                        }`}
                      >
                        <div className="flex items-start justify-between gap-2">
                          <div>
                            <span className="text-[10px] font-bold text-[#e86bc4] uppercase">
                              Parada #{p.orden} {esSiguiente ? '· Siguiente parada' : ''}
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
                              p.completada
                                ? 'bg-[#dff8e9] text-[#39855b]'
                                : esSiguiente
                                ? 'bg-[#eaf8fc] text-[#0077ad]'
                                : 'bg-[#fff0d3] text-[#986b16]'
                            }`}
                          >
                            {p.completada ? 'Visitado' : esSiguiente ? 'Siguiente' : 'Pendiente'}
                          </span>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>

          {/* Mapa OSM interactivo */}
          <div className="lg:col-span-7 space-y-4 order-1 lg:order-2">
            <div className="bg-white rounded-[24px] p-3 border border-[#ececf2] shadow-sm overflow-hidden">
              <div className="h-[380px] sm:h-[460px] rounded-[18px] overflow-hidden">
                <OSMMap
                  markers={markers}
                  activeMarkerId={activeMarkerId}
                  showRoute={true}
                  routeColor={colors.primary.DEFAULT}
                  onMarkerClick={(m) => {
                    if (m.id === 'origen-sucursal') {
                      setSelectedVisit({ id: 'origen-sucursal', esSucursal: true });
                      return;
                    }
                    const found = paradas.find((p) => String(p.puntoDeVenta?.id) === String(m.id));
                    if (found) setSelectedVisit(found);
                  }}
                />
              </div>
            </div>

            {selectedVisit && !selectedVisit.esSucursal && (() => {
              const esSiguienteParada =
                !selectedVisit.completada &&
                siguienteParadaPendiente &&
                String(siguienteParadaPendiente.puntoDeVenta?.id) ===
                  String(selectedVisit.puntoDeVenta?.id);

              return (
                <div className="bg-white rounded-[20px] p-4 border border-[#ececf2] shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div>
                    <span className="text-[10px] font-bold text-[#e86bc4] uppercase">
                      Parada #{selectedVisit.orden} Seleccionada
                      {esSiguienteParada ? ' · Siguiente en Ruta' : ''}
                    </span>
                    <h4 className="font-bold text-[15px] text-[#2b2b3a]">
                      {selectedVisit.puntoDeVenta?.nombreComercio}
                    </h4>
                    <p className="text-[11px] text-[#6b6b80]">
                      {formatearDireccion(selectedVisit.puntoDeVenta?.direccion)}
                    </p>
                  </div>

                  <div className="flex items-center gap-2 flex-wrap">
                    {esSiguienteParada && selectedVisit.puntoDeVenta && (
                      <button
                        type="button"
                        onClick={() => {
                          seleccionarCliente(selectedVisit.puntoDeVenta);
                          navigate('/tienda');
                        }}
                        className="px-3.5 py-2 rounded-[12px] bg-[#eaf8fc] hover:bg-[#d7f1fa] text-[#009ee3] border border-[#cdecfb] text-[12px] font-bold cursor-pointer transition-colors"
                      >
                        Tomar Pedido
                      </button>
                    )}

                    {selectedVisit.completada ? (
                      <span className="px-3.5 py-2 rounded-[12px] bg-[#dff8e9] text-[#39855b] text-[12px] font-bold">
                        Completada ✓
                      </span>
                    ) : esSiguienteParada ? (
                      <Button
                        onClick={() => handleMarcarParada(selectedVisit)}
                        disabled={procesando}
                        className="text-[12px] cursor-pointer"
                      >
                        {procesando ? 'Registrando...' : 'Marcar Visitada'}
                      </Button>
                    ) : (
                      <span className="px-3 py-2 rounded-[12px] bg-[#fff0d3] text-[#986b16] text-[11px] font-semibold">
                        Visitá primero la Parada #{siguienteParadaPendiente?.orden}
                      </span>
                    )}
                  </div>
                </div>
              );
            })()}

            {selectedVisit?.esSucursal && (
              <div className="bg-white rounded-[20px] p-4 border border-[#cdecfb] shadow-sm flex items-center justify-between gap-4">
                <div>
                  <span className="text-[10px] font-bold text-[#009ee3] uppercase">
                    Sucursal de Salida del Preventor
                  </span>
                  <h4 className="font-bold text-[15px] text-[#2b2b3a]">{sucursalNombre}</h4>
                  <p className="text-[11px] text-[#6b6b80]">{sucursalDireccionTexto}</p>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
