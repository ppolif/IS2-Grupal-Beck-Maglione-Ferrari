import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RutaProtegida({ children }) {
  const { isLogged, cargandoSesion, debeCambiarClave } = useAuth();
  const location = useLocation();

  if (cargandoSesion) {
    return (
      <div className="py-20 flex justify-center items-center">
        <div className="text-sm font-semibold text-[#6b6b80] flex items-center gap-2">
          <span className="w-4 h-4 border-2 border-[#e86bc4] border-t-transparent rounded-full animate-spin"></span>
          Cargando sesión...
        </div>
      </div>
    );
  }

  if (!isLogged) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (debeCambiarClave && location.pathname !== '/perfil') {
    return <Navigate to="/perfil?tab=seguridad" replace />;
  }

  return children;
}
