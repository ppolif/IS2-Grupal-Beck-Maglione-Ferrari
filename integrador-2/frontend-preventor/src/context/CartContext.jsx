/**
 * Contexto global del Carrito de Compras para el Preventista.
 * Permite seleccionar el comercio/punto de venta actual, agregar productos,
 * modificar cantidades y confirmar la orden emitiéndola hacia el backend.
 */
import { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react';
import { pedidoService } from '../services/pedidoService';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

const STORAGE_KEY = 'dm_preventor_cart';
const PDV_KEY = 'dm_preventor_active_pdv';

function readJsonFromStorage(key, fallback) {
  if (!key) return fallback;
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : fallback;
  } catch {
    return fallback;
  }
}

export function CartProvider({ children }) {
  const { usuario } = useAuth();
  const userScope = usuario?.id || usuario?.nombreUsuario || null;
  const cartStorageKey = userScope ? `${STORAGE_KEY}_${userScope}` : null;
  const pdvStorageKey = userScope ? `${PDV_KEY}_${userScope}` : null;

  // Ítems en el carrito: [{ id, nombre, precio, cantidad, presentacion, img }]
  const [items, setItems] = useState(() => readJsonFromStorage(cartStorageKey, []));

  // Punto de venta seleccionado para el que se está tomando el pedido
  const [clienteActual, setClienteActual] = useState(() =>
    readJsonFromStorage(pdvStorageKey, null)
  );

  const [codigoPromo, setCodigoPromo] = useState('');

  // Recargar carrito y comercio activo cuando cambia el usuario autenticado
  useEffect(() => {
    setItems(readJsonFromStorage(cartStorageKey, []));
    setClienteActual(readJsonFromStorage(pdvStorageKey, null));
    setCodigoPromo('');
  }, [cartStorageKey, pdvStorageKey]);

  // Persistir en localStorage aislado por usuario
  useEffect(() => {
    if (!cartStorageKey) return;
    localStorage.setItem(cartStorageKey, JSON.stringify(items));
  }, [cartStorageKey, items]);

  useEffect(() => {
    if (!pdvStorageKey) return;
    if (clienteActual) {
      localStorage.setItem(pdvStorageKey, JSON.stringify(clienteActual));
    } else {
      localStorage.removeItem(pdvStorageKey);
    }
  }, [pdvStorageKey, clienteActual]);

  /**
   * Agrega un producto o incrementa su cantidad
   */
  const agregarItem = useCallback((producto, cantidad = 1) => {
    setItems((prev) => {
      const index = prev.findIndex((i) => i.id === producto.id);
      if (index >= 0) {
        const next = [...prev];
        next[index] = { ...next[index], cantidad: next[index].cantidad + cantidad };
        return next;
      }
      return [
        ...prev,
        {
          id: producto.id,
          nombre: producto.nombre,
          precio: producto.precioVigente || producto.precio || 0,
          presentacion: producto.presentacion || 'Unidad',
          img: producto.imagenUrl || null,
          cantidad,
        },
      ];
    });
  }, []);

  /**
   * Modifica la cantidad de un ítem existente (si llega a 0, se elimina)
   */
  const actualizarCantidad = useCallback((productoId, delta) => {
    setItems((prev) =>
      prev
        .map((item) => {
          if (item.id === productoId) {
            const nuevaCant = item.cantidad + delta;
            return nuevaCant > 0 ? { ...item, cantidad: nuevaCant } : null;
          }
          return item;
        })
        .filter(Boolean)
    );
  }, []);

  /**
   * Quita por completo un producto del carrito
   */
  const removerItem = useCallback((productoId) => {
    setItems((prev) => prev.filter((i) => i.id !== productoId));
  }, []);

  /**
   * Vacía el carrito
   */
  const vaciarCarrito = useCallback(() => {
    setItems([]);
    setCodigoPromo('');
  }, []);

  /**
   * Asigna el comercio/punto de venta activo para la toma de pedido
   */
  const seleccionarCliente = useCallback((pdv) => {
    setClienteActual(pdv);
  }, []);

  // Totales calculados
  const totalUnidades = useMemo(
    () => items.reduce((acc, item) => acc + item.cantidad, 0),
    [items]
  );

  const subtotal = useMemo(
    () => items.reduce((acc, item) => acc + item.precio * item.cantidad, 0),
    [items]
  );

  /**
   * Confirma y envía el pedido al backend
   */
  const confirmarPedido = useCallback(async (tipoPago = 'EFECTIVO') => {
    if (!clienteActual?.id) {
      throw new Error('Debes seleccionar un comercio o punto de venta para asignar el pedido.');
    }
    if (items.length === 0) {
      throw new Error('El carrito no contiene productos.');
    }

    const payload = {
      puntoDeVentaId: clienteActual.id,
      codigoPromocion: codigoPromo.trim() || null,
      tipoPago,
      detalles: items.map((item) => ({
        productoId: item.id,
        cantidad: item.cantidad,
      })),
    };

    const nuevoPedido = await pedidoService.crearPedido(payload);
    vaciarCarrito();
    return nuevoPedido;
  }, [clienteActual, items, codigoPromo, vaciarCarrito]);

  const value = {
    items,
    clienteActual,
    codigoPromo,
    setCodigoPromo,
    totalUnidades,
    subtotal,
    agregarItem,
    actualizarCantidad,
    removerItem,
    vaciarCarrito,
    seleccionarCliente,
    confirmarPedido,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart debe ser utilizado dentro de un CartProvider');
  }
  return context;
}
