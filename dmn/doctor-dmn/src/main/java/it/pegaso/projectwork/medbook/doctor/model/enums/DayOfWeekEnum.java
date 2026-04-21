package it.pegaso.projectwork.medbook.doctor.model.enums;

/**
 * Enum che rappresenta i giorni della settimana per i template di disponibilità.
 * <p>
 * Definito come enum di dominio (anziché usare {@code java.time.DayOfWeek})
 * per garantire il controllo esplicito dei valori persistiti sul database
 * e la coerenza con la naming convention del progetto.
 * Persistito come stringa ({@code @Enumerated(EnumType.STRING)}) sul database.
 */
public enum DayOfWeekEnum {

    /** Lunedì. */
    LUNEDI,

    /** Martedì. */
    MARTEDI,

    /** Mercoledì. */
    MERCOLEDI,

    /** Giovedì. */
    GIOVEDI,

    /** Venerdì. */
    VENERDI,

    /** Sabato. */
    SABATO,

    /** Domenica. */
    DOMENICA
}
