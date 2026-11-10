import { useState, useEffect, useCallback } from 'react';
import { apiFetch } from '../services/api';

const PALETTE = ['#e86bc4', '#3fb8d9', '#f7b267', '#7bd389', '#9b87f5', '#ff8fa3'];

/**
 * Custom hook providing real backend data for the Executive Dashboard.
 */
export function useDashboard() {
  const [loading, setLoading] = useState(true);
  const [recentOrders, setRecentOrders] = useState([]);
  const [monthlySalesChart, setMonthlySalesChart] = useState([]);
  const [weeklyActivityChart, setWeeklyActivityChart] = useState([]);
  const [categoryDonutChart, setCategoryDonutChart] = useState([]);
  const [totalUnidadesStock, setTotalUnidadesStock] = useState(0);

  const cargarDashboard = useCallback(async () => {
    setLoading(true);
    try {
      const [pedRes, prodRes] = await Promise.all([
        apiFetch('/pedidos?size=50', { method: 'GET' }).catch(() => ({ elementos: [] })),
        apiFetch('/productos?size=200', { method: 'GET' }).catch(() => ({ elementos: [] })),
      ]);

      const pedidos = pedRes.elementos || pedRes.content || (Array.isArray(pedRes) ? pedRes : []);
      const productos = prodRes.elementos || prodRes.content || (Array.isArray(prodRes) ? prodRes : []);

      // Últimos 5 pedidos reales
      const recents = pedidos.slice(0, 5).map((p) => ({
        id: p.numeroPedido || p.id,
        comercio: p.puntoDeVenta?.nombreComercio || 'Punto de Venta',
        preventor: p.preventor
          ? `${p.preventor.nombre || ''} ${p.preventor.apellido || ''}`.trim()
          : 'Venta Directa / Web',
        monto: `$${Number(p.total || 0).toLocaleString('es-AR')}`,
        estado: p.estado || 'CARGADO',
      }));
      setRecentOrders(recents);

      // Ventas por fecha de pedidos válidos
      const pedidosValidos = pedidos.filter((p) => p.estado !== 'ANULADO');
      if (pedidosValidos.length > 0) {
        const ventasPorDia = {};
        pedidosValidos.forEach((p) => {
          const dia = p.fechaHoraCarga
            ? new Date(p.fechaHoraCarga).toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit' })
            : 'Hoy';
          ventasPorDia[dia] = (ventasPorDia[dia] || 0) + Number(p.total || 0);
        });
        setMonthlySalesChart(
          Object.entries(ventasPorDia).map(([dia, ventas]) => ({ dia, ventas }))
        );

        const diasNombres = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];
        const conteoDias = { Lun: 0, Mar: 0, Mié: 0, Jue: 0, Vie: 0, Sáb: 0, Dom: 0 };
        pedidosValidos.forEach((p) => {
          if (p.fechaHoraCarga) {
            const d = new Date(p.fechaHoraCarga);
            if (!isNaN(d)) {
              const nombre = diasNombres[d.getDay()];
              conteoDias[nombre] = (conteoDias[nombre] || 0) + 1;
            }
          }
        });
        setWeeklyActivityChart(
          Object.entries(conteoDias).map(([dia, cant]) => ({ dia, pedidos: cant }))
        );
      } else {
        setMonthlySalesChart([]);
        setWeeklyActivityChart([]);
      }

      // Stock por categoría
      const totalStock = productos.reduce((acc, p) => acc + Number(p.stockActual || 0), 0);
      setTotalUnidadesStock(totalStock);

      if (totalStock > 0) {
        const porCat = {};
        productos.forEach((p) => {
          const cant = Number(p.stockActual || 0);
          if (cant > 0) {
            const cat = p.categoriaNombre || p.subCategoriaNombre || 'General';
            porCat[cat] = (porCat[cat] || 0) + cant;
          }
        });
        setCategoryDonutChart(
          Object.entries(porCat).map(([name, unidades], idx) => ({
            name,
            value: Math.round((unidades / totalStock) * 100),
            unidades,
            color: PALETTE[idx % PALETTE.length],
          }))
        );
      } else {
        setCategoryDonutChart([]);
      }
    } catch (err) {
      console.error('Error al cargar dashboard:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarDashboard();
  }, [cargarDashboard]);

  return {
    loading,
    recentOrders,
    monthlySalesChart,
    weeklyActivityChart,
    categoryDonutChart,
    totalUnidadesStock,
  };
}
