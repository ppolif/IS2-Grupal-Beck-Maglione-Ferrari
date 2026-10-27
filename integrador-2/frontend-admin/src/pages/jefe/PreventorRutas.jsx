import { useState } from 'react';
import MapPlaceholder from '../../components/MapPlaceholder';
import Button from '../../components/Button';
import Chip from '../../components/Chip';

export default function PreventorRutas() {
  const [selectedVisit, setSelectedVisit] = useState({
    comercio: 'Kiosco El Sol',
    responsable: 'Roberto Gómez',
    direccion: 'Av. San Martín 1842, Godoy Cruz',
    distancia: '8 min · 2,4 km',
    preventor: 'Lucía Martínez',
    estado: 'Visitado',
    ultimoPedido: '$45.200 (hace 1 hora)'
  });

  const visits = [
    { comercio: 'Kiosco El Sol', direccion: 'Av. San Martín 1842, Godoy Cruz', preventor: 'Lucía Martínez', estado: 'Visitado', hora: '10:15' },
    { comercio: 'Almacén Don Pepe', direccion: 'Belgrano 450, Godoy Cruz', preventor: 'Lucía Martínez', estado: 'En camino', hora: '11:00' },
    { comercio: 'Drugstore Mitre', direccion: 'Mitre 920, Godoy Cruz', preventor: 'Lucía Martínez', estado: 'Pendiente', hora: '11:45' },
    { comercio: 'Kiosco Central', direccion: 'Rivadavia 110, Godoy Cruz', preventor: 'Lucía Martínez', estado: 'Pendiente', hora: '12:30' }
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Supervisión de Preventores
        </span>
        <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
          Rutero en Calle y Alta de Comercios
        </h2>
        <p className="text-[12px] text-[#6b6b80] mt-0.5">
          Vista heredada de preventores: monitoreo de visitas comerciales y auditoría de clientes registrados.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Stops list on left */}
        <div className="lg:col-span-5 space-y-3">
          <div className="bg-white rounded-[20px] p-5 border border-[#ececf2] shadow-sm">
            <div className="flex justify-between items-center pb-3 mb-3 border-b border-[#f0f0f4]">
              <h3 className="text-[14px] font-bold text-[#2b2b3a]">
                Paradas de Lucía Martínez
              </h3>
              <Chip variant="pink">Zona Godoy Cruz</Chip>
            </div>

            <div className="space-y-2">
              {visits.map((v) => {
                const isSelected = selectedVisit.comercio === v.comercio;
                return (
                  <div
                    key={v.comercio}
                    onClick={() => setSelectedVisit(v)}
                    className={`p-3.5 rounded-[14px] border transition-all cursor-pointer ${
                      isSelected
                        ? 'bg-[#fff0fb] border-[#e86bc4] shadow-xs'
                        : 'bg-[#fafafd] border-[#ececf2] hover:bg-white'
                    }`}
                  >
                    <div className="flex justify-between items-start">
                      <strong className="text-[13px] text-[#2b2b3a] leading-tight">
                        {v.comercio}
                      </strong>
                      <span
                        className={`text-[9px] font-semibold px-2 py-0.5 rounded-full ${
                          v.estado === 'Visitado'
                            ? 'bg-[#dff8e9] text-[#39855b]'
                            : v.estado === 'En camino'
                            ? 'bg-[#d9f7ff] text-[#247f99]'
                            : 'bg-[#fff2d3] text-[#986b16]'
                        }`}
                      >
                        {v.estado}
                      </span>
                    </div>
                    <p className="text-[11px] text-[#6b6b80] mt-1">{v.direccion}</p>
                    <div className="flex justify-between items-center mt-2 pt-2 border-t border-[#ececf2]/60 text-[10px] text-[#6b6b80]">
                      <span>Hora est.: {v.hora}</span>
                      <span className="text-[#3fb8d9] font-medium">Ver detalles →</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Map and Sheet Details on right */}
        <div className="lg:col-span-7 space-y-4">
          <div className="bg-white rounded-[22px] p-5 border border-[#ececf2] shadow-sm">
            <div className="flex justify-between items-center mb-3">
              <span className="text-[11px] font-bold text-[#6b6b80] uppercase tracking-wider">
                Geolocalización de Preventor
              </span>
              <span className="text-[11px] text-[#3fb8d9] font-semibold">
                GPS Activo · Actualizado hace 2 min
              </span>
            </div>

            {/* Map container with exact text required */}
            <MapPlaceholder className="h-[360px] w-full" />

            <div className="mt-4 p-4 rounded-[16px] bg-[#fafafd] border border-[#ececf2]">
              <div className="flex justify-between items-start">
                <div>
                  <span className="text-[10px] font-bold text-[#e86bc4] uppercase">
                    Comercio Seleccionado
                  </span>
                  <h3 className="text-lg font-bold text-[#2b2b3a] mt-0.5">
                    {selectedVisit.comercio}
                  </h3>
                  <p className="text-[12px] text-[#6b6b80]">{selectedVisit.direccion}</p>
                </div>
                <Button secondary onClick={() => alert('Abriendo historial comercial del cliente... (Simulado)')}>
                  Historial de Compras
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

