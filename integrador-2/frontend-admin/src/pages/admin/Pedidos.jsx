import { useState } from 'react';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { ordersMock } from '../../mocks/orders';

export default function Pedidos() {
  const [orders, setOrders] = useState(ordersMock);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);

  const handleOpenDetail = (order) => {
    setSelectedOrder(order);
    setIsDetailModalOpen(true);
  };

  const handleInvoiceOrder = (orderId) => {
    setOrders((prev) =>
      prev.map((o) =>
        o.id === orderId ? { ...o, facturado: true, estado: 'Facturado' } : o
      )
    );
    if (selectedOrder && selectedOrder.id === orderId) {
      setSelectedOrder({ ...selectedOrder, facturado: true, estado: 'Facturado' });
    }
    alert(`Pedido #${orderId} FACTURADO y preparado para despacho.`);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Recepción y Facturación
          </span>
          <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
            Pedidos Recepcionados
          </h2>
          <p className="text-[12px] text-[#6b6b80] mt-0.5">
            Revisá los pedidos enviados por los preventores, verificá mercadería y emití factura.
          </p>
        </div>
      </div>

      {/* Orders Table */}
      <div className="bg-white rounded-[20px] p-5 border border-[#ececf2] shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-[#ececf2] text-[10px] uppercase font-bold text-[#aaa9b8] tracking-wider">
                <th className="pb-3 px-3">Pedido</th>
                <th className="pb-3 px-3">Comercio</th>
                <th className="pb-3 px-3">Preventor</th>
                <th className="pb-3 px-3">Monto Total</th>
                <th className="pb-3 px-3">Estado</th>
                <th className="pb-3 px-3">Facturación</th>
                <th className="pb-3 px-3 text-right">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#f0f0f4] text-[12px]">
              {orders.map((o) => (
                <tr key={o.id} className="hover:bg-slate-50/70 transition-colors">
                  <td className="py-3.5 px-3 font-bold text-[#2b2b3a]">
                    #{o.id}
                  </td>
                  <td className="py-3.5 px-3 font-medium text-[#2b2b3a]">
                    {o.comercio}
                  </td>
                  <td className="py-3.5 px-3 text-[#6b6b80]">{o.preventor}</td>
                  <td className="py-3.5 px-3 font-bold text-[#2b2b3a]">
                    {o.monto}
                  </td>
                  <td className="py-3.5 px-3">
                    <span
                      className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full ${
                        o.estado === 'Facturado'
                          ? 'bg-[#dff8e9] text-[#39855b]'
                          : o.estado === 'En camino'
                          ? 'bg-[#d9f7ff] text-[#247f99]'
                          : o.estado === 'Entregado'
                          ? 'bg-[#f0f2f7] text-[#555]'
                          : 'bg-[#fff2d3] text-[#986b16]'
                      }`}
                    >
                      {o.estado}
                    </span>
                  </td>
                  <td className="py-3.5 px-3">
                    {o.facturado ? (
                      <span className="text-[11px] font-semibold text-[#39855b] flex items-center gap-1">
                        <Icon name="check" className="w-3.5 h-3.5" />
                        Facturado
                      </span>
                    ) : (
                      <span className="text-[11px] text-[#ff8fa3] font-semibold">
                        Sin facturar
                      </span>
                    )}
                  </td>
                  <td className="py-3.5 px-3 text-right space-x-2">
                    <button
                      onClick={() => handleOpenDetail(o)}
                      className="text-[#3fb8d9] hover:underline font-semibold text-[11px]"
                    >
                      Detalle
                    </button>
                    {!o.facturado && (
                      <button
                        onClick={() => handleInvoiceOrder(o.id)}
                        className="bg-[#e86bc4] text-white px-2.5 py-1 rounded-[8px] text-[10px] font-semibold hover:bg-[#d456b0] transition-colors"
                      >
                        Facturar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Order Detail & Invoice Modal */}
      <Modal
        isOpen={isDetailModalOpen}
        onClose={() => setIsDetailModalOpen(false)}
        tag="FACTURACIÓN"
        title={selectedOrder ? `Pedido #${selectedOrder.id}` : 'Detalle'}
        maxWidth="max-w-lg"
      >
        {selectedOrder && (
          <div className="space-y-4">
            <div className="p-3.5 rounded-[14px] bg-[#fafafd] border border-[#ececf2] text-[12px] space-y-1">
              <div className="flex justify-between">
                <span className="text-[#6b6b80]">Cliente / Comercio:</span>
                <strong className="text-[#2b2b3a]">{selectedOrder.comercio}</strong>
              </div>
              <div className="flex justify-between">
                <span className="text-[#6b6b80]">Preventor asignado:</span>
                <span className="text-[#2b2b3a]">{selectedOrder.preventor}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-[#6b6b80]">Fecha y hora:</span>
                <span className="text-[#2b2b3a]">{selectedOrder.fecha}</span>
              </div>
              <div className="flex justify-between pt-1 border-t border-[#f0f0f4]">
                <span className="text-[#6b6b80]">Estado:</span>
                <span className="font-semibold text-[#e86bc4]">{selectedOrder.estado}</span>
              </div>
            </div>

            <div>
              <h4 className="text-[12px] font-bold text-[#2b2b3a] mb-2 uppercase tracking-wider">
                Desglose de mercadería
              </h4>
              <div className="divide-y divide-[#f0f0f4] border border-[#ececf2] rounded-[12px] p-2 bg-white text-[12px]">
                {selectedOrder.items.map((item, idx) => (
                  <div key={idx} className="py-2 flex justify-between items-center px-1">
                    <div>
                      <strong className="block text-[#2b2b3a] font-medium">{item.nombre}</strong>
                      <span className="text-[10px] text-[#6b6b80]">Cantidad: {item.cant} bultos</span>
                    </div>
                    <span className="font-bold text-[#2b2b3a]">{item.sub}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="flex justify-between items-baseline pt-2 border-t border-[#ececf2] text-[14px]">
              <span className="font-bold text-[#2b2b3a]">Monto a facturar:</span>
              <strong className="text-xl font-bold text-[#e86bc4]">
                {selectedOrder.monto}
              </strong>
            </div>

            <div className="pt-2 flex justify-end gap-2.5">
              <Button secondary type="button" onClick={() => setIsDetailModalOpen(false)}>
                Cerrar
              </Button>
              {!selectedOrder.facturado ? (
                <Button onClick={() => handleInvoiceOrder(selectedOrder.id)}>
                  Facturar pedido ahora
                </Button>
              ) : (
                <Button secondary onClick={() => alert('Comprobante PDF generado (Simulado)')}>
                  Descargar Factura A
                </Button>
              )}
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}

