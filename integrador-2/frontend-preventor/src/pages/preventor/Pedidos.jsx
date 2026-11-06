import { useState } from 'react';
import { Link } from 'react-router-dom';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Chip from '../../components/Chip';
import { preventorOrdersMock } from '../../mocks/preventorOrdersMock';

export default function Pedidos() {
  const [orders, setOrders] = useState(preventorOrdersMock);
  const [filterState, setFilterState] = useState('TODOS');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [editingOrder, setEditingOrder] = useState(null);

  // Estados disponibles según la máquina de estados del dominio
  const filterTabs = [
    { id: 'TODOS', label: 'Todos' },
    { id: 'CARGADO', label: 'Cargados' },
    { id: 'SIN_STOCK', label: 'Sin Stock' },
    { id: 'FACTURADO', label: 'Facturados' },
    { id: 'EN_REPARTO', label: 'En Reparto' },
    { id: 'ENTREGADO', label: 'Entregados' }
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

  const filteredOrders = orders.filter((o) => {
    const matchesFilter = filterState === 'TODOS' || o.estado === filterState;
    const matchesSearch =
      o.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      o.comercio.toLowerCase().includes(searchTerm.toLowerCase()) ||
      o.direccion.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesFilter && matchesSearch;
  });

  const handleAnularPedido = (orderId) => {
    if (confirm(`¿Estás seguro de anular el pedido #${orderId}? Esta acción no se puede deshacer.`)) {
      setOrders((prev) =>
        prev.map((o) =>
          o.id === orderId
            ? {
                ...o,
                estado: 'ANULADO',
                puedeModificar: false,
                historial: [
                  ...o.historial,
                  {
                    fechaHora: 'Ahora',
                    estado: 'ANULADO',
                    usuario: 'Lucía Martínez (Preventor)',
                    observacion: 'Anulado por preventor a solicitud del cliente.'
                  }
                ]
              }
            : o
        )
      );
    }
  };

  const handleSaveEdit = (e) => {
    e.preventDefault();
    if (!editingOrder) return;
    setOrders((prev) =>
      prev.map((o) => (o.id === editingOrder.id ? editingOrder : o))
    );
    alert(`Pedido #${editingOrder.id} modificado exitosamente.`);
    setEditingOrder(null);
  };

  return (
    <div className="space-y-5 max-w-4xl mx-auto">
      {/* Encabezado */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Trazabilidad y Gestión
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a] mt-0.5">
            Mis Pedidos Gestionados
          </h1>
          <p className="text-[12px] text-[#6b6b80]">
            Consultá el estado real de tus solicitudes y modificá pedidos en estado <strong>CARGADO</strong>.
          </p>
        </div>

        <Link to="/tienda">
          <Button className="w-full sm:w-auto text-[12px] py-2.5">
            <Icon name="bag" className="w-4 h-4" />
            <span>Nuevo Pedido</span>
          </Button>
        </Link>
      </div>

      {/* Buscador y Filtros por Chip */}
      <div className="space-y-3">
        <div className="relative">
          <Icon name="search" className="w-4 h-4 text-[#aaa9ba] absolute left-3.5 top-3" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Buscar por N° pedido (#DM-2095), comercio o calle..."
            className="w-full bg-white border border-[#ececf2] rounded-[14px] pl-10 pr-4 py-2.5 text-[12px] text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4] shadow-sm"
          />
        </div>

        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 no-scrollbar">
          {filterTabs.map((tab) => {
            const isActive = filterState === tab.id;
            const count = tab.id === 'TODOS'
              ? orders.length
              : orders.filter((o) => o.estado === tab.id).length;

            return (
              <button
                key={tab.id}
                type="button"
                onClick={() => setFilterState(tab.id)}
                className={`px-3 py-1.5 rounded-full text-[11px] font-semibold whitespace-nowrap transition-all cursor-pointer ${
                  isActive
                    ? 'bg-[#2b2b3a] text-white shadow-sm'
                    : 'bg-white text-[#6b6b80] border border-[#ececf2] hover:bg-slate-50'
                }`}
              >
                {tab.label} ({count})
              </button>
            );
          })}
        </div>
      </div>

      {/* Listado de Pedidos en Tarjetas Mobile-First */}
      <div className="space-y-3">
        {filteredOrders.length === 0 ? (
          <div className="bg-white rounded-[20px] p-8 text-center border border-[#ececf2]">
            <Icon name="box" className="w-10 h-10 text-[#aaa9ba] mx-auto mb-2" />
            <h3 className="text-base font-bold text-[#2b2b3a]">No hay pedidos en este estado</h3>
            <p className="text-[12px] text-[#6b6b80] mt-1">
              Probá seleccionando otro filtro o iniciando una nueva solicitud desde la tienda.
            </p>
          </div>
        ) : (
          filteredOrders.map((order) => (
            <div
              key={order.id}
              className="bg-white rounded-[18px] p-4 sm:p-5 border border-[#ececf2] shadow-sm hover:border-[#d9d9e6] transition-all space-y-3"
            >
              {/* Top row: ID, Estado y Fecha */}
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <strong className="text-[14px] font-bold text-[#2b2b3a] font-mono">
                    #{order.id}
                  </strong>
                  <Chip variant={getStatusColor(order.estado)}>
                    {order.estado.replace('_', ' ')}
                  </Chip>
                </div>
                <span className="text-[11px] text-[#aaa9ba]">
                  {order.fechaCarga}
                </span>
              </div>

              {/* Info Comercio y Monto */}
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pt-1 border-t border-[#f0f0f4]">
                <div>
                  <h3 className="text-[13px] font-bold text-[#2b2b3a]">
                    {order.comercio}
                  </h3>
                  <p className="text-[11px] text-[#6b6b80]">
                    {order.direccion} · Titular: {order.titular}
                  </p>
                </div>
                <div className="text-left sm:text-right">
                  <span className="text-[10px] text-[#aaa9ba] block uppercase">Monto Total</span>
                  <strong className="text-[15px] font-bold text-[#2b2b3a]">
                    ${order.montoTotal.toLocaleString('es-AR')}
                  </strong>
                  <span className="text-[10px] text-[#6b6b80] block">
                    {order.bultos} bultos totales
                  </span>
                </div>
              </div>

              {/* Advertencia si está sin stock */}
              {order.estado === 'SIN_STOCK' && order.motivoSinStock && (
                <div className="p-2.5 rounded-[10px] bg-[#fdf2f4] border border-[#fbd3db] text-[11px] text-[#c95569] flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-[#c95569] shrink-0" />
                  <span>{order.motivoSinStock}</span>
                </div>
              )}

              {/* Botones de acción según regla de negocio */}
              <div className="pt-2 border-t border-[#f0f0f4] flex flex-wrap items-center justify-between gap-2">
                <button
                  type="button"
                  onClick={() => setSelectedOrder(order)}
                  className="text-[11px] font-semibold text-[#3fb8d9] hover:underline flex items-center gap-1 cursor-pointer"
                >
                  <Icon name="route" className="w-3.5 h-3.5" />
                  Ver trazabilidad ({order.historial.length})
                </button>

                <div className="flex items-center gap-2 ml-auto">
                  {order.estado === 'CARGADO' ? (
                    <>
                      <button
                        type="button"
                        onClick={() => handleAnularPedido(order.id)}
                        className="text-[11px] font-semibold text-[#ff8fa3] hover:underline px-2 py-1"
                      >
                        Anular
                      </button>
                      <button
                        type="button"
                        onClick={() => setEditingOrder(order)}
                        className="bg-[#ffe2f7] text-[#e86bc4] hover:bg-[#ffd5f4] text-[11px] font-bold px-3 py-1.5 rounded-[10px] transition-colors cursor-pointer"
                      >
                        Modificar Pedido
                      </button>
                    </>
                  ) : order.estado === 'FACTURADO' ? (
                    <span className="text-[10px] text-[#39855b] font-semibold bg-[#dff8e9] px-2.5 py-1 rounded-md">
                      {order.facturaNumero}
                    </span>
                  ) : order.estado === 'EN_REPARTO' ? (
                    <span className="text-[10px] text-[#247f99] font-semibold bg-[#d9f7ff] px-2.5 py-1 rounded-md">
                      En reparto por {order.distribuidor}
                    </span>
                  ) : null}
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* MODAL DE TRAZABILIDAD (HistorialEstadoPedido) */}
      {selectedOrder && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-[22px] max-w-md w-full p-6 shadow-xl border border-[#ececf2] space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-[#f0f0f4]">
              <div>
                <span className="text-[10px] text-[#aaa9ba] uppercase font-bold">Ciclo de vida</span>
                <h3 className="text-base font-bold text-[#2b2b3a]">
                  Trazabilidad Pedido #{selectedOrder.id}
                </h3>
              </div>
              <button
                type="button"
                onClick={() => setSelectedOrder(null)}
                className="w-7 h-7 rounded-full bg-slate-100 text-[#6b6b80] hover:text-[#2b2b3a] flex items-center justify-center"
              >
                ✕
              </button>
            </div>

            <div className="space-y-3">
              {selectedOrder.historial.map((h, i) => (
                <div key={i} className="flex gap-3 text-[12px] relative">
                  <div className="flex flex-col items-center">
                    <span className="w-3 h-3 rounded-full bg-[#e86bc4] shrink-0 mt-1" />
                    {i < selectedOrder.historial.length - 1 && (
                      <span className="w-0.5 flex-1 bg-[#f0f0f4] my-1" />
                    )}
                  </div>
                  <div className="flex-1 pb-3">
                    <div className="flex items-center justify-between">
                      <strong className="font-bold text-[#2b2b3a]">{h.estado}</strong>
                      <span className="text-[10px] text-[#aaa9ba]">{h.fechaHora}</span>
                    </div>
                    <p className="text-[11px] text-[#6b6b80] mt-0.5">{h.observacion}</p>
                    <span className="text-[10px] text-[#aaa9ba]">Por: {h.usuario}</span>
                  </div>
                </div>
              ))}
            </div>

            <Button
              secondary
              onClick={() => setSelectedOrder(null)}
              className="w-full text-[12px] py-2.5"
            >
              Cerrar
            </Button>
          </div>
        </div>
      )}

      {/* MODAL DE EDICIÓN DE PEDIDO (Solo estado CARGADO) */}
      {editingOrder && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <form
            onSubmit={handleSaveEdit}
            className="bg-white rounded-[22px] max-w-lg w-full p-6 shadow-xl border border-[#ececf2] space-y-4"
          >
            <div className="flex items-center justify-between pb-3 border-b border-[#f0f0f4]">
              <div>
                <span className="text-[10px] text-[#39855b] uppercase font-bold bg-[#dff8e9] px-2 py-0.5 rounded-md">
                  Estado: CARGADO (Modificable)
                </span>
                <h3 className="text-base font-bold text-[#2b2b3a] mt-1">
                  Modificar Ítems del Pedido #{editingOrder.id}
                </h3>
              </div>
              <button
                type="button"
                onClick={() => setEditingOrder(null)}
                className="w-7 h-7 rounded-full bg-slate-100 text-[#6b6b80] hover:text-[#2b2b3a] flex items-center justify-center"
              >
                ✕
              </button>
            </div>

            <div className="space-y-3">
              <label className="block text-[11px] font-semibold text-[#2b2b3a]">
                Detalle de productos en el pedido:
              </label>
              <div className="space-y-2 max-h-52 overflow-y-auto pr-1">
                {editingOrder.detalles.map((d, idx) => (
                  <div key={idx} className="p-3 rounded-[12px] bg-[#fafafd] border border-[#ececf2] flex items-center justify-between text-[12px]">
                    <div>
                      <strong className="text-[#2b2b3a] block">{d.producto}</strong>
                      <span className="text-[11px] text-[#6b6b80]">
                        Precio unitario: ${d.precio.toLocaleString('es-AR')}
                      </span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-[11px] text-[#aaa9ba]">Bultos:</span>
                      <input
                        type="number"
                        min="1"
                        max="99"
                        value={d.cantidad}
                        onChange={(e) => {
                          const newQ = parseInt(e.target.value) || 1;
                          const updatedDetalles = editingOrder.detalles.map((item, i) =>
                            i === idx
                              ? { ...item, cantidad: newQ, subtotal: newQ * item.precio }
                              : item
                          );
                          const newSubtotal = updatedDetalles.reduce((acc, it) => acc + it.subtotal, 0);
                          setEditingOrder({
                            ...editingOrder,
                            detalles: updatedDetalles,
                            subtotal: newSubtotal,
                            montoTotal: newSubtotal
                          });
                        }}
                        className="w-14 border border-[#ececf2] rounded-[8px] px-2 py-1 text-center font-bold text-[12px] bg-white focus:outline-none focus:border-[#e86bc4]"
                      />
                    </div>
                  </div>
                ))}
              </div>

              <div className="p-3 bg-[#f8f9fc] rounded-[12px] text-[12px] flex justify-between items-center">
                <span className="text-[#6b6b80]">Nuevo total del pedido:</span>
                <strong className="text-base font-bold text-[#e86bc4]">
                  ${editingOrder.montoTotal.toLocaleString('es-AR')}
                </strong>
              </div>
            </div>

            <div className="flex items-center gap-2 pt-2">
              <Button
                type="button"
                secondary
                onClick={() => setEditingOrder(null)}
                className="w-1/2 text-[12px] py-2.5"
              >
                Cancelar
              </Button>
              <Button
                type="submit"
                className="w-1/2 text-[12px] py-2.5"
              >
                Guardar cambios
              </Button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
