import { useState, useEffect, useCallback, useRef } from 'react';
import { productoService } from '../services/productoService';
import { categoriaService } from '../services/categoriaService';
import { subCategoriaService } from '../services/subCategoriaService';
import { imagenService } from '../services/imagenService';
import { vigenciaPrecioService } from '../services/vigenciaPrecioService';
import { stockService } from '../services/stockService';

/**
 * Custom hook managing the catalog state, filters, image uploads, price/stock adjustments, and deletions.
 */
export function useProductos() {
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

  return {
    // Data & state
    productos,
    categorias,
    subcategorias,
    cargando,
    totalElementos,
    cargarProductos,

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
  };
}
