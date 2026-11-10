import { useState, useEffect, useCallback } from 'react';
import { apiFetch } from '../services/api';

const PALETTE = ['#e86bc4', '#3fb8d9', '#f7b267', '#7bd389', '#9b87f5', '#ff8fa3'];

export function useEstadisticas() {
  const [loading, setLoading] = useState(true);
  const [annualGrowthData, setAnnualGrowthData] = useState([]);
  const [weeklyActivityChart, setWeeklyActivityChart] = useState([]);
  const [categoryDonutChart, setCategoryDonutChart] = useState([]);
  const [summary, setSummary] = useState({
    ticketPromedio: 0,
    efectividadPreventa: 0,
    cumplimientoEntrega: 0,
  });

  const cargarEstadisticas = useCallback(async () => {
    setLoading(true);
    try {
      const [pedRes, factRes, stockRes] = await Promise.all([
        apiFetch('/pedidos?size=200', { method: 'GET' }).catch(() => ({ elementos: [] })),
        apiFetch('/facturas?size=200', { method: 'GET' }).catch(() => ({ elementos: [] })),
        apiFetch('/estadisticas/stock-por-producto', { method: 'GET' }).catch(() => []),
      ]);

      const pedidos = pedRes.elementos || pedRes.content || (Array.isArray(pedRes) ? pedRes : []);
      const facturas = factRes.elementos || factRes.content || (Array.isArray(factRes) ? factRes : []);
      const stockItems = Array.isArray(stockRes) ? stockRes : [];

      const pedidosValidos = pedidos.filter((p) => p.estado !== 'ANULADO');
      const entregados = pedidosValidos.filter((p) => p.estado === 'ENTREGADO');
      const ticketPromedio =
        pedidosValidos.length > 0
          ? Math.round(pedidosValidos.reduce((acc, p) => acc + Number(p.total || 0), 0) / pedidosValidos.length)
          : 0;
      const cumplimientoEntrega =
        pedidosValidos.length > 0 ? Math.round((entregados.length / pedidosValidos.length) * 100) : 0;

      setSummary({
        ticketPromedio,
        efectividadPreventa: pedidosValidos.length > 0 ? 100 : 0,
        cumplimientoEntrega,
      });

      // Evolución mensual a partir de facturas reales
      const mesesNombres = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
      const porMes = {};
      facturas
        .filter((f) => f.estado !== 'ANULADA' && f.fechaFactura)
        .forEach((f) => {
          const d = new Date(f.fechaFactura);
          if (!isNaN(d)) {
            const key = `${mesesNombres[d.getMonth()]} ${d.getFullYear()}`;
            porMes[key] = (porMes[key] || 0) + Number(f.total || 0);
          }
        });
      const growthArr = Object.entries(porMes).map(([mes, total]) => ({
        mes,
        facturado: Number(total.toFixed(2)),
      }));
      setAnnualGrowthData(growthArr);

      // Actividad por día de semana a partir de pedidos reales
      if (pedidosValidos.length > 0) {
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
        setWeeklyActivityChart([]);
      }

      // Stock por producto / familia
      const conStock = stockItems.filter((s) => Number(s.cantidad || 0) > 0);
      if (conStock.length > 0) {
        setCategoryDonutChart(
          conStock.map((s, idx) => ({
            name: s.productoNombre || s.codigo || 'Producto',
            value: Number(s.cantidad || 0),
            color: PALETTE[idx % PALETTE.length],
          }))
        );
      } else {
        setCategoryDonutChart([]);
      }
    } catch (err) {
      console.error('Error al cargar estadísticas:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarEstadisticas();
  }, [cargarEstadisticas]);

  return {
    loading,
    summary,
    annualGrowthData,
    weeklyActivityChart,
    categoryDonutChart,
  };
}
