/**
 * Servicio para gestión y consulta del catálogo de productos y categorías desde el backend.
 * Conecta con los endpoints públicos y autenticados de Spring Boot.
 */
import { apiFetch } from './api';

export const productoService = {
  /**
   * Obtiene la lista de todos los productos disponibles con sus precios vigentes.
   * @returns {Promise<Array>} Lista de productos
   */
  async listarProductos() {
    return apiFetch('/productos');
  },

  /**
   * Obtiene el detalle de un producto por su identificador.
   * @param {string} id
   * @returns {Promise<Object>}
   */
  async obtenerPorId(id) {
    return apiFetch(`/productos/${id}`);
  },

  /**
   * Obtiene las categorías de productos disponibles.
   * @returns {Promise<Array>}
   */
  async listarCategorias() {
    return apiFetch('/categorias');
  },

  /**
   * Obtiene promociones o descuentos vigentes.
   * @returns {Promise<Array>}
   */
  async listarPromociones() {
    return apiFetch('/promociones');
  }
};
