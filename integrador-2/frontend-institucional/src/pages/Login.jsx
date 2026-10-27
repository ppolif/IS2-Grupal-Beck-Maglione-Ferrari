import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../components/Button';
import Logo from '../components/Logo';

export default function Login() {
  const [email, setEmail] = useState('juan.perez@comercio.com');
  const [password, setPassword] = useState('••••••••');
  const navigate = useNavigate();

  const handleLogin = (e) => {
    e.preventDefault();
    // Simular inicio de sesión
    navigate('/tienda');
  };

  return (
    <div className="max-w-md mx-auto my-8">
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

        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
              Email registrado *
            </label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="tucomercio@email.com"
              className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
            />
          </div>

          <div>
            <div className="flex justify-between items-center mb-1.5">
              <label className="block text-[11px] font-semibold text-[#2b2b3a]">
                Contraseña *
              </label>
              <a href="#" className="text-[10px] text-[#3fb8d9] hover:underline">
                ¿Olvidaste tu contraseña?
              </a>
            </div>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
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

          <Button type="submit" className="w-full py-3.5 text-[14px] justify-center mt-2">
            Ingresar a mi cuenta
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

