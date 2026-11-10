import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { usePagos } from '../../hooks/usePagos';

export default function Pagos() {
  const {
    activeTab,
    setActiveTab,
    loading,
    isModalOpen,
    setIsModalOpen,
    isRechazarModalOpen,
    setIsRechazarModalOpen,
    selectedPagoParaRechazo,
    motivoRechazo,
    setMotivoRechazo,
    transferenciasPendientes,
    handleAprobarTransferencia,
    handleAbrirRechazo,
    handleConfirmarRechazo,
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
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-primary">
            Tesorería y Cobranzas
          </span>
          <h2 className="text-xl font-bold text-ink mt-0.5">
            Registro y Control de Pagos
          </h2>
          <p className="text-[12px] text-muted mt-0.5">
            Conciliación de Mercado Pago QR, transferencias bancarias manuales y rendiciones de distribuidores.
          </p>
        </div>

        {/* Tabs de Navegación */}
        <div className="flex items-center gap-2 bg-surface-soft p-1.5 rounded-[14px] border border-border-subtle self-start sm:self-auto">
          <button
            type="button"
            onClick={() => setActiveTab('todos')}
            className={`px-4 py-2 rounded-[10px] text-[12px] font-semibold transition-all ${
              activeTab === 'todos'
                ? 'bg-white text-ink shadow-sm'
                : 'text-muted hover:text-ink'
            }`}
          >
            Todos los Pagos
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('transferencias')}
            className={`px-4 py-2 rounded-[10px] text-[12px] font-semibold transition-all flex items-center gap-2 ${
              activeTab === 'transferencias'
                ? 'bg-white text-ink shadow-sm'
                : 'text-muted hover:text-ink'
            }`}
          >
            <span>Transferencias Pendientes</span>
            {transferenciasPendientes.length > 0 && (
              <span className="bg-warning-alt-bg text-warning-text px-2 py-0.5 rounded-full text-[10px] font-bold">
                {transferenciasPendientes.length}
              </span>
            )}
          </button>
        </div>
      </div>

      {activeTab === 'todos' ? (
        <>
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
                <option value="Mercado Pago">Mercado Pago QR</option>
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
                Historial General de Pagos
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
                            p.estado === 'Acreditado' || p.estado === 'Aprobado'
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
        </>
      ) : (
        /* TAB: Transferencias Bancarias Pendientes de Validación */
        <div className="bg-white rounded-[20px] p-6 border border-border-subtle shadow-sm space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-divider">
            <div>
              <h3 className="text-[16px] font-bold text-ink flex items-center gap-2">
                <span>Transferencias Pendientes de Cotejo Bancario</span>
                <span className="w-2 h-2 rounded-full bg-warning animate-pulse" />
              </h3>
              <p className="text-[12px] text-muted">
                Verificá en la cuenta bancaria de la empresa el ingreso de fondos antes de aprobar. Al aprobar, el sistema emitirá la <strong>Factura A</strong> y descontará el <strong>stock</strong> automáticamente.
              </p>
            </div>
            {loading && (
              <span className="text-[12px] text-primary animate-pulse font-semibold">
                Sincronizando...
              </span>
            )}
          </div>

          {transferenciasPendientes.length === 0 ? (
            <div className="py-12 text-center text-muted">
              <Icon name="check" className="w-12 h-12 text-success-icon mx-auto mb-2 opacity-50" />
              <p className="font-semibold text-[14px]">No hay transferencias pendientes de verificación</p>
              <p className="text-[12px]">Todos los pagos por transferencia informados han sido cotejados.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-border-subtle text-[10px] uppercase font-bold text-muted-subtle tracking-wider">
                    <th className="pb-3 px-3">ID Pago</th>
                    <th className="pb-3 px-3">N° Pedido</th>
                    <th className="pb-3 px-3">Comercio</th>
                    <th className="pb-3 px-3">Monto</th>
                    <th className="pb-3 px-3">Banco Emisor</th>
                    <th className="pb-3 px-3">N° Comprobante</th>
                    <th className="pb-3 px-3">Fecha Declarada</th>
                    <th className="pb-3 px-3 text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-divider text-[12px]">
                  {transferenciasPendientes.map((trf) => (
                    <tr key={trf.id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="py-3 px-3 font-semibold text-ink">
                        PAG-{trf.id}
                      </td>
                      <td className="py-3 px-3 font-bold text-secondary">
                        #{trf.pedidoId}
                      </td>
                      <td className="py-3 px-3 font-medium text-ink">
                        {trf.comercio || 'Minorista Registrado'}
                      </td>
                      <td className="py-3 px-3 font-bold text-ink">
                        ${typeof trf.monto === 'number' ? trf.monto.toLocaleString('es-AR') : trf.monto}
                      </td>
                      <td className="py-3 px-3 text-muted">
                        {trf.bancoEmisor || 'Transferencia CBU'}
                      </td>
                      <td className="py-3 px-3 font-mono font-semibold text-[#0f344d]">
                        {trf.numeroComprobante || 'S/N'}
                      </td>
                      <td className="py-3 px-3 text-muted text-[11px]">
                        {trf.fechaCreacion ? new Date(trf.fechaCreacion).toLocaleString('es-AR') : 'Reciente'}
                      </td>
                      <td className="py-3 px-3 text-right space-x-2">
                        <button
                          type="button"
                          onClick={() => handleAprobarTransferencia(trf.id)}
                          disabled={loading}
                          className="px-3 py-1.5 rounded-[8px] bg-success-bg text-success-icon hover:bg-[#c2f0d4] text-[11px] font-bold cursor-pointer transition-colors inline-flex items-center gap-1"
                        >
                          <Icon name="check" className="w-3.5 h-3.5" />
                          <span>Aprobar</span>
                        </button>
                        <button
                          type="button"
                          onClick={() => handleAbrirRechazo(trf)}
                          disabled={loading}
                          className="px-3 py-1.5 rounded-[8px] bg-danger-bg text-danger hover:bg-[#ffd1d9] text-[11px] font-bold cursor-pointer transition-colors inline-flex items-center gap-1"
                        >
                          <Icon name="x" className="w-3.5 h-3.5" />
                          <span>Rechazar</span>
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Modal Registrar Pago Manual */}
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
                <option>Mercado Pago QR</option>
                <option>Cheque 30 días</option>
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

      {/* Modal Rechazar Transferencia */}
      <Modal
        isOpen={isRechazarModalOpen}
        onClose={() => setIsRechazarModalOpen(false)}
        tag="RECHAZO DE PAGO"
        title={`Rechazar Pago #${selectedPagoParaRechazo?.id}`}
      >
        <form onSubmit={handleConfirmarRechazo} className="space-y-4">
          <p className="text-[12px] text-muted">
            Indicá el motivo por el cual no se pudo conciliar la transferencia (ej: importe no acreditado, comprobante ilegible, titular no coincide). El pedido permanecerá sin facturar.
          </p>

          <div>
            <label className="block text-[11px] font-semibold text-ink mb-1">
              Motivo del Rechazo *
            </label>
            <textarea
              required
              rows={3}
              value={motivoRechazo}
              onChange={(e) => setMotivoRechazo(e.target.value)}
              placeholder="Ej: No se visualiza el ingreso en la cuenta bancaria del Galicia al cierre bancario."
              className="w-full border border-border-subtle rounded-[12px] p-2.5 text-[12px] text-ink focus:outline-none focus:border-danger"
            />
          </div>

          <div className="pt-2 flex justify-end gap-2.5">
            <Button secondary type="button" onClick={() => setIsRechazarModalOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" className="bg-danger hover:bg-danger-hover text-white">
              Confirmar Rechazo
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
