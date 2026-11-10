/**
 * Listado y Seguimiento de Pedidos para el Preventista.
 * Conectado con el endpoint /api/v1/pedidos del backend.
 */
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Chip from '../../components/Chip';
import { pedidoService } from '../../services/pedidoService';

export default function Pedidos() {
  const [orders, setOrders] = useState([]);
  const [filterState, setFilterState] = useState('TODOS');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  const filterTabs = [
    { id: 'TODOS', label: 'Todos' },
    { id: 'CARGADO', label: 'Cargados' },
    { id: 'SIN_STOCK', label: 'Sin Stock' },
    { id: 'FACTURADO', label: 'Facturados' },
    { id: 'EN_REPARTO', label: 'En Reparto' },
    { id: 'ENTREGADO', label: 'Entregados' },
  ];

  const getStatusColor = (estado) => {
    switch (estado) {
      case 'CARGADO':
        return 'warning';
      case 'FACTURADO':
        return 'success';
      case 'SIN_STOCK':
        return 'pink';
      case 'EN_REPARTO':
        return 'blue';
      case 'ENTREGADO':
        return 'success';
      case 'ANULADO':
        return 'muted';
      default:
        return 'muted';
    }
  };

  useEffect(() => {
    async function cargarPedidos() {
      try {
        setCargando(true);
        setError(null);
        const data = await pedidoService.listarPedidos({ size: 100 });
        const list = Array.isArray(data) ? data : (data?.content || []);
        setOrders(list);
      } catch (err) {
        console.error('Error al listar pedidos:', err);
        setError('No se pudieron cargar los pedidos desde el servidor.');
      } finally {
        setCargando(false);
      }
    }
    cargarPedidos();
  }, []);

  const filteredOrders = orders.filter((o) => {
    const matchesFilter = filterState === 'TODOS' || o.estado === filterState;
    const id = o.id || '';
    const comercio = o.puntoDeVenta?.nombreComercio || '';
    const term = searchTerm.toLowerCase();

    return matchesFilter && (id.toLowerCase().includes(term) || comercio.toLowerCase().includes(term));
  });

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Historial de Ventas
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a] mt-0.5">
            Mis Pedidos de Preventa
          </h1>
          <p className="text-[12px] text-[#6b6b80]">
            Pedidos registrados y su evolución en depósito, facturación y reparto.
          </p>
        </div>

        <Link to="/tienda">
          <Button className="w-full sm:w-auto text-[12px] py-2.5">
            <Icon name="bag" className="w-4 h-4" />
            <span>+ Cargar Nuevo Pedido</span>
          </Button>
        </Link>
      </div>

      {/* Filtros de Estado */}
      <div className="flex items-center gap-1.5 overflow-x-auto pb-1 no-scrollbar">
        {filterTabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setFilterState(tab.id)}
            className={`px-3.5 py-1.5 rounded-full text-[11px] font-semibold whitespace-nowrap transition-all cursor-pointer ${
              filterState === tab.id
                ? 'bg-[#e86bc4] text-white shadow-sm'
                : 'bg-white text-[#6b6b80] border border-[#ececf2] hover:bg-slate-50'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Buscador */}
      <div className="relative">
        <Icon name="search" className="w-4 h-4 text-[#aaa9ba] absolute left-3.5 top-3" />
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="Buscar por ID de pedido o nombre de comercio..."
          className="w-full bg-white border border-[#ececf2] rounded-[14px] pl-10 pr-4 py-2.5 text-[12px] text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4] shadow-sm"
        />
      </div>

      {/* Estados de carga o error */}
      {cargando && (
        <div className="flex justify-center items-center py-12">
          <div className="w-8 h-8 border-3 border-[#e86bc4] border-t-transparent rounded-full animate-spin"></div>
        </div>
      )}

      {error && (
        <div className="p-4 bg-[#ffe8ed] border border-[#ffccd5] rounded-[16px] text-[12px] text-[#e5484d]">
          {error}
        </div>
      )}

      {/* Listado de Pedidos */}
      {!cargando && !error && (
        <>
          {filteredOrders.length === 0 ? (
            <div className="text-center py-12 bg-white rounded-[20px] border border-[#ececf2]">
              <p className="text-[#6b6b80] text-[13px]">No hay pedidos con el filtro actual.</p>
            </div>
          ) : (
            <div className="space-y-3">
              {filteredOrders.map((order) => {
                const total = order.total ?? order.detalles?.reduce((acc, d) => acc + (d.subtotal || 0), 0) ?? 0;
                const formattedTotal = new Intl.NumberFormat('es-AR', {
                  style: 'currency',
                  currency: 'ARS',
                  maximumFractionDigits: 0,
                }).format(total);

                const pdv = order.puntoDeVenta;
                const dir = pdv?.direccion ? `${pdv.direccion.calle || ''} ${pdv.direccion.numero || ''}` : 'Mendoza';

                return (
                  <div
                    key={order.id}
                    className="bg-white rounded-[18px] p-4 sm:p-5 border border-[#ececf2] shadow-sm hover:border-[#d9d9e6] transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-4"
                  >
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="font-mono text-[12px] font-bold text-[#e86bc4]">
                          #{order.id?.slice(-6) || order.id}
                        </span>
                        <Chip variant={getStatusColor(order.estado)}>
                          {order.estado}
                        </Chip>
                      </div>
                      <h3 className="font-bold text-[14px] text-[#2b2b3a]">
                        {pdv?.nombreComercio || 'Comercio Minorista'}
                      </h3>
                      <p className="text-[11px] text-[#6b6b80]">
                        {dir} · Fecha: {order.fechaCreacion || order.fecha || 'Reciente'}
                      </p>
                    </div>

                    <div className="flex items-center justify-between sm:justify-end gap-4 pt-2 sm:pt-0 border-t sm:border-0 border-[#f0f0f4]">
                      <div className="text-left sm:text-right">
                        <span className="text-[10px] text-[#aaa9ba] block uppercase">Total</span>
                        <strong className="text-[15px] font-bold text-[#2b2b3a] font-mono">
                          {formattedTotal}
                        </strong>
                      </div>

                      <button
                        type="button"
                        onClick={() => setSelectedOrder(order)}
                        className="p-2 text-[#6b6b80] hover:text-[#e86bc4] rounded-[10px] bg-[#f8f9fc] hover:bg-[#ffe2f7]/50 transition-colors cursor-pointer"
                        title="Ver detalle"
                      >
                        <Icon name="eye" className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </>
      )}

      {/* Modal Detalle de Pedido */}
      {selectedOrder && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-[24px] max-w-lg w-full p-6 shadow-xl border border-[#ececf2] space-y-4 max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-start">
              <div>
                <span className="text-[10px] font-bold text-[#e86bc4] uppercase tracking-wider">
                  Detalle de Pedido
                </span>
                <h2 className="text-xl font-bold text-[#2b2b3a]">
                  #{selectedOrder.id}
                </h2>
              </div>
              <button
                type="button"
                onClick={() => setSelectedOrder(null)}
                className="text-[#aaa9ba] hover:text-[#2b2b3a] p-1 cursor-pointer font-bold"
              >
                ✕
              </button>
            </div>

            <div className="bg-[#f8f9fc] rounded-[14px] p-3 text-[12px] space-y-1">
              <p><strong>Comercio:</strong> {selectedOrder.puntoDeVenta?.nombreComercio}</p>
              <p><strong>Estado Actual:</strong> <Chip variant={getStatusColor(selectedOrder.estado)}>{selectedOrder.estado}</Chip></p>
            </div>

            <div className="space-y-2">
              <h4 className="text-[12px] font-bold text-[#2b2b3a]">Productos Solicitados:</h4>
              <div className="divide-y divide-[#ececf2]">
                {selectedOrder.detalles?.map((det, idx) => (
                  <div key={idx} className="py-2 flex justify-between text-[12px]">
                    <span>{det.producto?.nombre || 'Producto'} (x{det.cantidad})</span>
                    <span className="font-mono font-bold">${det.subtotal?.toLocaleString('es-AR')}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="pt-2">
              <Button onClick={() => setSelectedOrder(null)} className="w-full justify-center text-[12px] cursor-pointer">
                Cerrar
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
