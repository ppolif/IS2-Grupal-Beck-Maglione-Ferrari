import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../components/Button';
import Logo from '../components/Logo';
import PasswordInput from '../components/PasswordInput';
import { authService } from '../services/authService';

export default function Registro() {
  const [formData, setFormData] = useState({
    nombre: '',
    apellido: '',
    tipoDocumento: 'DNI',
    numeroDocumento: '',
    fechaNacimiento: '1995-05-15',
    telefono: '',
    calle: '',
    numero: '',
    nombreComercio: '',
    email: '',
    password: '',
    confirmPassword: '',
  });

  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();

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
        calle: formData.calle.trim() || null,
        numero: formData.numero.trim() || null,
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
            Registro de comedores de blockazo
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a] mt-1">
            Creá tu Cuenta de Usuario
          </h1>
          <p className="text-[12px] text-[#6b6b80] mt-1">
            Completá tus datos personales para INTENTAR comerte el blockazo.
          </p>
        </div>

        {errorMessage && (
          <div className="mb-4 p-3 bg-[#ffe8ed] text-[#ff8fa3] rounded-[12px] text-[12px] font-medium border border-[#ff8fa3]/30">
            {errorMessage}
          </div>
        )}

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
          </div>

          {/* Dirección: Calle y Número */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="sm:col-span-2">
              <label htmlFor="reg-calle" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Calle
              </label>
              <input
                type="text"
                id="reg-calle"
                name="calle"
                value={formData.calle}
                onChange={handleChange}
                placeholder="Av. San Martín"
                disabled={loading}
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
            <div>
              <label htmlFor="reg-numero" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Número
              </label>
              <input
                type="text"
                id="reg-numero"
                name="numero"
                value={formData.numero}
                onChange={handleChange}
                placeholder="1234"
                disabled={loading}
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
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
