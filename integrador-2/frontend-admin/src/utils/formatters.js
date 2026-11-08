/**
 * Pure formatting and mapping utility functions.
 */

/**
 * Returns human-readable label for user/employee roles.
 * @param {string} rolEnum - Role enum identifier.
 * @returns {string} Human-readable role label.
 */
export function formatRolLabel(rolEnum) {
  switch (rolEnum) {
    case 'PREVENTISTA':
    case 'preventor':
      return 'Preventista';
    case 'DISTRIBUIDOR':
    case 'distribuidor':
      return 'Distribuidor';
    case 'ADMINISTRATIVO':
    case 'administrativo':
      return 'Administrativo';
    case 'JEFE':
    case 'jefe':
      return 'Jefe Comercial';
    default:
      return rolEnum || 'Personal';
  }
}

/**
 * Returns semantic CSS classes for role badge styling.
 * @param {string} rolEnum - Role enum identifier.
 * @returns {string} Tailwind CSS class string.
 */
export function getBadgeClass(rolEnum) {
  const r = (rolEnum || '').toUpperCase();
  if (r === 'PREVENTISTA' || r === 'PREVENTOR') {
    return 'bg-primary-light text-primary-dark border border-primary-border';
  }
  if (r === 'DISTRIBUIDOR') {
    return 'bg-warning-bg text-warning-text border border-warning-border';
  }
  if (r === 'ADMINISTRATIVO') {
    return 'bg-info-bg text-info-text border border-info-border';
  }
  return 'bg-purple-bg text-purple-text border border-purple-border';
}

/**
 * Formats a numeric value into ARS currency string.
 * @param {number|string|null} val - Amount to format.
 * @returns {string} Formatted currency string.
 */
export function formatPrecio(val) {
  if (val === null || val === undefined || isNaN(val)) return '$ --';
  return new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    maximumFractionDigits: 0,
  }).format(val);
}

/**
 * Extracts initials from a full name (e.g. "Lucía Martínez" -> "LM").
 * @param {string} name - Full person or unit name.
 * @returns {string} Initials string.
 */
export function getInitials(name) {
  if (!name) return '';
  return name
    .split(' ')
    .map((x) => x[0])
    .join('');
}


