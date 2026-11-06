import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import SidebarBase from '../components/SidebarBase';
import Header from '../components/Header';

export default function AdministrativoLayout({ onSwitchRole, onLogout }) {
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();

  const adminLinks = [
    { to: '/pedidos', label: 'Pedidos', icon: 'box' },
    { to: '/stock', label: 'Stock', icon: 'chart' },
    { to: '/productos', label: 'Productos', icon: 'box' },
    { to: '/categorias', label: 'Categorías', icon: 'box' },
    { to: '/promociones', label: 'Promociones', icon: 'chart' },
    { to: '/facturas', label: 'Facturación', icon: 'box' },
    { to: '/rutas', label: 'Rutas del día', icon: 'route' },
    { to: '/pagos', label: 'Pagos', icon: 'chart' },
    { to: '/cambiar-password', label: 'Cambiar Contraseña', icon: 'key' },
  ];

  const titles = {
    '/pedidos': 'Gestión de Pedidos',
    '/stock': 'Inventario y Stock',
    '/productos': 'Catálogo de Productos',
    '/categorias': 'Categorías y Subcategorías',
    '/promociones': 'Códigos de Promoción',
    '/facturas': 'Comprobantes y Facturación',
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
                onClick={() => onSwitchRole('jefe')}
                className="text-[11px] font-bold text-[#e86bc4] bg-[#ffe2f7] border border-[#ffcef3] px-3 py-1.5 rounded-full hover:bg-[#ffd5f4] transition-colors cursor-pointer"
              >
                👑 Vista Jefe
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
