import Button from '../../components/Button';
import Icon from '../../components/Icon';
import {
  preventorRankingMock,
  topCandiesMock,
  monthlySalesChart,
} from '../../mocks/reports';
import { useReportes } from '../../hooks/useReportes';

export default function Reportes() {
  const {
    activeReport,
    setActiveReport,
    periodo,
    handleDownloadPDF,
    handleDownloadExcel,
  } = useReportes();

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-primary">
          Informes Gerenciales
        </span>
        <h2 className="text-xl font-bold text-ink mt-0.5">
          Reportes y Estadísticas Ejecutivas
        </h2>
        <p className="text-[12px] text-muted mt-0.5">
          Consultá y descargá consolidados de ventas periódicas, ranking de preventores y rotación de productos.
        </p>
      </div>

      {/* Filterbar & Download Actions matching consigna */}
      <div className="bg-white rounded-[18px] p-4 border border-border-subtle shadow-sm flex flex-col md:flex-row items-stretch md:items-center justify-between gap-4">
        {/* Filter chips / selects */}
        <div className="flex flex-wrap items-center gap-2.5">
          <span className="border border-border-subtle px-3 py-1.5 rounded-[10px] text-[11px] text-muted bg-surface-note">
            Período: <strong className="text-ink">{periodo}</strong>
          </span>

          <span className="border border-border-subtle px-3 py-1.5 rounded-[10px] text-[11px] text-muted bg-surface-note">
            Zona: <strong className="text-ink">Todas las Zonas (Mendoza)</strong>
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
      <div className="flex border-b border-border-subtle gap-6 text-[13px] font-bold">
        <button
          onClick={() => setActiveReport('ventas')}
          className={`pb-3 transition-colors cursor-pointer ${
            activeReport === 'ventas'
              ? 'text-primary border-b-2 border-primary'
              : 'text-muted-light hover:text-ink'
          }`}
        >
          1. Ventas por Período
        </button>
        <button
          onClick={() => setActiveReport('preventores')}
          className={`pb-3 transition-colors cursor-pointer ${
            activeReport === 'preventores'
              ? 'text-primary border-b-2 border-primary'
              : 'text-muted-light hover:text-ink'
          }`}
        >
          2. Rendimiento de Preventores
        </button>
        <button
          onClick={() => setActiveReport('golosinas')}
          className={`pb-3 transition-colors cursor-pointer ${
            activeReport === 'golosinas'
              ? 'text-primary border-b-2 border-primary'
              : 'text-muted-light hover:text-ink'
          }`}
        >
          3. Golosinas Más Vendidas
        </button>
      </div>

      {/* Report 1: Ventas por Período */}
      {activeReport === 'ventas' && (
        <div className="bg-white rounded-[20px] p-6 border border-border-subtle shadow-sm space-y-4">
          <div className="flex justify-between items-center pb-3 border-b border-divider">
            <div>
              <h3 className="text-[15px] font-bold text-ink">
                Reporte Global de Ventas - Período {periodo}
              </h3>
              <p className="text-[11px] text-muted">
                Facturación acumulada: <strong>$12.800.000</strong> (+18.4% vs Mayo)
              </p>
            </div>
            <span className="text-[11px] bg-success-bg text-success-icon font-bold px-3 py-1 rounded-full">
              Meta superada
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                  <th className="pb-3 px-3">Día</th>
                  <th className="pb-3 px-3">Ventas Netas</th>
                  <th className="pb-3 px-3">Período Anterior</th>
                  <th className="pb-3 px-3">Variación</th>
                  <th className="pb-3 px-3 text-right">Cumplimiento</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-divider text-[12px]">
                {monthlySalesChart.map((row) => {
                  const dif = row.ventas - row.previo;
                  const pct = Math.round((dif / row.previo) * 100);
                  return (
                    <tr key={row.dia} className="hover:bg-slate-50/70">
                      <td className="py-3 px-3 font-semibold text-ink">
                        Día {row.dia}
                      </td>
                      <td className="py-3 px-3 font-bold text-ink">
                        ${row.ventas.toLocaleString('es-AR')}
                      </td>
                      <td className="py-3 px-3 text-muted">
                        ${row.previo.toLocaleString('es-AR')}
                      </td>
                      <td className="py-3 px-3">
                        <span className={`font-semibold ${pct >= 0 ? 'text-success-icon' : 'text-brand-error'}`}>
                          {pct >= 0 ? `+${pct}%` : `${pct}%`}
                        </span>
                      </td>
                      <td className="py-3 px-3 text-right">
                        <span className="text-[10px] font-semibold text-secondary bg-info-bg px-2.5 py-0.5 rounded-full">
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
        <div className="bg-white rounded-[20px] p-6 border border-border-subtle shadow-sm space-y-4">
          <div className="flex justify-between items-center pb-3 border-b border-divider">
            <div>
              <h3 className="text-[15px] font-bold text-ink">
                Análisis de Rendimiento de la Fuerza de Preventores
              </h3>
              <p className="text-[11px] text-muted">
                Medición de visitas, efectividad de cierre y volumen facturado
              </p>
            </div>
            <span className="text-[11px] text-muted">
              Total preventores: <strong>3 activos</strong>
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                  <th className="pb-3 px-3">Posición</th>
                  <th className="pb-3 px-3">Preventor</th>
                  <th className="pb-3 px-3">Zona</th>
                  <th className="pb-3 px-3">Ventas Totales</th>
                  <th className="pb-3 px-3">Pedidos</th>
                  <th className="pb-3 px-3">Visitas</th>
                  <th className="pb-3 px-3 text-right">Efectividad</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-divider text-[12px]">
                {preventorRankingMock.map((r) => (
                  <tr key={r.preventor} className="hover:bg-slate-50/70">
                    <td className="py-3 px-3">
                      <span className="w-6 h-6 rounded-full bg-primary-light text-primary font-bold text-[11px] flex items-center justify-center">
                        #{r.ranking}
                      </span>
                    </td>
                    <td className="py-3 px-3 font-semibold text-ink">
                      {r.preventor}
                    </td>
                    <td className="py-3 px-3 text-muted">{r.zona}</td>
                    <td className="py-3 px-3 font-bold text-ink">
                      {r.ventasTotal}
                    </td>
                    <td className="py-3 px-3 text-muted">
                      {r.pedidosConcretados} pedidos
                    </td>
                    <td className="py-3 px-3 text-muted">
                      {r.visitasEfectuadas} visitas
                    </td>
                    <td className="py-3 px-3 text-right">
                      <strong className="text-success-icon font-bold text-[13px]">
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
        <div className="bg-white rounded-[20px] p-6 border border-border-subtle shadow-sm space-y-4">
          <div className="flex justify-between items-center pb-3 border-b border-divider">
            <div>
              <h3 className="text-[15px] font-bold text-ink">
                Análisis de Volumen de Golosinas y Confitería
              </h3>
              <p className="text-[11px] text-muted">
                Ranking de rotación en kioscos y almacenes minoristas
              </p>
            </div>
            <span className="text-[11px] bg-primary-light text-primary-dark font-semibold px-3 py-1 rounded-full">
              Líder: Alfajores
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                  <th className="pb-3 px-3">Top</th>
                  <th className="pb-3 px-3">Producto</th>
                  <th className="pb-3 px-3">Categoría</th>
                  <th className="pb-3 px-3">Unidades Vendidas</th>
                  <th className="pb-3 px-3">Total Facturado</th>
                  <th className="pb-3 px-3 text-right">Tendencia</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-divider text-[12px]">
                {topCandiesMock.map((c) => (
                  <tr key={c.producto} className="hover:bg-slate-50/70">
                    <td className="py-3 px-3 font-bold text-primary">
                      #{c.posicion}
                    </td>
                    <td className="py-3 px-3 font-semibold text-ink">
                      {c.producto}
                    </td>
                    <td className="py-3 px-3 text-muted">{c.categoria}</td>
                    <td className="py-3 px-3 font-bold text-ink">
                      {c.unidadesVendidas.toLocaleString('es-AR')} u.
                    </td>
                    <td className="py-3 px-3 font-semibold text-ink">
                      {c.totalFacturado}
                    </td>
                    <td className="py-3 px-3 text-right">
                      <span className="text-[11px] font-semibold text-success-icon">
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
