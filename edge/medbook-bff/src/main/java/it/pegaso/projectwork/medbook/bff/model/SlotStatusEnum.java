package it.pegaso.projectwork.medbook.bff.model;

/** Stato di uno slot di disponibilita nel calendario del medico. */
public enum SlotStatusEnum {
    /** Slot disponibile - la prenotazione e possibile. */
    LIBERO,
    /** Slot occupato - esiste gia una prenotazione per questo slot. */
    PRENOTATO
}
