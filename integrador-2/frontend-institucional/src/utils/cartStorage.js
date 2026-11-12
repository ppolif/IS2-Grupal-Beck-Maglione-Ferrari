const CART_STORAGE_KEY = 'dm_institucional_cart';
const PROMO_STORAGE_KEY = 'dm_institucional_promo';

function getActiveUserScope() {
  try {
    const raw = localStorage.getItem('dm_institucional_usuario');
    if (!raw) return 'anon';
    const parsed = JSON.parse(raw);
    return parsed?.id || parsed?.nombreUsuario || 'anon';
  } catch {
    return 'anon';
  }
}

function getCartKey() {
  return `${CART_STORAGE_KEY}_${getActiveUserScope()}`;
}

function getPromoKey() {
  return `${PROMO_STORAGE_KEY}_${getActiveUserScope()}`;
}

const DEFAULT_IMG =
  'https://images.unsplash.com/photo-1582058091505-f87a2e55a40f?auto=format&fit=crop&w=1000&q=85';

export function getProductImageUrl(imagenId) {
  if (!imagenId) return DEFAULT_IMG;
  if (imagenId.startsWith('http://') || imagenId.startsWith('https://')) return imagenId;
  const base = import.meta.env.VITE_API_URL || '/api/v1';
  return `${base}/imagenes/${imagenId}/contenido`;
}

export function mapPublicProduct(p) {
  const precioNum = Number(p.precio ?? p.precioVigente ?? 0);
  return {
    id: p.id,
    codigo: p.codigo || '',
    nombre: p.nombre || 'Producto',
    descripcion: p.descripcion || '',
    presentacion: p.presentacion || 'Unidad mayorista',
    unidades: Number(p.unidadesPorBulto || 1),
    categoria: p.categoriaNombre || p.subCategoriaNombre || 'Catálogo',
    precioNum,
    precio: `$${precioNum.toLocaleString('es-AR')}`,
    imagen: getProductImageUrl(p.imagenId),
    enOferta: Boolean(p.enOferta),
  };
}

export const cartStorage = {
  getItems() {
    try {
      const raw = localStorage.getItem(getCartKey());
      if (!raw) return [];
      const parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  },

  setItems(items) {
    try {
      localStorage.setItem(getCartKey(), JSON.stringify(items));
      window.dispatchEvent(new Event('dm_cart_updated'));
    } catch (e) {
      console.error('Error guardando carrito:', e);
    }
  },

  addItem(product, cantidad = 1) {
    const items = this.getItems();
    const idx = items.findIndex((it) => it.producto.id === product.id);
    if (idx >= 0) {
      items[idx] = {
        ...items[idx],
        cantidad: items[idx].cantidad + cantidad,
      };
    } else {
      items.push({
        producto: {
          id: product.id,
          nombre: product.nombre,
          presentacion: product.presentacion || 'Bulto mayorista',
          categoria: product.categoria || 'Catálogo',
          precioNum: Number(product.precioNum ?? product.precio ?? 0),
          precio:
            typeof product.precio === 'string'
              ? product.precio
              : `$${Number(product.precio ?? product.precioNum ?? 0).toLocaleString('es-AR')}`,
          imagen: product.imagen || DEFAULT_IMG,
        },
        cantidad,
      });
    }
    this.setItems(items);
  },

  clear() {
    localStorage.removeItem(getCartKey());
    localStorage.removeItem(getPromoKey());
    window.dispatchEvent(new Event('dm_cart_updated'));
  },

  getPromo() {
    try {
      const raw = localStorage.getItem(getPromoKey());
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  },

  setPromo(promo) {
    if (!promo) {
      localStorage.removeItem(getPromoKey());
    } else {
      localStorage.setItem(getPromoKey(), JSON.stringify(promo));
    }
    window.dispatchEvent(new Event('dm_cart_updated'));
  },
};
