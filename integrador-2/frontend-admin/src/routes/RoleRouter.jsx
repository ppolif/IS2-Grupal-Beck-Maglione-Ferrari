import { useAuth } from '../context/AuthContext';
import Login from '../pages/Login';
import AdministrativoRoutes from './AdministrativoRoutes';
import JefeRoutes from './JefeRoutes';
import Button from '../components/Button';

export default function RoleRouter() {
  const { isLogged, usuario, esJefe, esAdministrativo, cargandoSesion, logout } = useAuth();

  // Pantalla de carga mientras se valida el token persistente
  if (cargandoSesion) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center p-6 bg-[#f4f6fb]">
        <div className="flex items-center gap-3 bg-white px-6 py-4 rounded-[20px] shadow-sm border border-[#ececf2]">
          <svg className="animate-spin h-5 w-5 text-[#e86bc4]" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
          </svg>
          <span className="text-[13px] font-semibold text-[#2b2b3a]">Validando sesión activa...</span>
        </div>
      </div>
    );
  }

  // Si no está autenticado, siempre renderiza la pantalla de Login
  if (!isLogged) {
    return <Login />;
  }

  // Ruteo basado en el rol real validado por JWT
  if (esJefe) {
    return <JefeRoutes />;
  }

  if (esAdministrativo) {
    return <AdministrativoRoutes />;
  }

  // Fallback de seguridad: Rol desconocido o no autorizado para esta app
  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-[#f4f6fb]">
      <div className="bg-white p-8 rounded-[24px] shadow-sm border border-[#ececf2] max-w-md text-center space-y-4">
        <div className="w-12 h-12 mx-auto rounded-full bg-[#ffe8ed] text-[#d63353] flex items-center justify-center font-bold text-lg">
          !
        </div>
        <h2 className="text-lg font-bold text-[#2b2b3a]">Rol de Usuario No Autorizado</h2>
        <p className="text-[12px] text-[#6b6b80]">
          El usuario <strong>{usuario?.nombreUsuario}</strong> tiene asignado el rol <strong>"{usuario?.rol}"</strong>, el cual no cuenta con permisos para operar en este panel de control.
        </p>
        <div className="pt-2">
          <Button onClick={logout} className="w-full">
            Cerrar sesión e intentar con otra cuenta
          </Button>
        </div>
      </div>
    </div>
  );
}
