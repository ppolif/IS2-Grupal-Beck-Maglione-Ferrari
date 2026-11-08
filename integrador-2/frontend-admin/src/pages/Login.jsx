import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import Button from '../components/Button';
import Logo from '../components/Logo';
import Icon from '../components/Icon';

export default function Login() {
  const { login } = useAuth();
  const [nombreUsuario, setNombreUsuario] = useState('');
  const [clave, setClave] = useState('');
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!nombreUsuario.trim() || !clave) return;

    setError(null);
    setCargando(true);

    try {
      await login(nombreUsuario, clave);
      // La navegación la gestiona automáticamente RoleRouter al detectar isLogged
    } catch (err) {
      if (err.status === 404 || err.message === 'Not Found') {
        setError('No se pudo encontrar el servicio de autenticación en el backend (Error 404). Asegurate de que el backend esté corriendo en el puerto 8080.');
      } else {
        setError(err.message || 'Error al conectar con el servidor de autenticación');
      }
    } finally {
      setCargando(false);
    }
  };

  const handleQuickLogin = async (user, pass) => {
    setNombreUsuario(user);
    setClave(pass);
    setError(null);
    setCargando(true);

    try {
      await login(user, pass);
    } catch (err) {
      // Si la contraseña fue modificada previamente, intentamos con la clave actualizada
      const alternatePass = pass === 'dulcemayor' ? 'DulceMayor2026!' : 'dulcemayor';
      try {
        setClave(alternatePass);
        await login(user, alternatePass);
        return;
      } catch {
        // Continuamos con el error original si ambos fallan
      }

      if (err.status === 404 || err.message === 'Not Found') {
        setError('No se pudo encontrar el servicio de autenticación en el backend (Error 404). Asegurate de que el backend esté corriendo en el puerto 8080.');
      } else {
        setError(err.message || 'Error al conectar con el servidor de autenticación');
      }
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#f4f6fb] flex flex-col justify-center py-6 sm:py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl w-full mx-auto bg-white rounded-[28px] shadow-[0_20px_50px_rgba(43,43,58,0.06)] border border-[#ececf2] overflow-hidden grid grid-cols-1 lg:grid-cols-12 min-h-[580px]">
        {/* Left Side: Brand Panel */}
        <div className="lg:col-span-5 bg-gradient-to-br from-[#fff0fb] via-[#f3f9ff] to-[#e8faff] p-8 lg:p-10 flex flex-col justify-between border-b lg:border-b-0 lg:border-r border-[#ececf2]">
          <div>
            <div className="mb-8">
              <Logo />
            </div>

            <span className="inline-block text-[10px] font-bold tracking-[0.16em] uppercase text-[#e86bc4] bg-white/80 px-3 py-1 rounded-full border border-[#f0c7e8]">
              Panel de Control Central
            </span>

            <h1 className="text-2xl sm:text-[26px] font-black text-[#2b2b3a] mt-3 leading-snug">
              Gestión y Administración Mayorista
            </h1>

            <p className="text-[12px] text-[#6b6b80] mt-3 leading-relaxed">
              Acceso seguro y centralizado para el personal operativo y la jefatura comercial de Dulce Mayor.
            </p>
          </div>

          <div className="space-y-3 mt-8">
            <div className="p-3.5 bg-white/70 backdrop-blur-xs rounded-[16px] border border-white flex items-start gap-3">
              <div className="w-8 h-8 rounded-full bg-[#ffe2f7] text-[#a34187] flex items-center justify-center shrink-0">
                <Icon name="user" className="w-4 h-4" />
              </div>
              <div className="text-[11px]">
                <strong className="block text-[#2b2b3a] font-bold">Jefatura Comercial</strong>
                <p className="text-[#6b6b80] leading-tight">Supervisión, reportes, métricas y alta exclusiva de empleados.</p>
              </div>
            </div>

            <div className="p-3.5 bg-white/70 backdrop-blur-xs rounded-[16px] border border-white flex items-start gap-3">
              <div className="w-8 h-8 rounded-full bg-[#d9f7ff] text-[#247f99] flex items-center justify-center shrink-0">
                <Icon name="box" className="w-4 h-4" />
              </div>
              <div className="text-[11px]">
                <strong className="block text-[#2b2b3a] font-bold">Personal Administrativo</strong>
                <p className="text-[#6b6b80] leading-tight">Control de inventario, stock, catálogo de productos y pedidos.</p>
              </div>
            </div>
          </div>
        </div>

        {/* Right Side: Login Form */}
        <div className="lg:col-span-7 p-8 lg:p-12 flex flex-col justify-center">
          <div className="max-w-md w-full mx-auto">
            <div className="mb-6">
              <h2 className="text-xl font-bold text-[#2b2b3a]">Iniciar Sesión</h2>
              <p className="text-[12px] text-[#6b6b80] mt-0.5">
                Ingresá tus credenciales autorizadas para acceder al sistema.
              </p>
            </div>

            {error && (
              <div className="mb-5 p-3.5 bg-[#ffe8ed] border border-[#ffccd5] rounded-[14px] text-[12px] text-[#d63353] flex items-start gap-2.5">
                <Icon name="alert" className="w-4 h-4 shrink-0 mt-0.5" />
                <div className="flex-1 font-medium leading-relaxed">{error}</div>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                  Nombre de usuario o legajo *
                </label>
                <input
                  type="text"
                  required
                  autoFocus
                  value={nombreUsuario}
                  onChange={(e) => setNombreUsuario(e.target.value)}
                  placeholder="Ej: jefe o administrativo"
                  className="w-full border border-[#ececf2] rounded-[13px] px-3.5 py-2.5 text-[12px] text-[#2b2b3a] bg-[#fafafc] focus:bg-white focus:outline-none focus:border-[#e86bc4] transition-all"
                />
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                  Contraseña *
                </label>
                <input
                  type="password"
                  required
                  value={clave}
                  onChange={(e) => setClave(e.target.value)}
                  placeholder="••••••••"
                  className="w-full border border-[#ececf2] rounded-[13px] px-3.5 py-2.5 text-[12px] text-[#2b2b3a] bg-[#fafafc] focus:bg-white focus:outline-none focus:border-[#e86bc4] transition-all"
                />
              </div>

              <div className="pt-2">
                <Button
                  type="submit"
                  disabled={cargando}
                  className="w-full py-3 text-[13px]"
                >
                  {cargando ? (
                    <span className="inline-flex items-center gap-2">
                      <svg className="animate-spin h-4 w-4 text-white" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                      </svg>
                      Verificando credenciales...
                    </span>
                  ) : (
                    'Ingresar al panel administrativo'
                  )}
                </Button>
              </div>
            </form>

            {/* Quick Demo Access Pills */}
            <div className="mt-8 pt-6 border-t border-[#f0f0f4]">
              <span className="block text-[10px] uppercase font-bold text-[#a09fb0] tracking-wider mb-2.5">
                Accesos rápidos de demostración (1-click Login)
              </span>
              <div className="grid grid-cols-2 gap-2">
                <button
                  type="button"
                  disabled={cargando}
                  onClick={() => handleQuickLogin('jefe', 'dulcemayor')}
                  className="p-2.5 rounded-[12px] bg-[#fff5fc] border border-[#f5d5ee] text-left hover:bg-[#ffeaf8] transition-colors cursor-pointer disabled:opacity-50"
                >
                  <strong className="block text-[11px] text-[#93307b] font-bold">Jefe Comercial</strong>
                  <span className="text-[10px] text-[#6b6b80]">jefe / dulcemayor</span>
                </button>

                <button
                  type="button"
                  disabled={cargando}
                  onClick={() => handleQuickLogin('administrativo', 'dulcemayor')}
                  className="p-2.5 rounded-[12px] bg-[#f0faff] border border-[#cbeafd] text-left hover:bg-[#e4f5ff] transition-colors cursor-pointer disabled:opacity-50"
                >
                  <strong className="block text-[11px] text-[#1c6f87] font-bold">Administrativo</strong>
                  <span className="text-[10px] text-[#6b6b80]">administrativo / dulcemayor</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
