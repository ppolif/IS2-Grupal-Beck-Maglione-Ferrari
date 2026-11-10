import { useState, useEffect } from 'react';
import OSMMap from '../../components/OSMMap';
import Button from '../../components/Button';
import Chip from '../../components/Chip';
import Modal from '../../components/Modal';
import Icon from '../../components/Icon';
import { colors } from '../../theme/colors';
import { recorridoService, parseMonto } from '../../services/recorridoService';

export default function Recorrido() {
  const [deliveries, setDeliveries] = useState(() => recorridoService.obtenerEntregas());
  const [selectedDelivery, setSelectedDelivery] = useState(() => deliveries[0]);
  const [isCashModalOpen, setIsCashModalOpen] = useState(false);
  const [reciboNumero, setReciboNumero] = useState('REC-2084');
  const [observacionesCobro, setObservacionesCobro] = useState('');

  // Sincronizar selección inicial si cambia la lista
  useEffect(() => {
    if (!selectedDelivery && deliveries.length > 0) {
      setSelectedDelivery(deliveries[0]);
    }
  }, [deliveries, selectedDelivery]);

  // Contadores en tiempo real de la ruta actual
  const entregadosCount = deliveries.filter((d) => d.estadoEntrega === 'Entregado').length;
  const pendientesCount = deliveries.filter((d) => d.estadoEntrega !== 'Entregado').length;
  const recaudadoEfectivo = deliveries
    .filter((d) => d.estadoEntrega === 'Entregado' && (d.medioPago?.toLowerCase().includes('efectivo') || d.cobradoEnEfectivo))
    .reduce((acc, d) => acc + parseMonto(d.monto), 0);

  const avanzarSiguientePunto = (updatedDeliveries, currentId) => {
    const remaining = updatedDeliveries.filter((d) => d.id !== currentId && d.estadoEntrega !== 'Entregado');
    if (remaining.length > 0) {
      setSelectedDelivery(remaining[0]);
      alert(`✓ Entrega completada y Factura emitida.\n✓ Sistema propone siguiente parada: ${remaining[0].comercio} (${remaining[0].direccion}).\n✓ Se notificó automáticamente por correo al cliente.`);
    } else {
      alert(`¡Ruta completada! Todas las entregas del día fueron realizadas con éxito.`);
    }
  };

  const handleMarkDeliveredClick = () => {
    if (selectedDelivery.estadoPago === 'cash' || selectedDelivery.medioPago?.toLowerCase().includes('efectivo')) {
      setReciboNumero(`REC-${selectedDelivery.id.replace(/\D/g, '') || Math.floor(1000 + Math.random() * 9000)}`);
      setObservacionesCobro('');
      setIsCashModalOpen(true);
    } else {
      confirmarEntregaDirecta();
    }
  };

  const confirmarEntregaDirecta = () => {
    const updated = deliveries.map((d) =>
      d.id === selectedDelivery.id ? { ...d, estadoEntrega: 'Entregado' } : d
    );
    setDeliveries(updated);
    recorridoService.guardarEntregas(updated);
    avanzarSiguientePunto(updated, selectedDelivery.id);
  };

  const handleConfirmarCobroEfectivo = (e) => {
    e.preventDefault();
    const updated = deliveries.map((d) =>
      d.id === selectedDelivery.id
        ? {
            ...d,
            estadoEntrega: 'Entregado',
            estadoPago: 'paid',
            medioPago: 'Efectivo (Cobrado)',
            cobradoEnEfectivo: true,
            reciboNumero,
            observacionesCobro,
            fechaCobro: new Date().toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' })
          }
        : d
    );
    setDeliveries(updated);
    recorridoService.guardarEntregas(updated);
    setIsCashModalOpen(false);
    avanzarSiguientePunto(updated, selectedDelivery.id);
  };

  const handleLeavePending = (id) => {
    const updated = deliveries.map((d) =>
      d.id === id ? { ...d, estadoEntrega: 'Pendiente' } : d
    );
    setDeliveries(updated);
    recorridoService.guardarEntregas(updated);
    alert(`Pedido #${id} para "${selectedDelivery?.comercio}" dejado como PENDIENTE`);
  };

  return (
    <div className="space-y-6">
      {/* Title & Contadores de Ruta */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-3">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Ruta de Distribución
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a]">
            Entregas Programadas
          </h1>
        </div>

        {/* Bloque de Contadores del Distribuidor */}
        <div className="flex flex-wrap items-center gap-2">
          {/* Contador de Dinero Recaudado en Efectivo */}
          <div className="flex items-center gap-2 bg-[#f0fdf4] border border-[#bbf7d0] px-3.5 py-1.5 rounded-full text-[12px] text-[#15803d] font-bold shadow-xs">
            <span className="w-2.5 h-2.5 rounded-full bg-[#22c55e] animate-pulse" />
            <span>Recaudado en efectivo:</span>
            <strong className="text-[14px] text-[#166534] font-extrabold font-mono">
              ${recaudadoEfectivo.toLocaleString('es-AR')}
            </strong>
          </div>

          <Chip variant="blue">
            {entregadosCount} de {deliveries.length} entregados
          </Chip>

          <span className="text-[11px] font-medium bg-slate-100 text-[#475569] px-2.5 py-1 rounded-full border border-slate-200">
            {pendientesCount} pendientes
          </span>

          <span className="text-[12px] text-[#6b6b80] hidden sm:inline">Unidad 08 · Gran Mendoza</span>
        </div>
      </div>

      {/* Responsive layout */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Deliveries list */}
        <div className="lg:col-span-5 space-y-3 order-2 lg:order-1">
          <div className="bg-white rounded-[20px] p-4 border border-[#ececf2] shadow-sm">
            <h2 className="text-[14px] font-bold text-[#2b2b3a] mb-3 pb-2 border-b border-[#f0f0f4] flex items-center justify-between">
              <span>Paradas del recorrido ({deliveries.length})</span>
              <span className="text-[11px] text-[#6b6b80] font-normal">Vehículo en ruta</span>
            </h2>

            <div className="space-y-2">
              {deliveries.map((d) => {
                const isSelected = selectedDelivery?.id === d.id;
                const isEntregado = d.estadoEntrega === 'Entregado';
                return (
                  <div
                    key={d.id}
                    onClick={() => setSelectedDelivery(d)}
                    className={`p-3.5 rounded-[16px] border transition-all cursor-pointer ${
                      isSelected
                        ? 'bg-[#d9f7ff]/20 border-[#3fb8d9] shadow-sm'
                        : isEntregado
                        ? 'bg-[#f4fbf7] border-[#d2f3e2] opacity-75'
                        : 'bg-[#fafafd] border-[#ececf2] hover:bg-white'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="text-[10px] font-bold text-[#3fb8d9] uppercase">
                            Parada #{d.orden} · #{d.id}
                          </span>
                          {isEntregado && (
                            <span className="text-[9px] font-bold bg-[#dff8e9] text-[#39855b] px-2 py-0.2 rounded-full">
                              ✓ Entregado
                            </span>
                          )}
                        </div>
                        <h3 className="font-bold text-[14px] text-[#2b2b3a] leading-tight">
                          {d.comercio}
                        </h3>
                        <p className="text-[11px] text-[#6b6b80] mt-0.5">
                          {d.direccion}
                        </p>
                      </div>
                      <span
                        className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${
                          d.estadoPago === 'cash' || (d.medioPago?.toLowerCase().includes('efectivo') && !isEntregado)
                            ? 'bg-[#fff0d3] text-[#986b16]'
                            : 'bg-[#dff8e9] text-[#39855b]'
                        }`}
                      >
                        {d.estadoPago === 'cash' || (d.medioPago?.toLowerCase().includes('efectivo') && !isEntregado)
                          ? `Cobrar ${d.monto}`
                          : d.cobradoEnEfectivo
                          ? `Cobrado en Efectivo`
                          : 'Pagado'}
                      </span>
                    </div>

                    <div className="flex items-center justify-between mt-2 pt-2 border-t border-[#ececf2]/60 text-[10px] text-[#6b6b80]">
                      <span>{d.tiempoEstimado}</span>
                      <span>{d.bultos} bultos</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Map & Delivery Sheet */}
        <div className="lg:col-span-7 space-y-4 order-1 lg:order-2">
          <div className="bg-white rounded-[24px] p-4 sm:p-5 border border-[#ececf2] shadow-sm">
            <OSMMap
              className="h-[340px] sm:h-[420px] w-full"
              markers={deliveries}
              activeMarkerId={selectedDelivery?.id}
              showRoute={true}
              routeColor={colors.secondary.DEFAULT}
              onMarkerClick={(delivery) => setSelectedDelivery(delivery)}
            />

            {/* Delivery Sheet Card */}
            {selectedDelivery && (
              <div className="mt-4 bg-[#fafafd] rounded-[20px] p-5 border border-[#ececf2]">
                <div className="w-10 h-1 bg-[#dedee5] rounded-full mx-auto mb-3" />

                <div className="flex items-center justify-between mb-1">
                  <Chip variant="pink">
                    Entrega {selectedDelivery.orden} de {selectedDelivery.totalEntregas || deliveries.length}
                  </Chip>
                  <small className="text-[11px] text-[#6b6b80]">
                    {selectedDelivery.tiempoEstimado} · {selectedDelivery.distancia}
                  </small>
                </div>

                <h2 className="text-xl font-bold text-[#2b2b3a] mt-2 mb-0.5">
                  {selectedDelivery.comercio}
                </h2>
                <p className="text-[12px] text-[#6b6b80] mb-3">
                  {selectedDelivery.direccion}
                </p>

                {/* Ticket breakdown */}
                <div className="bg-white border border-[#ececf2] rounded-[13px] p-3 text-[11px] mb-4 space-y-1">
                  <div className="flex justify-between items-center">
                    <span className="font-semibold text-[#6b6b80]">
                      Pedido #{selectedDelivery.id}
                    </span>
                    <strong className="text-[12px] text-[#2b2b3a]">
                      {selectedDelivery.monto} · {selectedDelivery.medioPago}
                    </strong>
                  </div>
                  <small className="text-[#6b6b80] block text-[10px] pt-1 border-t border-[#f0f0f4]">
                    {selectedDelivery.items}
                  </small>
                </div>

                {/* Sheet Actions */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                  <Button
                    onClick={handleMarkDeliveredClick}
                    className="w-full justify-center text-[12px] cursor-pointer"
                  >
                    {selectedDelivery.estadoPago === 'cash' || selectedDelivery.medioPago?.toLowerCase().includes('efectivo')
                      ? 'Cobrar en Efectivo y Entregar'
                      : 'Marcar como entregado'}
                  </Button>
                  <Button
                    secondary
                    onClick={() => handleLeavePending(selectedDelivery.id)}
                    className="w-full justify-center text-[12px] cursor-pointer"
                  >
                    Dejar pendiente
                  </Button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Modal Registrar Cobro en Efectivo contra Entrega */}
      <Modal
        isOpen={isCashModalOpen}
        onClose={() => setIsCashModalOpen(false)}
        tag="RENDICIÓN DE COBRO CONTRA ENTREGA"
        title={`Cobro en Efectivo - Pedido #${selectedDelivery?.id}`}
      >
        <form onSubmit={handleConfirmarCobroEfectivo} className="space-y-4">
          <div className="p-3 bg-[#fafafd] rounded-[14px] border border-[#ececf2] text-[12px] space-y-1">
            <p><strong>Comercio:</strong> {selectedDelivery?.comercio}</p>
            <p><strong>Dirección:</strong> {selectedDelivery?.direccion}</p>
            <p className="pt-1 text-[13px]">
              <strong>Monto en efectivo a recibir:</strong>{' '}
              <span className="text-xl font-bold text-[#39855b]">{selectedDelivery?.monto}</span>
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
              className="w-full border border-[#ececf2] rounded-[10px] p-2.5 text-[12px] font-mono text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4]"
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
              className="w-full border border-[#ececf2] rounded-[10px] p-2.5 text-[12px] text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4]"
            />
          </div>

          <div className="p-2.5 bg-[#f0f9ff] border border-[#d2ecfa] rounded-[10px] text-[11px] text-[#0f344d] flex items-center gap-2">
            <Icon name="check" className="w-4 h-4 text-[#009ee3] shrink-0" />
            <span>Al confirmar el cobro, el dinero se suma al contador de caja del distribuidor, se emite la Factura y se notifica al próximo cliente.</span>
          </div>

          <div className="pt-2 flex justify-end gap-2.5">
            <Button secondary type="button" onClick={() => setIsCashModalOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit">
              Confirmar Cobro y Facturar
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
