export default function MapPlaceholder({ className = "h-full w-full", isDelivery = false }) {
  return (
    <div
      className={`relative rounded-[24px] bg-[#e9ebed] border border-[#d4d7da] overflow-hidden flex flex-col items-center justify-center p-6 text-center select-none shadow-inner ${className}`}
    >
      {/* Decorative road simulation lines from prototype */}
      <div className="absolute top-[140px] -left-[110px] w-[700px] h-[42px] bg-white border-2 border-[#d4d7da] rotate-[24deg] opacity-75 pointer-events-none" />
      <div className="absolute top-[350px] -left-[130px] w-[700px] h-[42px] bg-white border-2 border-[#d4d7da] -rotate-[33deg] opacity-75 pointer-events-none" />
      <div className="absolute top-[530px] -left-[180px] w-[700px] h-[42px] bg-white border-2 border-[#d4d7da] rotate-[9deg] opacity-75 pointer-events-none" />

      {/* Decorative SVG route curve */}
      <svg className="absolute inset-0 w-full h-[75%] z-[2]" viewBox="0 0 390 520" fill="none">
        <path
          d={
            isDelivery
              ? "M80 450 C100 350 280 380 260 250 S120 180 300 80"
              : "M76 430 C180 390 120 280 230 240 S300 140 275 70"
          }
          stroke={isDelivery ? "#3FB8D9" : "#E86BC4"}
          strokeWidth="6"
          strokeLinecap="round"
          strokeDasharray="2 3"
        />
        <circle cx="76" cy="430" r="14" fill="#fff" />
        <circle cx="76" cy="430" r="8" fill="#3FB8D9" />
      </svg>

      {/* Location labels */}
      <div className="absolute top-[220px] left-[18px] text-[#8e9096] rotate-[24deg] text-[12px] font-medium z-[3]">
        Av. San Martín
      </div>
      <div className="absolute top-[390px] right-[40px] text-[#8e9096] -rotate-[33deg] text-[12px] font-medium z-[3]">
        Godoy Cruz
      </div>

      {/* Center Pin & Mandatory text */}
      <div className="relative z-10 flex flex-col items-center">
        <div className="w-11 h-11 rounded-full bg-[#e86bc4] text-white flex items-center justify-center shadow-lg mb-2 text-xl">
          📍
        </div>
        <span className="font-bold text-[13px] text-[#2b2b3a] bg-white/95 px-3.5 py-1.5 rounded-full shadow-md border border-[#ececf2] mb-1">
          usar api de openstreetmap
        </span>
        <span className="text-[10px] text-[#6b6b80] bg-white/85 px-2.5 py-0.5 rounded-md">
          {isDelivery ? "Recorrido de Distribución Activo" : "Rutero de Preventa Activo"}
        </span>
      </div>
    </div>
  );
}

