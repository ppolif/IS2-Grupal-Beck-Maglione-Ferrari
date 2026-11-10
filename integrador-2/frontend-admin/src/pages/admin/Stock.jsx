import Kpis from '../../components/Kpis';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import { useStock } from '../../hooks/useStock';

export default function Stock() {
  const {
    stockList,
    loading,
    lowStockCount,
    zeroStockCount,
    filter,
    setFilter,
    isAdjustmentModal,
    setIsAdjustmentModal,
    selectedProduct,
    adjustAmount,
    setAdjustAmount,
    filtered,
    handleOpenAdjust,
    handleSaveAdjust,
  } = useStock();

  return (
    <div className="space-y-6">
      {/* Header and Operational KPIs */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-primary">
          Almacén & Logística
        </span>
        <h2 className="text-xl font-bold text-ink mt-0.5 mb-4">
          Control de Stock e Inventario
        </h2>
        <Kpis warehouse={true} />
      </div>

      {/* Main Stock Module */}
      <div className="bg-white rounded-[20px] p-6 border border-border-subtle shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-3 border-b border-divider">
          <div>
            <h3 className="text-[16px] font-bold text-ink">
              Inventario Físico en Depósito
            </h3>
            <p className="text-[11px] text-muted">
              Sincronizado con el catálogo de productos registrados
            </p>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setFilter('todos')}
              className={`px-3 py-1.5 rounded-full text-[11px] font-semibold transition-colors cursor-pointer ${
                filter === 'todos'
                  ? 'bg-primary text-white shadow-xs'
                  : 'bg-canvas text-muted'
              }`}
            >
              Todos ({stockList.length})
            </button>
            <button
              onClick={() => setFilter('bajo')}
              className={`px-3 py-1.5 rounded-full text-[11px] font-semibold transition-colors cursor-pointer ${
                filter === 'bajo'
                  ? 'bg-brand-warning text-warning-text shadow-xs'
                  : 'bg-canvas text-muted'
              }`}
            >
              Stock Bajo ({lowStockCount})
            </button>
            <button
              onClick={() => setFilter('cero')}
              className={`px-3 py-1.5 rounded-full text-[11px] font-semibold transition-colors cursor-pointer ${
                filter === 'cero'
                  ? 'bg-brand-error text-white shadow-xs'
                  : 'bg-canvas text-muted'
              }`}
            >
              Agotados ({zeroStockCount})
            </button>
          </div>
        </div>

        {/* Stock Table */}
        <div className="overflow-x-auto">
          {loading ? (
            <div className="py-10 text-center text-[12px] text-muted">
              Cargando inventario...
            </div>
          ) : filtered.length === 0 ? (
            <div className="py-10 text-center text-[13px] text-muted">
              {stockList.length === 0
                ? 'No hay productos dados de alta en el inventario aún. Podés registrarlos en la sección Productos y Precios.'
                : 'No hay productos que coincidan con el filtro seleccionado.'}
            </div>
          ) : (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                  <th className="pb-3 px-3">Producto</th>
                  <th className="pb-3 px-3">Categoría</th>
                  <th className="pb-3 px-3">Presentación</th>
                  <th className="pb-3 px-3">Stock Disponible</th>
                  <th className="pb-3 px-3">Estado</th>
                  <th className="pb-3 px-3 text-right">Ajuste</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-divider text-[12px]">
                {filtered.map((r) => (
                  <tr key={r.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="py-3 px-3 font-semibold text-ink">
                      {r.nombre}
                    </td>
                    <td className="py-3 px-3 text-muted">{r.categoria}</td>
                    <td className="py-3 px-3 text-muted">{r.presentacion}</td>
                    <td className="py-3 px-3 font-bold text-ink">
                      {r.stock} u.
                    </td>
                    <td className="py-3 px-3">
                      <span
                        className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full ${
                          r.stock === 0
                            ? 'bg-danger-bg text-danger-hover'
                            : r.stock <= 15
                            ? 'bg-warning-bg text-warning-text'
                            : 'bg-success-bg text-success-icon'
                        }`}
                      >
                        {r.stock === 0
                          ? 'Sin stock'
                          : r.stock <= 15
                          ? 'Stock bajo'
                          : 'Disponible'}
                      </span>
                    </td>
                    <td className="py-3 px-3 text-right">
                      <button
                        onClick={() => handleOpenAdjust(r)}
                        className="text-secondary hover:text-info-text font-semibold text-[11px] hover:underline cursor-pointer"
                      >
                        Ajustar unidades
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Adjust Modal */}
      <Modal
        isOpen={isAdjustmentModal}
        onClose={() => setIsAdjustmentModal(false)}
        tag="ALMACÉN"
        title="Ajuste Físico de Stock"
      >
        {selectedProduct && (
          <form onSubmit={handleSaveAdjust} className="space-y-4">
            <p className="text-[12px] text-muted">
              Ingresá la cantidad real contada en estantería para{' '}
              <strong className="text-ink">{selectedProduct.nombre}</strong>.
            </p>
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Unidades físicas contadas *
              </label>
              <input
                type="number"
                min="0"
                required
                value={adjustAmount}
                onChange={(e) => setAdjustAmount(e.target.value)}
                className="w-full border border-border-subtle rounded-[12px] p-3 text-[13px] text-ink focus:outline-none focus:border-primary"
              />
            </div>
            <div className="pt-2 flex justify-end gap-2.5">
              <Button secondary type="button" onClick={() => setIsAdjustmentModal(false)}>
                Cancelar
              </Button>
              <Button type="submit">Confirmar ajuste</Button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
}
