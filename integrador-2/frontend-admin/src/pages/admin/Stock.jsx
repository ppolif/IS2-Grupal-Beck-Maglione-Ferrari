import { useState } from 'react';
import Kpis from '../../components/Kpis';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { productsStockMock } from '../../mocks/products';

export default function Stock() {
  const [stockList, setStockList] = useState(productsStockMock);
  const [filter, setFilter] = useState('todos');
  const [isAdjustmentModal, setIsAdjustmentModal] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [adjustAmount, setAdjustAmount] = useState('');

  const filtered = stockList.filter((item) => {
    if (filter === 'bajo') return item.stock > 0 && item.stock <= 15;
    if (filter === 'cero') return item.stock === 0;
    return true;
  });

  const handleOpenAdjust = (p) => {
    setSelectedProduct(p);
    setAdjustAmount(p.stock.toString());
    setIsAdjustmentModal(true);
  };

  const handleSaveAdjust = (e) => {
    e.preventDefault();
    if (selectedProduct) {
      setStockList((prev) =>
        prev.map((item) =>
          item.id === selectedProduct.id
            ? { ...item, stock: parseInt(adjustAmount) || 0 }
            : item
        )
      );
      alert(`Stock de "${selectedProduct.nombre}" ajustado a ${adjustAmount} unidades.`);
      setIsAdjustmentModal(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header and Operational KPIs */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Almacén & Logística
        </span>
        <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5 mb-4">
          Control de Stock e Inventario
        </h2>
        {/* Kpis matching warehouse style from prototype */}
        <Kpis warehouse={true} />
      </div>

      {/* Main Stock Module matching prototype module */}
      <div className="bg-white rounded-[20px] p-6 border border-[#ececf2] shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-3 border-b border-[#f0f0f4]">
          <div>
            <h3 className="text-[16px] font-bold text-[#2b2b3a]">
              Inventario Físico en Depósito
            </h3>
            <p className="text-[11px] text-[#6b6b80]">
              Actualizado hace 5 minutos · Sincronizado con preventa
            </p>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setFilter('todos')}
              className={`px-3 py-1.5 rounded-full text-[11px] font-semibold transition-colors cursor-pointer ${
                filter === 'todos'
                  ? 'bg-[#e86bc4] text-white shadow-xs'
                  : 'bg-[#f0f2f7] text-[#6b6b80]'
              }`}
            >
              Todos ({stockList.length})
            </button>
            <button
              onClick={() => setFilter('bajo')}
              className={`px-3 py-1.5 rounded-full text-[11px] font-semibold transition-colors cursor-pointer ${
                filter === 'bajo'
                  ? 'bg-[#ffd98a] text-[#7d5707] shadow-xs'
                  : 'bg-[#f0f2f7] text-[#6b6b80]'
              }`}
            >
              Stock Bajo (2)
            </button>
            <button
              onClick={() => setFilter('cero')}
              className={`px-3 py-1.5 rounded-full text-[11px] font-semibold transition-colors cursor-pointer ${
                filter === 'cero'
                  ? 'bg-[#ff8fa3] text-white shadow-xs'
                  : 'bg-[#f0f2f7] text-[#6b6b80]'
              }`}
            >
              Agotados (1)
            </button>
          </div>
        </div>

        {/* Stock Table */}
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-[#ececf2] text-[10px] uppercase font-bold text-[#aaa9b8] tracking-wider">
                <th className="pb-3 px-3">Producto</th>
                <th className="pb-3 px-3">Categoría</th>
                <th className="pb-3 px-3">Presentación</th>
                <th className="pb-3 px-3">Stock Disponible</th>
                <th className="pb-3 px-3">Estado</th>
                <th className="pb-3 px-3 text-right">Ajuste</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#f0f0f4] text-[12px]">
              {filtered.map((r) => (
                <tr key={r.id} className="hover:bg-slate-50/70 transition-colors">
                  <td className="py-3 px-3 font-semibold text-[#2b2b3a]">
                    {r.nombre}
                  </td>
                  <td className="py-3 px-3 text-[#6b6b80]">{r.categoria}</td>
                  <td className="py-3 px-3 text-[#6b6b80]">{r.presentacion}</td>
                  <td className="py-3 px-3 font-bold text-[#2b2b3a]">
                    {r.stock} u.
                  </td>
                  <td className="py-3 px-3">
                    <span
                      className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full ${
                        r.stock === 0
                          ? 'bg-[#ffe2e5] text-[#ff8fa3]'
                          : r.stock <= 15
                          ? 'bg-[#fff2d3] text-[#986b16]'
                          : 'bg-[#dff8e9] text-[#39855b]'
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
                      className="text-[#3fb8d9] hover:text-[#247f99] font-semibold text-[11px] hover:underline cursor-pointer"
                    >
                      Ajustar unidades
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
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
            <p className="text-[12px] text-[#6b6b80]">
              Ingresá la cantidad real contada en estantería para{' '}
              <strong className="text-[#2b2b3a]">{selectedProduct.nombre}</strong>.
            </p>
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Unidades físicas contadas *
              </label>
              <input
                type="number"
                required
                value={adjustAmount}
                onChange={(e) => setAdjustAmount(e.target.value)}
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[13px] text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4]"
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

