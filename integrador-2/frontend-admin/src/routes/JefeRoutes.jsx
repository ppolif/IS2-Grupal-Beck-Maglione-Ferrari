import { Routes, Route, Navigate } from 'react-router-dom';
import JefeLayout from '../layouts/JefeLayout';

// Vistas exclusivas de Jefatura
import Dashboard from '../pages/jefe/Dashboard';
import Estadisticas from '../pages/jefe/Estadisticas';
import Reportes from '../pages/jefe/Reportes';
import Rendimiento from '../pages/jefe/Rendimiento';
import Usuarios from '../pages/jefe/Usuarios';
import Minoristas from '../pages/jefe/Minoristas';
import PreventorRutas from '../pages/jefe/PreventorRutas';

// Vistas operativas y comerciales
import Stock from '../pages/admin/Stock';
import Productos from '../pages/admin/Productos';
import Categorias from '../pages/admin/Categorias';
import Promociones from '../pages/admin/Promociones';
import Facturas from '../pages/admin/Facturas';
import Pedidos from '../pages/admin/Pedidos';
import Pagos from '../pages/admin/Pagos';
import Rutas from '../pages/admin/Rutas';
import CambiarPassword from '../pages/admin/CambiarPassword';

export default function JefeRoutes({ onSwitchRole, onLogout }) {
  return (
    <Routes>
      <Route element={<JefeLayout onSwitchRole={onSwitchRole} onLogout={onLogout} />}>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        {/* Vistas exclusivas */}
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/estadisticas" element={<Estadisticas />} />
        <Route path="/reportes" element={<Reportes />} />
        <Route path="/rendimiento" element={<Rendimiento />} />
        <Route path="/usuarios" element={<Usuarios />} />
        <Route path="/minoristas" element={<Minoristas />} />
        <Route path="/preventor-rutas" element={<PreventorRutas />} />

        {/* Vistas operativas heredadas/reutilizadas */}
        <Route path="/stock" element={<Stock />} />
        <Route path="/productos" element={<Productos />} />
        <Route path="/categorias" element={<Categorias />} />
        <Route path="/promociones" element={<Promociones />} />
        <Route path="/facturas" element={<Facturas />} />
        <Route path="/pedidos" element={<Pedidos />} />
        <Route path="/pagos" element={<Pagos />} />
        <Route path="/rutas" element={<Rutas />} />
        <Route path="/cambiar-password" element={<CambiarPassword />} />

        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Route>
    </Routes>
  );
}
