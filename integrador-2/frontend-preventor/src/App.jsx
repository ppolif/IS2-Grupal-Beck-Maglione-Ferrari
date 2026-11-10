import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import RoleRouter from './routes/RoleRouter';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <RoleRouter />
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
