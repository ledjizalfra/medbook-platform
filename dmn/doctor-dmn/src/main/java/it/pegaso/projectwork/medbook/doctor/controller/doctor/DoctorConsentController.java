package it.pegaso.projectwork.medbook.doctor.controller.doctor;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.doctor.server.api.DoctorConsentApi;
import it.pegaso.projectwork.medbook.doctor.server.model.AcceptDoctorConsentRequest;
import it.pegaso.projectwork.medbook.doctor.server.model.UpdateDoctorConsentRequest;
import it.pegaso.projectwork.medbook.doctor.service.doctor.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST per la gestione dei consensi privacy e marketing del medico.
 * Implementa l'interfaccia {@link DoctorConsentApi} generata dal plugin OpenAPI Generator.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DoctorConsentController implements DoctorConsentApi {

    private final DoctorService doctorService;

    @Override
    public ResponseEntity<MedBookApiResponse> getDoctorConsentStatus(
            MedBookContext context,
            String email) {

        MedBookApiResponse response = new MedBookApiResponse();
        response.setData(doctorService.getConsentStatus(context, email));
        response.setHttpStatus(HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> postAcceptDoctorConsent(
            MedBookContext context,
            String email,
            AcceptDoctorConsentRequest request) {

        doctorService.acceptConsent(context, email, request);
        MedBookApiVoidResponse response = new MedBookApiVoidResponse();
        response.setHttpStatus(HttpStatus.OK.value());
        response.setSuccess(true);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchUpdateDoctorConsent(
            MedBookContext context,
            String email,
            UpdateDoctorConsentRequest request) {

        doctorService.updateConsent(context, email, request);
        MedBookApiVoidResponse response = new MedBookApiVoidResponse();
        response.setHttpStatus(HttpStatus.OK.value());
        response.setSuccess(true);
        return ResponseEntity.ok(response);
    }
}
