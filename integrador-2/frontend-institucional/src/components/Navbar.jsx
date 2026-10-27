import { useState } from 'react';
import { NavLink, Link, useNavigate } from 'react-router-dom';
import Logo from './Logo';
import Button from './Button';
import Icon from './Icon';
import { usuarioMock } from '../mocks/session';

export default function Navbar() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [isLogged, setIsLogged] = useState(usuarioMock.logueado);
  const navigate = useNavigate();

  const cartCount = 3; // Mock de cantidad de productos en carrito

  const navLinks = [
    { to: '/', label: 'Inicio' },
    { to: '/tienda', label: 'Tienda' },
    { to: '/sucursales', label: 'Sucursales' },
    { to: '/contacto', label: 'Contacto' },
  ];

  return (
    <header className="sticky top-0 z-40 bg-white border-b border-[#ececf2]">
      <div className="max-w-[1440px] mx-auto h-[82px] px-4 sm:px-8 md:px-14 flex items-center justify-between gap-6">
        {/* Logo */}
        <Logo />

        {/* Desktop Links */}
        <nav className="hidden md:flex items-center gap-7 lg:gap-8 text-[13px] font-medium text-[#6b6b80]">
          {navLinks.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                isActive
                  ? 'text-[#e86bc4] font-bold border-b-2 border-[#e86bc4] pb-1 transition-colors'
                  : 'hover:text-[#e86bc4] pb-1 transition-colors'
              }
            >
              {link.label}
            </NavLink>
          ))}
        </nav>

        {/* Right side: Actions / Auth / Cart */}
        <div className="hidden md:flex items-center gap-3">
          {/* Cart button */}
          <Link
            to="/carrito"
            className="relative p-2.5 rounded-[12px] text-[#2b2b3a] hover:bg-[#ffe2f7]/50 transition-colors"
            title="Ver carrito"
          >
            <Icon name="bag" className="w-5 h-5 text-[#2b2b3a]" />
            {cartCount > 0 && (
              <span className="absolute -top-1 -right-1 bg-[#e86bc4] text-white text-[10px] font-bold w-5 h-5 rounded-full flex items-center justify-center shadow-sm">
                {cartCount}
              </span>
            )}
          </Link>

          {isLogged ? (
            <div className="flex items-center gap-3 pl-2 border-l border-[#ececf2]">
              <Link
                to="/perfil"
                className="flex items-center gap-2 hover:opacity-85 transition-opacity"
                title="Mi perfil"
              >
                <div className="w-9 h-9 rounded-full bg-gradient-to-br from-[#ffcef3] to-[#a1eafb] text-[#2b2b3a] font-bold text-[12px] flex items-center justify-center shadow-sm">
                  {usuarioMock.avatar}
                </div>
                <div className="hidden lg:flex flex-col text-left">
                  <span className="text-[12px] font-bold text-[#2b2b3a] leading-tight">
                    {usuarioMock.nombre}
                  </span>
                  <span className="text-[10px] text-[#6b6b80]">
                    {usuarioMock.comercio}
                  </span>
                </div>
              </Link>
              <button
                type="button"
                onClick={() => setIsLogged(false)}
                className="text-[11px] text-[#6b6b80] hover:text-[#ff8fa3] ml-2 px-2 py-1 rounded transition-colors"
                title="Simular cerrar sesión"
              >
                Salir
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2.5">
              <Button
                secondary
                onClick={() => navigate('/login')}
                className="px-4 py-2.5 text-[12px]"
              >
                Ingresar
              </Button>
              <Button
                onClick={() => navigate('/registro')}
                className="px-4 py-2.5 text-[12px]"
              >
                Registrarme
              </Button>
            </div>
          )}
        </div>

        {/* Mobile buttons: Cart & Hamburger */}
        <div className="flex items-center gap-2 md:hidden">
          <Link
            to="/carrito"
            className="relative p-2 rounded-[12px] text-[#2b2b3a]"
          >
            <Icon name="bag" className="w-5 h-5 text-[#2b2b3a]" />
            {cartCount > 0 && (
              <span className="absolute -top-1 -right-1 bg-[#e86bc4] text-white text-[10px] font-bold w-4 h-4 rounded-full flex items-center justify-center">
                {cartCount}
              </span>
            )}
          </Link>
          <button
            type="button"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="p-2 rounded-[12px] text-[#2b2b3a] hover:bg-slate-100 transition-colors"
            aria-label="Abrir menú"
          >
            <Icon name={mobileMenuOpen ? 'x' : 'menu'} className="w-6 h-6" />
          </button>
        </div>
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div className="md:hidden bg-white border-b border-[#ececf2] px-6 py-5 flex flex-col gap-4 animate-in slide-in-from-top duration-200">
          <nav className="flex flex-col gap-3">
            {navLinks.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                onClick={() => setMobileMenuOpen(false)}
                className={({ isActive }) =>
                  isActive
                    ? 'text-[#e86bc4] font-bold text-[15px] py-1'
                    : 'text-[#6b6b80] hover:text-[#e86bc4] text-[15px] py-1'
                }
              >
                {link.label}
              </NavLink>
            ))}
            <NavLink
              to="/carrito"
              onClick={() => setMobileMenuOpen(false)}
              className="text-[#6b6b80] hover:text-[#e86bc4] text-[15px] py-1 flex items-center justify-between"
            >
              <span>Mi Carrito</span>
              <span className="bg-[#ffe2f7] text-[#a34187] text-[11px] font-semibold px-2 py-0.5 rounded-full">
                {cartCount} items
              </span>
            </NavLink>
            {isLogged && (
              <NavLink
                to="/perfil"
                onClick={() => setMobileMenuOpen(false)}
                className="text-[#6b6b80] hover:text-[#e86bc4] text-[15px] py-1"
              >
                Mi Perfil ({usuarioMock.nombre})
              </NavLink>
            )}
          </nav>

          <div className="pt-3 border-t border-[#ececf2] flex flex-col gap-2">
            {isLogged ? (
              <Button
                secondary
                onClick={() => {
                  setIsLogged(false);
                  setMobileMenuOpen(false);
                }}
                className="w-full justify-center"
              >
                Cerrar sesión (Simulada)
              </Button>
            ) : (
              <>
                <Button
                  secondary
                  onClick={() => {
                    navigate('/login');
                    setMobileMenuOpen(false);
                  }}
                  className="w-full justify-center"
                >
                  Ingresar
                </Button>
                <Button
                  onClick={() => {
                    navigate('/registro');
                    setMobileMenuOpen(false);
                  }}
                  className="w-full justify-center"
                >
                  Registrarme
                </Button>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
}

