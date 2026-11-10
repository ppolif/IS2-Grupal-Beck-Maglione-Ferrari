import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  PieChart,
  Pie,
  Cell,
  AreaChart,
  Area,
} from 'recharts';
import { colors } from '../../theme/colors';
import { useEstadisticas } from '../../hooks/useEstadisticas';

export default function Estadisticas() {
  const {
    loading,
    summary,
    annualGrowthData,
    weeklyActivityChart,
    categoryDonutChart,
  } = useEstadisticas();

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-primary">
          Métricas y Analítica
        </span>
        <h2 className="text-xl font-bold text-ink mt-0.5">
          Estadísticas Comerciales y Rendimiento Global
        </h2>
        <p className="text-[12px] text-muted mt-0.5">
          Indicadores de facturación, distribución de pedidos y composición real de inventario.
        </p>
      </div>

      {/* Top 3 Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white rounded-[18px] p-4 border border-border-subtle shadow-sm">
          <small className="text-[10px] uppercase font-bold text-muted-label">Ticket Promedio Mayorista</small>
          <div className="flex items-baseline justify-between mt-1">
            <b className="text-2xl font-bold text-ink">
              ${summary.ticketPromedio.toLocaleString('es-AR')}
            </b>
          </div>
          <p className="text-[11px] text-muted mt-1">Promedio por pedido registrado</p>
        </div>

        <div className="bg-white rounded-[18px] p-4 border border-border-subtle shadow-sm">
          <small className="text-[10px] uppercase font-bold text-muted-label">Efectividad Operativa</small>
          <div className="flex items-baseline justify-between mt-1">
            <b className="text-2xl font-bold text-primary">{summary.efectividadPreventa}%</b>
          </div>
          <p className="text-[11px] text-muted mt-1">Pedidos activos sobre el total</p>
        </div>

        <div className="bg-white rounded-[18px] p-4 border border-border-subtle shadow-sm">
          <small className="text-[10px] uppercase font-bold text-muted-label">Cumplimiento de Entrega</small>
          <div className="flex items-baseline justify-between mt-1">
            <b className="text-2xl font-bold text-secondary">{summary.cumplimientoEntrega}%</b>
          </div>
          <p className="text-[11px] text-muted mt-1">Pedidos entregados sobre gestionados</p>
        </div>
      </div>

      {/* Chart 1: Revenue Evolution Area Chart */}
      <div className="bg-white rounded-[20px] p-5 border border-border-subtle shadow-sm">
        <div className="flex items-center justify-between mb-4 pb-2 border-b border-divider">
          <div>
            <h3 className="text-[15px] font-bold text-ink">
              Evolución de Facturación Mensual ($)
            </h3>
            <p className="text-[10px] text-muted">
              Facturación acumulada según comprobantes emitidos
            </p>
          </div>
        </div>

        {loading ? (
          <div className="h-[220px] flex items-center justify-center text-[12px] text-muted">
            Cargando evolución de facturación...
          </div>
        ) : annualGrowthData.length === 0 ? (
          <div className="h-[220px] flex items-center justify-center text-[13px] text-muted">
            No hay facturas emitidas aún para graficar la evolución mensual.
          </div>
        ) : (
          <div className="h-[260px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={annualGrowthData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorFacturado" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor={colors.primary.DEFAULT} stopOpacity={0.4} />
                    <stop offset="95%" stopColor={colors.primary.DEFAULT} stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke={colors.border.divider} vertical={false} />
                <XAxis dataKey="mes" tick={{ fontSize: 11, fill: colors.muted.subtle }} stroke={colors.border.DEFAULT} />
                <YAxis tick={{ fontSize: 11, fill: colors.muted.subtle }} stroke={colors.border.DEFAULT} />
                <Tooltip
                  formatter={(val) => [`$${Number(val).toLocaleString('es-AR')}`, 'Facturación']}
                  contentStyle={{ backgroundColor: '#fff', borderRadius: '10px', fontSize: '11px' }}
                />
                <Area
                  type="monotone"
                  dataKey="facturado"
                  stroke={colors.primary.DEFAULT}
                  strokeWidth={3}
                  fillOpacity={1}
                  fill="url(#colorFacturado)"
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      {/* Dual Charts: Daily Comparison & Product Breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        <div className="bg-white rounded-[20px] p-5 border border-border-subtle shadow-sm">
          <h3 className="text-[15px] font-bold text-ink mb-1">
            Volumen Semanal de Pedidos
          </h3>
          <p className="text-[10px] text-muted mb-4">
            Distribución operativa de lunes a domingo
          </p>

          {loading ? (
            <div className="h-[220px] flex items-center justify-center text-[12px] text-muted">
              Cargando actividad semanal...
            </div>
          ) : weeklyActivityChart.length === 0 ? (
            <div className="h-[220px] flex items-center justify-center text-[13px] text-muted">
              No hay pedidos registrados para mostrar el volumen semanal.
            </div>
          ) : (
            <div className="h-[220px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={weeklyActivityChart} margin={{ top: 5, right: 5, left: -25, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke={colors.border.divider} vertical={false} />
                  <XAxis dataKey="dia" tick={{ fontSize: 10, fill: colors.muted.subtle }} stroke={colors.border.DEFAULT} />
                  <YAxis tick={{ fontSize: 10, fill: colors.muted.subtle }} stroke={colors.border.DEFAULT} />
                  <Tooltip contentStyle={{ borderRadius: '10px', fontSize: '10px' }} />
                  <Bar dataKey="pedidos" fill={colors.secondary.DEFAULT} radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        <div className="bg-white rounded-[20px] p-5 border border-border-subtle shadow-sm">
          <h3 className="text-[15px] font-bold text-ink mb-1">
            Distribución de Stock por Producto
          </h3>
          <p className="text-[10px] text-muted mb-4">
            Unidades disponibles en depósito
          </p>

          {loading ? (
            <div className="h-[220px] flex items-center justify-center text-[12px] text-muted">
              Cargando distribución de stock...
            </div>
          ) : categoryDonutChart.length === 0 ? (
            <div className="h-[220px] flex items-center justify-center text-[13px] text-muted">
              No hay productos con stock registrado en depósito.
            </div>
          ) : (
            <div className="h-[220px] w-full flex items-center justify-center">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={categoryDonutChart}
                    innerRadius={50}
                    outerRadius={80}
                    dataKey="value"
                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  >
                    {categoryDonutChart.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={{ borderRadius: '10px', fontSize: '10px' }} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
