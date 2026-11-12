/**
 * Directorio de Comercios / Puntos de Venta para el Preventista.
 * Conectado con /api/v1/puntos-de-venta del backend.
 * Permite seleccionar el comercio activo para tomar pedidos desde el catálogo.
 */
import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import { comercioService } from '../../services/comercioService';
import { useCart } from '../../context/CartContext';

export default function Comercios() {
  const { seleccionarCliente, clienteActual } = useCart();
  const navigate = useNavigate();
  const [comercios, setComercios] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function cargarComercios() {
      try {
        setCargando(true);
        setError(null);
        const data = await comercioService.listarPuntosDeVenta({ size: 100 });
        const list = Array.isArray(data) ? data : (data?.content || []);
        setComercios(list);
      } catch (err) {
        console.error('Error al listar comercios:', err);
        setError(
          err.code === 'CAMBIO_CLAVE_REQUERIDO' || err.status === 403
            ? 'Cambie su clave para acceder'
            : 'No se pudieron obtener los comercios asignados.'
        );
      } finally {
        setCargando(false);
      }
    }
    cargarComercios();
  }, []);

  const handleTomarPedido = (pdv) => {
    seleccionarCliente(pdv);
    navigate('/tienda');
  };

  const filtered = comercios.filter((c) => {
    const term = searchTerm.toLowerCase();
    const nombre = c.nombreComercio || '';
    const titular = c.minorista ? `${c.minorista.nombre || ''} ${c.minorista.apellido || ''}` : '';
    const cuit = c.minorista?.cuit || c.minorista?.numeroDocumento || '';
    const dir = c.direccion ? `${c.direccion.calle || ''} ${c.direccion.localidad?.nombre || ''}` : '';

    return (
      nombre.toLowerCase().includes(term) ||
      titular.toLowerCase().includes(term) ||
      cuit.includes(term) ||
      dir.toLowerCase().includes(term)
    );
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
            Puntos de venta habilitados en el sistema central para toma de pedidos.
          </p>
        </div>

        <Link to="/registro-comercio">
          <Button className="w-full sm:w-auto text-[12px] py-2.5">
            <Icon name="box" className="w-4 h-4" />
            <span>+ Nuevo Comercio</span>
          </Button>
        </Link>
      </div>

      {/* Buscador */}
      <div className="relative">
        <Icon name="search" className="w-4 h-4 text-[#aaa9ba] absolute left-3.5 top-3" />
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="Buscar por nombre de comercio, titular, CUIT o calle..."
          className="w-full bg-white border border-[#ececf2] rounded-[14px] pl-10 pr-4 py-2.5 text-[12px] text-[#2b2b3a] focus:outline-none focus:border-[#e86bc4] shadow-sm"
        />
      </div>

      {/* Estados de carga / error */}
      {cargando && (
        <div className="flex justify-center items-center py-12">
          <div className="w-8 h-8 border-3 border-[#e86bc4] border-t-transparent rounded-full animate-spin"></div>
        </div>
      )}

      {error && (
        error === 'Cambie su clave para acceder' ? (
          <div className="max-w-md mx-auto my-8 p-8 bg-white rounded-[24px] border border-[#ececf2] text-center space-y-4 shadow-sm">
            <div className="w-16 h-16 rounded-full bg-[#eefaff] text-[#247f99] flex items-center justify-center mx-auto text-3xl font-bold">
              🔒
            </div>
            <h2 className="text-xl font-bold text-[#2b2b3a]">Cambie su clave para acceder</h2>
            <p className="text-[13px] text-[#6b6b80] leading-relaxed">
              Su usuario posee una contraseña temporal por defecto. Por razones de seguridad, debe actualizar su clave antes de operar en esta sección.
            </p>
            <Link
              to="/cambiar-password"
              className="inline-flex items-center justify-center w-full py-3 px-4 rounded-[14px] bg-[#247f99] text-white font-bold text-[13px] hover:bg-[#1c667b] transition-colors shadow-sm"
            >
              Cambiar contraseña ahora →
            </Link>
          </div>
        ) : (
          <div className="p-4 bg-[#ffe8ed] border border-[#ffccd5] rounded-[16px] text-[12px] text-[#e5484d]">
            {error}
          </div>
        )
      )}

      {/* Lista de Comercios */}
      {!cargando && !error && (
        <>
          {filtered.length === 0 ? (
            <div className="text-center py-12 bg-white rounded-[20px] border border-[#ececf2]">
              <p className="text-[#6b6b80] text-[13px]">No se encontraron comercios registrados.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {filtered.map((c) => {
                const isSelected = clienteActual?.id === c.id;
                const titular = c.minorista ? `${c.minorista.nombre || ''} ${c.minorista.apellido || ''}` : 'Sin titular';
                const doc = c.minorista?.cuit || c.minorista?.numeroDocumento || 'S/D';
                const calle = c.direccion ? `${c.direccion.calle || ''} ${c.direccion.numero || ''}` : 'Sin dirección informada';
                const localidad = c.direccion?.localidad?.nombre || 'Mendoza';

                return (
                  <div
                    key={c.id}
                    className={`bg-white rounded-[20px] p-5 border shadow-sm transition-all flex flex-col justify-between space-y-4 ${
                      isSelected ? 'border-[#e86bc4] ring-2 ring-[#e86bc4]/20' : 'border-[#ececf2] hover:border-[#d9d9e6]'
                    }`}
                  >
                    <div>
                      <div className="flex items-start justify-between gap-2 mb-2">
                        <div>
                          <h3 className="text-base font-bold text-[#2b2b3a]">{c.nombreComercio}</h3>
                          <span className="text-[11px] text-[#6b6b80]">
                            Titular: {titular} (Doc/CUIT: {doc})
                          </span>
                        </div>
                        <span className="text-[10px] font-semibold px-2.5 py-0.5 rounded-full bg-[#dff8e9] text-[#39855b]">
                          Activo
                        </span>
                      </div>

                      <div className="space-y-1.5 text-[12px] text-[#6b6b80] pt-2 border-t border-[#f0f0f4]">
                        <p className="flex items-center gap-1.5">
                          <Icon name="mapPin" className="w-3.5 h-3.5 text-[#3fb8d9]" />
                          <span>{calle}, {localidad}</span>
                        </p>
                        {c.direccion?.telefono && (
                          <p className="flex items-center gap-1.5">
                            <Icon name="phone" className="w-3.5 h-3.5 text-[#7ed6a5]" />
                            <span>{c.direccion.telefono}</span>
                          </p>
                        )}
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

                      <Button
                        onClick={() => handleTomarPedido(c)}
                        className={`text-[11px] px-3.5 py-2 cursor-pointer ${
                          isSelected ? 'bg-[#39855b] hover:bg-[#2c6e49]' : ''
                        }`}
                      >
                        <Icon name="bag" className="w-3.5 h-3.5" />
                        <span>{isSelected ? 'Comercio Activo ✓' : 'Tomar pedido'}</span>
                      </Button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </>
      )}
    </div>
  );
}
