import { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export function esDniTemporal(numeroDocumento) {
  if (!numeroDocumento) return true;
  const doc = String(numeroDocumento).trim().toUpperCase();
  return (
    doc === '' ||
    doc.startsWith('EXT-') ||
    doc === 'AUTENTICADO' ||
    doc === 'SIN REGISTRAR'
  );
}

export function AuthProvider({ children }) {
  const {
    isAuthenticated: auth0IsAuthenticated,
    isLoading: auth0IsLoading,
    user: auth0User,
    loginWithRedirect: auth0LoginWithRedirect,
    logout: auth0Logout,
    getIdTokenClaims,
  } = useAuth0();

  const [token, setToken] = useState(() => localStorage.getItem('dm_institucional_token'));
  const [usuario, setUsuario] = useState(() => {
    const raw = localStorage.getItem('dm_institucional_usuario');
    return raw ? JSON.parse(raw) : null;
  });
  const [perfil, setPerfil] = useState(() => {
    const raw = localStorage.getItem('dm_institucional_perfil');
    return raw ? JSON.parse(raw) : null;
  });
  const [debeCambiarClave, setDebeCambiarClave] = useState(() => {
    const raw = localStorage.getItem('dm_institucional_usuario');
    if (!raw) return false;
    try {
      const u = JSON.parse(raw);
      return Boolean(u?.claveTemporal);
    } catch {
      return false;
    }
  });
  const [cargandoLocal, setCargandoLocal] = useState(true);
  const sincronizandoAuth0Ref = useRef(false);

  const logout = useCallback(() => {
    const eraAuth0 = Boolean(auth0IsAuthenticated || usuario?.proveedorAuth !== 'LOCAL');
    localStorage.removeItem('dm_institucional_token');
    localStorage.removeItem('dm_institucional_usuario');
    localStorage.removeItem('dm_institucional_perfil');
    setToken(null);
    setUsuario(null);
    setPerfil(null);
    setDebeCambiarClave(false);
    window.dispatchEvent(new Event('dm_cart_updated'));

    if (eraAuth0 && typeof auth0Logout === 'function') {
      try {
        auth0Logout({
          logoutParams: {
            returnTo: `${window.location.origin}/login`,
          },
        });
      } catch (err) {
        console.warn('Error al ejecutar logout en Auth0:', err);
      }
    }
  }, [auth0IsAuthenticated, usuario?.proveedorAuth, auth0Logout]);

  const refrescarPerfil = useCallback(async () => {
    if (!localStorage.getItem('dm_institucional_token')) {
      setCargandoLocal(false);
      return null;
    }
    try {
      const data = await authService.obtenerMiPerfil();
      setPerfil(data);
      localStorage.setItem('dm_institucional_perfil', JSON.stringify(data));
      return data;
    } catch {
      // Si falla por 401, el evento dm-auth-unauthorized ejecutará logout
      return null;
    } finally {
      setCargandoLocal(false);
    }
  }, []);

  const loginConAuthResponse = useCallback(
    async (authResponse) => {
      const { token: nuevoToken, debeCambiarClave: cambioReq, usuario: usuarioResp } = authResponse;
      if (nuevoToken) {
        localStorage.setItem('dm_institucional_token', nuevoToken);
        setToken(nuevoToken);
      }
      localStorage.setItem('dm_institucional_usuario', JSON.stringify(usuarioResp));
      setUsuario(usuarioResp);
      setDebeCambiarClave(Boolean(cambioReq));
      window.dispatchEvent(new Event('dm_cart_updated'));

      if (nuevoToken) {
        try {
          const perf = await authService.obtenerMiPerfil();
          setPerfil(perf);
          localStorage.setItem('dm_institucional_perfil', JSON.stringify(perf));
        } catch {
          // En caso de error, el perfil se cargará en el siguiente intento
        }
      }
    },
    []
  );

  // Sincronizar automáticamente la sesión de Auth0 cuando el usuario vuelve autenticado
  useEffect(() => {
    if (!auth0IsLoading && auth0IsAuthenticated && auth0User && !sincronizandoAuth0Ref.current) {
      const usuarioLocal = localStorage.getItem('dm_institucional_usuario');
      const parsedLocal = usuarioLocal ? JSON.parse(usuarioLocal) : null;

      // Si no hay usuario local o es un usuario distinto al de Auth0
      if (!parsedLocal || parsedLocal.nombreUsuario !== auth0User.email) {
        sincronizandoAuth0Ref.current = true;

        (async () => {
          let proveedor = 'GOOGLE';
          if (auth0User.sub?.toLowerCase().includes('facebook')) {
            proveedor = 'FACEBOOK';
          } else if (auth0User.sub?.toLowerCase().includes('google')) {
            proveedor = 'GOOGLE';
          }

          try {
            const claims = await getIdTokenClaims?.();
            const tokenExterno = claims?.__raw || '';

            if (tokenExterno) {
              const res = await authService.loginExterno(proveedor, tokenExterno);
              if (res) {
                await loginConAuthResponse(res);
                return;
              }
            }
            throw new Error('Sin respuesta del backend para login-externo');
          } catch (err) {
            console.info('Operando con perfil Auth0:', err.message || err);
            // Fallback elegante para frontend-institucional:
            const usuarioSocial = {
              id: auth0User.sub,
              nombreUsuario: auth0User.email,
              rol: 'MINORISTA',
              claveTemporal: false,
              proveedorAuth: proveedor,
              picture: auth0User.picture,
            };
            const perfilSocial = {
              id: auth0User.sub,
              nombre: auth0User.given_name || auth0User.name || 'Cliente',
              apellido: auth0User.family_name || '',
              tipoDocumento: 'DNI',
              numeroDocumento: 'Autenticado',
              contactos: [{ medio: 'CORREO', email: auth0User.email }],
            };

            localStorage.setItem('dm_institucional_usuario', JSON.stringify(usuarioSocial));
            localStorage.setItem('dm_institucional_perfil', JSON.stringify(perfilSocial));
            setUsuario(usuarioSocial);
            setPerfil(perfilSocial);
            setDebeCambiarClave(false);
            window.dispatchEvent(new Event('dm_cart_updated'));
          } finally {
            sincronizandoAuth0Ref.current = false;
            setCargandoLocal(false);
          }
        })();
      }
    }
  }, [auth0IsLoading, auth0IsAuthenticated, auth0User, getIdTokenClaims, loginConAuthResponse]);

  const loginConAuth0 = useCallback(
    async (connection) => {
      const redirectUri =
        import.meta.env.VITE_AUTH0_CALLBACK_URL || `${window.location.origin}/login`;

      const options = {
        authorizationParams: {
          redirect_uri: redirectUri,
        },
        appState: {
          returnTo: window.location.pathname || '/perfil',
        },
      };

      if (connection) {
        options.authorizationParams.connection = connection;
      }

      await auth0LoginWithRedirect(options);
    },
    [auth0LoginWithRedirect]
  );

  const sincronizarPerfilActualizado = useCallback((perfilActualizado, nuevoEmail) => {
    setPerfil(perfilActualizado);
    localStorage.setItem('dm_institucional_perfil', JSON.stringify(perfilActualizado));

    if (nuevoEmail) {
      setUsuario((prev) => {
        if (!prev) return prev;
        const actualizado = { ...prev, nombreUsuario: nuevoEmail };
        localStorage.setItem('dm_institucional_usuario', JSON.stringify(actualizado));
        return actualizado;
      });
    }
  }, []);

  const marcarClaveActualizada = useCallback(() => {
    setDebeCambiarClave(false);
    setUsuario((prev) => {
      if (!prev) return prev;
      const actualizado = { ...prev, claveTemporal: false };
      localStorage.setItem('dm_institucional_usuario', JSON.stringify(actualizado));
      return actualizado;
    });
  }, []);

  const completarDni = useCallback(
    async (numeroDocumento, confirmarNumeroDocumento) => {
      const dniLimpio = String(numeroDocumento || '')
        .replace(/\./g, '')
        .replace(/-/g, '')
        .trim();
      const confirmarLimpio = String(confirmarNumeroDocumento || '')
        .replace(/\./g, '')
        .replace(/-/g, '')
        .trim();

      if (token) {
        const actualizado = await authService.completarDni(dniLimpio, confirmarLimpio);
        setPerfil(actualizado);
        localStorage.setItem('dm_institucional_perfil', JSON.stringify(actualizado));
        return actualizado;
      }

      let actualizado = null;
      setPerfil((prev) => {
        actualizado = {
          ...(prev || {}),
          tipoDocumento: 'DNI',
          numeroDocumento: dniLimpio,
        };
        localStorage.setItem('dm_institucional_perfil', JSON.stringify(actualizado));
        return actualizado;
      });
      return actualizado;
    },
    [token]
  );

  useEffect(() => {
    const handleUnauthorized = () => logout();
    window.addEventListener('dm-auth-unauthorized', handleUnauthorized);
    refrescarPerfil();
    return () => window.removeEventListener('dm-auth-unauthorized', handleUnauthorized);
  }, [logout, refrescarPerfil]);

  const cargandoSesion = cargandoLocal || (auth0IsLoading && !usuario && !token);
  const isLogged = Boolean((token && usuario) || (usuario && usuario.proveedorAuth !== 'LOCAL'));
  const esUsuarioExterno = Boolean(
    usuario && usuario.proveedorAuth && usuario.proveedorAuth !== 'LOCAL'
  );
  const debeCompletarDni = Boolean(
    isLogged && esUsuarioExterno && (!perfil || esDniTemporal(perfil.numeroDocumento))
  );

  const value = {
    token,
    usuario,
    perfil,
    isLogged,
    debeCambiarClave,
    debeCompletarDni,
    cargandoSesion,
    auth0User,
    auth0IsAuthenticated,
    loginConAuthResponse,
    loginConAuth0,
    sincronizarPerfilActualizado,
    marcarClaveActualizada,
    completarDni,
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
