import Icon from './Icon';

export default function Modal({
  isOpen,
  onClose,
  title,
  tag = 'NUEVO REGISTRO',
  children,
  maxWidth = 'max-w-md'
}) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/45 backdrop-blur-xs animate-in fade-in duration-200">
      <div
        className={`bg-white rounded-[22px] p-6 sm:p-7 shadow-[0_22px_80px_rgba(32,30,50,0.28)] border border-[#ececf2] w-full ${maxWidth} relative animate-in zoom-in-95 duration-200`}
      >
        <button
          type="button"
          onClick={onClose}
          className="absolute top-5 right-5 text-[#8c8b9c] hover:text-[#2b2b3a] p-1 rounded-full transition-colors"
          aria-label="Cerrar modal"
        >
          <Icon name="x" className="w-5 h-5" />
        </button>

        {tag && (
          <span className="text-[10px] font-bold tracking-[0.14em] uppercase text-[#e86bc4] block mb-1">
            {tag}
          </span>
        )}

        {title && (
          <h3 className="text-xl font-bold text-[#2b2b3a] mb-3">
            {title}
          </h3>
        )}

        <div className="mt-3">{children}</div>
      </div>
    </div>
  );
}

