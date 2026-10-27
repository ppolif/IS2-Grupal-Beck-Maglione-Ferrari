export default function Chip({ children, variant = 'blue', className = '' }) {
  const variants = {
    pink: 'bg-[#ffe2f7] text-[#a34187]',
    blue: 'bg-[#d9f7ff] text-[#247f99]',
    warning: 'bg-[#fff2d3] text-[#986b16]',
    success: 'bg-[#dff8e9] text-[#39855b]'
  };

  return (
    <span
      className={`text-[11px] font-semibold px-2.5 py-1 rounded-full inline-block ${variants[variant] || variants.blue} ${className}`}
    >
      {children}
    </span>
  );
}

