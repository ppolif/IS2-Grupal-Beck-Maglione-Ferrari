import { useState } from 'react';
import { productsStockMock } from '../mocks/products';

/**
 * Custom hook managing inventory stock list, filtering and physical adjustments.
 */
export function useStock() {
  const [stockList, setStockList] = useState(productsStockMock);
  const [filter, setFilter] = useState('todos');
  const [isAdjustmentModal, setIsAdjustmentModal] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [adjustAmount, setAdjustAmount] = useState('');

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

  const handleSaveAdjust = (e) => {
    e.preventDefault();
    if (selectedProduct) {
      setStockList((prev) =>
        prev.map((item) =>
          item.id === selectedProduct.id
            ? { ...item, stock: parseInt(adjustAmount, 10) || 0 }
            : item
        )
      );
      alert(`Stock de "${selectedProduct.nombre}" ajustado a ${adjustAmount} unidades.`);
      setIsAdjustmentModal(false);
    }
  };

  return {
    stockList,
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
