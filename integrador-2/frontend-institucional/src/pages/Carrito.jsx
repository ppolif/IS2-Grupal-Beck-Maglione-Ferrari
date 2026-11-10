import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../components/Button';
import Icon from '../components/Icon';
import { apiFetch } from '../services/api';
import { cartStorage } from '../utils/cartStorage';

export default function Carrito() {
  const navigate = useNavigate();
  const [items, setItems] = useState(() => cartStorage.getItems());
  const [promoCodeInput, setPromoCodeInput] = useState('');
  const [appliedPromo, setAppliedPromo] = useState(() => cartStorage.getPromo());
  const [promoError, setPromoError] = useState('');

  useEffect(() => {
    const sync = () => {
      setItems(cartStorage.getItems());
      setAppliedPromo(cartStorage.getPromo());
    };
    window.addEventListener('dm_cart_updated', sync);
    return () => window.removeEventListener('dm_cart_updated', sync);
  }, []);

  const updateQuantity = (index, delta) => {
    const updated = items.map((item, i) => {
      if (i === index) {
        const newQ = Math.max(1, item.cantidad + delta);
        return { ...item, cantidad: newQ };
      }
      return item;
    });
    setItems(updated);
    cartStorage.setItems(updated);
  };

  const removeItem = (index) => {
    const updated = items.filter((_, i) => i !== index);
    setItems(updated);
    cartStorage.setItems(updated);
  };

  const handleApplyPromo = async (e) => {
    e.preventDefault();
    const cleanCode = promoCodeInput.trim().toUpperCase();
    if (!cleanCode) return;

    try {
      const promo = await apiFetch(`/promociones/codigo/${encodeURIComponent(cleanCode)}`, {
        method: 'GET',
      });
      if (promo && promo.porcentajeDescuento) {
        const promoObj = {
          code: promo.codigo,
          descripcion: promo.descripcion || `${promo.porcentajeDescuento}% de descuento`,
          pct: Number(promo.porcentajeDescuento) / 100,
        };
        setAppliedPromo(promoObj);
        cartStorage.setPromo(promoObj);
        setPromoError('');
        setPromoCodeInput('');
      } else {
        setPromoError('El código ingresado no existe o ha expirado.');
      }
    } catch {
      setPromoError('El código ingresado no existe, ha expirado o requiere iniciar sesión.');
    }
  };

  const handleRemovePromo = () => {
    setAppliedPromo(null);
    cartStorage.setPromo(null);
    setPromoError('');
  };

  const subtotal = items.reduce(
    (acc, it) => acc + Number(it.producto.precioNum || 0) * Number(it.cantidad || 1),
    0
  );

  let descuento = 0;
  if (appliedPromo) {
    if (appliedPromo.pct) {
      descuento = Math.round(subtotal * appliedPromo.pct);
    } else if (appliedPromo.fijo) {
      descuento = Math.min(subtotal, appliedPromo.fijo);
    }
  }

  const total = Math.max(0, subtotal - descuento);

  return (
    <div className="space-y-8">
      {/* Breadcrumb / Title */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Pedido Mayorista
        </span>
        <h1 className="text-3xl font-bold text-[#2b2b3a] mt-1">
          Tu Carrito de Compras
        </h1>
        <p className="text-[13px] text-[#6b6b80] mt-0.5">
          Revisá los bultos seleccionados para tu comercio antes de confirmar la solicitud.
        </p>
      </div>

      {items.length === 0 ? (
        <div className="bg-white rounded-[22px] p-12 text-center border border-[#ececf2]">
          <div className="w-16 h-16 rounded-full bg-[#ffe2f7] text-[#e86bc4] flex items-center justify-center mx-auto mb-4">
            <Icon name="bag" className="w-8 h-8" />
          </div>
          <h2 className="text-xl font-bold text-[#2b2b3a]">El carrito está vacío</h2>
          <p className="text-[13px] text-[#6b6b80] mt-1 mb-6">
            Sumá productos mayoristas con stock desde el catálogo para generar tu pedido.
          </p>
          <Link to="/tienda">
            <Button>Ir a la tienda</Button>
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          {/* Cart items list */}
          <div className="lg:col-span-8 space-y-4">
            <div className="bg-white rounded-[22px] p-6 border border-[#ececf2] shadow-sm">
              <h2 className="text-[16px] font-bold text-[#2b2b3a] mb-4 pb-3 border-b border-[#f0f0f4]">
                Productos en el pedido ({items.length})
              </h2>

              <div className="divide-y divide-[#f0f0f4]">
                {items.map((item, idx) => (
                  <div
                    key={item.producto.id}
                    className="py-4 first:pt-0 last:pb-0 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4"
                  >
                    <div className="flex items-center gap-4">
                      <img
                        src={item.producto.imagen}
                        alt={item.producto.nombre}
                        className="w-16 h-16 rounded-[12px] object-cover border border-[#ececf2] shrink-0"
                      />
                      <div>
                        <h3 className="font-semibold text-[14px] text-[#2b2b3a]">
                          {item.producto.nombre}
                        </h3>
                        <p className="text-[11px] text-[#6b6b80]">
                          {item.producto.presentacion} · {item.producto.categoria}
                        </p>
                        <span className="text-[12px] font-bold text-[#e86bc4] mt-0.5 block">
                          {item.producto.precio} / unidad
                        </span>
                      </div>
                    </div>

                    {/* Quantity controls & Line total */}
                    <div className="flex items-center justify-between sm:justify-end gap-6 w-full sm:w-auto pt-2 sm:pt-0">
                      <div className="flex items-center border border-[#ececf2] rounded-[10px] bg-[#f8f9fc]">
                        <button
                          type="button"
                          onClick={() => updateQuantity(idx, -1)}
                          className="px-3 py-1.5 text-[14px] font-bold text-[#6b6b80] hover:text-[#e86bc4] transition-colors"
                        >
                          −
                        </button>
                        <span className="px-3 py-1.5 text-[12px] font-bold text-[#2b2b3a]">
                          {item.cantidad}
                        </span>
                        <button
                          type="button"
                          onClick={() => updateQuantity(idx, 1)}
                          className="px-3 py-1.5 text-[14px] font-bold text-[#6b6b80] hover:text-[#e86bc4] transition-colors"
                        >
                          +
                        </button>
                      </div>

                      <div className="text-right min-w-[90px]">
                        <span className="block text-[14px] font-bold text-[#2b2b3a]">
                          ${(item.producto.precioNum * item.cantidad).toLocaleString('es-AR')}
                        </span>
                        <button
                          type="button"
                          onClick={() => removeItem(idx)}
                          className="text-[10px] text-[#ff8fa3] hover:underline cursor-pointer"
                        >
                          Eliminar
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Order Summary Card */}
          <div className="lg:col-span-4 space-y-4 sticky top-28">
            <div className="bg-white rounded-[22px] p-6 border border-[#ececf2] shadow-sm space-y-4">
              <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
                Resumen de Compra
              </span>
              <h3 className="text-lg font-bold text-[#2b2b3a]">
                Total del Pedido
              </h3>

              {/* Promo Code Input & Management */}
              <div className="pt-2 border-t border-[#f0f0f4] space-y-2">
                <label className="block text-[11px] font-semibold text-[#2b2b3a]">
                  ¿Tenés un código de promoción?
                </label>

                {appliedPromo ? (
                  <div className="p-3 rounded-[12px] bg-[#dff8e9] border border-[#7ed6a5]/50 flex items-center justify-between">
                    <div>
                      <div className="flex items-center gap-1.5">
                        <span className="text-[11px] font-bold text-[#39855b] bg-white px-2 py-0.5 rounded-md border border-[#7ed6a5]/60">
                          {appliedPromo.code}
                        </span>
                        <span className="text-[10px] text-[#39855b] font-semibold">
                          Aplicado
                        </span>
                      </div>
                      <small className="text-[10px] text-[#39855b] block mt-0.5">
                        {appliedPromo.descripcion}
                      </small>
                    </div>
                    <button
                      type="button"
                      onClick={handleRemovePromo}
                      className="text-[10px] text-[#c95569] font-bold hover:underline ml-2 cursor-pointer"
                    >
                      Quitar
                    </button>
                  </div>
                ) : (
                  <form onSubmit={handleApplyPromo} className="space-y-1.5">
                    <div className="flex gap-2">
                      <input
                        type="text"
                        value={promoCodeInput}
                        onChange={(e) => {
                          setPromoCodeInput(e.target.value);
                          if (promoError) setPromoError('');
                        }}
                        placeholder="Ingresá tu código..."
                        className="flex-1 border border-[#ececf2] rounded-[10px] px-3 py-2 text-[11px] text-[#2b2b3a] uppercase font-mono bg-[#f8f9fc] focus:bg-white focus:outline-none focus:border-[#e86bc4]"
                      />
                      <Button type="submit" className="px-3.5 py-2 text-[11px]">
                        Aplicar
                      </Button>
                    </div>
                    {promoError && (
                      <p className="text-[10px] text-[#ff8fa3] font-medium">{promoError}</p>
                    )}
                  </form>
                )}
              </div>

              {/* Price Breakdown */}
              <div className="space-y-3 pt-3 border-t border-[#f0f0f4] text-[13px]">
                <div className="flex justify-between text-[#6b6b80]">
                  <span>Subtotal mercadería</span>
                  <strong className="text-[#2b2b3a] font-semibold">${subtotal.toLocaleString('es-AR')}</strong>
                </div>

                {descuento > 0 && (
                  <div className="flex justify-between text-[#39855b]">
                    <span>Descuento promoción</span>
                    <strong>−${descuento.toLocaleString('es-AR')}</strong>
                  </div>
                )}

                <div className="flex justify-between text-[#6b6b80]">
                  <span>Costo de envío</span>
                  <span className="text-[#39855b] font-semibold">Gratis (Gran Mendoza)</span>
                </div>

                <div className="pt-3 border-t border-[#ececf2] flex justify-between items-baseline">
                  <span className="text-[15px] font-bold text-[#2b2b3a]">Total final</span>
                  <strong className="text-[24px] font-bold text-[#2b2b3a]">
                    ${total.toLocaleString('es-AR')}
                  </strong>
                </div>
              </div>

              <Button
                onClick={() => navigate('/checkout')}
                className="w-full py-3.5 text-[14px] justify-center cursor-pointer"
              >
                Continuar al Pago y Entrega
              </Button>

              <Link to="/tienda" className="block text-center text-[12px] text-[#6b6b80] hover:text-[#e86bc4] pt-2">
                ← Continuar comprando
              </Link>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
