import { useState } from 'react';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import { minoristasMock } from '../../mocks/minoristasMock';

export default function Minoristas() {
  const [minoristas] = useState(minoristasMock);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedDepto, setSelectedDepto] = useState('Todos');

  const deptos = ['Todos', 'Godoy Cruz', 'Capital', 'Guaymallén', 'Las Heras', 'Maipú', 'Luján de Cuyo'];

  const handleExportPDF = () => {
    alert('Exportando padrón de clientes minoristas en formato PDF oficial... (Simulado)');
  };

  const handleExportExcel = () => {
    alert('Exportando padrón de clientes minoristas en formato Excel (.xlsx)... (Simulado)');
  };

  const filtered = minoristas.filter((m) => {
    const matchesSearch =
      m.nombreFantasia.toLowerCase().includes(searchTerm.toLowerCase()) ||
      m.razonSocial.toLowerCase().includes(searchTerm.toLowerCase()) ||
      m.cuit.includes(searchTerm) ||
      m.preventorAsignado.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesDepto = selectedDepto === 'Todos' || m.departamento === selectedDepto;
    return matchesSearch && matchesDepto;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Cartera Mayorista
          </span>
          <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
            Padrón de Clientes Minoristas y Puntos de Venta
          </h2>
          <p className="text-[12px] text-[#6b6b80] mt-0.5">
            Registro de comercios habilitados, zonas de cobertura y saldos de cuenta corriente.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button secondary onClick={handleExportPDF} className="text-[11px] px-3.5 py-2">
            <Icon name="download" className="w-3.5 h-3.5" />
            <span>Descargar PDF</span>
          </Button>
          <Button onClick={handleExportExcel} className="text-[11px] px-3.5 py-2">
            <Icon name="download" className="w-3.5 h-3.5" />
            <span>Descargar Excel</span>
          </Button>
        </div>
      </div>

      {/* Filterbar */}
      <div className="bg-white rounded-[18px] p-4 border border-[#ececf2] shadow-sm flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2 overflow-x-auto pb-1 sm:pb-0">
          <span className="text-[11px] text-[#6b6b80] font-semibold">Departamento:</span>
          {deptos.slice(0, 5).map((d) => (
            <button
              key={d}
              type="button"
              onClick={() => setSelectedDepto(d)}
              className={`px-3 py-1.5 rounded-[10px] text-[11px] font-semibold whitespace-nowrap transition-all cursor-pointer ${
                selectedDepto === d
                  ? 'bg-[#2b2b3a] text-white shadow-sm'
                  : 'bg-[#fafafd] text-[#6b6b80] border border-[#ececf2] hover:bg-slate-50'
              }`}
            >
              {d}
            </button>
          ))}
        </div>

        <div className="relative sm:w-72">
          <Icon name="search" className="w-4 h-4 text-[#aaa9ba] absolute left-3 top-2.5" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Buscar por comercio, CUIT o preventor..."
            className="w-full bg-[#fafafd] border border-[#ececf2] rounded-[10px] pl-9 pr-3 py-1.5 text-[11px] text-[#2b2b3a] focus:bg-white focus:outline-none focus:border-[#e86bc4]"
          />
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-[20px] p-6 border border-[#ececf2] shadow-sm">
        <div className="flex items-center justify-between pb-3 mb-3 border-b border-[#f0f0f4]">
          <h3 className="text-[15px] font-bold text-[#2b2b3a]">
            Clientes Minoristas Registrados
          </h3>
          <span className="text-[11px] text-[#6b6b80]">
            Total registros: <strong>{filtered.length}</strong>
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-[12px]">
            <thead>
              <tr className="border-b border-[#ececf2] text-[#6b6b80] font-semibold">
                <th className="pb-3 px-3">Comercio / Razón Social</th>
                <th className="pb-3 px-3">CUIT</th>
                <th className="pb-3 px-3">Ubicación</th>
                <th className="pb-3 px-3">Contacto</th>
                <th className="pb-3 px-3">Preventor Asignado</th>
                <th className="pb-3 px-3 text-center">Pedidos</th>
                <th className="pb-3 px-3 text-right">Saldo Deudor</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#f0f0f4]">
              {filtered.map((m) => (
                <tr key={m.id} className="hover:bg-[#fafafd] transition-colors">
                  <td className="py-3 px-3">
                    <strong className="text-[#2b2b3a] font-bold block">{m.nombreFantasia}</strong>
                    <span className="text-[10px] text-[#6b6b80]">{m.razonSocial}</span>
                  </td>
                  <td className="py-3 px-3 font-mono font-semibold text-[#6b6b80]">
                    {m.cuit}
                  </td>
                  <td className="py-3 px-3">
                    <span className="text-[#2b2b3a] font-medium block">{m.direccion}</span>
                    <span className="text-[10px] text-[#3fb8d9] font-bold">{m.departamento}</span>
                  </td>
                  <td className="py-3 px-3 text-[11px]">
                    <span className="text-[#2b2b3a] block">{m.telefono}</span>
                    <span className="text-[#aaa9ba]">{m.email}</span>
                  </td>
                  <td className="py-3 px-3 font-medium text-[#2b2b3a]">
                    💼 {m.preventorAsignado}
                  </td>
                  <td className="py-3 px-3 text-center font-bold text-[#2b2b3a]">
                    {m.pedidosHistoricos}
                  </td>
                  <td className="py-3 px-3 text-right font-bold">
                    {m.saldoDeudor > 0 ? (
                      <span className="text-[#ff8fa3]">${m.saldoDeudor.toLocaleString('es-AR')}</span>
                    ) : (
                      <span className="text-[#39855b]">Al día ($0)</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
