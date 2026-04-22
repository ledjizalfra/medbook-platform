package it.pegaso.projectwork.medbook.bff.service.clinic;

import it.pegaso.projectwork.medbook.bff.server.model.CreateAssignmentBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.CreateClinicBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdateAssignmentBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdateClinicBffRequest;
import it.pegaso.projectwork.medbook.bff.client.WelcomeNotificationFeignClient;
import it.pegaso.projectwork.medbook.clinic.client.api.AssignmentsFeignClient;
import it.pegaso.projectwork.medbook.clinic.client.api.ClinicsFeignClient;
import it.pegaso.projectwork.medbook.clinic.client.model.AssignmentStatusApiEnum;
import it.pegaso.projectwork.medbook.clinic.client.model.ClinicStatusApiEnum;
import it.pegaso.projectwork.medbook.clinic.client.model.CreateAssignmentRequest;
import it.pegaso.projectwork.medbook.clinic.client.model.CreateClinicRequest;
import it.pegaso.projectwork.medbook.clinic.client.model.UpdateAssignmentRequest;
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

/** Implementazione proxy di ClinicBffService. Adatta i DTO BFF ai DTO clinic-dmn. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicBffServiceImpl implements ClinicBffService {

    private final ClinicsFeignClient clinicsClient;
    private final AssignmentsFeignClient assignmentsClient;
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

    @Override
    public ResponseEntity<MedBookApiVoidResponse> deleteClinic(MedBookContext context, String clinicId) {
        return clinicsClient.deleteClinic(context, clinicId);
    }

    @Override
    public ResponseEntity<MedBookApiResponse> createAssignment(MedBookContext context, String clinicId,
            CreateAssignmentBffRequest bffReq) {
        CreateAssignmentRequest req = new CreateAssignmentRequest();
        req.setDoctorId(bffReq.getDoctorId());
        req.setValidFrom(java.time.LocalDate.now());
        return assignmentsClient.postCreateAssignment(context, clinicId, req);
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getAllAssignments(MedBookContext context, String clinicId,
            Integer page, Integer size, String sort, String doctorId) {
        // clinic-dmn non supporta paginazione nelle assegnazioni - passa solo i filtri disponibili.
        return assignmentsClient.getAllAssignments(context, clinicId, doctorId, null);
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> updateAssignment(MedBookContext context, String clinicId,
            String assignmentId, UpdateAssignmentBffRequest bffReq) {
        UpdateAssignmentRequest req = new UpdateAssignmentRequest();
        // UpdateAssignmentRequest supporta solo validTo e status - non specialization.
        if (StringUtils.hasText(bffReq.getStatus())) {
            try {
                req.setStatus(AssignmentStatusApiEnum.valueOf(bffReq.getStatus()));
            } catch (IllegalArgumentException e) {
                log.warn("Stato assegnazione non riconosciuto: {}", bffReq.getStatus());
            }
        }
        return assignmentsClient.patchUpdateAssignment(context, clinicId, assignmentId, req);
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> deleteAssignment(MedBookContext context, String clinicId,
            String assignmentId) {
        return assignmentsClient.deleteAssignment(context, clinicId, assignmentId);
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> restoreClinic(MedBookContext context, String clinicId) {
        return clinicsClient.patchRestoreClinic(context, clinicId);
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> restoreAssignment(MedBookContext context, String clinicId,
            String assignmentId) {
        return assignmentsClient.patchRestoreAssignment(context, clinicId, assignmentId);
    }
}
