import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('dm_admin_token'));
  const [usuario, setUsuario] = useState(() => {
    const raw = localStorage.getItem('dm_admin_usuario');
    return raw ? JSON.parse(raw) : null;
  });
  const [perfil, setPerfil] = useState(() => {
    const raw = localStorage.getItem('dm_admin_perfil');
    return raw ? JSON.parse(raw) : null;
  });
  const [debeCambiarClave, setDebeCambiarClave] = useState(() => {
    return localStorage.getItem('dm_admin_cambio_clave') === 'true';
  });
  const [cargandoSesion, setCargandoSesion] = useState(true);

  const logout = useCallback(() => {
    localStorage.removeItem('dm_admin_token');
    localStorage.removeItem('dm_admin_usuario');
    localStorage.removeItem('dm_admin_perfil');
    localStorage.removeItem('dm_admin_cambio_clave');
    setToken(null);
    setUsuario(null);
    setPerfil(null);
    setDebeCambiarClave(false);
  }, []);

  const refrescarPerfil = useCallback(async () => {
    if (!localStorage.getItem('dm_admin_token')) {
      setCargandoSesion(false);
      return null;
    }
    try {
      const data = await authService.obtenerPerfil();
      setPerfil(data);
      localStorage.setItem('dm_admin_perfil', JSON.stringify(data));
      return data;
    } catch {
      // Si falla por 401, el evento dm-admin-unauthorized ejecutará logout
      return null;
    } finally {
      setCargandoSesion(false);
    }
  }, []);

  const login = useCallback(
    async (nombreUsuario, clave) => {
      const authResponse = await authService.login(nombreUsuario, clave);
      const { token: nuevoToken, debeCambiarClave: cambioReq, usuario: usuarioResp } = authResponse;

      localStorage.setItem('dm_admin_token', nuevoToken);
      localStorage.setItem('dm_admin_usuario', JSON.stringify(usuarioResp));
      localStorage.setItem('dm_admin_cambio_clave', cambioReq ? 'true' : 'false');

      setToken(nuevoToken);
      setUsuario(usuarioResp);
      setDebeCambiarClave(Boolean(cambioReq));

      try {
        const perf = await authService.obtenerPerfil();
        setPerfil(perf);
        localStorage.setItem('dm_admin_perfil', JSON.stringify(perf));
      } catch {
        // En caso de que no tenga perfil extendido aún
      }

      return authResponse;
    },
    []
  );

  const cambiarClave = useCallback(async (claveActual, nuevaClave, confirmarClave) => {
    await authService.cambiarClave(claveActual, nuevaClave, confirmarClave);
    localStorage.setItem('dm_admin_cambio_clave', 'false');
    setDebeCambiarClave(false);
  }, []);

  useEffect(() => {
    const handleUnauthorized = () => logout();
    window.addEventListener('dm-admin-unauthorized', handleUnauthorized);
    refrescarPerfil();
    return () => window.removeEventListener('dm-admin-unauthorized', handleUnauthorized);
  }, [logout, refrescarPerfil]);

  const value = {
    token,
    usuario,
    perfil,
    isLogged: Boolean(token && usuario),
    esJefe: usuario?.rol === 'JEFE',
    esAdministrativo: usuario?.rol === 'ADMINISTRATIVO',
    debeCambiarClave,
    cargandoSesion,
    login,
    logout,
    cambiarClave,
    refrescarPerfil,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe ser utilizado dentro de un AuthProvider');
  }
  return context;
}
