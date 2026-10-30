import { useState } from 'react';
import { NavLink, Link, useNavigate } from 'react-router-dom';
import Logo from './Logo';
import Button from './Button';
import Icon from './Icon';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const { isLogged, perfil, usuario, logout } = useAuth();
  const navigate = useNavigate();

  const nombreDisplay = perfil ? `${perfil.nombre} ${perfil.apellido}` : (usuario?.nombreUsuario || 'Mi Cuenta');
  const iniciales = perfil
    ? `${perfil.nombre?.[0] || ''}${perfil.apellido?.[0] || ''}`.toUpperCase()
    : (usuario?.nombreUsuario?.[0] || 'U').toUpperCase();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-40 bg-white border-b border-[#ececf2]">
      <div className="max-w-[1440px] mx-auto h-[82px] px-4 sm:px-8 md:px-14 flex items-center justify-between gap-6">
        <Logo />

        <nav className="hidden md:flex items-center gap-7 lg:gap-8 text-[13px] font-medium text-[#6b6b80]">
          {isLogged ? (
            <NavLink
              to="/perfil"
              className={({ isActive }) =>
                isActive
                  ? 'text-[#e86bc4] font-bold border-b-2 border-[#e86bc4] pb-1 transition-colors'
                  : 'hover:text-[#e86bc4] pb-1 transition-colors'
              }
            >
              Mi Perfil
            </NavLink>
          ) : (
            <>
              <NavLink
                to="/login"
                className={({ isActive }) =>
                  isActive
                    ? 'text-[#e86bc4] font-bold border-b-2 border-[#e86bc4] pb-1 transition-colors'
                    : 'hover:text-[#e86bc4] pb-1 transition-colors'
                }
              >
                Iniciar Sesión
              </NavLink>
              <NavLink
                to="/registro"
                className={({ isActive }) =>
                  isActive
                    ? 'text-[#e86bc4] font-bold border-b-2 border-[#e86bc4] pb-1 transition-colors'
                    : 'hover:text-[#e86bc4] pb-1 transition-colors'
                }
              >
                Registro
              </NavLink>
            </>
          )}
        </nav>

        <div className="hidden md:flex items-center gap-3">
          {isLogged ? (
            <div className="flex items-center gap-3 pl-2">
              <Link
                to="/perfil"
                className="flex items-center gap-2 hover:opacity-85 transition-opacity"
                title="Mi perfil"
              >
                <div className="w-9 h-9 rounded-full bg-gradient-to-br from-[#ffcef3] to-[#a1eafb] text-[#2b2b3a] font-bold text-[12px] flex items-center justify-center shadow-sm">
                  {iniciales}
                </div>
                <div className="hidden lg:flex flex-col text-left">
                  <span className="text-[12px] font-bold text-[#2b2b3a] leading-tight max-w-[150px] truncate">
                    {nombreDisplay}
                  </span>
                  <span className="text-[10px] text-[#6b6b80] max-w-[150px] truncate">
                    {usuario?.nombreUsuario}
                  </span>
                </div>
              </Link>
              <button
                type="button"
                onClick={handleLogout}
                className="text-[11px] text-[#6b6b80] hover:text-[#ff8fa3] ml-2 px-2 py-1 rounded transition-colors cursor-pointer"
                title="Cerrar sesión"
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

        <div className="flex items-center gap-2 md:hidden">
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

      {mobileMenuOpen && (
        <div className="md:hidden bg-white border-b border-[#ececf2] px-6 py-5 flex flex-col gap-4 animate-in slide-in-from-top duration-200">
          {isLogged && (
            <nav className="flex flex-col gap-3">
              <NavLink
                to="/perfil"
                onClick={() => setMobileMenuOpen(false)}
                className="text-[#6b6b80] hover:text-[#e86bc4] text-[15px] py-1"
              >
                Mi Perfil ({nombreDisplay})
              </NavLink>
            </nav>
          )}

          <div className="pt-3 border-t border-[#ececf2] flex flex-col gap-2">
            {isLogged ? (
              <Button
                secondary
                onClick={() => {
                  setMobileMenuOpen(false);
                  handleLogout();
                }}
                className="w-full justify-center"
              >
                Cerrar sesión
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

