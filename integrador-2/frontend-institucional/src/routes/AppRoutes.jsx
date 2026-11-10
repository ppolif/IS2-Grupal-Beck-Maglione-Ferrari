import { Routes, Route, Navigate } from 'react-router-dom';
import ClienteLayout from '../layouts/ClienteLayout';
import Home from '../pages/Home';
import Tienda from '../pages/Tienda';
import Carrito from '../pages/Carrito';
import Checkout from '../pages/Checkout';
import PagoExito from '../pages/pago/PagoExito';
import PagoFallo from '../pages/pago/PagoFallo';
import PagoPendiente from '../pages/pago/PagoPendiente';
import Sucursales from '../pages/Sucursales';
import Contacto from '../pages/Contacto';
import Perfil from '../pages/Perfil';
import Login from '../pages/Login';
import Registro from '../pages/Registro';
import RutaProtegida from '../components/RutaProtegida';

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<ClienteLayout />}>
        <Route path="/" element={<Home />} />
        <Route path="/tienda" element={<Tienda />} />
        <Route path="/carrito" element={<Carrito />} />
        <Route
          path="/checkout"
          element={
            <RutaProtegida>
              <Checkout />
            </RutaProtegida>
          }
        />
        <Route path="/pago/exito" element={<PagoExito />} />
        <Route path="/pago/fallo" element={<PagoFallo />} />
        <Route path="/pago/pendiente" element={<PagoPendiente />} />
        <Route path="/sucursales" element={<Sucursales />} />
        <Route path="/contacto" element={<Contacto />} />
        <Route
          path="/perfil"
          element={
            <RutaProtegida>
              <Perfil />
            </RutaProtegida>
          }
        />
        <Route path="/login" element={<Login />} />
        <Route path="/registro" element={<Registro />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
