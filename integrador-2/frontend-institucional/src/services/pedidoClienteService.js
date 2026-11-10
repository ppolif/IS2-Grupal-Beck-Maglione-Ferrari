import { apiFetch } from './api';

const STORAGE_KEY = 'dm_cliente_pedidos';

const INITIAL_MOCK_ORDERS = [
  {
    id: 'DM-2084',
    fecha: '14 Jun 2025 · 10:15 hs',
    total: '$45.200',
    subtotal: '$50.100',
    descuento: '$4.900',
    estado: 'En camino',
    facturaNum: 'FC-A-0001-00002084',
    medioPago: 'Billetera Virtual / QR',
    direccionEntrega: 'Av. San Martín 1842, Godoy Cruz, Mendoza',
    items: [
      { nombre: 'Alfajor triple chocolate', cant: 2, precioUnit: '$18.900', subtotal: '$37.800' },
      { nombre: 'Gomitas frutales', cant: 1, precioUnit: '$6.450', subtotal: '$6.450' },
      { nombre: 'Chupetines surtidos', cant: 1, precioUnit: '$5.850', subtotal: '$5.850' },
    ],
    trazabilidad: [
      { estado: 'Pedido Confirmado', fecha: '14 Jun - 09:30 hs', done: true },
      { estado: 'Facturado en Depósito', fecha: '14 Jun - 10:15 hs', done: true },
      { estado: 'En Reparto con Distribuidor', fecha: '14 Jun - 11:00 hs', done: true },
      { estado: 'Entregado en Comercio', fecha: 'Pendiente', done: false },
    ],
  },
  {
    id: 'DM-1950',
    fecha: '28 May 2025 · 14:20 hs',
    total: '$82.400',
    subtotal: '$89.000',
    descuento: '$6.600',
    estado: 'Entregado',
    facturaNum: 'FC-A-0001-00001950',
    medioPago: 'Transferencia Bancaria',
    direccionEntrega: 'Av. San Martín 1842, Godoy Cruz, Mendoza',
    items: [
      { nombre: 'Alfajor triple chocolate', cant: 3, precioUnit: '$18.900', subtotal: '$56.700' },
      { nombre: 'Chocolate con maní', cant: 2, precioUnit: '$15.600', subtotal: '$31.200' },
      { nombre: 'Gomitas frutales', cant: 1, precioUnit: '$1.100', subtotal: '$1.100' },
    ],
    trazabilidad: [
      { estado: 'Pedido Confirmado', fecha: '28 May - 11:00 hs', done: true },
      { estado: 'Facturado', fecha: '28 May - 12:30 hs', done: true },
      { estado: 'En Reparto', fecha: '28 May - 14:00 hs', done: true },
      { estado: 'Entregado en Comercio', fecha: '28 May - 16:30 hs', done: true },
    ],
  },
];

export const pedidoClienteService = {
  /**
   * Obtiene la lista de pedidos del cliente minorista.
   */
  obtenerPedidos() {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (!stored) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(INITIAL_MOCK_ORDERS));
        return INITIAL_MOCK_ORDERS;
      }
      return JSON.parse(stored);
    } catch {
      return INITIAL_MOCK_ORDERS;
    }
  },

  /**
   * Registra y persiste un pedido confirmado por el cliente.
   */
  guardarPedido(nuevoPedido) {
    try {
      const pedidosActuales = this.obtenerPedidos();
      const actualizados = [nuevoPedido, ...pedidosActuales.filter((p) => p.id !== nuevoPedido.id)];
      localStorage.setItem(STORAGE_KEY, JSON.stringify(actualizados));
      window.dispatchEvent(new CustomEvent('dm-pedido-actualizado', { detail: nuevoPedido }));
      return nuevoPedido;
    } catch (err) {
      console.error('Error al persistir pedido localmente:', err);
      return nuevoPedido;
    }
  },

  /**
   * Consulta los pedidos del cliente en el backend si hay sesión activa.
   */
  async sincronizarConBackend() {
    try {
      const response = await apiFetch('/mi-cuenta/pedidos');
      if (response && response.content && Array.isArray(response.content)) {
        return response.content;
      }
    } catch {
      // Fallback silencioso a almacenamiento local
    }
    return this.obtenerPedidos();
  },
};
