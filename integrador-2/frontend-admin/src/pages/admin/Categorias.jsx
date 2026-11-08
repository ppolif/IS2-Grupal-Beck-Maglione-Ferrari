import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { useCategorias } from '../../hooks/useCategorias';

export default function Categorias() {
  const {
    // Data & state
    activeTab,
    setActiveTab,
    categorias,
    subcategorias,
    cargando,

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
  } = useCategorias();

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-primary">
            Catálogo y Clasificación
          </span>
          <h2 className="text-xl font-bold text-ink mt-0.5">
            Categorías y Subcategorías
          </h2>
          <p className="text-[12px] text-muted mt-0.5">
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
        <div className="p-4 bg-success-bg border border-success-border rounded-[16px] text-[12px] text-success-text flex items-center justify-between shadow-xs">
          <div className="flex items-center gap-2.5">
            <Icon name="check" className="w-5 h-5 shrink-0 text-success-icon" />
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
        <div className="p-4 bg-danger-bg border border-danger-border rounded-[16px] text-[12px] text-danger-hover flex items-center justify-between shadow-xs">
          <div className="flex items-center gap-2.5">
            <Icon name="alert" className="w-5 h-5 shrink-0 text-danger-hover" />
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
      <div className="bg-white rounded-[22px] p-6 border border-border-subtle shadow-sm">
        {/* Navigation Tabs */}
        <div className="flex items-center justify-between pb-4 border-b border-divider flex-wrap gap-3">
          <div className="flex items-center gap-2 bg-canvas p-1.5 rounded-[14px]">
            <button
              type="button"
              onClick={() => {
                setActiveTab('categorias');
                setBusqueda('');
              }}
              className={`flex items-center gap-2 px-4 py-2 rounded-[11px] text-[12px] font-semibold transition-all cursor-pointer ${
                activeTab === 'categorias'
                  ? 'bg-white text-ink shadow-xs'
                  : 'text-muted-light hover:text-ink'
              }`}
            >
              <Icon name="tag" className="w-4 h-4" />
              <span>Categorías</span>
              <span className="bg-primary-light text-primary-dark text-[10px] font-bold px-2 py-0.5 rounded-full">
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
                  ? 'bg-white text-ink shadow-xs'
                  : 'text-muted-light hover:text-ink'
              }`}
            >
              <Icon name="layers" className="w-4 h-4" />
              <span>Subcategorías</span>
              <span className="bg-info-bg text-info-text text-[10px] font-bold px-2 py-0.5 rounded-full">
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
                className="bg-input-bg border border-border-subtle text-ink text-[11px] font-medium px-3 py-2 rounded-[12px] outline-none focus:border-primary"
              >
                <option value="">Todas las categorías padre</option>
                {categorias.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.nombre}
                  </option>
                ))}
              </select>
            )}

            <div className="flex items-center gap-2 bg-input-bg px-3.5 py-2 rounded-[12px] text-[11px] border border-border-subtle">
              <Icon name="search" className="w-3.5 h-3.5 text-muted-icon" />
              <input
                type="text"
                value={busqueda}
                onChange={(e) => setBusqueda(e.target.value)}
                placeholder={`Buscar en ${activeTab}...`}
                className="bg-transparent border-none outline-none text-ink placeholder-muted-icon text-[11px] w-48 sm:w-56"
              />
            </div>
          </div>
        </div>

        {/* Content Table / Loading / Empty states */}
        {cargando ? (
          <div className="py-16 text-center text-[12px] text-muted">
            <svg className="animate-spin h-7 w-7 text-primary mx-auto mb-3" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
            </svg>
            Cargando catálogo de {activeTab}...
          </div>
        ) : activeTab === 'categorias' ? (
          /* TAB 1: CATEGORÍAS */
          categoriasFiltradas.length === 0 ? (
            <div className="py-16 text-center space-y-3">
              <div className="w-12 h-12 mx-auto rounded-full bg-offer-bg text-primary flex items-center justify-center">
                <Icon name="tag" className="w-6 h-6" />
              </div>
              <h3 className="text-[14px] font-bold text-ink">
                {busqueda ? 'No se encontraron categorías' : 'Aún no hay categorías registradas'}
              </h3>
              <p className="text-[12px] text-muted-light max-w-sm mx-auto">
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
                  <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                    <th className="pb-3 px-4">Nombre de la Categoría</th>
                    <th className="pb-3 px-4">Subcategorías Asociadas</th>
                    <th className="pb-3 px-4 text-center">Estado</th>
                    <th className="pb-3 px-4 text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-divider text-[12px]">
                  {categoriasFiltradas.map((cat) => {
                    const cantSubs = subCountPorCat[cat.id] || 0;
                    return (
                      <tr key={cat.id} className="hover:bg-slate-50/70 transition-colors">
                        <td className="py-3.5 px-4 font-semibold text-ink">
                          <div className="flex items-center gap-2.5">
                            <div className="w-7 h-7 rounded-[9px] bg-offer-bg text-primary flex items-center justify-center shrink-0">
                              <Icon name="tag" className="w-3.5 h-3.5" />
                            </div>
                            <span>{cat.nombre}</span>
                          </div>
                        </td>
                        <td className="py-3.5 px-4 text-muted">
                          <span className="font-semibold text-ink">{cantSubs}</span>{' '}
                          {cantSubs === 1 ? 'subcategoría' : 'subcategorías'}
                        </td>
                        <td className="py-3.5 px-4 text-center">
                          <span className="text-[10px] font-semibold px-2.5 py-0.5 rounded-full bg-success-bg text-success-icon">
                            Activa
                          </span>
                        </td>
                        <td className="py-3.5 px-4 text-right">
                          <div className="flex items-center justify-end gap-2">
                            <button
                              type="button"
                              onClick={() => handleAbrirEditarCategoria(cat)}
                              className="p-1.5 rounded-[9px] text-muted hover:text-secondary hover:bg-info-bg/30 transition-colors cursor-pointer"
                              title="Modificar categoría"
                            >
                              <Icon name="edit" className="w-4 h-4" />
                            </button>
                            <button
                              type="button"
                              onClick={() => handleConfirmarEliminar('categoria', cat.id, cat.nombre)}
                              className="p-1.5 rounded-[9px] text-muted hover:text-danger-hover hover:bg-danger-bg transition-colors cursor-pointer"
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
              <div className="w-12 h-12 mx-auto rounded-full bg-info-subtle-bg text-secondary flex items-center justify-center">
                <Icon name="layers" className="w-6 h-6" />
              </div>
              <h3 className="text-[14px] font-bold text-ink">
                {busqueda || filtroCategoriaId
                  ? 'No se encontraron subcategorías para el filtro aplicado'
                  : 'Aún no hay subcategorías registradas'}
              </h3>
              <p className="text-[12px] text-muted-light max-w-sm mx-auto">
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
                  <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                    <th className="pb-3 px-4">Subcategoría</th>
                    <th className="pb-3 px-4">Categoría Padre</th>
                    <th className="pb-3 px-4 text-center">Estado</th>
                    <th className="pb-3 px-4 text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-divider text-[12px]">
                  {subcategoriasFiltradas.map((sub) => (
                    <tr key={sub.id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="py-3.5 px-4 font-semibold text-ink">
                        <div className="flex items-center gap-2.5">
                          <div className="w-7 h-7 rounded-[9px] bg-info-subtle-bg text-secondary flex items-center justify-center shrink-0">
                            <Icon name="layers" className="w-3.5 h-3.5" />
                          </div>
                          <span>{sub.nombre}</span>
                        </div>
                      </td>
                      <td className="py-3.5 px-4">
                        <span className="text-[11px] font-semibold px-2.5 py-0.5 rounded-full bg-offer-bg text-primary-dark border border-offer-border">
                          {categoriasMap[sub.categoriaId] || 'Categoría no encontrada'}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 text-center">
                        <span className="text-[10px] font-semibold px-2.5 py-0.5 rounded-full bg-success-bg text-success-icon">
                          Activa
                        </span>
                      </td>
                      <td className="py-3.5 px-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            type="button"
                            onClick={() => handleAbrirEditarSubCategoria(sub)}
                            className="p-1.5 rounded-[9px] text-muted hover:text-secondary hover:bg-info-bg/30 transition-colors cursor-pointer"
                            title="Modificar subcategoría"
                          >
                            <Icon name="edit" className="w-4 h-4" />
                          </button>
                          <button
                            type="button"
                            onClick={() => handleConfirmarEliminar('subcategoria', sub.id, sub.nombre)}
                            className="p-1.5 rounded-[9px] text-muted hover:text-danger-hover hover:bg-danger-bg transition-colors cursor-pointer"
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
            <div className="p-3 bg-danger-bg border border-danger-border rounded-[12px] text-[12px] text-danger-hover flex items-center gap-2">
              <Icon name="alert" className="w-4 h-4 shrink-0" />
              <span>{errorModalCat}</span>
            </div>
          )}

          <div>
            <label className="block text-[11px] font-semibold text-ink mb-1">
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
              className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
            />
            <span className="text-[10px] text-muted-light mt-1 block">
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
            <div className="p-3 bg-danger-bg border border-danger-border rounded-[12px] text-[12px] text-danger-hover flex items-center gap-2">
              <Icon name="alert" className="w-4 h-4 shrink-0" />
              <span>{errorModalSub}</span>
            </div>
          )}

          <div>
            <label className="block text-[11px] font-semibold text-ink mb-1">
              Categoría padre asociada *
            </label>
            <select
              required
              value={categoriaPadreId}
              onChange={(e) => setCategoriaPadreId(e.target.value)}
              className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
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
            <label className="block text-[11px] font-semibold text-ink mb-1">
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
              className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
            />
            <span className="text-[10px] text-muted-light mt-1 block">
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
          <p className="text-[12px] text-ink">
            ¿Estás seguro de que deseás dar de baja{' '}
            {elementoAEliminar?.tipo === 'categoria' ? 'la categoría' : 'la subcategoría'}{' '}
            <strong className="text-danger-hover">"{elementoAEliminar?.nombre}"</strong>?
          </p>

          {elementoAEliminar?.tipo === 'categoria' && (subCountPorCat[elementoAEliminar.id] || 0) > 0 && (
            <div className="p-3 bg-warning-subtle-bg border border-warning-subtle-border rounded-[12px] text-[11px] text-warning-subtle-text flex items-start gap-2">
              <Icon name="alert" className="w-4 h-4 shrink-0 text-warning-subtle-icon mt-0.5" />
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
