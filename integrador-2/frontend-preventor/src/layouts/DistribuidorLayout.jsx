import { NavLink, Outlet, Link } from 'react-router-dom';
import Logo from '../components/Logo';
import Icon from '../components/Icon';
import { usuarioMock } from '../mocks/session';

export default function DistribuidorLayout() {
  const navItems = [
    { to: '/pedidos', label: 'Pedidos', icon: 'box' },
    { to: '/recorrido', label: 'Recorrido', icon: 'route' },
    { to: '/perfil', label: 'Perfil', icon: 'user' },
  ];

  return (
    <div className="min-h-screen bg-[#edf0f8] flex flex-col">
      {/* Top Bar */}
      <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-sm border-b border-[#ececf2]">
        <div className="max-w-[1280px] mx-auto h-[66px] px-4 sm:px-6 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Logo />
            <span className="hidden sm:inline-block text-[10px] font-bold uppercase tracking-wider text-[#3fb8d9] bg-[#d9f7ff] px-2.5 py-0.5 rounded-full">
              Distribuidor
            </span>
          </div>

          {/* Desktop/Tablet Navigation Links */}
          <nav className="hidden md:flex items-center gap-2">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `flex items-center gap-2 px-4 py-2 rounded-[12px] text-[12px] font-semibold transition-all ${
                    isActive
                      ? 'bg-[#d9f7ff] text-[#247f99] shadow-sm'
                      : 'text-[#6b6b80] hover:text-[#2b2b3a] hover:bg-slate-50'
                  }`
                }
              >
                <Icon name={item.icon} className="w-4 h-4" />
                <span>{item.label}</span>
              </NavLink>
            ))}
          </nav>

          {/* User Avatar */}
          <Link
            to="/perfil"
            className="flex items-center gap-2.5 p-1 rounded-full hover:opacity-85 transition-opacity"
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
                {usuarioMock.unidad}
              </span>
            </div>
          </Link>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-[1280px] w-full mx-auto p-4 sm:p-6 pb-24 md:pb-8">
        <Outlet />
      </main>

      {/* Mobile Bottom Navigation Bar */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 h-[72px] bg-white border-t border-[#ececf2] flex items-center justify-around px-4 z-50 shadow-[0_-4px_20px_rgba(0,0,0,0.05)]">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `flex flex-col items-center justify-center gap-1 flex-1 py-1 px-2 rounded-[12px] text-[10px] font-semibold transition-colors ${
                isActive
                  ? 'text-[#247f99] bg-[#d9f7ff]'
                  : 'text-[#9b9aaa] hover:text-[#2b2b3a]'
              }`
            }
          >
            <Icon name={item.icon} className="w-[19px] h-[19px]" />
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}

