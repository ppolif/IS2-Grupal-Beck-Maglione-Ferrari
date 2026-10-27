import { usuarioMock } from '../mocks/session';
import PreventorRoutes from './PreventorRoutes';
import DistribuidorRoutes from './DistribuidorRoutes';

// TODO: conectar con autenticación real
// Selector de interfaz según el rol del usuario autenticado
export default function RoleRouter() {
  const { rol } = usuarioMock;

  switch (rol) {
    case 'preventor':
      return <PreventorRoutes />;
    case 'distribuidor':
      return <DistribuidorRoutes />;
    default:
      return (
        <div className="min-h-screen flex items-center justify-center p-6 bg-[#edf0f8]">
          <div className="bg-white p-8 rounded-[20px] shadow-sm border border-[#ececf2] max-w-md text-center">
            <h2 className="text-xl font-bold text-[#ff8fa3]">Rol no autorizado</h2>
            <p className="text-[13px] text-[#6b6b80] mt-2">
              El rol "{rol}" no tiene acceso a esta aplicación. Configurá "preventor" o "distribuidor" en <code>src/mocks/session.js</code>.
            </p>
          </div>
        </div>
      );
  }
}

