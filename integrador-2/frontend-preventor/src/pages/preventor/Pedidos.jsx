/**
 * Listado y Seguimiento de Pedidos para el Preventista.
 * Conectado con /api/v1/pedidos y /api/v1/pedidos/{id}/pagos/qr/iniciar.
 */
import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Chip from '../../components/Chip';
import Modal from '../../components/Modal';
import MercadoPagoQrPanel from '../../components/MercadoPagoQrPanel';
import { pedidoService } from '../../services/pedidoService';

export default function Pedidos() {
  const [orders, setOrders] = useState([]);
  const [filterState, setFilterState] = useState('TODOS');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [qrOrder, setQrOrder] = useState(null);
  const [qrPagoAprobado, setQrPagoAprobado] = useState(false);
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

  const cargarPedidos = useCallback(async () => {
    try {
      setCargando(true);
      setError(null);
      const data = await pedidoService.listarPedidos({ size: 100 });
      const list = Array.isArray(data) ? data : (data?.content || []);
      setOrders(list);
    } catch (err) {
      console.error('Error al listar pedidos:', err);
      setError(
        err.code === 'CAMBIO_CLAVE_REQUERIDO' || err.status === 403
          ? 'Cambie su clave para acceder'
          : 'No se pudieron cargar los pedidos desde el servidor.'
      );
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    cargarPedidos();
  }, [cargarPedidos]);

  const abrirModalQr = (order) => {
    setQrPagoAprobado(false);
    setQrOrder(order);
  };

  const cerrarModalQr = () => {
    setQrOrder(null);
    setQrPagoAprobado(false);
  };

  const handlePagoAprobado = (orderId) => {
    setQrPagoAprobado(true);
    setOrders((prev) =>
      prev.map((o) => (o.id === orderId ? { ...o, estado: 'FACTURADO' } : o))
    );
    setSelectedOrder((prev) =>
      prev && prev.id === orderId ? { ...prev, estado: 'FACTURADO' } : prev
    );
  };

  const filteredOrders = orders.filter((o) => {
    const matchesFilter = filterState === 'TODOS' || o.estado === filterState;
    const id = o.id || '';
    const numStr = o.numeroPedido ? `DM-${o.numeroPedido}` : '';
    const comercio = o.puntoDeVenta?.nombreComercio || '';
    const term = searchTerm.toLowerCase();

    return (
      matchesFilter &&
      (id.toLowerCase().includes(term) ||
        numStr.toLowerCase().includes(term) ||
        comercio.toLowerCase().includes(term))
    );
  });

  const calcularTotalOrden = (order) =>
    order?.total ??
    order?.detalles?.reduce((acc, d) => acc + (d.subtotal || 0), 0) ??
    0;

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
            Pedidos registrados y su evolución en depósito, cobro QR y reparto.
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
          placeholder="Buscar por número de pedido o nombre de comercio..."
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
        error === 'Cambie su clave para acceder' ? (
          <div className="max-w-md mx-auto my-8 p-8 bg-white rounded-[24px] border border-[#ececf2] text-center space-y-4 shadow-sm">
            <div className="w-16 h-16 rounded-full bg-[#eefaff] text-[#247f99] flex items-center justify-center mx-auto shadow-xs">
              <Icon name="key" className="w-8 h-8" />
            </div>
            <h2 className="text-xl font-bold text-[#2b2b3a]">Cambie su clave para acceder</h2>
            <p className="text-[13px] text-[#6b6b80] leading-relaxed">
              Su usuario posee una contraseña temporal por defecto. Por razones de seguridad, debe actualizar su clave antes de operar en esta sección.
            </p>
            <Link
              to="/cambiar-password"
              className="inline-flex items-center justify-center w-full py-3 px-4 rounded-[14px] bg-[#247f99] text-white font-bold text-[13px] hover:bg-[#1c667b] transition-colors shadow-sm"
            >
              Cambiar contraseña ahora →
            </Link>
          </div>
        ) : (
          <div className="p-4 bg-[#ffe8ed] border border-[#ffccd5] rounded-[16px] text-[12px] text-[#e5484d]">
            {error}
          </div>
        )
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
                const total = calcularTotalOrden(order);
                const formattedTotal = new Intl.NumberFormat('es-AR', {
                  style: 'currency',
                  currency: 'ARS',
                  maximumFractionDigits: 0,
                }).format(total);

                const pdv = order.puntoDeVenta;
                const dir = pdv?.direccion
                  ? `${pdv.direccion.calle || ''} ${pdv.direccion.numeracion || pdv.direccion.numero || ''}`.trim() || 'Mendoza'
                  : 'Mendoza';
                const fechaTexto = order.fechaHoraCarga
                  ? new Date(order.fechaHoraCarga).toLocaleString('es-AR')
                  : order.fechaCreacion || order.fecha || 'Reciente';

                return (
                  <div
                    key={order.id}
                    className="bg-white rounded-[18px] p-4 sm:p-5 border border-[#ececf2] shadow-sm hover:border-[#d9d9e6] transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-4"
                  >
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="font-mono text-[12px] font-bold text-[#e86bc4]">
                          {order.numeroPedido ? `DM-${order.numeroPedido}` : `#${order.id?.slice(-6) || order.id}`}
                        </span>
                        <Chip variant={getStatusColor(order.estado)}>
                          {order.estado}
                        </Chip>
                      </div>
                      <h3 className="font-bold text-[14px] text-[#2b2b3a]">
                        {pdv?.nombreComercio || 'Comercio Minorista'}
                      </h3>
                      <p className="text-[11px] text-[#6b6b80]">
                        {dir} · Fecha: {fechaTexto}
                      </p>
                    </div>

                    <div className="flex items-center justify-between sm:justify-end gap-3 pt-2 sm:pt-0 border-t sm:border-0 border-[#f0f0f4]">
                      <div className="text-left sm:text-right mr-1">
                        <span className="text-[10px] text-[#aaa9ba] block uppercase">Total</span>
                        <strong className="text-[15px] font-bold text-[#2b2b3a] font-mono">
                          {formattedTotal}
                        </strong>
                      </div>

                      <div className="flex items-center gap-2">
                        {order.estado === 'CARGADO' && order.tipoPago === 'BILLETERA_VIRTUAL' && (
                          <button
                            type="button"
                            onClick={() => abrirModalQr(order)}
                            className="px-3 py-2 rounded-[10px] bg-[#eaf8fc] hover:bg-[#d7f1fa] text-[#009ee3] border border-[#cdecfb] text-[11px] font-bold flex items-center gap-1.5 cursor-pointer transition-colors"
                            title="Generar o mostrar QR de Mercado Pago"
                          >
                            <Icon name="qr" className="w-4 h-4" />
                            <span>Cobrar QR</span>
                          </button>
                        )}

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
                  </div>
                );
              })}
            </div>
          )}
        </>
      )}

      {/* Modal Detalle de Pedido (sin acceso a facturas para preventor) */}
      {selectedOrder && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-[24px] max-w-lg w-full p-6 shadow-xl border border-[#ececf2] space-y-4 max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-start">
              <div>
                <span className="text-[10px] font-bold text-[#e86bc4] uppercase tracking-wider">
                  Detalle de Pedido
                </span>
                <h2 className="text-xl font-bold text-[#2b2b3a]">
                  {selectedOrder.numeroPedido ? `DM-${selectedOrder.numeroPedido}` : `#${selectedOrder.id}`}
                </h2>
              </div>
              <button
                type="button"
                onClick={() => setSelectedOrder(null)}
                className="text-[#aaa9ba] hover:text-[#2b2b3a] p-1 cursor-pointer font-bold"
                aria-label="Cerrar detalle"
              >
                <Icon name="x" className="w-5 h-5" />
              </button>
            </div>

            <div className="bg-[#f8f9fc] rounded-[14px] p-3 text-[12px] space-y-1">
              <p><strong>Comercio:</strong> {selectedOrder.puntoDeVenta?.nombreComercio}</p>
              <p className="flex items-center gap-2">
                <strong>Estado Actual:</strong>{' '}
                <Chip variant={getStatusColor(selectedOrder.estado)}>{selectedOrder.estado}</Chip>
              </p>
              <p>
                <strong>Total:</strong>{' '}
                <span className="font-mono font-bold">
                  ${calcularTotalOrden(selectedOrder).toLocaleString('es-AR')}
                </span>
              </p>
            </div>

            <div className="space-y-2">
              <h4 className="text-[12px] font-bold text-[#2b2b3a]">Productos Solicitados:</h4>
              <div className="divide-y divide-[#ececf2]">
                {selectedOrder.detalles?.map((det, idx) => (
                  <div key={idx} className="py-2 flex justify-between text-[12px]">
                    <span>{det.producto?.nombre || det.productoNombre || 'Producto'} (x{det.cantidad})</span>
                    <span className="font-mono font-bold">${det.subtotal?.toLocaleString('es-AR')}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="pt-2 flex flex-col sm:flex-row gap-2">
              {selectedOrder.estado === 'CARGADO' && selectedOrder.tipoPago === 'BILLETERA_VIRTUAL' && (
                <button
                  type="button"
                  onClick={() => {
                    const orden = selectedOrder;
                    setSelectedOrder(null);
                    abrirModalQr(orden);
                  }}
                  className="flex-1 py-2.5 px-4 rounded-[12px] bg-[#009ee3] hover:bg-[#0089c7] text-white text-[12px] font-bold flex items-center justify-center gap-1.5 cursor-pointer transition-colors"
                >
                  <Icon name="qr" className="w-4 h-4" />
                  <span>Generar QR Mercado Pago</span>
                </button>
              )}

              <Button
                onClick={() => setSelectedOrder(null)}
                className="flex-1 justify-center text-[12px] cursor-pointer"
              >
                Cerrar
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Modal Cobro QR Mercado Pago */}
      <Modal
        isOpen={Boolean(qrOrder)}
        onClose={cerrarModalQr}
        tag="MERCADO PAGO QR"
        title={
          qrOrder
            ? `Cobrar ${qrOrder.numeroPedido ? `DM-${qrOrder.numeroPedido}` : 'Pedido'}`
            : ''
        }
        maxWidth="max-w-xl"
      >
        {qrOrder && (
          qrPagoAprobado ? (
            <div className="p-5 bg-[#dff8e9] rounded-[16px] text-center space-y-3 text-[#235839]">
              <div className="w-12 h-12 rounded-full bg-white text-[#39855b] flex items-center justify-center mx-auto shadow-sm">
                <Icon name="check" className="w-6 h-6" />
              </div>
              <h4 className="text-lg font-bold text-[#1f4e33]">
                ¡Pago Acreditado con Éxito!
              </h4>
              <p className="text-[12px] text-[#2e744c]">
                El pedido para <strong>{qrOrder.puntoDeVenta?.nombreComercio || 'el comercio'}</strong> fue confirmado por Mercado Pago y pasó a estado <strong>FACTURADO</strong>.
              </p>
              <Button onClick={cerrarModalQr} className="w-full justify-center text-[12px] cursor-pointer">
                Listo
              </Button>
            </div>
          ) : (
            <MercadoPagoQrPanel
              pedidoId={qrOrder.id}
              numeroPedido={qrOrder.numeroPedido}
              nombreComercio={qrOrder.puntoDeVenta?.nombreComercio}
              monto={calcularTotalOrden(qrOrder)}
              onPagoAprobado={() => handlePagoAprobado(qrOrder.id)}
              onPosponerPago={cerrarModalQr}
            />
          )
        )}
      </Modal>
    </div>
  );
}
