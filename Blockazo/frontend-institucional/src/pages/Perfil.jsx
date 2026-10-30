import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import Button from '../components/Button';
import PasswordInput from '../components/PasswordInput';
import ConfirmDeleteModal from '../components/ConfirmDeleteModal';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services/authService';

export default function Perfil() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { perfil, usuario, debeCambiarClave, sincronizarPerfilActualizado, marcarClaveActualizada, logout } = useAuth();

  const [activeTab, setActiveTab] = useState(() =>
    searchParams.get('tab') === 'seguridad' ? 'seguridad' : 'datos'
  );
  const [toastMessage, setToastMessage] = useState('');

  // Estados de edición de datos personales (incluye email de ingreso y dirección)
  const [isEditingDatos, setIsEditingDatos] = useState(false);
  const [datosForm, setDatosForm] = useState({
    nombre: '',
    apellido: '',
    tipoDocumento: 'DNI',
    numeroDocumento: '',
    fechaNacimiento: '',
    email: '',
    telefono: '',
    calle: '',
    numero: '',
  });
  const [guardandoDatos, setGuardandoDatos] = useState(false);
  const [errorDatos, setErrorDatos] = useState('');

  // Estados de cambio de contraseña
  const [claveForm, setClaveForm] = useState({
    claveActual: '',
    nuevaClave: '',
    confirmarClave: '',
  });
  const [guardandoClave, setGuardandoClave] = useState(false);
  const [errorClave, setErrorClave] = useState('');

  // Estados de baja de cuenta
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [eliminandoCuenta, setEliminandoCuenta] = useState(false);

  // Inicializar formulario de datos al cargar el perfil
  useEffect(() => {
    if (perfil) {
      const correoContacto = perfil.contactos?.find((c) => c.medio === 'CORREO')?.email;
      const telContacto = perfil.contactos?.find((c) => c.medio === 'TELEFONICO')?.telefono;
      const dirPrincipal = perfil.direcciones?.[0];

      setDatosForm({
        nombre: perfil.nombre || '',
        apellido: perfil.apellido || '',
        tipoDocumento: perfil.tipoDocumento || 'DNI',
        numeroDocumento: perfil.numeroDocumento || '',
        fechaNacimiento: perfil.fechaNacimiento || '',
        email: usuario?.nombreUsuario || correoContacto || '',
        telefono: telContacto || '',
        calle: dirPrincipal?.calle || '',
        numero: dirPrincipal?.numero || dirPrincipal?.numeracion || '',
      });
    }
  }, [perfil, usuario]);

  const showToast = (msg) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(''), 3500);
  };

  const handleGuardarDatos = async (e) => {
    e.preventDefault();
    setErrorDatos('');
    setGuardandoDatos(true);

    try {
      const resp = await authService.actualizarMiPerfil({
        nombre: datosForm.nombre.trim(),
        apellido: datosForm.apellido.trim(),
        tipoDocumento: datosForm.tipoDocumento,
        numeroDocumento: datosForm.numeroDocumento.trim(),
        fechaNacimiento: datosForm.fechaNacimiento || null,
        email: datosForm.email.trim().toLowerCase(),
        telefono: datosForm.telefono.trim() || null,
        calle: datosForm.calle.trim() || null,
        numero: datosForm.numero.trim() || null,
      });

      sincronizarPerfilActualizado(resp, datosForm.email.trim().toLowerCase());
      setIsEditingDatos(false);
      showToast('Datos personales actualizados correctamente.');
    } catch (err) {
      setErrorDatos(err.message || 'Error al actualizar los datos personales.');
    } finally {
      setGuardandoDatos(false);
    }
  };

  const handleCambiarClave = async (e) => {
    e.preventDefault();
    setErrorClave('');

    if (claveForm.nuevaClave !== claveForm.confirmarClave) {
      setErrorClave('La nueva contraseña y su confirmación no coinciden.');
      return;
    }
    if (claveForm.nuevaClave.length < 8) {
      setErrorClave('La nueva contraseña debe tener al menos 8 caracteres.');
      return;
    }
    if (claveForm.nuevaClave.trim().toLowerCase() === 'dulcemayor') {
      setErrorClave('La nueva contraseña no puede ser la clave por defecto.');
      return;
    }

    setGuardandoClave(true);

    try {
      await authService.cambiarClave(
        claveForm.claveActual,
        claveForm.nuevaClave,
        claveForm.confirmarClave
      );
      marcarClaveActualizada();
      setClaveForm({ claveActual: '', nuevaClave: '', confirmarClave: '' });
      showToast('¡Contraseña actualizada con éxito!');
    } catch (err) {
      setErrorClave(err.message || 'Error al actualizar la contraseña.');
    } finally {
      setGuardandoClave(false);
    }
  };

  const handleConfirmarEliminacion = async () => {
    setEliminandoCuenta(true);
    try {
      await authService.eliminarMiCuenta();
      setShowDeleteModal(false);
      logout();
      navigate('/login', {
        replace: true,
        state: { cuentaEliminada: true },
      });
    } catch (err) {
      showToast(err.message || 'No se pudo eliminar la cuenta.');
      setShowDeleteModal(false);
    } finally {
      setEliminandoCuenta(false);
    }
  };

  const nombreCompleto = perfil ? `${perfil.nombre} ${perfil.apellido}` : 'Cliente Mayorista';
  const emailLogin = usuario?.nombreUsuario || '';
  const iniciales = perfil
    ? `${perfil.nombre?.[0] || ''}${perfil.apellido?.[0] || ''}`.toUpperCase()
    : 'CM';

  return (
    <div className="space-y-8 max-w-4xl mx-auto relative px-4 sm:px-0">
      {toastMessage && (
        <div className="fixed top-24 right-6 z-50 bg-[#2b2b3a] text-white px-4 py-2.5 rounded-[12px] shadow-lg text-[12px] flex items-center gap-2 animate-in fade-in slide-in-from-top-2">
          <span className="text-[#7ed6a5]">✓</span>
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Header */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Mi Cuenta
        </span>
        <h1 className="text-3xl font-bold text-[#2b2b3a] mt-1">
          Perfil del Cliente
        </h1>
        <p className="text-[13px] text-[#6b6b80] mt-0.5">
          Administrá tus datos personales, tu mail de acceso y la seguridad de tu cuenta.
        </p>
      </div>

      {/* Profile Overview Card */}
      <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-sm flex flex-col sm:flex-row items-center sm:items-start gap-6 text-center sm:text-left">
        <div className="w-20 h-20 rounded-full bg-gradient-to-br from-[#ffcef3] to-[#a1eafb] text-[#2b2b3a] font-bold text-2xl flex items-center justify-center shadow-md shrink-0">
          {iniciales}
        </div>
        <div className="flex-1 space-y-1">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div>
              <h2 className="text-2xl font-bold text-[#2b2b3a]">{nombreCompleto}</h2>
              <p className="text-[13px] font-semibold text-[#e86bc4]">{emailLogin}</p>
            </div>
          </div>
          <p className="text-[12px] text-[#6b6b80] pt-1">
            {perfil?.tipoDocumento || 'Doc'}: {perfil?.numeroDocumento || 'Sin registrar'}
          </p>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-[#ececf2] gap-6 text-[13px] font-semibold">
        <button
          onClick={() => setActiveTab('datos')}
          className={`pb-3 transition-colors cursor-pointer ${
            activeTab === 'datos'
              ? 'text-[#e86bc4] border-b-2 border-[#e86bc4]'
              : 'text-[#6b6b80] hover:text-[#2b2b3a]'
          }`}
        >
          Datos Personales
        </button>
        <button
          onClick={() => setActiveTab('seguridad')}
          className={`pb-3 transition-colors cursor-pointer ${
            activeTab === 'seguridad'
              ? 'text-[#e86bc4] border-b-2 border-[#e86bc4]'
              : 'text-[#6b6b80] hover:text-[#2b2b3a]'
          }`}
        >
          Seguridad y Clave
        </button>
      </div>

      {/* Tab content: Datos Personales (Lectura y Edición) */}
      {activeTab === 'datos' && (
        <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-sm space-y-6">
          <div className="flex justify-between items-center pb-2 border-b border-[#f0f0f4]">
            <div>
              <h3 className="text-[16px] font-bold text-[#2b2b3a]">Información del Cliente</h3>
              <p className="text-[11px] text-[#6b6b80]">
                Podés modificar tus datos personales y tu correo electrónico de ingreso.
              </p>
            </div>
            {!isEditingDatos && (
              <Button
                secondary
                onClick={() => {
                  setErrorDatos('');
                  setIsEditingDatos(true);
                }}
                className="text-[12px] py-2 px-4"
              >
                Editar datos personales
              </Button>
            )}
          </div>

          {errorDatos && (
            <div className="p-3 bg-[#ffe8ed] text-[#ff8fa3] rounded-[12px] text-[12px] font-medium border border-[#ff8fa3]/30">
              {errorDatos}
            </div>
          )}

          {!isEditingDatos ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-[12px]">
              <div className="p-4 rounded-[14px] bg-[#f8f9fc] border border-[#ececf2]">
                <span className="text-[10px] uppercase font-bold text-[#6b6b80] block mb-1">Nombre Completo</span>
                <strong className="text-[#2b2b3a] text-[13px] font-semibold">{nombreCompleto}</strong>
              </div>
              <div className="p-4 rounded-[14px] bg-[#f8f9fc] border border-[#ececf2]">
                <span className="text-[10px] uppercase font-bold text-[#6b6b80] block mb-1">Documento de Identidad</span>
                <strong className="text-[#2b2b3a] text-[13px] font-semibold">
                  {perfil?.tipoDocumento} {perfil?.numeroDocumento}
                </strong>
              </div>
              <div className="p-4 rounded-[14px] bg-[#f8f9fc] border border-[#ececf2]">
                <span className="text-[10px] uppercase font-bold text-[#6b6b80] block mb-1">
                  Correo Electrónico (Mail de Ingreso)
                </span>
                <strong className="text-[#2b2b3a] text-[13px] font-semibold">{emailLogin}</strong>
              </div>
              <div className="p-4 rounded-[14px] bg-[#f8f9fc] border border-[#ececf2]">
                <span className="text-[10px] uppercase font-bold text-[#6b6b80] block mb-1">Teléfono Móvil</span>
                <strong className="text-[#2b2b3a] text-[13px] font-semibold">
                  {datosForm.telefono || 'Sin teléfono informado'}
                </strong>
              </div>
              <div className="p-4 rounded-[14px] bg-[#f8f9fc] border border-[#ececf2]">
                <span className="text-[10px] uppercase font-bold text-[#6b6b80] block mb-1">Dirección</span>
                <strong className="text-[#2b2b3a] text-[13px] font-semibold">
                  {datosForm.calle
                    ? `${datosForm.calle} ${datosForm.numero || ''}`.trim()
                    : 'Sin dirección informada'}
                </strong>
              </div>
              <div className="p-4 rounded-[14px] bg-[#f8f9fc] border border-[#ececf2]">
                <span className="text-[10px] uppercase font-bold text-[#6b6b80] block mb-1">Fecha de Nacimiento</span>
                <strong className="text-[#2b2b3a] text-[13px] font-semibold">
                  {perfil?.fechaNacimiento || 'Sin fecha informada'}
                </strong>
              </div>
            </div>
          ) : (
            <form onSubmit={handleGuardarDatos} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="edit-nombre" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                    Nombre *
                  </label>
                  <input
                    type="text"
                    id="edit-nombre"
                    required
                    value={datosForm.nombre}
                    onChange={(e) => setDatosForm({ ...datosForm, nombre: e.target.value })}
                    disabled={guardandoDatos}
                    className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  />
                </div>
                <div>
                  <label htmlFor="edit-apellido" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                    Apellido *
                  </label>
                  <input
                    type="text"
                    id="edit-apellido"
                    required
                    value={datosForm.apellido}
                    onChange={(e) => setDatosForm({ ...datosForm, apellido: e.target.value })}
                    disabled={guardandoDatos}
                    className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div>
                  <label htmlFor="edit-tipoDoc" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                    Tipo Doc. *
                  </label>
                  <select
                    id="edit-tipoDoc"
                    value={datosForm.tipoDocumento}
                    onChange={(e) => setDatosForm({ ...datosForm, tipoDocumento: e.target.value })}
                    disabled={guardandoDatos}
                    className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  >
                    <option value="DNI">DNI</option>
                    <option value="CUIT">CUIT</option>
                    <option value="PASAPORTE">Pasaporte</option>
                  </select>
                </div>
                <div className="sm:col-span-2">
                  <label htmlFor="edit-numDoc" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                    Número de Documento *
                  </label>
                  <input
                    type="text"
                    id="edit-numDoc"
                    required
                    value={datosForm.numeroDocumento}
                    onChange={(e) => setDatosForm({ ...datosForm, numeroDocumento: e.target.value })}
                    disabled={guardandoDatos}
                    className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="edit-email" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                    Correo Electrónico (Mail de Ingreso) *
                  </label>
                  <input
                    type="email"
                    id="edit-email"
                    required
                    value={datosForm.email}
                    onChange={(e) => setDatosForm({ ...datosForm, email: e.target.value })}
                    autoComplete="username"
                    spellCheck={false}
                    disabled={guardandoDatos}
                    className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  />
                  <span className="text-[10px] text-[#6b6b80] mt-1 block">
                    Al modificar este mail, se actualizará tu usuario para iniciar sesión.
                  </span>
                </div>
                <div>
                  <label htmlFor="edit-tel" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                    Teléfono Móvil
                  </label>
                  <input
                    type="tel"
                    id="edit-tel"
                    value={datosForm.telefono}
                    onChange={(e) => setDatosForm({ ...datosForm, telefono: e.target.value })}
                    placeholder="261 456 7890"
                    disabled={guardandoDatos}
                    className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  />
                </div>
              </div>

              {/* Dirección: Calle y Número */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="sm:col-span-2">
                  <label htmlFor="edit-calle" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                    Calle
                  </label>
                  <input
                    type="text"
                    id="edit-calle"
                    value={datosForm.calle}
                    onChange={(e) => setDatosForm({ ...datosForm, calle: e.target.value })}
                    placeholder="Av. San Martín"
                    disabled={guardandoDatos}
                    className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  />
                </div>
                <div>
                  <label htmlFor="edit-numero" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                    Número
                  </label>
                  <input
                    type="text"
                    id="edit-numero"
                    value={datosForm.numero}
                    onChange={(e) => setDatosForm({ ...datosForm, numero: e.target.value })}
                    placeholder="1234"
                    disabled={guardandoDatos}
                    className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  />
                </div>
              </div>

              <div>
                <label htmlFor="edit-fechaNac" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                  Fecha de Nacimiento
                </label>
                <input
                  type="date"
                  id="edit-fechaNac"
                  value={datosForm.fechaNacimiento}
                  onChange={(e) => setDatosForm({ ...datosForm, fechaNacimiento: e.target.value })}
                  disabled={guardandoDatos}
                  className="w-full sm:w-1/2 border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>

              <div className="flex gap-3 pt-2">
                <Button
                  type="submit"
                  disabled={guardandoDatos}
                  className="text-[12px] py-2.5 px-6"
                >
                  {guardandoDatos ? 'Guardando...' : 'Guardar cambios'}
                </Button>
                <Button
                  secondary
                  type="button"
                  disabled={guardandoDatos}
                  onClick={() => {
                    setErrorDatos('');
                    setIsEditingDatos(false);
                  }}
                  className="text-[12px] py-2.5 px-6"
                >
                  Cancelar
                </Button>
              </div>
            </form>
          )}
        </div>
      )}

      {/* Tab content: Seguridad y Clave + Zona de Baja de Cuenta */}
      {activeTab === 'seguridad' && (
        <div className="space-y-6">
          <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-sm space-y-4 max-w-lg">
            <h3 className="text-[16px] font-bold text-[#2b2b3a]">Cambiar Contraseña</h3>
            <p className="text-[11px] text-[#6b6b80]">
              Ingresá tu contraseña actual y definí tu nueva clave secreta de al menos 8 caracteres.
            </p>

            {errorClave && (
              <div className="p-3 bg-[#ffe8ed] text-[#ff8fa3] rounded-[12px] text-[12px] font-medium border border-[#ff8fa3]/30">
                {errorClave}
              </div>
            )}

            <form onSubmit={handleCambiarClave} className="space-y-3">
              <div>
                <label htmlFor="sec-actual" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                  Contraseña Actual *
                </label>
                <PasswordInput
                  id="sec-actual"
                  required
                  value={claveForm.claveActual}
                  onChange={(e) => setClaveForm({ ...claveForm, claveActual: e.target.value })}
                  autoComplete="current-password"
                  disabled={guardandoClave}
                />
              </div>
              <div>
                <label htmlFor="sec-nueva" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                  Nueva Contraseña *
                </label>
                <PasswordInput
                  id="sec-nueva"
                  required
                  value={claveForm.nuevaClave}
                  onChange={(e) => setClaveForm({ ...claveForm, nuevaClave: e.target.value })}
                  placeholder="Mínimo 8 caracteres"
                  autoComplete="new-password"
                  disabled={guardandoClave}
                />
              </div>
              <div>
                <label htmlFor="sec-confirmar" className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                  Confirmar Nueva Contraseña *
                </label>
                <PasswordInput
                  id="sec-confirmar"
                  required
                  value={claveForm.confirmarClave}
                  onChange={(e) => setClaveForm({ ...claveForm, confirmarClave: e.target.value })}
                  placeholder="Repetir nueva contraseña"
                  autoComplete="new-password"
                  disabled={guardandoClave}
                />
              </div>
              <div className="pt-2">
                <Button
                  type="submit"
                  disabled={guardandoClave}
                  className="text-[13px] py-2.5 px-6"
                >
                  {guardandoClave ? 'Actualizando...' : 'Actualizar contraseña'}
                </Button>
              </div>
            </form>
          </div>

          <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ff8fa3]/40 shadow-sm space-y-4 max-w-lg">
            <div>
              <span className="text-[10px] font-bold tracking-[0.14em] uppercase text-[#ff8fa3] block">
                Zona de baja
              </span>
              <h3 className="text-[16px] font-bold text-[#2b2b3a] mt-0.5">
                Dar de baja esta cuenta
              </h3>
              <p className="text-[12px] text-[#6b6b80] mt-1 leading-relaxed">
                Al solicitar la baja de tu cuenta, perderás acceso al portal de minoristas de forma definitiva.
              </p>
            </div>

            <div className="pt-1">
              <Button
                variant="danger"
                onClick={() => setShowDeleteModal(true)}
                className="text-[12px] py-2.5 px-5"
              >
                Eliminar cuenta
              </Button>
            </div>
          </div>
        </div>
      )}

      <ConfirmDeleteModal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        onConfirm={handleConfirmarEliminacion}
        loading={eliminandoCuenta}
        title="Eliminar cuenta mayorista"
        message="Esta seguro que desea eliminar esta cuenta? Esta accion no se puede deshacer en un futuro."
      />
    </div>
  );
}
