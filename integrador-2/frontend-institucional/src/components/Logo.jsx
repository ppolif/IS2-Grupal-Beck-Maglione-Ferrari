import { Link } from 'react-router-dom';

export default function Logo({ light = false }) {
  return (
    <Link to="/" className={`inline-flex items-center gap-2.5 font-bold tracking-tight select-none text-[18px] ${light ? 'text-white' : 'text-[#2b2b3a]'}`}>
      <span className="candy-mark" aria-hidden="true">
        <i />
        <b />
      </span>
      <span className="font-bold">Dulce Mayor</span>
    </Link>
  );
}

