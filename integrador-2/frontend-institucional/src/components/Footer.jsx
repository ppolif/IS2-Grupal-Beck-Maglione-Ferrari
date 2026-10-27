import { Link } from 'react-router-dom';
import Logo from './Logo';

export default function Footer() {
  return (
    <footer className="bg-[#292838] text-white pt-14 pb-10 px-6 sm:px-10 md:px-14 mt-16 rounded-t-[30px]">
      <div className="max-w-[1440px] mx-auto grid grid-cols-1 md:grid-cols-4 gap-10 pb-12 border-b border-white/10">
        {/* Brand column */}
        <div className="space-y-4 md:col-span-1">
          <Logo light />
          <p className="text-[12px] text-[#bcbac9] leading-relaxed">
            Mayorista líder de golosinas en Mendoza. Abastecemos a comercios y minoristas con el catálogo más completo, precios competitivos y logística propia.
          </p>
          <div className="pt-2 text-[11px] text-[#bcbac9]">
            <p className="font-semibold text-white">Casa Central:</p>
            <p>Av. San Martín 1045, Ciudad de Mendoza</p>
            <p>Tel: (0261) 420-9800</p>
          </div>
        </div>

        {/* Navigation links */}
        <div>
          <h4 className="text-[13px] font-bold tracking-wider uppercase text-[#a1eafb] mb-4">
            Navegación
          </h4>
          <ul className="space-y-2.5 text-[12px] text-[#bcbac9]">
            <li>
              <Link to="/" className="hover:text-white transition-colors">Inicio</Link>
            </li>
            <li>
              <Link to="/tienda" className="hover:text-white transition-colors">Tienda Mayorista</Link>
            </li>
            <li>
              <Link to="/sucursales" className="hover:text-white transition-colors">Sucursales y Depósitos</Link>
            </li>
            <li>
              <Link to="/contacto" className="hover:text-white transition-colors">Contacto y Asesoramiento</Link>
            </li>
            <li>
              <Link to="/carrito" className="hover:text-white transition-colors">Mi Carrito</Link>
            </li>
          </ul>
        </div>

        {/* Categories */}
        <div>
          <h4 className="text-[13px] font-bold tracking-wider uppercase text-[#ffcef3] mb-4">
            Categorías
          </h4>
          <ul className="space-y-2.5 text-[12px] text-[#bcbac9]">
            <li>
              <Link to="/tienda" className="hover:text-white transition-colors">Alfajores y Masas</Link>
            </li>
            <li>
              <Link to="/tienda" className="hover:text-white transition-colors">Golosinas y Gomitas</Link>
            </li>
            <li>
              <Link to="/tienda" className="hover:text-white transition-colors">Chupetines y Caramelos</Link>
            </li>
            <li>
              <Link to="/tienda" className="hover:text-white transition-colors">Chocolates y Bombones</Link>
            </li>
            <li>
              <Link to="/tienda" className="hover:text-white transition-colors">Galletitas y Snacks</Link>
            </li>
          </ul>
        </div>

        {/* Trust badge / Hours */}
        <div className="space-y-3">
          <h4 className="text-[13px] font-bold tracking-wider uppercase text-white mb-4">
            Horarios de Atención
          </h4>
          <p className="text-[12px] text-[#bcbac9]">
            <strong className="text-white">Lunes a Viernes:</strong><br />
            08:00 a 18:30 hs.
          </p>
          <p className="text-[12px] text-[#bcbac9]">
            <strong className="text-white">Sábados:</strong><br />
            08:30 a 13:00 hs.
          </p>
          <div className="pt-2">
            <span className="inline-block bg-white/10 text-[#a1eafb] text-[11px] font-semibold px-3 py-1.5 rounded-full">
              Distribución en todo Gran Mendoza
            </span>
          </div>
        </div>
      </div>

      <div className="max-w-[1440px] mx-auto mt-8 flex flex-col sm:flex-row items-center justify-between text-[11px] text-[#aaa9ba] gap-4">
        <p>© 2026 Dulce Mayor S.A. - Todos los derechos reservados.</p>
        <p>Desarrollo Front-end UI Kit v1.0 - React & Tailwind CSS</p>
      </div>
    </footer>
  );
}

