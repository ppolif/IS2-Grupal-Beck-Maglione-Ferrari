/**
 * Servicio centralizado para el cálculo y sincronización en tiempo real del dinero
 * recaudado en efectivo y el avance de entregas del distribuidor en su ruta activa.
 */

const RESUMEN_STORAGE_KEY = 'dm_distribuidor_resumen_ruta';

export function parseMonto(montoStr) {
  if (typeof montoStr === 'number') return montoStr;
  if (!montoStr) return 0;
  const cleaned = montoStr.toString().replace(/[^0-9,-]+/g, '').replace(',', '.');
  return parseFloat(cleaned) || 0;
}

export function esParadaEfectivo(parada) {
  if (!parada) return false;
  return Boolean(
    parada.esEfectivo ||
      parada.tipoPago === 'EFECTIVO' ||
      (typeof parada.medioPago === 'string' && parada.medioPago.toLowerCase().includes('efectivo'))
  );
}

const RESUMEN_INICIAL = {
  total: 0,
  entregadosCount: 0,
  pendientesCount: 0,
  recaudadoEfectivo: 0,
  pendienteEfectivo: 0,
  entregasEfectivoCount: 0,
  pendientesEfectivoCount: 0,
};

export const recorridoService = {
  calcularResumenDesdeRuta(ruta) {
    if (!ruta || !Array.isArray(ruta.paradas)) {
      return { ...RESUMEN_INICIAL };
    }

    const paradas = ruta.paradas;
    const entregados = paradas.filter((p) => p.completada);
    const pendientes = paradas.filter((p) => !p.completada);

    const entregasEfectivo = entregados.filter(esParadaEfectivo);
    const pendientesEfectivo = pendientes.filter(esParadaEfectivo);

    const recaudadoCalculado = entregasEfectivo.reduce(
      (acc, p) => acc + parseMonto(p.totalPedido ?? p.monto),
      0
    );
    const pendienteCalculado = pendientesEfectivo.reduce(
      (acc, p) => acc + parseMonto(p.totalPedido ?? p.monto),
      0
    );

    const recaudadoEfectivo =
      typeof ruta.dineroRecaudadoEfectivo === 'number'
        ? Math.max(ruta.dineroRecaudadoEfectivo, recaudadoCalculado)
        : recaudadoCalculado;

    const pendienteEfectivo =
      typeof ruta.dineroPendienteEfectivo === 'number'
        ? ruta.dineroPendienteEfectivo
        : pendienteCalculado;

    return {
      total: paradas.length,
      entregadosCount: entregados.length,
      pendientesCount: pendientes.length,
      recaudadoEfectivo,
      pendienteEfectivo,
      entregasEfectivoCount: entregasEfectivo.length,
      pendientesEfectivoCount: pendientesEfectivo.length,
    };
  },

  sincronizarRuta(ruta) {
    const resumen = this.calcularResumenDesdeRuta(ruta);
    try {
      localStorage.setItem(RESUMEN_STORAGE_KEY, JSON.stringify(resumen));
      window.dispatchEvent(new CustomEvent('dm-deliveries-updated', { detail: resumen }));
    } catch {
      // Fallback silencioso si localStorage no está disponible
    }
    return resumen;
  },

  obtenerResumen() {
    try {
      const raw = localStorage.getItem(RESUMEN_STORAGE_KEY);
      if (raw) {
        return { ...RESUMEN_INICIAL, ...JSON.parse(raw) };
      }
    } catch {
      // Fallback silencioso
    }
    return { ...RESUMEN_INICIAL };
  },
};
