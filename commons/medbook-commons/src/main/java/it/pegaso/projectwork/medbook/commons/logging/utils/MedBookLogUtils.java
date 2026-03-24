package it.pegaso.projectwork.medbook.commons.logging.utils;

/**
 * Classe di utilità per il logging del tempo di esecuzione delle operazioni.
 * Fornisce metodi statici riutilizzabili da tutti i MS del progetto MedBook.
 */
public final class MedBookLogUtils {

    private MedBookLogUtils() {
        // Costruttore privato — classe di sole utility, non istanziabile
    }

    /**
     * Registra il tempo di inizio di un'operazione e restituisce il timestamp.
     *
     * @return timestamp di inizio in millisecondi
     */
    public static long startTimer() {
        return System.currentTimeMillis();
    }

    /**
     * Calcola e restituisce il tempo di esecuzione in millisecondi.
     *
     * @param startTime timestamp di inizio restituito da startTimer()
     * @return tempo di esecuzione in millisecondi
     */
    public static long elapsedTime(long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Restituisce il tempo di esecuzione formattato come stringa leggibile.
     *
     * @param startTime timestamp di inizio restituito da startTimer()
     * @return stringa formattata es. "125 ms"
     */
    public static String elapsedTimeFormatted(long startTime) {
        return elapsedTime(startTime) + " ms";
    }
}