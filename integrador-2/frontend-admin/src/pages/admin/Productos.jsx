import { useState, useEffect, useCallback, useRef } from 'react';
import { Link } from 'react-router-dom';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { productoService } from '../../services/productoService';
import { categoriaService } from '../../services/categoriaService';
import { subCategoriaService } from '../../services/subCategoriaService';
import { imagenService } from '../../services/imagenService';
import { vigenciaPrecioService } from '../../services/vigenciaPrecioService';
import { stockService } from '../../services/stockService';

export default function Productos() {
  // Datos
  const [productos, setProductos] = useState([]);
  const [categorias, setCategorias] = useState([]);
  const [subcategorias, setSubcategorias] = useState([]);
  const [cargando, setCargando] = useState(true);

  // Paginación y Totales
  const [totalElementos, setTotalElementos] = useState(0);

  // Filtros
  const [searchTerm, setSearchTerm] = useState('');
  const [filtroCategoria, setFiltroCategoria] = useState('');
  const [filtroSubCategoria, setFiltroSubCategoria] = useState('');
  const [filtroOferta, setFiltroOferta] = useState(false);
  const [filtroConStock, setFiltroConStock] = useState(false);

  // Notificaciones
  const [mensajeExito, setMensajeExito] = useState(null);
  const [mensajeError, setMensajeError] = useState(null);

  // Modal Alta / Edición de Producto
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [guardando, setGuardando] = useState(false);
  const [errorModal, setErrorModal] = useState(null);

  // Formulario Producto
  const [formData, setFormData] = useState({
    codigo: '',
    nombre: '',
    descripcion: '',
    presentacion: '',
    unidadesPorBulto: 24,
    categoriaPadreId: '',
    subCategoriaId: '',
    enOferta: false,
    precioInicial: '',
    stockInicial: '',
    imagenId: null,
  });

  // Manejo de Imagen en Modal
  const [imagenPreview, setImagenPreview] = useState(null);
  const [subiendoImagen, setSubiendoImagen] = useState(false);
  const fileInputRef = useRef(null);

  // Modal Actualización Rápida de Precio
  const [modalPrecioOpen, setModalPrecioOpen] = useState(false);
  const [productoPrecio, setProductoPrecio] = useState(null);
  const [nuevoPrecio, setNuevoPrecio] = useState('');
  const [actualizandoPrecio, setActualizandoPrecio] = useState(false);

  // Modal Reposición Rápida de Stock
  const [modalStockOpen, setModalStockOpen] = useState(false);
  const [productoStock, setProductoStock] = useState(null);
  const [cantidadReponer, setCantidadReponer] = useState('');
  const [motivoReposicion, setMotivoReposicion] = useState('Ingreso de fábrica');
  const [reponiendoStock, setReponiendoStock] = useState(false);

  // Modal Eliminación
  const [modalEliminarOpen, setModalEliminarOpen] = useState(false);
  const [productoAEliminar, setProductoAEliminar] = useState(null);
  const [eliminando, setEliminando] = useState(false);

  // Cargar Categorías y Subcategorías para los selectores
  const cargarCategorias = useCallback(async () => {
    try {
      const [cats, subs] = await Promise.all([
        categoriaService.listar(),
        subCategoriaService.listar(),
      ]);
      setCategorias(Array.isArray(cats) ? cats : []);
      setSubcategorias(Array.isArray(subs) ? subs : []);
    } catch {
      // Ignora error si aún no hay conexión
    }
  }, []);

  // Cargar Productos desde la API
  const cargarProductos = useCallback(async () => {
    setCargando(true);
    setMensajeError(null);
    try {
      const filtros = {
        q: searchTerm.trim() || undefined,
        categoriaId: filtroCategoria || undefined,
        subCategoriaId: filtroSubCategoria || undefined,
        enOferta: filtroOferta ? true : undefined,
        soloConStock: filtroConStock ? true : undefined,
        size: 50,
      };

      const res = await productoService.listar(filtros);
      if (res && Array.isArray(res.content)) {
        setProductos(res.content);
        setTotalElementos(res.totalElementos || res.content.length);
      } else if (Array.isArray(res)) {
        setProductos(res);
        setTotalElementos(res.length);
      } else {
        setProductos([]);
        setTotalElementos(0);
      }
    } catch (err) {
      setMensajeError(err.message || 'Error al conectar con el catálogo de productos');
    } finally {
      setCargando(false);
    }
  }, [searchTerm, filtroCategoria, filtroSubCategoria, filtroOferta, filtroConStock]);

  useEffect(() => {
    cargarCategorias();
  }, [cargarCategorias]);

  useEffect(() => {
    const timer = setTimeout(() => {
      cargarProductos();
    }, 250);
    return () => clearTimeout(timer);
  }, [cargarProductos]);

  // Subcategorías disponibles según la categoría seleccionada en el formulario
  const subcategoriasDisponibles = subcategorias.filter(
    (s) => s.categoriaId === formData.categoriaPadreId
  );

  // Apertura de Modal Crear
  const handleOpenCreate = () => {
    setEditingProduct(null);
    const primeraCat = categorias[0]?.id || '';
    const primeraSub = subcategorias.find((s) => s.categoriaId === primeraCat)?.id || '';

    setFormData({
      codigo: '',
      nombre: '',
      descripcion: '',
      presentacion: 'Caja x 24 u.',
      unidadesPorBulto: 24,
      categoriaPadreId: primeraCat,
      subCategoriaId: primeraSub,
      enOferta: false,
      precioInicial: '15000',
      stockInicial: '50',
      imagenId: null,
    });
    setImagenPreview(null);
    setErrorModal(null);
    setIsModalOpen(true);
  };

  // Apertura de Modal Editar
  const handleOpenEdit = (product) => {
    setEditingProduct(product);
    setFormData({
      codigo: product.codigo,
      nombre: product.nombre,
      descripcion: product.descripcion || '',
      presentacion: product.presentacion,
      unidadesPorBulto: product.unidadesPorBulto || 1,
      categoriaPadreId: product.categoriaId || '',
      subCategoriaId: product.subCategoriaId || '',
      enOferta: Boolean(product.enOferta),
      precioInicial: product.precioVigente ? product.precioVigente.toString() : '',
      stockInicial: product.stockActual !== undefined ? product.stockActual.toString() : '0',
      imagenId: product.imagenId || null,
    });
    setImagenPreview(product.imagenId ? imagenService.obtenerUrlContenido(product.imagenId) : null);
    setErrorModal(null);
    setIsModalOpen(true);
  };

  // Cambio de Categoría Padre en el Formulario
  const handleCategoriaChange = (nuevaCatId) => {
    const primeraSub = subcategorias.find((s) => s.categoriaId === nuevaCatId)?.id || '';
    setFormData((prev) => ({
      ...prev,
      categoriaPadreId: nuevaCatId,
      subCategoriaId: primeraSub,
    }));
  };

  // Subida de Archivo de Imagen
  const handleFileChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) {
      setErrorModal('El archivo supera el tamaño máximo permitido de 5 MB.');
      return;
    }

    setSubiendoImagen(true);
    setErrorModal(null);

    try {
      const res = await imagenService.subirImagen(file, 'PRODUCTO');
      setFormData((prev) => ({ ...prev, imagenId: res.id }));
      setImagenPreview(imagenService.obtenerUrlContenido(res.id));
    } catch (err) {
      setErrorModal(err.message || 'Error al subir la imagen.');
    } finally {
      setSubiendoImagen(false);
    }
  };

  // Guardar Producto (Alta o Modificación)
  const handleSave = async (e) => {
    e.preventDefault();
    setErrorModal(null);

    if (!formData.codigo.trim()) {
      setErrorModal('El código comercial es obligatorio.');
      return;
    }
    if (!formData.nombre.trim()) {
      setErrorModal('El nombre del producto es obligatorio.');
      return;
    }
    if (!formData.subCategoriaId) {
      setErrorModal('Debe seleccionar una subcategoría obligatoria.');
      return;
    }

    setGuardando(true);

    const payload = {
      codigo: formData.codigo.trim().toUpperCase(),
      nombre: formData.nombre.trim(),
      descripcion: formData.descripcion.trim() || null,
      presentacion: formData.presentacion.trim(),
      unidadesPorBulto: parseInt(formData.unidadesPorBulto, 10) || 1,
      enOferta: Boolean(formData.enOferta),
      subCategoriaId: formData.subCategoriaId,
      imagenId: formData.imagenId || null,
      precioInicial: formData.precioInicial ? parseFloat(formData.precioInicial) : null,
      stockInicial: formData.stockInicial ? parseInt(formData.stockInicial, 10) : 0,
    };

    try {
      if (editingProduct) {
        await productoService.modificar(editingProduct.id, payload);
        setMensajeExito(`Producto "${payload.nombre}" actualizado con éxito.`);
      } else {
        await productoService.crear(payload);
        setMensajeExito(`Producto "${payload.nombre}" registrado exitosamente en el catálogo.`);
      }
      setIsModalOpen(false);
      cargarProductos();
    } catch (err) {
      if (err.status === 409 || err.code === 'DUPLICADO') {
        setErrorModal(`Ya existe un producto con el código "${payload.codigo}".`);
      } else {
        setErrorModal(err.message || 'Error al guardar el producto.');
      }
    } finally {
      setGuardando(false);
    }
  };

  // Handlers Actualización de Precio
  const handleOpenPrecio = (prod) => {
    setProductoPrecio(prod);
    setNuevoPrecio(prod.precioVigente ? prod.precioVigente.toString() : '');
    setModalPrecioOpen(true);
  };

  const handleGuardarPrecio = async (e) => {
    e.preventDefault();
    if (!productoPrecio || !nuevoPrecio) return;

    setActualizandoPrecio(true);
    try {
      await vigenciaPrecioService.registrarPrecio(productoPrecio.id, nuevoPrecio);
      setMensajeExito(`Nuevo precio mayorista fijado para "${productoPrecio.nombre}".`);
      setModalPrecioOpen(false);
      cargarProductos();
    } catch (err) {
      setMensajeError(err.message || 'Error al actualizar el precio.');
    } finally {
      setActualizandoPrecio(false);
    }
  };

  // Handlers Reposición de Stock
  const handleOpenStock = (prod) => {
    setProductoStock(prod);
    setCantidadReponer('20');
    setMotivoReposicion('Ingreso de fábrica');
    setModalStockOpen(true);
  };

  const handleGuardarStock = async (e) => {
    e.preventDefault();
    if (!productoStock || !cantidadReponer) return;

    setReponiendoStock(true);
    try {
      await stockService.reponerStock(productoStock.id, cantidadReponer, motivoReposicion);
      setMensajeExito(`Stock repuesto (+${cantidadReponer} bultos) para "${productoStock.nombre}".`);
      setModalStockOpen(false);
      cargarProductos();
    } catch (err) {
      setMensajeError(err.message || 'Error al reponer stock.');
    } finally {
      setReponiendoStock(false);
    }
  };

  // Handlers Eliminación
  const handleConfirmarEliminar = (prod) => {
    setProductoAEliminar(prod);
    setModalEliminarOpen(true);
  };

  const handleEjecutarEliminacion = async () => {
    if (!productoAEliminar) return;

    setEliminando(true);
    try {
      await productoService.eliminar(productoAEliminar.id);
      setMensajeExito(`Producto "${productoAEliminar.nombre}" dado de baja.`);
      setModalEliminarOpen(false);
      setProductoAEliminar(null);
      cargarProductos();
    } catch (err) {
      setMensajeError(err.message || 'Error al eliminar el producto.');
      setModalEliminarOpen(false);
    } finally {
      setEliminando(false);
    }
  };

  // Helper formateo moneda
  const formatPrecio = (val) => {
    if (val === null || val === undefined || isNaN(val)) return '$ --';
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
      maximumFractionDigits: 0,
    }).format(val);
  };

  return (
    <div className="space-y-6">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Catálogo Comercial
          </span>
          <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
            Gestión de Productos Mayoristas
          </h2>
          <p className="text-[12px] text-[#6b6b80] mt-0.5">
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

      {/* Aviso si no hay subcategorías */}
      {subcategorias.length === 0 && !cargando && (
        <div className="p-4 bg-[#fff0fb] border border-[#f5d5ee] rounded-[16px] text-[12px] text-[#93307b] flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Icon name="tag" className="w-5 h-5 shrink-0 text-[#e86bc4]" />
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
      <div className="bg-white rounded-[20px] p-4 border border-[#ececf2] shadow-sm flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-3 flex-wrap">
        <div className="flex items-center gap-2.5 flex-1 min-w-[280px]">
          <div className="relative flex-1">
            <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[#aaa9ba]">
              <Icon name="search" className="w-4 h-4" />
            </span>
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Buscar por código o nombre de producto..."
              className="w-full pl-10 pr-4 py-2 rounded-[12px] border border-[#ececf2] bg-[#f8f9fc] text-[12px] text-[#2b2b3a] focus:bg-white focus:outline-none focus:border-[#e86bc4]"
            />
          </div>

          {/* Filtro Categoría */}
          <select
            value={filtroCategoria}
            onChange={(e) => {
              setFiltroCategoria(e.target.value);
              setFiltroSubCategoria('');
            }}
            className="bg-[#f8f9fc] border border-[#ececf2] text-[#2b2b3a] text-[11px] font-medium px-3 py-2 rounded-[12px] outline-none focus:border-[#e86bc4]"
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
              className="bg-[#f8f9fc] border border-[#ececf2] text-[#2b2b3a] text-[11px] font-medium px-3 py-2 rounded-[12px] outline-none focus:border-[#e86bc4]"
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
        <div className="flex items-center gap-4 text-[11px] text-[#6b6b80] font-medium flex-wrap">
          <label className="flex items-center gap-2 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={filtroOferta}
              onChange={(e) => setFiltroOferta(e.target.checked)}
              className="rounded text-[#e86bc4] focus:ring-[#e86bc4]"
            />
            <span>Solo en oferta</span>
          </label>

          <label className="flex items-center gap-2 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={filtroConStock}
              onChange={(e) => setFiltroConStock(e.target.checked)}
              className="rounded text-[#e86bc4] focus:ring-[#e86bc4]"
            />
            <span>Solo con stock</span>
          </label>

          <span className="text-[#aaa9ba]">|</span>

          <span className="text-[#2b2b3a]">
            Total: <strong>{totalElementos}</strong> artículos
          </span>
        </div>
      </div>

      {/* Products Table */}
      <div className="bg-white rounded-[22px] p-6 border border-[#ececf2] shadow-sm">
        {cargando ? (
          <div className="py-16 text-center text-[12px] text-[#6b6b80]">
            <svg className="animate-spin h-7 w-7 text-[#e86bc4] mx-auto mb-3" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
            </svg>
            Cargando catálogo mayorista...
          </div>
        ) : productos.length === 0 ? (
          <div className="py-16 text-center space-y-3">
            <div className="w-12 h-12 mx-auto rounded-full bg-[#fff0fb] text-[#e86bc4] flex items-center justify-center">
              <Icon name="box" className="w-6 h-6" />
            </div>
            <h3 className="text-[14px] font-bold text-[#2b2b3a]">
              {searchTerm || filtroCategoria || filtroSubCategoria || filtroOferta || filtroConStock
                ? 'No se encontraron productos con los filtros aplicados'
                : 'Aún no hay productos registrados en el catálogo'}
            </h3>
            <p className="text-[12px] text-[#8c8b9c] max-w-sm mx-auto">
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
                <tr className="border-b border-[#ececf2] text-[10px] uppercase font-bold text-[#aaa9b8] tracking-wider">
                  <th className="pb-3 px-3">Producto</th>
                  <th className="pb-3 px-3">Código</th>
                  <th className="pb-3 px-3">Clasificación</th>
                  <th className="pb-3 px-3">Presentación</th>
                  <th className="pb-3 px-3">Precio Mayorista</th>
                  <th className="pb-3 px-3">Stock Actual</th>
                  <th className="pb-3 px-3 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#f0f0f4] text-[12px]">
                {productos.map((p) => {
                  const stockNum = p.stockActual ?? 0;
                  const stockBadge =
                    stockNum === 0
                      ? 'bg-[#ffe8ed] text-[#d63353]'
                      : stockNum <= 20
                      ? 'bg-[#fff0d3] text-[#986b16]'
                      : 'bg-[#dff8e9] text-[#39855b]';

                  const stockLabel =
                    stockNum === 0 ? 'Sin stock' : stockNum <= 20 ? 'Bajo stock' : 'Normal';

                  const imgUrl = p.imagenId ? imagenService.obtenerUrlContenido(p.imagenId) : null;

                  return (
                    <tr key={p.id} className="hover:bg-slate-50/70 transition-colors">
                      {/* Thumbnail & Name */}
                      <td className="py-3 px-3">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-[12px] bg-[#f8f9fc] border border-[#ececf2] overflow-hidden flex items-center justify-center shrink-0">
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
                              <Icon name="box" className="w-5 h-5 text-[#aaa9ba]" />
                            )}
                          </div>
                          <div className="leading-tight">
                            <strong className="block text-[#2b2b3a] font-semibold">
                              {p.nombre}
                            </strong>
                            {p.enOferta && (
                              <span className="inline-block mt-0.5 text-[9px] font-bold uppercase tracking-wider text-[#e86bc4] bg-[#ffe2f7] px-2 py-0.2 rounded-full">
                                En oferta
                              </span>
                            )}
                          </div>
                        </div>
                      </td>

                      {/* Code */}
                      <td className="py-3 px-3 font-mono text-[11px] text-[#8c8b9c]">
                        {p.codigo}
                      </td>

                      {/* Category & Subcategory */}
                      <td className="py-3 px-3">
                        <div className="space-y-0.5">
                          <span className="inline-block text-[10px] font-semibold px-2 py-0.5 rounded-full bg-[#f4f6fb] text-[#2b2b3a] border border-[#ececf2]">
                            {p.categoriaNombre || 'Sin cat.'}
                          </span>
                          <span className="block text-[11px] text-[#a34187] font-medium">
                            {p.subCategoriaNombre || 'General'}
                          </span>
                        </div>
                      </td>

                      {/* Presentation */}
                      <td className="py-3 px-3 text-[#6b6b80]">
                        <span>{p.presentacion}</span>
                        {p.unidadesPorBulto && (
                          <span className="block text-[10px] text-[#aaa9ba]">
                            ({p.unidadesPorBulto} u./bulto)
                          </span>
                        )}
                      </td>

                      {/* Price */}
                      <td className="py-3 px-3 font-semibold text-[#2b2b3a]">
                        <span>{formatPrecio(p.precioVigente)}</span>
                      </td>

                      {/* Stock */}
                      <td className="py-3 px-3">
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-[#2b2b3a]">{stockNum}</span>
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
                            className="p-1.5 rounded-[9px] text-[#6b6b80] hover:text-[#39855b] hover:bg-[#dff8e9]/60 transition-colors cursor-pointer"
                            title="Actualizar precio mayorista"
                          >
                            <Icon name="dollar" className="w-4 h-4" />
                          </button>

                          {/* Reponer Stock */}
                          <button
                            type="button"
                            onClick={() => handleOpenStock(p)}
                            className="p-1.5 rounded-[9px] text-[#6b6b80] hover:text-[#3fb8d9] hover:bg-[#d9f7ff]/40 transition-colors cursor-pointer"
                            title="Reponer stock"
                          >
                            <Icon name="truck" className="w-4 h-4" />
                          </button>

                          {/* Modificar Ficha */}
                          <button
                            type="button"
                            onClick={() => handleOpenEdit(p)}
                            className="p-1.5 rounded-[9px] text-[#6b6b80] hover:text-[#e86bc4] hover:bg-[#ffe2f7]/50 transition-colors cursor-pointer"
                            title="Editar especificaciones"
                          >
                            <Icon name="edit" className="w-4 h-4" />
                          </button>

                          {/* Eliminar */}
                          <button
                            type="button"
                            onClick={() => handleConfirmarEliminar(p)}
                            className="p-1.5 rounded-[9px] text-[#6b6b80] hover:text-[#d63353] hover:bg-[#ffe8ed] transition-colors cursor-pointer"
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
            <div className="p-3 bg-[#ffe8ed] border border-[#ffccd5] rounded-[12px] text-[12px] text-[#d63353] flex items-center gap-2">
              <Icon name="alert" className="w-4 h-4 shrink-0" />
              <span>{errorModal}</span>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
            {/* Código comercial */}
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Código de producto *
              </label>
              <input
                type="text"
                required
                maxLength={50}
                value={formData.codigo}
                onChange={(e) => setFormData({ ...formData, codigo: e.target.value.toUpperCase() })}
                placeholder="Ej. ALF-TRIP-01"
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] font-mono uppercase text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            {/* Nombre */}
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Nombre comercial *
              </label>
              <input
                type="text"
                required
                maxLength={150}
                value={formData.nombre}
                onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                placeholder="Ej. Alfajor Triple Chocolate"
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            {/* Selector de Categoría Padre */}
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Categoría principal *
              </label>
              <select
                required
                value={formData.categoriaPadreId}
                onChange={(e) => handleCategoriaChange(e.target.value)}
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
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
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Subcategoría (Obligatoria) *
              </label>
              <select
                required
                value={formData.subCategoriaId}
                onChange={(e) => setFormData({ ...formData, subCategoriaId: e.target.value })}
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
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
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Presentación comercial *
              </label>
              <input
                type="text"
                required
                value={formData.presentacion}
                onChange={(e) => setFormData({ ...formData, presentacion: e.target.value })}
                placeholder="Ej. Caja x 24 u. / Display x 50 u."
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            {/* Unidades por bulto */}
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Unidades por bulto *
              </label>
              <input
                type="number"
                min="1"
                required
                value={formData.unidadesPorBulto}
                onChange={(e) => setFormData({ ...formData, unidadesPorBulto: e.target.value })}
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            {/* Precio inicial (Solo alta) */}
            {!editingProduct && (
              <div>
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
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
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>
            )}

            {/* Stock inicial (Solo alta) */}
            {!editingProduct && (
              <div>
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                  Stock inicial (Bultos) *
                </label>
                <input
                  type="number"
                  min="0"
                  required
                  value={formData.stockInicial}
                  onChange={(e) => setFormData({ ...formData, stockInicial: e.target.value })}
                  placeholder="Ej. 100"
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>
            )}
          </div>

          {/* En Oferta Checkbox */}
          <div className="pt-1">
            <label className="flex items-center gap-2 cursor-pointer select-none text-[12px] text-[#2b2b3a] font-medium">
              <input
                type="checkbox"
                checked={formData.enOferta}
                onChange={(e) => setFormData({ ...formData, enOferta: e.target.checked })}
                className="rounded text-[#e86bc4] focus:ring-[#e86bc4]"
              />
              <span>Destacar producto en sección "En Oferta"</span>
            </label>
          </div>

          {/* Descripción */}
          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Descripción o notas de cata / sabor
            </label>
            <textarea
              rows="2"
              maxLength={500}
              value={formData.descripcion}
              onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
              placeholder="Detalle de sabor, ingredientes o especificaciones para el cliente..."
              className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
            />
          </div>

          {/* Subida de Imagen con Preview */}
          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
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
              className="border-2 border-dashed border-[#ececf2] rounded-[16px] p-4 flex items-center justify-center gap-4 text-[#6b6b80] text-[11px] hover:border-[#3fb8d9] transition-colors cursor-pointer bg-[#fafafd]"
            >
              {imagenPreview ? (
                <div className="flex items-center gap-3">
                  <img
                    src={imagenPreview}
                    alt="Preview"
                    className="w-14 h-14 object-cover rounded-[10px] border border-[#ececf2]"
                  />
                  <div className="text-left">
                    <span className="text-[#3fb8d9] font-bold block text-[12px]">Imagen seleccionada</span>
                    <span className="text-[10px] text-[#aaa9ba]">Hacé clic para cambiar la foto</span>
                  </div>
                </div>
              ) : subiendoImagen ? (
                <div className="flex items-center gap-2 text-[#3fb8d9]">
                  <svg className="animate-spin h-5 w-5 text-[#3fb8d9]" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                  </svg>
                  <span>Subiendo imagen al servidor...</span>
                </div>
              ) : (
                <div className="flex items-center gap-2">
                  <Icon name="camera" className="w-5 h-5 text-[#3fb8d9]" />
                  <span className="text-[#3fb8d9] font-bold">Cargar imagen (PNG, JPG o WebP hasta 5 MB)</span>
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
          <p className="text-[12px] text-[#6b6b80]">
            Establecé el nuevo precio de vigencia para{' '}
            <strong className="text-[#2b2b3a]">{productoPrecio?.nombre}</strong>.
          </p>

          <div className="p-3 bg-[#f8f9fc] rounded-[12px] border border-[#ececf2] text-[11px] flex justify-between">
            <span className="text-[#6b6b80]">Precio actual vigente:</span>
            <strong className="text-[#2b2b3a]">{formatPrecio(productoPrecio?.precioVigente)}</strong>
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
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
              className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
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
          <p className="text-[12px] text-[#6b6b80]">
            Registrá la entrada de mercadería para{' '}
            <strong className="text-[#2b2b3a]">{productoStock?.nombre}</strong>.
          </p>

          <div className="p-3 bg-[#f8f9fc] rounded-[12px] border border-[#ececf2] text-[11px] flex justify-between">
            <span className="text-[#6b6b80]">Stock disponible actual:</span>
            <strong className="text-[#2b2b3a]">{productoStock?.stockActual || 0} bultos</strong>
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
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
              className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Motivo u observación
            </label>
            <input
              type="text"
              value={motivoReposicion}
              onChange={(e) => setMotivoReposicion(e.target.value)}
              placeholder="Ej. Ingreso de fábrica lote 402"
              className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
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
          <p className="text-[12px] text-[#2b2b3a]">
            ¿Estás seguro de que deseás dar de baja el producto{' '}
            <strong className="text-[#d63353]">"{productoAEliminar?.nombre}"</strong> ({productoAEliminar?.codigo})?
          </p>

          <p className="text-[11px] text-[#6b6b80]">
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
