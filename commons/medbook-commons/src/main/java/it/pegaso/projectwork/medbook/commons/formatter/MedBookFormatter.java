package it.pegaso.projectwork.medbook.commons.formatter;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Componente centralizzato per la normalizzazione dei dati in ingresso
 * e la formattazione dei dati in uscita verso il frontend.
 *
 * Normalizzazione input: applicata nel BFF prima di chiamare i DMN,
 * garantisce che i dati salvati nel DB siano sempre consistenti
 * indipendentemente da come l'utente li ha digitati.
 *
 * Formattazione output: applicata nel BFF prima di restituire
 * la risposta al FE, converte i tipi Java in formati leggibili.
 */
public class MedBookFormatter {

    // =========================================================================
    // Formattazione nomi propri - input
    // =========================================================================

    /*
     * Normalizza un nome proprio: ogni parola ha la prima lettera in maiuscolo
     * e le restanti in minuscolo. Gestisce nomi composti separati da spazio.
     * Esempi: "mario" -> "Mario", "marie laure" -> "Marie Laure",
     *         "JEAN-PIERRE" -> "Jean-pierre"
     */
    public String formatFirstName(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return trimmed;
        return Arrays.stream(trimmed.split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase()
                        + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    /*
     * Normalizza un cognome: tutto maiuscolo.
     * Esempi: "Smith" -> "SMITH", "kenfact dogmo" -> "KENFACT DOGMO"
     */
    public String formatLastName(String value) {
        if (value == null) return null;
        return value.trim().toUpperCase();
    }

    // =========================================================================
    // Formattazione nomi medici — output
    // =========================================================================

    /*
     * Costruisce il nome completo del medico con il titolo professionale
     * adeguato al sesso: "Dott." per MASCHILE, "Dott.ssa" per FEMMINILE.
     * Il nome viene normalizzato (prima lettera maiuscola) e il cognome
     * convertito tutto in maiuscolo per coerenza grafica.
     * Esempi:
     *   ("mario", "rossi", "MASCHILE")   -> "Dott. Mario ROSSI"
     *   ("maria", "bianchi", "FEMMINILE") -> "Dott.ssa Maria BIANCHI"
     */
    public String formatDoctorCompleteName(String firstName, String lastName, String gender) {
        String title = "FEMMINILE".equalsIgnoreCase(gender) ? "Dott.ssa" : "Dott.";
        String fmtFirst = formatFirstName(firstName);
        String fmtLast  = formatLastName(lastName);
        return title + " " + fmtFirst + " " + fmtLast;
    }

    // =========================================================================
    // Formattazione altri campi stringa - input
    // =========================================================================

    /*
     * Normalizza un indirizzo email: tutto minuscolo con trim.
     * Esempio: "Mario.ROSSI@Gmail.com " -> "mario.rossi@gmail.com"
     */
    public String formatEmail(String value) {
        if (value == null) return null;
        return value.trim().toLowerCase();
    }

    /*
     * Normalizza un numero di telefono: rimuove spazi interni,
     * conserva il prefisso internazionale '+' se presente.
     * Esempio: "+39 333 123 4567" -> "+393331234567"
     */
    public String formatPhone(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.startsWith("+")) {
            return "+" + trimmed.substring(1).replaceAll("[^0-9]", "");
        }
        return trimmed.replaceAll("[^0-9]", "");
    }

    /*
     * Normalizza una citta': ogni parola con prima lettera maiuscola.
     * Stesso comportamento di formatFirstName.
     * Esempio: "ROMA" -> "Roma", "new york" -> "New York"
     */
    public String formatCity(String value) {
        return formatFirstName(value);
    }

    /*
     * Normalizza il nome provincia: tutto maiuscolo con trim.
     * A DB si salva il nome per esteso. La sigla e usata solo nel codice per il CF.
     * Esempi: 'torino' -> 'TORINO', 'Milano' -> 'MILANO'
     */
    public String formatProvince(String value) {
        if (value == null) return null;
        return value.trim().toUpperCase();
    }

    /*
     * Normalizza un CAP: solo cifre con trim.
     * Esempio: "20 121" -> "20121"
     */
    public String formatPostalCode(String value) {
        if (value == null) return null;
        return value.trim().replaceAll("[^0-9]", "");
    }

    /*
     * Normalizza un codice fiscale: tutto maiuscolo con trim.
     * Esempio: "rssmra80a01h501u" -> "RSSMRA80A01H501U"
     */
    public String formatFiscalCode(String value) {
        if (value == null) return null;
        return value.trim().toUpperCase();
    }

    /*
     * Applica solo il trim a una stringa generica.
     * Da usare per tutti i campi che non hanno una regola specifica.
     */
    public String trim(String value) {
        if (value == null) return null;
        return value.trim();
    }
}
