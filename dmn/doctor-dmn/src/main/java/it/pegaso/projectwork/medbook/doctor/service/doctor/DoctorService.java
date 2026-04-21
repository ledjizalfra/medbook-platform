package it.pegaso.projectwork.medbook.doctor.service.doctor;

import it.pegaso.projectwork.medbook.doctor.server.model.*;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;

/**
 * Contratto del service per la gestione del ciclo di vita dei medici.
 * Implementato da {@link DoctorServiceImpl}.
 */
public interface DoctorService {

    // Crea un nuovo medico e restituisce la business key generata
    CreateDoctorOutput createDoctor(MedBookContext context, CreateDoctorRequest request);

    // Recupera un medico tramite la sua business key
    DoctorDetailOutput getDoctorById(MedBookContext context, String doctorId);

    // Recupera tutti i medici con paginazione e filtri opzionali (email per ricerca esatta)
    DoctorListOutput getAllDoctors(MedBookContext context, DoctorStatusApiEnum status,
                                   String firstName, String lastName,
                                   MedicalSpecializationApiEnum specialization, String email,
                                   String phone, String licenseNumber,
                                   java.time.LocalDate createdFrom, java.time.LocalDate createdTo,
                                   java.time.LocalDate updatedFrom, java.time.LocalDate updatedTo,
                                   Integer page, Integer size, String sort);

    // Aggiorna parzialmente i dati di un medico
    void updateDoctor(MedBookContext context, String doctorId, UpdateDoctorRequest request);

    // Elimina logicamente un medico (soft delete) e disattiva le sue disponibilità
    void deleteDoctor(MedBookContext context, String doctorId);

    // Ripristina un medico eliminato con soft delete
    void restoreDoctor(MedBookContext context, String doctorId);

    // Recupera lo stato dei consensi del medico identificato per email
    DoctorConsentStatusOutput getConsentStatus(MedBookContext context, String email);

    // Accettazione consensi al first-login (privacy obbligatorio, marketing opzionale)
    void acceptConsent(MedBookContext context, String email, AcceptDoctorConsentRequest request);

    // Modifica consensi facoltativi (solo marketing)
    void updateConsent(MedBookContext context, String email, UpdateDoctorConsentRequest request);

    // Medici ATTIVI con consenso privacy accettato — uso operativo (prenotazioni)
    DoctorListOutput getDoctorsForBooking(MedBookContext context, Integer page, Integer size, String sort);

    // Medici ATTIVI con consenso privacy accettato, filtrati per specializzazione — uso operativo (prenotazioni)
    DoctorListOutput getDoctorsForBookingBySpecialization(MedBookContext context,
            MedicalSpecializationApiEnum specialization, Integer page, Integer size, String sort);
}
