import { useState } from 'react';

export default function FloatingChat() {
  const [open, setOpen] = useState(false);

  return (
    <div className="fixed right-6 sm:right-11 bottom-8 flex items-center gap-2 z-50">
      {open ? (
        <div className="bg-white p-4 rounded-[18px] shadow-[0_14px_40px_rgba(56,52,84,0.18)] border border-[#ececf2] max-w-[280px]">
          <div className="flex justify-between items-center mb-2">
            <span className="font-bold text-[12px] text-[#2b2b3a]">Atención Comercial</span>
            <button
              onClick={() => setOpen(false)}
              className="text-[#6b6b80] hover:text-[#2b2b3a] text-[13px] px-1"
            >
              ✕
            </button>
          </div>
          <p className="text-[11px] text-[#6b6b80] mb-3">
            ¡Hola! ¿Buscás armar un pedido mayorista para tu negocio? Escribinos y te asesoramos al instante.
          </p>
          <a
            href="https://wa.me/"
            target="_blank"
            rel="noreferrer"
            className="block text-center bg-[#7ed6a5] hover:bg-[#68c692] text-white text-[11px] font-semibold py-2 rounded-[10px] transition-colors"
          >
            Chatear por WhatsApp
          </a>
        </div>
      ) : (
        <span
          onClick={() => setOpen(true)}
          className="hidden sm:inline-block bg-white text-[#2b2b3a] font-medium text-[11px] px-3.5 py-2.5 rounded-[12px] shadow-[0_14px_40px_rgba(56,52,84,0.12)] border border-[#ececf2] cursor-pointer hover:bg-slate-50 transition-colors"
        >
          ¿Te armo un presupuesto?
        </span>
      )}
      <button
        onClick={() => setOpen(!open)}
        className="w-[52px] h-[52px] rounded-full bg-[#e86bc4] hover:bg-[#d456b0] text-white font-bold text-[15px] flex items-center justify-center shadow-[0_8px_20px_rgba(232,107,196,0.35)] cursor-pointer transition-transform active:scale-95"
        title="Atención personalizada"
        aria-label="Abrir chat comercial"
      >
        DM
      </button>
    </div>
  );
}

