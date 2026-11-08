import Icon from './Icon';
import { useAuth } from '../context/AuthContext';

export default function Header({ title, onToggleMobileSidebar }) {
  const { usuario, perfil, esJefe, logout } = useAuth();

  const nombreMostrar = perfil?.nombre
    ? `${perfil.nombre} ${perfil.apellido || ''}`.trim()
    : usuario?.nombreUsuario || 'Usuario';

  const rolLabel = esJefe ? 'Jefatura Comercial' : 'Personal Administrativo';

  // Generar iniciales para el avatar
  const iniciales = perfil?.nombre && perfil?.apellido
    ? `${perfil.nombre[0]}${perfil.apellido[0]}`.toUpperCase()
    : (usuario?.nombreUsuario?.slice(0, 2) || 'DM').toUpperCase();

  return (
    <header className="flex items-center justify-between gap-4 mb-6">
      <div className="flex items-center gap-3">
        {/* Mobile menu button */}
        <button
          type="button"
          onClick={onToggleMobileSidebar}
          className="lg:hidden p-2 rounded-[12px] bg-white border border-[#ececf2] text-[#2b2b3a] hover:bg-slate-50 transition-colors cursor-pointer"
          aria-label="Abrir menú"
        >
          <Icon name="menu" className="w-5 h-5" />
        </button>

        <div>
          <span className="text-[10px] text-[#9896a8] font-medium tracking-wide block">
            Dulce Mayor / {rolLabel} / {title}
          </span>
          <h1 className="text-2xl sm:text-[28px] font-bold text-[#2b2b3a] mt-0.5 leading-tight">
            {title}
          </h1>
        </div>
      </div>

      {/* Header Tools */}
      <div className="flex items-center gap-2.5 bg-white p-1.5 rounded-[30px] shadow-[0_14px_40px_rgba(56,52,84,0.08)] border border-[#ececf2]">
        <div className="hidden sm:flex items-center gap-2 bg-[#f5f6fa] text-[#a3a2ae] px-4 py-2 rounded-[22px] text-[11px] min-w-[180px]">
          <Icon name="search" className="w-3.5 h-3.5" />
          <input
            type="text"
            placeholder="Buscar en panel..."
            className="bg-transparent border-none outline-none text-[#2b2b3a] placeholder-[#a3a2ae] w-full text-[11px]"
          />
        </div>

        {/* User Info & Avatar */}
        <div className="flex items-center gap-2 pl-2 pr-1">
          <div
            className={`w-[36px] h-[36px] rounded-full font-bold text-[12px] flex items-center justify-center shadow-xs select-none ${
              esJefe
                ? 'bg-gradient-to-br from-[#ffcef3] to-[#ffd2b3] text-[#7a2262]'
                : 'bg-gradient-to-br from-[#d0f4ff] to-[#d6e2ff] text-[#1c5d75]'
            }`}
            title={`Conectado como ${nombreMostrar} (${rolLabel})`}
          >
            {iniciales}
          </div>

          <div className="hidden md:block text-left text-[11px] leading-tight pr-1">
            <strong className="block text-[#2b2b3a] font-semibold truncate max-w-[120px]">
              {nombreMostrar}
            </strong>
            <span className="text-[10px] text-[#8c8b9c]">{esJefe ? 'Jefe' : 'Admin'}</span>
          </div>
        </div>

        {/* Logout button */}
        <button
          type="button"
          onClick={logout}
          className="w-[32px] h-[32px] rounded-full hover:bg-[#ffe8ed] text-[#8c8b9c] hover:text-[#d63353] flex items-center justify-center transition-colors cursor-pointer"
          title="Cerrar sesión"
        >
          <Icon name="logout" className="w-4 h-4" />
        </button>
      </div>
    </header>
  );
}
