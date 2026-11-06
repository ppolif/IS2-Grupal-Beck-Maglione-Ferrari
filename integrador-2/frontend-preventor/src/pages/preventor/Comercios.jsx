import { useState } from 'react';
import { Link } from 'react-router-dom';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import { comerciosPreventorMock } from '../../mocks/preventorOrdersMock';

export default function Comercios() {
  const [comercios] = useState(comerciosPreventorMock);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedDay, setSelectedDay] = useState('TODOS');

  const days = ['TODOS', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'];

  const filtered = comercios.filter((c) => {
    const matchesSearch =
      c.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.direccion.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.cuit.includes(searchTerm);
    const matchesDay = selectedDay === 'TODOS' || c.diaVisita.includes(selectedDay);
    return matchesSearch && matchesDay;
  });

  return (
    <div className="space-y-5 max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Cartera de Clientes
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a] mt-0.5">
            Comercios Asignados
          </h1>
          <p className="text-[12px] text-[#6b6b80]">
            Puntos de venta asignados en tu zona de recorrido (Gran Mendoza).
          </p>
        </div>

        <Link to="/registro-comercio">
          <Button className="w-full sm:w-auto text-[12px] py-2.5">
            <Icon name="box" className="w-4 h-4" />
            <span>+ Nuevo Comercio</span>
          </Button>
        </Link>
      </div>

      {/* Buscador y filtro por día */}
      <div className="space-y-3">
        <div className="relative">
          <Icon name="search" className="w-4 h-4 text-[#aaa9ba] absolute left-3.5 top-3" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Buscar por nombre de comercio, CUIT o dirección..."
            className="w-full bg-white border border-[#ececf2] rounded-[14px] pl-10 pr-4 py-2.5 text-[12px] text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4] shadow-sm"
          />
        </div>

        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 no-scrollbar">
          {days.map((day) => (
            <button
              key={day}
              type="button"
              onClick={() => setSelectedDay(day)}
              className={`px-3 py-1.5 rounded-full text-[11px] font-semibold whitespace-nowrap transition-all cursor-pointer ${
                selectedDay === day
                  ? 'bg-[#2b2b3a] text-white shadow-sm'
                  : 'bg-white text-[#6b6b80] border border-[#ececf2] hover:bg-slate-50'
              }`}
            >
              {day}
            </button>
          ))}
        </div>
      </div>

      {/* Lista de Comercios */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {filtered.map((c) => (
          <div
            key={c.id}
            className="bg-white rounded-[20px] p-5 border border-[#ececf2] shadow-sm hover:border-[#d9d9e6] transition-all flex flex-col justify-between space-y-4"
          >
            <div>
              <div className="flex items-start justify-between gap-2 mb-2">
                <div>
                  <h3 className="text-base font-bold text-[#2b2b3a]">{c.nombre}</h3>
                  <span className="text-[11px] text-[#6b6b80]">Titular: {c.titular} (CUIT {c.cuit})</span>
                </div>
                <span className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full ${
                  c.estadoCuenta.includes('Al día')
                    ? 'bg-[#dff8e9] text-[#39855b]'
                    : 'bg-[#faf0d9] text-[#b88014]'
                }`}>
                  {c.estadoCuenta}
                </span>
              </div>

              <div className="space-y-1.5 text-[12px] text-[#6b6b80] pt-2 border-t border-[#f0f0f4]">
                <p className="flex items-center gap-1.5">
                  <Icon name="mapPin" className="w-3.5 h-3.5 text-[#3fb8d9]" />
                  <span>{c.direccion}, {c.localidad}</span>
                </p>
                <p className="flex items-center gap-1.5">
                  <Icon name="phone" className="w-3.5 h-3.5 text-[#7ed6a5]" />
                  <span>{c.telefono}</span>
                </p>
                <p className="text-[11px] text-[#aaa9ba]">
                  📅 Días habituales de visita: <strong>{c.diaVisita}</strong>
                </p>
                <p className="text-[11px] text-[#aaa9ba]">
                  📦 Último pedido registrado: <strong>{c.ultimoPedido}</strong>
                </p>
              </div>
            </div>

            <div className="pt-3 border-t border-[#f0f0f4] flex items-center justify-between gap-2">
              <Link
                to="/recorrido"
                className="text-[11px] font-bold text-[#3fb8d9] hover:underline flex items-center gap-1"
              >
                <Icon name="route" className="w-3.5 h-3.5" />
                <span>Ver en mapa</span>
              </Link>

              <Link to="/tienda">
                <Button className="text-[11px] px-3.5 py-2">
                  <Icon name="bag" className="w-3.5 h-3.5" />
                  <span>Tomar pedido</span>
                </Button>
              </Link>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
