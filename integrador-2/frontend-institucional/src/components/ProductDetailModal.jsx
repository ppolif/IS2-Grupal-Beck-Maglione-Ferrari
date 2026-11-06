import { useState } from 'react';
import Button from './Button';
import Icon from './Icon';

export default function ProductDetailModal({ product, onClose, onAddToCart }) {
  const [bultosCount, setBultosCount] = useState(1);

  if (!product) return null;

  const precioNum = product.precioNum || parseInt(product.precio?.replace(/[^0-9]/g, '')) || 18900;
  const unidades = product.unidades || 24;
  const precioPorUnidad = Math.round(precioNum / unidades);
  const subtotal = precioNum * bultosCount;

  const handleAdd = () => {
    if (onAddToCart) {
      onAddToCart({ ...product, cantidadSeleccionada: bultosCount });
    }
    alert(`Se agregaron ${bultosCount} bultos de "${product.nombre}" al carrito.`);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in duration-150">
      <div className="bg-white rounded-[24px] max-w-lg w-full overflow-hidden shadow-2xl border border-[#ececf2] flex flex-col max-h-[90vh]">
        {/* Header con imagen destacada */}
        <div className="relative h-60 bg-slate-50">
          <img
            src={product.imagen}
            alt={product.nombre}
            className="w-full h-full object-cover"
          />
          <button
            type="button"
            onClick={onClose}
            className="absolute top-3 right-3 w-8 h-8 rounded-full bg-black/40 hover:bg-black/60 text-white flex items-center justify-center text-sm transition-colors cursor-pointer"
          >
            ✕
          </button>
          <div className="absolute bottom-3 left-3 flex gap-2">
            <span className="bg-white/95 backdrop-blur-sm text-[10px] font-bold text-[#e86bc4] px-3 py-1 rounded-full shadow-sm">
              {product.categoria || 'Golosinas'}
            </span>
            <span className="bg-[#dff8e9] text-[10px] font-bold text-[#39855b] px-3 py-1 rounded-full shadow-sm flex items-center gap-1">
              <span className="w-1.5 h-1.5 rounded-full bg-[#39855b]" />
              Stock disponible: 180 bultos
            </span>
          </div>
        </div>

        {/* Contenido / Especificaciones */}
        <div className="p-6 space-y-4 overflow-y-auto">
          <div>
            <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
              Ficha Técnica Mayorista
            </span>
            <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
              {product.nombre}
            </h2>
            <p className="text-[12px] text-[#6b6b80] mt-1">
              Presentación para venta en mostrador y exhibidores. Apto para venta individual en kioscos y minimarkets.
            </p>
          </div>

          <div className="grid grid-cols-2 gap-3 text-[12px] p-3 rounded-[14px] bg-[#fafafd] border border-[#ececf2]">
            <div>
              <span className="text-[#aaa9ba] text-[10px] uppercase font-bold block">Presentación</span>
              <strong className="text-[#2b2b3a]">{product.presentacion || 'Caja x 24 u.'}</strong>
            </div>
            <div>
              <span className="text-[#aaa9ba] text-[10px] uppercase font-bold block">Costo unitario aprox.</span>
              <strong className="text-[#3fb8d9]">${precioPorUnidad.toLocaleString('es-AR')} / u.</strong>
            </div>
            <div>
              <span className="text-[#aaa9ba] text-[10px] uppercase font-bold block">Vigencia de Precio</span>
              <span className="text-[#2b2b3a] font-medium">Actualizado hoy</span>
            </div>
            <div>
              <span className="text-[#aaa9ba] text-[10px] uppercase font-bold block">Origen</span>
              <span className="text-[#2b2b3a] font-medium">Mendoza, Argentina</span>
            </div>
          </div>

          {/* Selector de cantidad y precio */}
          <div className="pt-2 border-t border-[#f0f0f4] flex items-center justify-between">
            <div>
              <span className="text-[10px] text-[#aaa9ba] uppercase font-bold block">Cantidad de bultos:</span>
              <div className="flex items-center gap-2 mt-1">
                <button
                  type="button"
                  onClick={() => setBultosCount(Math.max(1, bultosCount - 1))}
                  className="w-8 h-8 rounded-[10px] border border-[#ececf2] bg-white text-[#2b2b3a] font-bold text-sm hover:bg-slate-50 transition-colors cursor-pointer"
                >
                  -
                </button>
                <span className="font-bold text-[14px] w-8 text-center text-[#2b2b3a]">
                  {bultosCount}
                </span>
                <button
                  type="button"
                  onClick={() => setBultosCount(bultosCount + 1)}
                  className="w-8 h-8 rounded-[10px] border border-[#ececf2] bg-white text-[#2b2b3a] font-bold text-sm hover:bg-slate-50 transition-colors cursor-pointer"
                >
                  +
                </button>
              </div>
            </div>

            <div className="text-right">
              <span className="text-[10px] text-[#aaa9ba] uppercase font-bold block">Subtotal estimado:</span>
              <strong className="text-2xl font-bold text-[#e86bc4]">
                ${subtotal.toLocaleString('es-AR')}
              </strong>
            </div>
          </div>
        </div>

        {/* Footer con botón de compra */}
        <div className="p-4 bg-slate-50 border-t border-[#ececf2] flex items-center gap-3">
          <Button secondary onClick={onClose} className="w-1/3 text-[12px] py-2.5">
            Cerrar
          </Button>
          <Button onClick={handleAdd} className="w-2/3 text-[12px] py-2.5">
            <Icon name="bag" className="w-4 h-4" />
            <span>Agregar al Carrito</span>
          </Button>
        </div>
      </div>
    </div>
  );
}
