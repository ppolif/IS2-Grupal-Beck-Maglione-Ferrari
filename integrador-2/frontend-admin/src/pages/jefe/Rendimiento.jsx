import { useState } from 'react';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import { preventorRankingMock } from '../../mocks/reports';

export default function Rendimiento() {
  const [zonaFilter, setZonaFilter] = useState('Todas');

  const filtered = preventorRankingMock.filter((p) => {
    if (zonaFilter === 'Todas') return true;
    return p.zona === zonaFilter;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Fuerza de Ventas
        </span>
        <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
          Ranking y Rendimiento de Preventores
        </h2>
        <p className="text-[12px] text-[#6b6b80] mt-0.5">
          Métricas individuales de cobertura geográfica, efectividad de visita y volumen facturado.
        </p>
      </div>

      {/* Filterbar matching prototype filterbar */}
      <div className="bg-white rounded-[18px] p-4 border border-[#ececf2] shadow-sm flex flex-wrap items-center gap-3">
        <span className="border border-[#ececf2] px-3.5 py-2 rounded-[10px] text-[11px] text-[#6b6b80] bg-[#fafafd]">
          Período: <strong className="text-[#2b2b3a]">Mes Actual (Junio 2025)</strong>
        </span>

        <div className="flex items-center gap-1.5 border border-[#ececf2] px-3 py-1.5 rounded-[10px] bg-[#fafafd] text-[11px] text-[#6b6b80]">
          <span>Zona:</span>
          <select
            value={zonaFilter}
            onChange={(e) => setZonaFilter(e.target.value)}
            className="bg-transparent text-[#2b2b3a] font-semibold outline-none cursor-pointer"
          >
            <option value="Todas">Todas las zonas</option>
            <option value="Godoy Cruz">Godoy Cruz</option>
            <option value="Capital">Capital</option>
            <option value="Guaymallén">Guaymallén</option>
          </select>
        </div>

        <div className="ml-auto">
          <Button secondary onClick={() => alert('Descargando ranking en PDF... (Simulado)')}>
            <Icon name="download" className="w-3.5 h-3.5" />
            <span>Descargar PDF</span>
          </Button>
        </div>
      </div>

      {/* Main Ranking Table Module matching prototype */}
      <div className="bg-white rounded-[20px] p-6 border border-[#ececf2] shadow-sm space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-[#f0f0f4]">
          <h3 className="text-[15px] font-bold text-[#2b2b3a]">
            Ranking de Preventores
          </h3>
          <span className="text-[11px] text-[#6b6b80]">
            Criterio: Facturación neta mensual
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-[#ececf2] text-[10px] uppercase font-bold text-[#aaa9b8] tracking-wider">
                <th className="pb-3 px-3">Posición</th>
                <th className="pb-3 px-3">Preventor</th>
                <th className="pb-3 px-3">Zona Comercial</th>
                <th className="pb-3 px-3">Ventas Totales</th>
                <th className="pb-3 px-3">Visitas Realizadas</th>
                <th className="pb-3 px-3">Pedidos Concretados</th>
                <th className="pb-3 px-3 text-right">Efectividad</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#f0f0f4] text-[12px]">
              {filtered.map((r) => (
                <tr key={r.preventor} className="hover:bg-slate-50/70 transition-colors">
                  <td className="py-3 px-3">
                    <span
                      className={`w-7 h-7 rounded-full flex items-center justify-center font-bold text-[12px] ${
                        r.ranking === 1
                          ? 'bg-[#ffe2f7] text-[#a34187]'
                          : r.ranking === 2
                          ? 'bg-[#d9f7ff] text-[#247f99]'
                          : 'bg-[#f0f2f7] text-[#6b6b80]'
                      }`}
                    >
                      #{r.ranking}
                    </span>
                  </td>
                  <td className="py-3 px-3 font-semibold text-[#2b2b3a]">
                    {r.preventor}
                  </td>
                  <td className="py-3 px-3 text-[#6b6b80]">{r.zona}</td>
                  <td className="py-3 px-3 font-bold text-[#2b2b3a]">
                    {r.ventasTotal}
                  </td>
                  <td className="py-3 px-3 text-[#6b6b80]">
                    {r.visitasEfectuadas} visitas
                  </td>
                  <td className="py-3 px-3 text-[#6b6b80]">
                    {r.pedidosConcretados} pedidos
                  </td>
                  <td className="py-3 px-3 text-right">
                    <span className="text-[12px] font-bold text-[#39855b] bg-[#dff8e9] px-2.5 py-0.5 rounded-full inline-block">
                      {r.efectividad}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

