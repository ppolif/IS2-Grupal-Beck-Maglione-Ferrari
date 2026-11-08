import { useState, useEffect, useCallback } from 'react';
import { empleadoService } from '../services/empleadoService';

/**
 * Custom hook managing employee state, filters, API operations and modal workflows.
 */
export function useUsuarios() {
  const [empleados, setEmpleados] = useState([]);
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

  // Modal Baja / Eliminación
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [eliminando, setEliminando] = useState(false);
  const [errorDelete, setErrorDelete] = useState(null);
  const [empleadoAEliminar, setEmpleadoAEliminar] = useState(null);

  const cargarEmpleados = useCallback(async () => {
    setCargando(true);
    setErrorGlobal(null);
    try {
      const res = await empleadoService.listar({ size: 100 });
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
  }, []);

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
    setErrorCreate(null);
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
        medio: 'TELEFONO',
        tipoContacto: 'LABORAL',
        telefono: telefono.trim(),
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
      setErrorCreate(err.message || 'Error al registrar el empleado.');
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
    const tel = emp.contactos?.find((c) => c.medio === 'TELEFONO')?.telefono || '';
    setEditEmail(correo);
    setEditTelefono(tel);
    setEditRol(emp.usuario?.rol || 'PREVENTISTA');
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
        medio: 'TELEFONO',
        tipoContacto: 'LABORAL',
        telefono: editTelefono.trim(),
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
      setErrorEdit(err.message || 'Error al actualizar el empleado.');
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

    const coincideBusqueda =
      nombreCompleto.includes(term) ||
      legajoStr.includes(term) ||
      docStr.includes(term) ||
      userStr.includes(term) ||
      emailStr.includes(term);

    return coincideRol && coincideBusqueda;
  });

  return {
    // Data & state
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
