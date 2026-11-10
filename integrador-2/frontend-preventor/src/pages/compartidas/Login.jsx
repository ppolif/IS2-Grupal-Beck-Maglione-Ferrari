import { useState } from 'react';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Logo from '../../components/Logo';
import { useAuth } from '../../context/AuthContext';

export default function Login() {
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    try {
      await login(username, password);
    } catch (err) {
      console.error('Error al iniciar sesión:', err);
      setError(err.message || 'Credenciales inválidas. Por favor verificá tus datos.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#edf0f8] flex items-center justify-center p-4">
      <div className="w-full max-w-sm bg-white rounded-[26px] p-6 sm:p-8 shadow-[0_20px_60px_rgba(37,35,55,0.12)] border border-[#ececf2] space-y-6">
        {/* Header Branding */}
        <div className="text-center space-y-2">
          <div className="flex justify-center mb-1">
            <Logo />
          </div>
          <span className="text-[10px] font-bold tracking-[0.14em] uppercase text-[#e86bc4] bg-[#ffe2f7] px-3 py-0.5 rounded-full inline-block">
            Portal Operativo Móvil
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a]">
            Iniciar Sesión
          </h1>
          <p className="text-[12px] text-[#6b6b80]">
            Acceso exclusivo para Preventores y Distribuidores de Dulce Mayor.
          </p>
        </div>

        {error && (
          <div className="p-3 bg-[#ffe8ed] border border-[#ffccd5] rounded-[12px] text-[12px] text-[#e5484d] flex items-start gap-2">
            <Icon name="info" className="w-4 h-4 flex-shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        {/* Formulario */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
              Usuario o Correo Electrónico
            </label>
            <div className="relative">
              <Icon name="user" className="w-4 h-4 text-[#aaa9ba] absolute left-3.5 top-3" />
              <input
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="operaciones o correo@dulcemayor.com"
                className="w-full border border-[#ececf2] rounded-[12px] pl-10 pr-3 py-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
              Contraseña
            </label>
            <div className="relative">
              <input
                type={showPassword ? 'text' : 'password'}
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full border border-[#ececf2] rounded-[12px] px-3 py-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="text-[11px] font-semibold text-[#aaa9ba] hover:text-[#2b2b3a] absolute right-3 top-3 cursor-pointer"
              >
                {showPassword ? 'Ocultar' : 'Ver'}
              </button>
            </div>
          </div>

          <Button
            type="submit"
            disabled={isLoading}
            className="w-full py-3 text-[13px] justify-center shadow-md cursor-pointer"
          >
            {isLoading ? 'Ingresando...' : 'Iniciar Sesión'}
          </Button>
        </form>
      </div>
    </div>
  );
}
