package it.pegaso.projectwork.medbook.doctor.service.specialization;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.doctor.server.model.*;

/**
 * Contratto del service per la gestione degli assignment specializzazione-medico.
 * La business key composta e (doctorId, specializationId).
 * Implementato da {@link DoctorSpecializationServiceImpl}.
 */
public interface DoctorSpecializationService {

    // Associa una specializzazione al medico — verifica unicita (doctorId, specializationId)
    void createDoctorSpecialization(MedBookContext context, String doctorId,
                                     CreateDoctorSpecializationRequest request);

    // Recupera tutti gli assignment di un medico
    DoctorSpecializationListOutput getAllDoctorSpecializations(MedBookContext context, String doctorId);

    // Aggiornamento parziale — solo isPrimary modificabile
    void updateDoctorSpecialization(MedBookContext context, String doctorId, String specializationId,
                                     UpdateDoctorSpecializationRequest request);

    // Soft delete — blocca se e l'unica specializzazione rimasta
    void deleteDoctorSpecialization(MedBookContext context, String doctorId, String specializationId);

    // Ripristino soft delete
    void restoreDoctorSpecialization(MedBookContext context, String doctorId, String specializationId);
}
