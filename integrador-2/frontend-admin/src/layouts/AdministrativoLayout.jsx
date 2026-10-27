import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import SidebarBase from '../components/SidebarBase';
import Header from '../components/Header';

export default function AdministrativoLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();

  // Links fijos escritos directamente en JSX según regla de la consigna
  const adminLinks = [
    { to: '/pedidos', label: 'Pedidos', icon: 'box' },
    { to: '/stock', label: 'Stock', icon: 'chart' },
    { to: '/productos', label: 'Productos', icon: 'box' },
    { to: '/rutas', label: 'Rutas del día', icon: 'route' },
    { to: '/pagos', label: 'Pagos', icon: 'chart' },
    { to: '/cambiar-password', label: 'Cambiar Contraseña', icon: 'key' },
  ];

  // Helper title for breadcrumb
  const titles = {
    '/pedidos': 'Gestión de Pedidos',
    '/stock': 'Inventario y Stock',
    '/productos': 'Catálogo de Productos',
    '/rutas': 'Rutas del Día',
    '/pagos': 'Registro de Pagos',
    '/cambiar-password': 'Cambiar Contraseña',
  };

  const currentTitle = titles[location.pathname] || 'Panel Administrativo';

  return (
    <div className="min-h-screen bg-[#f4f6fb] flex flex-col lg:flex-row">
      {/* Sidebar */}
      <SidebarBase
        links={adminLinks}
        roleTitle="ALMACÉN Y ADMIN"
        mobileOpen={mobileOpen}
        onCloseMobile={() => setMobileOpen(false)}
      />

      {/* Main Area */}
      <div className="flex-1 flex flex-col p-4 sm:p-6 lg:p-8 min-w-0">
        <Header
          title={currentTitle}
          onToggleMobileSidebar={() => setMobileOpen(true)}
        />
        <main className="flex-1">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

