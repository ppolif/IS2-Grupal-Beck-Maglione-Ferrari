import { useState } from 'react';
import Button from '../components/Button';
import Icon from '../components/Icon';
import Logo from '../components/Logo';

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
      if (onLoginSuccess) {
        onLoginSuccess('administrativo');
      }
    }, 500);
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
    <div className="min-h-screen bg-[#f4f6fb] flex items-center justify-center p-4 sm:p-6">
      <div className="w-full max-w-4xl bg-white rounded-[26px] shadow-[0_20px_60px_rgba(37,35,55,0.12)] border border-[#ececf2] overflow-hidden grid grid-cols-1 md:grid-cols-12 min-h-[520px]">

        {/* Panel Izquierdo: Branding & Contexto (5 cols) */}
        <div className="md:col-span-5 bg-gradient-to-br from-[#2b2b3a] to-[#1c1b26] p-8 text-white flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-6">
              <span className="candy-mark"><i /><b /></span>
              <span className="font-bold text-xl tracking-tight text-white">Dulce Mayor</span>
            </div>

            <span className="text-[10px] font-bold uppercase tracking-wider text-[#e86bc4] bg-white/10 px-3 py-1 rounded-full">
              Panel Administrativo
            </span>

            <h2 className="text-2xl font-bold mt-4 leading-tight">
              Gestión Integral y Distribución Mayorista
            </h2>

            <p className="text-[12px] text-[#aaa9ba] mt-3 leading-relaxed">
              Sistema centralizado para control de stock en almacén, recepción y facturación de pedidos, tesorería y supervisión logística de Mendoza.
            </p>
          </div>

          <div className="pt-6 border-t border-white/10 text-[11px] text-[#8e8d9e] space-y-1">
            <p>✓ Arquitectura 3 Capas · Spring Boot & React</p>
            <p>✓ Autenticación segura mediante JWT</p>
          </div>
        </div>

        {/* Panel Derecho: Formulario (7 cols) */}
        <div className="md:col-span-7 p-8 sm:p-10 flex flex-col justify-center space-y-6">
          <div>
            <h1 className="text-2xl font-bold text-[#2b2b3a]">
              Ingreso al Sistema
            </h1>
            <p className="text-[12px] text-[#6b6b80] mt-1">
              Ingresá tus credenciales de empleado para acceder al panel.
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Usuario o Correo Institucional
              </label>
              <div className="relative">
                <Icon name="user" className="w-4 h-4 text-[#aaa9ba] absolute left-3.5 top-3" />
                <input
                  type="text"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="admin@dulcemayor.com"
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
              {isLoading ? 'Verificando...' : 'Acceder al Dashboard'}
            </Button>
          </form>

          {/* Selector Rápido Demo */}
          <div className="pt-4 border-t border-[#f0f0f4] space-y-2">
            <span className="text-[10px] text-[#aaa9ba] uppercase font-bold text-center block">
              Acceso Rápido Demo (Modo Prototipo)
            </span>
            <div className="grid grid-cols-2 gap-3">
              <button
                type="button"
                onClick={() => handleQuickLogin('administrativo')}
                className="p-2.5 bg-[#fef5fc] hover:bg-[#ffeefb] text-[#e86bc4] border border-[#ffcef3] rounded-[12px] text-[11px] font-bold text-center transition-all cursor-pointer"
              >
                📋 Administrativo / Almacén
              </button>
              <button
                type="button"
                onClick={() => handleQuickLogin('jefe')}
                className="p-2.5 bg-[#f0faff] hover:bg-[#e6f6ff] text-[#247f99] border border-[#a1eafb] rounded-[12px] text-[11px] font-bold text-center transition-all cursor-pointer"
              >
                👑 Jefe Administrativo
              </button>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}
