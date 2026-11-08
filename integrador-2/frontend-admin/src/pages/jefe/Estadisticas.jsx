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
  Area
} from 'recharts';
import Chip from '../../components/Chip';
import { colors } from '../../theme/colors';
import { useEstadisticas } from '../../hooks/useEstadisticas';

export default function Estadisticas() {
  const {
    annualGrowthData,
    weeklyActivityChart,
    categoryDonutChart
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
          Indicadores de crecimiento mensual, distribución de ventas y comportamiento de la demanda.
        </p>
      </div>

      {/* Top 3 Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white rounded-[18px] p-4 border border-border-subtle shadow-sm">
          <small className="text-[10px] uppercase font-bold text-muted-label">Ticket Promedio Mayorista</small>
          <div className="flex items-baseline justify-between mt-1">
            <b className="text-2xl font-bold text-ink">$38.450</b>
            <span className="text-[10px] font-semibold text-trend-up bg-success-bg px-2 py-0.5 rounded-full">
              +5.1% mensual
            </span>
          </div>
          <p className="text-[11px] text-muted mt-1">Promedio por pedido entregado</p>
        </div>

        <div className="bg-white rounded-[18px] p-4 border border-border-subtle shadow-sm">
          <small className="text-[10px] uppercase font-bold text-muted-label">Efectividad de Preventa</small>
          <div className="flex items-baseline justify-between mt-1">
            <b className="text-2xl font-bold text-primary">83.7%</b>
            <span className="text-[10px] font-semibold text-trend-up bg-success-bg px-2 py-0.5 rounded-full">
              +3.2% mensual
            </span>
          </div>
          <p className="text-[11px] text-muted mt-1">Visitas con pedido concretado</p>
        </div>

        <div className="bg-white rounded-[18px] p-4 border border-border-subtle shadow-sm">
          <small className="text-[10px] uppercase font-bold text-muted-label">Cumplimiento de Entrega</small>
          <div className="flex items-baseline justify-between mt-1">
            <b className="text-2xl font-bold text-secondary">96.4%</b>
            <span className="text-[10px] font-semibold text-trend-up bg-success-bg px-2 py-0.5 rounded-full">
              En tiempo
            </span>
          </div>
          <p className="text-[11px] text-muted mt-1">Dentro de las 24 hs hábiles</p>
        </div>
      </div>

      {/* Chart 1: Revenue Evolution Area Chart */}
      <div className="bg-white rounded-[20px] p-5 border border-border-subtle shadow-sm">
        <div className="flex items-center justify-between mb-4 pb-2 border-b border-divider">
          <div>
            <h3 className="text-[15px] font-bold text-ink">
              Evolución de Facturación Semestral (Millones de Pesos)
            </h3>
            <p className="text-[10px] text-muted">
              Crecimiento acumulado Enero - Junio 2025
            </p>
          </div>
          <Chip variant="pink">Crecimiento +50.5%</Chip>
        </div>

        <div className="h-[260px] w-full">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={annualGrowthData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
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
                formatter={(val) => [`$${val} M`, 'Facturación']}
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
        </div>

        <div className="bg-white rounded-[20px] p-5 border border-border-subtle shadow-sm">
          <h3 className="text-[15px] font-bold text-ink mb-1">
            Proporción de Stock por Familia
          </h3>
          <p className="text-[10px] text-muted mb-4">
            Composición porcentual en depósito
          </p>

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
        </div>
      </div>
    </div>
  );
}
