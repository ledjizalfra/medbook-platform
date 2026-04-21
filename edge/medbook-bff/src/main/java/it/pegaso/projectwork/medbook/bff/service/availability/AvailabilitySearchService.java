package it.pegaso.projectwork.medbook.bff.service.availability;

import it.pegaso.projectwork.medbook.bff.server.model.SlotViewResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Servizio per la ricerca degli slot disponibili.
 * Genera gli slot in memoria a partire dai template di disponibilita di doctor-dmn,
 * marca quelli occupati con i dati di appointment-dmn,
 * e arricchisce il risultato con nomi medico e sede. */
public interface AvailabilitySearchService {

    /** Cerca gli slot disponibili nel range [dateFrom, dateTo] con i filtri opzionali.
     * Se dateTo supera l'orizzonte massimo configurato, viene troncata automaticamente. */
    List<SlotViewResponse> searchAvailableSlots(MedBookContext context, String clinicId,
            String doctorId, String specialization, LocalDate dateFrom, LocalDate dateTo);

    /** Restituisce i dati di riferimento per i filtri della ricerca disponibilita.
     * Estrae specializzazioni e medici distinti dai template di disponibilita attivi.
     * Risultato: { specializations: [...], doctors: [{doctorId, fullName, specializations: [...]}] } */
    Map<String, Object> getAvailabilityFilters(MedBookContext context);
}
