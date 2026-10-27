import { Link } from 'react-router-dom';
import Button from '../components/Button';
import Icon from '../components/Icon';
import ProductCard from '../components/ProductCard';
import { productsMock, candyImg } from '../mocks/products';

export default function Home() {
  const previewProducts = productsMock.slice(0, 4);

  return (
    <div className="space-y-12">
      {/* Hero Section matching prototype web-hero */}
      <section className="relative overflow-hidden rounded-[26px] bg-gradient-to-tr from-[#ffe7f8] via-[#f7eefc] to-[#dff8ff] p-8 sm:p-12 md:p-16 min-h-[490px] flex items-center shadow-[0_14px_40px_rgba(56,52,84,0.08)]">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center w-full">
          <div className="lg:col-span-7 space-y-4 z-10">
            <span className="inline-block bg-white/70 backdrop-blur-sm text-[#e86bc4] text-[11px] font-bold tracking-[0.14em] uppercase px-3 py-1.5 rounded-full border border-white/60">
              Mayorista de Mendoza
            </span>
            <h1 className="text-4xl sm:text-5xl lg:text-[54px] font-bold text-[#2b2b3a] leading-[1.08] tracking-tight">
              Las mejores golosinas <br className="hidden sm:inline" />
              al por mayor
            </h1>
            <p className="text-[16px] sm:text-[17px] text-[#6b6b80] max-w-[500px] leading-relaxed">
              Todo lo que tu comercio necesita, con precios que dan gusto y entrega rápida en toda la provincia.
            </p>
            <div className="pt-2 flex flex-wrap items-center gap-4">
              <Link to="/tienda">
                <Button className="text-[14px] px-6 py-3.5">
                  Ver la tienda <span className="ml-1">→</span>
                </Button>
              </Link>
              <Link to="/registro">
                <Button secondary className="text-[14px] px-6 py-3.5">
                  Registrar mi comercio
                </Button>
              </Link>
            </div>
            <div className="pt-4 flex items-center gap-2 text-[12px] text-[#6b6b80]">
              <strong className="text-[#2b2b3a] font-bold text-[14px]">+450</strong> comercios de Mendoza eligen Dulce Mayor cada mes
            </div>
          </div>

          {/* Photo stack from prototype */}
          <div className="hidden lg:flex lg:col-span-5 relative justify-center items-center h-[380px]">
            <img
              src={candyImg}
              alt="Golosinas coloridas mayoristas"
              className="w-[90%] h-full object-cover rounded-[48%_48%_20%_20%] rotate-3 shadow-[0_14px_40px_rgba(56,52,84,0.15)] border-4 border-white"
            />
            {/* Dollar indicator badge */}
            <div className="absolute right-0 bottom-6 bg-white px-5 py-3.5 rounded-[16px] shadow-[0_14px_40px_rgba(56,52,84,0.12)] border border-[#ececf2] flex flex-col">
              <small className="text-[9px] font-bold tracking-wider text-[#6b6b80] uppercase">Dólar Hoy</small>
              <strong className="text-[20px] font-bold text-[#2b2b3a] leading-tight">$ 1.185</strong>
              <span className="text-[8px] text-[#6b6b80]">Actualizado 10:32</span>
            </div>
          </div>
        </div>
      </section>

      {/* Shop Preview Section matching prototype shop-preview */}
      <section className="bg-white rounded-[22px] p-6 sm:p-10 md:p-12 border border-[#ececf2] shadow-[0_14px_40px_rgba(56,52,84,0.06)]">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
          <div>
            <span className="text-[11px] font-bold tracking-[0.14em] text-[#e86bc4] uppercase">
              Para tu negocio
            </span>
            <h2 className="text-2xl sm:text-[32px] font-bold text-[#2b2b3a] mt-1">
              Una muestra de nuestra tienda
            </h2>
          </div>
          <Link to="/tienda">
            <Button secondary>
              Ver catálogo completo
            </Button>
          </Link>
        </div>

        {/* 4 columns in desktop, 2 in tablet/mobile */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          {previewProducts.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      </section>

      {/* Benefits Section matching prototype benefits */}
      <section className="bg-[#292838] text-white rounded-[24px] p-8 sm:p-10 md:p-12">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="flex flex-col sm:flex-row md:flex-col items-start gap-4">
            <span className="w-[44px] h-[44px] rounded-[13px] bg-[#a1eafb] text-[#257e96] flex items-center justify-center p-2.5 shrink-0">
              <Icon name="box" className="w-6 h-6" />
            </span>
            <div>
              <h3 className="text-[16px] font-bold mb-1 text-white">Precios mayoristas</h3>
              <p className="text-[12px] text-[#bcbac9] leading-relaxed">
                Comprá por bulto y hacé crecer tu margen de ganancia con listas escalonadas y promociones semanales.
              </p>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row md:flex-col items-start gap-4">
            <span className="w-[44px] h-[44px] rounded-[13px] bg-[#a1eafb] text-[#257e96] flex items-center justify-center p-2.5 shrink-0">
              <Icon name="route" className="w-6 h-6" />
            </span>
            <div>
              <h3 className="text-[16px] font-bold mb-1 text-white">Entrega a domicilio</h3>
              <p className="text-[12px] text-[#bcbac9] leading-relaxed">
                Flota de distribución propia con recorridos diarios en toda el área metropolitana de Gran Mendoza.
              </p>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row md:flex-col items-start gap-4">
            <span className="w-[44px] h-[44px] rounded-[13px] bg-[#a1eafb] text-[#257e96] flex items-center justify-center p-2.5 shrink-0">
              <Icon name="user" className="w-6 h-6" />
            </span>
            <div>
              <h3 className="text-[16px] font-bold mb-1 text-white">Atención cercana</h3>
              <p className="text-[12px] text-[#bcbac9] leading-relaxed">
                Te asesora personalmente un preventor asignado a tu zona, optimizando tus compras y reabastecimiento.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

