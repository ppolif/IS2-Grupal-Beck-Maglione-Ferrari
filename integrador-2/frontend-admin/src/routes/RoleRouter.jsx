import { useState } from 'react';
import { usuarioMock } from '../mocks/session';
import AdministrativoRoutes from './AdministrativoRoutes';
import JefeRoutes from './JefeRoutes';
import Login from '../pages/Login';

export default function RoleRouter() {
  const [activeRole, setActiveRole] = useState(usuarioMock.rol);
  const [isAuthenticated, setIsAuthenticated] = useState(true);

  if (!isAuthenticated) {
    return (
      <Login
        onLoginSuccess={(role) => {
          setActiveRole(role);
          setIsAuthenticated(true);
        }}
      />
    );
  }

  switch (activeRole) {
    case 'administrativo':
      return <AdministrativoRoutes onSwitchRole={(r) => setActiveRole(r)} onLogout={() => setIsAuthenticated(false)} />;
    case 'jefe':
      return <JefeRoutes onSwitchRole={(r) => setActiveRole(r)} onLogout={() => setIsAuthenticated(false)} />;
    default:
      return (
        <Login
          onLoginSuccess={(role) => {
            setActiveRole(role);
            setIsAuthenticated(true);
          }}
        />
      );
  }
}
