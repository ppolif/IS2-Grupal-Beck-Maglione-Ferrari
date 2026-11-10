import Button from '../../components/Button';
import Icon from '../../components/Icon';
import { useReportes } from '../../hooks/useReportes';

export default function Reportes() {
  const {
    activeReport,
    setActiveReport,
    periodo,
    loading,
    ventasRows,
    preventoresRows,
    golosinasRows,
    totalFacturadoVentas,
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

      {/* Filterbar & Download Actions */}
      <div className="bg-white rounded-[18px] p-4 border border-border-subtle shadow-sm flex flex-col md:flex-row items-stretch md:items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-2.5">
          <span className="border border-border-subtle px-3 py-1.5 rounded-[10px] text-[11px] text-muted bg-surface-note">
            Período: <strong className="text-ink">{periodo}</strong>
          </span>

          <span className="border border-border-subtle px-3 py-1.5 rounded-[10px] text-[11px] text-muted bg-surface-note">
            Zona: <strong className="text-ink">Todas las Zonas (Mendoza)</strong>
          </span>
        </div>

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
                Facturación acumulada: <strong>${totalFacturadoVentas.toLocaleString('es-AR')}</strong>
              </p>
            </div>
            <span className="text-[11px] text-muted">
              Registros: <strong>{ventasRows.length}</strong>
            </span>
          </div>

          <div className="overflow-x-auto">
            {loading ? (
              <div className="py-10 text-center text-[12px] text-muted">
                Cargando reporte de ventas...
              </div>
            ) : ventasRows.length === 0 ? (
              <div className="py-10 text-center text-[13px] text-muted">
                No hay ventas registradas para mostrar en este reporte.
              </div>
            ) : (
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                    <th className="pb-3 px-3">Fecha</th>
                    <th className="pb-3 px-3">Pedido</th>
                    <th className="pb-3 px-3">Comercio</th>
                    <th className="pb-3 px-3">Departamento</th>
                    <th className="pb-3 px-3">Preventor</th>
                    <th className="pb-3 px-3">Estado</th>
                    <th className="pb-3 px-3 text-right">Total Venta</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-divider text-[12px]">
                  {ventasRows.map((row, idx) => (
                    <tr key={`${row.numeroPedido}-${idx}`} className="hover:bg-slate-50/70">
                      <td className="py-3 px-3 font-semibold text-ink">{row.fecha}</td>
                      <td className="py-3 px-3 text-secondary font-bold">#{row.numeroPedido}</td>
                      <td className="py-3 px-3 font-medium text-ink">{row.comercio}</td>
                      <td className="py-3 px-3 text-muted">{row.departamento}</td>
                      <td className="py-3 px-3 text-muted">{row.preventor}</td>
                      <td className="py-3 px-3">
                        <span className="text-[10px] font-semibold text-secondary bg-info-bg px-2.5 py-0.5 rounded-full">
                          {row.estado}
                        </span>
                      </td>
                      <td className="py-3 px-3 text-right font-bold text-ink">
                        ${Number(row.total || 0).toLocaleString('es-AR')}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
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
                Medición de pedidos gestionados, entregas concretadas y volumen facturado
              </p>
            </div>
            <span className="text-[11px] text-muted">
              Total preventores: <strong>{preventoresRows.length}</strong>
            </span>
          </div>

          <div className="overflow-x-auto">
            {loading ? (
              <div className="py-10 text-center text-[12px] text-muted">
                Cargando rendimiento de preventores...
              </div>
            ) : preventoresRows.length === 0 ? (
              <div className="py-10 text-center text-[13px] text-muted">
                No hay preventores ni actividad registrada para mostrar en este reporte.
              </div>
            ) : (
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                    <th className="pb-3 px-3">Posición</th>
                    <th className="pb-3 px-3">Legajo</th>
                    <th className="pb-3 px-3">Preventor</th>
                    <th className="pb-3 px-3">Sucursal</th>
                    <th className="pb-3 px-3">Pedidos Gestionados</th>
                    <th className="pb-3 px-3">Pedidos Entregados</th>
                    <th className="pb-3 px-3 text-right">Ventas Totales</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-divider text-[12px]">
                  {preventoresRows.map((r, idx) => (
                    <tr key={`${r.legajo}-${idx}`} className="hover:bg-slate-50/70">
                      <td className="py-3 px-3">
                        <span className="w-6 h-6 rounded-full bg-primary-light text-primary font-bold text-[11px] flex items-center justify-center">
                          #{idx + 1}
                        </span>
                      </td>
                      <td className="py-3 px-3 text-muted font-mono">{r.legajo}</td>
                      <td className="py-3 px-3 font-semibold text-ink">{r.preventor}</td>
                      <td className="py-3 px-3 text-muted">{r.sucursal}</td>
                      <td className="py-3 px-3 text-muted">{r.cantidadPedidos || 0} pedidos</td>
                      <td className="py-3 px-3 text-muted">{r.pedidosEntregados || 0} entregados</td>
                      <td className="py-3 px-3 text-right font-bold text-ink">
                        ${Number(r.montoTotal || 0).toLocaleString('es-AR')}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
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
            <span className="text-[11px] text-muted">
              Productos con ventas: <strong>{golosinasRows.length}</strong>
            </span>
          </div>

          <div className="overflow-x-auto">
            {loading ? (
              <div className="py-10 text-center text-[12px] text-muted">
                Cargando ranking de productos más vendidos...
              </div>
            ) : golosinasRows.length === 0 ? (
              <div className="py-10 text-center text-[13px] text-muted">
                No hay ventas de productos registradas para mostrar en este reporte.
              </div>
            ) : (
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                    <th className="pb-3 px-3">Top</th>
                    <th className="pb-3 px-3">Código</th>
                    <th className="pb-3 px-3">Producto</th>
                    <th className="pb-3 px-3">Presentación</th>
                    <th className="pb-3 px-3">Unidades Vendidas</th>
                    <th className="pb-3 px-3 text-right">Total Facturado</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-divider text-[12px]">
                  {golosinasRows.map((c, idx) => (
                    <tr key={`${c.codigo}-${idx}`} className="hover:bg-slate-50/70">
                      <td className="py-3 px-3 font-bold text-primary">#{idx + 1}</td>
                      <td className="py-3 px-3 font-mono text-muted">{c.codigo}</td>
                      <td className="py-3 px-3 font-semibold text-ink">{c.producto}</td>
                      <td className="py-3 px-3 text-muted">{c.presentacion}</td>
                      <td className="py-3 px-3 font-bold text-ink">
                        {Number(c.unidadesVendidas || 0).toLocaleString('es-AR')} u.
                      </td>
                      <td className="py-3 px-3 text-right font-semibold text-ink">
                        ${Number(c.montoTotal || 0).toLocaleString('es-AR')}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
