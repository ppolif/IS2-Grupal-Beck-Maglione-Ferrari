import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../../components/Button';
import { cartItemsMock } from '../../mocks/products';

export default function Carrito() {
  const [cart, setCart] = useState(cartItemsMock);
  const [confirmed, setConfirmed] = useState(false);
  const [promoCodeInput, setPromoCodeInput] = useState('');
  const [appliedPromo, setAppliedPromo] = useState({
    code: 'MAYORISTA20',
    descripcion: '20% descuento por acuerdo comercial',
    pct: 0.20
  });
  const [promoError, setPromoError] = useState('');
  const navigate = useNavigate();

  const PROMO_CODES = {
    'MAYORISTA20': { code: 'MAYORISTA20', descripcion: '20% descuento por acuerdo comercial', pct: 0.20 },
    'DULCE10': { code: 'DULCE10', descripcion: '10% bonificación de preventa', pct: 0.10 },
    'PROMOALFAJORES': { code: 'PROMOALFAJORES', descripcion: 'Bonificación fija de $4.900', fijo: 4900 }
  };

  const updateQuantity = (id, delta) => {
    setCart((prev) =>
      prev.map((item) => {
        if (item.id === id) {
          const newQ = Math.max(1, item.cantidad + delta);
          return { ...item, cantidad: newQ };
        }
        return item;
      })
    );
  };

  const handleApplyPromo = (e) => {
    e.preventDefault();
    const clean = promoCodeInput.trim().toUpperCase();
    if (!clean) return;

    if (PROMO_CODES[clean]) {
      setAppliedPromo(PROMO_CODES[clean]);
      setPromoError('');
      setPromoCodeInput('');
    } else {
      setPromoError('Código inválido o sin vigencia para este comercio.');
    }
  };

  const handleRemovePromo = () => {
    setAppliedPromo(null);
    setPromoError('');
  };

  const subtotal = cart.reduce((acc, it) => acc + it.precioNum * it.cantidad, 0);

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
    <div className="max-w-xl mx-auto space-y-6">
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Pedido en Curso
        </span>
        <h1 className="text-2xl font-bold text-[#2b2b3a] mt-1">
          Tu Carrito de Preventa
        </h1>
        <p className="text-[12px] text-[#6b6b80] mt-0.5">
          Cliente: <strong>Kiosco El Sol</strong> (Av. San Martín 1842)
        </p>
      </div>

      <div className="bg-white rounded-[22px] p-5 sm:p-6 border border-[#ececf2] shadow-sm">
        {confirmed ? (
          <div className="text-center p-8 bg-[#dff8e9] rounded-[16px] text-[#39855b] space-y-3">
            <h3 className="text-lg font-bold">¡Pedido registrado con éxito!</h3>
            <p className="text-[12px]">
              El pedido #DM-2085 fue transmitido al almacén con los descuentos aplicados para su preparación y despacho.
            </p>
            <div className="pt-2">
              <Button onClick={() => navigate('/recorrido')}>
                Volver a la hoja de ruta
              </Button>
            </div>
          </div>
        ) : (
          <div className="space-y-4">
            {/* Cart lines matching prototype */}
            <div className="divide-y divide-[#f0f0f4]">
              {cart.map((p) => (
                <div
                  key={p.id}
                  className="py-3 first:pt-0 last:pb-0 grid grid-cols-[58px_1fr_auto] gap-3 items-center"
                >
                  <img
                    src={p.imagen}
                    alt={p.nombre}
                    className="w-[58px] h-[58px] rounded-[10px] object-cover border border-[#ececf2]"
                  />
                  <div className="flex flex-col text-[11px]">
                    <b className="text-[13px] text-[#2b2b3a] leading-tight font-semibold">
                      {p.nombre}
                    </b>
                    <small className="text-[#6b6b80] text-[10px]">{p.presentacion}</small>
                    <div className="flex items-center gap-2 mt-1.5 text-[#e86bc4] font-bold">
                      <button
                        type="button"
                        onClick={() => updateQuantity(p.id, -1)}
                        className="w-5 h-5 rounded bg-[#ffe2f7] flex items-center justify-center text-[12px] cursor-pointer"
                      >
                        −
                      </button>
                      <span className="text-[12px] text-[#2b2b3a]">{p.cantidad}</span>
                      <button
                        type="button"
                        onClick={() => updateQuantity(p.id, 1)}
                        className="w-5 h-5 rounded bg-[#ffe2f7] flex items-center justify-center text-[12px] cursor-pointer"
                      >
                        +
                      </button>
                    </div>
                  </div>
                  <strong className="text-[13px] font-bold text-[#2b2b3a]">
                    ${(p.precioNum * p.cantidad).toLocaleString('es-AR')}
                  </strong>
                </div>
              ))}
            </div>

            {/* Código de Promoción Input */}
            <div className="pt-3 border-t border-[#f0f0f4] space-y-2">
              <label className="block text-[11px] font-semibold text-[#2b2b3a]">
                Código de Promoción / Acuerdo de Preventa
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
                      placeholder="Ej. MAYORISTA20 o DULCE10"
                      className="flex-1 border border-[#ececf2] rounded-[10px] px-3 py-2 text-[11px] text-[#2b2b3a] uppercase font-mono bg-[#f8f9fc] focus:bg-white focus:outline-none focus:border-[#e86bc4]"
                    />
                    <Button type="submit" className="px-3.5 py-2 text-[11px]">
                      Aplicar
                    </Button>
                  </div>
                  {promoError && (
                    <p className="text-[10px] text-[#ff8fa3] font-medium">{promoError}</p>
                  )}
                  <span className="text-[9px] text-[#aaa9ba] block">
                    Códigos vigentes: <code className="text-[#e86bc4] font-semibold">MAYORISTA20</code>, <code className="text-[#e86bc4] font-semibold">DULCE10</code>, <code className="text-[#e86bc4] font-semibold">PROMOALFAJORES</code>
                  </span>
                </form>
              )}
            </div>

            {/* Total summary from prototype */}
            <div className="bg-[#fafafd] rounded-[14px] p-4 border border-[#ececf2] space-y-2 text-[12px]">
              <div className="flex justify-between text-[#6b6b80]">
                <span>Subtotal mercadería</span>
                <b className="text-[#2b2b3a]">${subtotal.toLocaleString('es-AR')}</b>
              </div>

              {descuento > 0 && (
                <div className="flex justify-between text-[#39855b]">
                  <span>Descuento aplicado</span>
                  <b>−${descuento.toLocaleString('es-AR')}</b>
                </div>
              )}

              <div className="flex justify-between text-[15px] pt-2 border-t border-[#ececf2] text-[#2b2b3a]">
                <strong>Total a Facturar</strong>
                <strong className="text-[#e86bc4] text-[18px]">
                  ${total.toLocaleString('es-AR')}
                </strong>
              </div>
            </div>

            <div className="pt-2">
              <Button
                onClick={() => setConfirmed(true)}
                className="w-full py-3.5 text-[14px] justify-center cursor-pointer"
              >
                Confirmar pedido
              </Button>
            </div>

            <div className="text-center pt-2">
              <Link to="/tienda" className="text-[11px] text-[#3fb8d9] font-medium hover:underline">
                + Agregar más productos desde la tienda
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
