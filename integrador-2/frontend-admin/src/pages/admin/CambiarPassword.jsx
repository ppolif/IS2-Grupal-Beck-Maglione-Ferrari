import { useState } from 'react';
import Button from '../../components/Button';

export default function CambiarPassword() {
  const [success, setSuccess] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    setSuccess(true);
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
          Actualizá periódicamente tus credenciales para mantener la seguridad de las operaciones.
        </p>
      </div>

      <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-sm">
        {success ? (
          <div className="p-6 text-center bg-[#dff8e9] rounded-[14px] text-[#39855b] space-y-2">
            <h3 className="font-bold text-[15px]">¡Contraseña actualizada correctamente!</h3>
            <p className="text-[12px] text-[#39855b]/90">
              Tus nuevas credenciales ya están activas en el sistema.
            </p>
            <div className="pt-2">
              <Button onClick={() => setSuccess(false)}>
                Aceptar
              </Button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Contraseña actual *
              </label>
              <input
                type="password"
                required
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
                placeholder="Mínimo 8 caracteres alfanuméricos"
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
                placeholder="Reiterar la nueva clave"
                className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>

            <div className="pt-2">
              <Button type="submit" className="w-full sm:w-auto px-8 py-3">
                Guardar nueva contraseña
              </Button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}

