import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import Button from '../components/Button';
import Logo from '../components/Logo';
import PasswordInput from '../components/PasswordInput';
import { authService } from '../services/authService';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const location = useLocation();
  const navigate = useNavigate();
  const { loginConAuthResponse } = useAuth();

  // Si proviene del registro, precargar el email registrado
  const [email, setEmail] = useState(location.state?.registeredEmail || '');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const registroExitoso = Boolean(location.state?.registroExitoso);

  const handleLogin = async (e) => {
    e.preventDefault();
    setErrorMessage('');
    setLoading(true);

    try {
      const authRes = await authService.login(email.trim().toLowerCase(), password);

      // Verificar que el usuario sea del rol MINORISTA
      if (authRes?.usuario?.rol !== 'MINORISTA') {
        setErrorMessage('Este portal es de acceso exclusivo para clientes minoristas.');
        setLoading(false);
        return;
      }

      await loginConAuthResponse(authRes);

      // Redirigir a la ruta previa o a /perfil
      const destino = location.state?.from?.pathname || '/perfil';
      navigate(destino, { replace: true });
    } catch (err) {
      setErrorMessage(
        err.code === 'CREDENCIALES_INVALIDAS'
          ? 'Correo electrónico o contraseña incorrectos, o la cuenta fue dada de baja.'
          : (err.message || 'Error al iniciar sesión.')
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto my-8 px-4">
      <div className="bg-white rounded-[24px] p-8 border border-[#ececf2] shadow-[0_14px_40px_rgba(56,52,84,0.08)]">
        <div className="text-center mb-6">
          <div className="flex justify-center mb-3">
            <Logo />
          </div>
          <h1 className="text-2xl font-bold text-[#2b2b3a] mt-1">
            Iniciar Sesión
          </h1>
          <p className="text-[12px] text-[#6b6b80] mt-1">
            ¿Estás listo para comerte el blockazo?
          </p>
        </div>

        {registroExitoso && (
          <div className="mb-4 p-3.5 bg-[#dff8e9] text-[#39855b] rounded-[14px] text-[12px] flex items-start gap-2 border border-[#7ed6a5]/40 animate-in fade-in">
            <span className="font-bold">✓</span>
            <div>
              <p className="font-semibold">¡Cuenta creada con éxito!</p>
              <p className="text-[11px] opacity-90">Ingresá tu contraseña para acceder a tu panel.</p>
            </div>
          </div>
        )}

        {errorMessage && (
          <div className="mb-4 p-3 bg-[#ffe8ed] text-[#ff8fa3] rounded-[12px] text-[12px] font-medium border border-[#ff8fa3]/30">
            {errorMessage}
          </div>
        )}

        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label htmlFor="login-email" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
              Email registrado *
            </label>
            <input
              type="email"
              id="login-email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="tucomercio@email.com"
              autoComplete="username"
              spellCheck={false}
              disabled={loading}
              className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
            />
          </div>

          <div>
            <div className="flex justify-between items-center mb-1.5">
              <label htmlFor="login-pass" className="block text-[11px] font-semibold text-[#2b2b3a]">
                Contraseña *
              </label>
              <a href="#" className="text-[10px] text-[#3fb8d9] hover:underline">
                ¿Olvidaste tu contraseña?
              </a>
            </div>
            <PasswordInput
              id="login-pass"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              autoComplete="current-password"
              disabled={loading}
            />
          </div>

          <div className="flex items-center gap-2 pt-1">
            <input
              type="checkbox"
              id="remember"
              defaultChecked
              className="rounded accent-[#e86bc4]"
            />
            <label htmlFor="remember" className="text-[11px] text-[#6b6b80]">
              Recordar mis datos en este equipo
            </label>
          </div>

          <Button
            type="submit"
            disabled={loading}
            className="w-full py-3.5 text-[14px] justify-center mt-2"
          >
            {loading ? 'Iniciando sesión...' : 'Ingresar a mi cuenta'}
          </Button>
        </form>

        <div className="mt-6 pt-5 border-t border-[#f0f0f4] text-center text-[12px] text-[#6b6b80]">
          ¿Aún no tenés cuenta mayorista?{' '}
          <Link to="/registro" className="text-[#e86bc4] font-semibold hover:underline">
            Registrate acá
          </Link>
        </div>
      </div>
    </div>
  );
}
