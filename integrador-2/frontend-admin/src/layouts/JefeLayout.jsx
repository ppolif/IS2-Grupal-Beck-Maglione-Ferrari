import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import SidebarBase from '../components/SidebarBase';
import Header from '../components/Header';

export default function JefeLayout({ onSwitchRole, onLogout }) {
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();

  const jefeLinks = [
    { to: '/dashboard', label: 'Dashboard', icon: 'home' },
    { to: '/estadisticas', label: 'Estadísticas', icon: 'chart' },
    { to: '/reportes', label: 'Reportes', icon: 'box' },
    { to: '/rendimiento', label: 'Rendimiento', icon: 'chart' },
    { to: '/usuarios', label: 'Usuarios', icon: 'user' },
    { to: '/minoristas', label: 'Clientes Minoristas', icon: 'user' },
    { to: '/stock', label: 'Stock Almacén', icon: 'chart' },
    { to: '/productos', label: 'Productos', icon: 'box' },
    { to: '/categorias', label: 'Categorías', icon: 'box' },
    { to: '/promociones', label: 'Promociones', icon: 'chart' },
    { to: '/facturas', label: 'Facturación', icon: 'box' },
    { to: '/pedidos', label: 'Pedidos', icon: 'box' },
    { to: '/pagos', label: 'Pagos', icon: 'chart' },
    { to: '/rutas', label: 'Rutas Distribución', icon: 'route' },
    { to: '/preventor-rutas', label: 'Supervisión Preventores', icon: 'route' },
    { to: '/cambiar-password', label: 'Cambiar Contraseña', icon: 'key' },
  ];

  const titles = {
    '/dashboard': 'Dashboard General',
    '/estadisticas': 'Estadísticas y Métricas',
    '/reportes': 'Reportes Gerenciales',
    '/rendimiento': 'Rendimiento de Preventores',
    '/usuarios': 'Usuarios del Sistema',
    '/minoristas': 'Padrón de Clientes Minoristas',
    '/stock': 'Inventario y Stock',
    '/productos': 'Catálogo de Productos',
    '/categorias': 'Categorías y Subcategorías',
    '/promociones': 'Códigos de Promoción',
    '/facturas': 'Comprobantes y Facturación',
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
        <div className="flex items-center justify-between pb-3">
          <Header
            title={currentTitle}
            onToggleMobileSidebar={() => setMobileOpen(true)}
          />

          {/* Selector de rol rápido & Salir para testing demo */}
          <div className="hidden sm:flex items-center gap-2">
            {onSwitchRole && (
              <button
                type="button"
                onClick={() => onSwitchRole('administrativo')}
                className="text-[11px] font-bold text-[#247f99] bg-[#eefaff] border border-[#a1eafb] px-3 py-1.5 rounded-full hover:bg-[#dff3fb] transition-colors cursor-pointer"
              >
                📋 Vista Administrativo
              </button>
            )}
            {onLogout && (
              <button
                type="button"
                onClick={onLogout}
                className="text-[11px] font-semibold text-[#6b6b80] hover:text-[#ff8fa3] px-2 py-1"
              >
                Cerrar sesión
              </button>
            )}
          </div>
        </div>

        <main className="flex-1">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
