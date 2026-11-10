import { deliveriesMock } from '../mocks/deliveries';

const STORAGE_KEY = 'dm_distribuidor_deliveries';

export function parseMonto(montoStr) {
  if (typeof montoStr === 'number') return montoStr;
  if (!montoStr) return 0;
  // Limpiar "$", ".", espacios y convertir coma a punto
  const cleaned = montoStr.replace(/[^0-9,-]+/g, '').replace(',', '.');
  return parseFloat(cleaned) || 0;
}

export const recorridoService = {
  obtenerEntregas() {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        return JSON.parse(stored);
      }
    } catch {
      // Fallback
    }
    return deliveriesMock;
  },

  guardarEntregas(deliveries) {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(deliveries));
      window.dispatchEvent(new CustomEvent('dm-deliveries-updated'));
    } catch {
      // Fallback
    }
  },

  obtenerResumen() {
    const deliveries = this.obtenerEntregas();
    const entregados = deliveries.filter((d) => d.estadoEntrega === 'Entregado');
    const pendientes = deliveries.filter((d) => d.estadoEntrega !== 'Entregado');

    // Dinero recaudado en efectivo: solo pedidos entregados cuyo cobro se realizó en efectivo
    const recaudadoEfectivo = entregados
      .filter((d) => d.medioPago?.toLowerCase().includes('efectivo') || d.cobradoEnEfectivo)
      .reduce((acc, d) => acc + parseMonto(d.monto), 0);

    // Dinero pendiente de cobro en efectivo en las paradas restantes
    const pendienteEfectivo = pendientes
      .filter((d) => d.estadoPago === 'cash' || d.medioPago?.toLowerCase().includes('efectivo'))
      .reduce((acc, d) => acc + parseMonto(d.monto), 0);

    return {
      total: deliveries.length,
      entregadosCount: entregados.length,
      pendientesCount: pendientes.length,
      recaudadoEfectivo,
      pendienteEfectivo,
    };
  },
};
