/**
 * Hoja de Ruta interactiva y Mapa OpenStreetMap del Preventor.
 * Conectado con /api/v1/rutas-preventor/mi-ruta-del-dia del backend.
 */
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import OSMMap from '../../components/OSMMap';
import Button from '../../components/Button';
import Chip from '../../components/Chip';
import { colors } from '../../theme/colors';
import { rutaService } from '../../services/rutaService';
import { useCart } from '../../context/CartContext';

export default function Recorrido() {
  const navigate = useNavigate();
  const { seleccionarCliente } = useCart();
  const [ruta, setRuta] = useState(null);
  const [selectedVisit, setSelectedVisit] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const [procesando, setProcesando] = useState(false);

  // Carga la ruta de hoy desde el backend
  useEffect(() => {
    async function cargarRuta() {
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
        setError('No tenés una ruta de preventa generada para hoy.');
      } finally {
        setCargando(false);
      }
    }
    cargarRuta();
  }, []);

  const handleMarcarParada = async (parada) => {
    if (!ruta?.id || !parada?.puntoDeVenta?.id) return;
    try {
      setProcesando(true);
      await rutaService.completarParadaPreventor(ruta.id, {
        puntoDeVentaId: parada.puntoDeVenta.id,
        pedidoId: null, // Visita sin pedido inmediato o ya cargado
      });

      // Actualizar localmente la parada como completada
      setRuta((prev) => {
        if (!prev) return prev;
        return {
          ...prev,
          paradas: prev.paradas.map((p) =>
            p.puntoDeVenta?.id === parada.puntoDeVenta.id ? { ...p, completada: true } : p
          ),
        };
      });

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

  // Adaptar marcadores al componente Leaflet OSMMap
  const markers = paradas.map((p) => {
    const pdv = p.puntoDeVenta;
    const lat = pdv?.direccion?.latitud ?? -32.8895;
    const lng = pdv?.direccion?.longitud ?? -68.8458;
    return {
      id: pdv?.id || p.orden,
      lat,
      lng,
      label: pdv?.nombreComercio || `Parada #${p.orden}`,
      sublabel: pdv?.direccion?.calle || 'Mendoza',
      status: p.completada ? 'completed' : 'pending',
      orden: p.orden,
    };
  });

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
        <div className="flex items-center gap-2">
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
        <div className="p-6 bg-white rounded-[20px] border border-[#ececf2] text-center space-y-2">
          <span className="text-3xl">📍</span>
          <h3 className="font-bold text-[#2b2b3a] text-lg">Sin ruta asignada</h3>
          <p className="text-[12px] text-[#6b6b80] max-w-sm mx-auto">{error}</p>
        </div>
      )}

      {!cargando && !error && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          {/* Listado de paradas */}
          <div className="lg:col-span-5 space-y-3 order-2 lg:order-1">
            <div className="bg-white rounded-[20px] p-4 border border-[#ececf2] shadow-sm">
              <h2 className="text-[14px] font-bold text-[#2b2b3a] mb-3 pb-2 border-b border-[#f0f0f4] flex items-center justify-between">
                <span>Comercios en la ruta ({paradas.length})</span>
                <span className="text-[11px] text-[#6b6b80] font-normal">Orden optimizado</span>
              </h2>

              {paradas.length === 0 ? (
                <p className="text-[12px] text-[#6b6b80] text-center py-4">No hay comercios asignados para hoy.</p>
              ) : (
                <div className="space-y-2">
                  {paradas.map((p) => {
                    const isSelected = selectedVisit?.puntoDeVenta?.id === p.puntoDeVenta?.id;
                    const pdv = p.puntoDeVenta;

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
                              Parada #{p.orden}
                            </span>
                            <h3 className="font-bold text-[14px] text-[#2b2b3a] leading-tight">
                              {pdv?.nombreComercio}
                            </h3>
                            <p className="text-[11px] text-[#6b6b80] mt-0.5">
                              {pdv?.direccion?.calle || 'Mendoza'}
                            </p>
                          </div>
                          <span
                            className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${
                              p.completada ? 'bg-[#dff8e9] text-[#39855b]' : 'bg-[#fff0d3] text-[#986b16]'
                            }`}
                          >
                            {p.completada ? 'Visitado' : 'Pendiente'}
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
                  activeMarkerId={selectedVisit?.puntoDeVenta?.id}
                  showRoute={true}
                  routeColor={colors.primary.DEFAULT}
                />
              </div>
            </div>

            {selectedVisit && (
              <div className="bg-white rounded-[20px] p-4 border border-[#ececf2] shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <span className="text-[10px] font-bold text-[#e86bc4] uppercase">
                    Parada #{selectedVisit.orden} Seleccionada
                  </span>
                  <h4 className="font-bold text-[15px] text-[#2b2b3a]">
                    {selectedVisit.puntoDeVenta?.nombreComercio}
                  </h4>
                  <p className="text-[11px] text-[#6b6b80]">
                    {selectedVisit.puntoDeVenta?.direccion?.calle || 'Mendoza'}
                  </p>
                </div>

                <div className="flex items-center gap-2">
                  {selectedVisit.puntoDeVenta && (
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

                  <Button
                    onClick={() => handleMarcarParada(selectedVisit)}
                    disabled={procesando || selectedVisit.completada}
                    className={`text-[12px] cursor-pointer ${selectedVisit.completada ? 'opacity-50' : ''}`}
                  >
                    {selectedVisit.completada ? 'Completada ✓' : 'Marcar Visitada'}
                  </Button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
