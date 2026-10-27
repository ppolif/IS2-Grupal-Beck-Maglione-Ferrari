import { Routes, Route, Navigate } from 'react-router-dom';
import PreventorLayout from '../layouts/PreventorLayout';
import Recorrido from '../pages/preventor/Recorrido';
import RegistroComercio from '../pages/preventor/RegistroComercio';
import Tienda from '../pages/preventor/Tienda';
import Carrito from '../pages/preventor/Carrito';
import Perfil from '../pages/compartidas/Perfil';
import CambiarPassword from '../pages/compartidas/CambiarPassword';

export default function PreventorRoutes() {
  return (
    <Routes>
      <Route element={<PreventorLayout />}>
        <Route path="/" element={<Navigate to="/recorrido" replace />} />
        <Route path="/recorrido" element={<Recorrido />} />
        <Route path="/registro-comercio" element={<RegistroComercio />} />
        <Route path="/tienda" element={<Tienda />} />
        <Route path="/carrito" element={<Carrito />} />
        <Route path="/perfil" element={<Perfil />} />
        <Route path="/cambiar-password" element={<CambiarPassword />} />
        <Route path="*" element={<Navigate to="/recorrido" replace />} />
      </Route>
    </Routes>
  );
}

