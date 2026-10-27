import { NavLink } from 'react-router-dom';
import Logo from './Logo';
import Icon from './Icon';

export default function SidebarBase({
  links = [],
  roleTitle = 'GESTIÓN',
  mobileOpen = false,
  onCloseMobile
}) {
  return (
    <>
      {/* Mobile Backdrop */}
      {mobileOpen && (
        <div
          onClick={onCloseMobile}
          className="fixed inset-0 bg-black/40 backdrop-blur-xs z-40 lg:hidden transition-opacity"
        />
      )}

      {/* Sidebar container */}
      <aside
        className={`fixed top-0 bottom-0 left-0 w-[240px] bg-white border-r border-[#ececf2] p-5 flex flex-col justify-between z-50 transition-transform duration-200 lg:static lg:translate-x-0 ${
          mobileOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div>
          {/* Logo & Mobile close button */}
          <div className="flex items-center justify-between pb-6 px-1">
            <Logo />
            <button
              type="button"
              onClick={onCloseMobile}
              className="lg:hidden text-[#6b6b80] hover:text-[#2b2b3a] p-1"
            >
              <Icon name="x" className="w-5 h-5" />
            </button>
          </div>

          {/* Section label from prototype: GESTIÓN */}
          <div className="text-[10px] font-bold text-[#aaa9ba] tracking-[0.15em] uppercase px-3 py-2">
            {roleTitle}
          </div>

          {/* Navigation Links */}
          <nav className="space-y-1 mt-1">
            {links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                onClick={onCloseMobile}
                className={({ isActive }) =>
                  `w-full flex items-center gap-3 px-3.5 py-2.5 rounded-[13px] text-[12px] font-medium transition-all ${
                    isActive
                      ? 'bg-[#fff0fb] text-[#a83f8b] font-semibold shadow-xs'
                      : 'text-[#8c8b9c] hover:text-[#2b2b3a] hover:bg-slate-50'
                  }`
                }
              >
                <Icon name={link.icon} className="w-[18px] h-[18px] shrink-0" />
                <span className="truncate">{link.label}</span>
              </NavLink>
            ))}
          </nav>
        </div>

        {/* Help Card from prototype: side-help */}
        <div className="mt-6 p-4 rounded-[17px] bg-gradient-to-br from-[#ffcef3] to-[#a1eafb] text-[#2b2b3a] shadow-xs">
          <strong className="block text-[12px] font-bold">Centro de ayuda</strong>
          <p className="text-[10px] text-[#554e5c] mt-0.5 leading-tight">
            ¿Necesitás asistencia técnica o soporte de facturación?
          </p>
          <a
            href="mailto:soporte@dulcemayor.com"
            className="inline-block text-[10px] font-bold text-[#2b2b3a] underline mt-2"
          >
            Contactar soporte
          </a>
        </div>
      </aside>
    </>
  );
}

