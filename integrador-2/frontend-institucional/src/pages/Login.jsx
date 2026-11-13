import { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import Button from '../components/Button';
import Logo from '../components/Logo';
import PasswordInput from '../components/PasswordInput';
import { authService } from '../services/authService';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const location = useLocation();
  const navigate = useNavigate();
  const { loginConAuthResponse, loginConAuth0, isLogged, cargandoSesion, debeCompletarDni } = useAuth();

  // Si proviene del registro, precargar el email registrado
  const [email, setEmail] = useState(location.state?.registeredEmail || '');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [loadingSocial, setLoadingSocial] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const registroExitoso = Boolean(location.state?.registroExitoso);

  // Redirigir si la sesión ya está activa (por ejemplo, al retornar de Auth0)
  useEffect(() => {
    if (isLogged && !cargandoSesion) {
      if (debeCompletarDni) {
        navigate('/completar-dni', { replace: true });
        return;
      }
      const destino = location.state?.from?.pathname || '/perfil';
      navigate(destino, { replace: true });
    }
  }, [isLogged, cargandoSesion, debeCompletarDni, navigate, location.state]);

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

  const handleSocialLogin = async (connection) => {
    setErrorMessage('');
    setLoadingSocial(true);
    try {
      await loginConAuth0(connection);
    } catch (err) {
      setErrorMessage(err.message || 'No se pudo iniciar el flujo de autenticación social.');
      setLoadingSocial(false);
    }
  };

  return (
    <div className="max-w-md mx-auto my-8 px-4">
      <div className="bg-white rounded-[24px] p-8 border border-[#ececf2] shadow-[0_14px_40px_rgba(56,52,84,0.08)]">
        <div className="text-center mb-6">
          <div className="flex justify-center mb-3">
            <Logo />
          </div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Portal Clientes Mayoristas
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a] mt-1">
            Iniciar Sesión
          </h1>
          <p className="text-[12px] text-[#6b6b80] mt-1">
            Accedé a tus listas de precios exclusivas y pedidos en curso.
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

        {/* Botón de inicio de sesión con Google (Auth0) */}
        <div className="mb-5">
          <button
            type="button"
            disabled={loading || loadingSocial}
            onClick={() => handleSocialLogin('google-oauth2')}
            className="w-full flex items-center justify-center gap-3 py-3 px-4 rounded-[14px] border border-[#d8d8e5] bg-white hover:bg-slate-50 transition-all font-semibold text-[13px] text-[#2b2b3a] shadow-xs hover:border-[#b8b8cc] cursor-pointer disabled:opacity-50"
          >
            <svg className="w-4 h-4 shrink-0" viewBox="0 0 24 24">
              <path
                fill="#4285F4"
                d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.82-2.4 3.68v3.05h3.88c2.27-2.09 3.66-5.17 3.66-9.17z"
              />
              <path
                fill="#34A853"
                d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.1-6.72-4.93H1.25v3.15C3.26 21.36 7.34 24 12 24z"
              />
              <path
                fill="#FBBC05"
                d="M5.28 14.27c-.25-.72-.38-1.49-.38-2.27s.13-1.55.38-2.27V6.58H1.25C.45 8.18 0 9.99 0 12s.45 3.82 1.25 5.42l4.03-3.15z"
              />
              <path
                fill="#EA4335"
                d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.34 0 3.26 2.64 1.25 6.58l4.03 3.15c.95-2.83 3.6-4.98 6.72-4.98z"
              />
            </svg>
            <span>Continuar con Google</span>
          </button>
        </div>

        {/* Divisor */}
        <div className="relative my-5">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-[#ececf2]" />
          </div>
          <div className="relative flex justify-center text-[10px] uppercase">
            <span className="bg-white px-3 text-[#9b9ba8] font-bold tracking-wider">
              o con tu correo
            </span>
          </div>
        </div>

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
              disabled={loading || loadingSocial}
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
              disabled={loading || loadingSocial}
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
            disabled={loading || loadingSocial}
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
