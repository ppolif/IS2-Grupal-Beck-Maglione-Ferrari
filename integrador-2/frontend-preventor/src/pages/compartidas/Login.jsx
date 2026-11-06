import { useState } from 'react';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Logo from '../../components/Logo';

export default function Login({ onLoginSuccess }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      // Por defecto entra como preventor si no se especifica
      if (onLoginSuccess) {
        onLoginSuccess('preventor');
      }
    }, 600);
  };

  const handleQuickLogin = (role) => {
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      if (onLoginSuccess) {
        onLoginSuccess(role);
      }
    }, 400);
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

        {/* Formulario */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
              Correo Electrónico
            </label>
            <div className="relative">
              <Icon name="user" className="w-4 h-4 text-[#aaa9ba] absolute left-3.5 top-3" />
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="operaciones@dulcemayor.com"
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
                className="text-[11px] font-semibold text-[#aaa9ba] hover:text-[#2b2b3a] absolute right-3 top-3"
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

        {/* Acceso Rápido Demo (Pills de prueba) */}
        <div className="pt-4 border-t border-[#f0f0f4] space-y-2">
          <span className="text-[10px] text-[#aaa9ba] uppercase font-bold text-center block">
            Acceso Rápido Demo (Sin backend)
          </span>
          <div className="grid grid-cols-2 gap-2">
            <button
              type="button"
              onClick={() => handleQuickLogin('preventor')}
              className="p-2.5 bg-[#fef5fc] hover:bg-[#ffeefb] text-[#e86bc4] border border-[#ffcef3] rounded-[12px] text-[11px] font-bold text-center transition-all cursor-pointer"
            >
              💼 Modo Preventor
            </button>
            <button
              type="button"
              onClick={() => handleQuickLogin('distribuidor')}
              className="p-2.5 bg-[#f0faff] hover:bg-[#e6f6ff] text-[#247f99] border border-[#a1eafb] rounded-[12px] text-[11px] font-bold text-center transition-all cursor-pointer"
            >
              🚚 Modo Repartidor
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
