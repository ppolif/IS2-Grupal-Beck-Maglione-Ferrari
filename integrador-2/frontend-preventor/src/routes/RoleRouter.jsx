import { useState } from 'react';
import { usuarioMock } from '../mocks/session';
import PreventorRoutes from './PreventorRoutes';
import DistribuidorRoutes from './DistribuidorRoutes';
import Login from '../pages/compartidas/Login';

export default function RoleRouter() {
  // Manejo de estado de rol local interactivo para demos
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
    case 'preventor':
      return <PreventorRoutes onSwitchRole={(r) => setActiveRole(r)} onLogout={() => setIsAuthenticated(false)} />;
    case 'distribuidor':
      return <DistribuidorRoutes onSwitchRole={(r) => setActiveRole(r)} onLogout={() => setIsAuthenticated(false)} />;
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
