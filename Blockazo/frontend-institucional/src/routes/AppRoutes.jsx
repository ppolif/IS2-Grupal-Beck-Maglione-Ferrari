import { Routes, Route, Navigate } from 'react-router-dom';
import ClienteLayout from '../layouts/ClienteLayout';
import Perfil from '../pages/Perfil';
import Login from '../pages/Login';
import Registro from '../pages/Registro';
import RutaProtegida from '../components/RutaProtegida';

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<ClienteLayout />}>
        <Route path="/" element={<Navigate to="/perfil" replace />} />
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
        <Route path="*" element={<Navigate to="/perfil" replace />} />
      </Route>
    </Routes>
  );
}

