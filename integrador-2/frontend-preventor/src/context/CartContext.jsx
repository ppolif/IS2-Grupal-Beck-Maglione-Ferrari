/**
 * Contexto global del Carrito de Compras para el Preventista.
 * Permite seleccionar el comercio/punto de venta actual, agregar productos,
 * modificar cantidades y confirmar la orden emitiéndola hacia el backend.
 */
import { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react';
import { pedidoService } from '../services/pedidoService';

const CartContext = createContext(null);

const STORAGE_KEY = 'dm_preventor_cart';
const PDV_KEY = 'dm_preventor_active_pdv';

export function CartProvider({ children }) {
  // Ítems en el carrito: [{ id, nombre, precio, cantidad, presentacion, img }]
  const [items, setItems] = useState(() => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  });

  // Punto de venta seleccionado para el que se está tomando el pedido
  const [clienteActual, setClienteActual] = useState(() => {
    try {
      const raw = localStorage.getItem(PDV_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  });

  const [codigoPromo, setCodigoPromo] = useState('');

  // Persistir en localStorage
  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  }, [items]);

  useEffect(() => {
    if (clienteActual) {
      localStorage.setItem(PDV_KEY, JSON.stringify(clienteActual));
    } else {
      localStorage.removeItem(PDV_KEY);
    }
  }, [clienteActual]);

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
  const confirmarPedido = useCallback(async () => {
    if (!clienteActual?.id) {
      throw new Error('Debes seleccionar un comercio o punto de venta para asignar el pedido.');
    }
    if (items.length === 0) {
      throw new Error('El carrito no contiene productos.');
    }

    const payload = {
      puntoDeVentaId: clienteActual.id,
      codigoPromocion: codigoPromo.trim() || null,
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
