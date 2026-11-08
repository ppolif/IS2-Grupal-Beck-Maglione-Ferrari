import { useState, useEffect, useCallback } from 'react';
import { categoriaService } from '../services/categoriaService';
import { subCategoriaService } from '../services/subCategoriaService';

/**
 * Custom hook managing categories and subcategories state, filters and modal CRUD operations.
 */
export function useCategorias() {
  const [activeTab, setActiveTab] = useState('categorias'); // 'categorias' | 'subcategorias'

  // Data states
  const [categorias, setCategorias] = useState([]);
  const [subcategorias, setSubcategorias] = useState([]);
  const [cargando, setCargando] = useState(true);

  // Filters
  const [busqueda, setBusqueda] = useState('');
  const [filtroCategoriaId, setFiltroCategoriaId] = useState('');

  // Notifications
  const [mensajeExito, setMensajeExito] = useState(null);
  const [mensajeError, setMensajeError] = useState(null);

  // Modal Categoría
  const [modalCatOpen, setModalCatOpen] = useState(false);
  const [categoriaEditando, setCategoriaEditando] = useState(null);
  const [nombreCategoria, setNombreCategoria] = useState('');
  const [guardandoCat, setGuardandoCat] = useState(false);
  const [errorModalCat, setErrorModalCat] = useState(null);

  // Modal Subcategoría
  const [modalSubOpen, setModalSubOpen] = useState(false);
  const [subCategoriaEditando, setSubCategoriaEditando] = useState(null);
  const [nombreSubCategoria, setNombreSubCategoria] = useState('');
  const [categoriaPadreId, setCategoriaPadreId] = useState('');
  const [guardandoSub, setGuardandoSub] = useState(false);
  const [errorModalSub, setErrorModalSub] = useState(null);

  // Modal Confirmación Eliminación
  const [modalEliminarOpen, setModalEliminarOpen] = useState(false);
  const [elementoAEliminar, setElementoAEliminar] = useState(null); // { tipo: 'categoria'|'subcategoria', id, nombre }
  const [eliminando, setEliminando] = useState(false);

  // Carga inicial
  const cargarDatos = useCallback(async () => {
    setCargando(true);
    setMensajeError(null);
    try {
      const [cats, subs] = await Promise.all([
        categoriaService.listar(),
        subCategoriaService.listar(),
      ]);
      setCategorias(Array.isArray(cats) ? cats : []);
      setSubcategorias(Array.isArray(subs) ? subs : []);
    } catch (err) {
      setMensajeError(err.message || 'Error al cargar las categorías y subcategorías');
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    cargarDatos();
  }, [cargarDatos]);

  // Handlers Categoría
  const handleAbrirCrearCategoria = () => {
    setCategoriaEditando(null);
    setNombreCategoria('');
    setErrorModalCat(null);
    setModalCatOpen(true);
  };

  const handleAbrirEditarCategoria = (cat) => {
    setCategoriaEditando(cat);
    setNombreCategoria(cat.nombre);
    setErrorModalCat(null);
    setModalCatOpen(true);
  };

  const handleGuardarCategoria = async (e) => {
    e.preventDefault();
    if (!nombreCategoria.trim()) return;

    setGuardandoCat(true);
    setErrorModalCat(null);

    try {
      if (categoriaEditando) {
        await categoriaService.modificar(categoriaEditando.id, { nombre: nombreCategoria.trim() });
        setMensajeExito(`Categoría "${nombreCategoria.trim()}" actualizada exitosamente.`);
      } else {
        await categoriaService.crear({ nombre: nombreCategoria.trim() });
        setMensajeExito(`Categoría "${nombreCategoria.trim()}" creada exitosamente.`);
      }
      setModalCatOpen(false);
      await cargarDatos();
    } catch (err) {
      if (err.status === 409 || err.code === 'DUPLICADO') {
        setErrorModalCat(`Ya existe una categoría con el nombre "${nombreCategoria.trim()}".`);
      } else {
        setErrorModalCat(err.message || 'Error al guardar la categoría.');
      }
    } finally {
      setGuardandoCat(false);
    }
  };

  // Handlers Subcategoría
  const handleAbrirCrearSubCategoria = () => {
    setSubCategoriaEditando(null);
    setNombreSubCategoria('');
    setCategoriaPadreId(filtroCategoriaId || (categorias[0]?.id || ''));
    setErrorModalSub(null);
    setModalSubOpen(true);
  };

  const handleAbrirEditarSubCategoria = (sub) => {
    setSubCategoriaEditando(sub);
    setNombreSubCategoria(sub.nombre);
    setCategoriaPadreId(sub.categoriaId);
    setErrorModalSub(null);
    setModalSubOpen(true);
  };

  const handleGuardarSubCategoria = async (e) => {
    e.preventDefault();
    if (!nombreSubCategoria.trim() || !categoriaPadreId) return;

    setGuardandoSub(true);
    setErrorModalSub(null);

    try {
      if (subCategoriaEditando) {
        await subCategoriaService.modificar(subCategoriaEditando.id, {
          nombre: nombreSubCategoria.trim(),
          categoriaId: categoriaPadreId,
        });
        setMensajeExito(`Subcategoría "${nombreSubCategoria.trim()}" actualizada exitosamente.`);
      } else {
        await subCategoriaService.crear({
          nombre: nombreSubCategoria.trim(),
          categoriaId: categoriaPadreId,
        });
        setMensajeExito(`Subcategoría "${nombreSubCategoria.trim()}" creada exitosamente.`);
      }
      setModalSubOpen(false);
      await cargarDatos();
    } catch (err) {
      if (err.status === 409 || err.code === 'DUPLICADO') {
        setErrorModalSub(`Ya existe una subcategoría con el nombre "${nombreSubCategoria.trim()}" en la categoría seleccionada.`);
      } else {
        setErrorModalSub(err.message || 'Error al guardar la subcategoría.');
      }
    } finally {
      setGuardandoSub(false);
    }
  };

  // Handlers Eliminación
  const handleConfirmarEliminar = (tipo, id, nombre) => {
    setElementoAEliminar({ tipo, id, nombre });
    setModalEliminarOpen(true);
  };

  const handleEjecutarEliminacion = async () => {
    if (!elementoAEliminar) return;

    setEliminando(true);
    try {
      if (elementoAEliminar.tipo === 'categoria') {
        await categoriaService.eliminar(elementoAEliminar.id);
        setMensajeExito(`Categoría "${elementoAEliminar.nombre}" eliminada exitosamente.`);
      } else {
        await subCategoriaService.eliminar(elementoAEliminar.id);
        setMensajeExito(`Subcategoría "${elementoAEliminar.nombre}" eliminada exitosamente.`);
      }
      setModalEliminarOpen(false);
      setElementoAEliminar(null);
      await cargarDatos();
    } catch (err) {
      setMensajeError(err.message || 'Error al eliminar el elemento.');
      setModalEliminarOpen(false);
    } finally {
      setEliminando(false);
    }
  };

  // Helper mapa de categorías por ID
  const categoriasMap = categorias.reduce((acc, c) => {
    acc[c.id] = c.nombre;
    return acc;
  }, {});

  // Contar subcategorías por categoría
  const subCountPorCat = subcategorias.reduce((acc, s) => {
    acc[s.categoriaId] = (acc[s.categoriaId] || 0) + 1;
    return acc;
  }, {});

  // Filtros reactivos
  const categoriasFiltradas = categorias.filter((c) =>
    busqueda.trim() === '' || c.nombre.toLowerCase().includes(busqueda.toLowerCase())
  );

  const subcategoriasFiltradas = subcategorias.filter((s) => {
    const coincideCat = !filtroCategoriaId || s.categoriaId === filtroCategoriaId;
    const coincideBusqueda =
      busqueda.trim() === '' ||
      s.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
      (categoriasMap[s.categoriaId] || '').toLowerCase().includes(busqueda.toLowerCase());
    return coincideCat && coincideBusqueda;
  });

  return {
    // Data & state
    activeTab,
    setActiveTab,
    categorias,
    subcategorias,
    cargando,
    cargarDatos,

    // Filters & Computed
    busqueda,
    setBusqueda,
    filtroCategoriaId,
    setFiltroCategoriaId,
    categoriasMap,
    subCountPorCat,
    categoriasFiltradas,
    subcategoriasFiltradas,

    // Notifications
    mensajeExito,
    setMensajeExito,
    mensajeError,
    setMensajeError,

    // Modal Category
    modalCatOpen,
    setModalCatOpen,
    categoriaEditando,
    nombreCategoria,
    setNombreCategoria,
    guardandoCat,
    errorModalCat,
    handleAbrirCrearCategoria,
    handleAbrirEditarCategoria,
    handleGuardarCategoria,

    // Modal Subcategory
    modalSubOpen,
    setModalSubOpen,
    subCategoriaEditando,
    nombreSubCategoria,
    setNombreSubCategoria,
    categoriaPadreId,
    setCategoriaPadreId,
    guardandoSub,
    errorModalSub,
    handleAbrirCrearSubCategoria,
    handleAbrirEditarSubCategoria,
    handleGuardarSubCategoria,

    // Modal Delete
    modalEliminarOpen,
    setModalEliminarOpen,
    elementoAEliminar,
    eliminando,
    handleConfirmarEliminar,
    handleEjecutarEliminacion,
  };
}
