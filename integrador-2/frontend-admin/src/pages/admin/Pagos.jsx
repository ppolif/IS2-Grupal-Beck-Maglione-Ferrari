import { useState } from 'react';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { paymentsMock } from '../../mocks/payments';

export default function Pagos() {
  const [payments, setPayments] = useState(paymentsMock);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [periodo, setPeriodo] = useState('Junio 2025');
  const [medio, setMedio] = useState('Todos');

  const [formData, setFormData] = useState({
    pedidoId: 'DM-2084',
    comercio: 'Kiosco El Sol',
    monto: '$45.200',
    medioPago: 'Transferencia Bancaria',
    comprobante: 'TRF-909988'
  });

  const handleRegisterPayment = (e) => {
    e.preventDefault();
    const newPayment = {
      id: `PAG-${Math.floor(4092 + Math.random() * 100)}`,
      pedidoId: formData.pedidoId,
      comercio: formData.comercio,
      monto: formData.monto,
      medioPago: formData.medioPago,
      fecha: '14/06/2025 11:40',
      estado: 'Acreditado',
      comprobante: formData.comprobante
    };
    setPayments([newPayment, ...payments]);
    alert(`Pago de ${formData.monto} para pedido #${formData.pedidoId} REGISTRADO con éxito.`);
    setIsModalOpen(false);
  };

  const filtered = payments.filter((p) => {
    if (medio === 'Todos') return true;
    return p.medioPago.includes(medio);
  });

  return (
    <div className="space-y-6">
      {/* Title */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Tesorería y Cobranzas
        </span>
        <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
          Registro y Control de Pagos
        </h2>
        <p className="text-[12px] text-[#6b6b80] mt-0.5">
          Registrá los pagos recibidos vía transferencia, cheques o rendiciones en efectivo de distribuidores.
        </p>
      </div>

      {/* Filterbar matching prototype filterbar */}
      <div className="bg-white rounded-[18px] p-4 border border-[#ececf2] shadow-sm flex flex-wrap items-center gap-3">
        <span className="border border-[#ececf2] px-3.5 py-2 rounded-[10px] text-[11px] text-[#6b6b80] bg-[#fafafd]">
          Período: <strong className="text-[#2b2b3a]">Junio 2025</strong>
        </span>

        <span className="border border-[#ececf2] px-3.5 py-2 rounded-[10px] text-[11px] text-[#6b6b80] bg-[#fafafd]">
          Departamento: <strong className="text-[#2b2b3a]">Gran Mendoza</strong>
        </span>

        <div className="flex items-center gap-1.5 border border-[#ececf2] px-3 py-1.5 rounded-[10px] bg-[#fafafd] text-[11px] text-[#6b6b80]">
          <span>Medio de pago:</span>
          <select
            value={medio}
            onChange={(e) => setMedio(e.target.value)}
            className="bg-transparent text-[#2b2b3a] font-semibold outline-none cursor-pointer"
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
      <div className="bg-white rounded-[20px] p-6 border border-[#ececf2] shadow-sm">
        <div className="flex items-center justify-between pb-3 mb-3 border-b border-[#f0f0f4]">
          <h3 className="text-[15px] font-bold text-[#2b2b3a]">
            Estado de Pagos Registrados
          </h3>
          <span className="text-[11px] text-[#6b6b80]">
            Total registros: <strong>{filtered.length}</strong>
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-[#ececf2] text-[10px] uppercase font-bold text-[#aaa9b8] tracking-wider">
                <th className="pb-3 px-3">Nº Pago</th>
                <th className="pb-3 px-3">Pedido</th>
                <th className="pb-3 px-3">Comercio</th>
                <th className="pb-3 px-3">Monto</th>
                <th className="pb-3 px-3">Medio de Pago</th>
                <th className="pb-3 px-3">Fecha</th>
                <th className="pb-3 px-3">Estado</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#f0f0f4] text-[12px]">
              {filtered.map((p) => (
                <tr key={p.id} className="hover:bg-slate-50/70 transition-colors">
                  <td className="py-3 px-3 font-semibold text-[#2b2b3a]">
                    {p.id}
                  </td>
                  <td className="py-3 px-3 font-bold text-[#3fb8d9]">
                    #{p.pedidoId}
                  </td>
                  <td className="py-3 px-3 font-medium text-[#2b2b3a]">
                    {p.comercio}
                  </td>
                  <td className="py-3 px-3 font-bold text-[#2b2b3a]">
                    {p.monto}
                  </td>
                  <td className="py-3 px-3 text-[#6b6b80]">{p.medioPago}</td>
                  <td className="py-3 px-3 text-[#6b6b80] text-[11px]">{p.fecha}</td>
                  <td className="py-3 px-3">
                    <span
                      className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full ${
                        p.estado === 'Acreditado'
                          ? 'bg-[#dff8e9] text-[#39855b]'
                          : p.estado.includes('Cobrado')
                          ? 'bg-[#d9f7ff] text-[#247f99]'
                          : 'bg-[#fff2d3] text-[#986b16]'
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
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Nº de Pedido a Imputar *
            </label>
            <input
              type="text"
              required
              value={formData.pedidoId}
              onChange={(e) => setFormData({ ...formData, pedidoId: e.target.value })}
              className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4]"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Nombre del Comercio / Cliente *
            </label>
            <input
              type="text"
              required
              value={formData.comercio}
              onChange={(e) => setFormData({ ...formData, comercio: e.target.value })}
              className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4]"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Monto cobrado *
              </label>
              <input
                type="text"
                required
                value={formData.monto}
                onChange={(e) => setFormData({ ...formData, monto: e.target.value })}
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Medio de Pago *
              </label>
              <select
                value={formData.medioPago}
                onChange={(e) => setFormData({ ...formData, medioPago: e.target.value })}
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              >
                <option>Transferencia Bancaria</option>
                <option>Efectivo</option>
                <option>Cheque 30 días</option>
                <option>Depósito en cuenta</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Nº de Comprobante / Recibo
            </label>
            <input
              type="text"
              value={formData.comprobante}
              onChange={(e) => setFormData({ ...formData, comprobante: e.target.value })}
              className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4]"
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

