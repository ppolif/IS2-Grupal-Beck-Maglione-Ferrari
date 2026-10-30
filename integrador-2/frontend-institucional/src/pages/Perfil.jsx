import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import Button from '../components/Button';
import Icon from '../components/Icon';
import PasswordInput from '../components/PasswordInput';
import ConfirmDeleteModal from '../components/ConfirmDeleteModal';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services/authService';

export default function Perfil() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { perfil, usuario, debeCambiarClave, sincronizarPerfilActualizado, marcarClaveActualizada, logout } = useAuth();

  const [activeTab, setActiveTab] = useState(() => searchParams.get('tab') || 'pedidos');
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [showQrModal, setShowQrModal] = useState(false);
  const [copiedAlias, setCopiedAlias] = useState(false);
  const [toastMessage, setToastMessage] = useState('');

  // Estados de edición de datos personales (incluye email de ingreso)
  const [isEditingDatos, setIsEditingDatos] = useState(false);
  const [datosForm, setDatosForm] = useState({
    nombre: '',
    apellido: '',
    tipoDocumento: 'DNI',
    numeroDocumento: '',
    fechaNacimiento: '',
    email: '',
    telefono: '',
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

      setDatosForm({
        nombre: perfil.nombre || '',
        apellido: perfil.apellido || '',
        tipoDocumento: perfil.tipoDocumento || 'DNI',
        numeroDocumento: perfil.numeroDocumento || '',
        fechaNacimiento: perfil.fechaNacimiento || '',
        email: usuario?.nombreUsuario || correoContacto || '',
        telefono: telContacto || '',
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

  // Mock de pedidos para consulta
  const orders = [
    {
      id: 'DM-2084',
      fecha: '14 Jun 2025 · 10:15 hs',
      total: '$45.200',
      subtotal: '$50.100',
      descuento: '$4.900',
      estado: 'En camino',
      facturaNum: 'FC-A-0001-00002084',
      medioPago: 'Billetera Virtual / QR',
      direccionEntrega: 'Av. San Martín 1842, Godoy Cruz, Mendoza',
      items: [
        { nombre: 'Alfajor triple chocolate', cant: 2, precioUnit: '$18.900', subtotal: '$37.800' },
        { nombre: 'Gomitas frutales', cant: 1, precioUnit: '$6.450', subtotal: '$6.450' },
        { nombre: 'Chupetines surtidos', cant: 1, precioUnit: '$5.850', subtotal: '$5.850' },
      ],
      trazabilidad: [
        { estado: 'Pedido Confirmado', fecha: '14 Jun - 09:30 hs', done: true },
        { estado: 'Facturado en Depósito', fecha: '14 Jun - 10:15 hs', done: true },
        { estado: 'En Reparto con Distribuidor', fecha: '14 Jun - 11:00 hs', done: true },
        { estado: 'Entregado en Comercio', fecha: 'Pendiente', done: false },
      ],
    },
    {
      id: 'DM-1950',
      fecha: '28 May 2025 · 14:20 hs',
      total: '$82.400',
      subtotal: '$89.000',
      descuento: '$6.600',
      estado: 'Entregado',
      facturaNum: 'FC-A-0001-00001950',
      medioPago: 'Transferencia Bancaria',
      direccionEntrega: 'Av. San Martín 1842, Godoy Cruz, Mendoza',
      items: [
        { nombre: 'Alfajor triple chocolate', cant: 3, precioUnit: '$18.900', subtotal: '$56.700' },
        { nombre: 'Chocolate con maní', cant: 2, precioUnit: '$15.600', subtotal: '$31.200' },
        { nombre: 'Gomitas frutales', cant: 1, precioUnit: '$1.100', subtotal: '$1.100' },
      ],
      trazabilidad: [
        { estado: 'Pedido Confirmado', fecha: '28 May - 11:00 hs', done: true },
        { estado: 'Facturado', fecha: '28 May - 12:30 hs', done: true },
        { estado: 'En Reparto', fecha: '28 May - 14:00 hs', done: true },
        { estado: 'Entregado en Comercio', fecha: '28 May - 16:30 hs', done: true },
      ],
    },
  ];

  const handleOpenDetail = (order) => {
    setSelectedOrder(order);
    setShowDetailModal(true);
  };

  const handleOpenQr = (order) => {
    setSelectedOrder(order);
    setShowQrModal(true);
  };

  const handleDownloadPdf = (orderId, facturaNum) => {
    showToast(`Descargando factura electrónica PDF (${facturaNum})...`);
  };

  const handleCopyAlias = () => {
    navigator.clipboard?.writeText('DULCE.MAYOR.PAGO');
    setCopiedAlias(true);
    setTimeout(() => setCopiedAlias(false), 2500);
    showToast('¡Alias copiado al portapapeles!');
  };

  const nombreCompleto = perfil ? `${perfil.nombre} ${perfil.apellido}` : 'Cliente Mayorista';
  const emailLogin = usuario?.nombreUsuario || '';
  const iniciales = perfil
    ? `${perfil.nombre?.[0] || ''}${perfil.apellido?.[0] || ''}`.toUpperCase()
    : 'CM';

  return (
    <div className="space-y-8 max-w-4xl mx-auto relative px-4 sm:px-0">
      {/* Toast Notification */}
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
          Administrá tus datos personales, tu mail de acceso, consultá el detalle de tus pedidos y gestioná tu seguridad.
        </p>
      </div>

      {debeCambiarClave && (
        <div className="p-4 rounded-[16px] bg-[#fff6e0] border border-[#ffd98a] text-[#8a6519] text-[12px] flex items-center gap-3">
          <span className="text-lg font-bold">⚠️</span>
          <div>
            <p className="font-bold">Debe cambiar su contraseña temporal</p>
            <p className="text-[11px] opacity-90">
              Por motivos de seguridad, debe actualizar su clave en la pestaña "Seguridad y Clave" antes de realizar pedidos.
            </p>
          </div>
        </div>
      )}

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
            <span className="inline-block bg-[#dff8e9] text-[#39855b] text-[11px] font-semibold px-3 py-1 rounded-full self-center sm:self-auto">
              Cliente Mayorista Activo
            </span>
          </div>
          <p className="text-[12px] text-[#6b6b80] pt-1">
            {perfil?.tipoDocumento || 'Doc'}: {perfil?.numeroDocumento || 'Sin registrar'} · Zona Mendoza
          </p>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-[#ececf2] gap-6 text-[13px] font-semibold">
        <button
          onClick={() => setActiveTab('pedidos')}
          className={`pb-3 transition-colors cursor-pointer ${
            activeTab === 'pedidos'
              ? 'text-[#e86bc4] border-b-2 border-[#e86bc4]'
              : 'text-[#6b6b80] hover:text-[#2b2b3a]'
          }`}
        >
          Historial de Pedidos ({orders.length})
        </button>
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

      {/* Tab content: Pedidos */}
      {activeTab === 'pedidos' && (
        <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-sm space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-2 border-b border-[#f0f0f4] gap-2">
            <div>
              <h3 className="text-[16px] font-bold text-[#2b2b3a]">Pedidos Realizados</h3>
              <p className="text-[11px] text-[#6b6b80]">
                Consultá el detalle de productos, descargá facturas en PDF o pagá con código QR.
              </p>
            </div>
          </div>

          <div className="space-y-3.5">
            {orders.map((p) => (
              <div
                key={p.id}
                className="p-5 rounded-[18px] border border-[#ececf2] flex flex-col md:flex-row items-start md:items-center justify-between gap-4 bg-[#fdfdfd] hover:border-[#e86bc4]/50 transition-colors"
              >
                <div className="space-y-1">
                  <div className="flex items-center gap-2.5 flex-wrap">
                    <strong className="text-[#2b2b3a] text-[14px]">Pedido #{p.id}</strong>
                    <span className="text-[11px] text-[#6b6b80]">· {p.fecha}</span>
                    <span
                      className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full ${
                        p.estado === 'En camino'
                          ? 'bg-[#d9f7ff] text-[#247f99]'
                          : 'bg-[#dff8e9] text-[#39855b]'
                      }`}
                    >
                      {p.estado}
                    </span>
                    <span className="text-[10px] text-[#8c8b9c] font-medium bg-[#f0f2f7] px-2 py-0.5 rounded-md">
                      {p.medioPago}
                    </span>
                  </div>
                  <p className="text-[12px] text-[#6b6b80]">
                    {p.items.map((it) => `${it.cant}x ${it.nombre}`).join(' · ')}
                  </p>
                </div>

                <div className="flex items-center gap-3 w-full md:w-auto justify-between md:justify-end border-t md:border-t-0 pt-3 md:pt-0">
                  <strong className="text-[16px] text-[#2b2b3a] font-bold">
                    {p.total}
                  </strong>

                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => handleOpenDetail(p)}
                      className="px-3 py-1.5 rounded-[10px] text-[12px] font-semibold text-[#3fb8d9] bg-[#d9f7ff]/40 hover:bg-[#d9f7ff] transition-colors cursor-pointer"
                    >
                      Ver detalle
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDownloadPdf(p.id, p.facturaNum)}
                      className="px-3 py-1.5 rounded-[10px] text-[12px] font-semibold text-[#2b2b3a] bg-slate-100 hover:bg-slate-200 transition-colors flex items-center gap-1 cursor-pointer"
                      title="Descargar factura en PDF"
                    >
                      <span>PDF</span>
                    </button>
                    <button
                      type="button"
                      onClick={() => handleOpenQr(p)}
                      className="px-3 py-1.5 rounded-[10px] text-[12px] font-semibold text-white bg-[#e86bc4] hover:bg-[#d456b0] transition-colors flex items-center gap-1 shadow-xs cursor-pointer"
                      title="Ver QR de pago"
                    >
                      <span>QR</span>
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Tab content: Datos Personales (Lectura y Edición) */}
      {activeTab === 'datos' && (
        <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-sm space-y-6">
          <div className="flex justify-between items-center pb-2 border-b border-[#f0f0f4]">
            <div>
              <h3 className="text-[16px] font-bold text-[#2b2b3a]">Información del Cliente Minorista</h3>
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
          {/* Card Cambio de Clave */}
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

          {/* Card Zona de Peligro: Baja de Cuenta */}
          <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ff8fa3]/40 shadow-sm space-y-4 max-w-lg">
            <div>
              <span className="text-[10px] font-bold tracking-[0.14em] uppercase text-[#ff8fa3] block">
                Zona de baja
              </span>
              <h3 className="text-[16px] font-bold text-[#2b2b3a] mt-0.5">
                Dar de baja esta cuenta
              </h3>
              <p className="text-[12px] text-[#6b6b80] mt-1 leading-relaxed">
                Al solicitar la baja de tu cuenta, perderás acceso al portal de minoristas de Dulce Mayor de forma definitiva.
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

      {/* MODAL DE CONFIRMACIÓN DE ELIMINACIÓN DE CUENTA */}
      <ConfirmDeleteModal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        onConfirm={handleConfirmarEliminacion}
        loading={eliminandoCuenta}
        title="Eliminar cuenta mayorista"
        message="Esta seguro que desea eliminar esta cuenta? Esta accion no se puede deshacer en un futuro."
      />

      {/* MODAL 1: DETALLE DE PEDIDO */}
      {showDetailModal && selectedOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/45 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-[22px] p-6 sm:p-7 shadow-[0_22px_80px_rgba(32,30,50,0.28)] border border-[#ececf2] w-full max-w-xl max-h-[90vh] overflow-y-auto space-y-4">
            <div className="flex justify-between items-start pb-3 border-b border-[#f0f0f4]">
              <div>
                <span className="text-[10px] font-bold tracking-[0.14em] uppercase text-[#e86bc4] block">
                  Detalle del Pedido
                </span>
                <h3 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
                  Pedido #{selectedOrder.id}
                </h3>
                <p className="text-[11px] text-[#6b6b80]">{selectedOrder.fecha}</p>
              </div>
              <button
                type="button"
                onClick={() => setShowDetailModal(false)}
                className="text-[#8c8b9c] hover:text-[#2b2b3a] text-lg p-1 cursor-pointer"
              >
                ✕
              </button>
            </div>

            {/* Status & Payment Method */}
            <div className="p-3.5 rounded-[14px] bg-[#f8f9fc] border border-[#ececf2] flex flex-wrap justify-between items-center gap-3 text-[12px]">
              <div>
                <span className="text-[#6b6b80] block text-[10px] uppercase font-bold">Estado actual:</span>
                <span className="font-bold text-[#e86bc4]">{selectedOrder.estado}</span>
              </div>
              <div>
                <span className="text-[#6b6b80] block text-[10px] uppercase font-bold">Medio de pago:</span>
                <span className="font-semibold text-[#2b2b3a]">{selectedOrder.medioPago}</span>
              </div>
              <div>
                <span className="text-[#6b6b80] block text-[10px] uppercase font-bold">Factura asociada:</span>
                <span className="font-mono text-[#3fb8d9] font-bold">{selectedOrder.facturaNum}</span>
              </div>
            </div>

            {/* Items Table */}
            <div>
              <h4 className="text-[12px] font-bold text-[#2b2b3a] uppercase tracking-wider mb-2">
                Productos y Bultos
              </h4>
              <div className="border border-[#ececf2] rounded-[14px] overflow-hidden text-[12px]">
                <table className="w-full text-left">
                  <thead className="bg-[#f8f9fc] text-[10px] font-bold text-[#8c8b9c] uppercase border-b border-[#ececf2]">
                    <tr>
                      <th className="py-2.5 px-3">Producto</th>
                      <th className="py-2.5 px-3 text-center">Cant.</th>
                      <th className="py-2.5 px-3 text-right">P. Unit</th>
                      <th className="py-2.5 px-3 text-right">Subtotal</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[#f0f0f4]">
                    {selectedOrder.items.map((it, i) => (
                      <tr key={i} className="hover:bg-slate-50/60">
                        <td className="py-2.5 px-3 font-medium text-[#2b2b3a]">{it.nombre}</td>
                        <td className="py-2.5 px-3 text-center text-[#6b6b80]">{it.cant}</td>
                        <td className="py-2.5 px-3 text-right text-[#6b6b80]">{it.precioUnit}</td>
                        <td className="py-2.5 px-3 text-right font-bold text-[#2b2b3a]">{it.subtotal}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Financial summary */}
            <div className="p-3.5 rounded-[14px] bg-[#fafafd] border border-[#ececf2] text-[12px] space-y-1.5">
              <div className="flex justify-between text-[#6b6b80]">
                <span>Subtotal mercadería</span>
                <span className="font-semibold text-[#2b2b3a]">{selectedOrder.subtotal}</span>
              </div>
              <div className="flex justify-between text-[#39855b]">
                <span>Descuento aplicado</span>
                <span className="font-semibold">−{selectedOrder.descuento}</span>
              </div>
              <div className="flex justify-between text-[15px] pt-2 border-t border-[#ececf2]">
                <strong className="text-[#2b2b3a]">Total Facturado</strong>
                <strong className="text-[#e86bc4] font-bold text-[18px]">
                  {selectedOrder.total}
                </strong>
              </div>
            </div>

            {/* Trazabilidad */}
            <div>
              <h4 className="text-[12px] font-bold text-[#2b2b3a] uppercase tracking-wider mb-2">
                Seguimiento de Entrega
              </h4>
              <div className="space-y-2">
                {selectedOrder.trazabilidad.map((step, idx) => (
                  <div key={idx} className="flex items-center gap-3 text-[11px]">
                    <span
                      className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold ${
                        step.done
                          ? 'bg-[#dff8e9] text-[#39855b]'
                          : 'bg-[#f0f0f4] text-[#aaa9ba]'
                      }`}
                    >
                      {step.done ? '✓' : idx + 1}
                    </span>
                    <span className={step.done ? 'font-medium text-[#2b2b3a]' : 'text-[#aaa9ba]'}>
                      {step.estado}
                    </span>
                    <span className="text-[#aaa9ba] text-[10px] ml-auto">{step.fecha}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Modal Actions */}
            <div className="pt-3 border-t border-[#f0f0f4] flex flex-wrap gap-2.5 justify-end">
              <Button
                secondary
                onClick={() => handleDownloadPdf(selectedOrder.id, selectedOrder.facturaNum)}
                className="text-[12px] py-2"
              >
                <span>Descargar Factura PDF</span>
              </Button>
              <Button
                onClick={() => {
                  setShowDetailModal(false);
                  setShowQrModal(true);
                }}
                className="text-[12px] py-2"
              >
                <span>Pagar con Código QR</span>
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* MODAL 2: VER QR DE PAGO */}
      {showQrModal && selectedOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/45 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-[22px] p-6 sm:p-7 shadow-[0_22px_80px_rgba(32,30,50,0.28)] border border-[#ececf2] w-full max-w-sm text-center space-y-4">
            <div className="flex justify-between items-center pb-2 border-b border-[#f0f0f4]">
              <span className="text-[10px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
                Billetera Virtual
              </span>
              <button
                type="button"
                onClick={() => setShowQrModal(false)}
                className="text-[#8c8b9c] hover:text-[#2b2b3a] text-lg p-1 cursor-pointer"
              >
                ✕
              </button>
            </div>

            <h3 className="text-xl font-bold text-[#2b2b3a]">
              Código QR de Pago
            </h3>
            <p className="text-[11px] text-[#6b6b80]">
              Escaneá con Mercado Pago o tu app bancaria para abonar el Pedido #{selectedOrder.id}.
            </p>

            {/* Simulated QR Code Component */}
            <div className="p-4 bg-white border-2 border-dashed border-[#e86bc4] rounded-[18px] inline-block shadow-inner">
              <svg
                className="w-48 h-48 mx-auto"
                viewBox="0 0 200 200"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <rect width="200" height="200" fill="#ffffff" />
                <rect x="15" y="15" width="45" height="45" rx="6" fill="#2b2b3a" />
                <rect x="23" y="23" width="29" height="29" rx="3" fill="#ffffff" />
                <rect x="29" y="29" width="17" height="17" rx="2" fill="#e86bc4" />

                <rect x="140" y="15" width="45" height="45" rx="6" fill="#2b2b3a" />
                <rect x="148" y="23" width="29" height="29" rx="3" fill="#ffffff" />
                <rect x="154" y="29" width="17" height="17" rx="2" fill="#3fb8d9" />

                <rect x="15" y="140" width="45" height="45" rx="6" fill="#2b2b3a" />
                <rect x="23" y="148" width="29" height="29" rx="3" fill="#ffffff" />
                <rect x="29" y="154" width="17" height="17" rx="2" fill="#e86bc4" />

                <rect x="70" y="20" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="90" y="20" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="110" y="20" width="12" height="12" rx="2" fill="#2b2b3a" />

                <rect x="70" y="40" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="110" y="40" width="12" height="12" rx="2" fill="#e86bc4" />
                <rect x="90" y="60" width="12" height="12" rx="2" fill="#2b2b3a" />

                <rect x="20" y="75" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="40" y="75" width="12" height="12" rx="2" fill="#3fb8d9" />
                <rect x="70" y="80" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="90" y="80" width="20" height="20" rx="3" fill="#e86bc4" />
                <rect x="120" y="80" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="145" y="75" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="165" y="75" width="12" height="12" rx="2" fill="#2b2b3a" />

                <rect x="20" y="95" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="40" y="95" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="70" y="110" width="12" height="12" rx="2" fill="#3fb8d9" />
                <rect x="120" y="110" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="145" y="95" width="12" height="12" rx="2" fill="#3fb8d9" />
                <rect x="165" y="95" width="12" height="12" rx="2" fill="#2b2b3a" />

                <rect x="70" y="140" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="90" y="140" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="110" y="140" width="12" height="12" rx="2" fill="#e86bc4" />
                <rect x="140" y="140" width="20" height="20" rx="3" fill="#2b2b3a" />
                <rect x="170" y="140" width="12" height="12" rx="2" fill="#2b2b3a" />

                <rect x="70" y="165" width="20" height="12" rx="2" fill="#2b2b3a" />
                <rect x="100" y="165" width="12" height="12" rx="2" fill="#3fb8d9" />
                <rect x="120" y="165" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="150" y="165" width="30" height="12" rx="2" fill="#2b2b3a" />
              </svg>
            </div>

            <div className="bg-[#fafafd] p-3 rounded-[14px] border border-[#ececf2]">
              <span className="text-[11px] text-[#6b6b80] block">Importe exacto a transferir:</span>
              <strong className="text-2xl font-bold text-[#e86bc4]">
                {selectedOrder.total}
              </strong>
            </div>

            <div className="p-3 bg-[#f8f9fc] rounded-[14px] border border-[#ececf2] text-[11px] text-left space-y-1">
              <span className="text-[#6b6b80] block font-semibold text-[10px] uppercase">
                O transferí directamente por Alias:
              </span>
              <div className="flex items-center justify-between bg-white px-2.5 py-1.5 rounded-[8px] border border-[#ececf2]">
                <code className="font-mono font-bold text-[#2b2b3a]">DULCE.MAYOR.PAGO</code>
                <button
                  type="button"
                  onClick={handleCopyAlias}
                  className="text-[10px] font-bold text-[#3fb8d9] hover:underline cursor-pointer"
                >
                  {copiedAlias ? '¡Copiado!' : 'Copiar'}
                </button>
              </div>
              <span className="text-[10px] text-[#aaa9ba] block pt-0.5">
                CBU: 0000003100012345678901 · Titular: Dulce Mayor S.A.
              </span>
            </div>

            <div className="pt-2 flex flex-col gap-2">
              <Button
                onClick={() => {
                  setShowQrModal(false);
                  showToast('¡Pago informado! Lo validaremos a la brevedad.');
                }}
                className="w-full justify-center text-[13px]"
              >
                Ya realicé la transferencia
              </Button>
              <button
                type="button"
                onClick={() => setShowQrModal(false)}
                className="text-[11px] text-[#6b6b80] hover:text-[#2b2b3a] py-1 cursor-pointer"
              >
                Volver
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
