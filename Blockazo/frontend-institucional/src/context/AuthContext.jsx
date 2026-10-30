import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('blockazo_token'));
  const [usuario, setUsuario] = useState(() => {
    const raw = localStorage.getItem('blockazo_usuario');
    return raw ? JSON.parse(raw) : null;
  });
  const [perfil, setPerfil] = useState(() => {
    const raw = localStorage.getItem('blockazo_perfil');
    return raw ? JSON.parse(raw) : null;
  });
  const [debeCambiarClave, setDebeCambiarClave] = useState(false);
  const [cargandoSesion, setCargandoSesion] = useState(true);

  const logout = useCallback(() => {
    localStorage.removeItem('blockazo_token');
    localStorage.removeItem('blockazo_usuario');
    localStorage.removeItem('blockazo_perfil');
    setToken(null);
    setUsuario(null);
    setPerfil(null);
    setDebeCambiarClave(false);
  }, []);

  const refrescarPerfil = useCallback(async () => {
    if (!localStorage.getItem('blockazo_token')) {
      setCargandoSesion(false);
      return null;
    }
    try {
      const data = await authService.obtenerMiPerfil();
      setPerfil(data);
      localStorage.setItem('blockazo_perfil', JSON.stringify(data));
      return data;
    } catch {
      return null;
    } finally {
      setCargandoSesion(false);
    }
  }, []);

  const loginConAuthResponse = useCallback(
    async (authResponse) => {
      const { token: nuevoToken, debeCambiarClave: cambioReq, usuario: usuarioResp } = authResponse;
      localStorage.setItem('blockazo_token', nuevoToken);
      localStorage.setItem('blockazo_usuario', JSON.stringify(usuarioResp));
      setToken(nuevoToken);
      setUsuario(usuarioResp);
      setDebeCambiarClave(Boolean(cambioReq));

      try {
        const perf = await authService.obtenerMiPerfil();
        setPerfil(perf);
        localStorage.setItem('blockazo_perfil', JSON.stringify(perf));
      } catch {
        // En caso de error, el perfil se cargará en el siguiente intento
      }
    },
    []
  );

  const sincronizarPerfilActualizado = useCallback((perfilActualizado, nuevoEmail) => {
    setPerfil(perfilActualizado);
    localStorage.setItem('blockazo_perfil', JSON.stringify(perfilActualizado));

    if (nuevoEmail) {
      setUsuario((prev) => {
        if (!prev) return prev;
        const actualizado = { ...prev, nombreUsuario: nuevoEmail };
        localStorage.setItem('blockazo_usuario', JSON.stringify(actualizado));
        return actualizado;
      });
    }
  }, []);

  const marcarClaveActualizada = useCallback(() => {
    setDebeCambiarClave(false);
  }, []);

  useEffect(() => {
    const handleUnauthorized = () => logout();
    window.addEventListener('dm-auth-unauthorized', handleUnauthorized);
    refrescarPerfil();
    return () => window.removeEventListener('dm-auth-unauthorized', handleUnauthorized);
  }, [logout, refrescarPerfil]);

  const value = {
    token,
    usuario,
    perfil,
    isLogged: Boolean(token && usuario),
    debeCambiarClave,
    cargandoSesion,
    loginConAuthResponse,
    sincronizarPerfilActualizado,
    marcarClaveActualizada,
    refrescarPerfil,
    logout,
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
