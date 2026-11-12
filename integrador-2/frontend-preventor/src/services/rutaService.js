/**
 * Servicio para la gestión operativa de rutas diarias, seguimiento GPS y paradas
 * tanto de preventores como de repartidores/distribuidores.
 */
import { apiFetch } from './api';

export const rutaService = {
  // ==========================================
  // OPERACIONES DE PREVENTORES
  // ==========================================

  /**
   * Obtiene el itinerario y paradas de visita asignadas al preventor para hoy.
   * @param {string} [fecha] - Formato YYYY-MM-DD opcional
   * @returns {Promise<Object>} RutaResponse con paradas y coordenadas
   */
  async obtenerMiRutaPreventorDelDia(fecha) {
    const qs = fecha ? `?fecha=${fecha}` : '';
    return apiFetch(`/rutas-preventor/mi-ruta-del-dia${qs}`);
  },

  /**
   * Genera o recalcula la ruta de visitas diaria para el preventor autenticado.
   * Si no se indica fecha, el backend utiliza su fecha operativa actual (LocalDate.now()).
   * @param {string} [fecha] - Formato YYYY-MM-DD opcional
   * @returns {Promise<Object>} RutaResponse
   */
  async generarRutaPreventor(fecha) {
    const payload = fecha ? { fecha } : {};
    return apiFetch('/rutas-preventor/generar', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  /**
   * Registra una visita concretada en un punto de venta.
   * @param {string} rutaId - Identificador de la ruta del día
   * @param {Object} data - { puntoDeVentaId, pedidoId }
   * @returns {Promise<Object>} Avance con la siguiente parada sugerida
   */
  async completarParadaPreventor(rutaId, data) {
    return apiFetch(`/rutas-preventor/${rutaId}/parada-completada`, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  /**
   * Cierra formalmente la jornada del preventor marcando la ruta como finalizada.
   * @param {string} rutaId
   * @returns {Promise<Object>}
   */
  async cerrarRutaPreventor(rutaId) {
    return apiFetch(`/rutas-preventor/${rutaId}/cerrar`, {
      method: 'POST',
    });
  },

  // ==========================================
  // OPERACIONES DE DISTRIBUIDORES / REPARTO
  // ==========================================

  /**
   * Obtiene la ruta y remitos de entrega asignados al chofer/distribuidor para hoy.
   * @param {string} [fecha] - Formato YYYY-MM-DD opcional
   * @returns {Promise<Object>} RutaResponse con pedidos y paradas
   */
  async obtenerMiRutaDistribuidorDelDia(fecha) {
    const qs = fecha ? `?fecha=${fecha}` : '';
    return apiFetch(`/rutas-distribuidor/mi-ruta-del-dia${qs}`);
  },

  /**
   * Genera o recalcula la ruta de reparto diaria para los distribuidores a partir de pedidos facturados.
   * @param {string} [fecha] - Formato YYYY-MM-DD opcional
   * @returns {Promise<Object>} RutaResponse
   */
  async generarRutaDistribuidor(fecha) {
    const payload = fecha ? { fecha } : {};
    return apiFetch('/rutas-distribuidor/generar', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  /**
   * Registra la entrega exitosa de un pedido en destino, disparando el aviso por mail
   * al siguiente comercio y actualizando la carga.
   * @param {string} rutaId - Identificador de la ruta
   * @param {Object} data - { pedidoId, observacion }
   * @returns {Promise<Object>}
   */
  async registrarEntregaDistribuidor(rutaId, data) {
    return apiFetch(`/rutas-distribuidor/${rutaId}/entregas`, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }
};
