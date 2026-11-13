import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import Button from '../components/Button';
import { useAuth } from '../context/AuthContext';

export default function CompletarDni() {
  const [done, setDone] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [numeroDocumento, setNumeroDocumento] = useState('');
  const [confirmarDocumento, setConfirmarDocumento] = useState('');

  const navigate = useNavigate();
  const { completarDni, debeCompletarDni, usuario, logout } = useAuth();

  // Si el usuario ya cargó su DNI real anteriormente, no permitir volver a entrar (solo 1 vez)
  if (!debeCompletarDni && !done) {
    return <Navigate to="/perfil" replace />;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    const dniLimpio = numeroDocumento.replace(/\./g, '').replace(/-/g, '').trim();
    const confirmarLimpio = confirmarDocumento.replace(/\./g, '').replace(/-/g, '').trim();

    if (!/^\d{7,8}$/.test(dniLimpio)) {
      setError('El DNI debe contener entre 7 y 8 dígitos numéricos válidos.');
      return;
    }

    if (dniLimpio !== confirmarLimpio) {
      setError('El número de DNI y su confirmación no coinciden.');
      return;
    }

    setLoading(true);
    try {
      await completarDni(dniLimpio, confirmarLimpio);
      setDone(true);
      setTimeout(() => {
        navigate('/perfil', { replace: true });
      }, 1400);
    } catch (err) {
      setError(err.message || 'Error al registrar el DNI.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto p-4 sm:p-6 my-6 space-y-6">
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Verificación de Identidad (Única Vez)
        </span>
        <h1 className="text-2xl font-bold text-[#2b2b3a] mt-1">
          Completar Documento (DNI)
        </h1>
        <p className="text-[12px] text-[#6b6b80] mt-1 leading-relaxed">
          Bienvenido/a <strong>{usuario?.nombreUsuario}</strong>. Por tratarse de tu primer ingreso con Google / Auth0, necesitamos que registres tu DNI real por única vez antes de operar en la tienda.
        </p>
      </div>

      <div className="bg-white p-6 rounded-[20px] border border-[#ececf2] shadow-sm space-y-4">
        <div className="p-3.5 rounded-[12px] bg-[#fff6e0] border border-[#ffd98a] text-[#8a6519] text-[11px] leading-relaxed">
          <strong className="font-bold block mb-0.5">⚠️ Importante:</strong>
          Este dato se solicita por única vez para asociar tus pedidos y facturación. Una vez confirmado, tu DNI quedará fijado y no podrá volver a modificarse.
        </div>

        {error && (
          <div className="p-3 bg-red-50 text-red-600 rounded-[12px] text-[12px] border border-red-100">
            {error}
          </div>
        )}

        {done ? (
          <div className="text-center p-6 bg-[#dff8e9] rounded-[14px] text-[#39855b]">
            <p className="font-bold text-[14px]">¡DNI registrado con éxito!</p>
            <p className="text-[11px] mt-1">Desbloqueando funciones y redirigiendo a tu cuenta...</p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Número de DNI Real *
              </label>
              <input
                type="text"
                inputMode="numeric"
                required
                value={numeroDocumento}
                onChange={(e) => setNumeroDocumento(e.target.value)}
                placeholder="Ej: 40123456 (sin puntos)"
                disabled={loading}
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                Confirmar Número de DNI *
              </label>
              <input
                type="text"
                inputMode="numeric"
                required
                value={confirmarDocumento}
                onChange={(e) => setConfirmarDocumento(e.target.value)}
                placeholder="Repetir tu número de DNI"
                disabled={loading}
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div className="pt-2 flex flex-col sm:flex-row gap-3">
              <Button
                type="submit"
                disabled={loading}
                className="flex-1 justify-center py-3 cursor-pointer"
              >
                {loading ? 'Guardando...' : 'Confirmar y Guardar DNI'}
              </Button>
              <Button
                secondary
                type="button"
                onClick={logout}
                disabled={loading}
                className="px-4 py-3 cursor-pointer"
              >
                Cerrar sesión
              </Button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
