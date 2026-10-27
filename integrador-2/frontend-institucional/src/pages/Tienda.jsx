import { useState } from 'react';
import ProductCard from '../components/ProductCard';
import Icon from '../components/Icon';
import { productsMock } from '../mocks/products';

export default function Tienda() {
  const [selectedCategory, setSelectedCategory] = useState('Todos');
  const [searchTerm, setSearchTerm] = useState('');

  const categories = ['Todos', 'Alfajores', 'Golosinas', 'Chupetines', 'Chocolates'];

  const filteredProducts = productsMock.filter((p) => {
    const matchesCategory =
      selectedCategory === 'Todos' || p.categoria === selectedCategory;
    const matchesSearch =
      p.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.presentacion.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  return (
    <div className="space-y-8">
      {/* Catalog Header */}
      <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-[0_14px_40px_rgba(56,52,84,0.06)]">
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Catálogo Mayorista
        </span>
        <h1 className="text-3xl font-bold text-[#2b2b3a] mt-1 mb-2">
          Golosinas y Confitería
        </h1>
        <p className="text-[13px] text-[#6b6b80] max-w-2xl mb-6">
          Precios por bulto cerrado y caja exhibidora para kioscos, almacenes, minimarkets y distribuidoras.
        </p>

        {/* Search & Category Filter bar */}
        <div className="flex flex-col md:flex-row gap-4 items-stretch md:items-center justify-between pt-4 border-t border-[#f0f0f4]">
          {/* Search bar */}
          <div className="relative flex-1 max-w-md">
            <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[#aaa9ba]">
              <Icon name="search" className="w-4 h-4" />
            </span>
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Buscar por nombre, presentación o marca..."
              className="w-full pl-10 pr-4 py-2.5 rounded-[12px] border border-[#ececf2] bg-[#f8f9fc] text-[12px] text-[#2b2b3a] focus:bg-white focus:outline-none focus:border-[#e86bc4] transition-colors"
            />
          </div>

          {/* Categories */}
          <div className="flex items-center gap-2 overflow-x-auto pb-1 sm:pb-0 scrollbar-none">
            {categories.map((cat) => (
              <button
                key={cat}
                type="button"
                onClick={() => setSelectedCategory(cat)}
                className={`px-3.5 py-1.5 rounded-full text-[11px] font-semibold whitespace-nowrap transition-all cursor-pointer ${
                  selectedCategory === cat
                    ? 'bg-[#e86bc4] text-white shadow-sm'
                    : 'bg-[#f0f2f7] text-[#6b6b80] hover:bg-[#ffe2f7] hover:text-[#a34187]'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Results grid */}
      <div>
        <div className="flex items-center justify-between mb-4 px-1">
          <span className="text-[12px] text-[#6b6b80]">
            Mostrando <strong>{filteredProducts.length}</strong> productos
          </span>
          <span className="text-[11px] text-[#3fb8d9] font-medium">
            Precios vigentes con IVA incluido
          </span>
        </div>

        {filteredProducts.length === 0 ? (
          <div className="bg-white rounded-[20px] p-12 text-center border border-[#ececf2]">
            <p className="text-[15px] font-semibold text-[#2b2b3a]">
              No encontramos productos para tu búsqueda
            </p>
            <p className="text-[12px] text-[#6b6b80] mt-1">
              Probá con otra palabra clave o limpiá los filtros.
            </p>
            <button
              onClick={() => {
                setSelectedCategory('Todos');
                setSearchTerm('');
              }}
              className="mt-4 text-[12px] text-[#e86bc4] font-semibold hover:underline"
            >
              Restablecer filtros
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-5">
            {filteredProducts.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

