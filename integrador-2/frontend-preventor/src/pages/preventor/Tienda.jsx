/**
 * Vista de Catálogo de Productos y Golosinas (Tienda) para Preventores.
 * Carga productos y categorías en tiempo real desde el backend Spring Boot.
 * Permite buscar, filtrar y agregar directamente al CartContext.
 */
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Icon from '../../components/Icon';
import Button from '../../components/Button';
import { productoService } from '../../services/productoService';
import { useCart } from '../../context/CartContext';

export default function Tienda() {
  const { totalUnidades, agregarItem, clienteActual } = useCart();
  const [productos, setProductos] = useState([]);
  const [categorias, setCategorias] = useState(['Todos']);
  const [searchTerm, setSearchTerm] = useState('');
  const [category, setCategory] = useState('Todos');
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState(null);

  // Carga inicial de datos desde el backend
  useEffect(() => {
    async function cargarDatos() {
      try {
        setCargando(true);
        setError(null);
        const [listaProds, listaCats] = await Promise.all([
          productoService.listarProductos(),
          productoService.listarCategorias().catch(() => []),
        ]);

        // Asegurar formato de arreglo
        const prods = Array.isArray(listaProds) ? listaProds : (listaProds?.content || []);
        setProductos(prods);

        if (Array.isArray(listaCats) && listaCats.length > 0) {
          const nombresCats = listaCats.map((c) => c.nombre || c.denominacion).filter(Boolean);
          setCategorias(['Todos', ...nombresCats]);
        }
      } catch (err) {
        console.error('Error al cargar catálogo de productos:', err);
        setError(
          err.code === 'CAMBIO_CLAVE_REQUERIDO' || err.status === 403
            ? 'Cambie su clave para acceder'
            : 'No se pudo cargar el catálogo de productos. Verificá la conexión con el servidor.'
        );
      } finally {
        setCargando(false);
      }
    }

    cargarDatos();
  }, []);

  // Manejo de notificación flotante al agregar al carrito
  const handleAgregar = (item) => {
    agregarItem(item, 1);
    setToast(`Agregado: ${item.nombre}`);
    setTimeout(() => setToast(null), 2000);
  };

  // Filtrado dinámico por texto y categoría seleccionada
  const filtered = productos.filter((p) => {
    const pCat = p.categoria?.nombre || p.categoria || '';
    const matchesCat = category === 'Todos' || pCat.toLowerCase() === category.toLowerCase();
    const term = searchTerm.toLowerCase();
    const matchesSearch =
      (p.nombre && p.nombre.toLowerCase().includes(term)) ||
      (p.descripcion && p.descripcion.toLowerCase().includes(term)) ||
      (p.codigo && p.codigo.toLowerCase().includes(term));
    return matchesCat && matchesSearch;
  });

  return (
    <div className="space-y-6">
      {/* Toast Notificación */}
      {toast && (
        <div className="fixed bottom-20 right-6 z-50 bg-[#2b2b3a] text-white text-[12px] px-4 py-2.5 rounded-full shadow-lg animate-bounce">
          ✓ {toast}
        </div>
      )}

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
            {clienteActual ? (
              <span>Tomando pedido para: <strong className="text-[#2b2b3a]">{clienteActual.nombreComercio}</strong></span>
            ) : (
              <span>Seleccioná golosinas y confitería para armar el pedido del cliente.</span>
            )}
          </p>
        </div>
        <Link to="/carrito">
          <Button className="text-[12px]">
            <Icon name="bag" className="w-4 h-4" />
            <span>Ver Carrito ({totalUnidades} {totalUnidades === 1 ? 'ítem' : 'ítems'})</span>
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
            placeholder="Buscar golosinas, códigos o descripciones..."
            className="w-full pl-10 pr-4 py-2.5 rounded-[12px] border border-[#ececf2] bg-[#f8f9fc] text-[12px] text-[#2b2b3a] focus:bg-white focus:outline-none focus:border-[#e86bc4]"
          />
        </div>

        <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
          {categorias.map((c) => (
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

      {/* Estado de Carga o Error */}
      {cargando && (
        <div className="flex justify-center items-center py-12">
          <div className="w-8 h-8 border-3 border-[#e86bc4] border-t-transparent rounded-full animate-spin"></div>
        </div>
      )}

      {error && (
        error === 'Cambie su clave para acceder' ? (
          <div className="max-w-md mx-auto my-8 p-8 bg-white rounded-[24px] border border-[#ececf2] text-center space-y-4 shadow-sm">
            <div className="w-16 h-16 rounded-full bg-[#eefaff] text-[#247f99] flex items-center justify-center mx-auto text-3xl font-bold">
              🔒
            </div>
            <h2 className="text-xl font-bold text-[#2b2b3a]">Cambie su clave para acceder</h2>
            <p className="text-[13px] text-[#6b6b80] leading-relaxed">
              Su usuario posee una contraseña temporal por defecto. Por razones de seguridad, debe actualizar su clave antes de operar en esta sección.
            </p>
            <Link
              to="/cambiar-password"
              className="inline-flex items-center justify-center w-full py-3 px-4 rounded-[14px] bg-[#247f99] text-white font-bold text-[13px] hover:bg-[#1c667b] transition-colors shadow-sm"
            >
              Cambiar contraseña ahora →
            </Link>
          </div>
        ) : (
          <div className="p-4 bg-[#ffe8ed] border border-[#ffccd5] rounded-[16px] text-[12px] text-[#e5484d]">
            {error}
          </div>
        )
      )}

      {/* Grilla de Productos */}
      {!cargando && !error && (
        <>
          {filtered.length === 0 ? (
            <div className="text-center py-12 bg-white rounded-[20px] border border-[#ececf2]">
              <p className="text-[#6b6b80] text-[13px]">No se encontraron productos disponibles.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {filtered.map((item) => {
                const precio = item.precioVigente ?? item.precio ?? 0;
                const formattedPrecio = new Intl.NumberFormat('es-AR', {
                  style: 'currency',
                  currency: 'ARS',
                  maximumFractionDigits: 0,
                }).format(precio);

                return (
                  <article
                    key={item.id}
                    className="border border-[#ececf2] rounded-[18px] p-3 bg-white shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow"
                  >
                    <div>
                      {item.imagenUrl ? (
                        <img
                          src={item.imagenUrl}
                          alt={item.nombre}
                          className="w-full h-32 object-cover rounded-[12px] mb-2.5 bg-slate-50"
                        />
                      ) : (
                        <div className="w-full h-32 rounded-[12px] mb-2.5 bg-gradient-to-br from-[#ffeefb] to-[#edf0f8] flex items-center justify-center text-[#e86bc4]">
                          <Icon name="box" className="w-10 h-10 opacity-40" />
                        </div>
                      )}
                      <span className="text-[10px] font-bold tracking-wider text-[#e86bc4] uppercase block">
                        {item.categoria?.nombre || 'Mayorista'}
                      </span>
                      <h3 className="font-semibold text-[14px] text-[#2b2b3a] leading-tight mt-0.5">
                        {item.nombre}
                      </h3>
                      <p className="text-[11px] text-[#6b6b80] mt-0.5 line-clamp-2">
                        {item.descripcion || item.presentacion || 'Presentación comercial estándar'}
                      </p>
                    </div>

                    <div className="flex items-center justify-between mt-3 pt-2 border-t border-[#f0f0f4]">
                      <strong className="text-[14px] font-bold text-[#2b2b3a]">
                        {formattedPrecio}
                      </strong>
                      <button
                        onClick={() => handleAgregar(item)}
                        className="w-8 h-8 rounded-[10px] bg-[#e86bc4] hover:bg-[#d456b0] text-white text-[18px] flex items-center justify-center cursor-pointer shadow-sm active:scale-95 transition-all"
                        title="Agregar al pedido"
                      >
                        +
                      </button>
                    </div>
                  </article>
                );
              })}
            </div>
          )}
        </>
      )}
    </div>
  );
}
