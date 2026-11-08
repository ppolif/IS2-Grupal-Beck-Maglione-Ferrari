import { Link } from 'react-router-dom';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { useProductos } from '../../hooks/useProductos';
import { formatPrecio } from '../../utils/formatters';
import { imagenService } from '../../services/imagenService';

export default function Productos() {
  const {
    // Data & state
    productos,
    categorias,
    subcategorias,
    cargando,
    totalElementos,

    // Filters
    searchTerm,
    setSearchTerm,
    filtroCategoria,
    setFiltroCategoria,
    filtroSubCategoria,
    setFiltroSubCategoria,
    filtroOferta,
    setFiltroOferta,
    filtroConStock,
    setFiltroConStock,

    // Notifications
    mensajeExito,
    setMensajeExito,
    mensajeError,
    setMensajeError,

    // Create / Edit Modal
    isModalOpen,
    setIsModalOpen,
    editingProduct,
    guardando,
    errorModal,
    formData,
    setFormData,
    subcategoriasDisponibles,
    handleOpenCreate,
    handleOpenEdit,
    handleCategoriaChange,
    handleSave,

    // Image Upload
    imagenPreview,
    subiendoImagen,
    fileInputRef,
    handleFileChange,

    // Price Modal
    modalPrecioOpen,
    setModalPrecioOpen,
    productoPrecio,
    nuevoPrecio,
    setNuevoPrecio,
    actualizandoPrecio,
    handleOpenPrecio,
    handleGuardarPrecio,

    // Stock Modal
    modalStockOpen,
    setModalStockOpen,
    productoStock,
    cantidadReponer,
    setCantidadReponer,
    motivoReposicion,
    setMotivoReposicion,
    reponiendoStock,
    handleOpenStock,
    handleGuardarStock,

    // Delete Modal
    modalEliminarOpen,
    setModalEliminarOpen,
    productoAEliminar,
    eliminando,
    handleConfirmarEliminar,
    handleEjecutarEliminacion,
  } = useProductos();

  return (
    <div className="space-y-6">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-primary">
            Catálogo Comercial
          </span>
          <h2 className="text-xl font-bold text-ink mt-0.5">
            Gestión de Productos Mayoristas
          </h2>
          <p className="text-[12px] text-muted mt-0.5">
            Alta, edición de especificaciones, listas de precios mayoristas y control de stock.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Link to="/categorias">
            <Button secondary>
              <Icon name="tag" className="w-4 h-4" />
              <span>Categorías</span>
            </Button>
          </Link>
          <Button onClick={handleOpenCreate} disabled={subcategorias.length === 0}>
            <Icon name="plus" className="w-4 h-4" />
            <span>Nuevo Producto</span>
          </Button>
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

      {/* Aviso si no hay subcategorías */}
      {subcategorias.length === 0 && !cargando && (
        <div className="p-4 bg-offer-bg border border-offer-border rounded-[16px] text-[12px] text-offer-text flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Icon name="tag" className="w-5 h-5 shrink-0 text-primary" />
            <span>
              Para poder crear productos es obligatorio contar con al menos una <strong>subcategoría</strong> activa.
            </span>
          </div>
          <Link to="/categorias">
            <Button className="py-1.5 px-3 text-[11px]">Crear Subcategoría</Button>
          </Link>
        </div>
      )}

      {/* Search & Filters Bar */}
      <div className="bg-white rounded-[20px] p-4 border border-border-subtle shadow-sm flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-3 flex-wrap">
        <div className="flex items-center gap-2.5 flex-1 min-w-[280px]">
          <div className="relative flex-1">
            <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-muted-subtle">
              <Icon name="search" className="w-4 h-4" />
            </span>
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Buscar por código o nombre de producto..."
              className="w-full pl-10 pr-4 py-2 rounded-[12px] border border-border-subtle bg-surface-note text-[12px] text-ink focus:bg-white focus:outline-none focus:border-primary"
            />
          </div>

          {/* Filtro Categoría */}
          <select
            value={filtroCategoria}
            onChange={(e) => {
              setFiltroCategoria(e.target.value);
              setFiltroSubCategoria('');
            }}
            className="bg-surface-note border border-border-subtle text-ink text-[11px] font-medium px-3 py-2 rounded-[12px] outline-none focus:border-primary"
          >
            <option value="">Todas las categorías</option>
            {categorias.map((c) => (
              <option key={c.id} value={c.id}>
                {c.nombre}
              </option>
            ))}
          </select>

          {/* Filtro Subcategoría */}
          {filtroCategoria && (
            <select
              value={filtroSubCategoria}
              onChange={(e) => setFiltroSubCategoria(e.target.value)}
              className="bg-surface-note border border-border-subtle text-ink text-[11px] font-medium px-3 py-2 rounded-[12px] outline-none focus:border-primary"
            >
              <option value="">Todas las subcategorías</option>
              {subcategorias
                .filter((s) => s.categoriaId === filtroCategoria)
                .map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.nombre}
                  </option>
                ))}
            </select>
          )}
        </div>

        {/* Checkbox filters */}
        <div className="flex items-center gap-4 text-[11px] text-muted font-medium flex-wrap">
          <label className="flex items-center gap-2 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={filtroOferta}
              onChange={(e) => setFiltroOferta(e.target.checked)}
              className="rounded text-primary focus:ring-primary"
            />
            <span>Solo en oferta</span>
          </label>

          <label className="flex items-center gap-2 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={filtroConStock}
              onChange={(e) => setFiltroConStock(e.target.checked)}
              className="rounded text-primary focus:ring-primary"
            />
            <span>Solo con stock</span>
          </label>

          <span className="text-muted-subtle">|</span>

          <span className="text-ink">
            Total: <strong>{totalElementos}</strong> artículos
          </span>
        </div>
      </div>

      {/* Products Table */}
      <div className="bg-white rounded-[22px] p-6 border border-border-subtle shadow-sm">
        {cargando ? (
          <div className="py-16 text-center text-[12px] text-muted">
            <svg className="animate-spin h-7 w-7 text-primary mx-auto mb-3" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
            </svg>
            Cargando catálogo mayorista...
          </div>
        ) : productos.length === 0 ? (
          <div className="py-16 text-center space-y-3">
            <div className="w-12 h-12 mx-auto rounded-full bg-offer-bg text-primary flex items-center justify-center">
              <Icon name="box" className="w-6 h-6" />
            </div>
            <h3 className="text-[14px] font-bold text-ink">
              {searchTerm || filtroCategoria || filtroSubCategoria || filtroOferta || filtroConStock
                ? 'No se encontraron productos con los filtros aplicados'
                : 'Aún no hay productos registrados en el catálogo'}
            </h3>
            <p className="text-[12px] text-muted-light max-w-sm mx-auto">
              {subcategorias.length === 0
                ? 'Primero debés dar de alta al menos una subcategoría en el módulo de Categorías.'
                : 'Registrá el primer producto para comenzar a operar las ventas mayoristas.'}
            </p>
            {subcategorias.length > 0 && !searchTerm && (
              <div className="pt-2">
                <Button onClick={handleOpenCreate}>Dar de alta primer producto</Button>
              </div>
            )}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                  <th className="pb-3 px-3">Producto</th>
                  <th className="pb-3 px-3">Código</th>
                  <th className="pb-3 px-3">Clasificación</th>
                  <th className="pb-3 px-3">Presentación</th>
                  <th className="pb-3 px-3">Precio Mayorista</th>
                  <th className="pb-3 px-3">Stock Actual</th>
                  <th className="pb-3 px-3 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-divider text-[12px]">
                {productos.map((p) => {
                  const stockNum = p.stockActual ?? 0;
                  const stockBadge =
                    stockNum === 0
                      ? 'bg-danger-bg text-danger-hover'
                      : stockNum <= 20
                      ? 'bg-warning-bg text-warning-text'
                      : 'bg-success-bg text-success-icon';

                  const stockLabel =
                    stockNum === 0 ? 'Sin stock' : stockNum <= 20 ? 'Bajo stock' : 'Normal';

                  const imgUrl = p.imagenId ? imagenService.obtenerUrlContenido(p.imagenId) : null;

                  return (
                    <tr key={p.id} className="hover:bg-slate-50/70 transition-colors">
                      {/* Thumbnail & Name */}
                      <td className="py-3 px-3">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-[12px] bg-surface-note border border-border-subtle overflow-hidden flex items-center justify-center shrink-0">
                            {imgUrl ? (
                              <img
                                src={imgUrl}
                                alt={p.nombre}
                                className="w-full h-full object-cover"
                                onError={(e) => {
                                  e.target.onerror = null;
                                  e.target.style.display = 'none';
                                }}
                              />
                            ) : (
                              <Icon name="box" className="w-5 h-5 text-muted-subtle" />
                            )}
                          </div>
                          <div className="leading-tight">
                            <strong className="block text-ink font-semibold">
                              {p.nombre}
                            </strong>
                            {p.enOferta && (
                              <span className="inline-block mt-0.5 text-[9px] font-bold uppercase tracking-wider text-primary bg-primary-light px-2 py-0.2 rounded-full">
                                En oferta
                              </span>
                            )}
                          </div>
                        </div>
                      </td>

                      {/* Code */}
                      <td className="py-3 px-3 font-mono text-[11px] text-muted-light">
                        {p.codigo}
                      </td>

                      {/* Category & Subcategory */}
                      <td className="py-3 px-3">
                        <div className="space-y-0.5">
                          <span className="inline-block text-[10px] font-semibold px-2 py-0.5 rounded-full bg-canvas text-ink border border-border-subtle">
                            {p.categoriaNombre || 'Sin cat.'}
                          </span>
                          <span className="block text-[11px] text-primary-dark font-medium">
                            {p.subCategoriaNombre || 'General'}
                          </span>
                        </div>
                      </td>

                      {/* Presentation */}
                      <td className="py-3 px-3 text-muted">
                        <span>{p.presentacion}</span>
                        {p.unidadesPorBulto && (
                          <span className="block text-[10px] text-muted-subtle">
                            ({p.unidadesPorBulto} u./bulto)
                          </span>
                        )}
                      </td>

                      {/* Price */}
                      <td className="py-3 px-3 font-semibold text-ink">
                        <span>{formatPrecio(p.precioVigente)}</span>
                      </td>

                      {/* Stock */}
                      <td className="py-3 px-3">
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-ink">{stockNum}</span>
                          <span className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${stockBadge}`}>
                            {stockLabel}
                          </span>
                        </div>
                      </td>

                      {/* Actions */}
                      <td className="py-3 px-3 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          {/* Modificar Precio */}
                          <button
                            type="button"
                            onClick={() => handleOpenPrecio(p)}
                            className="p-1.5 rounded-[9px] text-muted hover:text-success-icon hover:bg-success-bg/60 transition-colors cursor-pointer"
                            title="Actualizar precio mayorista"
                          >
                            <Icon name="dollar" className="w-4 h-4" />
                          </button>

                          {/* Reponer Stock */}
                          <button
                            type="button"
                            onClick={() => handleOpenStock(p)}
                            className="p-1.5 rounded-[9px] text-muted hover:text-secondary hover:bg-info-bg/40 transition-colors cursor-pointer"
                            title="Reponer stock"
                          >
                            <Icon name="truck" className="w-4 h-4" />
                          </button>

                          {/* Modificar Ficha */}
                          <button
                            type="button"
                            onClick={() => handleOpenEdit(p)}
                            className="p-1.5 rounded-[9px] text-muted hover:text-primary hover:bg-primary-light/50 transition-colors cursor-pointer"
                            title="Editar especificaciones"
                          >
                            <Icon name="edit" className="w-4 h-4" />
                          </button>

                          {/* Eliminar */}
                          <button
                            type="button"
                            onClick={() => handleConfirmarEliminar(p)}
                            className="p-1.5 rounded-[9px] text-muted hover:text-danger-hover hover:bg-danger-bg transition-colors cursor-pointer"
                            title="Dar de baja producto"
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
        )}
      </div>

      {/* MODAL: ALTA / EDICIÓN DE PRODUCTO */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        tag={editingProduct ? 'MODIFICAR PRODUCTO' : 'NUEVO PRODUCTO'}
        title={editingProduct ? 'Editar Producto en Catálogo' : 'Dar de Alta Nuevo Producto'}
      >
        <form onSubmit={handleSave} className="space-y-4">
          {errorModal && (
            <div className="p-3 bg-danger-bg border border-danger-border rounded-[12px] text-[12px] text-danger-hover flex items-center gap-2">
              <Icon name="alert" className="w-4 h-4 shrink-0" />
              <span>{errorModal}</span>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
            {/* Código comercial */}
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Código de producto *
              </label>
              <input
                type="text"
                required
                maxLength={50}
                value={formData.codigo}
                onChange={(e) => setFormData({ ...formData, codigo: e.target.value.toUpperCase() })}
                placeholder="Ej. ALF-TRIP-01"
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] font-mono uppercase text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>

            {/* Nombre */}
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Nombre comercial *
              </label>
              <input
                type="text"
                required
                maxLength={150}
                value={formData.nombre}
                onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                placeholder="Ej. Alfajor Triple Chocolate"
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>

            {/* Selector de Categoría Padre */}
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Categoría principal *
              </label>
              <select
                required
                value={formData.categoriaPadreId}
                onChange={(e) => handleCategoriaChange(e.target.value)}
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              >
                <option value="" disabled>Seleccione categoría</option>
                {categorias.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.nombre}
                  </option>
                ))}
              </select>
            </div>

            {/* Selector dependiente de Subcategoría (OBLIGATORIA) */}
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Subcategoría (Obligatoria) *
              </label>
              <select
                required
                value={formData.subCategoriaId}
                onChange={(e) => setFormData({ ...formData, subCategoriaId: e.target.value })}
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              >
                <option value="" disabled>
                  {subcategoriasDisponibles.length === 0
                    ? 'Sin subcategorías en esta categoría'
                    : 'Seleccione subcategoría'}
                </option>
                {subcategoriasDisponibles.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.nombre}
                  </option>
                ))}
              </select>
            </div>

            {/* Presentación */}
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Presentación comercial *
              </label>
              <input
                type="text"
                required
                value={formData.presentacion}
                onChange={(e) => setFormData({ ...formData, presentacion: e.target.value })}
                placeholder="Ej. Caja x 24 u. / Display x 50 u."
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>

            {/* Unidades por bulto */}
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Unidades por bulto *
              </label>
              <input
                type="number"
                min="1"
                required
                value={formData.unidadesPorBulto}
                onChange={(e) => setFormData({ ...formData, unidadesPorBulto: e.target.value })}
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>

            {/* Precio inicial (Solo alta) */}
            {!editingProduct && (
              <div>
                <label className="block text-[11px] font-semibold text-ink mb-1">
                  Precio mayorista inicial ($) *
                </label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  required
                  value={formData.precioInicial}
                  onChange={(e) => setFormData({ ...formData, precioInicial: e.target.value })}
                  placeholder="Ej. 18900"
                  className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
                />
              </div>
            )}

            {/* Stock inicial (Solo alta) */}
            {!editingProduct && (
              <div>
                <label className="block text-[11px] font-semibold text-ink mb-1">
                  Stock inicial (Bultos) *
                </label>
                <input
                  type="number"
                  min="0"
                  required
                  value={formData.stockInicial}
                  onChange={(e) => setFormData({ ...formData, stockInicial: e.target.value })}
                  placeholder="Ej. 100"
                  className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
                />
              </div>
            )}
          </div>

          {/* En Oferta Checkbox */}
          <div className="pt-1">
            <label className="flex items-center gap-2 cursor-pointer select-none text-[12px] text-ink font-medium">
              <input
                type="checkbox"
                checked={formData.enOferta}
                onChange={(e) => setFormData({ ...formData, enOferta: e.target.checked })}
                className="rounded text-primary focus:ring-primary"
              />
              <span>Destacar producto en sección "En Oferta"</span>
            </label>
          </div>

          {/* Descripción */}
          <div>
            <label className="block text-[11px] font-semibold text-ink mb-1">
              Descripción o notas de cata / sabor
            </label>
            <textarea
              rows="2"
              maxLength={500}
              value={formData.descripcion}
              onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
              placeholder="Detalle de sabor, ingredientes o especificaciones para el cliente..."
              className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
            />
          </div>

          {/* Subida de Imagen con Preview */}
          <div>
            <label className="block text-[11px] font-semibold text-ink mb-1">
              Imagen del producto
            </label>
            <input
              type="file"
              ref={fileInputRef}
              accept="image/png,image/jpeg,image/webp"
              onChange={handleFileChange}
              className="hidden"
            />

            <div
              onClick={() => fileInputRef.current?.click()}
              className="border-2 border-dashed border-border-subtle rounded-[16px] p-4 flex items-center justify-center gap-4 text-muted text-[11px] hover:border-secondary transition-colors cursor-pointer bg-surface-note"
            >
              {imagenPreview ? (
                <div className="flex items-center gap-3">
                  <img
                    src={imagenPreview}
                    alt="Preview"
                    className="w-14 h-14 object-cover rounded-[10px] border border-border-subtle"
                  />
                  <div className="text-left">
                    <span className="text-secondary font-bold block text-[12px]">Imagen seleccionada</span>
                    <span className="text-[10px] text-muted-subtle">Hacé clic para cambiar la foto</span>
                  </div>
                </div>
              ) : subiendoImagen ? (
                <div className="flex items-center gap-2 text-secondary">
                  <svg className="animate-spin h-5 w-5 text-secondary" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                  </svg>
                  <span>Subiendo imagen al servidor...</span>
                </div>
              ) : (
                <div className="flex items-center gap-2">
                  <Icon name="camera" className="w-5 h-5 text-secondary" />
                  <span className="text-secondary font-bold">Cargar imagen (PNG, JPG o WebP hasta 5 MB)</span>
                </div>
              )}
            </div>
          </div>

          <div className="pt-3 flex justify-end gap-2.5">
            <Button secondary type="button" onClick={() => setIsModalOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={guardando || subiendoImagen}>
              {guardando ? 'Guardando...' : editingProduct ? 'Actualizar producto' : 'Guardar producto'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* MODAL: ACTUALIZAR PRECIO RÁPIDO */}
      <Modal
        isOpen={modalPrecioOpen}
        onClose={() => setModalPrecioOpen(false)}
        tag="LISTA DE PRECIOS"
        title="Fijar Nuevo Precio Mayorista"
      >
        <form onSubmit={handleGuardarPrecio} className="space-y-4">
          <p className="text-[12px] text-muted">
            Establecé el nuevo precio de vigencia para{' '}
            <strong className="text-ink">{productoPrecio?.nombre}</strong>.
          </p>

          <div className="p-3 bg-surface-note rounded-[12px] border border-border-subtle text-[11px] flex justify-between">
            <span className="text-muted">Precio actual vigente:</span>
            <strong className="text-ink">{formatPrecio(productoPrecio?.precioVigente)}</strong>
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-ink mb-1">
              Nuevo precio mayorista ($) *
            </label>
            <input
              type="number"
              step="0.01"
              min="0.01"
              required
              autoFocus
              value={nuevoPrecio}
              onChange={(e) => setNuevoPrecio(e.target.value)}
              placeholder="Ej. 21500"
              className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
            />
          </div>

          <div className="pt-2 flex justify-end gap-2.5">
            <Button secondary type="button" onClick={() => setModalPrecioOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={actualizandoPrecio}>
              {actualizandoPrecio ? 'Actualizando...' : 'Fijar nuevo precio'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* MODAL: REPONER STOCK RÁPIDO */}
      <Modal
        isOpen={modalStockOpen}
        onClose={() => setModalStockOpen(false)}
        tag="MOVIMIENTO DE INVENTARIO"
        title="Registrar Ingreso de Stock"
      >
        <form onSubmit={handleGuardarStock} className="space-y-4">
          <p className="text-[12px] text-muted">
            Registrá la entrada de mercadería para{' '}
            <strong className="text-ink">{productoStock?.nombre}</strong>.
          </p>

          <div className="p-3 bg-surface-note rounded-[12px] border border-border-subtle text-[11px] flex justify-between">
            <span className="text-muted">Stock disponible actual:</span>
            <strong className="text-ink">{productoStock?.stockActual || 0} bultos</strong>
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-ink mb-1">
              Cantidad de bultos a ingresar *
            </label>
            <input
              type="number"
              min="1"
              required
              autoFocus
              value={cantidadReponer}
              onChange={(e) => setCantidadReponer(e.target.value)}
              placeholder="Ej. 50"
              className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-ink mb-1">
              Motivo u observación
            </label>
            <input
              type="text"
              value={motivoReposicion}
              onChange={(e) => setMotivoReposicion(e.target.value)}
              placeholder="Ej. Ingreso de fábrica lote 402"
              className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
            />
          </div>

          <div className="pt-2 flex justify-end gap-2.5">
            <Button secondary type="button" onClick={() => setModalStockOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={reponiendoStock}>
              {reponiendoStock ? 'Registrando...' : 'Confirmar reposición'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* MODAL: CONFIRMAR ELIMINACIÓN */}
      <Modal
        isOpen={modalEliminarOpen}
        onClose={() => setModalEliminarOpen(false)}
        tag="CONFIRMAR BAJA"
        title="Dar de Baja Producto"
      >
        <div className="space-y-4">
          <p className="text-[12px] text-ink">
            ¿Estás seguro de que deseás dar de baja el producto{' '}
            <strong className="text-danger-hover">"{productoAEliminar?.nombre}"</strong> ({productoAEliminar?.codigo})?
          </p>

          <p className="text-[11px] text-muted">
            El producto dejará de ser visible en la tienda minorista y en los pedidos de preventores. Sus históricos de ventas se preservarán íntegros.
          </p>

          <div className="pt-2 flex justify-end gap-2.5">
            <Button secondary type="button" onClick={() => setModalEliminarOpen(false)}>
              Cancelar
            </Button>
            <Button variant="danger" disabled={eliminando} onClick={handleEjecutarEliminacion}>
              {eliminando ? 'Eliminando...' : 'Sí, dar de baja'}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
