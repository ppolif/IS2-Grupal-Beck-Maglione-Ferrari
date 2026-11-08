import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import Button from '../../components/Button';
import Icon from '../../components/Icon';

export default function CambiarPassword() {
  const { cambiarClave, debeCambiarClave } = useAuth();
  const [claveActual, setClaveActual] = useState('');
  const [nuevaClave, setNuevaClave] = useState('');
  const [confirmarClave, setConfirmarClave] = useState('');
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (nuevaClave !== confirmarClave) {
      setError('La nueva contraseña y su confirmación no coinciden.');
      return;
    }

    if (nuevaClave.length < 8) {
      setError('La nueva contraseña debe contener al menos 8 caracteres.');
      return;
    }

    setCargando(true);
    try {
      await cambiarClave(claveActual, nuevaClave, confirmarClave);
      setSuccess(true);
      setClaveActual('');
      setNuevaClave('');
      setConfirmarClave('');
    } catch (err) {
      setError(err.message || 'Error al modificar la contraseña');
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="max-w-xl mx-auto space-y-6">
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Seguridad de Cuenta
        </span>
        <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
          Modificar Contraseña de Acceso
        </h2>
        <p className="text-[12px] text-[#6b6b80] mt-0.5">
          {debeCambiarClave
            ? 'Por razones de seguridad, debés actualizar tu contraseña temporal antes de continuar operando.'
            : 'Actualizá periódicamente tus credenciales para mantener la seguridad de las operaciones.'}
        </p>
      </div>

      {debeCambiarClave && !success && (
        <div className="p-4 bg-[#fff4db] border border-[#fce4aa] rounded-[16px] text-[12px] text-[#93630f] flex items-start gap-3">
          <Icon name="alert" className="w-5 h-5 shrink-0 text-[#b57a15]" />
          <div>
            <strong className="block font-bold">Cambio de contraseña obligatorio</strong>
            <span>Iniciaste sesión con una clave temporal. Establecé una clave definitiva para habilitar todas las funciones.</span>
          </div>
        </div>
      )}

      <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-sm">
        {success ? (
          <div className="p-6 text-center bg-[#dff8e9] rounded-[16px] text-[#39855b] space-y-3">
            <div className="w-10 h-10 mx-auto rounded-full bg-[#c9f2d8] flex items-center justify-center text-[#2e744c]">
              <Icon name="check" className="w-5 h-5" />
            </div>
            <h3 className="font-bold text-[15px]">¡Contraseña actualizada correctamente!</h3>
            <p className="text-[12px] text-[#39855b]/90 max-w-sm mx-auto">
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
              <div className="p-3 bg-[#ffe8ed] border border-[#ffccd5] rounded-[12px] text-[12px] text-[#d63353] flex items-center gap-2">
                <Icon name="alert" className="w-4 h-4 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Contraseña actual *
              </label>
              <input
                type="password"
                required
                value={claveActual}
                onChange={(e) => setClaveActual(e.target.value)}
                placeholder="••••••••"
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Nueva contraseña *
              </label>
              <input
                type="password"
                required
                value={nuevaClave}
                onChange={(e) => setNuevaClave(e.target.value)}
                placeholder="Mínimo 8 caracteres"
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Confirmar nueva contraseña *
              </label>
              <input
                type="password"
                required
                value={confirmarClave}
                onChange={(e) => setConfirmarClave(e.target.value)}
                placeholder="Reiterar la nueva clave"
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
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
