import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import Button from '../components/Button';
import Logo from '../components/Logo';
import PasswordInput from '../components/PasswordInput';
import { authService } from '../services/authService';
import { useAuth } from '../context/AuthContext';

export default function Registro() {
  const [formData, setFormData] = useState({
    nombre: '',
    apellido: '',
    numeroDocumento: '',
    fechaNacimiento: '1995-05-15',
    calle: '',
    numero: '',
    nombreComercio: '',
    email: '',
    password: '',
    confirmPassword: '',
  });

  // Selects en cascada de dirección geográfica
  const [paises, setPaises] = useState([]);
  const [provincias, setProvincias] = useState([]);
  const [departamentos, setDepartamentos] = useState([]);
  const [localidades, setLocalidades] = useState([]);

  const [paisId, setPaisId] = useState('');
  const [provinciaId, setProvinciaId] = useState('');
  const [departamentoId, setDepartamentoId] = useState('');
  const [localidadId, setLocalidadId] = useState('');

  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();
  const { loginConAuthResponse } = useAuth();
  const { loginWithPopup, getIdTokenClaims } = useAuth0();

  // Carga inicial de países disponibles
  useEffect(() => {
    authService
      .obtenerPaises()
      .then((data) => setPaises(Array.isArray(data) ? data : []))
      .catch(() => setPaises([]));
  }, []);

  const handlePaisChange = async (e) => {
    const selected = e.target.value;
    setPaisId(selected);
    setProvinciaId('');
    setDepartamentoId('');
    setLocalidadId('');
    setProvincias([]);
    setDepartamentos([]);
    setLocalidades([]);

    if (selected) {
      try {
        const data = await authService.obtenerProvincias(selected);
        setProvincias(Array.isArray(data) ? data : []);
      } catch {
        setProvincias([]);
      }
    }
  };

  const handleProvinciaChange = async (e) => {
    const selected = e.target.value;
    setProvinciaId(selected);
    setDepartamentoId('');
    setLocalidadId('');
    setDepartamentos([]);
    setLocalidades([]);

    if (selected) {
      try {
        const data = await authService.obtenerDepartamentos(selected);
        setDepartamentos(Array.isArray(data) ? data : []);
      } catch {
        setDepartamentos([]);
      }
    }
  };

  const handleDepartamentoChange = async (e) => {
    const selected = e.target.value;
    setDepartamentoId(selected);
    setLocalidadId('');
    setLocalidades([]);

    if (selected) {
      try {
        const data = await authService.obtenerLocalidades(selected);
        setLocalidades(Array.isArray(data) ? data : []);
      } catch {
        setLocalidades([]);
      }
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
        numeroDocumento: formData.numeroDocumento.trim(),
        fechaNacimiento: formData.fechaNacimiento || null,
        calle: formData.calle.trim() || null,
        numero: formData.numero ? String(formData.numero).trim() : null,
        localidadId: localidadId || null,
        nombreComercio: formData.nombreComercio.trim() || null,
        email: formData.email.trim().toLowerCase(),
        clave: formData.password,
      });

      // Redirigir a login para que el usuario ingrese
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

  const handleSocialRegister = async () => {
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
      navigate('/perfil', { replace: true });
    } catch (err) {
      if (err.message && !err.message.includes('popup_closed_by_user')) {
        setErrorMessage(err.message || 'Error al registrarse con Google.');
      }
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
          <h1 className="text-2xl font-bold text-[#2b2b3a] mt-1">
            Creá tu Cuenta de Usuario
          </h1>
          <p className="text-[12px] text-[#6b6b80] mt-1">
            Completá tus datos para registrarte en el sistema.
          </p>
        </div>

        {errorMessage && (
          <div className="mb-4 p-3 bg-[#ffe8ed] text-[#ff8fa3] rounded-[12px] text-[12px] font-medium border border-[#ff8fa3]/30">
            {errorMessage}
          </div>
        )}

        {/* Registro con Google */}
        <button
          type="button"
          onClick={handleSocialRegister}
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
          Registrarse con Google
        </button>

        <div className="flex items-center my-5">
          <div className="flex-1 border-t border-[#ececf2]"></div>
          <span className="px-3 text-[11px] text-[#8e8ea0] uppercase tracking-wider font-medium">o completá el formulario</span>
          <div className="flex-1 border-t border-[#ececf2]"></div>
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
                placeholder="Juan"
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
                placeholder="Pérez"
                disabled={loading}
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
          </div>

          {/* Documento y Fecha de Nacimiento */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
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
                placeholder="34589123"
                disabled={loading}
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
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
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
          </div>

          {/* Selects en cascada de Ubicación Geográfica */}
          <div className="bg-[#fcfcfe] p-4 rounded-[16px] border border-[#f0f0f5] space-y-3">
            <h3 className="text-[12px] font-bold text-[#2b2b3a]">Ubicación y Localidad</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label htmlFor="sel-pais" className="block text-[11px] font-medium text-[#6b6b80] mb-1">
                  País
                </label>
                <select
                  id="sel-pais"
                  value={paisId}
                  onChange={handlePaisChange}
                  disabled={loading || paises.length === 0}
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                >
                  <option value="">Seleccionar país...</option>
                  {paises.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.nombre}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label htmlFor="sel-provincia" className="block text-[11px] font-medium text-[#6b6b80] mb-1">
                  Provincia
                </label>
                <select
                  id="sel-provincia"
                  value={provinciaId}
                  onChange={handleProvinciaChange}
                  disabled={loading || !paisId || provincias.length === 0}
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                >
                  <option value="">Seleccionar provincia...</option>
                  {provincias.map((pr) => (
                    <option key={pr.id} value={pr.id}>
                      {pr.nombre}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label htmlFor="sel-depto" className="block text-[11px] font-medium text-[#6b6b80] mb-1">
                  Departamento
                </label>
                <select
                  id="sel-depto"
                  value={departamentoId}
                  onChange={handleDepartamentoChange}
                  disabled={loading || !provinciaId || departamentos.length === 0}
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                >
                  <option value="">Seleccionar departamento...</option>
                  {departamentos.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.nombre}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label htmlFor="sel-localidad" className="block text-[11px] font-medium text-[#6b6b80] mb-1">
                  Localidad
                </label>
                <select
                  id="sel-localidad"
                  value={localidadId}
                  onChange={(e) => setLocalidadId(e.target.value)}
                  disabled={loading || !departamentoId || localidades.length === 0}
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                >
                  <option value="">Seleccionar localidad...</option>
                  {localidades.map((l) => (
                    <option key={l.id} value={l.id}>
                      {l.nombre} (CP: {l.codigoPostal})
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          {/* Dirección: Nombre de la calle (texto) y Numeración (numérico) */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="sm:col-span-2">
              <label htmlFor="reg-calle" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Nombre de la calle
              </label>
              <input
                type="text"
                id="reg-calle"
                name="calle"
                value={formData.calle}
                onChange={handleChange}
                placeholder="Ej. San Martín"
                disabled={loading}
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
            <div>
              <label htmlFor="reg-numero" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Numeración
              </label>
              <input
                type="number"
                min="1"
                id="reg-numero"
                name="numero"
                value={formData.numero}
                onChange={handleChange}
                placeholder="Ej. 1250"
                disabled={loading}
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
          </div>

          {/* Nombre de Comercio (opcional) */}
          <div>
            <label htmlFor="reg-comercio" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
              Nombre de Comercio / Fantasía <span className="text-[#8e8ea0] font-normal">(opcional)</span>
            </label>
            <input
              type="text"
              id="reg-comercio"
              name="nombreComercio"
              value={formData.nombreComercio}
              onChange={handleChange}
              placeholder="Ej. Kiosco Central"
              disabled={loading}
              className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
            />
          </div>

          {/* Correo Electrónico */}
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

          {/* Contraseñas */}
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
            className="w-full py-3.5 text-[14px] justify-center mt-4 cursor-pointer"
          >
            {loading ? 'Creando cuenta...' : 'Crear mi cuenta'}
          </Button>
        </form>

        <div className="mt-6 pt-5 border-t border-[#f0f0f4] text-center text-[12px] text-[#6b6b80]">
          ¿Ya tenés una cuenta?{' '}
          <Link to="/login" className="text-[#e86bc4] font-semibold hover:underline">
            Iniciá sesión acá
          </Link>
        </div>
      </div>
    </div>
  );
}
