package it.pegaso.projectwork.medbook.bff.service.clinic;

import it.pegaso.projectwork.medbook.bff.server.model.CreateClinicBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdateClinicBffRequest;
import it.pegaso.projectwork.medbook.appointment.client.api.AppointmentsFeignClient;
import it.pegaso.projectwork.medbook.appointment.client.model.AppointmentStatusApiEnum;
import it.pegaso.projectwork.medbook.appointment.client.model.CancelAppointmentRequest;
import it.pegaso.projectwork.medbook.appointment.client.model.CancelledByApiEnum;
import it.pegaso.projectwork.medbook.bff.client.WelcomeNotificationFeignClient;
import it.pegaso.projectwork.medbook.clinic.client.api.ClinicsFeignClient;
import it.pegaso.projectwork.medbook.clinic.client.model.ClinicStatusApiEnum;
import it.pegaso.projectwork.medbook.clinic.client.model.CreateClinicRequest;
import it.pegaso.projectwork.medbook.clinic.client.model.UpdateClinicRequest;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Implementazione proxy di ClinicBffService. Adatta i DTO BFF ai DTO clinic-dmn. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicBffServiceImpl implements ClinicBffService {

    private final AppointmentsFeignClient appointmentsClient;
    private final ClinicsFeignClient clinicsClient;
    private final WelcomeNotificationFeignClient welcomeNotificationClient;

    @Override
    public ResponseEntity<MedBookApiResponse> createClinic(MedBookContext context,
            CreateClinicBffRequest bffReq) {
        // Validazione T&C obbligatoria
        if (!Boolean.TRUE.equals(bffReq.getTermsAccepted())) {
            throw new MedBookBusinessException(
                    MedBookErrorCode.VALIDATION_ERROR,
                    "L'accettazione dei Termini di Servizio è obbligatoria per la registrazione della sede");
        }

        CreateClinicRequest req = new CreateClinicRequest();
        req.setName(bffReq.getName());
        req.setEmail(bffReq.getEmail());
        req.setPhone(bffReq.getPhone());
        req.setAddress(bffReq.getAddress());
        req.setCity(bffReq.getCity());
        req.setPostalCode(bffReq.getPostalCode());
        req.setProvince(bffReq.getProvince());
        req.setTermsAccepted(true);
        ResponseEntity<MedBookApiResponse> response = clinicsClient.postCreateClinic(context, req);

        // Invia notifica di benvenuto alla clinica — best-effort.
        sendClinicWelcomeNotification(bffReq);

        return response;
    }

    /** Invia notifica di benvenuto alla clinica — best-effort, non propaga eccezioni. */
    private void sendClinicWelcomeNotification(CreateClinicBffRequest bffReq) {
        try {
            java.util.Map<String, String> body = java.util.Map.of(
                    "clinicId", bffReq.getName(),
                    "clinicName", bffReq.getName(),
                    "clinicEmail", bffReq.getEmail());
            welcomeNotificationClient.sendClinicWelcome(body);
            log.info("Notifica benvenuto clinica inviata per {}", bffReq.getName());
        } catch (Exception e) {
            log.error("Errore invio notifica benvenuto clinica {}: {}", bffReq.getName(), e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getAllClinics(MedBookContext context, Integer page,
            Integer size, String sort, String status, String city, String name, String email,
            String province, String phone, String address, String postalCode,
            LocalDate createdFrom, LocalDate createdTo,
            LocalDate updatedFrom, LocalDate updatedTo) {
        // Converte il filtro status in ClinicStatusApiEnum se presente.
        ClinicStatusApiEnum statusEnum = null;
        if (StringUtils.hasText(status)) {
            try {
                statusEnum = ClinicStatusApiEnum.valueOf(status);
            } catch (IllegalArgumentException e) {
                log.warn("Stato sede non riconosciuto: {}", status);
            }
        }
        return clinicsClient.getAllClinics(context, page, size, sort, statusEnum, city,
                name, email, province, phone, address, postalCode,
                createdFrom, createdTo, updatedFrom, updatedTo);
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getClinicById(MedBookContext context, String clinicId) {
        return clinicsClient.getClinicById(context, clinicId);
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> updateClinic(MedBookContext context, String clinicId,
            UpdateClinicBffRequest bffReq) {
        UpdateClinicRequest req = new UpdateClinicRequest();
        req.setName(bffReq.getName());
        req.setEmail(bffReq.getEmail());
        req.setPhone(bffReq.getPhone());
        req.setAddress(bffReq.getAddress());
        req.setCity(bffReq.getCity());
        req.setPostalCode(bffReq.getPostalCode());
        req.setProvince(bffReq.getProvince());
        if (StringUtils.hasText(bffReq.getStatus())) {
            try {
                req.setStatus(ClinicStatusApiEnum.valueOf(bffReq.getStatus()));
            } catch (IllegalArgumentException e) {
                log.warn("Stato sede non riconosciuto: {}", bffReq.getStatus());
            }
        }
        return clinicsClient.patchUpdateClinic(context, clinicId, req);
    }

    /**
     * Cancellazione logica a cascata della clinica:
     * 1. Annulla tutti gli appuntamenti PRENOTATO per la clinica
     * 2. Cancella la clinica (clinic-dmn)
     */
    @Override
    @SuppressWarnings("unchecked")
    public ResponseEntity<MedBookApiVoidResponse> deleteClinic(MedBookContext context, String clinicId) {
        // Annulla appuntamenti PRENOTATO per questa clinica
        try {
            ResponseEntity<MedBookApiResponse> resp = appointmentsClient.getListAppointments(
                    context, 0, 1000, null, null, null, clinicId,
                    AppointmentStatusApiEnum.PRENOTATO, null, null);
            if (resp.getBody() != null && resp.getBody().getData() != null) {
                Object data = resp.getBody().getData();
                List<?> appointments = data instanceof List ? (List<?>) data : List.of();
                for (Object appt : appointments) {
                    if (appt instanceof Map) {
                        String apptId = (String) ((Map<String, Object>) appt).get("appointmentId");
                        if (apptId != null) {
                            try {
                                CancelAppointmentRequest cancelReq = new CancelAppointmentRequest();
                                cancelReq.setCancellationReason("Clinica disattivata");
                                cancelReq.setCancelledBy(CancelledByApiEnum.AMMINISTRATORE);
                                appointmentsClient.patchCancelAppointment(context, apptId, cancelReq);
                                log.info("Appuntamento {} annullato (cascata clinica {})", apptId, clinicId);
                            } catch (Exception e) {
                                log.warn("Errore annullamento appuntamento {}: {}", apptId, e.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Errore recupero appuntamenti clinica {}: {}", clinicId, e.getMessage());
        }

        return clinicsClient.deleteClinic(context, clinicId);
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> restoreClinic(MedBookContext context, String clinicId) {
        return clinicsClient.patchRestoreClinic(context, clinicId);
    }
}
