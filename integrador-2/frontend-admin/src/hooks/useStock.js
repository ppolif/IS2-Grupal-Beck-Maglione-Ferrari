import { useState, useEffect, useCallback } from 'react';
import { productoService } from '../services/productoService';
import { stockService } from '../services/stockService';

/**
 * Custom hook managing inventory stock list, filtering and physical adjustments.
 */
export function useStock() {
  const [stockList, setStockList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('todos');
  const [isAdjustmentModal, setIsAdjustmentModal] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [adjustAmount, setAdjustAmount] = useState('');

  const cargarStock = useCallback(async () => {
    setLoading(true);
    try {
      const data = await productoService.listar({ size: 200 });
      const items = data.elementos || data.content || (Array.isArray(data) ? data : []);
      const mapped = items.map((p) => ({
        id: p.id,
        codigo: p.codigo,
        nombre: p.nombre,
        categoria: p.categoriaNombre || p.subCategoriaNombre || 'Sin categoría',
        presentacion: p.presentacion || '-',
        stock: Number(p.stockActual || 0),
      }));
      setStockList(mapped);
    } catch (err) {
      console.error('Error al cargar inventario:', err);
      setStockList([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarStock();
  }, [cargarStock]);

  const lowStockCount = stockList.filter((item) => item.stock > 0 && item.stock <= 15).length;
  const zeroStockCount = stockList.filter((item) => item.stock === 0).length;

  const filtered = stockList.filter((item) => {
    if (filter === 'bajo') return item.stock > 0 && item.stock <= 15;
    if (filter === 'cero') return item.stock === 0;
    return true;
  });

  const handleOpenAdjust = (p) => {
    setSelectedProduct(p);
    setAdjustAmount(p.stock.toString());
    setIsAdjustmentModal(true);
  };

  const handleSaveAdjust = async (e) => {
    e.preventDefault();
    if (!selectedProduct) return;
    try {
      const cantidad = parseInt(adjustAmount, 10);
      if (isNaN(cantidad) || cantidad < 0) {
        alert('Ingresá una cantidad válida mayor o igual a 0.');
        return;
      }
      await stockService.modificarStock(selectedProduct.id, cantidad, 'Ajuste físico de inventario');
      setIsAdjustmentModal(false);
      await cargarStock();
    } catch (err) {
      alert(`Error al ajustar stock: ${err.message}`);
    }
  };

  return {
    stockList,
    loading,
    lowStockCount,
    zeroStockCount,
    filter,
    setFilter,
    isAdjustmentModal,
    setIsAdjustmentModal,
    selectedProduct,
    adjustAmount,
    setAdjustAmount,
    filtered,
    handleOpenAdjust,
    handleSaveAdjust,
  };
}
