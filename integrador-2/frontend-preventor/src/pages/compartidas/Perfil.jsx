import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { usuarioMock } from '../../mocks/session';
import { recorridoService } from '../../services/recorridoService';
import Icon from '../../components/Icon';

export default function Perfil() {
  const navigate = useNavigate();
  const isPreventor = usuarioMock.rol === 'preventor';

  const [resumenRuta, setResumenRuta] = useState(() => recorridoService.obtenerResumen());

  useEffect(() => {
    const handleUpdate = () => {
      setResumenRuta(recorridoService.obtenerResumen());
    };
    window.addEventListener('dm-deliveries-updated', handleUpdate);
    return () => window.removeEventListener('dm-deliveries-updated', handleUpdate);
  }, []);

  return (
    <div className="max-w-md mx-auto p-4 sm:p-6 space-y-6">
      <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
        Mi Cuenta
      </span>
      <h1 className="text-2xl font-bold text-[#2b2b3a]">Perfil de Operador</h1>

      {/* Profile Card */}
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

        {/* Contadores: Entregados y Pendientes */}
        <div className="grid grid-cols-2 gap-4 mt-6 pt-5 border-t border-[#f0f0f4]">
          <div className="flex flex-col items-center">
            <b className="text-2xl font-bold text-[#2b2b3a]">
              {isPreventor ? '8' : resumenRuta.entregadosCount}
            </b>
            <span className="text-[11px] text-[#6b6b80] mt-0.5">
              {isPreventor ? 'Visitas Realizadas' : 'Pedidos Entregados'}
            </span>
          </div>
          <div className="flex flex-col items-center border-l border-[#f0f0f4]">
            <b className="text-2xl font-bold text-[#e86bc4]">
              {isPreventor ? '4' : resumenRuta.pendientesCount}
            </b>
            <span className="text-[11px] text-[#6b6b80] mt-0.5">
              {isPreventor ? 'Visitas Pendientes' : 'Pedidos Pendientes'}
            </span>
          </div>
        </div>

        {/* Contador exclusivo para el Distribuidor: Dinero recaudado en efectivo en su ruta actual */}
        {!isPreventor && (
          <div className="mt-5 p-4 bg-gradient-to-br from-[#f2fdf7] to-[#e6f9ef] border border-[#bbf7d0] rounded-[18px] text-center shadow-xs space-y-1">
            <div className="flex items-center justify-center gap-1.5 text-[#15803d]">
              <span className="w-2 h-2 rounded-full bg-[#22c55e] animate-pulse" />
              <span className="text-[10px] font-bold uppercase tracking-wider">
                Recaudación en Efectivo (Ruta Actual)
              </span>
            </div>

            <strong className="text-3xl font-extrabold text-[#166534] font-mono block">
              ${resumenRuta.recaudadoEfectivo.toLocaleString('es-AR')}
            </strong>

            <p className="text-[11px] text-[#3d7a55]">
              Efectivo cobrado en mano pendiente de rendición en tesorería.
            </p>

            {resumenRuta.pendienteEfectivo > 0 && (
              <div className="pt-2 mt-2 border-t border-[#bbf7d0]/70 flex items-center justify-between text-[11px] text-[#2c6343]">
                <span>Pendiente por cobrar en ruta:</span>
                <strong className="font-semibold">${resumenRuta.pendienteEfectivo.toLocaleString('es-AR')}</strong>
              </div>
            )}
          </div>
        )}
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
        (Podés cambiarlo a <code className="text-[#e86bc4]">"distribuidor"</code> o <code className="text-[#e86bc4]">"preventor"</code> en <code className="text-[#e86bc4]">src/mocks/session.js</code>)
      </div>
    </div>
  );
}
