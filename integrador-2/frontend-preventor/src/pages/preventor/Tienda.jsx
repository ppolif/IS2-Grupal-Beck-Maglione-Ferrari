import { useState } from 'react';
import { Link } from 'react-router-dom';
import Icon from '../../components/Icon';
import Button from '../../components/Button';
import { productsMock } from '../../mocks/products';

export default function Tienda() {
  const [searchTerm, setSearchTerm] = useState('');
  const [category, setCategory] = useState('Todos');

  const categories = ['Todos', 'Alfajores', 'Golosinas', 'Chupetines', 'Chocolates'];

  const filtered = productsMock.filter((p) => {
    const matchesCat = category === 'Todos' || p.categoria === category;
    const matchesSearch =
      p.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.presentacion.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesCat && matchesSearch;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Preventa en Local
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a]">
            Catálogo de Productos
          </h1>
          <p className="text-[12px] text-[#6b6b80] mt-0.5">
            Seleccioná golosinas y confitería para armar el pedido del cliente actual.
          </p>
        </div>
        <Link to="/carrito">
          <Button className="text-[12px]">
            <Icon name="bag" className="w-4 h-4" />
            <span>Ver Carrito (2 items)</span>
          </Button>
        </Link>
      </div>

      {/* Search & Categories */}
      <div className="bg-white rounded-[20px] p-4 border border-[#ececf2] shadow-sm space-y-3">
        <div className="relative">
          <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[#aaa9ba]">
            <Icon name="search" className="w-4 h-4" />
          </span>
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Buscar golosinas para el cliente..."
            className="w-full pl-10 pr-4 py-2.5 rounded-[12px] border border-[#ececf2] bg-[#f8f9fc] text-[12px] text-[#2b2b3a] focus:bg-white focus:outline-none focus:border-[#e86bc4]"
          />
        </div>

        <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
          {categories.map((c) => (
            <button
              key={c}
              onClick={() => setCategory(c)}
              className={`px-3 py-1 rounded-full text-[11px] font-semibold whitespace-nowrap transition-colors cursor-pointer ${
                category === c
                  ? 'bg-[#e86bc4] text-white shadow-sm'
                  : 'bg-[#f0f2f7] text-[#6b6b80] hover:bg-[#ffe2f7]'
              }`}
            >
              {c}
            </button>
          ))}
        </div>
      </div>

      {/* Products Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        {filtered.map((item) => (
          <article
            key={item.id}
            className="border border-[#ececf2] rounded-[18px] p-3 bg-white shadow-sm flex flex-col justify-between"
          >
            <div>
              <img
                src={item.imagen}
                alt={item.nombre}
                className="w-full h-32 object-cover rounded-[12px] mb-2.5"
              />
              <span className="text-[10px] font-bold tracking-wider text-[#e86bc4] uppercase block">
                Precio Mayorista
              </span>
              <h3 className="font-semibold text-[14px] text-[#2b2b3a] leading-tight mt-0.5">
                {item.nombre}
              </h3>
              <p className="text-[11px] text-[#6b6b80] mt-0.5">{item.presentacion}</p>
            </div>

            <div className="flex items-center justify-between mt-3 pt-2 border-t border-[#f0f0f4]">
              <strong className="text-[14px] font-bold text-[#2b2b3a]">
                {item.precio}
              </strong>
              <button
                onClick={() => alert(`Agregado al pedido: ${item.nombre}`)}
                className="w-8 h-8 rounded-[10px] bg-[#e86bc4] hover:bg-[#d456b0] text-white text-[18px] flex items-center justify-center cursor-pointer shadow-sm active:scale-95"
                title="Agregar al pedido"
              >
                +
              </button>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}

