import { useState, useEffect } from 'react';
import Icon from './Icon';
import { apiFetch } from '../services/api';

export default function Kpis({ warehouse = false }) {
  const [stats, setStats] = useState({
    pedidosHoy: 0,
    stockBajo: 0,
    sinStock: 0,
    enDistribucion: 0,
    ventasMes: 0,
    totalPedidos: 0,
    clientesActivos: 0,
    ticketPromedio: 0,
  });

  useEffect(() => {
    let mounted = true;
    async function cargarKpis() {
      try {
        const [pedRes, prodRes, minRes, factRes] = await Promise.all([
          apiFetch('/pedidos?size=200', { method: 'GET' }).catch(() => ({ elementos: [] })),
          apiFetch('/productos?size=200', { method: 'GET' }).catch(() => ({ elementos: [] })),
          apiFetch('/minoristas?size=200', { method: 'GET' }).catch(() => ({ elementos: [] })),
          apiFetch('/facturas?size=200', { method: 'GET' }).catch(() => ({ elementos: [] })),
        ]);

        if (!mounted) return;

        const pedidos = pedRes.elementos || pedRes.content || (Array.isArray(pedRes) ? pedRes : []);
        const productos = prodRes.elementos || prodRes.content || (Array.isArray(prodRes) ? prodRes : []);
        const minoristas = minRes.elementos || minRes.content || (Array.isArray(minRes) ? minRes : []);
        const facturas = factRes.elementos || factRes.content || (Array.isArray(factRes) ? factRes : []);

        const hoyStr = new Date().toISOString().split('T')[0];
        const pedidosHoy = pedidos.filter((p) => String(p.fechaHoraCarga || '').startsWith(hoyStr)).length;
        const stockBajo = productos.filter((p) => Number(p.stockActual || 0) > 0 && Number(p.stockActual || 0) <= 15).length;
        const sinStock = productos.filter((p) => Number(p.stockActual || 0) === 0).length;
        const enDistribucion = pedidos.filter((p) => p.estado === 'EN_REPARTO').length;

        const facturasActivas = facturas.filter((f) => f.estado !== 'ANULADA');
        const ventasMes = facturasActivas.reduce((acc, f) => acc + Number(f.total || 0), 0);
        const pedidosValidos = pedidos.filter((p) => p.estado !== 'ANULADO');
        const ticketPromedio =
          pedidosValidos.length > 0
            ? Math.round(pedidosValidos.reduce((acc, p) => acc + Number(p.total || 0), 0) / pedidosValidos.length)
            : 0;

        setStats({
          pedidosHoy,
          stockBajo,
          sinStock,
          enDistribucion,
          ventasMes,
          totalPedidos: pedidosValidos.length,
          clientesActivos: minoristas.length,
          ticketPromedio,
        });
      } catch (err) {
        console.error('Error al cargar KPIs:', err);
      }
    }
    cargarKpis();
    return () => {
      mounted = false;
    };
  }, []);

  const items = warehouse
    ? [
        { label: 'Pedidos hoy', val: String(stats.pedidosHoy), badge: 'Del día', icon: 'chart', isBlue: false },
        { label: 'Stock bajo', val: String(stats.stockBajo), badge: '≤ 15 u.', icon: 'bag', isBlue: true },
        { label: 'Sin stock', val: String(stats.sinStock), badge: 'Agotados', icon: 'box', isBlue: false },
        { label: 'En distribución', val: String(stats.enDistribucion), badge: 'En reparto', icon: 'route', isBlue: true },
      ]
    : [
        {
          label: 'Ventas facturadas',
          val: `$${stats.ventasMes.toLocaleString('es-AR')}`,
          badge: 'Acumulado',
          icon: 'chart',
          isBlue: false,
        },
        { label: 'Pedidos', val: String(stats.totalPedidos), badge: 'Registrados', icon: 'bag', isBlue: true },
        { label: 'Clientes activos', val: String(stats.clientesActivos), badge: 'Minoristas', icon: 'user', isBlue: false },
        {
          label: 'Ticket promedio',
          val: `$${stats.ticketPromedio.toLocaleString('es-AR')}`,
          badge: 'Por pedido',
          icon: 'box',
          isBlue: true,
        },
      ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3.5">
      {items.map((x) => (
        <div
          key={x.label}
          className="bg-white rounded-[18px] p-4 sm:p-4.5 border border-[#ececf2] shadow-sm flex items-center gap-3.5"
        >
          <span
            className={`w-[45px] h-[45px] rounded-[14px] flex items-center justify-center shrink-0 ${
              x.isBlue
                ? 'bg-[#e1f8fd] text-[#3fb8d9]'
                : 'bg-[#ffe9f9] text-[#e86bc4]'
            }`}
          >
            <Icon name={x.icon} className="w-5 h-5" />
          </span>
          <div className="flex flex-col">
            <small className="text-[10px] text-[#9b99a8] font-medium leading-none">
              {x.label}
            </small>
            <b className="text-[19px] text-[#2b2b3a] font-bold mt-1 leading-tight">
              {x.val}
            </b>
            <em className="not-italic text-[9px] font-semibold text-[#40a56a] mt-0.5">
              {x.badge}
            </em>
          </div>
        </div>
      ))}
    </div>
  );
}
