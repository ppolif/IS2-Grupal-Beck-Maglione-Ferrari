import { useState } from 'react';
import Button from '../../components/Button';

export default function RegistroComercio() {
  const [success, setSuccess] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    setSuccess(true);
  };

  const fields = [
    { label: 'Nombre del comercio', placeholder: 'Kiosco El Sol', defaultVal: 'Kiosco El Sol' },
    { label: 'Responsable', placeholder: 'Nombre y apellido del titular', defaultVal: 'Roberto Gómez' },
    { label: 'CUIT / CUIL', placeholder: '20-28192834-4', defaultVal: '20-28192834-4' },
    { label: 'Teléfono', placeholder: '261 422 1099', defaultVal: '261 422 1099' },
    { label: 'Email', placeholder: 'comercio@email.com', defaultVal: 'kioscoelsol@gmail.com' },
    { label: 'Dirección', placeholder: 'Av. San Martín 1842, Godoy Cruz', defaultVal: 'Av. San Martín 1842, Godoy Cruz' },
  ];

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Nuevo Minorista
        </span>
        <h1 className="text-2xl font-bold text-[#2b2b3a] mt-1">
          Registrar Comercio en Ruta
        </h1>
        <p className="text-[12px] text-[#6b6b80] mt-0.5">
          Completá la información del nuevo punto de venta para darlo de alta en tu recorrido.
        </p>
      </div>

      <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-sm">
        {success ? (
          <div className="p-8 text-center bg-[#dff8e9] rounded-[16px] text-[#39855b] space-y-3">
            <h3 className="text-[17px] font-bold">¡Comercio registrado con éxito!</h3>
            <p className="text-[12px] text-[#39855b]/90">
              El cliente fue incorporado a tu hoja de ruta y ya podés cargarle pedidos mayoristas.
            </p>
            <div className="pt-2">
              <Button onClick={() => setSuccess(false)}>
                Registrar otro comercio
              </Button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {fields.map((f) => (
                <div key={f.label} className="space-y-1">
                  <label className="block text-[11px] font-semibold text-[#2b2b3a]">
                    {f.label} *
                  </label>
                  <input
                    type="text"
                    required
                    defaultValue={f.defaultVal}
                    placeholder={f.placeholder}
                    className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white placeholder-[#aaa9ba] focus:outline-none focus:border-[#e86bc4]"
                  />
                </div>
              ))}
            </div>

            <div className="pt-4 border-t border-[#f0f0f4] flex flex-col sm:flex-row gap-3">
              <Button type="submit" className="w-full sm:w-auto px-8 py-3.5 text-[13px]">
                Registrar comercio
              </Button>
              <Button
                secondary
                type="button"
                onClick={() => window.history.back()}
                className="w-full sm:w-auto px-6 py-3.5 text-[13px]"
              >
                Volver
              </Button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}

