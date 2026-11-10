/**
 * Pantalla de Perfil de Operador (Preventor / Distribuidor).
 * Consume los datos reales de sesión provistos por AuthContext.
 */
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Perfil() {
  const { userData, logout } = useAuth();
  const usuarioActivo = userData || {
    nombre: 'Operador',
    avatar: 'OP',
    rol: 'preventor',
    zona: 'Zona Asignada',
    unidad: 'Móvil Asignado',
    email: 'operador@dulcemayor.com',
  };
  const isPreventor = usuarioActivo.rol === 'preventor';

  return (
    <div className="max-w-md mx-auto p-4 sm:p-6 space-y-6">
      <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
        Mi Cuenta
      </span>
      <h1 className="text-2xl font-bold text-[#2b2b3a]">Perfil de Operador</h1>

      {/* Profile Card */}
      <div className="text-center bg-white p-6 sm:p-8 rounded-[20px] border border-[#ececf2] shadow-sm">
        <div className="w-[76px] h-[76px] rounded-full bg-gradient-to-br from-[#ffcef3] to-[#a1eafb] flex items-center justify-center mx-auto font-bold text-xl text-[#2b2b3a] shadow-md">
          {usuarioActivo.avatar}
        </div>
        <h2 className="text-xl font-bold text-[#2b2b3a] mt-3 mb-1">
          {usuarioActivo.nombre}
        </h2>
        <p className="text-[12px] text-[#6b6b80] font-medium">
          {isPreventor ? usuarioActivo.zona : usuarioActivo.unidad}
        </p>
        <p className="text-[11px] text-[#aaa9ba] mt-0.5">
          {usuarioActivo.email}
        </p>
      </div>

      {/* Options */}
      <div className="space-y-2">
        <Link
          to="/cambiar-password"
          className="bg-white border border-[#ececf2] p-4 rounded-[14px] text-[13px] font-semibold text-[#2b2b3a] flex items-center justify-between hover:bg-slate-50 transition-colors"
        >
          <span>Cambiar contraseña</span>
          <span className="text-[#6b6b80]">→</span>
        </Link>

        <button
          onClick={logout}
          className="w-full bg-white border border-[#ececf2] p-4 rounded-[14px] text-[13px] font-semibold text-[#c95569] flex items-center justify-between hover:bg-red-50/50 transition-colors cursor-pointer"
        >
          <span>Cerrar sesión</span>
          <span className="text-[#c95569]">→</span>
        </button>
      </div>

      <div className="p-4 bg-[#f8f9fc] rounded-[14px] border border-[#ececf2] text-[11px] text-[#6b6b80]">
        <strong className="text-[#2b2b3a] block mb-0.5">Rol en sesión:</strong>
        <span className="font-mono bg-white px-2 py-0.5 rounded border border-[#ececf2]">
          {usuarioActivo.rolOriginal || usuarioActivo.rol}
        </span>
      </div>
    </div>
  );
}
