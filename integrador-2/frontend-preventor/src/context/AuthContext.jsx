import { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('dm_preventor_token'));
  const [usuario, setUsuario] = useState(() => {
    const raw = localStorage.getItem('dm_preventor_usuario');
    return raw ? JSON.parse(raw) : null;
  });
  const [perfil, setPerfil] = useState(() => {
    const raw = localStorage.getItem('dm_preventor_perfil');
    return raw ? JSON.parse(raw) : null;
  });
  const [cargandoSesion, setCargandoSesion] = useState(true);

  const logout = useCallback(() => {
    localStorage.removeItem('dm_preventor_token');
    localStorage.removeItem('dm_preventor_usuario');
    localStorage.removeItem('dm_preventor_perfil');
    setToken(null);
    setUsuario(null);
    setPerfil(null);
  }, []);

  const refrescarPerfil = useCallback(async () => {
    if (!localStorage.getItem('dm_preventor_token')) {
      setCargandoSesion(false);
      return null;
    }
    try {
      const data = await authService.obtenerPerfil();
      setPerfil(data);
      localStorage.setItem('dm_preventor_perfil', JSON.stringify(data));
      return data;
    } catch {
      return null;
    } finally {
      setCargandoSesion(false);
    }
  }, []);

  const login = useCallback(
    async (nombreUsuario, clave) => {
      const authResponse = await authService.login(nombreUsuario, clave);
      const { token: nuevoToken, usuario: usuarioResp } = authResponse;

      localStorage.setItem('dm_preventor_token', nuevoToken);
      localStorage.setItem('dm_preventor_usuario', JSON.stringify(usuarioResp));

      setToken(nuevoToken);
      setUsuario(usuarioResp);

      try {
        const perf = await authService.obtenerPerfil();
        setPerfil(perf);
        localStorage.setItem('dm_preventor_perfil', JSON.stringify(perf));
      } catch {
        // En caso de que no tenga perfil extendido aún
      }

      return authResponse;
    },
    []
  );

  useEffect(() => {
    const handleUnauthorized = () => {
      logout();
    };

    window.addEventListener('dm-preventor-unauthorized', handleUnauthorized);
    return () => {
      window.removeEventListener('dm-preventor-unauthorized', handleUnauthorized);
    };
  }, [logout]);

  useEffect(() => {
    if (token) {
      refrescarPerfil();
    } else {
      setCargandoSesion(false);
    }
  }, [token, refrescarPerfil]);

  // Helpers para mostrar nombre y avatar
  const userData = useMemo(() => {
    if (!usuario) return null;
    const nombreCompleto = perfil?.nombre && perfil?.apellido
      ? `${perfil.nombre} ${perfil.apellido}`
      : usuario.nombreUsuario || 'Usuario';

    let avatar = 'OP';
    if (perfil?.nombre && perfil?.apellido) {
      avatar = `${perfil.nombre[0]}${perfil.apellido[0]}`.toUpperCase();
    } else if (usuario.nombreUsuario) {
      avatar = usuario.nombreUsuario.slice(0, 2).toUpperCase();
    }

    // Normalización de rol a minúsculas para coincidir con las rutas: 'preventor' o 'distribuidor'
    const rolNormalizado = usuario.rol === 'PREVENTISTA' || usuario.rol === 'preventor'
      ? 'preventor'
      : 'distribuidor';

    return {
      id: usuario.id,
      nombre: nombreCompleto,
      email: usuario.email || `${usuario.nombreUsuario}@dulcemayor.com`,
      avatar,
      rol: rolNormalizado,
      rolOriginal: usuario.rol,
      zona: 'Zona Asignada',
      unidad: 'Móvil de Reparto'
    };
  }, [usuario, perfil]);

  const value = {
    token,
    usuario,
    perfil,
    userData,
    cargandoSesion,
    isAuthenticated: Boolean(token),
    login,
    logout,
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
