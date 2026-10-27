import { useState } from 'react';
import MapPlaceholder from '../../components/MapPlaceholder';

export default function Rutas() {
  const [tab, setTab] = useState('preventores');

  const agents =
    tab === 'preventores'
      ? [
          { name: 'Lucía Martínez', stops: '8 de 12 visitas', pct: 67, zona: 'Godoy Cruz' },
          { name: 'Martín Flores', stops: '5 de 10 visitas', pct: 50, zona: 'Capital' },
          { name: 'Sofía Vidal', stops: '7 de 9 visitas', pct: 78, zona: 'Guaymallén' }
        ]
      : [
          { name: 'Unidad 08 (Reparto)', stops: '6 de 10 entregas', pct: 60, zona: 'Godoy Cruz / Maipú' },
          { name: 'Unidad 03 (Reparto)', stops: '8 de 12 entregas', pct: 66, zona: 'Ciudad / Las Heras' },
          { name: 'Unidad 05 (Reparto)', stops: '4 de 8 entregas', pct: 50, zona: 'Guaymallén Este' }
        ];

  return (
    <div className="space-y-6">
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Supervisión Logística
        </span>
        <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
          Rutas del Día en Tiempo Real
        </h2>
        <p className="text-[12px] text-[#6b6b80] mt-0.5">
          Monitoreo del avance de visitas comerciales y entregas de vehículos en Gran Mendoza.
        </p>
      </div>

      <div className="bg-white rounded-[22px] border border-[#ececf2] shadow-sm overflow-hidden">
        {/* Tabs from prototype */}
        <div className="flex gap-8 px-6 pt-5 pb-3 border-b border-[#ececf2] text-[13px] font-bold">
          <button
            onClick={() => setTab('preventores')}
            className={`pb-2 transition-all cursor-pointer ${
              tab === 'preventores'
                ? 'text-[#e86bc4] border-b-2 border-[#e86bc4]'
                : 'text-[#8c8b9c] hover:text-[#2b2b3a]'
            }`}
          >
            Preventores en Calle
          </button>
          <button
            onClick={() => setTab('distribuidores')}
            className={`pb-2 transition-all cursor-pointer ${
              tab === 'distribuidores'
                ? 'text-[#e86bc4] border-b-2 border-[#e86bc4]'
                : 'text-[#8c8b9c] hover:text-[#2b2b3a]'
            }`}
          >
            Unidades de Distribución
          </button>
        </div>

        {/* Layout: Sidebar list + Map matching prototype routes-layout */}
        <div className="grid grid-cols-1 lg:grid-cols-12 min-h-[580px]">
          {/* Agent list */}
          <div className="lg:col-span-4 p-4 border-r border-[#ececf2] space-y-2 bg-[#fdfdfd]">
            <span className="text-[10px] font-bold text-[#aaa9ba] uppercase tracking-wider block mb-2 px-1">
              {tab === 'preventores' ? 'Fuerza de Ventas' : 'Flota de Camiones'}
            </span>

            {agents.map((a, i) => {
              const initials = a.name
                .split(' ')
                .map((x) => x[0])
                .join('');
              return (
                <div
                  key={a.name}
                  className="flex items-center gap-3 p-3.5 rounded-[16px] border border-[#ececf2] bg-white hover:border-[#3fb8d9] transition-all cursor-pointer shadow-xs"
                >
                  <span className="w-10 h-10 rounded-full bg-gradient-to-br from-[#ffcef3] to-[#a1eafb] text-[#2b2b3a] font-bold text-[12px] flex items-center justify-center shrink-0">
                    {initials}
                  </span>
                  <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-baseline">
                      <b className="text-[13px] text-[#2b2b3a] font-semibold truncate">
                        {a.name}
                      </b>
                      <small className="text-[10px] text-[#6b6b80] font-medium">
                        {a.stops}
                      </small>
                    </div>
                    <span className="text-[10px] text-[#3fb8d9] font-medium block">
                      {a.zona}
                    </span>
                    {/* Progress bar matching prototype */}
                    <div className="h-1.5 bg-[#f0f0f4] rounded-full mt-2 overflow-hidden">
                      <div
                        className="h-full bg-gradient-to-r from-[#e86bc4] to-[#3fb8d9] rounded-full"
                        style={{ width: `${a.pct}%` }}
                      />
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Map column with exact text required */}
          <div className="lg:col-span-8 p-4 bg-[#f8f9fc] flex flex-col justify-center">
            <MapPlaceholder className="h-[480px] w-full" />
          </div>
        </div>
      </div>
    </div>
  );
}

