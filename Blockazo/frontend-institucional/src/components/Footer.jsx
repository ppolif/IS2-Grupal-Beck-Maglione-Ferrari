import { Link } from 'react-router-dom';
import Logo from './Logo';

export default function Footer() {
  return (
    <footer className="bg-[#292838] text-white pt-10 pb-8 px-6 sm:px-10 md:px-14 mt-16 rounded-t-[30px]">
      <div className="max-w-[1440px] mx-auto flex flex-col md:flex-row justify-between gap-8 pb-8 border-b border-white/10">
        <div className="space-y-3 max-w-md">
          <Logo light />
          <p className="text-[12px] text-[#bcbac9] leading-relaxed">
            Módulo aislado de Gestión de Usuarios Minoristas (Registro, Inicio de Sesión y Administración de Perfil).
          </p>
        </div>

        <div>
          <h4 className="text-[13px] font-bold tracking-wider uppercase text-[#a1eafb] mb-3">
            Accesos Rápidos
          </h4>
          <ul className="space-y-2 text-[12px] text-[#bcbac9]">
            <li>
              <Link to="/login" className="hover:text-white transition-colors">Iniciar Sesión</Link>
            </li>
            <li>
              <Link to="/registro" className="hover:text-white transition-colors">Registro de Cuenta</Link>
            </li>
            <li>
              <Link to="/perfil" className="hover:text-white transition-colors">Mi Perfil</Link>
            </li>
          </ul>
        </div>
      </div>

      <div className="max-w-[1440px] mx-auto mt-6 flex flex-col sm:flex-row items-center justify-between text-[11px] text-[#aaa9ba] gap-4">
        <p>© 2026 Dulce Mayor S.A. - Blockazo (ABM Usuarios)</p>
        <p>React & Tailwind CSS</p>
      </div>
    </footer>
  );
}

