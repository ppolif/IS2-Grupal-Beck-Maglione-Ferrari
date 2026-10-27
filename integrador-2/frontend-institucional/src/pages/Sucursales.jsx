import { useState } from 'react';
import MapPlaceholder from '../components/MapPlaceholder';
import Icon from '../components/Icon';
import Button from '../components/Button';
import { branchesMock } from '../mocks/branches';

export default function Sucursales() {
  const [activeBranch, setActiveBranch] = useState(branchesMock[0]);

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Red de Distribución
        </span>
        <h1 className="text-3xl font-bold text-[#2b2b3a] mt-1">
          Sucursales y Depósitos en Mendoza
        </h1>
        <p className="text-[13px] text-[#6b6b80] mt-0.5">
          Conocé nuestros puntos de atención mayorista, retiros programados y centros logísticos.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Branch list (Left side on desktop, stacked on mobile) */}
        <div className="lg:col-span-5 space-y-4">
          {branchesMock.map((branch) => {
            const isSelected = activeBranch.id === branch.id;
            return (
              <div
                key={branch.id}
                onClick={() => setActiveBranch(branch)}
                className={`p-5 rounded-[20px] border transition-all cursor-pointer ${
                  isSelected
                    ? 'bg-white border-[#e86bc4] shadow-[0_14px_40px_rgba(232,107,196,0.12)] ring-1 ring-[#e86bc4]'
                    : 'bg-white border-[#ececf2] hover:border-[#3fb8d9]'
                }`}
              >
                <div className="flex items-start justify-between gap-2">
                  <span className="text-[10px] font-bold uppercase tracking-wider text-[#3fb8d9] bg-[#d9f7ff] px-2.5 py-1 rounded-full">
                    {branch.tipo}
                  </span>
                  {isSelected && (
                    <span className="text-[10px] font-bold text-[#e86bc4] bg-[#ffe2f7] px-2 py-0.5 rounded-full">
                      Seleccionada
                    </span>
                  )}
                </div>

                <h3 className="font-bold text-[16px] text-[#2b2b3a] mt-3">
                  {branch.nombre}
                </h3>

                <div className="mt-3 space-y-2 text-[12px] text-[#6b6b80]">
                  <div className="flex items-center gap-2">
                    <span className="text-[#e86bc4]">
                      <Icon name="mapPin" className="w-4 h-4" />
                    </span>
                    <span>{branch.direccion}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-[#3fb8d9]">
                      <Icon name="phone" className="w-4 h-4" />
                    </span>
                    <span>{branch.telefono}</span>
                  </div>
                </div>

                <div className="mt-4 pt-3 border-t border-[#f0f0f4] text-[11px] text-[#6b6b80]">
                  <strong>Horario:</strong> {branch.horario}
                </div>
              </div>
            );
          })}
        </div>

        {/* Map and detail (Right side on desktop) */}
        <div className="lg:col-span-7 space-y-6">
          <div className="bg-white rounded-[22px] p-6 border border-[#ececf2] shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <div>
                <span className="text-[10px] font-bold tracking-wider text-[#6b6b80] uppercase">
                  Ubicación en el mapa
                </span>
                <h3 className="text-[17px] font-bold text-[#2b2b3a]">
                  {activeBranch.nombre}
                </h3>
              </div>
              <Button secondary className="text-[11px] px-3 py-1.5">
                Cómo llegar
              </Button>
            </div>

            {/* Responsive map placeholder with exact text required */}
            <MapPlaceholder className="h-[400px] sm:h-[460px] w-full" />

            <div className="mt-4 p-4 rounded-[14px] bg-[#f8f9fc] flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
              <div>
                <span className="text-[11px] text-[#6b6b80] block">Dirección exacta:</span>
                <strong className="text-[13px] text-[#2b2b3a]">{activeBranch.direccion}</strong>
              </div>
              <span className="text-[11px] bg-[#dff8e9] text-[#39855b] font-semibold px-2.5 py-1 rounded-full">
                Abierto hoy hasta 18:30 hs
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

