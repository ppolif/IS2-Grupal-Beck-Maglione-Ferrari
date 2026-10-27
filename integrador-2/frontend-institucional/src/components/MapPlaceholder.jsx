export default function MapPlaceholder({ className = "h-80 w-full" }) {
  return (
    <div
      className={`relative rounded-[20px] bg-[#e9ebed] border border-[#d4d7da] overflow-hidden flex flex-col items-center justify-center p-6 text-center select-none shadow-inner ${className}`}
    >
      {/* Decorative road simulation lines matching prototype */}
      <div className="absolute -top-10 -left-20 w-[600px] h-10 bg-white border border-[#d4d7da] rotate-12 opacity-70 pointer-events-none" />
      <div className="absolute top-1/2 -right-20 w-[600px] h-10 bg-white border border-[#d4d7da] -rotate-12 opacity-70 pointer-events-none" />
      <div className="absolute -bottom-10 left-10 w-[600px] h-10 bg-white border border-[#d4d7da] rotate-45 opacity-60 pointer-events-none" />

      {/* Decorative pin */}
      <div className="relative z-10 flex flex-col items-center">
        <div className="w-10 h-10 rounded-full bg-[#e86bc4] text-white flex items-center justify-center shadow-md mb-2 text-lg">
          📍
        </div>
        <span className="font-bold text-[13px] text-[#2b2b3a] bg-white/95 px-3 py-1 rounded-full shadow-sm border border-[#ececf2] mb-1">
          usar api de openstreetmap
        </span>
        <span className="text-[10px] text-[#6b6b80] bg-white/80 px-2 py-0.5 rounded-md">
          Mendoza, Argentina
        </span>
      </div>
    </div>
  );
}

