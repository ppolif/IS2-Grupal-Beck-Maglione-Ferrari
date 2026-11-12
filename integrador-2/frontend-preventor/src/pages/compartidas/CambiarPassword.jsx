import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../../components/Button';
import { useAuth } from '../../context/AuthContext';

export default function CambiarPassword() {
  const [done, setDone] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [claveActual, setClaveActual] = useState('');
  const [nuevaClave, setNuevaClave] = useState('');
  const [confirmarClave, setConfirmarClave] = useState('');

  const navigate = useNavigate();
  const { cambiarClave } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (nuevaClave.length < 8) {
      setError('La nueva contraseña debe tener al menos 8 caracteres.');
      return;
    }

    if (nuevaClave !== confirmarClave) {
      setError('La nueva contraseña y su confirmación no coinciden.');
      return;
    }

    setLoading(true);
    try {
      await cambiarClave(claveActual, nuevaClave, confirmarClave);
      setDone(true);
      setTimeout(() => {
        navigate('/perfil');
      }, 1500);
    } catch (err) {
      setError(err.message || 'Error al modificar la contraseña.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto p-4 sm:p-6 space-y-6">
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Seguridad
        </span>
        <h1 className="text-2xl font-bold text-[#2b2b3a] mt-1">
          Cambiar Contraseña
        </h1>
        <p className="text-[12px] text-[#6b6b80] mt-1">
          Actualizá tu clave de acceso para resguardar tus operaciones en ruta.
        </p>
      </div>

      <div className="bg-white p-6 rounded-[20px] border border-[#ececf2] shadow-sm">
        {error && (
          <div className="p-3 mb-4 bg-red-50 text-red-600 rounded-[12px] text-[12px] border border-red-100">
            {error}
          </div>
        )}

        {done ? (
          <div className="text-center p-6 bg-[#dff8e9] rounded-[14px] text-[#39855b]">
            <p className="font-bold text-[14px]">¡Contraseña actualizada con éxito!</p>
            <p className="text-[11px] mt-1">Redirigiendo a tu perfil...</p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Contraseña Actual *
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
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Nueva Contraseña *
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
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Confirmar Nueva Contraseña *
              </label>
              <input
                type="password"
                required
                value={confirmarClave}
                onChange={(e) => setConfirmarClave(e.target.value)}
                placeholder="Repetir nueva contraseña"
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div className="pt-2 flex gap-3">
              <Button type="submit" disabled={loading} className="flex-1 justify-center py-3 cursor-pointer">
                {loading ? 'Guardando...' : 'Guardar cambios'}
              </Button>
              <Button
                secondary
                type="button"
                onClick={() => navigate('/perfil')}
                className="px-4 py-3 cursor-pointer"
              >
                Cancelar
              </Button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}

