package it.pegaso.projectwork.medbook.bff.service.clinic;

import it.pegaso.projectwork.medbook.bff.server.model.CreateClinicBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdateClinicBffRequest;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

/** Proxy verso clinic-dmn per operazioni CRUD sulle sedi. */
public interface ClinicBffService {

    ResponseEntity<MedBookApiResponse> createClinic(MedBookContext context, CreateClinicBffRequest request);

    ResponseEntity<MedBookApiResponse> getAllClinics(MedBookContext context, Integer page, Integer size,
            String sort, String status, String city, String name, String email, String province,
            String phone, String address, String postalCode,
            LocalDate createdFrom, LocalDate createdTo, LocalDate updatedFrom, LocalDate updatedTo);

    ResponseEntity<MedBookApiResponse> getClinicById(MedBookContext context, String clinicId);

    ResponseEntity<MedBookApiVoidResponse> updateClinic(MedBookContext context, String clinicId,
            UpdateClinicBffRequest request);

    ResponseEntity<MedBookApiVoidResponse> deleteClinic(MedBookContext context, String clinicId);

    ResponseEntity<MedBookApiVoidResponse> restoreClinic(MedBookContext context, String clinicId);
}
