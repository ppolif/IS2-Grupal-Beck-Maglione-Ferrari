import { useState } from 'react';
import { useAuth } from '../context/AuthContext';

/**
 * Custom hook handling login form submission, authentication state and quick-login fallbacks.
 */
export function useLoginForm() {
  const { login } = useAuth();
  const [nombreUsuario, setNombreUsuario] = useState('');
  const [clave, setClave] = useState('');
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!nombreUsuario.trim() || !clave) return;

    setError(null);
    setCargando(true);

    try {
      await login(nombreUsuario, clave);
      // La navegación la gestiona automáticamente RoleRouter al detectar isLogged
    } catch (err) {
      if (err.status === 404 || err.message === 'Not Found') {
        setError(
          'No se pudo encontrar el servicio de autenticación en el backend (Error 404). Asegurate de que el backend esté corriendo en el puerto 8080.'
        );
      } else {
        setError(err.message || 'Error al conectar con el servidor de autenticación');
      }
    } finally {
      setCargando(false);
    }
  };

  const handleQuickLogin = async (user, pass) => {
    setNombreUsuario(user);
    setClave(pass);
    setError(null);
    setCargando(true);

    try {
      await login(user, pass);
      return;
    } catch (err) {
      // Si la contraseña fue modificada previamente, intentamos con las claves de prueba habituales
      const alternates = [
        pass === 'dulcemayor' ? 'DulceMayor2026!' : 'dulcemayor',
        'AdminMayor2026!',
      ];
      for (const alt of alternates) {
        if (alt === pass) continue;
        try {
          setClave(alt);
          await login(user, alt);
          return;
        } catch {
          // continuar probando
        }
      }

      if (err.status === 404 || err.message === 'Not Found') {
        setError(
          'No se pudo encontrar el servicio de autenticación en el backend (Error 404). Asegurate de que el backend esté corriendo en el puerto 8080.'
        );
      } else {
        setError(err.message || 'Error al conectar con el servidor de autenticación');
      }
    } finally {
      setCargando(false);
    }
  };

  return {
    nombreUsuario,
    setNombreUsuario,
    clave,
    setClave,
    cargando,
    error,
    handleSubmit,
    handleQuickLogin,
  };
}
