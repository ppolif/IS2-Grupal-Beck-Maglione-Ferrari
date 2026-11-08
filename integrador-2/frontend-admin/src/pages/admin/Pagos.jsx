import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { usePagos } from '../../hooks/usePagos';

export default function Pagos() {
  const {
    isModalOpen,
    setIsModalOpen,
    medio,
    setMedio,
    formData,
    setFormData,
    handleRegisterPayment,
    filtered
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
          Registrá los pagos recibidos vía transferencia, cheques o rendiciones en efectivo de distribuidores.
        </p>
      </div>

      {/* Filterbar matching prototype filterbar */}
      <div className="bg-white rounded-[18px] p-4 border border-border-subtle shadow-sm flex flex-wrap items-center gap-3">
        <span className="border border-border-subtle px-3.5 py-2 rounded-[10px] text-[11px] text-muted bg-surface-soft">
          Período: <strong className="text-ink">Junio 2025</strong>
        </span>

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
            <option value="Cheque">Cheque</option>
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
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                <th className="pb-3 px-3">Nº Pago</th>
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
                <tr key={p.id} className="hover:bg-slate-50/70 transition-colors">
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
                    <span
                      className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full ${
                        p.estado === 'Acreditado'
                          ? 'bg-success-bg text-success-icon'
                          : p.estado.includes('Cobrado')
                          ? 'bg-info-bg text-info-text'
                          : 'bg-warning-alt-bg text-warning-text'
                      }`}
                    >
                      {p.estado}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal Registrar Pago */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        tag="COBRANZAS"
        title="Registrar Nuevo Pago"
      >
        <form onSubmit={handleRegisterPayment} className="space-y-4">
          <div>
            <label className="block text-[11px] font-semibold text-ink mb-1">
              Nº de Pedido a Imputar *
            </label>
            <input
              type="text"
              required
              value={formData.pedidoId}
              onChange={(e) => setFormData({ ...formData, pedidoId: e.target.value })}
              className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink focus:outline-none focus:border-primary"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-ink mb-1">
              Nombre del Comercio / Cliente *
            </label>
            <input
              type="text"
              required
              value={formData.comercio}
              onChange={(e) => setFormData({ ...formData, comercio: e.target.value })}
              className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink focus:outline-none focus:border-primary"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Monto cobrado *
              </label>
              <input
                type="text"
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
                <option>Transferencia Bancaria</option>
                <option>Efectivo</option>
                <option>Cheque 30 días</option>
                <option>Depósito en cuenta</option>
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
      </Modal>
    </div>
  );
}
