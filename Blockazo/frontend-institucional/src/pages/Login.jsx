import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import Button from '../components/Button';
import Logo from '../components/Logo';
import PasswordInput from '../components/PasswordInput';
import { authService } from '../services/authService';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const location = useLocation();
  const navigate = useNavigate();
  const { loginConAuthResponse } = useAuth();
  const { loginWithPopup, getIdTokenClaims } = useAuth0();

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

  const handleSocialLogin = async () => {
    setErrorMessage('');
    setLoading(true);

    try {
      await loginWithPopup({
        authorizationParams: {
          connection: 'google-oauth2',
        },
      });

      const claims = await getIdTokenClaims();
      if (!claims || !claims.__raw) {
        throw new Error('No se pudo obtener el token de identidad desde Auth0.');
      }

      const authRes = await authService.loginSocial(claims.__raw);
      await loginConAuthResponse(authRes);

      const destino = location.state?.from?.pathname || '/perfil';
      navigate(destino, { replace: true });
    } catch (err) {
      if (err.message && !err.message.includes('popup_closed_by_user')) {
        setErrorMessage(err.message || 'Error al autenticarse con redes sociales.');
      }
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
            Ingresá a tu cuenta de Blockazo
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

        {/* Botón de Login con Google / Redes Sociales */}
        <button
          type="button"
          onClick={handleSocialLogin}
          disabled={loading}
          className="w-full flex items-center justify-center gap-3 py-3 px-4 rounded-[12px] border border-[#ececf2] hover:bg-[#f8f8fb] text-[#2b2b3a] text-[13px] font-medium transition cursor-pointer disabled:opacity-50 shadow-sm"
        >
          <svg className="w-4 h-4" viewBox="0 0 24 24">
            <path
              fill="#4285F4"
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
            />
            <path
              fill="#34A853"
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
            />
            <path
              fill="#FBBC05"
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
            />
            <path
              fill="#EA4335"
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
            />
          </svg>
          Continuar con Google
        </button>

        <div className="flex items-center my-5">
          <div className="flex-1 border-t border-[#ececf2]"></div>
          <span className="px-3 text-[11px] text-[#8e8ea0] uppercase tracking-wider font-medium">o ingresá con correo</span>
          <div className="flex-1 border-t border-[#ececf2]"></div>
        </div>

        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label htmlFor="login-email" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
              Correo Electrónico *
            </label>
            <input
              type="email"
              id="login-email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="tu@email.com"
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
            className="w-full py-3.5 text-[14px] justify-center mt-2 cursor-pointer"
          >
            {loading ? 'Iniciando sesión...' : 'Ingresar a mi cuenta'}
          </Button>
        </form>

        <div className="mt-6 pt-5 border-t border-[#f0f0f4] text-center text-[12px] text-[#6b6b80]">
          ¿Aún no tenés cuenta?{' '}
          <Link to="/registro" className="text-[#e86bc4] font-semibold hover:underline">
            Registrate acá
          </Link>
        </div>
      </div>
    </div>
  );
}
