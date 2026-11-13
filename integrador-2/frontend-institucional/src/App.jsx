import { BrowserRouter } from 'react-router-dom';
import AppRoutes from './routes/AppRoutes';
import { AuthProvider } from './context/AuthContext';
import { Auth0ProviderWithConfig } from './context/Auth0ProviderWithConfig';

export default function App() {
  return (
    <BrowserRouter>
      <Auth0ProviderWithConfig>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </Auth0ProviderWithConfig>
    </BrowserRouter>
  );
}
