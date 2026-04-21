/**
 * Costanti per i componenti UI condivisi.
 *
 * Raggruppate per categoria per chiarire il contesto d'uso.
 */

// Durata delle snackbar in millisecondi - usate in tutti i form e le liste
export const SNACKBAR_DURATION = {
  SHORT: 3000,  // messaggi brevi: successo, info, errori semplici
  LONG:  4000,  // messaggi che richiedono piu tempo di lettura: errori di salvataggio
} as const;

// Canali di notifica supportati dalla piattaforma
export const NOTIFICATION_CHANNEL = {
  EMAIL: 'EMAIL',
  SMS:   'SMS',
} as const;

// Valori di stato del dominio condivisi tra piu componenti
export const APPOINTMENT_STATUS = {
  PRENOTATO:      'PRENOTATO',
  IN_CORSO:       'IN_CORSO',
  COMPLETATO:     'COMPLETATO',
  CANCELLATO:     'CANCELLATO',
  NON_PRESENTATO: 'NON_PRESENTATO',
} as const;

export const SLOT_STATUS = {
  LIBERO:     'LIBERO',
  OCCUPATO:   'OCCUPATO',
} as const;

// Identità del brand — modificare qui per aggiornare tutta l'app
export const BRAND = {
  NAME: 'MedBook',
  SUFFIX: 'Platform'
} as const;

// Mappa specializzazione => icona Material — usata in home, services e ovunque si mostrano specializzazioni
export const SPECIALIZATION_ICON_MAP: Record<string, string> = {
  cardiologia:     'favorite',
  dermatologia:    'healing',
  neurologia:      'psychology',
  ortopedia:       'accessibility_new',
  pediatria:       'child_care',
  ginecologia:     'pregnant_woman',
  oculistica:      'visibility',
  psichiatria:     'self_improvement',
  oncologia:       'biotech',
  endocrinologia:  'science',
} as const;

// Mappa genere enum => label italiana — usata in profili e form
export const GENDER_LABEL_MAP: Record<string, string> = {
  MASCHILE:  'Maschile',
  FEMMINILE: 'Femminile',
} as const;

// Genere enum => codice CF (M/F) — usato nel validatore codice fiscale
export const GENDER_CF_MAP: Record<string, string> = {
  MASCHILE:  'M',
  FEMMINILE: 'F',
} as const;
