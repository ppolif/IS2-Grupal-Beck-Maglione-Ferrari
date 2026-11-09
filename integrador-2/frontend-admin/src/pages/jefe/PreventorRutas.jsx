import OSMMap from '../../components/OSMMap';
import Button from '../../components/Button';
import Chip from '../../components/Chip';
import { usePreventorRutas } from '../../hooks/usePreventorRutas';

export default function PreventorRutas() {
  const {
    selectedVisit,
    setSelectedVisit,
    visits,
    handleOpenHistorial
  } = usePreventorRutas();

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-primary">
          Supervisión de Preventores
        </span>
        <h2 className="text-xl font-bold text-ink mt-0.5">
          Rutero en Calle y Alta de Comercios
        </h2>
        <p className="text-[12px] text-muted mt-0.5">
          Vista heredada de preventores: monitoreo de visitas comerciales y auditoría de clientes registrados.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Stops list on left */}
        <div className="lg:col-span-5 space-y-3">
          <div className="bg-white rounded-[20px] p-5 border border-border-subtle shadow-sm">
            <div className="flex justify-between items-center pb-3 mb-3 border-b border-divider">
              <h3 className="text-[14px] font-bold text-ink">
                Paradas de Lucía Martínez
              </h3>
              <Chip variant="pink">Zona Godoy Cruz</Chip>
            </div>

            <div className="space-y-2">
              {visits.map((v) => {
                const isSelected = selectedVisit.comercio === v.comercio;
                return (
                  <div
                    key={v.comercio}
                    onClick={() => setSelectedVisit(v)}
                    className={`p-3.5 rounded-[14px] border transition-all cursor-pointer ${
                      isSelected
                        ? 'bg-offer-bg border-primary shadow-xs'
                        : 'bg-surface-soft border-border-subtle hover:bg-white'
                    }`}
                  >
                    <div className="flex justify-between items-start">
                      <strong className="text-[13px] text-ink leading-tight">
                        {v.comercio}
                      </strong>
                      <span
                        className={`text-[9px] font-semibold px-2 py-0.5 rounded-full ${
                          v.estado === 'Visitado'
                            ? 'bg-success-bg text-success-icon'
                            : v.estado === 'En camino'
                            ? 'bg-info-bg text-info-text'
                            : 'bg-warning-alt-bg text-warning-text'
                        }`}
                      >
                        {v.estado}
                      </span>
                    </div>
                    <p className="text-[11px] text-muted mt-1">{v.direccion}</p>
                    <div className="flex justify-between items-center mt-2 pt-2 border-t border-border-subtle/60 text-[10px] text-muted">
                      <span>Hora est.: {v.hora}</span>
                      <span className="text-secondary font-medium">Ver detalles →</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Map and Sheet Details on right */}
        <div className="lg:col-span-7 space-y-4">
          <div className="bg-white rounded-[22px] p-5 border border-border-subtle shadow-sm">
            <div className="flex justify-between items-center mb-3">
              <span className="text-[11px] font-bold text-muted uppercase tracking-wider">
                Geolocalización de Preventor
              </span>
              <span className="text-[11px] text-secondary font-semibold">
                GPS Activo · Actualizado hace 2 min
              </span>
            </div>

            {/* OpenStreetMap interactive route */}
            <OSMMap
              className="h-[360px] w-full"
              markers={visits}
              activeMarkerId={selectedVisit.id}
              showRoute={true}
              routeColor="#e86bc4"
              onMarkerClick={(visit) => setSelectedVisit(visit)}
            />

            <div className="mt-4 p-4 rounded-[16px] bg-surface-soft border border-border-subtle">
              <div className="flex justify-between items-start">
                <div>
                  <span className="text-[10px] font-bold text-primary uppercase">
                    Comercio Seleccionado
                  </span>
                  <h3 className="text-lg font-bold text-ink mt-0.5">
                    {selectedVisit.comercio}
                  </h3>
                  <p className="text-[12px] text-muted">{selectedVisit.direccion}</p>
                </div>
                <Button secondary onClick={handleOpenHistorial}>
                  Historial de Compras
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
