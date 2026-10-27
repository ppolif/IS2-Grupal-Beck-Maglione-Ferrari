import { useState } from 'react';
import Button from '../components/Button';
import Icon from '../components/Icon';

export default function Contacto() {
  const [sent, setSent] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    setSent(true);
  };

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Canales de Atención
        </span>
        <h1 className="text-3xl font-bold text-[#2b2b3a] mt-1">
          Contactate con Dulce Mayor
        </h1>
        <p className="text-[13px] text-[#6b6b80] mt-0.5">
          Estamos para responder tus consultas comerciales, altas de cuentas y asesoramiento preventor.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Contact info cards */}
        <div className="lg:col-span-5 space-y-4">
          <div className="bg-white rounded-[22px] p-6 border border-[#ececf2] shadow-sm space-y-6">
            <h2 className="text-[16px] font-bold text-[#2b2b3a] pb-3 border-b border-[#f0f0f4]">
              Datos de la Empresa
            </h2>

            <div className="space-y-4 text-[13px]">
              <div className="flex items-start gap-3.5">
                <span className="w-9 h-9 rounded-[11px] bg-[#ffe2f7] text-[#e86bc4] flex items-center justify-center shrink-0">
                  <Icon name="mapPin" className="w-5 h-5" />
                </span>
                <div>
                  <strong className="text-[#2b2b3a] block">Casa Central</strong>
                  <span className="text-[#6b6b80]">Av. San Martín 1045, Ciudad de Mendoza, CP 5500</span>
                </div>
              </div>

              <div className="flex items-start gap-3.5">
                <span className="w-9 h-9 rounded-[11px] bg-[#d9f7ff] text-[#247f99] flex items-center justify-center shrink-0">
                  <Icon name="phone" className="w-5 h-5" />
                </span>
                <div>
                  <strong className="text-[#2b2b3a] block">Líneas Telefónicas</strong>
                  <span className="text-[#6b6b80]">Central: (0261) 420-9800</span><br />
                  <span className="text-[#6b6b80]">Preventa: (0261) 420-9801</span>
                </div>
              </div>

              <div className="flex items-start gap-3.5">
                <span className="w-9 h-9 rounded-[11px] bg-[#dff8e9] text-[#39855b] flex items-center justify-center shrink-0">
                  <Icon name="mail" className="w-5 h-5" />
                </span>
                <div>
                  <strong className="text-[#2b2b3a] block">Correo Electrónico</strong>
                  <span className="text-[#6b6b80]">ventas@dulcemayor.com.ar</span><br />
                  <span className="text-[#6b6b80]">atencion@dulcemayor.com.ar</span>
                </div>
              </div>
            </div>

            <div className="p-4 rounded-[14px] bg-[#292838] text-white">
              <span className="text-[10px] font-bold uppercase text-[#a1eafb] tracking-wider block mb-1">
                Atención a Kioscos y Comercios
              </span>
              <p className="text-[12px] text-[#bcbac9]">
                Si querés que un preventor visite tu local directamente en tu zona, dejanos tus datos y te asignamos un asesor en 24 hs.
              </p>
            </div>
          </div>
        </div>

        {/* Contact Form */}
        <div className="lg:col-span-7">
          <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-sm">
            <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
              Formulario de Consulta
            </span>
            <h2 className="text-xl font-bold text-[#2b2b3a] mt-1 mb-4">
              Envianos tu mensaje
            </h2>

            {sent ? (
              <div className="p-8 text-center bg-[#dff8e9] rounded-[16px] text-[#39855b]">
                <div className="w-12 h-12 bg-white rounded-full flex items-center justify-center mx-auto mb-3 shadow-sm">
                  <Icon name="check" className="w-6 h-6 text-[#39855b]" />
                </div>
                <h3 className="text-[16px] font-bold">¡Mensaje enviado con éxito!</h3>
                <p className="text-[12px] text-[#39855b]/80 mt-1">
                  Un representante comercial responderá a tu email o teléfono a la brevedad.
                </p>
                <button
                  type="button"
                  onClick={() => setSent(false)}
                  className="mt-4 text-[12px] font-semibold underline"
                >
                  Enviar otra consulta
                </button>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                      Nombre y Apellido *
                    </label>
                    <input
                      type="text"
                      required
                      placeholder="Ej. Lucas Gómez"
                      className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white placeholder-[#aaa9ba] focus:outline-none focus:border-[#e86bc4]"
                    />
                  </div>
                  <div>
                    <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                      Nombre del Comercio
                    </label>
                    <input
                      type="text"
                      placeholder="Ej. Kiosco Los Andes"
                      className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white placeholder-[#aaa9ba] focus:outline-none focus:border-[#e86bc4]"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                      Email de Contacto *
                    </label>
                    <input
                      type="email"
                      required
                      placeholder="nombre@comercio.com"
                      className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white placeholder-[#aaa9ba] focus:outline-none focus:border-[#e86bc4]"
                    />
                  </div>
                  <div>
                    <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                      Teléfono / WhatsApp *
                    </label>
                    <input
                      type="tel"
                      required
                      placeholder="Ej. 261 555 1234"
                      className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white placeholder-[#aaa9ba] focus:outline-none focus:border-[#e86bc4]"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                    Asunto de la consulta
                  </label>
                  <select
                    className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  >
                    <option>Alta de nueva cuenta mayorista</option>
                    <option>Visita de preventor a comercio</option>
                    <option>Consulta sobre lista de precios y stock</option>
                    <option>Consulta sobre estado de entrega</option>
                    <option>Otro motivo</option>
                  </select>
                </div>

                <div>
                  <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1.5">
                    Mensaje o detalle *
                  </label>
                  <textarea
                    rows={4}
                    required
                    placeholder="Contanos tu necesidad, ubicación de tu comercio o productos de interés..."
                    className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px] text-[#2b2b3a] bg-white placeholder-[#aaa9ba] focus:outline-none focus:border-[#e86bc4]"
                  />
                </div>

                <div className="pt-2">
                  <Button type="submit" className="w-full sm:w-auto px-8 py-3.5">
                    Enviar consulta
                  </Button>
                </div>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

