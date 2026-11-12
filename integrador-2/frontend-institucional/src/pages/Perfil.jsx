import { useState, useEffect, useCallback } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import Button from '../components/Button';
import Icon from '../components/Icon';
import PasswordInput from '../components/PasswordInput';
import ConfirmDeleteModal from '../components/ConfirmDeleteModal';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services/authService';
import { apiFetch } from '../services/api';
import { pedidoClienteService } from '../services/pedidoClienteService';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

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

  // Estados de puntos de venta
  const [puntosDeVenta, setPuntosDeVenta] = useState([]);
  const [loadingPdv, setLoadingPdv] = useState(false);
  const [showPdvModal, setShowPdvModal] = useState(false);
  const [localidades, setLocalidades] = useState([]);
  const [cargandoLocalidades, setCargandoLocalidades] = useState(false);
  const [guardandoPdv, setGuardandoPdv] = useState(false);
  const [errorPdv, setErrorPdv] = useState('');
  const [pdvForm, setPdvForm] = useState({
    nombreComercio: '',
    calle: '',
    numeracion: '',
    barrio: '',
    manzanaPiso: '',
    casaDepartamento: '',
    referencia: '',
    localidadId: '',
    latitud: -32.8895,
    longitud: -68.8458,
  });

  const cargarMisPuntosDeVenta = useCallback(async () => {
    setLoadingPdv(true);
    try {
      const res = await authService.listarMisPuntosDeVenta();
      const list = res?.elementos || res?.content || (Array.isArray(res) ? res : []);
      setPuntosDeVenta(list);
    } catch (err) {
      console.error('Error al listar puntos de venta:', err);
      setPuntosDeVenta([]);
    } finally {
      setLoadingPdv(false);
    }
  }, []);

  const cargarLocalidades = useCallback(async () => {
    setCargandoLocalidades(true);
    try {
      const locs = await authService.listarLocalidades();
      const arr = Array.isArray(locs) ? locs : locs?.content || locs?.elementos || [];
      setLocalidades(arr);
      if (arr.length > 0) {
        setPdvForm((prev) => ({
          ...prev,
          localidadId: prev.localidadId || arr[0].id,
        }));
      }
    } catch (err) {
      console.error('Error al listar localidades:', err);
      setLocalidades([]);
    } finally {
      setCargandoLocalidades(false);
    }
  }, []);

  useEffect(() => {
    cargarMisPuntosDeVenta();
    cargarLocalidades();
  }, [cargarMisPuntosDeVenta, cargarLocalidades]);

  const handleCrearPuntoDeVenta = async (e) => {
    e.preventDefault();
    setErrorPdv('');
    if (!pdvForm.nombreComercio.trim()) {
      setErrorPdv('El nombre del comercio es obligatorio.');
      return;
    }
    if (!pdvForm.calle.trim()) {
      setErrorPdv('La calle es obligatoria.');
      return;
    }
    if (!pdvForm.numeracion.trim()) {
      setErrorPdv('La numeración es obligatoria.');
      return;
    }
    if (!pdvForm.localidadId) {
      setErrorPdv('Debe seleccionar una localidad.');
      return;
    }

    setGuardandoPdv(true);
    try {
      await authService.crearPuntoDeVenta({
        nombreComercio: pdvForm.nombreComercio.trim(),
        minoristaId: perfil?.id || null,
        direccion: {
          calle: pdvForm.calle.trim(),
          numeracion: pdvForm.numeracion.trim(),
          barrio: pdvForm.barrio.trim() || null,
          manzanaPiso: pdvForm.manzanaPiso.trim() || null,
          casaDepartamento: pdvForm.casaDepartamento.trim() || null,
          referencia: pdvForm.referencia.trim() || null,
          latitud: pdvForm.latitud ? parseFloat(pdvForm.latitud) : -32.8895,
          longitud: pdvForm.longitud ? parseFloat(pdvForm.longitud) : -68.8458,
          localidadId: pdvForm.localidadId,
        },
      });

      showToast('¡Punto de venta registrado con éxito!');
      setShowPdvModal(false);
      setPdvForm({
        nombreComercio: '',
        calle: '',
        numeracion: '',
        barrio: '',
        manzanaPiso: '',
        casaDepartamento: '',
        referencia: '',
        localidadId: localidades[0]?.id || '',
        latitud: -32.8895,
        longitud: -68.8458,
      });
      await cargarMisPuntosDeVenta();
    } catch (err) {
      setErrorPdv(err.message || 'Error al registrar el punto de venta.');
    } finally {
      setGuardandoPdv(false);
    }
  };

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

  const [orders, setOrders] = useState([]);
  const [loadingOrders, setLoadingOrders] = useState(true);

  const cargarPedidos = useCallback(() => {
    setLoadingOrders(true);
    const localOrders = pedidoClienteService.obtenerPedidos();
    const localPorId = {};
    localOrders.forEach((lo) => {
      if (lo.rawId) localPorId[lo.rawId] = lo;
      if (lo.id) localPorId[lo.id] = lo;
    });

    Promise.all([
      apiFetch('/mi-cuenta/pedidos?size=50').catch(() => ({ elementos: [] })),
      apiFetch('/mi-cuenta/facturas?size=50').catch(() => ({ elementos: [] })),
    ])
      .then(([pedRes, factRes]) => {
        const listaPed = pedRes?.elementos || pedRes?.content || (Array.isArray(pedRes) ? pedRes : []);
        const listaFact = factRes?.elementos || factRes?.content || (Array.isArray(factRes) ? factRes : []);
        const factPorPedido = {};
        listaFact.forEach((f) => {
          if (f.pedidoId) factPorPedido[f.pedidoId] = f;
        });

        const mapped = listaPed.map((p) => {
          const factura = factPorPedido[p.id];
          const localMatch = localPorId[p.id] || localPorId[`DM-${p.numeroPedido}`] || localPorId[p.numeroPedido];
          const fechaStr = p.fechaHoraCarga
            ? new Date(p.fechaHoraCarga).toLocaleString('es-AR')
            : '-';
          const dir = p.puntoDeVenta?.direccion;
          const direccionEntrega = dir
            ? `${dir.calle || ''} ${dir.numeracion || ''}, ${dir.localidadNombre || dir.departamentoNombre || 'Mendoza'}`.trim()
            : p.puntoDeVenta?.nombreComercio || 'Mendoza';

          return {
            id: p.numeroPedido || p.id,
            rawId: p.id,
            facturaId: factura?.id || p.facturaId || null,
            fecha: fechaStr,
            total: `$${Number(p.total || 0).toLocaleString('es-AR')}`,
            subtotal: `$${Number(p.subtotal || p.total || 0).toLocaleString('es-AR')}`,
            descuento: `$${Number(p.descuento || 0).toLocaleString('es-AR')}`,
            estado: p.estado || localMatch?.estado || 'CARGADO',
            facturaNum: factura?.numeroFactura
              ? `FA-${String(factura.numeroFactura).padStart(8, '0')}`
              : 'Pendiente de emisión',
            medioPago: localMatch?.medioPago || localMatch?.metodoPago || 'Mayorista',
            direccionEntrega,
            items: (p.detalles || []).map((d) => ({
              nombre: d.productoNombre || d.producto?.nombre || 'Producto',
              cant: d.cantidad || 1,
              precioUnit: `$${Number(d.precioUnitario || 0).toLocaleString('es-AR')}`,
              subtotal: `$${Number(d.subtotal || 0).toLocaleString('es-AR')}`,
            })),
            trazabilidad: [
              { estado: 'Pedido Confirmado', fecha: fechaStr, done: true },
              {
                estado: 'Facturado en Depósito',
                fecha: p.estado === 'FACTURADO' || p.estado === 'EN_REPARTO' || p.estado === 'ENTREGADO' ? 'Completado' : 'Pendiente',
                done: p.estado === 'FACTURADO' || p.estado === 'EN_REPARTO' || p.estado === 'ENTREGADO',
              },
              {
                estado: 'En Reparto con Distribuidor',
                fecha: p.estado === 'EN_REPARTO' || p.estado === 'ENTREGADO' ? 'Completado' : 'Pendiente',
                done: p.estado === 'EN_REPARTO' || p.estado === 'ENTREGADO',
              },
              {
                estado: 'Entregado en Comercio',
                fecha: p.estado === 'ENTREGADO' ? 'Completado' : 'Pendiente',
                done: p.estado === 'ENTREGADO',
              },
            ],
          };
        });
        setOrders(mapped);
      })
      .finally(() => {
        setLoadingOrders(false);
      });
  }, []);

  useEffect(() => {
    cargarPedidos();
    window.addEventListener('dm-pedido-actualizado', cargarPedidos);
    return () => window.removeEventListener('dm-pedido-actualizado', cargarPedidos);
  }, [cargarPedidos]);

  const handleOpenDetail = (order) => {
    setSelectedOrder(order);
    setShowDetailModal(true);
  };

  const handleOpenQr = (order) => {
    setSelectedOrder(order);
    setShowQrModal(true);
  };

  const handleDownloadPdf = async (order) => {
    if (!order?.facturaId) {
      showToast('Este pedido aún no posee factura emitida para descargar.');
      return;
    }
    try {
      const token = localStorage.getItem('dm_institucional_token') || localStorage.getItem('dm_token');
      const res = await fetch(`${API_BASE_URL}/facturas/${order.facturaId}/pdf`, {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${order.facturaNum || 'factura'}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      showToast(`No se pudo descargar la factura: ${err.message}`);
    }
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
          onClick={() => setActiveTab('puntos-venta')}
          className={`pb-3 transition-colors cursor-pointer ${
            activeTab === 'puntos-venta'
              ? 'text-[#e86bc4] border-b-2 border-[#e86bc4]'
              : 'text-[#6b6b80] hover:text-[#2b2b3a]'
          }`}
        >
          Puntos de Venta ({puntosDeVenta.length})
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

          {debeCambiarClave && (
            <div className="p-4 rounded-[16px] bg-[#fff6e0] border border-[#ffd98a] text-[#8a6519] text-[12px] flex items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <span className="text-xl">⚠️</span>
                <div>
                  <p className="font-bold text-[13px]">No podés ver tu historial de pedidos ni operar</p>
                  <p className="text-[11px] opacity-90 mt-0.5">
                    Tu usuario fue dado de alta con una contraseña temporal. Por seguridad, debés modificarla en la pestaña "Seguridad y Clave" para desbloquear tus pedidos y comprobantes.
                  </p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setActiveTab('seguridad')}
                className="shrink-0 bg-[#8a6519] text-white px-3.5 py-1.5 rounded-[10px] font-semibold text-[11px] hover:bg-[#715214] transition-colors cursor-pointer"
              >
                Cambiar clave ahora →
              </button>
            </div>
          )}

          {loadingOrders ? (
            <div className="py-10 text-center text-[12px] text-[#6b6b80]">
              Cargando historial de pedidos...
            </div>
          ) : debeCambiarClave ? (
            <div className="py-10 text-center text-[13px] text-[#6b6b80] space-y-2">
              <span className="text-3xl block">🔒</span>
              <p className="font-semibold text-[#2b2b3a]">Historial de pedidos bloqueado temporalmente</p>
              <p className="text-[12px] max-w-md mx-auto">
                Para consultar los pedidos tomados en tu comercio y descargar tus facturas, primero cambiá tu clave en la pestaña <strong>Seguridad y Clave</strong>.
              </p>
            </div>
          ) : orders.length === 0 ? (
            <div className="py-10 text-center text-[13px] text-[#6b6b80]">
              No tenés pedidos registrados en tu historial todavía.
            </div>
          ) : (
            <div className="space-y-3.5">
              {orders.map((p) => (
                <div
                  key={p.rawId || p.id}
                  className="p-5 rounded-[18px] border border-[#ececf2] flex flex-col md:flex-row items-start md:items-center justify-between gap-4 bg-[#fdfdfd] hover:border-[#e86bc4]/50 transition-colors"
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2.5 flex-wrap">
                      <strong className="text-[#2b2b3a] text-[14px]">Pedido #{p.id}</strong>
                      <span className="text-[11px] text-[#6b6b80]">· {p.fecha}</span>
                      <span
                        className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full ${
                          p.estado === 'EN_REPARTO' || p.estado === 'En camino' || p.estado === 'Listo para reparto'
                            ? 'bg-[#d9f7ff] text-[#247f99]'
                            : p.estado === 'Pendiente de Pago' || p.estado === 'CARGADO'
                            ? 'bg-[#faf0d9] text-[#b88014]'
                            : p.estado === 'Pago Rechazado' || p.estado === 'ANULADO'
                            ? 'bg-[#fee2e2] text-[#b91c1c]'
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
                        onClick={() => handleDownloadPdf(p)}
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
          )}
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

              {/* Datos inmutables: documento y fecha de nacimiento solo lectura */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="p-3.5 rounded-[12px] bg-[#f8f9fc] border border-[#ececf2]">
                  <span className="text-[10px] font-bold uppercase text-[#6b6b80] block mb-1">
                    Documento de Identidad (No editable)
                  </span>
                  <span className="text-[12px] font-semibold text-[#2b2b3a]">
                    {perfil?.tipoDocumento} {perfil?.numeroDocumento}
                  </span>
                </div>
                <div className="p-3.5 rounded-[12px] bg-[#f8f9fc] border border-[#ececf2]">
                  <span className="text-[10px] font-bold uppercase text-[#6b6b80] block mb-1">
                    Fecha de Nacimiento (No editable)
                  </span>
                  <span className="text-[12px] font-semibold text-[#2b2b3a]">
                    {perfil?.fechaNacimiento || 'Sin fecha informada'}
                  </span>
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

      {/* Tab content: Puntos de Venta del Minorista */}
      {activeTab === 'puntos-venta' && (
        <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-sm space-y-6">
          <div className="flex flex-col sm:flex-row justify-between sm:items-center gap-3 pb-3 border-b border-[#f0f0f4]">
            <div>
              <h3 className="text-[16px] font-bold text-[#2b2b3a]">Locales y Puntos de Venta</h3>
              <p className="text-[11px] text-[#6b6b80]">
                Administrá las direcciones y comercios donde recibís las entregas de tus pedidos mayoristas.
              </p>
            </div>
            <Button
              onClick={() => {
                setErrorPdv('');
                cargarLocalidades();
                setShowPdvModal(true);
              }}
              className="text-[12px] py-2 px-4 shrink-0 flex items-center gap-1.5"
            >
              <span>+</span>
              <span>Nuevo punto de venta</span>
            </Button>
          </div>

          {loadingPdv ? (
            <div className="py-10 text-center text-[#6b6b80] text-[13px] flex items-center justify-center gap-2">
              <span className="w-4 h-4 border-2 border-[#e86bc4] border-t-transparent rounded-full animate-spin"></span>
              Cargando puntos de venta...
            </div>
          ) : puntosDeVenta.length === 0 ? (
            <div className="py-12 px-4 text-center rounded-[16px] bg-[#fafafd] border border-[#ececf2] space-y-3">
              <div className="w-12 h-12 rounded-full bg-[#ffe2f7] text-[#e86bc4] flex items-center justify-center mx-auto text-xl font-bold">
                🏪
              </div>
              <h4 className="text-[14px] font-bold text-[#2b2b3a]">No tenés puntos de venta registrados</h4>
              <p className="text-[12px] text-[#6b6b80] max-w-md mx-auto">
                Registrá tu primer comercio o kiosco con su dirección física para asignarle envíos al hacer tus pedidos.
              </p>
              <Button
                onClick={() => {
                  setErrorPdv('');
                  cargarLocalidades();
                  setShowPdvModal(true);
                }}
                className="text-[12px] py-2 px-4"
              >
                Agregar mi primer punto de venta
              </Button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {puntosDeVenta.map((pdv, idx) => {
                const dir = pdv.direccion;
                const dirTexto = dir
                  ? `${dir.calle || ''} ${dir.numeracion || ''} ${dir.barrio ? `(${dir.barrio})` : ''}`.trim()
                  : 'Sin dirección';
                const localidadTexto = dir?.localidadNombre || dir?.departamentoNombre || 'Mendoza';
                const preventorTexto = pdv.preventor
                  ? `${pdv.preventor.nombre || ''} ${pdv.preventor.apellido || ''}`.trim()
                  : 'Asignación automática por zona';

                return (
                  <div
                    key={pdv.id || idx}
                    className="p-5 rounded-[18px] border border-[#ececf2] bg-[#fdfdfd] hover:border-[#e86bc4]/40 transition-all flex flex-col justify-between space-y-3"
                  >
                    <div>
                      <div className="flex items-center justify-between mb-1.5">
                        <strong className="text-[14px] font-bold text-[#2b2b3a]">
                          {pdv.nombreComercio}
                        </strong>
                        <span className="text-[10px] bg-[#dff8e9] text-[#39855b] font-semibold px-2.5 py-0.5 rounded-full">
                          Activo
                        </span>
                      </div>
                      <p className="text-[12px] text-[#6b6b80] flex items-start gap-1.5 mt-2">
                        <Icon name="mapPin" className="w-3.5 h-3.5 text-[#3fb8d9] shrink-0 mt-0.5" />
                        <span>{dirTexto}, {localidadTexto}</span>
                      </p>
                      {dir?.referencia && (
                        <p className="text-[11px] text-[#8c8b9c] italic ml-5 mt-1">
                          Ref: {dir.referencia}
                        </p>
                      )}
                    </div>

                    <div className="pt-2 border-t border-[#f0f0f4] flex items-center justify-between text-[11px] text-[#6b6b80]">
                      <span>Preventor: <strong className="text-[#2b2b3a]">{preventorTexto}</strong></span>
                      <span className="font-mono text-[10px] text-[#aaa9ba]">ID #{pdv.id?.slice(0, 8)}</span>
                    </div>
                  </div>
                );
              })}
            </div>
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
                onClick={() => handleDownloadPdf(selectedOrder)}
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

      {/* Modal Alta de Punto de Venta */}
      {showPdvModal && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in duration-150">
          <div className="bg-white rounded-[24px] max-w-lg w-full overflow-hidden shadow-2xl border border-[#ececf2] p-6 space-y-4 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between pb-3 border-b border-[#f0f0f4]">
              <div>
                <h3 className="text-[16px] font-bold text-[#2b2b3a]">Nuevo Punto de Venta</h3>
                <p className="text-[11px] text-[#6b6b80]">Ingresá los datos de tu comercio o local físico.</p>
              </div>
              <button
                type="button"
                onClick={() => setShowPdvModal(false)}
                className="text-gray-400 hover:text-gray-600 text-lg cursor-pointer"
              >
                ✕
              </button>
            </div>

            {errorPdv && (
              <div className="p-3 bg-[#ffe8ed] text-[#ff8fa3] rounded-[12px] text-[12px] font-medium border border-[#ff8fa3]/30">
                {errorPdv}
              </div>
            )}

            <form onSubmit={handleCrearPuntoDeVenta} className="space-y-3">
              <div>
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                  Nombre de Fantasía o Comercio *
                </label>
                <input
                  type="text"
                  required
                  placeholder="Ej. Kiosco San Martín, Minimarket Central"
                  value={pdvForm.nombreComercio}
                  onChange={(e) => setPdvForm({ ...pdvForm, nombreComercio: e.target.value })}
                  disabled={guardandoPdv}
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div className="sm:col-span-2">
                  <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                    Calle *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="Av. San Martín, Belgrano, etc."
                    value={pdvForm.calle}
                    onChange={(e) => setPdvForm({ ...pdvForm, calle: e.target.value })}
                    disabled={guardandoPdv}
                    className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                    Numeración *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="1234 o S/N"
                    value={pdvForm.numeracion}
                    onChange={(e) => setPdvForm({ ...pdvForm, numeracion: e.target.value })}
                    disabled={guardandoPdv}
                    className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                    Barrio (opcional)
                  </label>
                  <input
                    type="text"
                    placeholder="Ej. Centro, Bombal"
                    value={pdvForm.barrio}
                    onChange={(e) => setPdvForm({ ...pdvForm, barrio: e.target.value })}
                    disabled={guardandoPdv}
                    className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                    Localidad *
                  </label>
                  <select
                    required
                    value={pdvForm.localidadId}
                    onChange={(e) => setPdvForm({ ...pdvForm, localidadId: e.target.value })}
                    disabled={guardandoPdv || cargandoLocalidades}
                    className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  >
                    <option value="">{cargandoLocalidades ? 'Cargando localidades...' : 'Seleccione una localidad...'}</option>
                    {localidades.map((loc) => (
                      <option key={loc.id} value={loc.id}>
                        {loc.nombre} {loc.codigoPostal ? `(CP ${loc.codigoPostal})` : ''}
                      </option>
                    ))}
                  </select>
                  {localidades.length === 0 && !cargandoLocalidades && (
                    <div className="flex items-center justify-between mt-1">
                      <span className="text-[11px] text-[#ff8fa3]">No se encontraron localidades.</span>
                      <button
                        type="button"
                        onClick={cargarLocalidades}
                        className="text-[11px] text-[#e86bc4] underline cursor-pointer hover:opacity-80"
                      >
                        Reintentar
                      </button>
                    </div>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                    Manzana / Piso
                  </label>
                  <input
                    type="text"
                    placeholder="Ej. Piso 1 o Mza B"
                    value={pdvForm.manzanaPiso}
                    onChange={(e) => setPdvForm({ ...pdvForm, manzanaPiso: e.target.value })}
                    disabled={guardandoPdv}
                    className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                    Casa / Departamento
                  </label>
                  <input
                    type="text"
                    placeholder="Ej. Casa 4 o Dpto B"
                    value={pdvForm.casaDepartamento}
                    onChange={(e) => setPdvForm({ ...pdvForm, casaDepartamento: e.target.value })}
                    disabled={guardandoPdv}
                    className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  />
                </div>
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                  Referencia de entrega
                </label>
                <input
                  type="text"
                  placeholder="Ej. Portón verde frente a la plaza, timbre 2"
                  value={pdvForm.referencia}
                  onChange={(e) => setPdvForm({ ...pdvForm, referencia: e.target.value })}
                  disabled={guardandoPdv}
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>

              <div className="pt-3 border-t border-[#f0f0f4] flex gap-2 justify-end">
                <Button
                  secondary
                  type="button"
                  disabled={guardandoPdv}
                  onClick={() => setShowPdvModal(false)}
                  className="text-[12px] py-2 px-4"
                >
                  Cancelar
                </Button>
                <Button
                  type="submit"
                  disabled={guardandoPdv}
                  className="text-[12px] py-2 px-5"
                >
                  {guardandoPdv ? 'Guardando...' : 'Crear punto de venta'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
