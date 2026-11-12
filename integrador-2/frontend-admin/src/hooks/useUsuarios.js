import { useState, useEffect, useCallback } from 'react';
import { empleadoService } from '../services/empleadoService';
import { mayoristaService } from '../services/mayoristaService';

/**
 * Custom hook managing employee state, filters, API operations and modal workflows.
 */
export function useUsuarios() {
  const [empleados, setEmpleados] = useState([]);
  const [mayoristas, setMayoristas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [errorGlobal, setErrorGlobal] = useState(null);
  const [exitoMensaje, setExitoMensaje] = useState(null);

  // Filtros
  const [filtroRol, setFiltroRol] = useState('TODOS');
  const [busqueda, setBusqueda] = useState('');

  // Modal Alta
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [creando, setCreando] = useState(false);
  const [errorCreate, setErrorCreate] = useState(null);
  const [nombre, setNombre] = useState('');
  const [apellido, setApellido] = useState('');
  const [tipoDocumento, setTipoDocumento] = useState('DNI');
  const [numeroDocumento, setNumeroDocumento] = useState('');
  const [legajo, setLegajo] = useState('');
  const [nombreUsuario, setNombreUsuario] = useState('');
  const [email, setEmail] = useState('');
  const [telefono, setTelefono] = useState('');
  const [rol, setRol] = useState('PREVENTISTA');
  const [mayoristaId, setMayoristaId] = useState('');

  // Modal Edición
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [errorEdit, setErrorEdit] = useState(null);
  const [empleadoAEditar, setEmpleadoAEditar] = useState(null);
  const [editNombre, setEditNombre] = useState('');
  const [editApellido, setEditApellido] = useState('');
  const [editTipoDocumento, setEditTipoDocumento] = useState('DNI');
  const [editNumeroDocumento, setEditNumeroDocumento] = useState('');
  const [editLegajo, setEditLegajo] = useState('');
  const [editNombreUsuario, setEditNombreUsuario] = useState('');
  const [editEmail, setEditEmail] = useState('');
  const [editTelefono, setEditTelefono] = useState('');
  const [editRol, setEditRol] = useState('PREVENTISTA');
  const [editMayoristaId, setEditMayoristaId] = useState('');

  // Modal Baja / Eliminación
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [eliminando, setEliminando] = useState(false);
  const [errorDelete, setErrorDelete] = useState(null);
  const [empleadoAEliminar, setEmpleadoAEliminar] = useState(null);

  const cargarMayoristas = useCallback(async () => {
    try {
      const res = await mayoristaService.listar();
      const list = Array.isArray(res) ? res : [];
      setMayoristas(list);
      if (list.length > 0) {
        setMayoristaId((prev) => prev || list[0].id);
      }
    } catch {
      // Ignorar si falla la carga silenciosa de mayoristas o manejar en errorGlobal
    }
  }, []);

  const cargarEmpleados = useCallback(async () => {
    setCargando(true);
    setErrorGlobal(null);
    try {
      const [res] = await Promise.all([
        empleadoService.listar({ size: 100 }),
        cargarMayoristas(),
      ]);
      if (res && Array.isArray(res.content)) {
        setEmpleados(res.content);
      } else {
        setEmpleados([]);
      }
    } catch (err) {
      setErrorGlobal(err.message || 'No se pudieron cargar los empleados del servidor.');
      setEmpleados([]);
    } finally {
      setCargando(false);
    }
  }, [cargarMayoristas]);

  useEffect(() => {
    cargarEmpleados();
  }, [cargarEmpleados]);

  const resetCreateForm = () => {
    setNombre('');
    setApellido('');
    setTipoDocumento('DNI');
    setNumeroDocumento('');
    setLegajo('');
    setNombreUsuario('');
    setEmail('');
    setTelefono('');
    setRol('PREVENTISTA');
    if (mayoristas.length > 0) {
      setMayoristaId(mayoristas[0].id);
    }
    setErrorCreate(null);
  };

  const formatearError = (err, fallback) => {
    if (err.errors && Array.isArray(err.errors) && err.errors.length > 0) {
      return err.errors.map((e) => e.mensaje || `${e.campo}: error`).join(' - ');
    }
    return err.message || fallback;
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setErrorCreate(null);
    setCreando(true);

    const contactos = [];
    if (email.trim()) {
      contactos.push({
        medio: 'CORREO',
        tipoContacto: 'LABORAL',
        email: email.trim(),
      });
    }
    if (telefono.trim()) {
      contactos.push({
        medio: 'TELEFONICO',
        tipoContacto: 'LABORAL',
        telefono: telefono.trim(),
        tipoTelefono: 'CELULAR',
      });
    }

    const payload = {
      nombre: nombre.trim(),
      apellido: apellido.trim(),
      tipoDocumento,
      numeroDocumento: numeroDocumento.trim(),
      legajo: legajo.trim(),
      nombreUsuario: nombreUsuario.trim().toLowerCase(),
      rol,
      mayoristaId: mayoristaId || (mayoristas.length > 0 ? mayoristas[0].id : null),
      contactos,
      direcciones: [],
    };

    try {
      const creado = await empleadoService.crear(payload);
      setExitoMensaje(
        `Empleado "${creado.nombre} ${creado.apellido}" dado de alta con éxito con usuario "${creado.usuario?.nombreUsuario}". Clave temporal: dulcemayor`
      );
      setIsCreateOpen(false);
      resetCreateForm();
      cargarEmpleados();
    } catch (err) {
      setErrorCreate(formatearError(err, 'Error al registrar el empleado.'));
    } finally {
      setCreando(false);
    }
  };

  const openEditModal = (emp) => {
    setEmpleadoAEditar(emp);
    setEditNombre(emp.nombre || '');
    setEditApellido(emp.apellido || '');
    setEditTipoDocumento(emp.tipoDocumento || 'DNI');
    setEditNumeroDocumento(emp.numeroDocumento || '');
    setEditLegajo(emp.legajo || '');
    setEditNombreUsuario(emp.usuario?.nombreUsuario || '');

    const correo = emp.contactos?.find((c) => c.medio === 'CORREO')?.email || '';
    const tel = emp.contactos?.find((c) => c.medio === 'TELEFONICO' || c.medio === 'TELEFONO')?.telefono || '';
    setEditEmail(correo);
    setEditTelefono(tel);
    setEditRol(emp.usuario?.rol || 'PREVENTISTA');
    setEditMayoristaId(emp.mayoristaId || (mayoristas.length > 0 ? mayoristas[0].id : ''));
    setErrorEdit(null);
    setIsEditOpen(true);
  };

  const handleEdit = async (e) => {
    e.preventDefault();
    if (!empleadoAEditar) return;
    setErrorEdit(null);
    setGuardando(true);

    const contactos = [];
    if (editEmail.trim()) {
      contactos.push({
        medio: 'CORREO',
        tipoContacto: 'LABORAL',
        email: editEmail.trim(),
      });
    }
    if (editTelefono.trim()) {
      contactos.push({
        medio: 'TELEFONICO',
        tipoContacto: 'LABORAL',
        telefono: editTelefono.trim(),
        tipoTelefono: 'CELULAR',
      });
    }

    const payload = {
      nombre: editNombre.trim(),
      apellido: editApellido.trim(),
      tipoDocumento: editTipoDocumento,
      numeroDocumento: editNumeroDocumento.trim(),
      legajo: editLegajo.trim(),
      nombreUsuario: editNombreUsuario.trim().toLowerCase(),
      rol: editRol,
      mayoristaId: editMayoristaId || empleadoAEditar.mayoristaId || (mayoristas.length > 0 ? mayoristas[0].id : null),
      contactos,
      direcciones: empleadoAEditar.direcciones || [],
    };

    try {
      const actualizado = await empleadoService.modificar(empleadoAEditar.id, payload);
      setExitoMensaje(
        `Datos de "${actualizado.nombre} ${actualizado.apellido}" actualizados correctamente.`
      );
      setIsEditOpen(false);
      setEmpleadoAEditar(null);
      cargarEmpleados();
    } catch (err) {
      setErrorEdit(formatearError(err, 'Error al actualizar el empleado.'));
    } finally {
      setGuardando(false);
    }
  };

  const openDeleteModal = (emp) => {
    setEmpleadoAEliminar(emp);
    setErrorDelete(null);
    setIsDeleteOpen(true);
  };

  const handleDelete = async () => {
    if (!empleadoAEliminar) return;
    setEliminando(true);
    setErrorDelete(null);
    try {
      await empleadoService.eliminar(empleadoAEliminar.id);
      setExitoMensaje(
        `Empleado "${empleadoAEliminar.nombre} ${empleadoAEliminar.apellido}" eliminado correctamente.`
      );
      setIsDeleteOpen(false);
      setEmpleadoAEliminar(null);
      cargarEmpleados();
    } catch (err) {
      setErrorDelete(err.message || 'No se pudo eliminar el empleado.');
    } finally {
      setEliminando(false);
    }
  };

  // Filtrado local reactivo
  const empleadosFiltrados = empleados.filter((emp) => {
    const rolActual = emp.usuario?.rol?.toUpperCase() || '';
    const coincideRol =
      filtroRol === 'TODOS' ||
      rolActual === filtroRol ||
      (filtroRol === 'PREVENTISTA' && rolActual === 'PREVENTOR') ||
      (filtroRol === 'ADMINISTRATIVO' && rolActual === 'ADMINISTRATIVO');

    const term = busqueda.trim().toLowerCase();
    if (!term) return coincideRol;

    const nombreCompleto = `${emp.nombre || ''} ${emp.apellido || ''}`.toLowerCase();
    const legajoStr = (emp.legajo || '').toLowerCase();
    const docStr = (emp.numeroDocumento || '').toLowerCase();
    const userStr = (emp.usuario?.nombreUsuario || '').toLowerCase();
    const emailStr = (emp.contactos?.find((c) => c.medio === 'CORREO')?.email || '').toLowerCase();
    const telStr = (emp.contactos?.find((c) => c.medio === 'TELEFONICO' || c.medio === 'TELEFONO')?.telefono || '').toLowerCase();

    const coincideBusqueda =
      nombreCompleto.includes(term) ||
      legajoStr.includes(term) ||
      docStr.includes(term) ||
      userStr.includes(term) ||
      emailStr.includes(term) ||
      telStr.includes(term);

    return coincideRol && coincideBusqueda;
  });

  return {
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
    empleadoAEditar,
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
  };
}
