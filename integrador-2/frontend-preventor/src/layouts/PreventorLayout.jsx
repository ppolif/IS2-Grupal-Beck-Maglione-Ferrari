import { NavLink, Outlet, Link } from 'react-router-dom';
import Logo from '../components/Logo';
import Icon from '../components/Icon';
import { usuarioMock } from '../mocks/session';

export default function PreventorLayout({ onSwitchRole, onLogout }) {
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
            <span className="hidden sm:inline-block text-[10px] font-bold uppercase tracking-wider text-[#e86bc4] bg-[#ffe2f7] px-2.5 py-0.5 rounded-full">
              Preventor
            </span>

            {/* Selector rápido de rol para demo */}
            {onSwitchRole && (
              <button
                type="button"
                onClick={() => onSwitchRole('distribuidor')}
                className="hidden sm:inline-flex items-center gap-1 text-[11px] font-semibold text-[#247f99] bg-[#eefaff] border border-[#a1eafb] px-2.5 py-1 rounded-full hover:bg-[#dff3fb] transition-colors cursor-pointer"
                title="Cambiar a vista de Distribuidor"
              >
                <span>🚚 Ir a Reparto</span>
              </button>
            )}
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
                <Icon name={item.icon} className="w-3.5 h-3.5" />
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
                {usuarioMock.avatar}
              </div>
              <div className="hidden lg:flex flex-col text-left">
                <span className="text-[12px] font-bold text-[#2b2b3a] leading-tight">
                  {usuarioMock.nombre}
                </span>
                <span className="text-[10px] text-[#6b6b80]">
                  {usuarioMock.zona}
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

      {/* Main Content Area */}
      <main className="flex-1 max-w-[1280px] w-full mx-auto p-4 sm:p-6 pb-24 md:pb-8">
        <Outlet />
      </main>

      {/* Mobile Bottom Navigation Bar */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 h-[72px] bg-white border-t border-[#ececf2] flex items-center justify-around px-1 z-50 shadow-[0_-4px_20px_rgba(0,0,0,0.05)]">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `flex flex-col items-center justify-center gap-0.5 flex-1 py-1 px-0.5 rounded-[10px] text-[8.5px] font-semibold transition-colors ${
                isActive
                  ? 'text-[#e86bc4] bg-[#fff0fb]'
                  : 'text-[#9b9aaa] hover:text-[#2b2b3a]'
              }`
            }
          >
            <Icon name={item.icon} className="w-[18px] h-[18px]" />
            <span className="truncate">{item.label}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
