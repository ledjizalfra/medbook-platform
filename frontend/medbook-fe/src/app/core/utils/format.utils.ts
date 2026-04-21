/**
 * Funzioni di formattazione centralizzate per il FE.
 *
 * Regole:
 * - Nome (firstName): Title Case => "Mario"
 * - Cognome (lastName): UPPERCASE => "ROSSI"
 * - Nome completo: "Mario ROSSI"
 */

/** Converte in Title Case: prima lettera maiuscola, resto minuscolo per ogni parola */
export function titleCase(value: string | null | undefined): string {
  if (!value) return '';
  return value.trim().split(/\s+/)
    .map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
    .join(' ');
}

/** Formatta un nome completo: firstName in Title Case + lastName in UPPERCASE */
export function formatFullName(firstName: string | null | undefined, lastName: string | null | undefined): string {
  const first = titleCase(firstName);
  const last = (lastName ?? '').trim().toUpperCase();
  return `${first} ${last}`.trim();
}
