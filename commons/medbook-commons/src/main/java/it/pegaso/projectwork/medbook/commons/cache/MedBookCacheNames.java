package it.pegaso.projectwork.medbook.commons.cache;

/**
 * Costanti con i nomi logici delle cache condivise della piattaforma MedBook.
 * Tutti i moduli che usano la cache referenziano queste costanti —
 * mai stringhe hardcoded nelle annotazioni @Cacheable / @CacheEvict.
 */
public final class MedBookCacheNames {

    /** Cache dei template di disponibilità medico.
     * Usata dal BFF per evitare chiamate ripetute a doctor-dmn durante la ricerca slot.
     * Chiave: hash dei parametri di filtro (clinicId, doctorId, specialization, dayOfWeek).
     * TTL e dimensione massima configurabili via medbook.cache.doctor-availability-templates.* */
    public static final String DOCTOR_AVAILABILITY_TEMPLATES = "doctorAvailabilityTemplates";

    /** Cache del profilo attore (paziente o medico) per email.
     * Usata dal BFF ActorCacheService per evitare chiamate ripetute ai DMN
     * durante la risoluzione dell'utente autenticato (Approccio A).
     * Chiave: email (preferred_username dal JWT) — unica e immutabile per ogni attore.
     * TTL lungo: i dati cached (businessKey, email, CF, licenseNumber, dateOfBirth, gender)
     * sono immutabili o raramente modificabili.
     * TTL e dimensione massima configurabili via medbook.cache.actor-profile.* */
    public static final String ACTOR_PROFILE = "actorProfile";

    // Aggiungere nuovi nomi cache qui man mano che servono

    private MedBookCacheNames() {}
}
