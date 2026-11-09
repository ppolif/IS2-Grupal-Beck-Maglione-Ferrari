import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { usePedidos } from '../../hooks/usePedidos';

export default function Pedidos() {
  const {
    orders,
    loading,
    selectedOrder,
    isDetailModalOpen,
    actionLoading,
    setIsDetailModalOpen,
    handleOpenDetail,
    handleInvoiceOrder,
    handleReenviarFactura,
  } = usePedidos();

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-primary">
            Recepción y Facturación
          </span>
          <h2 className="text-xl font-bold text-ink mt-0.5">
            Pedidos Recepcionados
          </h2>
          <p className="text-[12px] text-muted mt-0.5">
            Revisá los pedidos enviados por los preventores, verificá mercadería y emití factura.
          </p>
        </div>
      </div>

      {/* Orders Table */}
      <div className="bg-white rounded-[20px] p-5 border border-border-subtle shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                <th className="pb-3 px-3">Pedido</th>
                <th className="pb-3 px-3">Comercio</th>
                <th className="pb-3 px-3">Preventor</th>
                <th className="pb-3 px-3">Monto Total</th>
                <th className="pb-3 px-3">Estado</th>
                <th className="pb-3 px-3">Facturación</th>
                <th className="pb-3 px-3 text-right">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-divider text-[12px]">
              {orders.map((o) => (
                <tr key={o.id} className="hover:bg-slate-50/70 transition-colors">
                  <td className="py-3.5 px-3 font-bold text-ink">
                    #{o.id}
                  </td>
                  <td className="py-3.5 px-3 font-medium text-ink">
                    {o.comercio}
                  </td>
                  <td className="py-3.5 px-3 text-muted">{o.preventor}</td>
                  <td className="py-3.5 px-3 font-bold text-ink">
                    {o.monto}
                  </td>
                  <td className="py-3.5 px-3">
                    <span
                      className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full ${
                        o.estado === 'Facturado'
                          ? 'bg-success-bg text-success-icon'
                          : o.estado === 'En camino'
                          ? 'bg-info-bg text-info-text'
                          : o.estado === 'Entregado'
                          ? 'bg-canvas text-muted'
                          : 'bg-warning-bg text-warning-text'
                      }`}
                    >
                      {o.estado}
                    </span>
                  </td>
                  <td className="py-3.5 px-3">
                    {o.facturado ? (
                      <span className="text-[11px] font-semibold text-success-icon flex items-center gap-1">
                        <Icon name="check" className="w-3.5 h-3.5" />
                        Facturado
                      </span>
                    ) : (
                      <span className="text-[11px] text-brand-error font-semibold">
                        Sin facturar
                      </span>
                    )}
                  </td>
                  <td className="py-3.5 px-3 text-right space-x-2">
                    <button
                      onClick={() => handleOpenDetail(o)}
                      className="text-secondary hover:underline font-semibold text-[11px] cursor-pointer"
                    >
                      Detalle
                    </button>
                    {!o.facturado && (
                      <button
                        onClick={() => handleInvoiceOrder(o.id)}
                        className="bg-primary text-white px-2.5 py-1 rounded-[8px] text-[10px] font-semibold hover:bg-primary/90 transition-colors cursor-pointer"
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
            <div className="p-3.5 rounded-[14px] bg-surface-note border border-border-subtle text-[12px] space-y-1">
              <div className="flex justify-between">
                <span className="text-muted">Cliente / Comercio:</span>
                <strong className="text-ink">{selectedOrder.comercio}</strong>
              </div>
              <div className="flex justify-between">
                <span className="text-muted">Preventor asignado:</span>
                <span className="text-ink">{selectedOrder.preventor}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted">Fecha y hora:</span>
                <span className="text-ink">{selectedOrder.fecha}</span>
              </div>
              <div className="flex justify-between pt-1 border-t border-divider">
                <span className="text-muted">Estado:</span>
                <span className="font-semibold text-primary">{selectedOrder.estado}</span>
              </div>
            </div>

            <div>
              <h4 className="text-[12px] font-bold text-ink mb-2 uppercase tracking-wider">
                Desglose de mercadería
              </h4>
              <div className="divide-y divide-divider border border-border-subtle rounded-[12px] p-2 bg-white text-[12px]">
                {selectedOrder.items.map((item, idx) => (
                  <div key={idx} className="py-2 flex justify-between items-center px-1">
                    <div>
                      <strong className="block text-ink font-medium">{item.nombre}</strong>
                      <span className="text-[10px] text-muted">Cantidad: {item.cant} bultos</span>
                    </div>
                    <span className="font-bold text-ink">{item.sub}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="flex justify-between items-baseline pt-2 border-t border-border-subtle text-[14px]">
              <span className="font-bold text-ink">Monto a facturar:</span>
              <strong className="text-xl font-bold text-primary">
                {selectedOrder.monto}
              </strong>
            </div>

            <div className="pt-2 flex justify-end gap-2.5">
              <Button secondary type="button" onClick={() => setIsDetailModalOpen(false)}>
                Cerrar
              </Button>
              {!selectedOrder.facturado ? (
                <Button disabled={actionLoading} onClick={() => handleInvoiceOrder(selectedOrder.id)}>
                  {actionLoading ? 'Facturando...' : 'Facturar pedido ahora'}
                </Button>
              ) : (
                <>
                  {selectedOrder.facturaId && (
                    <Button secondary onClick={() => handleReenviarFactura(selectedOrder.facturaId)}>
                      Reenviar Factura por Mail
                    </Button>
                  )}
                  <Button secondary onClick={() => alert('Factura emitida correctamente.')}>
                    Descargar Factura A
                  </Button>
                </>
              )}
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
