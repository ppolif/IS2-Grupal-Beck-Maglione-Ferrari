import { Link } from 'react-router-dom';

export default function Logo({ light = false, className = '' }) {
  return (
    <Link to="/" className={`inline-flex items-center gap-2 font-bold tracking-tight select-none text-[15px] ${light ? 'text-white' : 'text-[#2b2b3a]'} ${className}`}>
      <img
        src="/logo.png"
        alt="Dulce Mayor"
        className="w-7 h-7 object-contain shrink-0 rounded"
      />
      <span>Dulce Mayor</span>
    </Link>
  );
}

