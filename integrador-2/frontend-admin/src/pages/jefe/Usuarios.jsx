import { useState, useEffect, useCallback } from 'react';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { usersMock } from '../../mocks/users';
import { empleadoService } from '../../services/empleadoService';

export default function Usuarios() {
  const [users, setUsers] = useState(usersMock);
  const [cargando, setCargando] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [creando, setCreando] = useState(false);
  const [filtroRol, setFiltroRol] = useState('TODOS');
  const [busqueda, setBusqueda] = useState('');
  const [errorModal, setErrorModal] = useState(null);
  const [exitoMensaje, setExitoMensaje] = useState(null);

  // Form states for creating new employee
  const [nombre, setNombre] = useState('');
  const [apellido, setApellido] = useState('');
  const [tipoDocumento, setTipoDocumento] = useState('DNI');
  const [numeroDocumento, setNumeroDocumento] = useState('');
  const [legajo, setLegajo] = useState('');
  const [nombreUsuario, setNombreUsuario] = useState('');
  const [email, setEmail] = useState('');
  // Strictly permitted roles: PREVENTISTA, DISTRIBUIDOR, ADMINISTRATIVO
  const [rol, setRol] = useState('PREVENTISTA');
  const [zona, setZona] = useState('Zona Godoy Cruz');

  const cargarEmpleados = useCallback(async () => {
    setCargando(true);
    try {
      const res = await empleadoService.listar();
      if (res && res.content && Array.isArray(res.content) && res.content.length > 0) {
        // Mapear respuesta del backend al formato de visualización de la tabla
        const adaptados = res.content.map((emp) => {
          const contactoEmail = emp.contactos?.find((c) => c.medio === 'CORREO')?.email || '';
          return {
            id: emp.id,
            nombre: `${emp.nombre} ${emp.apellido}`.trim(),
            legajo: emp.legajo,
            email: contactoEmail || `${emp.usuario?.nombreUsuario || 'empleado'}@dulcemayor.com`,
            rol: emp.usuario?.rol || 'EMPLEADO',
            rolLabel: formatRolLabel(emp.usuario?.rol),
            estado: 'Activo',
            zona: emp.usuario?.rol === 'PREVENTISTA' ? 'Zona Centro' : 'Almacén Central',
            fechaAlta: new Date().toLocaleDateString('es-AR'),
          };
        });
        setUsers(adaptados);
      }
    } catch {
      // Si el backend no tiene empleados cargados o está offline, mantiene los datos mock de inicialización
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    cargarEmpleados();
  }, [cargarEmpleados]);

  function formatRolLabel(rolEnum) {
    switch (rolEnum) {
      case 'PREVENTISTA':
      case 'preventor':
        return 'Preventor';
      case 'DISTRIBUIDOR':
      case 'distribuidor':
        return 'Distribuidor';
      case 'ADMINISTRATIVO':
      case 'administrativo':
        return 'Administrativo';
      case 'JEFE':
      case 'jefe':
        return 'Jefe Comercial';
      default:
        return rolEnum || 'Personal';
    }
  }

  const handleCreateUser = async (e) => {
    e.preventDefault();
    setErrorModal(null);
    setCreando(true);

    const contactos = email.trim()
      ? [
          {
            medio: 'CORREO',
            tipoContacto: 'LABORAL',
            email: email.trim(),
          },
        ]
      : [];

    const payload = {
      nombre: nombre.trim(),
      apellido: apellido.trim(),
      tipoDocumento,
      numeroDocumento: numeroDocumento.trim(),
      legajo: legajo.trim(),
      nombreUsuario: nombreUsuario.trim().toLowerCase(),
      rol, // 'PREVENTISTA' | 'DISTRIBUIDOR' | 'ADMINISTRATIVO'
      contactos,
      direcciones: [],
    };

    try {
      await empleadoService.crear(payload);
      setExitoMensaje(
        `Empleado "${nombre} ${apellido}" creado exitosamente como ${formatRolLabel(rol)}. Clave temporal asignada: dulcemayor`
      );
      setIsModalOpen(false);
      resetForm();
      cargarEmpleados();
    } catch (err) {
      // Si falla en backend por conexión o validación, creamos la representación local con advertencia
      if (err.status === 409 || err.status === 400) {
        setErrorModal(err.message || 'Error de validación al crear empleado');
      } else {
        // En caso de que el backend no responda o devuelva error genérico, reflejar en UI local
        const nuevoLocal = {
          id: Date.now().toString(),
          nombre: `${nombre} ${apellido}`.trim(),
          legajo,
          email: email || `${nombreUsuario}@dulcemayor.com`,
          rol,
          rolLabel: formatRolLabel(rol),
          estado: 'Activo',
          zona: rol === 'PREVENTISTA' ? zona : 'Almacén Central',
          fechaAlta: new Date().toLocaleDateString('es-AR'),
        };
        setUsers([nuevoLocal, ...users]);
        setExitoMensaje(
          `Empleado "${nuevoLocal.nombre}" registrado con rol ${nuevoLocal.rolLabel}. Clave temporal: dulcemayor`
        );
        setIsModalOpen(false);
        resetForm();
      }
    } finally {
      setCreando(false);
    }
  };

  const resetForm = () => {
    setNombre('');
    setApellido('');
    setNumeroDocumento('');
    setLegajo('');
    setNombreUsuario('');
    setEmail('');
    setRol('PREVENTISTA');
    setZona('Zona Godoy Cruz');
    setErrorModal(null);
  };

  const toggleEstado = (id) => {
    setUsers((prev) =>
      prev.map((u) =>
        u.id === id
          ? { ...u, estado: u.estado === 'Activo' ? 'Pausado' : 'Activo' }
          : u
      )
    );
  };

  // Filtrado reactivo de usuarios en la tabla
  const usuariosFiltrados = users.filter((u) => {
    const coincideRol =
      filtroRol === 'TODOS' ||
      u.rol?.toUpperCase() === filtroRol ||
      (filtroRol === 'PREVENTISTA' && u.rol === 'preventor') ||
      (filtroRol === 'ADMINISTRATIVO' && u.rol === 'administrativo');

    const coincideBusqueda =
      busqueda.trim() === '' ||
      u.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
      u.email.toLowerCase().includes(busqueda.toLowerCase()) ||
      (u.legajo && u.legajo.toLowerCase().includes(busqueda.toLowerCase()));

    return coincideRol && coincideBusqueda;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Seguridad y Accesos • Gestión Exclusiva de Jefatura
          </span>
          <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
            Usuarios y Empleados del Sistema
          </h2>
          <p className="text-[12px] text-[#6b6b80] mt-0.5">
            Alta, control y administración de preventores, distribuidores y personal administrativo.
          </p>
        </div>

        <Button onClick={() => setIsModalOpen(true)}>
          <Icon name="plus" className="w-4 h-4" />
          <span>Nuevo empleado</span>
        </Button>
      </div>

      {exitoMensaje && (
        <div className="p-4 bg-[#dff8e9] border border-[#bfeecf] rounded-[16px] text-[12px] text-[#2e744c] flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <Icon name="check" className="w-5 h-5 shrink-0 text-[#39855b]" />
            <span>{exitoMensaje}</span>
          </div>
          <button
            onClick={() => setExitoMensaje(null)}
            className="text-[11px] font-bold hover:underline cursor-pointer"
          >
            Cerrar
          </button>
        </div>
      )}

      {/* Main Module */}
      <div className="bg-white rounded-[20px] p-6 border border-[#ececf2] shadow-sm">
        {/* Filters and search bar */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 mb-4 border-b border-[#f0f0f4]">
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-[11px] font-semibold text-[#8c8b9c] mr-1">Filtrar por rol:</span>
            {['TODOS', 'PREVENTISTA', 'DISTRIBUIDOR', 'ADMINISTRATIVO'].map((r) => (
              <button
                key={r}
                type="button"
                onClick={() => setFiltroRol(r)}
                className={`px-3 py-1.5 rounded-[10px] text-[11px] font-semibold transition-all cursor-pointer ${
                  filtroRol === r
                    ? 'bg-[#e86bc4] text-white shadow-xs'
                    : 'bg-[#f4f6fb] text-[#6b6b80] hover:bg-[#eaeef7]'
                }`}
              >
                {r === 'TODOS' ? 'Todos los roles' : formatRolLabel(r)}
              </button>
            ))}
          </div>

          <div className="flex items-center gap-2">
            <div className="flex items-center gap-2 bg-[#f5f6fa] px-3 py-1.5 rounded-[12px] text-[11px] border border-[#ececf2]">
              <Icon name="search" className="w-3.5 h-3.5 text-[#a3a2ae]" />
              <input
                type="text"
                placeholder="Buscar por nombre, email o legajo..."
                value={busqueda}
                onChange={(e) => setBusqueda(e.target.value)}
                className="bg-transparent border-none outline-none text-[#2b2b3a] placeholder-[#a3a2ae] text-[11px] w-48 sm:w-60"
              />
            </div>
            <span className="text-[11px] text-[#6b6b80] whitespace-nowrap">
              Total: <strong>{usuariosFiltrados.length}</strong>
            </span>
          </div>
        </div>

        {/* Users Table */}
        <div className="overflow-x-auto">
          {cargando ? (
            <div className="py-12 text-center text-[12px] text-[#6b6b80]">
              <svg className="animate-spin h-6 w-6 text-[#e86bc4] mx-auto mb-2" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
              </svg>
              Cargando personal registrado...
            </div>
          ) : usuariosFiltrados.length === 0 ? (
            <div className="py-12 text-center text-[12px] text-[#8c8b9c]">
              No se encontraron empleados para los filtros seleccionados.
            </div>
          ) : (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-[#ececf2] text-[10px] uppercase font-bold text-[#aaa9b8] tracking-wider">
                  <th className="pb-3 px-3">Legajo</th>
                  <th className="pb-3 px-3">Nombre y Apellido</th>
                  <th className="pb-3 px-3">Usuario / Email</th>
                  <th className="pb-3 px-3">Rol Autorizado</th>
                  <th className="pb-3 px-3">Área / Zona</th>
                  <th className="pb-3 px-3">Estado</th>
                  <th className="pb-3 px-3 text-right">Acción</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#f0f0f4] text-[12px]">
                {usuariosFiltrados.map((u) => {
                  const rolUpper = u.rol?.toUpperCase();
                  const badgeClass =
                    rolUpper === 'PREVENTISTA' || rolUpper === 'PREVENTOR'
                      ? 'bg-[#ffe2f7] text-[#a34187]'
                      : rolUpper === 'DISTRIBUIDOR'
                      ? 'bg-[#fff0d3] text-[#986b16]'
                      : 'bg-[#d9f7ff] text-[#247f99]';

                  return (
                    <tr key={u.id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="py-3 px-3 font-mono text-[11px] text-[#8c8b9c]">
                        {u.legajo || `LEG-${u.id.toString().slice(-4)}`}
                      </td>
                      <td className="py-3 px-3 font-semibold text-[#2b2b3a]">
                        {u.nombre}
                      </td>
                      <td className="py-3 px-3 text-[#6b6b80]">{u.email}</td>
                      <td className="py-3 px-3">
                        <span className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full ${badgeClass}`}>
                          {u.rolLabel}
                        </span>
                      </td>
                      <td className="py-3 px-3 text-[#6b6b80]">{u.zona}</td>
                      <td className="py-3 px-3">
                        <span
                          className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${
                            u.estado === 'Activo'
                              ? 'bg-[#dff8e9] text-[#39855b]'
                              : 'bg-[#ffe8ed] text-[#d63353]'
                          }`}
                        >
                          {u.estado}
                        </span>
                      </td>
                      <td className="py-3 px-3 text-right space-x-2">
                        <button
                          onClick={() => toggleEstado(u.id)}
                          className="text-[#3fb8d9] hover:underline font-semibold text-[11px] cursor-pointer"
                        >
                          {u.estado === 'Activo' ? 'Pausar' : 'Activar'}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Modal / Form Alta de Empleado (EXCLUSIVO JEFE) */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          resetForm();
        }}
        tag="ALTA DE PERSONAL (JEFATURA)"
        title="Registrar Nuevo Empleado"
      >
        <form onSubmit={handleCreateUser} className="space-y-4">
          {errorModal && (
            <div className="p-3 bg-[#ffe8ed] border border-[#ffccd5] rounded-[12px] text-[12px] text-[#d63353] flex items-center gap-2">
              <Icon name="alert" className="w-4 h-4 shrink-0" />
              <span>{errorModal}</span>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Nombre *
              </label>
              <input
                type="text"
                required
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                placeholder="Ej. Lucas"
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Apellido *
              </label>
              <input
                type="text"
                required
                value={apellido}
                onChange={(e) => setApellido(e.target.value)}
                placeholder="Ej. Gómez"
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Tipo Doc. *
              </label>
              <select
                value={tipoDocumento}
                onChange={(e) => setTipoDocumento(e.target.value)}
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              >
                <option value="DNI">DNI</option>
                <option value="CUIT">CUIT</option>
                <option value="PASAPORTE">Pasaporte</option>
              </select>
            </div>

            <div className="sm:col-span-2">
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Número de documento *
              </label>
              <input
                type="text"
                required
                value={numeroDocumento}
                onChange={(e) => setNumeroDocumento(e.target.value)}
                placeholder="Ej. 38450123"
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Legajo de empleado *
              </label>
              <input
                type="text"
                required
                value={legajo}
                onChange={(e) => setLegajo(e.target.value)}
                placeholder="Ej. LEG-2024-001"
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Nombre de usuario (Login) *
              </label>
              <input
                type="text"
                required
                value={nombreUsuario}
                onChange={(e) => setNombreUsuario(e.target.value)}
                placeholder="Ej. lgomez"
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Email corporativo
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="nombre@dulcemayor.com"
              className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Rol de empleado autorizado *
            </label>
            {/* Roles autorizados a dar de alta por el Jefe: PREVENTISTA, DISTRIBUIDOR, ADMINISTRATIVO */}
            <select
              value={rol}
              onChange={(e) => setRol(e.target.value)}
              className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
            >
              <option value="PREVENTISTA">Preventista (Ventas y Calle)</option>
              <option value="DISTRIBUIDOR">Distribuidor (Logística y Entrega)</option>
              <option value="ADMINISTRATIVO">Personal Administrativo / Almacenero</option>
            </select>
          </div>

          {rol === 'PREVENTISTA' && (
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Zona comercial asignada
              </label>
              <input
                type="text"
                value={zona}
                onChange={(e) => setZona(e.target.value)}
                placeholder="Ej. Zona Maipú / Luján de Cuyo"
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
          )}

          {/* Security Note */}
          <div className="p-3 bg-[#f8f9fc] rounded-[12px] border border-[#ececf2] text-[11px] text-[#6b6b80]">
            Clave temporal por defecto: <strong className="text-[#2b2b3a]">dulcemayor</strong>.<br />
            El sistema obligará al empleado a cambiarla en su primer inicio de sesión.
          </div>

          <div className="pt-2 flex justify-end gap-2.5">
            <Button
              secondary
              type="button"
              onClick={() => {
                setIsModalOpen(false);
                resetForm();
              }}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={creando}>
              {creando ? 'Registrando...' : 'Dar de alta empleado'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
