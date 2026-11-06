import { Routes, Route, Navigate } from 'react-router-dom';
import AdministrativoLayout from '../layouts/AdministrativoLayout';
import Pedidos from '../pages/admin/Pedidos';
import Stock from '../pages/admin/Stock';
import Productos from '../pages/admin/Productos';
import Categorias from '../pages/admin/Categorias';
import Promociones from '../pages/admin/Promociones';
import Facturas from '../pages/admin/Facturas';
import Rutas from '../pages/admin/Rutas';
import Pagos from '../pages/admin/Pagos';
import CambiarPassword from '../pages/admin/CambiarPassword';

export default function AdministrativoRoutes({ onSwitchRole, onLogout }) {
  return (
    <Routes>
      <Route element={<AdministrativoLayout onSwitchRole={onSwitchRole} onLogout={onLogout} />}>
        <Route path="/" element={<Navigate to="/pedidos" replace />} />
        <Route path="/pedidos" element={<Pedidos />} />
        <Route path="/stock" element={<Stock />} />
        <Route path="/productos" element={<Productos />} />
        <Route path="/categorias" element={<Categorias />} />
        <Route path="/promociones" element={<Promociones />} />
        <Route path="/facturas" element={<Facturas />} />
        <Route path="/rutas" element={<Rutas />} />
        <Route path="/pagos" element={<Pagos />} />
        <Route path="/cambiar-password" element={<CambiarPassword />} />
        <Route path="*" element={<Navigate to="/pedidos" replace />} />
      </Route>
    </Routes>
  );
}
