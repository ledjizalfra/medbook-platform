/**
 * Costanti TTL (Time To Live) per la cache in-memory degli store.
 * Valori in millisecondi. Modificare qui per aggiustare la durata della cache.
 */
export const STORE_TTL = {
  /** Catalogo specializzazioni — dato quasi statico, TTL lungo */
  SPECIALIZATIONS: 30 * 60 * 1000,  // 30 minuti

  /** Lista cliniche — aggiornamento poco frequente */
  CLINICS: 10 * 60 * 1000,          // 10 minuti

  /** Lista/dettaglio medici — aggiornamento poco frequente */
  DOCTORS: 5 * 60 * 1000,           // 5 minuti

  /** Lista/dettaglio pazienti — aggiornamento moderato */
  PATIENTS: 5 * 60 * 1000,          // 5 minuti

  /** Prenotazioni — dato piu volatile */
  APPOINTMENTS: 2 * 60 * 1000,      // 2 minuti

  /** Disponibilita medici — dato moderatamente volatile */
  AVAILABILITIES: 3 * 60 * 1000,    // 3 minuti
} as const;
