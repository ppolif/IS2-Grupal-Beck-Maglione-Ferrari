/**
 * Servicio de soporte para el cálculo de montos y almacenamiento local de cobros de distribución.
 */
export function parseMonto(montoStr) {
  if (typeof montoStr === 'number') return montoStr;
  if (!montoStr) return 0;
  const cleaned = montoStr.toString().replace(/[^0-9,-]+/g, '').replace(',', '.');
  return parseFloat(cleaned) || 0;
}
