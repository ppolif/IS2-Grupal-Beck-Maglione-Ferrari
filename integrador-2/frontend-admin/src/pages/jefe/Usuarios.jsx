import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { useUsuarios } from '../../hooks/useUsuarios';
import { formatRolLabel, getBadgeClass } from '../../utils/formatters';

export default function Usuarios() {
  const {
    // Data & state
    mayoristas,
    cargando,
    errorGlobal,
    exitoMensaje,
    setExitoMensaje,
    cargarEmpleados,

    // Filters
    filtroRol,
    setFiltroRol,
    busqueda,
    setBusqueda,
    empleadosFiltrados,

    // Create Modal
    isCreateOpen,
    setIsCreateOpen,
    creando,
    errorCreate,
    nombre,
    setNombre,
    apellido,
    setApellido,
    tipoDocumento,
    setTipoDocumento,
    numeroDocumento,
    setNumeroDocumento,
    legajo,
    setLegajo,
    nombreUsuario,
    setNombreUsuario,
    email,
    setEmail,
    telefono,
    setTelefono,
    rol,
    setRol,
    mayoristaId,
    setMayoristaId,
    resetCreateForm,
    handleCreate,

    // Edit Modal
    isEditOpen,
    setIsEditOpen,
    guardando,
    errorEdit,
    setEmpleadoAEditar,
    editNombre,
    setEditNombre,
    editApellido,
    setEditApellido,
    editTipoDocumento,
    setEditTipoDocumento,
    editNumeroDocumento,
    setEditNumeroDocumento,
    editLegajo,
    setEditLegajo,
    editNombreUsuario,
    setEditNombreUsuario,
    editEmail,
    setEditEmail,
    editTelefono,
    setEditTelefono,
    editRol,
    editMayoristaId,
    setEditMayoristaId,
    openEditModal,
    handleEdit,

    // Delete Modal
    isDeleteOpen,
    setIsDeleteOpen,
    eliminando,
    errorDelete,
    empleadoAEliminar,
    setEmpleadoAEliminar,
    openDeleteModal,
    handleDelete,
  } = useUsuarios();

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-primary">
            Seguridad y Accesos • Gestión Exclusiva de Jefatura
          </span>
          <h2 className="text-xl font-bold text-ink mt-0.5">
            Usuarios y Empleados del Sistema
          </h2>
          <p className="text-[12px] text-muted mt-0.5">
            Alta, edición y administración de preventores, distribuidores y personal administrativo.
          </p>
        </div>

        <Button onClick={() => setIsCreateOpen(true)}>
          <Icon name="plus" className="w-4 h-4" />
          <span>Nuevo empleado</span>
        </Button>
      </div>

      {/* Banner de Éxito */}
      {exitoMensaje && (
        <div className="p-4 bg-success-bg border border-success-border rounded-[16px] text-[12px] text-success-text flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <Icon name="check" className="w-5 h-5 shrink-0 text-success-icon" />
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

      {/* Banner de Error Global */}
      {errorGlobal && (
        <div className="p-4 bg-danger-bg border border-danger-border rounded-[16px] text-[12px] text-danger-hover flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <Icon name="alert" className="w-5 h-5 shrink-0 text-danger-hover" />
            <span>{errorGlobal}</span>
          </div>
          <button
            onClick={cargarEmpleados}
            className="text-[11px] font-bold underline hover:no-underline cursor-pointer"
          >
            Reintentar
          </button>
        </div>
      )}

      {/* Main Module Card */}
      <div className="bg-white rounded-[20px] p-6 border border-border-subtle shadow-sm">
        {/* Filters and Search */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 mb-4 border-b border-divider">
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-[11px] font-semibold text-muted-light mr-1">Filtrar por rol:</span>
            {['TODOS', 'PREVENTISTA', 'DISTRIBUIDOR', 'ADMINISTRATIVO'].map((r) => (
              <button
                key={r}
                type="button"
                onClick={() => setFiltroRol(r)}
                className={`px-3 py-1.5 rounded-[10px] text-[11px] font-semibold transition-all cursor-pointer ${
                  filtroRol === r
                    ? 'bg-primary text-white shadow-xs'
                    : 'bg-canvas text-muted hover:bg-canvas-hover'
                }`}
              >
                {r === 'TODOS' ? 'Todos' : formatRolLabel(r)}
              </button>
            ))}
          </div>

          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2 bg-input-bg px-3 py-1.5 rounded-[12px] text-[11px] border border-border-subtle">
              <Icon name="search" className="w-3.5 h-3.5 text-muted-icon" />
              <input
                type="text"
                placeholder="Buscar por nombre, legajo o usuario..."
                value={busqueda}
                onChange={(e) => setBusqueda(e.target.value)}
                className="bg-transparent border-none outline-none text-ink placeholder-muted-icon text-[11px] w-48 sm:w-60"
              />
            </div>
            <span className="text-[11px] text-muted whitespace-nowrap">
              Total: <strong>{empleadosFiltrados.length}</strong>
            </span>
          </div>
        </div>

        {/* Table Content */}
        <div className="overflow-x-auto">
          {cargando ? (
            <div className="py-12 text-center text-[12px] text-muted">
              <svg className="animate-spin h-6 w-6 text-primary mx-auto mb-2" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
              </svg>
              Cargando personal registrado desde el backend...
            </div>
          ) : empleadosFiltrados.length === 0 ? (
            <div className="py-12 text-center text-[12px] text-muted-light">
              <Icon name="user" className="w-8 h-8 text-muted-subtle mx-auto mb-2" />
              No se encontraron empleados registrados con los criterios seleccionados.
            </div>
          ) : (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                  <th className="pb-3 px-3">Legajo</th>
                  <th className="pb-3 px-3">Empleado</th>
                  <th className="pb-3 px-3">Sede / Sucursal</th>
                  <th className="pb-3 px-3">Documento</th>
                  <th className="pb-3 px-3">Usuario</th>
                  <th className="pb-3 px-3">Rol Autorizado</th>
                  <th className="pb-3 px-3 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-divider text-[12px]">
                {empleadosFiltrados.map((emp) => {
                  const rolUsuario = emp.usuario?.rol;
                  const emailContacto = emp.contactos?.find((c) => c.medio === 'CORREO')?.email;
                  const telContacto = emp.contactos?.find((c) => c.medio === 'TELEFONO')?.telefono;

                  return (
                    <tr key={emp.id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="py-3 px-3 font-mono text-[11px] font-semibold text-muted">
                        {emp.legajo}
                      </td>
                      <td className="py-3 px-3">
                        <div className="font-semibold text-ink">
                          {emp.nombre} {emp.apellido}
                        </div>
                      </td>
                      <td className="py-3 px-3 text-[11px] text-muted">
                        {emp.mayoristaRazonSocial || 'Sede Central'}
                      </td>
                      <td className="py-3 px-3 text-muted text-[11px]">
                        <span className="font-semibold text-ink">{emp.tipoDocumento}</span> {emp.numeroDocumento}
                      </td>
                      <td className="py-3 px-3 text-[11px]">
                        <div className="font-medium text-ink">
                          {emp.usuario?.nombreUsuario || 'sin-usuario'}
                        </div>
                      </td>
                      <td className="py-3 px-3">
                        <span className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full ${getBadgeClass(rolUsuario)}`}>
                          {formatRolLabel(rolUsuario)}
                        </span>
                      </td>
                      <td className="py-3 px-3 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            onClick={() => openEditModal(emp)}
                            className="p-1.5 text-secondary hover:bg-secondary-light rounded-[8px] transition-colors cursor-pointer"
                            title="Editar empleado"
                          >
                            <Icon name="edit" className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => openDeleteModal(emp)}
                            className="p-1.5 text-danger hover:bg-danger-light rounded-[8px] transition-colors cursor-pointer"
                            title="Dar de baja / eliminar"
                          >
                            <Icon name="trash" className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* MODAL 1: ALTA DE EMPLEADO (EXCLUSIVO JEFE) */}
      <Modal
        isOpen={isCreateOpen}
        onClose={() => {
          setIsCreateOpen(false);
          resetCreateForm();
        }}
        tag="ALTA DE PERSONAL (JEFATURA)"
        title="Registrar Nuevo Empleado"
      >
        <form onSubmit={handleCreate} className="space-y-4">
          {errorCreate && (
            <div className="p-3 bg-danger-bg border border-danger-border rounded-[12px] text-[12px] text-danger-hover flex items-center gap-2">
              <Icon name="alert" className="w-4 h-4 shrink-0" />
              <span>{errorCreate}</span>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Nombre *
              </label>
              <input
                type="text"
                required
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                placeholder="Ej. Lucas"
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Apellido *
              </label>
              <input
                type="text"
                required
                value={apellido}
                onChange={(e) => setApellido(e.target.value)}
                placeholder="Ej. Gómez"
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Tipo Doc. *
              </label>
              <select
                value={tipoDocumento}
                onChange={(e) => setTipoDocumento(e.target.value)}
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              >
                <option value="DNI">DNI</option>
                <option value="CUIT">CUIT</option>
                <option value="PASAPORTE">Pasaporte</option>
              </select>
            </div>

            <div className="sm:col-span-2">
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Número de Documento *
              </label>
              <input
                type="text"
                required
                value={numeroDocumento}
                onChange={(e) => setNumeroDocumento(e.target.value)}
                placeholder="Ej. 38450123"
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Legajo de Empleado *
              </label>
              <input
                type="text"
                required
                value={legajo}
                onChange={(e) => setLegajo(e.target.value)}
                placeholder="Ej. LEG-2024-001"
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Nombre de Usuario (Email de Login) *
              </label>
              <input
                type="email"
                required
                value={nombreUsuario}
                onChange={(e) => {
                  const val = e.target.value;
                  setNombreUsuario(val);
                  if (!email || email === nombreUsuario) {
                    setEmail(val);
                  }
                }}
                placeholder="ejemplo@dulcemayor.com"
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
              <p className="text-[10px] text-muted-light mt-1">
                Debe ser el correo electrónico con el que el empleado iniciará sesión.
              </p>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Email Corporativo
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="lgomez@dulcemayor.com"
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Teléfono
              </label>
              <input
                type="text"
                value={telefono}
                onChange={(e) => setTelefono(e.target.value)}
                placeholder="Ej. +54 9 261 555-1234"
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Rol de Empleado Autorizado *
              </label>
              <select
                value={rol}
                onChange={(e) => setRol(e.target.value)}
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              >
                <option value="PREVENTISTA">Preventista (Ventas y Pedidos)</option>
                <option value="DISTRIBUIDOR">Distribuidor (Logística y Entrega)</option>
                <option value="ADMINISTRATIVO">Personal Administrativo</option>
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Sede Central o Sucursal Mayorista *
              </label>
              <select
                value={mayoristaId}
                onChange={(e) => setMayoristaId(e.target.value)}
                required
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              >
                {mayoristas.length === 0 ? (
                  <option value="">Cargando sedes...</option>
                ) : (
                  mayoristas.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.razonSocial || m.nombreFantasia || m.id} ({m.tipoEmpresa === 'SEDE_CENTRAL' ? 'Sede Central' : 'Sucursal'})
                    </option>
                  ))
                )}
              </select>
            </div>
          </div>

          <div className="p-3 bg-surface-note rounded-[12px] border border-border-subtle text-[11px] text-muted">
            Clave temporal inicial asignada: <strong className="text-ink">dulcemayor</strong>.<br />
            El sistema exigirá al empleado actualizarla en su primer inicio de sesión.
          </div>

          <div className="pt-2 flex justify-end gap-2.5">
            <Button
              secondary
              type="button"
              onClick={() => {
                setIsCreateOpen(false);
                resetCreateForm();
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

      {/* MODAL 2: EDICIÓN DE EMPLEADO */}
      <Modal
        isOpen={isEditOpen}
        onClose={() => {
          setIsEditOpen(false);
          setEmpleadoAEditar(null);
        }}
        tag="GESTIÓN DE EMPLEADO"
        title="Modificar Empleado"
      >
        <form onSubmit={handleEdit} className="space-y-4">
          {errorEdit && (
            <div className="p-3 bg-danger-bg border border-danger-border rounded-[12px] text-[12px] text-danger-hover flex items-center gap-2">
              <Icon name="alert" className="w-4 h-4 shrink-0" />
              <span>{errorEdit}</span>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Nombre *
              </label>
              <input
                type="text"
                required
                value={editNombre}
                onChange={(e) => setEditNombre(e.target.value)}
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Apellido *
              </label>
              <input
                type="text"
                required
                value={editApellido}
                onChange={(e) => setEditApellido(e.target.value)}
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Tipo Doc. *
              </label>
              <select
                value={editTipoDocumento}
                onChange={(e) => setEditTipoDocumento(e.target.value)}
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              >
                <option value="DNI">DNI</option>
                <option value="CUIT">CUIT</option>
                <option value="PASAPORTE">Pasaporte</option>
              </select>
            </div>

            <div className="sm:col-span-2">
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Número de Documento *
              </label>
              <input
                type="text"
                required
                value={editNumeroDocumento}
                onChange={(e) => setEditNumeroDocumento(e.target.value)}
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Legajo *
              </label>
              <input
                type="text"
                required
                value={editLegajo}
                onChange={(e) => setEditLegajo(e.target.value)}
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Nombre de Usuario (Email de Login) *
              </label>
              <input
                type="email"
                required
                value={editNombreUsuario}
                onChange={(e) => setEditNombreUsuario(e.target.value)}
                placeholder="ejemplo@dulcemayor.com"
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
              <p className="text-[10px] text-muted-light mt-1">
                Debe ser el correo electrónico con el que el empleado inicia sesión.
              </p>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Email Corporativo
              </label>
              <input
                type="email"
                value={editEmail}
                onChange={(e) => setEditEmail(e.target.value)}
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Teléfono
              </label>
              <input
                type="text"
                value={editTelefono}
                onChange={(e) => setEditTelefono(e.target.value)}
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Rol Asignado
              </label>
              <input
                type="text"
                readOnly
                disabled
                value={formatRolLabel(editRol)}
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-muted bg-surface-note cursor-not-allowed"
              />
              <p className="text-[10px] text-muted-light mt-1">
                La especialidad de la entidad está ligada a su registro inicial.
              </p>
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Sede Central o Sucursal Mayorista *
              </label>
              <select
                value={editMayoristaId}
                onChange={(e) => setEditMayoristaId(e.target.value)}
                required
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              >
                {mayoristas.length === 0 ? (
                  <option value="">Cargando sedes...</option>
                ) : (
                  mayoristas.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.razonSocial || m.nombreFantasia || m.id} ({m.tipoEmpresa === 'SEDE_CENTRAL' ? 'Sede Central' : 'Sucursal'})
                    </option>
                  ))
                )}
              </select>
            </div>
          </div>

          <div className="pt-2 flex justify-end gap-2.5">
            <Button
              secondary
              type="button"
              onClick={() => {
                setIsEditOpen(false);
                setEmpleadoAEditar(null);
              }}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Guardar Cambios'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* MODAL 3: ELIMINACIÓN DE EMPLEADO */}
      <Modal
        isOpen={isDeleteOpen}
        onClose={() => {
          setIsDeleteOpen(false);
          setEmpleadoAEliminar(null);
        }}
        tag="BAJA DE PERSONAL"
        title="Confirmar Baja de Empleado"
      >
        <div className="space-y-4">
          {errorDelete && (
            <div className="p-3 bg-danger-bg border border-danger-border rounded-[12px] text-[12px] text-danger-hover flex items-center gap-2">
              <Icon name="alert" className="w-4 h-4 shrink-0" />
              <span>{errorDelete}</span>
            </div>
          )}

          <p className="text-[13px] text-ink">
            ¿Estás seguro de que deseas dar de baja al empleado{' '}
            <strong>
              {empleadoAEliminar?.nombre} {empleadoAEliminar?.apellido}
            </strong>{' '}
            (Legajo: <code>{empleadoAEliminar?.legajo}</code>)?
          </p>

          <div className="p-3 bg-danger-bg rounded-[12px] border border-danger-border text-[11px] text-danger-hover">
            <strong>Atención:</strong> Esta acción eliminará el registro del empleado y revocará permanentemente su usuario de acceso al sistema (<code>@{empleadoAEliminar?.usuario?.nombreUsuario}</code>).
          </div>

          <div className="pt-2 flex justify-end gap-2.5">
            <Button
              secondary
              type="button"
              onClick={() => {
                setIsDeleteOpen(false);
                setEmpleadoAEliminar(null);
              }}
            >
              Cancelar
            </Button>
            <button
              type="button"
              disabled={eliminando}
              onClick={handleDelete}
              className="px-4 py-2 rounded-[14px] text-[12px] font-semibold bg-danger text-white hover:bg-danger-hover transition-all disabled:opacity-60 cursor-pointer"
            >
              {eliminando ? 'Eliminando...' : 'Sí, dar de baja'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
