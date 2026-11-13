import { useState, useEffect, useCallback } from 'react';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import { apiFetch } from '../../services/api';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

export default function Minoristas() {
  const [minoristas, setMinoristas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedDepto, setSelectedDepto] = useState('Todos');

  const deptos = ['Todos', 'Godoy Cruz', 'Capital', 'Guaymallén', 'Las Heras', 'Maipú', 'Luján de Cuyo'];

  const cargarMinoristas = useCallback(async () => {
    setLoading(true);
    try {
      const [minRes, pdvRes, pedRes, factRes] = await Promise.all([
        apiFetch('/minoristas?size=100', { method: 'GET' }),
        apiFetch('/puntos-de-venta?size=200', { method: 'GET' }).catch(() => ({ elementos: [] })),
        apiFetch('/pedidos?size=200', { method: 'GET' }).catch(() => ({ elementos: [] })),
        apiFetch('/facturas?size=200', { method: 'GET' }).catch(() => ({ elementos: [] })),
      ]);

      const listaMin = minRes.elementos || minRes.content || (Array.isArray(minRes) ? minRes : []);
      const listaPdv = pdvRes.elementos || pdvRes.content || (Array.isArray(pdvRes) ? pdvRes : []);
      const listaPed = pedRes.elementos || pedRes.content || (Array.isArray(pedRes) ? pedRes : []);
      const listaFact = factRes.elementos || factRes.content || (Array.isArray(factRes) ? factRes : []);

      const mapped = listaMin.map((m) => {
        const comercios = listaPdv.filter((p) => p.minorista?.id === m.id);
        const pdvPrincipal = comercios[0];
        const dir = pdvPrincipal?.direccion || (m.direcciones && m.direcciones[0]) || null;

        const emailContacto = (m.contactos || []).find((c) => c.email)?.email || '-';
        const telContacto = (m.contactos || []).find((c) => c.telefono)?.telefono || '-';

        const preventorAsignado = pdvPrincipal?.preventor
          ? `${pdvPrincipal.preventor.nombre || ''} ${pdvPrincipal.preventor.apellido || ''}`.trim()
          : 'Sin asignar';

        const pdvIds = new Set(comercios.map((c) => c.id));
        const pedidosCliente = listaPed.filter((p) => pdvIds.has(p.puntoDeVenta?.id));
        const pedidoIds = new Set(pedidosCliente.map((p) => p.id));

        const saldoDeudor = listaFact
          .filter((f) => pedidoIds.has(f.pedidoId) && f.estado === 'PENDIENTE_PAGO')
          .reduce((acc, f) => acc + Number(f.saldo || 0), 0);

        return {
          id: m.id,
          nombreFantasia:
            comercios.length > 0
              ? comercios.map((c) => c.nombreComercio).join(', ')
              : `${m.nombre || ''} ${m.apellido || ''}`.trim(),
          razonSocial: `${m.nombre || ''} ${m.apellido || ''}`.trim(),
          cuit: `${m.tipoDocumento || 'DOC'} ${m.numeroDocumento || '-'}`,
          direccion: dir ? `${dir.calle || ''} ${dir.numeracion || ''}`.trim() || '-' : '-',
          departamento: dir?.departamentoNombre || dir?.localidadNombre || '-',
          telefono: telContacto,
          email: emailContacto,
          preventorAsignado,
          pedidosHistoricos: pedidosCliente.length,
          saldoDeudor,
        };
      });

      setMinoristas(mapped);
    } catch (err) {
      console.error('Error al cargar minoristas:', err);
      setMinoristas([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarMinoristas();
  }, [cargarMinoristas]);

  const descargarReporte = async (formato) => {
    try {
      const token = localStorage.getItem('dm_admin_token');
      const res = await fetch(`${API_BASE_URL}/reportes/minoristas?formato=${formato}`, {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `reporte-minoristas.${formato}`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert(`No se pudo exportar el reporte: ${err.message}`);
    }
  };

  const filtered = minoristas.filter((m) => {
    const q = searchTerm.toLowerCase();
    const matchesSearch =
      m.nombreFantasia.toLowerCase().includes(q) ||
      m.razonSocial.toLowerCase().includes(q) ||
      m.cuit.toLowerCase().includes(q) ||
      m.preventorAsignado.toLowerCase().includes(q);
    const matchesDepto =
      selectedDepto === 'Todos' ||
      m.departamento.toLowerCase().includes(selectedDepto.toLowerCase());
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
          <Button secondary onClick={() => descargarReporte('pdf')} className="text-[11px] px-3.5 py-2">
            <Icon name="download" className="w-3.5 h-3.5" />
            <span>Descargar PDF</span>
          </Button>
          <Button onClick={() => descargarReporte('xlsx')} className="text-[11px] px-3.5 py-2">
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
            placeholder="Buscar por comercio, documento o preventor..."
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
          {loading ? (
            <div className="py-10 text-center text-[12px] text-[#6b6b80]">
              Cargando padrón de minoristas...
            </div>
          ) : filtered.length === 0 ? (
            <div className="py-10 text-center text-[13px] text-[#6b6b80]">
              {minoristas.length === 0
                ? 'No hay clientes minoristas registrados para mostrar en esta sección.'
                : 'No se encontraron clientes minoristas con el filtro actual.'}
            </div>
          ) : (
            <table className="w-full text-left text-[12px]">
              <thead>
                <tr className="border-b border-[#ececf2] text-[#6b6b80] font-semibold">
                  <th className="pb-3 px-3">Comercio / Titular</th>
                  <th className="pb-3 px-3">Documento</th>
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
                      {m.preventorAsignado}
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
          )}
        </div>
      </div>
    </div>
  );
}
