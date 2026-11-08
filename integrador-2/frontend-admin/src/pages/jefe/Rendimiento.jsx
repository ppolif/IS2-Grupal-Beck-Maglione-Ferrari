import Button from '../../components/Button';
import Icon from '../../components/Icon';
import { useRendimiento } from '../../hooks/useRendimiento';

export default function Rendimiento() {
  const {
    zonaFilter,
    setZonaFilter,
    filtered,
    handleDownloadPdf
  } = useRendimiento();

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-primary">
          Fuerza de Ventas
        </span>
        <h2 className="text-xl font-bold text-ink mt-0.5">
          Ranking y Rendimiento de Preventores
        </h2>
        <p className="text-[12px] text-muted mt-0.5">
          Métricas individuales de cobertura geográfica, efectividad de visita y volumen facturado.
        </p>
      </div>

      {/* Filterbar matching prototype filterbar */}
      <div className="bg-white rounded-[18px] p-4 border border-border-subtle shadow-sm flex flex-wrap items-center gap-3">
        <span className="border border-border-subtle px-3.5 py-2 rounded-[10px] text-[11px] text-muted bg-surface-soft">
          Período: <strong className="text-ink">Mes Actual (Junio 2025)</strong>
        </span>

        <div className="flex items-center gap-1.5 border border-border-subtle px-3 py-1.5 rounded-[10px] bg-surface-soft text-[11px] text-muted">
          <span>Zona:</span>
          <select
            value={zonaFilter}
            onChange={(e) => setZonaFilter(e.target.value)}
            className="bg-transparent text-ink font-semibold outline-none cursor-pointer"
          >
            <option value="Todas">Todas las zonas</option>
            <option value="Godoy Cruz">Godoy Cruz</option>
            <option value="Capital">Capital</option>
            <option value="Guaymallén">Guaymallén</option>
          </select>
        </div>

        <div className="ml-auto">
          <Button secondary onClick={handleDownloadPdf}>
            <Icon name="download" className="w-3.5 h-3.5" />
            <span>Descargar PDF</span>
          </Button>
        </div>
      </div>

      {/* Main Ranking Table Module matching prototype */}
      <div className="bg-white rounded-[20px] p-6 border border-border-subtle shadow-sm space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-divider">
          <h3 className="text-[15px] font-bold text-ink">
            Ranking de Preventores
          </h3>
          <span className="text-[11px] text-muted">
            Criterio: Facturación neta mensual
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                <th className="pb-3 px-3">Posición</th>
                <th className="pb-3 px-3">Preventor</th>
                <th className="pb-3 px-3">Zona Comercial</th>
                <th className="pb-3 px-3">Ventas Totales</th>
                <th className="pb-3 px-3">Visitas Realizadas</th>
                <th className="pb-3 px-3">Pedidos Concretados</th>
                <th className="pb-3 px-3 text-right">Efectividad</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-divider text-[12px]">
              {filtered.map((r) => (
                <tr key={r.preventor} className="hover:bg-slate-50/70 transition-colors">
                  <td className="py-3 px-3">
                    <span
                      className={`w-7 h-7 rounded-full flex items-center justify-center font-bold text-[12px] ${
                        r.ranking === 1
                          ? 'bg-primary-light text-primary-dark'
                          : r.ranking === 2
                          ? 'bg-info-bg text-info-text'
                          : 'bg-rank-neutral-bg text-muted'
                      }`}
                    >
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
                    {r.visitasEfectuadas} visitas
                  </td>
                  <td className="py-3 px-3 text-muted">
                    {r.pedidosConcretados} pedidos
                  </td>
                  <td className="py-3 px-3 text-right">
                    <span className="text-[12px] font-bold text-success-icon bg-success-bg px-2.5 py-0.5 rounded-full inline-block">
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
