import { useState } from 'react';
import { Link } from 'react-router-dom';
import Chip from '../../components/Chip';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import { deliveriesMock } from '../../mocks/deliveries';

export default function Pedidos() {
  const [filter, setFilter] = useState('todos');

  const filtered = deliveriesMock.filter((d) => {
    if (filter === 'cobrar') return d.estadoPago === 'cash';
    if (filter === 'pagados') return d.estadoPago === 'paid';
    return true;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Hoy · 14 Jun
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a] mt-0.5">
            10 entregas asignadas
          </h1>
          <p className="text-[12px] text-[#6b6b80] mt-0.5">
            Unidad 08 · Reparto programado Gran Mendoza
          </p>
        </div>

        <Link to="/recorrido">
          <Button className="text-[12px]">
            <Icon name="route" className="w-4 h-4" />
            <span>Ver Mapa de Ruta</span>
          </Button>
        </Link>
      </div>

      {/* Filter tabs */}
      <div className="flex items-center gap-2">
        <button
          onClick={() => setFilter('todos')}
          className={`px-3 py-1.5 rounded-full text-[11px] font-semibold transition-colors cursor-pointer ${
            filter === 'todos'
              ? 'bg-[#e86bc4] text-white shadow-sm'
              : 'bg-white border border-[#ececf2] text-[#6b6b80] hover:bg-slate-50'
          }`}
        >
          Todas (10)
        </button>
        <button
          onClick={() => setFilter('cobrar')}
          className={`px-3 py-1.5 rounded-full text-[11px] font-semibold transition-colors cursor-pointer ${
            filter === 'cobrar'
              ? 'bg-[#ffd98a] text-[#7d5707] shadow-sm'
              : 'bg-white border border-[#ececf2] text-[#6b6b80] hover:bg-slate-50'
          }`}
        >
          Cobro en Efectivo (4)
        </button>
        <button
          onClick={() => setFilter('pagados')}
          className={`px-3 py-1.5 rounded-full text-[11px] font-semibold transition-colors cursor-pointer ${
            filter === 'pagados'
              ? 'bg-[#7ed6a5] text-[#1c643b] shadow-sm'
              : 'bg-white border border-[#ececf2] text-[#6b6b80] hover:bg-slate-50'
          }`}
        >
          Ya Pagados (6)
        </button>
      </div>

      {/* Deliveries list matching prototype order-card */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {filtered.map((d) => (
          <div
            key={d.id}
            className="bg-white border border-[#ececf2] rounded-[18px] p-4 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow"
          >
            <div>
              <div className="flex items-start justify-between gap-2 mb-2">
                <span className="text-[10px] font-bold text-[#e86bc4] tracking-wider uppercase">
                  #{d.id}
                </span>
                <strong
                  className={`text-[10px] font-bold px-2.5 py-1 rounded-full ${
                    d.estadoPago === 'cash'
                      ? 'bg-[#fff0d3] text-[#986b16]'
                      : 'bg-[#dff8e9] text-[#39855b]'
                  }`}
                >
                  {d.estadoPago === 'cash'
                    ? `Cobrar ${d.monto}`
                    : 'Transferencia · pagado'}
                </strong>
              </div>

              <h3 className="font-bold text-[15px] text-[#2b2b3a] leading-tight">
                {d.comercio}
              </h3>
              <p className="text-[11px] text-[#6b6b80] mt-0.5">
                {d.direccion}
              </p>
              <div className="mt-2 text-[10px] text-[#6b6b80] bg-[#fafafd] p-2 rounded-[10px] border border-[#ececf2]">
                <strong>Contenido:</strong> {d.items} ({d.bultos} bultos)
              </div>
            </div>

            <div className="flex items-center justify-between mt-3 pt-3 border-t border-[#f0f0f4] text-[11px]">
              <span className="text-[#6b6b80]">{d.distancia}</span>
              <Link
                to="/recorrido"
                className="text-[#3fb8d9] font-semibold hover:underline flex items-center gap-1"
              >
                <span>Ver en mapa</span>
                <span>→</span>
              </Link>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

