import { useAuth } from '../../context/AuthContext';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import { useCambiarPassword } from '../../hooks/useCambiarPassword';

export default function CambiarPassword() {
  const { cambiarClave, debeCambiarClave } = useAuth();
  const {
    claveActual,
    setClaveActual,
    nuevaClave,
    setNuevaClave,
    confirmarClave,
    setConfirmarClave,
    cargando,
    error,
    success,
    setSuccess,
    handleSubmit
  } = useCambiarPassword({ cambiarClave });

  return (
    <div className="max-w-xl mx-auto space-y-6">
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-primary">
          Seguridad de Cuenta
        </span>
        <h2 className="text-xl font-bold text-ink mt-0.5">
          Modificar Contraseña de Acceso
        </h2>
        <p className="text-[12px] text-muted mt-0.5">
          {debeCambiarClave
            ? 'Por razones de seguridad, debés actualizar tu contraseña temporal antes de continuar operando.'
            : 'Actualizá periódicamente tus credenciales para mantener la seguridad de las operaciones.'}
        </p>
      </div>

      {debeCambiarClave && !success && (
        <div className="p-4 bg-warning-subtle-bg border border-warning-subtle-border rounded-[16px] text-[12px] text-warning-subtle-text flex items-start gap-3">
          <Icon name="alert" className="w-5 h-5 shrink-0 text-warning-subtle-icon" />
          <div>
            <strong className="block font-bold">Cambio de contraseña obligatorio</strong>
            <span>Iniciaste sesión con una clave temporal. Establecé una clave definitiva para habilitar todas las funciones.</span>
          </div>
        </div>
      )}

      <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-border-subtle shadow-sm">
        {success ? (
          <div className="p-6 text-center bg-success-bg rounded-[16px] text-success-icon space-y-3">
            <div className="w-10 h-10 mx-auto rounded-full bg-success-circle-bg flex items-center justify-center text-success-text">
              <Icon name="check" className="w-5 h-5" />
            </div>
            <h3 className="font-bold text-[15px]">¡Contraseña actualizada correctamente!</h3>
            <p className="text-[12px] text-success-icon/90 max-w-sm mx-auto">
              Tus nuevas credenciales ya están activas y registradas en el servidor seguro.
            </p>
            <div className="pt-2">
              <Button onClick={() => setSuccess(false)}>
                Entendido
              </Button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <div className="p-3 bg-danger-bg border border-danger-border rounded-[12px] text-[12px] text-danger-hover flex items-center gap-2">
                <Icon name="alert" className="w-4 h-4 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Contraseña actual *
              </label>
              <input
                type="password"
                required
                value={claveActual}
                onChange={(e) => setClaveActual(e.target.value)}
                placeholder="••••••••"
                className="w-full border border-border-subtle rounded-[12px] p-3 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Nueva contraseña *
              </label>
              <input
                type="password"
                required
                value={nuevaClave}
                onChange={(e) => setNuevaClave(e.target.value)}
                placeholder="Mínimo 8 caracteres"
                className="w-full border border-border-subtle rounded-[12px] p-3 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-ink mb-1">
                Confirmar nueva contraseña *
              </label>
              <input
                type="password"
                required
                value={confirmarClave}
                onChange={(e) => setConfirmarClave(e.target.value)}
                placeholder="Reiterar la nueva clave"
                className="w-full border border-border-subtle rounded-[12px] p-3 text-[12px] text-ink bg-white focus:outline-none focus:border-primary"
              />
            </div>

            <div className="pt-2">
              <Button type="submit" disabled={cargando} className="w-full sm:w-auto px-8 py-3">
                {cargando ? 'Actualizando clave...' : 'Guardar nueva contraseña'}
              </Button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
