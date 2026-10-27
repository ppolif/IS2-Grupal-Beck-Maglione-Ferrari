import { useState } from 'react';
import MapPlaceholder from '../../components/MapPlaceholder';
import Button from '../../components/Button';
import Chip from '../../components/Chip';
import { deliveriesMock } from '../../mocks/deliveries';

export default function Recorrido() {
  const [selectedDelivery, setSelectedDelivery] = useState(deliveriesMock[0]);
  const [deliveries, setDeliveries] = useState(deliveriesMock);

  const handleMarkDelivered = (id) => {
    setDeliveries((prev) =>
      prev.map((d) => (d.id === id ? { ...d, estadoEntrega: 'Entregado' } : d))
    );
    alert(`Pedido #${id} para "${selectedDelivery.comercio}" marcado como ENTREGADO`);
  };

  const handleLeavePending = (id) => {
    setDeliveries((prev) =>
      prev.map((d) => (d.id === id ? { ...d, estadoEntrega: 'Pendiente' } : d))
    );
    alert(`Pedido #${id} para "${selectedDelivery.comercio}" dejado como PENDIENTE`);
  };

  return (
    <div className="space-y-6">
      {/* Title */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Ruta de Distribución
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a]">
            Entregas Programadas
          </h1>
        </div>
        <div className="flex items-center gap-2">
          <Chip variant="blue">Entrega 4 de 10 en curso</Chip>
          <span className="text-[12px] text-[#6b6b80] hidden sm:inline">Unidad 08 · Gran Mendoza</span>
        </div>
      </div>

      {/* Responsive layout: mobile stacked, lg side-by-side */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Deliveries list (desktop / tablet) */}
        <div className="lg:col-span-5 space-y-3 order-2 lg:order-1">
          <div className="bg-white rounded-[20px] p-4 border border-[#ececf2] shadow-sm">
            <h2 className="text-[14px] font-bold text-[#2b2b3a] mb-3 pb-2 border-b border-[#f0f0f4] flex items-center justify-between">
              <span>Paradas del recorrido ({deliveries.length})</span>
              <span className="text-[11px] text-[#6b6b80] font-normal">Vehículo en ruta</span>
            </h2>

            <div className="space-y-2">
              {deliveries.map((d) => {
                const isSelected = selectedDelivery.id === d.id;
                return (
                  <div
                    key={d.id}
                    onClick={() => setSelectedDelivery(d)}
                    className={`p-3.5 rounded-[16px] border transition-all cursor-pointer ${
                      isSelected
                        ? 'bg-[#d9f7ff]/20 border-[#3fb8d9] shadow-sm'
                        : 'bg-[#fafafd] border-[#ececf2] hover:bg-white'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <div>
                        <span className="text-[10px] font-bold text-[#3fb8d9] uppercase">
                          Parada #{d.orden} · #{d.id}
                        </span>
                        <h3 className="font-bold text-[14px] text-[#2b2b3a] leading-tight">
                          {d.comercio}
                        </h3>
                        <p className="text-[11px] text-[#6b6b80] mt-0.5">
                          {d.direccion}
                        </p>
                      </div>
                      <span
                        className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${
                          d.estadoPago === 'cash'
                            ? 'bg-[#fff0d3] text-[#986b16]'
                            : 'bg-[#dff8e9] text-[#39855b]'
                        }`}
                      >
                        {d.estadoPago === 'cash' ? d.monto : 'Pagado'}
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

        {/* Map & Delivery Sheet with ticket breakdown from prototype */}
        <div className="lg:col-span-7 space-y-4 order-1 lg:order-2">
          <div className="bg-white rounded-[24px] p-4 sm:p-5 border border-[#ececf2] shadow-sm">
            {/* Map container with exact text required */}
            <MapPlaceholder className="h-[340px] sm:h-[420px] w-full" isDelivery={true} />

            {/* Delivery Sheet Card matching prototype */}
            <div className="mt-4 bg-[#fafafd] rounded-[20px] p-5 border border-[#ececf2]">
              <div className="w-10 h-1 bg-[#dedee5] rounded-full mx-auto mb-3" />

              <div className="flex items-center justify-between mb-1">
                <Chip variant="pink">
                  Entrega {selectedDelivery.orden} de {selectedDelivery.totalEntregas}
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

              {/* Ticket breakdown from prototype */}
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
                  onClick={() => handleMarkDelivered(selectedDelivery.id)}
                  className="w-full justify-center text-[12px]"
                >
                  Marcar como entregado
                </Button>
                <Button
                  secondary
                  onClick={() => handleLeavePending(selectedDelivery.id)}
                  className="w-full justify-center text-[12px]"
                >
                  Dejar pendiente
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

