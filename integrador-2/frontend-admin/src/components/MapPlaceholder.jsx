export default function MapPlaceholder({ className = "h-full w-full" }) {
  return (
    <div
      className={`relative rounded-[20px] bg-[#e9ebed] border border-[#d4d7da] overflow-hidden flex flex-col items-center justify-center p-6 text-center select-none shadow-inner ${className}`}
    >
      {/* Decorative road simulation lines */}
      <div className="absolute top-[80px] -left-[100px] w-[700px] h-[36px] bg-white border border-[#d4d7da] rotate-[20deg] opacity-75 pointer-events-none" />
      <div className="absolute top-[260px] -left-[120px] w-[700px] h-[36px] bg-white border border-[#d4d7da] -rotate-[25deg] opacity-75 pointer-events-none" />

      {/* Decorative SVG path */}
      <svg className="absolute inset-0 w-full h-[85%] z-[2]" viewBox="0 0 390 520" fill="none">
        <path
          d="M80 450 C100 350 280 380 260 250 S120 180 300 80"
          stroke="#3FB8D9"
          strokeWidth="6"
          strokeLinecap="round"
          strokeDasharray="2 3"
        />
        <circle cx="260" cy="250" r="12" fill="#fff" />
        <circle cx="260" cy="250" r="6" fill="#E86BC4" />
      </svg>

      {/* Center Pin & Mandatory text */}
      <div className="relative z-10 flex flex-col items-center">
        <div className="w-10 h-10 rounded-full bg-[#e86bc4] text-white flex items-center justify-center shadow-md mb-2 text-lg">
          📍
        </div>
        <span className="font-bold text-[13px] text-[#2b2b3a] bg-white/95 px-3.5 py-1.5 rounded-full shadow-sm border border-[#ececf2] mb-1">
          usar api de openstreetmap
        </span>
        <span className="text-[10px] text-[#6b6b80] bg-white/85 px-2 py-0.5 rounded-md">
          Monitoreo de Rutas en Gran Mendoza
        </span>
      </div>
    </div>
  );
}

