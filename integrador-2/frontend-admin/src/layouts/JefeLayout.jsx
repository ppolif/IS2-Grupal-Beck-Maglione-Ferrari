import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import SidebarBase from '../components/SidebarBase';
import Header from '../components/Header';

export default function JefeLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();

  // Links fijos escritos directamente en JSX según regla de la consigna
  const jefeLinks = [
    { to: '/dashboard', label: 'Dashboard', icon: 'home' },
    { to: '/estadisticas', label: 'Estadísticas', icon: 'chart' },
    { to: '/reportes', label: 'Reportes', icon: 'box' },
    { to: '/rendimiento', label: 'Rendimiento', icon: 'chart' },
    { to: '/usuarios', label: 'Usuarios', icon: 'user' },
    { to: '/stock', label: 'Stock Almacén', icon: 'chart' },
    { to: '/productos', label: 'Productos', icon: 'box' },
    { to: '/pedidos', label: 'Pedidos', icon: 'box' },
    { to: '/pagos', label: 'Pagos', icon: 'chart' },
    { to: '/rutas', label: 'Rutas de Distribución', icon: 'route' },
    { to: '/preventor-rutas', label: 'Supervisión Preventores', icon: 'route' },
    { to: '/cambiar-password', label: 'Cambiar Contraseña', icon: 'key' },
  ];

  const titles = {
    '/dashboard': 'Dashboard General',
    '/estadisticas': 'Estadísticas y Métricas',
    '/reportes': 'Reportes Gerenciales',
    '/rendimiento': 'Rendimiento de Preventores',
    '/usuarios': 'Usuarios del Sistema',
    '/stock': 'Inventario y Stock',
    '/productos': 'Catálogo de Productos',
    '/pedidos': 'Gestión de Pedidos',
    '/pagos': 'Registro de Pagos',
    '/rutas': 'Rutas del Día',
    '/preventor-rutas': 'Supervisión de Preventores',
    '/cambiar-password': 'Cambiar Contraseña',
  };

  const currentTitle = titles[location.pathname] || 'Panel de Jefatura';

  return (
    <div className="min-h-screen bg-[#f4f6fb] flex flex-col lg:flex-row">
      {/* Sidebar */}
      <SidebarBase
        links={jefeLinks}
        roleTitle="JEFATURA Y GESTIÓN"
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

