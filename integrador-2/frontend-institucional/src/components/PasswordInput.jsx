import { useState } from 'react';
import Icon from './Icon';

/**
 * Input de contraseña con toggle accesible para alternar entre ver
 * el texto plano y ver los símbolos ocultos.
 * Desactiva autocorrección y autocapitalización para evitar interferencias al revelarla.
 */
export default function PasswordInput({
  id,
  name,
  value,
  onChange,
  placeholder = '••••••••',
  required = false,
  minLength = 8,
  autoComplete = 'current-password',
  className = '',
  ariaDescribedBy,
  disabled = false,
}) {
  const [mostrar, setMostrar] = useState(false);

  return (
    <div className="relative flex items-center">
      <input
        type={mostrar ? 'text' : 'password'}
        id={id}
        name={name}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        minLength={minLength}
        autoComplete={autoComplete}
        spellCheck={false}
        autoCorrect="off"
        autoCapitalize="off"
        disabled={disabled}
        aria-describedby={ariaDescribedBy}
        className={`w-full border border-[#ececf2] rounded-[12px] p-3 pr-11 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4] disabled:bg-slate-100 disabled:cursor-not-allowed ${className}`}
      />
      <button
        type="button"
        onClick={() => setMostrar((prev) => !prev)}
        aria-pressed={mostrar}
        aria-label={mostrar ? 'Ocultar contraseña' : 'Ver contraseña'}
        title={mostrar ? 'Ocultar contraseña' : 'Ver contraseña'}
        disabled={disabled}
        tabIndex={-1}
        className="absolute right-3 p-1 text-[#8c8b9c] hover:text-[#2b2b3a] transition-colors cursor-pointer select-none focus:outline-none"
      >
        <Icon name={mostrar ? 'eyeOff' : 'eye'} className="w-4 h-4" />
      </button>
    </div>
  );
}

