import { useState, useEffect, useCallback } from 'react';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { facturaService } from '../../services/facturaService';
import { pedidoService } from '../../services/pedidoService';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

export default function Facturas() {
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterState, setFilterState] = useState('Todas');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedInvoice, setSelectedInvoice] = useState(null);
  const [paymentModalInvoice, setPaymentModalInvoice] = useState(null);
  const [anularModalInvoice, setAnularModalInvoice] = useState(null);

  const [pagoMonto, setPagoMonto] = useState('');
  const [pagoMedio, setPagoMedio] = useState('Transferencia Bancaria');
  const [pagoComprobante, setPagoComprobante] = useState('');
  const [motivoAnulacion, setMotivoAnulacion] = useState('');

  const cargarFacturas = useCallback(async () => {
    setLoading(true);
    try {
      const [factData, pedData] = await Promise.all([
        facturaService.listar({ size: 100 }),
        pedidoService.listar({ size: 100 }).catch(() => ({ elementos: [] })),
      ]);

      const listaFact = factData.elementos || factData.content || (Array.isArray(factData) ? factData : []);
      const listaPed = pedData.elementos || pedData.content || (Array.isArray(pedData) ? pedData : []);
      const pedidosMap = {};
      listaPed.forEach((p) => {
        pedidosMap[p.id] = p;
      });

      const mapped = listaFact.map((f) => {
        const pedido = pedidosMap[f.pedidoId];
        const cliente =
          pedido?.puntoDeVenta?.nombreComercio ||
          (pedido?.puntoDeVenta?.minorista
            ? `${pedido.puntoDeVenta.minorista.nombre || ''} ${pedido.puntoDeVenta.minorista.apellido || ''}`.trim()
            : 'Cliente Mayorista');
        const cuit = pedido?.puntoDeVenta?.minorista?.numeroDocumento || '-';

        return {
          id: f.id,
          numeroFactura: f.numeroFactura ? `FA-${String(f.numeroFactura).padStart(8, '0')}` : f.id,
          cae: f.numeroFactura ? `74${String(f.numeroFactura).padStart(12, '0')}` : '-',
          fechaVencimientoCae: f.fechaFactura
            ? new Date(f.fechaFactura).toLocaleDateString('es-AR')
            : '-',
          pedidoId: pedido?.numeroPedido || f.pedidoId || '-',
          cliente,
          cuit,
          fechaEmision: f.fechaFactura ? new Date(f.fechaFactura).toLocaleString('es-AR') : '-',
          total: Number(f.total || 0),
          totalPagado: Number(f.totalPagado || 0),
          saldo: Number(f.saldo || 0),
          estado: f.estado || 'PENDIENTE_PAGO',
          items: (f.detalles || []).map((d) => ({
            producto: d.productoNombre || 'Producto',
            cantidad: d.cantidad || 1,
            precio: Number(d.precioUnitario || 0),
            subtotal: Number(d.subtotal || 0),
          })),
          pagos: (f.pagos || []).map((pg) => ({
            id: pg.id,
            fecha: pg.fechaPago ? new Date(pg.fechaPago).toLocaleString('es-AR') : '-',
            medio: pg.formaDePago?.tipoPago || 'Pago',
            comprobante: pg.referencia || 'S/N',
            monto: Number(pg.monto || 0),
          })),
        };
      });
      setInvoices(mapped);
    } catch (err) {
      console.error('Error al cargar facturas:', err);
      setInvoices([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarFacturas();
  }, [cargarFacturas]);

  const handleDownloadPDF = async (facturaId, numeroFactura) => {
    try {
      const token = localStorage.getItem('dm_admin_token');
      const res = await fetch(`${API_BASE_URL}/facturas/${facturaId}/pdf`, {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${numeroFactura}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert(`No se pudo descargar el PDF: ${err.message}`);
    }
  };

  const handleReenviarCorreo = async (facturaId, numeroFactura) => {
    try {
      await facturaService.reenviarCorreo(facturaId);
      alert(`Comprobante ${numeroFactura} reenviado exitosamente por correo electrónico.`);
    } catch (err) {
      alert(`Error al reenviar comprobante: ${err.message}`);
    }
  };

  const handleImputarPago = async (e) => {
    e.preventDefault();
    if (!paymentModalInvoice) return;
    const monto = parseFloat(pagoMonto) || 0;
    if (monto <= 0 || monto > paymentModalInvoice.saldo) {
      alert(
        `El monto debe ser mayor a 0 y menor o igual al saldo pendiente ($${paymentModalInvoice.saldo.toLocaleString('es-AR')}).`
      );
      return;
    }

    try {
      const formaDePagoId = await facturaService.obtenerOCrearFormaDePagoId(pagoMedio);
      await facturaService.registrarPago(paymentModalInvoice.id, {
        formaDePagoId,
        monto,
        referencia: pagoComprobante || 'S/N',
        observacion: pagoMedio,
      });
      alert(`Pago de $${monto.toLocaleString('es-AR')} imputado a la factura ${paymentModalInvoice.numeroFactura}.`);
      setPaymentModalInvoice(null);
      setPagoMonto('');
      setPagoComprobante('');
      await cargarFacturas();
    } catch (err) {
      alert(`Error al imputar cobro: ${err.message}`);
    }
  };

  const handleConfirmarAnulacion = async (e) => {
    e.preventDefault();
    if (!anularModalInvoice) return;
    try {
      await facturaService.anular(anularModalInvoice.id);
      alert(`Factura ${anularModalInvoice.numeroFactura} ANULADA con éxito.`);
      setAnularModalInvoice(null);
      setMotivoAnulacion('');
      await cargarFacturas();
    } catch (err) {
      alert(`Error al anular factura: ${err.message}`);
    }
  };

  const filtered = invoices.filter((inv) => {
    const matchesFilter =
      filterState === 'Todas' ||
      (filterState === 'Pendientes' && inv.estado === 'PENDIENTE_PAGO') ||
      (filterState === 'Pagadas' && inv.estado === 'PAGADA') ||
      (filterState === 'Anuladas' && inv.estado === 'ANULADA');
    const q = searchTerm.toLowerCase();
    const matchesSearch =
      String(inv.numeroFactura).toLowerCase().includes(q) ||
      String(inv.pedidoId).toLowerCase().includes(q) ||
      String(inv.cliente).toLowerCase().includes(q) ||
      String(inv.cuit).toLowerCase().includes(q);
    return matchesFilter && matchesSearch;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Administración y Finanzas
          </span>
          <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
            Gestión Centralizada de Facturación
          </h2>
          <p className="text-[12px] text-[#6b6b80] mt-0.5">
            Control de comprobantes electrónicos, imputación de pagos y anulación de facturas.
          </p>
        </div>
      </div>

      {/* Filterbar */}
      <div className="bg-white rounded-[18px] p-4 border border-[#ececf2] shadow-sm flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          {['Todas', 'Pendientes', 'Pagadas', 'Anuladas'].map((tab) => (
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
            placeholder="Buscar por N° factura, CUIT o cliente..."
            className="w-full bg-[#fafafd] border border-[#ececf2] rounded-[10px] pl-9 pr-3 py-1.5 text-[11px] text-[#2b2b3a] focus:bg-white focus:outline-none focus:border-[#e86bc4]"
          />
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-[20px] p-6 border border-[#ececf2] shadow-sm">
        <div className="flex items-center justify-between pb-3 mb-3 border-b border-[#f0f0f4]">
          <h3 className="text-[15px] font-bold text-[#2b2b3a]">
            Comprobantes Electrónicos Emitidos
          </h3>
          <span className="text-[11px] text-[#6b6b80]">
            Total registros: <strong>{filtered.length}</strong>
          </span>
        </div>

        <div className="overflow-x-auto">
          {loading ? (
            <div className="py-10 text-center text-[12px] text-[#6b6b80]">
              Cargando comprobantes emitidos...
            </div>
          ) : filtered.length === 0 ? (
            <div className="py-10 text-center text-[13px] text-[#6b6b80]">
              {invoices.length === 0
                ? 'No hay facturas emitidas para mostrar en esta sección.'
                : 'No se encontraron facturas con los filtros aplicados.'}
            </div>
          ) : (
            <table className="w-full text-left text-[12px]">
              <thead>
                <tr className="border-b border-[#ececf2] text-[#6b6b80] font-semibold">
                  <th className="pb-3 px-3">Comprobante</th>
                  <th className="pb-3 px-3">Pedido</th>
                  <th className="pb-3 px-3">Cliente / Doc.</th>
                  <th className="pb-3 px-3">Fecha</th>
                  <th className="pb-3 px-3">Total</th>
                  <th className="pb-3 px-3">Saldo</th>
                  <th className="pb-3 px-3">Estado</th>
                  <th className="pb-3 px-3 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#f0f0f4]">
                {filtered.map((inv) => (
                  <tr key={inv.id} className="hover:bg-[#fafafd] transition-colors">
                    <td className="py-3 px-3 font-mono font-bold text-[#2b2b3a]">
                      {inv.numeroFactura}
                    </td>
                    <td className="py-3 px-3 font-mono text-[#3fb8d9] font-bold">
                      #{inv.pedidoId}
                    </td>
                    <td className="py-3 px-3 font-medium text-[#2b2b3a]">
                      {inv.cliente}
                      <span className="text-[10px] text-[#6b6b80] block">Doc: {inv.cuit}</span>
                    </td>
                    <td className="py-3 px-3 text-[11px] text-[#6b6b80]">
                      {inv.fechaEmision}
                    </td>
                    <td className="py-3 px-3 font-bold text-[#2b2b3a]">
                      ${inv.total.toLocaleString('es-AR')}
                    </td>
                    <td className="py-3 px-3 font-bold">
                      {inv.saldo > 0 ? (
                        <span className="text-[#ff8fa3]">${inv.saldo.toLocaleString('es-AR')}</span>
                      ) : (
                        <span className="text-[#39855b]">$0</span>
                      )}
                    </td>
                    <td className="py-3 px-3">
                      <span
                        className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full ${
                          inv.estado === 'PAGADA'
                            ? 'bg-[#dff8e9] text-[#39855b]'
                            : inv.estado === 'PENDIENTE_PAGO'
                            ? 'bg-[#faf0d9] text-[#b88014]'
                            : 'bg-[#f0f0f4] text-[#6b6b80]'
                        }`}
                      >
                        {inv.estado === 'PENDIENTE_PAGO' ? 'Pendiente' : inv.estado}
                      </span>
                    </td>
                    <td className="py-3 px-3 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          type="button"
                          onClick={() => setSelectedInvoice(inv)}
                          className="text-[11px] font-semibold text-[#3fb8d9] hover:underline"
                          title="Ver detalle de ítems y pagos"
                        >
                          Detalle
                        </button>

                        <button
                          type="button"
                          onClick={() => handleDownloadPDF(inv.id, inv.numeroFactura)}
                          className="text-[11px] font-semibold text-[#e86bc4] hover:underline"
                          title="Descargar PDF"
                        >
                          PDF
                        </button>

                        {inv.estado === 'PENDIENTE_PAGO' && (
                          <button
                            type="button"
                            onClick={() => {
                              setPaymentModalInvoice(inv);
                              setPagoMonto(inv.saldo.toString());
                            }}
                            className="bg-[#ffe2f7] text-[#e86bc4] text-[10px] font-bold px-2 py-0.5 rounded-[6px] hover:bg-[#ffd5f4]"
                          >
                            Cobrar
                          </button>
                        )}

                        {inv.estado !== 'ANULADA' && (
                          <button
                            type="button"
                            onClick={() => setAnularModalInvoice(inv)}
                            className="text-[11px] font-semibold text-[#aaa9ba] hover:text-[#ff8fa3]"
                            title="Anular comprobante"
                          >
                            ✕
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* MODAL DETALLE DE FACTURA */}
      {selectedInvoice && (
        <Modal
          isOpen={!!selectedInvoice}
          onClose={() => setSelectedInvoice(null)}
          title={`Detalle de ${selectedInvoice.numeroFactura}`}
        >
          <div className="space-y-4 text-[12px]">
            <div className="grid grid-cols-2 gap-3 bg-[#fafafd] p-3 rounded-[12px] border border-[#ececf2]">
              <div>
                <span className="text-[#aaa9ba] block text-[10px] uppercase font-bold">Cliente</span>
                <strong className="text-[#2b2b3a]">{selectedInvoice.cliente}</strong>
                <span className="text-[#6b6b80] block">Doc: {selectedInvoice.cuit}</span>
              </div>
              <div>
                <span className="text-[#aaa9ba] block text-[10px] uppercase font-bold">Emisión</span>
                <strong className="text-[#2b2b3a]">{selectedInvoice.numeroFactura}</strong>
                <span className="text-[#6b6b80] block">Fecha: {selectedInvoice.fechaEmision}</span>
              </div>
            </div>

            {/* Ítems */}
            <div>
              <span className="text-[11px] font-bold text-[#2b2b3a] block mb-1.5">Ítems facturados:</span>
              <div className="border border-[#ececf2] rounded-[10px] overflow-hidden">
                <table className="w-full text-left">
                  <thead className="bg-[#fafafd] text-[#6b6b80] text-[11px]">
                    <tr>
                      <th className="p-2">Producto</th>
                      <th className="p-2 text-center">Cant.</th>
                      <th className="p-2 text-right">P. Unit</th>
                      <th className="p-2 text-right">Subtotal</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[#ececf2]">
                    {selectedInvoice.items.map((it, i) => (
                      <tr key={i}>
                        <td className="p-2 font-medium">{it.producto}</td>
                        <td className="p-2 text-center">{it.cantidad}</td>
                        <td className="p-2 text-right">${it.precio.toLocaleString('es-AR')}</td>
                        <td className="p-2 text-right font-bold">${it.subtotal.toLocaleString('es-AR')}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Pagos imputados */}
            <div>
              <span className="text-[11px] font-bold text-[#2b2b3a] block mb-1">Pagos registrados contra esta factura:</span>
              {selectedInvoice.pagos.length === 0 ? (
                <p className="text-[11px] text-[#aaa9ba] italic">No hay pagos registrados aún.</p>
              ) : (
                <div className="space-y-1.5">
                  {selectedInvoice.pagos.map((pg) => (
                    <div key={pg.id} className="p-2 rounded-[8px] bg-[#f2fdf7] border border-[#d2f3e2] flex items-center justify-between text-[11px]">
                      <div>
                        <strong className="text-[#39855b]">{pg.medio}</strong> ({pg.comprobante}) - {pg.fecha}
                      </div>
                      <strong className="text-[#39855b]">${pg.monto.toLocaleString('es-AR')}</strong>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="flex justify-between items-center pt-3 border-t border-[#f0f0f4]">
              <button
                type="button"
                onClick={() => handleReenviarCorreo(selectedInvoice.id, selectedInvoice.numeroFactura)}
                className="text-[11px] font-bold text-[#3fb8d9] hover:underline flex items-center gap-1.5"
              >
                <Icon name="mail" className="w-3.5 h-3.5" />
                <span>Reenviar por correo</span>
              </button>
              <Button onClick={() => setSelectedInvoice(null)}>Cerrar</Button>
            </div>
          </div>
        </Modal>
      )}

      {/* MODAL COBRAR / IMPUTAR PAGO */}
      {paymentModalInvoice && (
        <Modal
          isOpen={!!paymentModalInvoice}
          onClose={() => setPaymentModalInvoice(null)}
          title={`Registrar Cobro para ${paymentModalInvoice.numeroFactura}`}
        >
          <form onSubmit={handleImputarPago} className="space-y-4">
            <div className="p-3 bg-[#fafafd] rounded-[10px] border border-[#ececf2] text-[12px]">
              <div className="flex justify-between">
                <span>Cliente:</span>
                <strong>{paymentModalInvoice.cliente}</strong>
              </div>
              <div className="flex justify-between mt-1 text-[#ff8fa3] font-bold">
                <span>Saldo pendiente:</span>
                <span>${paymentModalInvoice.saldo.toLocaleString('es-AR')}</span>
              </div>
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Monto a Imputar ($) *
              </label>
              <input
                type="number"
                step="0.01"
                required
                value={pagoMonto}
                onChange={(e) => setPagoMonto(e.target.value)}
                max={paymentModalInvoice.saldo}
                className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Medio de Pago *
              </label>
              <select
                value={pagoMedio}
                onChange={(e) => setPagoMedio(e.target.value)}
                className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] bg-white focus:outline-none focus:border-[#e86bc4]"
              >
                <option value="Transferencia Bancaria">Transferencia Bancaria</option>
                <option value="Efectivo (Distribuidor)">Efectivo (Distribuidor)</option>
                <option value="Billetera Virtual">Billetera Virtual</option>
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                N° de Comprobante / Referencia
              </label>
              <input
                type="text"
                value={pagoComprobante}
                onChange={(e) => setPagoComprobante(e.target.value)}
                placeholder="Ej: TRF-909988 o REC-0044"
                className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div className="flex justify-end gap-2 pt-3 border-t border-[#f0f0f4]">
              <Button secondary type="button" onClick={() => setPaymentModalInvoice(null)}>Cancelar</Button>
              <Button type="submit">Imputar Cobro</Button>
            </div>
          </form>
        </Modal>
      )}

      {/* MODAL ANULAR FACTURA */}
      {anularModalInvoice && (
        <Modal
          isOpen={!!anularModalInvoice}
          onClose={() => setAnularModalInvoice(null)}
          title={`Anular Comprobante ${anularModalInvoice.numeroFactura}`}
        >
          <form onSubmit={handleConfirmarAnulacion} className="space-y-4">
            <div className="p-3 bg-[#fdf2f4] rounded-[10px] border border-[#fbd3db] text-[12px] text-[#c95569]">
              <strong>Atención:</strong> Al anular la factura se revertirá el saldo adeudado del minorista.
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Motivo de Anulación *
              </label>
              <input
                type="text"
                required
                value={motivoAnulacion}
                onChange={(e) => setMotivoAnulacion(e.target.value)}
                placeholder="Ej: Error de facturación / Devolución de mercadería"
                className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div className="flex justify-end gap-2 pt-3 border-t border-[#f0f0f4]">
              <Button secondary type="button" onClick={() => setAnularModalInvoice(null)}>Cancelar</Button>
              <Button type="submit" variant="danger">Confirmar Anulación</Button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
