import { Routes, Route, Navigate } from 'react-router-dom';
import DistribuidorLayout from '../layouts/DistribuidorLayout';
import Pedidos from '../pages/distribuidor/Pedidos';
import Recorrido from '../pages/distribuidor/Recorrido';
import Perfil from '../pages/compartidas/Perfil';
import CambiarPassword from '../pages/compartidas/CambiarPassword';

export default function DistribuidorRoutes() {
  return (
    <Routes>
      <Route element={<DistribuidorLayout />}>
        <Route path="/" element={<Navigate to="/pedidos" replace />} />
        <Route path="/pedidos" element={<Pedidos />} />
        <Route path="/recorrido" element={<Recorrido />} />
        <Route path="/perfil" element={<Perfil />} />
        <Route path="/cambiar-password" element={<CambiarPassword />} />
        <Route path="*" element={<Navigate to="/pedidos" replace />} />
      </Route>
    </Routes>
  );
}

