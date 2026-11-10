import { useState, useEffect, useCallback } from 'react';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { apiFetch } from '../../services/api';
import { productoService } from '../../services/productoService';

export default function Promociones() {
  const [promotions, setPromotions] = useState([]);
  const [productos, setProductos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterState, setFilterState] = useState('Todas');
  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingPromo, setEditingPromo] = useState(null);

  const hoyIso = new Date().toISOString().split('T')[0];

  const [formData, setFormData] = useState({
    codigo: '',
    descripcion: '',
    porcentajeDescuento: 10,
    cantidadMinima: 0,
    productoId: '',
    fechaDesde: hoyIso,
    fechaHasta: '2026-12-31',
  });

  const cargarDatos = useCallback(async () => {
    setLoading(true);
    try {
      const [promosRes, prodsRes] = await Promise.all([
        apiFetch('/promociones', { method: 'GET' }),
        productoService.listar({ size: 200 }).catch(() => ({ elementos: [] })),
      ]);

      const listaPromos = Array.isArray(promosRes)
        ? promosRes
        : promosRes.elementos || promosRes.content || [];
      const listaProds = Array.isArray(prodsRes)
        ? prodsRes
        : prodsRes.elementos || prodsRes.content || [];

      setProductos(listaProds);

      const mapped = listaPromos.map((p) => {
        const hoy = new Date().toISOString().split('T')[0];
        const vigente = (!p.fechaDesde || p.fechaDesde <= hoy) && (!p.fechaHasta || p.fechaHasta >= hoy);
        return {
          id: p.id,
          codigo: p.codigo,
          descripcion: p.descripcion || '',
          porcentajeDescuento: Number(p.porcentajeDescuento || 0),
          cantidadMinima: Number(p.cantidadMinima || 0),
          productoId: p.productoId || '',
          productoDestacado: p.productoNombre || '',
          fechaDesde: p.fechaDesde || hoy,
          fechaHasta: p.fechaHasta || hoy,
          estado: vigente ? 'Vigente' : 'Vencida',
        };
      });
      setPromotions(mapped);
    } catch (err) {
      console.error('Error al cargar promociones:', err);
      setPromotions([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarDatos();
  }, [cargarDatos]);

  const handleOpenCreate = () => {
    setEditingPromo(null);
    setFormData({
      codigo: '',
      descripcion: '',
      porcentajeDescuento: 10,
      cantidadMinima: 0,
      productoId: '',
      fechaDesde: hoyIso,
      fechaHasta: '2026-12-31',
    });
    setIsModalOpen(true);
  };

  const handleOpenEdit = (p) => {
    setEditingPromo(p);
    setFormData({
      codigo: p.codigo,
      descripcion: p.descripcion,
      porcentajeDescuento: p.porcentajeDescuento,
      cantidadMinima: p.cantidadMinima,
      productoId: p.productoId || '',
      fechaDesde: p.fechaDesde,
      fechaHasta: p.fechaHasta,
    });
    setIsModalOpen(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const payload = {
      codigo: formData.codigo.toUpperCase().trim(),
      descripcion: formData.descripcion.trim(),
      porcentajeDescuento: Number(formData.porcentajeDescuento),
      cantidadMinima: Number(formData.cantidadMinima || 0),
      productoId: formData.productoId ? formData.productoId : null,
      fechaDesde: formData.fechaDesde,
      fechaHasta: formData.fechaHasta,
    };

    try {
      if (editingPromo) {
        await apiFetch(`/promociones/${editingPromo.id}`, {
          method: 'PUT',
          body: JSON.stringify(payload),
        });
      } else {
        await apiFetch('/promociones', {
          method: 'POST',
          body: JSON.stringify(payload),
        });
      }
      setIsModalOpen(false);
      await cargarDatos();
    } catch (err) {
      alert(`Error al guardar la promoción: ${err.message}`);
    }
  };

  const handleDelete = async (id, codigo) => {
    if (!confirm(`¿Dar de baja la promoción ${codigo}?`)) return;
    try {
      await apiFetch(`/promociones/${id}`, { method: 'DELETE' });
      await cargarDatos();
    } catch (err) {
      alert(`Error al eliminar promoción: ${err.message}`);
    }
  };

  const filtered = promotions.filter((p) => {
    const matchesFilter =
      filterState === 'Todas' ||
      (filterState === 'Vigentes' && p.estado === 'Vigente') ||
      (filterState === 'Vencidas' && p.estado === 'Vencida');
    const matchesSearch =
      p.codigo.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.descripcion.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesFilter && matchesSearch;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Comercial y Marketing
          </span>
          <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
            Códigos de Promoción y Descuentos
          </h2>
          <p className="text-[12px] text-[#6b6b80] mt-0.5">
            Configurá descuentos por volumen, cupones temporales y promociones en productos destacados.
          </p>
        </div>

        <Button onClick={handleOpenCreate}>
          <Icon name="plus" className="w-4 h-4" />
          <span>Nueva Promoción</span>
        </Button>
      </div>

      {/* Filterbar */}
      <div className="bg-white rounded-[18px] p-4 border border-[#ececf2] shadow-sm flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          {['Todas', 'Vigentes', 'Vencidas'].map((tab) => (
            <button
              key={tab}
              type="button"
              onClick={() => setFilterState(tab)}
              className={`px-3 py-1.5 rounded-[10px] text-[11px] font-semibold transition-all cursor-pointer ${
                filterState === tab
                  ? 'bg-[#2b2b3a] text-white shadow-sm'
                  : 'bg-[#fafafd] text-[#6b6b80] border border-[#ececf2] hover:bg-slate-50'
              }`}
            >
              {tab}
            </button>
          ))}
        </div>

        <div className="relative sm:w-72">
          <Icon name="search" className="w-4 h-4 text-[#aaa9ba] absolute left-3 top-2.5" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Buscar por código..."
            className="w-full bg-[#fafafd] border border-[#ececf2] rounded-[10px] pl-9 pr-3 py-1.5 text-[11px] text-[#2b2b3a] focus:bg-white focus:outline-none focus:border-[#e86bc4]"
          />
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-[20px] p-6 border border-[#ececf2] shadow-sm">
        <div className="flex items-center justify-between pb-3 mb-3 border-b border-[#f0f0f4]">
          <h3 className="text-[15px] font-bold text-[#2b2b3a]">
            Promociones Configuradas
          </h3>
          <span className="text-[11px] text-[#6b6b80]">
            Total registros: <strong>{filtered.length}</strong>
          </span>
        </div>

        <div className="overflow-x-auto">
          {loading ? (
            <div className="py-10 text-center text-[12px] text-[#6b6b80]">
              Cargando promociones...
            </div>
          ) : filtered.length === 0 ? (
            <div className="py-10 text-center text-[13px] text-[#6b6b80]">
              {promotions.length === 0
                ? 'No hay promociones dadas de alta aún. Hacé clic en "Nueva Promoción" para crear una.'
                : 'No se encontraron promociones con el filtro actual.'}
            </div>
          ) : (
            <table className="w-full text-left text-[12px]">
              <thead>
                <tr className="border-b border-[#ececf2] text-[#6b6b80] font-semibold">
                  <th className="pb-3 px-3">Código</th>
                  <th className="pb-3 px-3">Descripción</th>
                  <th className="pb-3 px-3">Descuento</th>
                  <th className="pb-3 px-3">Condición Mínima</th>
                  <th className="pb-3 px-3">Vigencia</th>
                  <th className="pb-3 px-3">Estado</th>
                  <th className="pb-3 px-3 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#f0f0f4]">
                {filtered.map((p) => (
                  <tr key={p.id} className="hover:bg-[#fafafd] transition-colors">
                    <td className="py-3 px-3 font-mono font-bold text-[#e86bc4]">
                      {p.codigo}
                    </td>
                    <td className="py-3 px-3 font-medium text-[#2b2b3a] max-w-xs truncate">
                      {p.descripcion}
                      {p.productoDestacado && (
                        <span className="text-[10px] text-[#3fb8d9] block">
                          Producto: {p.productoDestacado}
                        </span>
                      )}
                    </td>
                    <td className="py-3 px-3 font-bold text-[#2b2b3a]">
                      {p.porcentajeDescuento}% OFF
                    </td>
                    <td className="py-3 px-3 text-[#6b6b80]">
                      {p.cantidadMinima > 0 ? `${p.cantidadMinima} bultos mín.` : 'Sin mínimo'}
                    </td>
                    <td className="py-3 px-3 text-[11px] text-[#6b6b80]">
                      {p.fechaDesde} al {p.fechaHasta}
                    </td>
                    <td className="py-3 px-3">
                      <span
                        className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full ${
                          p.estado === 'Vigente'
                            ? 'bg-[#dff8e9] text-[#39855b]'
                            : 'bg-[#f0f0f4] text-[#6b6b80]'
                        }`}
                      >
                        {p.estado}
                      </span>
                    </td>
                    <td className="py-3 px-3 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          type="button"
                          onClick={() => handleOpenEdit(p)}
                          className="text-[11px] font-semibold text-[#3fb8d9] hover:underline"
                        >
                          Editar
                        </button>
                        <button
                          type="button"
                          onClick={() => handleDelete(p.id, p.codigo)}
                          className="text-[11px] font-semibold text-[#ff8fa3] hover:underline"
                        >
                          Baja
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Modal Alta / Edición */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingPromo ? `Editar Promoción ${editingPromo.codigo}` : 'Crear Nueva Promoción'}
      >
        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Código de Promoción *
              </label>
              <input
                type="text"
                required
                value={formData.codigo}
                onChange={(e) => setFormData({ ...formData, codigo: e.target.value })}
                placeholder="EJ: DULCE15"
                className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] uppercase font-mono bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Porcentaje de Descuento (%) *
              </label>
              <input
                type="number"
                min="1"
                max="100"
                required
                value={formData.porcentajeDescuento}
                onChange={(e) =>
                  setFormData({ ...formData, porcentajeDescuento: parseFloat(e.target.value) || 0 })
                }
                className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Descripción del Descuento *
            </label>
            <input
              type="text"
              required
              value={formData.descripcion}
              onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
              placeholder="Ej: 15% de descuento por compra mayor a 5 bultos"
              className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] bg-white focus:outline-none focus:border-[#e86bc4]"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Cantidad Mínima de Bultos
              </label>
              <input
                type="number"
                min="0"
                value={formData.cantidadMinima}
                onChange={(e) =>
                  setFormData({ ...formData, cantidadMinima: parseInt(e.target.value, 10) || 0 })
                }
                placeholder="0 = Sin requisito"
                className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Producto Destacado (Opcional)
              </label>
              <select
                value={formData.productoId}
                onChange={(e) => setFormData({ ...formData, productoId: e.target.value })}
                className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] bg-white focus:outline-none focus:border-[#e86bc4]"
              >
                <option value="">Aplica a toda la compra</option>
                {productos.map((prod) => (
                  <option key={prod.id} value={prod.id}>
                    {prod.nombre}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Fecha Desde *
              </label>
              <input
                type="date"
                required
                value={formData.fechaDesde}
                onChange={(e) => setFormData({ ...formData, fechaDesde: e.target.value })}
                className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Fecha Hasta *
              </label>
              <input
                type="date"
                required
                value={formData.fechaHasta}
                onChange={(e) => setFormData({ ...formData, fechaHasta: e.target.value })}
                className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-[#f0f0f4]">
            <Button secondary type="button" onClick={() => setIsModalOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit">
              {editingPromo ? 'Guardar Cambios' : 'Crear Promoción'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
