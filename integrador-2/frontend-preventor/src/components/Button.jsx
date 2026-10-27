export default function Button({
  children,
  secondary = false,
  variant = "default",
  className = "",
  onClick,
  type = "button"
}) {
  const baseClasses =
    "inline-flex items-center justify-center gap-2 px-4 py-2.5 rounded-[13px] font-semibold text-[12px] transition-all cursor-pointer select-none active:scale-[0.98] min-h-[44px]";

  let styleClasses = "";

  if (secondary || variant === "secondary") {
    styleClasses =
      "bg-white text-[#3fb8d9] border-[1.5px] border-[#3fb8d9] hover:bg-[#d9f7ff]/30 shadow-none";
  } else if (variant === "danger") {
    styleClasses =
      "bg-[#ff8fa3] text-white hover:bg-[#e6758b] shadow-[0_8px_20px_rgba(255,143,163,0.25)]";
  } else {
    styleClasses =
      "bg-[#e86bc4] text-white hover:bg-[#d456b0] shadow-[0_8px_20px_rgba(232,107,196,0.2)]";
  }

  return (
    <button
      type={type}
      onClick={onClick}
      className={`${baseClasses} ${styleClasses} ${className}`}
    >
      {children}
    </button>
  );
}

