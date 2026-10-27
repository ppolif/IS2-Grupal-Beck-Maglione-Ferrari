import { useState } from 'react';
import MapPlaceholder from '../../components/MapPlaceholder';
import Button from '../../components/Button';
import Chip from '../../components/Chip';
import Icon from '../../components/Icon';
import { visitsMock } from '../../mocks/visits';

export default function Recorrido() {
  const [selectedVisit, setSelectedVisit] = useState(visitsMock[0]);
  const [visits, setVisits] = useState(visitsMock);

  const handleMarkVisited = (id) => {
    setVisits((prev) =>
      prev.map((v) => (v.id === id ? { ...v, estado: 'Visitado' } : v))
    );
    alert(`Visita a "${selectedVisit.comercio}" marcada como VISITADA`);
  };

  const handleLeavePending = (id) => {
    setVisits((prev) =>
      prev.map((v) => (v.id === id ? { ...v, estado: 'Pendiente' } : v))
    );
    alert(`Visita a "${selectedVisit.comercio}" dejada como PENDIENTE`);
  };

  return (
    <div className="space-y-6">
      {/* Title header for desktop/tablet */}
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
          <Chip variant="pink">3 de 12 visitas completadas</Chip>
          <span className="text-[12px] text-[#6b6b80] hidden sm:inline">Zona Godoy Cruz</span>
        </div>
      </div>

      {/* Responsive layout: mobile stacked, lg side-by-side */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Scheduled visits list (visible on desktop/tablet, collapsable or tabbed) */}
        <div className="lg:col-span-5 space-y-3 order-2 lg:order-1">
          <div className="bg-white rounded-[20px] p-4 border border-[#ececf2] shadow-sm">
            <h2 className="text-[14px] font-bold text-[#2b2b3a] mb-3 pb-2 border-b border-[#f0f0f4] flex items-center justify-between">
              <span>Comercios en la ruta ({visits.length})</span>
              <span className="text-[11px] text-[#6b6b80] font-normal">Orden optimizado</span>
            </h2>

            <div className="space-y-2">
              {visits.map((v) => {
                const isSelected = selectedVisit.id === v.id;
                return (
                  <div
                    key={v.id}
                    onClick={() => setSelectedVisit(v)}
                    className={`p-3.5 rounded-[16px] border transition-all cursor-pointer ${
                      isSelected
                        ? 'bg-[#fff0fb] border-[#e86bc4] shadow-sm'
                        : 'bg-[#fafafd] border-[#ececf2] hover:bg-white'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <div>
                        <span className="text-[10px] font-bold text-[#e86bc4] uppercase">
                          Parada #{v.orden}
                        </span>
                        <h3 className="font-bold text-[14px] text-[#2b2b3a] leading-tight">
                          {v.comercio}
                        </h3>
                        <p className="text-[11px] text-[#6b6b80] mt-0.5">
                          {v.direccion}
                        </p>
                      </div>
                      <span
                        className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${
                          v.estado === 'Visitado'
                            ? 'bg-[#dff8e9] text-[#39855b]'
                            : 'bg-[#ffe2f7] text-[#a34187]'
                        }`}
                      >
                        {v.estado}
                      </span>
                    </div>

                    <div className="flex items-center justify-between mt-2 pt-2 border-t border-[#ececf2]/60 text-[10px] text-[#6b6b80]">
                      <span>{v.distancia}</span>
                      <span className="text-[#3fb8d9] font-medium">{v.telefono}</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Map and Active Visit Sheet (matching prototype sheet) */}
        <div className="lg:col-span-7 space-y-4 order-1 lg:order-2">
          <div className="bg-white rounded-[24px] p-4 sm:p-5 border border-[#ececf2] shadow-sm">
            {/* Map container with exact text required */}
            <MapPlaceholder className="h-[340px] sm:h-[420px] w-full" isDelivery={false} />

            {/* Visit Sheet Card directly beneath the map (or floating overlay on mobile) */}
            <div className="mt-4 bg-[#fafafd] rounded-[20px] p-5 border border-[#ececf2]">
              <div className="w-10 h-1 bg-[#dedee5] rounded-full mx-auto mb-3" />

              <div className="flex items-center justify-between mb-1">
                <Chip variant="pink">
                  {selectedVisit.orden} de {selectedVisit.totalVisitas} visitas
                </Chip>
                <small className="text-[11px] text-[#6b6b80]">
                  {selectedVisit.distancia}
                </small>
              </div>

              <h2 className="text-xl font-bold text-[#2b2b3a] mt-2 mb-0.5">
                {selectedVisit.comercio}
              </h2>
              <p className="text-[12px] text-[#6b6b80] mb-3">
                {selectedVisit.direccion}
              </p>

              <div className="p-3 bg-white rounded-[12px] border border-[#ececf2] text-[11px] text-[#6b6b80] mb-4 flex items-center justify-between">
                <span>Responsable: <strong>{selectedVisit.responsable}</strong></span>
                <span>Último pedido: <strong className="text-[#2b2b3a]">{selectedVisit.ultimoPedido}</strong></span>
              </div>

              {/* Action buttons from prototype */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                <Button
                  onClick={() => handleMarkVisited(selectedVisit.id)}
                  className="w-full justify-center text-[12px]"
                >
                  Marcar como visitado
                </Button>
                <Button
                  secondary
                  onClick={() => handleLeavePending(selectedVisit.id)}
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

