export default function ProductCard({
  product,
  onAddToCart,
  onClick,
}) {
  if (!product) return null;

  const {
    nombre = 'Producto',
    presentacion = 'Unidad mayorista',
    precio = '$0',
    categoria = 'Catálogo',
    imagen = 'https://images.unsplash.com/photo-1582058091505-f87a2e55a40f?auto=format&fit=crop&w=1000&q=85',
  } = product;

  return (
    <article
      onClick={() => onClick && onClick(product)}
      className="border border-[#ececf2] rounded-[20px] p-2.5 bg-white shadow-sm hover:shadow-md transition-all flex flex-col justify-between cursor-pointer group"
    >
      <div>
        <div className="relative overflow-hidden rounded-[14px] bg-slate-50 h-[150px]">
          <img
            src={imagen}
            alt={nombre}
            className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
            loading="lazy"
          />
          {categoria && (
            <span className="absolute top-2 left-2 bg-white/90 text-[10px] font-semibold text-[#6b6b80] px-2 py-0.5 rounded-full backdrop-blur-sm">
              {categoria}
            </span>
          )}
        </div>
        <span className="block mt-3 mx-1 text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Precio mayorista
        </span>
        <h3 className="font-semibold text-[15px] text-[#2b2b3a] mx-1 my-0.5 leading-snug group-hover:text-[#e86bc4] transition-colors">
          {nombre}
        </h3>
        <p className="text-[12px] text-[#6b6b80] mx-1 mb-2">
          {presentacion}
        </p>
      </div>
      <div className="flex items-center justify-between mx-1 mt-3 pt-2 border-t border-[#f0f0f4]">
        <strong className="text-[16px] font-bold text-[#2b2b3a]">
          {precio}
        </strong>
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            onAddToCart && onAddToCart(product);
          }}
          className="w-[34px] h-[34px] rounded-[12px] bg-[#e86bc4] hover:bg-[#d456b0] text-white text-[21px] flex items-center justify-center transition-colors cursor-pointer shadow-sm active:scale-95"
          title="Agregar al carrito"
          aria-label={`Agregar ${nombre} al carrito`}
        >
          +
        </button>
      </div>
    </article>
  );
}
