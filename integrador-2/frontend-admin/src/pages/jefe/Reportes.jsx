import { useState } from 'react';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import {
  preventorRankingMock,
  topCandiesMock,
  monthlySalesChart
} from '../../mocks/reports';

export default function Reportes() {
  const [activeReport, setActiveReport] = useState('ventas');
  const [periodo, setPeriodo] = useState('Junio 2025');

  const handleDownloadPDF = () => {
    alert(`Descargando "${getReportTitle()}" en formato PDF... (Simulado)`);
  };

  const handleDownloadExcel = () => {
    alert(`Descargando "${getReportTitle()}" en formato Excel (.xlsx)... (Simulado)`);
  };

  const getReportTitle = () => {
    if (activeReport === 'ventas') return 'Reporte Global de Ventas por Período';
    if (activeReport === 'preventores') return 'Análisis de Rendimiento de Preventores';
    return 'Análisis del Volumen de Golosinas Más Vendidas';
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Informes Gerenciales
        </span>
        <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
          Reportes y Estadísticas Ejecutivas
        </h2>
        <p className="text-[12px] text-[#6b6b80] mt-0.5">
          Consultá y descargá consolidados de ventas periódicas, ranking de preventores y rotación de productos.
        </p>
      </div>

      {/* Filterbar & Download Actions matching consigna */}
      <div className="bg-white rounded-[18px] p-4 border border-[#ececf2] shadow-sm flex flex-col md:flex-row items-stretch md:items-center justify-between gap-4">
        {/* Filter chips / selects */}
        <div className="flex flex-wrap items-center gap-2.5">
          <span className="border border-[#ececf2] px-3 py-1.5 rounded-[10px] text-[11px] text-[#6b6b80] bg-[#fafafd]">
            Período: <strong className="text-[#2b2b3a]">{periodo}</strong>
          </span>

          <span className="border border-[#ececf2] px-3 py-1.5 rounded-[10px] text-[11px] text-[#6b6b80] bg-[#fafafd]">
            Zona: <strong className="text-[#2b2b3a]">Todas las Zonas (Mendoza)</strong>
          </span>
        </div>

        {/* Buttons Descargar PDF y Descargar Excel */}
        <div className="flex items-center gap-2.5">
          <Button
            secondary
            onClick={handleDownloadPDF}
            className="text-[11px] px-3.5 py-2"
          >
            <Icon name="download" className="w-3.5 h-3.5" />
            <span>Descargar PDF</span>
          </Button>

          <Button
            onClick={handleDownloadExcel}
            className="text-[11px] px-3.5 py-2"
          >
            <Icon name="download" className="w-3.5 h-3.5" />
            <span>Descargar Excel</span>
          </Button>
        </div>
      </div>

      {/* Report Selection Tabs */}
      <div className="flex border-b border-[#ececf2] gap-6 text-[13px] font-bold">
        <button
          onClick={() => setActiveReport('ventas')}
          className={`pb-3 transition-colors cursor-pointer ${
            activeReport === 'ventas'
              ? 'text-[#e86bc4] border-b-2 border-[#e86bc4]'
              : 'text-[#8c8b9c] hover:text-[#2b2b3a]'
          }`}
        >
          1. Ventas por Período
        </button>
        <button
          onClick={() => setActiveReport('preventores')}
          className={`pb-3 transition-colors cursor-pointer ${
            activeReport === 'preventores'
              ? 'text-[#e86bc4] border-b-2 border-[#e86bc4]'
              : 'text-[#8c8b9c] hover:text-[#2b2b3a]'
          }`}
        >
          2. Rendimiento de Preventores
        </button>
        <button
          onClick={() => setActiveReport('golosinas')}
          className={`pb-3 transition-colors cursor-pointer ${
            activeReport === 'golosinas'
              ? 'text-[#e86bc4] border-b-2 border-[#e86bc4]'
              : 'text-[#8c8b9c] hover:text-[#2b2b3a]'
          }`}
        >
          3. Golosinas Más Vendidas
        </button>
      </div>

      {/* Report 1: Ventas por Período */}
      {activeReport === 'ventas' && (
        <div className="bg-white rounded-[20px] p-6 border border-[#ececf2] shadow-sm space-y-4">
          <div className="flex justify-between items-center pb-3 border-b border-[#f0f0f4]">
            <div>
              <h3 className="text-[15px] font-bold text-[#2b2b3a]">
                Reporte Global de Ventas - Período {periodo}
              </h3>
              <p className="text-[11px] text-[#6b6b80]">
                Facturación acumulada: <strong>$12.800.000</strong> (+18.4% vs Mayo)
              </p>
            </div>
            <span className="text-[11px] bg-[#dff8e9] text-[#39855b] font-bold px-3 py-1 rounded-full">
              Meta superada
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-[#ececf2] text-[10px] uppercase font-bold text-[#aaa9b8] tracking-wider">
                  <th className="pb-3 px-3">Día</th>
                  <th className="pb-3 px-3">Ventas Netas</th>
                  <th className="pb-3 px-3">Período Anterior</th>
                  <th className="pb-3 px-3">Variación</th>
                  <th className="pb-3 px-3 text-right">Cumplimiento</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#f0f0f4] text-[12px]">
                {monthlySalesChart.map((row) => {
                  const dif = row.ventas - row.previo;
                  const pct = Math.round((dif / row.previo) * 100);
                  return (
                    <tr key={row.dia} className="hover:bg-slate-50/70">
                      <td className="py-3 px-3 font-semibold text-[#2b2b3a]">
                        Día {row.dia}
                      </td>
                      <td className="py-3 px-3 font-bold text-[#2b2b3a]">
                        ${row.ventas.toLocaleString('es-AR')}
                      </td>
                      <td className="py-3 px-3 text-[#6b6b80]">
                        ${row.previo.toLocaleString('es-AR')}
                      </td>
                      <td className="py-3 px-3">
                        <span className={`font-semibold ${pct >= 0 ? 'text-[#39855b]' : 'text-[#ff8fa3]'}`}>
                          {pct >= 0 ? `+${pct}%` : `${pct}%`}
                        </span>
                      </td>
                      <td className="py-3 px-3 text-right">
                        <span className="text-[10px] font-semibold text-[#3fb8d9] bg-[#d9f7ff] px-2.5 py-0.5 rounded-full">
                          En meta
                        </span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Report 2: Rendimiento de Preventores */}
      {activeReport === 'preventores' && (
        <div className="bg-white rounded-[20px] p-6 border border-[#ececf2] shadow-sm space-y-4">
          <div className="flex justify-between items-center pb-3 border-b border-[#f0f0f4]">
            <div>
              <h3 className="text-[15px] font-bold text-[#2b2b3a]">
                Análisis de Rendimiento de la Fuerza de Preventores
              </h3>
              <p className="text-[11px] text-[#6b6b80]">
                Medición de visitas, efectividad de cierre y volumen facturado
              </p>
            </div>
            <span className="text-[11px] text-[#6b6b80]">
              Total preventores: <strong>3 activos</strong>
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-[#ececf2] text-[10px] uppercase font-bold text-[#aaa9b8] tracking-wider">
                  <th className="pb-3 px-3">Posición</th>
                  <th className="pb-3 px-3">Preventor</th>
                  <th className="pb-3 px-3">Zona</th>
                  <th className="pb-3 px-3">Ventas Totales</th>
                  <th className="pb-3 px-3">Pedidos</th>
                  <th className="pb-3 px-3">Visitas</th>
                  <th className="pb-3 px-3 text-right">Efectividad</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#f0f0f4] text-[12px]">
                {preventorRankingMock.map((r) => (
                  <tr key={r.preventor} className="hover:bg-slate-50/70">
                    <td className="py-3 px-3">
                      <span className="w-6 h-6 rounded-full bg-[#ffe2f7] text-[#e86bc4] font-bold text-[11px] flex items-center justify-center">
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
                      {r.pedidosConcretados} pedidos
                    </td>
                    <td className="py-3 px-3 text-[#6b6b80]">
                      {r.visitasEfectuadas} visitas
                    </td>
                    <td className="py-3 px-3 text-right">
                      <strong className="text-[#39855b] font-bold text-[13px]">
                        {r.efectividad}
                      </strong>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Report 3: Golosinas Más Vendidas */}
      {activeReport === 'golosinas' && (
        <div className="bg-white rounded-[20px] p-6 border border-[#ececf2] shadow-sm space-y-4">
          <div className="flex justify-between items-center pb-3 border-b border-[#f0f0f4]">
            <div>
              <h3 className="text-[15px] font-bold text-[#2b2b3a]">
                Análisis de Volumen de Golosinas y Confitería
              </h3>
              <p className="text-[11px] text-[#6b6b80]">
                Ranking de rotación en kioscos y almacenes minoristas
              </p>
            </div>
            <span className="text-[11px] bg-[#ffe2f7] text-[#a34187] font-semibold px-3 py-1 rounded-full">
              Líder: Alfajores
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-[#ececf2] text-[10px] uppercase font-bold text-[#aaa9b8] tracking-wider">
                  <th className="pb-3 px-3">Top</th>
                  <th className="pb-3 px-3">Producto</th>
                  <th className="pb-3 px-3">Categoría</th>
                  <th className="pb-3 px-3">Unidades Vendidas</th>
                  <th className="pb-3 px-3">Total Facturado</th>
                  <th className="pb-3 px-3 text-right">Tendencia</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#f0f0f4] text-[12px]">
                {topCandiesMock.map((c) => (
                  <tr key={c.producto} className="hover:bg-slate-50/70">
                    <td className="py-3 px-3 font-bold text-[#e86bc4]">
                      #{c.posicion}
                    </td>
                    <td className="py-3 px-3 font-semibold text-[#2b2b3a]">
                      {c.producto}
                    </td>
                    <td className="py-3 px-3 text-[#6b6b80]">{c.categoria}</td>
                    <td className="py-3 px-3 font-bold text-[#2b2b3a]">
                      {c.unidadesVendidas.toLocaleString('es-AR')} u.
                    </td>
                    <td className="py-3 px-3 font-semibold text-[#2b2b3a]">
                      {c.totalFacturado}
                    </td>
                    <td className="py-3 px-3 text-right">
                      <span className="text-[11px] font-semibold text-[#39855b]">
                        {c.crecimiento}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

