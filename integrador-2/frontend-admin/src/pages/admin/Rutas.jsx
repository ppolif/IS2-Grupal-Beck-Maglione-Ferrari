import OSMMap from '../../components/OSMMap';
import { useRutas } from '../../hooks/useRutas';
import { getInitials } from '../../utils/formatters';
import { colors } from '../../theme/colors';

export default function Rutas() {
  const { tab, setTab, agents, loading, selectedAgentId, setSelectedAgentId } = useRutas();

  const sedeCentralLat = -32.889458;
  const sedeCentralLng = -68.845839;
  const sedeCentralNombre = 'Dulce Mayor - Sede Central';
  const sedeCentralDireccion = 'Av. San Martín 1020, Ciudad de Mendoza';

  const sedeCentralMarker = {
    id: 'sede-central-origen',
    nombre: sedeCentralNombre,
    comercio: sedeCentralNombre,
    direccion: sedeCentralDireccion,
    zona: 'Centro de Expedición',
    estado: 'Punto de Partida (Sede Central)',
    orden: 'S',
    lat: sedeCentralLat,
    lng: sedeCentralLng,
    color: '#247f99',
    esSucursal: true,
  };

  const agentMarkers = agents.map((a, idx) => ({
    id: a.id,
    nombre: a.name,
    zona: a.zona,
    direccion: a.zona,
    estado: `${a.stops} (${a.pct}%)`,
    lat: a.lat,
    lng: a.lng,
    orden: idx + 1,
  }));

  const mapMarkers =
    tab === 'distribuidores'
      ? [sedeCentralMarker, ...agentMarkers]
      : agentMarkers;

  return (
    <div className="space-y-6">
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-primary">
          Supervisión Logística
        </span>
        <h2 className="text-xl font-bold text-ink mt-0.5">
          Rutas del Día en Tiempo Real
        </h2>
        <p className="text-[12px] text-muted mt-0.5">
          Monitoreo del avance de visitas comerciales y entregas de vehículos en Gran Mendoza.
        </p>
      </div>

      <div className="bg-white rounded-[22px] border border-border-subtle shadow-sm overflow-hidden">
        {/* Tabs */}
        <div className="flex gap-8 px-6 pt-5 pb-3 border-b border-border-subtle text-[13px] font-bold">
          <button
            onClick={() => setTab('preventores')}
            className={`pb-2 transition-all cursor-pointer ${
              tab === 'preventores'
                ? 'text-primary border-b-2 border-primary'
                : 'text-muted-light hover:text-ink'
            }`}
          >
            Preventores en Calle
          </button>
          <button
            onClick={() => setTab('distribuidores')}
            className={`pb-2 transition-all cursor-pointer ${
              tab === 'distribuidores'
                ? 'text-primary border-b-2 border-primary'
                : 'text-muted-light hover:text-ink'
            }`}
          >
            Unidades de Distribución
          </button>
        </div>

        {/* Layout: Sidebar list + Map */}
        <div className="grid grid-cols-1 lg:grid-cols-12 min-h-[580px]">
          {/* Agent list */}
          <div className="lg:col-span-4 p-4 border-r border-border-subtle space-y-2 bg-[#fdfdfd]">
            {tab === 'distribuidores' && (
              <div
                onClick={() => setSelectedAgentId('sede-central-origen')}
                className={`flex items-start gap-3 p-3 rounded-[16px] border transition-all cursor-pointer mb-3 ${
                  selectedAgentId === 'sede-central-origen'
                    ? 'bg-[#eefaff] border-[#247f99] shadow-xs'
                    : 'bg-[#f4fbfe] border-[#cdecfb] hover:bg-[#edf8fd]'
                }`}
              >
                <div className="w-9 h-9 rounded-full bg-[#247f99] text-white font-bold text-[12px] flex items-center justify-center shrink-0">
                  S
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex justify-between items-baseline">
                    <strong className="text-[12px] text-[#0f344d] block">
                      {sedeCentralNombre}
                    </strong>
                    <span className="text-[9px] font-bold uppercase tracking-wider text-[#247f99] bg-[#d9f7ff] px-2 py-0.5 rounded-full">
                      Origen Común
                    </span>
                  </div>
                  <p className="text-[11px] text-[#426c88] mt-0.5">
                    {sedeCentralDireccion}
                  </p>
                </div>
              </div>
            )}

            {loading ? (
              <div className="py-10 text-center text-[12px] text-muted">
                Cargando rutas activas...
              </div>
            ) : agents.length === 0 ? (
              <div className="py-10 text-center text-[13px] text-muted px-3">
                No hay rutas de {tab === 'preventores' ? 'preventores' : 'distribución'} registradas para mostrar en esta sección.
              </div>
            ) : (
              agents.map((a) => {
                const isSelected = selectedAgentId === a.id;
                return (
                  <div
                    key={a.id}
                    onClick={() => setSelectedAgentId(a.id)}
                    className={`flex items-center gap-3.5 p-3.5 rounded-[16px] border transition-all cursor-pointer ${
                      isSelected
                        ? 'bg-offer-bg border-primary shadow-xs'
                        : 'bg-white border-border-subtle hover:border-secondary'
                    }`}
                  >
                    <div className="w-10 h-10 rounded-full bg-primary-soft text-primary font-bold text-[13px] flex items-center justify-center shrink-0">
                      {getInitials(a.name)}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex justify-between items-baseline mb-0.5">
                        <strong className="text-[13px] text-ink truncate block">
                          {a.name}
                        </strong>
                        <small className="text-[10px] text-muted font-medium">
                          {a.stops}
                        </small>
                      </div>
                      <span className="text-[10px] text-secondary font-medium block">
                        {a.zona}
                      </span>
                      <div className="h-1.5 bg-divider rounded-full mt-2 overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-primary to-secondary rounded-full"
                          style={{ width: `${a.pct}%` }}
                        />
                      </div>
                    </div>
                  </div>
                );
              })
            )}
          </div>

          {/* Map column with OpenStreetMap */}
          <div className="lg:col-span-8 p-4 bg-surface-note flex flex-col justify-center">
            <OSMMap
              className="h-[480px] w-full"
              markers={mapMarkers}
              activeMarkerId={selectedAgentId}
              routeColor={tab === 'preventores' ? colors.primary.DEFAULT : colors.secondary.DEFAULT}
              onMarkerClick={(m) => setSelectedAgentId(m.id)}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
