import { useState, useEffect, useCallback } from 'react';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { categoriaService } from '../../services/categoriaService';
import { subCategoriaService } from '../../services/subCategoriaService';

export default function Categorias() {
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

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Catálogo y Clasificación
          </span>
          <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
            Categorías y Subcategorías
          </h2>
          <p className="text-[12px] text-[#6b6b80] mt-0.5">
            Organizá la estructura jerárquica de los productos mayoristas de Dulce Mayor.
          </p>
        </div>

        <div className="flex items-center gap-2">
          {activeTab === 'categorias' ? (
            <Button onClick={handleAbrirCrearCategoria}>
              <Icon name="plus" className="w-4 h-4" />
              <span>Nueva categoría</span>
            </Button>
          ) : (
            <Button onClick={handleAbrirCrearSubCategoria} disabled={categorias.length === 0}>
              <Icon name="plus" className="w-4 h-4" />
              <span>Nueva subcategoría</span>
            </Button>
          )}
        </div>
      </div>

      {/* Notifications */}
      {mensajeExito && (
        <div className="p-4 bg-[#dff8e9] border border-[#bfeecf] rounded-[16px] text-[12px] text-[#2e744c] flex items-center justify-between shadow-xs">
          <div className="flex items-center gap-2.5">
            <Icon name="check" className="w-5 h-5 shrink-0 text-[#39855b]" />
            <span className="font-medium">{mensajeExito}</span>
          </div>
          <button
            onClick={() => setMensajeExito(null)}
            className="text-[11px] font-bold hover:underline cursor-pointer"
          >
            Cerrar
          </button>
        </div>
      )}

      {mensajeError && (
        <div className="p-4 bg-[#ffe8ed] border border-[#ffccd5] rounded-[16px] text-[12px] text-[#d63353] flex items-center justify-between shadow-xs">
          <div className="flex items-center gap-2.5">
            <Icon name="alert" className="w-5 h-5 shrink-0 text-[#d63353]" />
            <span className="font-medium">{mensajeError}</span>
          </div>
          <button
            onClick={() => setMensajeError(null)}
            className="text-[11px] font-bold hover:underline cursor-pointer"
          >
            Cerrar
          </button>
        </div>
      )}

      {/* Main Container */}
      <div className="bg-white rounded-[22px] p-6 border border-[#ececf2] shadow-sm">
        {/* Navigation Tabs */}
        <div className="flex items-center justify-between pb-4 border-b border-[#f0f0f4] flex-wrap gap-3">
          <div className="flex items-center gap-2 bg-[#f4f6fb] p-1.5 rounded-[14px]">
            <button
              type="button"
              onClick={() => {
                setActiveTab('categorias');
                setBusqueda('');
              }}
              className={`flex items-center gap-2 px-4 py-2 rounded-[11px] text-[12px] font-semibold transition-all cursor-pointer ${
                activeTab === 'categorias'
                  ? 'bg-white text-[#2b2b3a] shadow-xs'
                  : 'text-[#8c8b9c] hover:text-[#2b2b3a]'
              }`}
            >
              <Icon name="tag" className="w-4 h-4" />
              <span>Categorías</span>
              <span className="bg-[#ffe2f7] text-[#a34187] text-[10px] font-bold px-2 py-0.5 rounded-full">
                {categorias.length}
              </span>
            </button>

            <button
              type="button"
              onClick={() => {
                setActiveTab('subcategorias');
                setBusqueda('');
              }}
              className={`flex items-center gap-2 px-4 py-2 rounded-[11px] text-[12px] font-semibold transition-all cursor-pointer ${
                activeTab === 'subcategorias'
                  ? 'bg-white text-[#2b2b3a] shadow-xs'
                  : 'text-[#8c8b9c] hover:text-[#2b2b3a]'
              }`}
            >
              <Icon name="layers" className="w-4 h-4" />
              <span>Subcategorías</span>
              <span className="bg-[#d9f7ff] text-[#247f99] text-[10px] font-bold px-2 py-0.5 rounded-full">
                {subcategorias.length}
              </span>
            </button>
          </div>

          {/* Search bar & Category filter */}
          <div className="flex items-center gap-3 flex-wrap">
            {activeTab === 'subcategorias' && (
              <select
                value={filtroCategoriaId}
                onChange={(e) => setFiltroCategoriaId(e.target.value)}
                className="bg-[#f5f6fa] border border-[#ececf2] text-[#2b2b3a] text-[11px] font-medium px-3 py-2 rounded-[12px] outline-none focus:border-[#e86bc4]"
              >
                <option value="">Todas las categorías padre</option>
                {categorias.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.nombre}
                  </option>
                ))}
              </select>
            )}

            <div className="flex items-center gap-2 bg-[#f5f6fa] px-3.5 py-2 rounded-[12px] text-[11px] border border-[#ececf2]">
              <Icon name="search" className="w-3.5 h-3.5 text-[#a3a2ae]" />
              <input
                type="text"
                value={busqueda}
                onChange={(e) => setBusqueda(e.target.value)}
                placeholder={`Buscar en ${activeTab}...`}
                className="bg-transparent border-none outline-none text-[#2b2b3a] placeholder-[#a3a2ae] text-[11px] w-48 sm:w-56"
              />
            </div>
          </div>
        </div>

        {/* Content Table / Loading / Empty states */}
        {cargando ? (
          <div className="py-16 text-center text-[12px] text-[#6b6b80]">
            <svg className="animate-spin h-7 w-7 text-[#e86bc4] mx-auto mb-3" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
            </svg>
            Cargando catálogo de {activeTab}...
          </div>
        ) : activeTab === 'categorias' ? (
          /* TAB 1: CATEGORÍAS */
          categoriasFiltradas.length === 0 ? (
            <div className="py-16 text-center space-y-3">
              <div className="w-12 h-12 mx-auto rounded-full bg-[#fff0fb] text-[#e86bc4] flex items-center justify-center">
                <Icon name="tag" className="w-6 h-6" />
              </div>
              <h3 className="text-[14px] font-bold text-[#2b2b3a]">
                {busqueda ? 'No se encontraron categorías' : 'Aún no hay categorías registradas'}
              </h3>
              <p className="text-[12px] text-[#8c8b9c] max-w-sm mx-auto">
                {busqueda
                  ? 'Probá modificando el término de búsqueda.'
                  : 'Creá la primera categoría para empezar a organizar los productos mayoristas.'}
              </p>
              {!busqueda && (
                <div className="pt-2">
                  <Button onClick={handleAbrirCrearCategoria}>Crear primera categoría</Button>
                </div>
              )}
            </div>
          ) : (
            <div className="overflow-x-auto mt-4">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-[#ececf2] text-[10px] uppercase font-bold text-[#aaa9b8] tracking-wider">
                    <th className="pb-3 px-4">Nombre de la Categoría</th>
                    <th className="pb-3 px-4">Subcategorías Asociadas</th>
                    <th className="pb-3 px-4 text-center">Estado</th>
                    <th className="pb-3 px-4 text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#f0f0f4] text-[12px]">
                  {categoriasFiltradas.map((cat) => {
                    const cantSubs = subCountPorCat[cat.id] || 0;
                    return (
                      <tr key={cat.id} className="hover:bg-slate-50/70 transition-colors">
                        <td className="py-3.5 px-4 font-semibold text-[#2b2b3a]">
                          <div className="flex items-center gap-2.5">
                            <div className="w-7 h-7 rounded-[9px] bg-[#fff0fb] text-[#e86bc4] flex items-center justify-center shrink-0">
                              <Icon name="tag" className="w-3.5 h-3.5" />
                            </div>
                            <span>{cat.nombre}</span>
                          </div>
                        </td>
                        <td className="py-3.5 px-4 text-[#6b6b80]">
                          <span className="font-semibold text-[#2b2b3a]">{cantSubs}</span>{' '}
                          {cantSubs === 1 ? 'subcategoría' : 'subcategorías'}
                        </td>
                        <td className="py-3.5 px-4 text-center">
                          <span className="text-[10px] font-semibold px-2.5 py-0.5 rounded-full bg-[#dff8e9] text-[#39855b]">
                            Activa
                          </span>
                        </td>
                        <td className="py-3.5 px-4 text-right">
                          <div className="flex items-center justify-end gap-2">
                            <button
                              type="button"
                              onClick={() => handleAbrirEditarCategoria(cat)}
                              className="p-1.5 rounded-[9px] text-[#6b6b80] hover:text-[#3fb8d9] hover:bg-[#d9f7ff]/30 transition-colors cursor-pointer"
                              title="Modificar categoría"
                            >
                              <Icon name="edit" className="w-4 h-4" />
                            </button>
                            <button
                              type="button"
                              onClick={() => handleConfirmarEliminar('categoria', cat.id, cat.nombre)}
                              className="p-1.5 rounded-[9px] text-[#6b6b80] hover:text-[#d63353] hover:bg-[#ffe8ed] transition-colors cursor-pointer"
                              title="Eliminar categoría"
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
            </div>
          )
        ) : (
          /* TAB 2: SUBCATEGORÍAS */
          subcategoriasFiltradas.length === 0 ? (
            <div className="py-16 text-center space-y-3">
              <div className="w-12 h-12 mx-auto rounded-full bg-[#f0faff] text-[#3fb8d9] flex items-center justify-center">
                <Icon name="layers" className="w-6 h-6" />
              </div>
              <h3 className="text-[14px] font-bold text-[#2b2b3a]">
                {busqueda || filtroCategoriaId
                  ? 'No se encontraron subcategorías para el filtro aplicado'
                  : 'Aún no hay subcategorías registradas'}
              </h3>
              <p className="text-[12px] text-[#8c8b9c] max-w-sm mx-auto">
                {categorias.length === 0
                  ? 'Primero debés dar de alta al menos una categoría padre.'
                  : 'Creá subcategorías para clasificar los productos con mayor precisión.'}
              </p>
              {categorias.length > 0 && !busqueda && (
                <div className="pt-2">
                  <Button onClick={handleAbrirCrearSubCategoria}>Crear primera subcategoría</Button>
                </div>
              )}
            </div>
          ) : (
            <div className="overflow-x-auto mt-4">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-[#ececf2] text-[10px] uppercase font-bold text-[#aaa9b8] tracking-wider">
                    <th className="pb-3 px-4">Subcategoría</th>
                    <th className="pb-3 px-4">Categoría Padre</th>
                    <th className="pb-3 px-4 text-center">Estado</th>
                    <th className="pb-3 px-4 text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#f0f0f4] text-[12px]">
                  {subcategoriasFiltradas.map((sub) => (
                    <tr key={sub.id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="py-3.5 px-4 font-semibold text-[#2b2b3a]">
                        <div className="flex items-center gap-2.5">
                          <div className="w-7 h-7 rounded-[9px] bg-[#f0faff] text-[#3fb8d9] flex items-center justify-center shrink-0">
                            <Icon name="layers" className="w-3.5 h-3.5" />
                          </div>
                          <span>{sub.nombre}</span>
                        </div>
                      </td>
                      <td className="py-3.5 px-4">
                        <span className="text-[11px] font-semibold px-2.5 py-0.5 rounded-full bg-[#fff0fb] text-[#a34187] border border-[#f5d5ee]">
                          {categoriasMap[sub.categoriaId] || 'Categoría no encontrada'}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 text-center">
                        <span className="text-[10px] font-semibold px-2.5 py-0.5 rounded-full bg-[#dff8e9] text-[#39855b]">
                          Activa
                        </span>
                      </td>
                      <td className="py-3.5 px-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            type="button"
                            onClick={() => handleAbrirEditarSubCategoria(sub)}
                            className="p-1.5 rounded-[9px] text-[#6b6b80] hover:text-[#3fb8d9] hover:bg-[#d9f7ff]/30 transition-colors cursor-pointer"
                            title="Modificar subcategoría"
                          >
                            <Icon name="edit" className="w-4 h-4" />
                          </button>
                          <button
                            type="button"
                            onClick={() => handleConfirmarEliminar('subcategoria', sub.id, sub.nombre)}
                            className="p-1.5 rounded-[9px] text-[#6b6b80] hover:text-[#d63353] hover:bg-[#ffe8ed] transition-colors cursor-pointer"
                            title="Eliminar subcategoría"
                          >
                            <Icon name="trash" className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )
        )}
      </div>

      {/* MODAL: ALTA / EDICIÓN CATEGORÍA */}
      <Modal
        isOpen={modalCatOpen}
        onClose={() => setModalCatOpen(false)}
        tag={categoriaEditando ? 'MODIFICAR CATEGORÍA' : 'NUEVA CATEGORÍA'}
        title={categoriaEditando ? 'Editar Categoría del Catálogo' : 'Crear Categoría Principal'}
      >
        <form onSubmit={handleGuardarCategoria} className="space-y-4">
          {errorModalCat && (
            <div className="p-3 bg-[#ffe8ed] border border-[#ffccd5] rounded-[12px] text-[12px] text-[#d63353] flex items-center gap-2">
              <Icon name="alert" className="w-4 h-4 shrink-0" />
              <span>{errorModalCat}</span>
            </div>
          )}

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Nombre de la categoría *
            </label>
            <input
              type="text"
              required
              maxLength={100}
              autoFocus
              value={nombreCategoria}
              onChange={(e) => setNombreCategoria(e.target.value)}
              placeholder="Ej. Alfajores, Chocolates, Galletitas..."
              className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
            />
            <span className="text-[10px] text-[#8c8b9c] mt-1 block">
              Máximo 100 caracteres. El nombre debe ser único en el sistema.
            </span>
          </div>

          <div className="pt-2 flex justify-end gap-2.5">
            <Button secondary type="button" onClick={() => setModalCatOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={guardandoCat}>
              {guardandoCat ? 'Guardando...' : categoriaEditando ? 'Actualizar categoría' : 'Crear categoría'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* MODAL: ALTA / EDICIÓN SUBCATEGORÍA */}
      <Modal
        isOpen={modalSubOpen}
        onClose={() => setModalSubOpen(false)}
        tag={subCategoriaEditando ? 'MODIFICAR SUBCATEGORÍA' : 'NUEVA SUBCATEGORÍA'}
        title={subCategoriaEditando ? 'Editar Subcategoría' : 'Crear Subcategoría'}
      >
        <form onSubmit={handleGuardarSubCategoria} className="space-y-4">
          {errorModalSub && (
            <div className="p-3 bg-[#ffe8ed] border border-[#ffccd5] rounded-[12px] text-[12px] text-[#d63353] flex items-center gap-2">
              <Icon name="alert" className="w-4 h-4 shrink-0" />
              <span>{errorModalSub}</span>
            </div>
          )}

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Categoría padre asociada *
            </label>
            <select
              required
              value={categoriaPadreId}
              onChange={(e) => setCategoriaPadreId(e.target.value)}
              className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
            >
              <option value="" disabled>Seleccione una categoría</option>
              {categorias.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.nombre}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Nombre de la subcategoría *
            </label>
            <input
              type="text"
              required
              maxLength={100}
              autoFocus
              value={nombreSubCategoria}
              onChange={(e) => setNombreSubCategoria(e.target.value)}
              placeholder="Ej. Triples, Con Maní, Rellenos..."
              className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
            />
            <span className="text-[10px] text-[#8c8b9c] mt-1 block">
              Máximo 100 caracteres. No puede repetirse dentro de la misma categoría.
            </span>
          </div>

          <div className="pt-2 flex justify-end gap-2.5">
            <Button secondary type="button" onClick={() => setModalSubOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={guardandoSub}>
              {guardandoSub ? 'Guardando...' : subCategoriaEditando ? 'Actualizar subcategoría' : 'Crear subcategoría'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* MODAL: CONFIRMACIÓN DE ELIMINACIÓN */}
      <Modal
        isOpen={modalEliminarOpen}
        onClose={() => setModalEliminarOpen(false)}
        tag="CONFIRMAR BAJA"
        title={`Eliminar ${elementoAEliminar?.tipo === 'categoria' ? 'Categoría' : 'Subcategoría'}`}
      >
        <div className="space-y-4">
          <p className="text-[12px] text-[#2b2b3a]">
            ¿Estás seguro de que deseás dar de baja{' '}
            {elementoAEliminar?.tipo === 'categoria' ? 'la categoría' : 'la subcategoría'}{' '}
            <strong className="text-[#d63353]">"{elementoAEliminar?.nombre}"</strong>?
          </p>

          {elementoAEliminar?.tipo === 'categoria' && (subCountPorCat[elementoAEliminar.id] || 0) > 0 && (
            <div className="p-3 bg-[#fff4db] border border-[#fce4aa] rounded-[12px] text-[11px] text-[#93630f] flex items-start gap-2">
              <Icon name="alert" className="w-4 h-4 shrink-0 text-[#b57a15] mt-0.5" />
              <span>
                Esta categoría tiene <strong>{subCountPorCat[elementoAEliminar.id]} subcategorías</strong> vinculadas.
              </span>
            </div>
          )}

          <div className="pt-2 flex justify-end gap-2.5">
            <Button secondary type="button" onClick={() => setModalEliminarOpen(false)}>
              Cancelar
            </Button>
            <Button
              variant="danger"
              disabled={eliminando}
              onClick={handleEjecutarEliminacion}
            >
              {eliminando ? 'Eliminando...' : 'Sí, dar de baja'}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
