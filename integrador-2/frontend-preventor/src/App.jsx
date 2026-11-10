import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import RoleRouter from './routes/RoleRouter';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <RoleRouter />
      </AuthProvider>
    </BrowserRouter>
  );
}
