import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../components/Button';
import Logo from '../components/Logo';

export default function Registro() {
  const [submitted, setSubmitted] = useState(false);
  const [tipoDocumento, setTipoDocumento] = useState('DNI');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errorPassword, setErrorPassword] = useState('');
  const navigate = useNavigate();

  const handleRegister = (e) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      setErrorPassword('Las contraseñas no coinciden.');
      return;
    }
    setErrorPassword('');
    setSubmitted(true);
  };

  return (
    <div className="max-w-xl mx-auto my-8">
      <div className="bg-white rounded-[24px] p-6 sm:p-8 border border-[#ececf2] shadow-[0_14px_40px_rgba(56,52,84,0.08)]">
        <div className="text-center mb-6">
          <div className="flex justify-center mb-3">
            <Logo />
          </div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Portal Clientes Minoristas
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a] mt-1">
            Creá tu Cuenta de Usuario
          </h1>
          <p className="text-[12px] text-[#6b6b80] mt-1">
            Completá tus datos personales para acceder a la plataforma mayorista de Dulce Mayor.
          </p>
        </div>

        {submitted ? (
          <div className="p-8 text-center bg-[#dff8e9] rounded-[16px] text-[#39855b] space-y-3">
            <div className="w-12 h-12 bg-white rounded-full flex items-center justify-center mx-auto mb-1 shadow-sm text-2xl">
              ✓
            </div>
            <h3 className="text-[17px] font-bold">¡Usuario registrado con éxito!</h3>
            <p className="text-[12px] text-[#39855b]/90 leading-relaxed">
              Tu cuenta de usuario ha sido creada. Ya podés iniciar sesión, consultar el catálogo con precios mayoristas y realizar pedidos.
            </p>
            <div className="pt-3 flex flex-col sm:flex-row gap-3 justify-center">
              <Button onClick={() => navigate('/login')} className="w-full sm:w-auto px-6">
                Iniciar sesión
              </Button>
              <Button secondary onClick={() => navigate('/tienda')} className="w-full sm:w-auto px-6">
                Explorar catálogo
              </Button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleRegister} className="space-y-4">
            {/* Nombre y Apellido */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                  Nombre *
                </label>
                <input
                  type="text"
                  required
                  placeholder="Ej. Juan"
                  className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>
              <div>
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                  Apellido *
                </label>
                <input
                  type="text"
                  required
                  placeholder="Ej. Pérez"
                  className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>
            </div>

            {/* Documento y Tipo */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div>
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                  Tipo Doc. *
                </label>
                <select
                  value={tipoDocumento}
                  onChange={(e) => setTipoDocumento(e.target.value)}
                  className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4] cursor-pointer"
                >
                  <option value="DNI">DNI</option>
                  <option value="CUIT">CUIT</option>
                  <option value="PASAPORTE">Pasaporte</option>
                </select>
              </div>
              <div className="sm:col-span-2">
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                  Número de Documento *
                </label>
                <input
                  type="text"
                  required
                  placeholder={tipoDocumento === 'CUIT' ? '20-34589123-4' : '34589123'}
                  className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>
            </div>

            {/* Fecha de Nacimiento y Teléfono */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                  Fecha de Nacimiento *
                </label>
                <input
                  type="date"
                  required
                  defaultValue="1992-05-14"
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>
              <div>
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                  Teléfono / Celular *
                </label>
                <input
                  type="tel"
                  required
                  placeholder="261 456 7890"
                  className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>
            </div>

            {/* Email (Nombre de usuario) */}
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Correo Electrónico (será tu nombre de usuario) *
              </label>
              <input
                type="email"
                required
                placeholder="juan.perez@email.com"
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            {/* Contraseñas */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                  Contraseña *
                </label>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Mínimo 8 caracteres"
                  className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>
              <div>
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                  Confirmar Contraseña *
                </label>
                <input
                  type="password"
                  required
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Repetir contraseña"
                  className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>
            </div>

            {errorPassword && (
              <p className="text-[11px] text-[#ff8fa3] font-semibold">{errorPassword}</p>
            )}

            <Button type="submit" className="w-full py-3.5 text-[14px] justify-center mt-2">
              Crear mi cuenta de usuario
            </Button>
          </form>
        )}

        <div className="mt-6 pt-5 border-t border-[#f0f0f4] text-center text-[12px] text-[#6b6b80]">
          ¿Ya tenés una cuenta registrada?{' '}
          <Link to="/login" className="text-[#e86bc4] font-semibold hover:underline">
            Iniciá sesión acá
          </Link>
        </div>
      </div>
    </div>
  );
}
