import Icon from './Icon';
import { usuarioMock } from '../mocks/session';

export default function Header({ title, onToggleMobileSidebar }) {
  return (
    <header className="flex items-center justify-between gap-4 mb-6">
      <div className="flex items-center gap-3">
        {/* Mobile menu button */}
        <button
          type="button"
          onClick={onToggleMobileSidebar}
          className="lg:hidden p-2 rounded-[12px] bg-white border border-[#ececf2] text-[#2b2b3a] hover:bg-slate-50 transition-colors"
          aria-label="Abrir menú"
        >
          <Icon name="menu" className="w-5 h-5" />
        </button>

        <div>
          <span className="text-[10px] text-[#9896a8] font-medium tracking-wide block">
            Dulce Mayor / {usuarioMock.rol === 'jefe' ? 'Jefatura' : 'Administración'} / {title}
          </span>
          <h1 className="text-2xl sm:text-[28px] font-bold text-[#2b2b3a] mt-0.5 leading-tight">
            {title}
          </h1>
        </div>
      </div>

      {/* Header Tools matching prototype header-tools */}
      <div className="flex items-center gap-2.5 bg-white p-1.5 rounded-[30px] shadow-[0_14px_40px_rgba(56,52,84,0.08)] border border-[#ececf2]">
        <div className="hidden sm:flex items-center gap-2 bg-[#f5f6fa] text-[#a3a2ae] px-4 py-2 rounded-[22px] text-[11px] min-w-[180px]">
          <Icon name="search" className="w-3.5 h-3.5" />
          <input
            type="text"
            placeholder="Buscar en panel..."
            className="bg-transparent border-none outline-none text-[#2b2b3a] placeholder-[#a3a2ae] w-full text-[11px]"
          />
        </div>

        <button
          type="button"
          className="w-[30px] h-[30px] rounded-full hover:bg-slate-100 flex items-center justify-center text-[#6b6b80] transition-colors"
          title="Notificaciones"
        >
          <Icon name="bell" className="w-4 h-4" />
        </button>

        <div
          className="w-[36px] h-[36px] rounded-full bg-gradient-to-br from-[#ffcef3] to-[#a1eafb] text-[#2b2b3a] font-bold text-[12px] flex items-center justify-center shadow-sm select-none"
          title={`Conectado como ${usuarioMock.nombre} (${usuarioMock.rol})`}
        >
          {usuarioMock.avatar}
        </div>
      </div>
    </header>
  );
}

