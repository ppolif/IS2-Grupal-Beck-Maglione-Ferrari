import { usuarioMock } from '../mocks/session';
import AdministrativoRoutes from './AdministrativoRoutes';
import JefeRoutes from './JefeRoutes';

// TODO: conectar con autenticación real
// Selector de interfaz según el rol del usuario administrativo
export default function RoleRouter() {
  const { rol } = usuarioMock;

  switch (rol) {
    case 'administrativo':
      return <AdministrativoRoutes />;
    case 'jefe':
      return <JefeRoutes />;
    default:
      return (
        <div className="min-h-screen flex items-center justify-center p-6 bg-[#f4f6fb]">
          <div className="bg-white p-8 rounded-[20px] shadow-sm border border-[#ececf2] max-w-md text-center">
            <h2 className="text-xl font-bold text-[#ff8fa3]">Rol no autorizado</h2>
            <p className="text-[13px] text-[#6b6b80] mt-2">
              El rol "{rol}" no tiene acceso a esta aplicación administrativa. Configurá "administrativo" o "jefe" en <code>src/mocks/session.js</code>.
            </p>
          </div>
        </div>
      );
  }
}

