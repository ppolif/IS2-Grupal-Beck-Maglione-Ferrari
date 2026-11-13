import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../components/Button';
import Logo from '../components/Logo';
import PasswordInput from '../components/PasswordInput';
import { authService } from '../services/authService';
import { useAuth } from '../context/AuthContext';

export default function Registro() {
  const [formData, setFormData] = useState({
    nombre: '',
    apellido: '',
    tipoDocumento: 'DNI',
    numeroDocumento: '',
    fechaNacimiento: '1995-05-15',
    telefono: '',
    nombreComercio: '',
    email: '',
    password: '',
    confirmPassword: '',
  });

  const [loading, setLoading] = useState(false);
  const [loadingSocial, setLoadingSocial] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();
  const { loginConAuth0 } = useAuth();

  const handleSocialRegister = async (connection) => {
    setErrorMessage('');
    setLoadingSocial(true);
    try {
      await loginConAuth0(connection);
    } catch (err) {
      setErrorMessage(err.message || 'Error al iniciar con Auth0.');
      setLoadingSocial(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setErrorMessage('');

    if (formData.password !== formData.confirmPassword) {
      setErrorMessage('Las contraseñas no coinciden.');
      return;
    }
    if (formData.password.length < 8) {
      setErrorMessage('La contraseña debe tener al menos 8 caracteres.');
      return;
    }
    if (formData.password.trim().toLowerCase() === 'dulcemayor') {
      setErrorMessage('La contraseña no puede ser la clave por defecto del sistema.');
      return;
    }

    setLoading(true);

    try {
      await authService.registrarMinorista({
        nombre: formData.nombre.trim(),
        apellido: formData.apellido.trim(),
        tipoDocumento: formData.tipoDocumento,
        numeroDocumento: formData.numeroDocumento.trim(),
        fechaNacimiento: formData.fechaNacimiento || null,
        telefono: formData.telefono.trim() || null,
        nombreComercio: formData.nombreComercio.trim() || null,
        email: formData.email.trim().toLowerCase(),
        clave: formData.password,
      });

      // Redirigir a login para que el usuario ingrese explícitamente a su cuenta
      navigate('/login', {
        replace: true,
        state: {
          registeredEmail: formData.email.trim().toLowerCase(),
          registroExitoso: true,
        },
      });
    } catch (err) {
      setErrorMessage(err.message || 'Error al procesar el registro.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-xl mx-auto my-8 px-4">
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

        {errorMessage && (
          <div className="mb-4 p-3 bg-[#ffe8ed] text-[#ff8fa3] rounded-[12px] text-[12px] font-medium border border-[#ff8fa3]/30">
            {errorMessage}
          </div>
        )}

        {/* Acceso rápido con Google / Auth0 */}
        <div className="space-y-2.5 mb-6">
          <button
            type="button"
            disabled={loading || loadingSocial}
            onClick={() => handleSocialRegister('google-oauth2')}
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
            <span>Crear cuenta rápida con Google</span>
          </button>
        </div>

        {/* Divisor */}
        <div className="relative my-6">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-[#ececf2]" />
          </div>
          <div className="relative flex justify-center text-[10px] uppercase">
            <span className="bg-white px-3 text-[#9b9ba8] font-bold tracking-wider">
              o completá el formulario de alta
            </span>
          </div>
        </div>

        <form onSubmit={handleRegister} className="space-y-4">
          {/* Nombre y Apellido */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label htmlFor="reg-nombre" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Nombre *
              </label>
              <input
                type="text"
                id="reg-nombre"
                name="nombre"
                required
                value={formData.nombre}
                onChange={handleChange}
                placeholder="Ej. Juan"
                autoComplete="given-name"
                disabled={loading}
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
            <div>
              <label htmlFor="reg-apellido" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Apellido *
              </label>
              <input
                type="text"
                id="reg-apellido"
                name="apellido"
                required
                value={formData.apellido}
                onChange={handleChange}
                placeholder="Ej. Pérez"
                autoComplete="family-name"
                disabled={loading}
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
          </div>

          {/* Documento y Tipo */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label htmlFor="reg-tipoDoc" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Tipo Doc. *
              </label>
              <select
                id="reg-tipoDoc"
                name="tipoDocumento"
                value={formData.tipoDocumento}
                onChange={handleChange}
                disabled={loading}
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4] cursor-pointer"
              >
                <option value="DNI">DNI</option>
                <option value="CUIT">CUIT</option>
                <option value="PASAPORTE">Pasaporte</option>
              </select>
            </div>
            <div className="sm:col-span-2">
              <label htmlFor="reg-numDoc" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Número de Documento *
              </label>
              <input
                type="text"
                id="reg-numDoc"
                name="numeroDocumento"
                required
                value={formData.numeroDocumento}
                onChange={handleChange}
                placeholder={formData.tipoDocumento === 'CUIT' ? '20-34589123-4' : '34589123'}
                disabled={loading}
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
          </div>

          {/* Fecha de Nacimiento y Teléfono */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label htmlFor="reg-fechaNac" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Fecha de Nacimiento
              </label>
              <input
                type="date"
                id="reg-fechaNac"
                name="fechaNacimiento"
                value={formData.fechaNacimiento}
                onChange={handleChange}
                disabled={loading}
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
            <div>
              <label htmlFor="reg-tel" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Teléfono / Celular
              </label>
              <input
                type="tel"
                id="reg-tel"
                name="telefono"
                value={formData.telefono}
                onChange={handleChange}
                placeholder="261 456 7890"
                autoComplete="tel"
                disabled={loading}
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
          </div>

          {/* Nombre de Comercio (opcional) */}
          <div>
            <label htmlFor="reg-comercio" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
              Nombre de Fantasía o Comercio (Opcional)
            </label>
            <input
              type="text"
              id="reg-comercio"
              name="nombreComercio"
              value={formData.nombreComercio}
              onChange={handleChange}
              placeholder="Ej. Kiosco San Martín"
              disabled={loading}
              className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
            />
          </div>

          {/* Email (Nombre de usuario) */}
          <div>
            <label htmlFor="reg-email" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
              Correo Electrónico (será tu usuario para iniciar sesión) *
            </label>
            <input
              type="email"
              id="reg-email"
              name="email"
              required
              value={formData.email}
              onChange={handleChange}
              placeholder="juan.perez@email.com"
              autoComplete="username"
              spellCheck={false}
              disabled={loading}
              className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
            />
          </div>

          {/* Contraseñas con botón de mostrar/ocultar */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label htmlFor="reg-pass" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Contraseña *
              </label>
              <PasswordInput
                id="reg-pass"
                name="password"
                required
                value={formData.password}
                onChange={handleChange}
                placeholder="Mínimo 8 caracteres"
                autoComplete="new-password"
                disabled={loading}
              />
            </div>
            <div>
              <label htmlFor="reg-confirmPass" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Confirmar Contraseña *
              </label>
              <PasswordInput
                id="reg-confirmPass"
                name="confirmPassword"
                required
                value={formData.confirmPassword}
                onChange={handleChange}
                placeholder="Repetir contraseña"
                autoComplete="new-password"
                disabled={loading}
              />
            </div>
          </div>

          <Button
            type="submit"
            disabled={loading}
            className="w-full py-3.5 text-[14px] justify-center mt-2"
          >
            {loading ? 'Creando cuenta...' : 'Crear mi cuenta de usuario'}
          </Button>
        </form>

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
