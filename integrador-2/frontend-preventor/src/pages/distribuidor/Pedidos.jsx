/**
 * Hoja de Carga y Remitos de Entrega para el Distribuidor / Repartidor.
 * Conectado con la ruta diaria /api/v1/rutas-distribuidor/mi-ruta-del-dia.
 */
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import { rutaService } from '../../services/rutaService';

export default function Pedidos() {
  const [ruta, setRuta] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function cargarEntregas() {
      try {
        setCargando(true);
        setError(null);
        const data = await rutaService.obtenerMiRutaDistribuidorDelDia();
        setRuta(data);
      } catch (err) {
        console.error('Error al cargar entregas de distribuidor:', err);
        setError('No tenés una ruta de reparto asignada para hoy.');
      } finally {
        setCargando(false);
      }
    }
    cargarEntregas();
  }, []);

  const paradas = ruta?.paradas || [];
  const pendientes = paradas.filter((p) => !p.completada).length;
  const completadas = paradas.filter((p) => p.completada).length;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#3fb8d9]">
            Hoja de Ruta del Repartidor
          </span>
          <h1 className="text-2xl font-bold text-[#2b2b3a] mt-0.5">
            {paradas.length} paradas asignadas
          </h1>
          <p className="text-[12px] text-[#6b6b80] mt-0.5">
            {ruta?.responsable ? `Distribuidor: ${ruta.responsable.nombre || ''}` : 'Reparto programado'} · {completadas} entregadas, {pendientes} pendientes
          </p>
        </div>

        <Link to="/recorrido">
          <Button className="text-[12px]">
            <Icon name="route" className="w-4 h-4" />
            <span>Ver Mapa de Ruta</span>
          </Button>
        </Link>
      </div>

      {cargando && (
        <div className="flex justify-center items-center py-16">
          <div className="w-8 h-8 border-3 border-[#3fb8d9] border-t-transparent rounded-full animate-spin"></div>
        </div>
      )}

      {error && (
        <div className="p-6 bg-white rounded-[20px] border border-[#ececf2] text-center space-y-2">
          <span className="text-3xl">🚚</span>
          <h3 className="font-bold text-[#2b2b3a] text-lg">Sin entregas pendientes</h3>
          <p className="text-[12px] text-[#6b6b80] max-w-sm mx-auto">{error}</p>
        </div>
      )}

      {!cargando && !error && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {paradas.map((p) => {
            const pdv = p.puntoDeVenta;
            const dir = pdv?.direccion ? `${pdv.direccion.calle || ''} ${pdv.direccion.numero || ''}` : 'Mendoza';

            return (
              <div
                key={p.orden}
                className="bg-white border border-[#ececf2] rounded-[18px] p-4 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow"
              >
                <div>
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <span className="text-[10px] font-bold text-[#3fb8d9] tracking-wider uppercase">
                      Parada #{p.orden} {p.pedidoId ? `· Pedido #${p.pedidoId}` : ''}
                    </span>
                    <strong
                      className={`text-[10px] font-bold px-2.5 py-1 rounded-full ${
                        p.completada
                          ? 'bg-[#dff8e9] text-[#39855b]'
                          : 'bg-[#fff0d3] text-[#986b16]'
                      }`}
                    >
                      {p.completada ? 'Entregado ✓' : 'Pendiente'}
                    </strong>
                  </div>

                  <h3 className="font-bold text-[15px] text-[#2b2b3a] leading-tight">
                    {pdv?.nombreComercio}
                  </h3>
                  <p className="text-[11px] text-[#6b6b80] mt-0.5">
                    {dir}
                  </p>
                </div>

                <div className="flex items-center justify-between mt-3 pt-3 border-t border-[#f0f0f4] text-[11px]">
                  <span className="text-[#aaa9ba]">Secuencia de parada {p.orden}</span>
                  <Link
                    to="/recorrido"
                    className="text-[#3fb8d9] font-semibold hover:underline flex items-center gap-1"
                  >
                    <span>Ver en mapa</span>
                    <span>→</span>
                  </Link>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
