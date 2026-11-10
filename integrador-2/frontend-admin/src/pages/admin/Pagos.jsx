import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { usePagos } from '../../hooks/usePagos';

export default function Pagos() {
  const {
    facturasPendientes,
    loading,
    isModalOpen,
    setIsModalOpen,
    medio,
    setMedio,
    formData,
    setFormData,
    handleRegisterPayment,
    filtered,
  } = usePagos();

  return (
    <div className="space-y-6">
      {/* Title */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-primary">
          Tesorería y Cobranzas
        </span>
        <h2 className="text-xl font-bold text-ink mt-0.5">
          Registro y Control de Pagos
        </h2>
        <p className="text-[12px] text-muted mt-0.5">
          Registrá los pagos recibidos vía transferencia, billetera virtual o rendiciones en efectivo de distribuidores.
        </p>
      </div>

      {/* Filterbar */}
      <div className="bg-white rounded-[18px] p-4 border border-border-subtle shadow-sm flex flex-wrap items-center gap-3">
        <span className="border border-border-subtle px-3.5 py-2 rounded-[10px] text-[11px] text-muted bg-surface-soft">
          Departamento: <strong className="text-ink">Gran Mendoza</strong>
        </span>

        <div className="flex items-center gap-1.5 border border-border-subtle px-3 py-1.5 rounded-[10px] bg-surface-soft text-[11px] text-muted">
          <span>Medio de pago:</span>
          <select
            value={medio}
            onChange={(e) => setMedio(e.target.value)}
            className="bg-transparent text-ink font-semibold outline-none cursor-pointer"
          >
            <option value="Todos">Todos</option>
            <option value="Transferencia">Transferencia</option>
            <option value="Efectivo">Efectivo</option>
            <option value="Billetera">Billetera Virtual</option>
          </select>
        </div>

        <div className="ml-auto">
          <Button onClick={() => setIsModalOpen(true)}>
            <Icon name="plus" className="w-4 h-4" />
            <span>Registrar pago</span>
          </Button>
        </div>
      </div>

      {/* Table Module */}
      <div className="bg-white rounded-[20px] p-6 border border-border-subtle shadow-sm">
        <div className="flex items-center justify-between pb-3 mb-3 border-b border-divider">
          <h3 className="text-[15px] font-bold text-ink">
            Estado de Pagos Registrados
          </h3>
          <span className="text-[11px] text-muted">
            Total registros: <strong>{filtered.length}</strong>
          </span>
        </div>

        <div className="overflow-x-auto">
          {loading ? (
            <div className="py-10 text-center text-[12px] text-muted">
              Cargando pagos registrados...
            </div>
          ) : filtered.length === 0 ? (
            <div className="py-10 text-center text-[13px] text-muted">
              No hay pagos registrados para mostrar en esta sección.
            </div>
          ) : (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                  <th className="pb-3 px-3">Comprobante</th>
                  <th className="pb-3 px-3">Pedido</th>
                  <th className="pb-3 px-3">Comercio</th>
                  <th className="pb-3 px-3">Monto</th>
                  <th className="pb-3 px-3">Medio de Pago</th>
                  <th className="pb-3 px-3">Fecha</th>
                  <th className="pb-3 px-3">Estado</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-divider text-[12px]">
                {filtered.map((p) => (
                  <tr key={p.rawId || p.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="py-3 px-3 font-semibold text-ink">
                      {p.id}
                    </td>
                    <td className="py-3 px-3 font-bold text-secondary">
                      #{p.pedidoId}
                    </td>
                    <td className="py-3 px-3 font-medium text-ink">
                      {p.comercio}
                    </td>
                    <td className="py-3 px-3 font-bold text-ink">
                      {p.monto}
                    </td>
                    <td className="py-3 px-3 text-muted">{p.medioPago}</td>
                    <td className="py-3 px-3 text-muted text-[11px]">{p.fecha}</td>
                    <td className="py-3 px-3">
                      <span className="text-[10px] font-semibold px-2.5 py-0.5 rounded-full bg-success-bg text-success-icon">
                        {p.estado}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Modal Registrar Pago */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        tag="COBRANZAS"
        title="Registrar Nuevo Pago"
      >
        {facturasPendientes.length === 0 ? (
          <div className="space-y-4 text-[12px] text-muted">
            <p>No hay facturas con saldo pendiente de pago en este momento.</p>
            <div className="flex justify-end">
              <Button secondary type="button" onClick={() => setIsModalOpen(false)}>
                Cerrar
              </Button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleRegisterPayment} className="space-y-4">
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Factura / Pedido a Imputar *
              </label>
              <select
                required
                value={formData.facturaId}
                onChange={(e) => {
                  const sel = facturasPendientes.find((f) => f.id === e.target.value);
                  setFormData({
                    ...formData,
                    facturaId: e.target.value,
                    monto: sel ? String(sel.saldo) : formData.monto,
                  });
                }}
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              >
                {facturasPendientes.map((f) => (
                  <option key={f.id} value={f.id}>
                    {f.numeroFactura} · Pedido #{f.numeroPedido} · {f.comercio} (Saldo: ${f.saldo.toLocaleString('es-AR')})
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-[11px] font-semibold text-ink mb-1">
                  Monto cobrado ($) *
                </label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={formData.monto}
                  onChange={(e) => setFormData({ ...formData, monto: e.target.value })}
                  className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink focus:outline-none focus:border-primary"
                />
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-ink mb-1">
                  Medio de Pago *
                </label>
                <select
                  value={formData.medioPago}
                  onChange={(e) => setFormData({ ...formData, medioPago: e.target.value })}
                  className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
                >
                  <option value="Transferencia Bancaria">Transferencia Bancaria</option>
                  <option value="Efectivo">Efectivo</option>
                  <option value="Billetera Virtual">Billetera Virtual</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Nº de Comprobante / Recibo
              </label>
              <input
                type="text"
                value={formData.comprobante}
                onChange={(e) => setFormData({ ...formData, comprobante: e.target.value })}
                placeholder="Ej: TRF-909988"
                className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink focus:outline-none focus:border-primary"
              />
            </div>

            <div className="pt-2 flex justify-end gap-2.5">
              <Button secondary type="button" onClick={() => setIsModalOpen(false)}>
                Cancelar
              </Button>
              <Button type="submit">Guardar pago</Button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
}
