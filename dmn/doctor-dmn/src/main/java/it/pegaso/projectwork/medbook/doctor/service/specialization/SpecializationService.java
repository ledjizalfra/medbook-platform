package it.pegaso.projectwork.medbook.doctor.service.specialization;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.doctor.server.model.SpecializationListOutput;

/**
 * Contratto del service per il catalogo statico delle specializzazioni mediche.
 * Implementato da {@link SpecializationServiceImpl}.
 */
public interface SpecializationService {

    // Recupera tutte le specializzazioni dal catalogo SPECIALIZATIONS, ordinate per nome
    SpecializationListOutput getAllSpecializations(MedBookContext context);
}
