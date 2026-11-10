/**
 * Recorrido y Mapa Interactivo de Reparto para Distribuidores.
 * Conectado con /api/v1/rutas-distribuidor/mi-ruta-del-dia y /entregas del backend.
 */
import { useState, useEffect } from 'react';
import OSMMap from '../../components/OSMMap';
import Button from '../../components/Button';
import Chip from '../../components/Chip';
import Modal from '../../components/Modal';
import { colors } from '../../theme/colors';
import { rutaService } from '../../services/rutaService';

export default function Recorrido() {
  const [ruta, setRuta] = useState(null);
  const [selectedParada, setSelectedParada] = useState(null);
  const [isCashModalOpen, setIsCashModalOpen] = useState(false);
  const [reciboNumero, setReciboNumero] = useState('REC-1001');
  const [observacionesCobro, setObservacionesCobro] = useState('');
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const [procesando, setProcesando] = useState(false);

  useEffect(() => {
    async function cargarRutaDistribuidor() {
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
        setError('No tenés una ruta de reparto asignada para el día de hoy.');
      } finally {
        setCargando(false);
      }
    }
    cargarRutaDistribuidor();
  }, []);

  const handleConfirmarEntrega = async (observacion = '') => {
    if (!ruta?.id || !selectedParada?.pedidoId) return;
    try {
      setProcesando(true);
      await rutaService.registrarEntregaDistribuidor(ruta.id, {
        pedidoId: selectedParada.pedidoId,
        observacion: observacion || 'Entrega confirmada por distribuidor',
      });

      // Actualizar parada a completada localmente
      setRuta((prev) => {
        if (!prev) return prev;
        const nextParadas = prev.paradas.map((p) =>
          p.orden === selectedParada.orden ? { ...p, completada: true } : p
        );
        return { ...prev, paradas: nextParadas };
      });

      setIsCashModalOpen(false);
      alert(`✓ Entrega a "${selectedParada.puntoDeVenta?.nombreComercio}" registrada con éxito.\n✓ Se notificó por correo al siguiente comercio de la ruta.`);
    } catch (err) {
      alert(err.message || 'Error al registrar la entrega.');
    } finally {
      setProcesando(false);
    }
  };

  const paradas = ruta?.paradas || [];
  const entregadosCount = paradas.filter((p) => p.completada).length;
  const pendientesCount = paradas.filter((p) => !p.completada).length;

  const markers = paradas.map((p) => {
    const pdv = p.puntoDeVenta;
    const lat = pdv?.direccion?.latitud ?? -32.8900;
    const lng = pdv?.direccion?.longitud ?? -68.8450;
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
        <div className="p-6 bg-white rounded-[20px] border border-[#ececf2] text-center space-y-2">
          <span className="text-3xl">🚚</span>
          <h3 className="font-bold text-[#2b2b3a] text-lg">Sin ruta de reparto</h3>
          <p className="text-[12px] text-[#6b6b80] max-w-sm mx-auto">{error}</p>
        </div>
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
                            {pdv?.direccion?.calle || 'Mendoza'}
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
                  activeMarkerId={selectedParada?.puntoDeVenta?.id}
                  showRoute={true}
                  routeColor={colors.secondary.DEFAULT}
                />
              </div>
            </div>

            {selectedParada && (
              <div className="bg-white rounded-[20px] p-4 border border-[#ececf2] shadow-sm flex items-center justify-between gap-4">
                <div>
                  <span className="text-[10px] font-bold text-[#3fb8d9] uppercase">
                    Parada #{selectedParada.orden} en destino
                  </span>
                  <h4 className="font-bold text-[15px] text-[#2b2b3a]">
                    {selectedParada.puntoDeVenta?.nombreComercio}
                  </h4>
                  <p className="text-[11px] text-[#6b6b80]">
                    {selectedParada.puntoDeVenta?.direccion?.calle || 'Mendoza'}
                  </p>
                </div>

                <Button
                  onClick={() => handleConfirmarEntrega()}
                  disabled={procesando || selectedParada.completada}
                  className={`text-[12px] bg-[#3fb8d9] hover:bg-[#2fa3c2] cursor-pointer ${selectedParada.completada ? 'opacity-50' : ''}`}
                >
                  {selectedParada.completada ? 'Entregado ✓' : 'Marcar Entregado'}
                </Button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
