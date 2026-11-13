import { NavLink, Outlet, Link, useLocation } from 'react-router-dom';
import Logo from '../components/Logo';
import Icon from '../components/Icon';
import { useAuth } from '../context/AuthContext';

export default function PreventorLayout({ onLogout }) {
  const { userData, debeCambiarClave } = useAuth();
  const location = useLocation();
  const usuarioActivo = userData || {
    nombre: 'Operador',
    avatar: 'OP',
    zona: 'Zona Asignada',
  };

  const navItems = [
    { to: '/recorrido', label: 'Recorrido', icon: 'route' },
    { to: '/pedidos', label: 'Mis Pedidos', icon: 'box' },
    { to: '/comercios', label: 'Comercios', icon: 'user' },
    { to: '/tienda', label: 'Tienda', icon: 'home' },
    { to: '/carrito', label: 'Carrito', icon: 'bag' },
    { to: '/perfil', label: 'Perfil', icon: 'user' },
  ];

  return (
    <div className="min-h-screen bg-[#edf0f8] flex flex-col">
      {/* Top Bar matching phone-top and responsive desktop navbar */}
      <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-sm border-b border-[#ececf2]">
        <div className="max-w-[1280px] mx-auto h-[66px] px-4 sm:px-6 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Logo />
            <span className="text-[10px] font-bold uppercase tracking-wider text-[#e86bc4] bg-[#ffe2f7] px-2.5 py-0.5 rounded-full">
              Preventor
            </span>
          </div>

          {/* Desktop/Tablet Navigation Links */}
          <nav className="hidden md:flex items-center gap-1">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `flex items-center gap-1.5 px-3 py-2 rounded-[12px] text-[12px] font-semibold transition-all ${
                    isActive
                      ? 'bg-[#fff0fb] text-[#e86bc4] shadow-sm'
                      : 'text-[#6b6b80] hover:text-[#2b2b3a] hover:bg-slate-50'
                  }`
                }
              >
                <Icon name={item.icon} className="w-4 h-4" />
                <span>{item.label}</span>
              </NavLink>
            ))}
          </nav>

          {/* User Avatar & Logout */}
          <div className="flex items-center gap-2">
            <Link
              to="/perfil"
              className="flex items-center gap-2 p-1 rounded-full hover:opacity-85 transition-opacity"
              title="Mi cuenta"
            >
              <div className="w-9 h-9 rounded-full bg-gradient-to-br from-[#ffcef3] to-[#a1eafb] text-[#2b2b3a] font-bold text-[12px] flex items-center justify-center shadow-sm">
                {usuarioActivo.avatar}
              </div>
              <div className="hidden lg:flex flex-col text-left">
                <span className="text-[12px] font-bold text-[#2b2b3a] leading-tight">
                  {usuarioActivo.nombre}
                </span>
                <span className="text-[10px] text-[#6b6b80]">
                  {usuarioActivo.zona}
                </span>
              </div>
            </Link>

            {onLogout && (
              <button
                type="button"
                onClick={onLogout}
                className="text-[11px] font-bold text-[#aaa9ba] hover:text-[#ff8fa3] p-1.5 rounded-lg transition-colors cursor-pointer"
                title="Cerrar sesión"
              >
                Salir
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Aviso global de clave temporal */}
      {debeCambiarClave && (
        <div className="bg-[#fff6e0] border-b border-[#ffd98a] text-[#8a6519] px-4 py-2.5 text-[12px] shadow-sm">
          <div className="max-w-[1280px] mx-auto flex flex-col sm:flex-row items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <span className="font-bold uppercase tracking-wider text-[10px] bg-[#8a6519] text-white px-2 py-0.5 rounded-md">Aviso</span>
              <strong className="font-bold">Cambie su clave para acceder:</strong>
              <span className="opacity-90">Su usuario posee una contraseña temporal por defecto.</span>
            </div>
            {location.pathname !== '/cambiar-password' && (
              <Link
                to="/cambiar-password"
                className="shrink-0 bg-[#8a6519] text-white px-3 py-1 rounded-[8px] font-semibold text-[11px] hover:bg-[#715214] transition-colors"
              >
                Cambiar clave ahora →
              </Link>
            )}
          </div>
        </div>
      )}

      {/* Main Content Area */}
      <main className="flex-1 max-w-[1280px] w-full mx-auto p-4 sm:p-6 pb-24 md:pb-8">
        {debeCambiarClave && location.pathname !== '/cambiar-password' && location.pathname !== '/perfil' ? (
          <div className="max-w-md mx-auto my-12 p-8 bg-white rounded-[24px] border border-[#ececf2] text-center space-y-4 shadow-sm">
            <div className="w-16 h-16 rounded-full bg-[#eefaff] text-[#247f99] flex items-center justify-center mx-auto shadow-xs">
              <Icon name="key" className="w-8 h-8" />
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
          <Outlet />
        )}
      </main>

      {/* Mobile Bottom Navigation Bar */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 h-[72px] bg-white border-t border-[#ececf2] flex items-center justify-around px-1 z-50 shadow-[0_-4px_20px_rgba(0,0,0,0.05)]">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `flex flex-col items-center justify-center flex-1 h-full py-1 text-[10px] font-semibold transition-all ${
                isActive
                  ? 'text-[#e86bc4]'
                  : 'text-[#aaa9ba] hover:text-[#2b2b3a]'
              }`
            }
          >
            <div className="p-1 rounded-full mb-0.5">
              <Icon name={item.icon} className="w-5 h-5" />
            </div>
            <span className="truncate max-w-[58px] text-center tracking-tight">{item.label}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
