import { Link } from 'react-router-dom';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import Kpis from '../../components/Kpis';
import Chip from '../../components/Chip';
import { useDashboard } from '../../hooks/useDashboard';
import { colors } from '../../theme/colors';

export default function Dashboard() {
  const {
    recentOrders,
    monthlySalesChart,
    weeklyActivityChart,
    categoryDonutChart,
  } = useDashboard();

  return (
    <div className="space-y-6">
      {/* Top Manager KPIs */}
      <Kpis warehouse={false} />

      {/* Grid: Line Chart (Ventas del mes) & Bar Chart (Pedidos por día) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Monthly Sales Line Chart */}
        <div className="lg:col-span-8 bg-white rounded-[20px] p-5 border border-border-subtle shadow-sm">
          <div className="flex items-center justify-between mb-4 pb-2 border-b border-divider">
            <div>
              <h3 className="text-[15px] font-bold text-ink">
                Ventas del Mes en Curso
              </h3>
              <p className="text-[10px] text-muted">
                Comparativo diario vs período anterior
              </p>
            </div>
            <Chip variant="blue">Junio 2025</Chip>
          </div>

          <div className="h-[240px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={monthlySalesChart} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke={colors.border.divider} vertical={false} />
                <XAxis dataKey="dia" tick={{ fontSize: 10, fill: colors.muted.subtle }} stroke={colors.border.DEFAULT} />
                <YAxis tick={{ fontSize: 10, fill: colors.muted.subtle }} stroke={colors.border.DEFAULT} />
                <Tooltip
                  formatter={(value) => [`$${value.toLocaleString('es-AR')}`, 'Ventas']}
                  contentStyle={{ backgroundColor: colors.surface.DEFAULT, borderRadius: '12px', border: `1px solid ${colors.border.DEFAULT}`, fontSize: '11px' }}
                />
                <Line
                  type="monotone"
                  dataKey="ventas"
                  stroke={colors.primary.DEFAULT}
                  strokeWidth={3.5}
                  dot={{ r: 4, fill: colors.primary.DEFAULT, strokeWidth: 2, stroke: colors.surface.DEFAULT }}
                  activeDot={{ r: 6 }}
                />
                <Line
                  type="monotone"
                  dataKey="previo"
                  stroke={colors.secondary.DEFAULT}
                  strokeWidth={2.5}
                  strokeDasharray="4 4"
                  dot={false}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Weekly Activity Bar Chart */}
        <div className="lg:col-span-4 bg-white rounded-[20px] p-5 border border-border-subtle shadow-sm">
          <div className="mb-4 pb-2 border-b border-divider">
            <h3 className="text-[15px] font-bold text-ink">
              Pedidos por Día de Semana
            </h3>
            <p className="text-[10px] text-muted">
              Volumen de pedidos procesados
            </p>
          </div>

          <div className="h-[240px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={weeklyActivityChart} margin={{ top: 10, right: 10, left: -25, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke={colors.border.divider} vertical={false} />
                <XAxis dataKey="dia" tick={{ fontSize: 10, fill: colors.muted.subtle }} stroke={colors.border.DEFAULT} />
                <YAxis tick={{ fontSize: 10, fill: colors.muted.subtle }} stroke={colors.border.DEFAULT} />
                <Tooltip
                  formatter={(value) => [`${value} pedidos`, 'Volumen']}
                  contentStyle={{ backgroundColor: colors.surface.DEFAULT, borderRadius: '12px', border: `1px solid ${colors.border.DEFAULT}`, fontSize: '11px' }}
                />
                <Bar
                  dataKey="pedidos"
                  fill={colors.secondary.DEFAULT}
                  radius={[8, 8, 2, 2]}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Lower Grid: Recent Orders Table & Categories Donut Chart */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Recent Orders Table */}
        <div className="lg:col-span-8 bg-white rounded-[20px] p-5 border border-border-subtle shadow-sm">
          <div className="flex items-center justify-between mb-3 pb-2 border-b border-divider">
            <h3 className="text-[15px] font-bold text-ink">
              Últimos Pedidos Ingresados
            </h3>
            <Link
              to="/pedidos"
              className="text-[10px] font-bold tracking-wider uppercase text-primary hover:underline"
            >
              Ver todos →
            </Link>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-border-subtle text-[9px] uppercase font-bold text-muted-subtle tracking-wider">
                  <th className="pb-2.5 px-2">Pedido</th>
                  <th className="pb-2.5 px-2">Comercio</th>
                  <th className="pb-2.5 px-2">Preventor</th>
                  <th className="pb-2.5 px-2">Monto</th>
                  <th className="pb-2.5 px-2 text-right">Estado</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-divider text-[11px]">
                {recentOrders.map((o) => (
                  <tr key={o.id} className="hover:bg-slate-50/70">
                    <td className="py-2.5 px-2 font-bold text-ink">
                      #{o.id}
                    </td>
                    <td className="py-2.5 px-2 font-medium text-ink">
                      {o.comercio}
                    </td>
                    <td className="py-2.5 px-2 text-muted">{o.preventor}</td>
                    <td className="py-2.5 px-2 font-bold text-ink">
                      {o.monto}
                    </td>
                    <td className="py-2.5 px-2 text-right">
                      <span className="text-[9px] font-semibold text-success-icon bg-success-bg px-2 py-0.5 rounded-full inline-block">
                        {o.estado}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Categories Donut Chart */}
        <div className="lg:col-span-4 bg-white rounded-[20px] p-5 border border-border-subtle shadow-sm flex flex-col justify-between">
          <div className="mb-2 pb-2 border-b border-divider">
            <h3 className="text-[15px] font-bold text-ink">
              Stock por Categoría
            </h3>
            <p className="text-[10px] text-muted">
              Distribución de unidades almacenadas
            </p>
          </div>

          <div className="h-[170px] w-full flex items-center justify-center relative">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={categoryDonutChart}
                  innerRadius={50}
                  outerRadius={75}
                  paddingAngle={3}
                  dataKey="value"
                >
                  {categoryDonutChart.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(val) => [`${val}%`, 'Proporción']}
                  contentStyle={{ backgroundColor: colors.surface.DEFAULT, borderRadius: '10px', fontSize: '10px' }}
                />
              </PieChart>
            </ResponsiveContainer>
            {/* Center label */}
            <div className="absolute text-center pointer-events-none">
              <b className="text-[16px] font-bold text-ink block leading-none">1.284</b>
              <span className="text-[8px] text-muted">unidades</span>
            </div>
          </div>

          {/* Legend */}
          <div className="flex justify-around items-center pt-2 border-t border-divider text-[10px]">
            {categoryDonutChart.map((c) => (
              <div key={c.name} className="flex items-center gap-1.5">
                <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: c.color }} />
                <span className="text-muted font-medium">{c.name} {c.value}%</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
