import { Auth0Provider, Auth0Context, initialContext } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';

/**
 * Proveedor de Auth0 con validación de configuración.
 * Si las variables de entorno VITE_AUTH0_DOMAIN o VITE_AUTH0_CLIENT_ID no están configuradas,
 * monta un contexto de respaldo sin bloquear la ejecución ni la autenticación tradicional local.
 */
export function Auth0ProviderWithConfig({ children }) {
  const navigate = useNavigate();

  const domain = import.meta.env.VITE_AUTH0_DOMAIN;
  const clientId = import.meta.env.VITE_AUTH0_CLIENT_ID;
  const callbackUrl =
    import.meta.env.VITE_AUTH0_CALLBACK_URL || `${window.location.origin}/login`;
  const audience = import.meta.env.VITE_AUTH0_AUDIENCE;

  const isConfigured = Boolean(
    domain && clientId && domain.trim() !== '' && clientId.trim() !== ''
  );

  const onRedirectCallback = (appState) => {
    navigate(appState?.returnTo || '/perfil', { replace: true });
  };

  if (!isConfigured) {
    const unconfiguredContext = {
      ...initialContext,
      isLoading: false,
      isAuthenticated: false,
      isConfigured: false,
      loginWithRedirect: async () => {
        alert(
          'Auth0 no está configurado en las variables de entorno del frontend.\n\n' +
            'Por favor configure VITE_AUTH0_DOMAIN y VITE_AUTH0_CLIENT_ID en el archivo .env de frontend-institucional.'
        );
      },
      logout: () => {},
      getIdTokenClaims: async () => null,
      getAccessTokenSilently: async () => null,
    };

    return (
      <Auth0Context.Provider value={unconfiguredContext}>
        {children}
      </Auth0Context.Provider>
    );
  }

  return (
    <Auth0Provider
      domain={domain}
      clientId={clientId}
      authorizationParams={{
        redirect_uri: callbackUrl,
        scope: 'openid profile email',
        ...(audience ? { audience } : {}),
      }}
      cacheLocation="localstorage"
      useRefreshTokens={true}
      onRedirectCallback={onRedirectCallback}
    >
      {children}
    </Auth0Provider>
  );
}
