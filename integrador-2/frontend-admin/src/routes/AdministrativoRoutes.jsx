import { Routes, Route, Navigate } from 'react-router-dom';
import AdministrativoLayout from '../layouts/AdministrativoLayout';
import Pedidos from '../pages/admin/Pedidos';
import Stock from '../pages/admin/Stock';
import Productos from '../pages/admin/Productos';
import Rutas from '../pages/admin/Rutas';
import Pagos from '../pages/admin/Pagos';
import CambiarPassword from '../pages/admin/CambiarPassword';

export default function AdministrativoRoutes() {
  return (
    <Routes>
      <Route element={<AdministrativoLayout />}>
        <Route path="/" element={<Navigate to="/pedidos" replace />} />
        <Route path="/pedidos" element={<Pedidos />} />
        <Route path="/stock" element={<Stock />} />
        <Route path="/productos" element={<Productos />} />
        <Route path="/rutas" element={<Rutas />} />
        <Route path="/pagos" element={<Pagos />} />
        <Route path="/cambiar-password" element={<CambiarPassword />} />
        <Route path="*" element={<Navigate to="/pedidos" replace />} />
      </Route>
    </Routes>
  );
}

