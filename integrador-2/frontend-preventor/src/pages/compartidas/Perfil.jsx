import { Link, useNavigate } from 'react-router-dom';
import { usuarioMock } from '../../mocks/session';

export default function Perfil() {
  const navigate = useNavigate();
  const isPreventor = usuarioMock.rol === 'preventor';

  return (
    <div className="max-w-md mx-auto p-4 sm:p-6 space-y-6">
      <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
        Mi Cuenta
      </span>
      <h1 className="text-2xl font-bold text-[#2b2b3a]">Perfil de Operador</h1>

      {/* Profile Card from prototype */}
      <div className="text-center bg-white p-6 sm:p-8 rounded-[20px] border border-[#ececf2] shadow-sm">
        <div className="w-[76px] h-[76px] rounded-full bg-gradient-to-br from-[#ffcef3] to-[#a1eafb] flex items-center justify-center mx-auto font-bold text-xl text-[#2b2b3a] shadow-md">
          {usuarioMock.avatar}
        </div>
        <h2 className="text-xl font-bold text-[#2b2b3a] mt-3 mb-1">
          {usuarioMock.nombre}
        </h2>
        <p className="text-[12px] text-[#6b6b80] font-medium">
          {isPreventor ? usuarioMock.zona : usuarioMock.unidad}
        </p>

        {/* Stat pair */}
        <div className="grid grid-cols-2 gap-4 mt-6 pt-5 border-t border-[#f0f0f4]">
          <div className="flex flex-col items-center">
            <b className="text-2xl font-bold text-[#2b2b3a]">8</b>
            <span className="text-[11px] text-[#6b6b80] mt-0.5">
              {isPreventor ? 'Visitas Realizadas' : 'Entregas Completadas'}
            </span>
          </div>
          <div className="flex flex-col items-center border-l border-[#f0f0f4]">
            <b className="text-2xl font-bold text-[#e86bc4]">4</b>
            <span className="text-[11px] text-[#6b6b80] mt-0.5">Pendientes Hoy</span>
          </div>
        </div>
      </div>

      {/* Options from prototype */}
      <div className="space-y-2">
        <Link
          to="/cambiar-password"
          className="bg-white border border-[#ececf2] p-4 rounded-[14px] text-[13px] font-semibold text-[#2b2b3a] flex items-center justify-between hover:bg-slate-50 transition-colors"
        >
          <span>Cambiar contraseña</span>
          <span className="text-[#6b6b80]">→</span>
        </Link>

        <button
          onClick={() => alert('Sesión cerrada (Simulado)')}
          className="w-full bg-white border border-[#ececf2] p-4 rounded-[14px] text-[13px] font-semibold text-[#c95569] flex items-center justify-between hover:bg-red-50/50 transition-colors cursor-pointer"
        >
          <span>Cerrar sesión</span>
          <span className="text-[#c95569]">→</span>
        </button>
      </div>

      <div className="p-4 bg-[#f8f9fc] rounded-[14px] border border-[#ececf2] text-[11px] text-[#6b6b80]">
        <strong className="text-[#2b2b3a] block mb-0.5">Rol activo en sesión:</strong>
        <span className="font-mono bg-white px-2 py-0.5 rounded border border-[#ececf2]">
          {usuarioMock.rol}
        </span>{' '}
        (Podés cambiarlo en <code className="text-[#e86bc4]">src/mocks/session.js</code>)
      </div>
    </div>
  );
}

