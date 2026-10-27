import { useState } from 'react';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { productsStockMock } from '../../mocks/products';

export default function Productos() {
  const [products, setProducts] = useState(productsStockMock);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  const [formData, setFormData] = useState({
    nombre: '',
    categoria: 'Alfajores',
    presentacion: '',
    precioMayorista: '',
    stockInicial: '',
    descripcion: ''
  });

  const handleOpenCreate = () => {
    setEditingProduct(null);
    setFormData({
      nombre: '',
      categoria: 'Alfajores',
      presentacion: '',
      precioMayorista: '',
      stockInicial: '',
      descripcion: ''
    });
    setIsModalOpen(true);
  };

  const handleOpenEdit = (product) => {
    setEditingProduct(product);
    setFormData({
      nombre: product.nombre,
      categoria: product.categoria,
      presentacion: product.presentacion,
      precioMayorista: product.precioMayorista,
      stockInicial: product.stock.toString(),
      descripcion: `Presentación comercial de ${product.nombre}`
    });
    setIsModalOpen(true);
  };

  const handleSave = (e) => {
    e.preventDefault();
    if (editingProduct) {
      setProducts((prev) =>
        prev.map((p) =>
          p.id === editingProduct.id
            ? {
                ...p,
                nombre: formData.nombre,
                categoria: formData.categoria,
                presentacion: formData.presentacion,
                precioMayorista: formData.precioMayorista,
                stock: parseInt(formData.stockInicial) || p.stock
              }
            : p
        )
      );
      alert('Producto modificado con éxito');
    } else {
      const newP = {
        id: Date.now(),
        nombre: formData.nombre,
        categoria: formData.categoria,
        presentacion: formData.presentacion,
        precioMayorista: formData.precioMayorista,
        precioNum: 10000,
        stock: parseInt(formData.stockInicial) || 0,
        stockMinimo: 15,
        estado: 'Normal'
      };
      setProducts([newP, ...products]);
      alert('Nuevo producto guardado en catálogo');
    }
    setIsModalOpen(false);
  };

  const filtered = products.filter((p) =>
    p.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.categoria.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Catálogo
          </span>
          <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
            Gestión de Productos
          </h2>
          <p className="text-[12px] text-[#6b6b80] mt-0.5">
            Alta, modificación de precios mayoristas y datos de presentación comercial.
          </p>
        </div>

        <Button onClick={handleOpenCreate}>
          <Icon name="plus" className="w-4 h-4" />
          <span>Nuevo Producto</span>
        </Button>
      </div>

      {/* Search and stats bar */}
      <div className="bg-white rounded-[20px] p-4 border border-[#ececf2] shadow-sm flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
        <div className="relative flex-1 max-w-sm">
          <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[#aaa9ba]">
            <Icon name="search" className="w-4 h-4" />
          </span>
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Buscar por producto o categoría..."
            className="w-full pl-10 pr-4 py-2 rounded-[12px] border border-[#ececf2] bg-[#f8f9fc] text-[12px] text-[#2b2b3a] focus:bg-white focus:outline-none focus:border-[#e86bc4]"
          />
        </div>
        <div className="text-[12px] text-[#6b6b80] self-end sm:self-auto">
          Total: <strong>{products.length}</strong> artículos registrados
        </div>
      </div>

      {/* Products Table (Responsive: Cards on mobile, Table on md+) */}
      <div className="bg-white rounded-[20px] p-5 border border-[#ececf2] shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-[#ececf2] text-[10px] uppercase font-bold text-[#aaa9b8] tracking-wider">
                <th className="pb-3 px-3">Producto</th>
                <th className="pb-3 px-3">Categoría</th>
                <th className="pb-3 px-3">Presentación</th>
                <th className="pb-3 px-3">Precio Mayorista</th>
                <th className="pb-3 px-3">Stock Actual</th>
                <th className="pb-3 px-3 text-right">Acción</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#f0f0f4] text-[12px]">
              {filtered.map((p) => (
                <tr key={p.id} className="hover:bg-slate-50/70 transition-colors">
                  <td className="py-3.5 px-3 font-semibold text-[#2b2b3a]">
                    {p.nombre}
                  </td>
                  <td className="py-3.5 px-3 text-[#6b6b80]">
                    <span className="bg-[#f0f2f7] text-[#555] px-2.5 py-0.5 rounded-full text-[10px] font-medium">
                      {p.categoria}
                    </span>
                  </td>
                  <td className="py-3.5 px-3 text-[#6b6b80]">{p.presentacion}</td>
                  <td className="py-3.5 px-3 font-bold text-[#2b2b3a]">
                    {p.precioMayorista}
                  </td>
                  <td className="py-3.5 px-3">
                    <span
                      className={`text-[11px] font-bold px-2 py-0.5 rounded-full ${
                        p.stock === 0
                          ? 'bg-[#ffe2e5] text-[#ff8fa3]'
                          : p.stock < 15
                          ? 'bg-[#fff2d3] text-[#986b16]'
                          : 'bg-[#dff8e9] text-[#39855b]'
                      }`}
                    >
                      {p.stock} u.
                    </span>
                  </td>
                  <td className="py-3.5 px-3 text-right">
                    <button
                      onClick={() => handleOpenEdit(p)}
                      className="text-[#3fb8d9] hover:text-[#247f99] font-semibold text-[11px] hover:underline cursor-pointer"
                    >
                      Editar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal Alta / Edición matching prototype form-card */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        tag="CATÁLOGO"
        title={editingProduct ? 'Modificar Producto' : 'Alta de Producto'}
        maxWidth="max-w-xl"
      >
        <p className="text-[11px] text-[#6b6b80] mb-4">
          Completá la información comercial, presentación y stock para la tienda.
        </p>

        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Nombre del producto *
              </label>
              <input
                type="text"
                required
                value={formData.nombre}
                onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                placeholder="Ej. Alfajor triple chocolate"
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Categoría *
              </label>
              <select
                value={formData.categoria}
                onChange={(e) => setFormData({ ...formData, categoria: e.target.value })}
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              >
                <option>Alfajores</option>
                <option>Golosinas</option>
                <option>Chupetines</option>
                <option>Chocolates</option>
                <option>Galletitas</option>
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Presentación comercial *
              </label>
              <input
                type="text"
                required
                value={formData.presentacion}
                onChange={(e) => setFormData({ ...formData, presentacion: e.target.value })}
                placeholder="Ej. Caja x 24 u. / Display x 50 u."
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Precio mayorista *
              </label>
              <input
                type="text"
                required
                value={formData.precioMayorista}
                onChange={(e) => setFormData({ ...formData, precioMayorista: e.target.value })}
                placeholder="Ej. $18.900"
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Stock inicial / actual *
              </label>
              <input
                type="number"
                required
                value={formData.stockInicial}
                onChange={(e) => setFormData({ ...formData, stockInicial: e.target.value })}
                placeholder="100"
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Descripción breve
              </label>
              <input
                type="text"
                value={formData.descripcion}
                onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
                placeholder="Detalle de sabor o características"
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
          </div>

          {/* Prototype upload area */}
          <div className="border-2 border-dashed border-[#ececf2] rounded-[16px] h-24 flex flex-col items-center justify-center text-[#6b6b80] text-[11px] hover:border-[#3fb8d9] transition-colors cursor-pointer bg-[#fafafd]">
            <span className="text-[#3fb8d9] font-bold text-[13px]">＋ Cargar imagen del producto</span>
            <span className="text-[10px] text-[#aaa9ba]">PNG o JPG hasta 5 MB</span>
          </div>

          <div className="pt-3 flex justify-end gap-2.5">
            <Button secondary type="button" onClick={() => setIsModalOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit">
              {editingProduct ? 'Actualizar producto' : 'Guardar producto'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}

