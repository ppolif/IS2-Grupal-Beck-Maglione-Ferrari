import PreventorRoutes from './PreventorRoutes';
import DistribuidorRoutes from './DistribuidorRoutes';
import Login from '../pages/compartidas/Login';
import { useAuth } from '../context/AuthContext';

export default function RoleRouter() {
  const { isAuthenticated, userData, logout, cargandoSesion } = useAuth();

  if (cargandoSesion) {
    return (
      <div className="min-h-screen bg-[#edf0f8] flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-3 border-[#e86bc4] border-t-transparent rounded-full animate-spin"></div>
          <span className="text-[12px] font-semibold text-[#6b6b80]">Cargando sesión...</span>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Login />;
  }

  const role = userData?.rol || 'preventor';

  switch (role) {
    case 'preventor':
      return <PreventorRoutes onLogout={logout} />;
    case 'distribuidor':
      return <DistribuidorRoutes onLogout={logout} />;
    default:
      return <Login />;
  }
}
