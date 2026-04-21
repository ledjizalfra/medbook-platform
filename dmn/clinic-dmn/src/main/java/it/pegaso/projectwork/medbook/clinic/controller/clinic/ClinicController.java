package it.pegaso.projectwork.medbook.clinic.controller.clinic;

import it.pegaso.projectwork.medbook.clinic.server.api.ClinicsApi;
import it.pegaso.projectwork.medbook.clinic.server.model.*;
import it.pegaso.projectwork.medbook.clinic.service.clinic.ClinicService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ClinicController implements ClinicsApi {

    private final ClinicService clinicService;

    @Override
    public ResponseEntity<MedBookApiResponse> postCreateClinic(
            MedBookContext context, CreateClinicRequest createClinicRequest) {

        CreateClinicOutput output = clinicService.createClinic(context, createClinicRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MedBookApiResponse()
                        .httpStatus(HttpStatus.CREATED.value())
                        .success(true)
                        .timestamp(LocalDateTime.now())
                        .data(output));
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getAllClinics(
            MedBookContext context, Integer page, Integer size, String sort,
            ClinicStatusApiEnum status, String city,
            String name, String email, String province,
            String phone, String address, String postalCode,
            LocalDate createdFrom, LocalDate createdTo,
            LocalDate updatedFrom, LocalDate updatedTo) {

        ClinicListOutput output = clinicService.getAllClinics(context, status, city,
                name, email, province, phone, address, postalCode,
                createdFrom, createdTo, updatedFrom, updatedTo,
                page, size, sort);

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output.getClinics())
                .page(output.getPage()));
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getClinicById(
            MedBookContext context, String clinicId) {

        ClinicDetailOutput output = clinicService.getClinicById(context, clinicId);

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output));
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchUpdateClinic(
            MedBookContext context, String clinicId, UpdateClinicRequest updateClinicRequest) {

        clinicService.updateClinic(context, clinicId, updateClinicRequest);

        return ResponseEntity.ok(new MedBookApiVoidResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now()));
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> deleteClinic(
            MedBookContext context, String clinicId) {

        clinicService.deleteClinic(context, clinicId);

        return ResponseEntity.ok(new MedBookApiVoidResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now()));
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchRestoreClinic(
            MedBookContext context, String clinicId) {

        clinicService.restoreClinic(context, clinicId);

        return ResponseEntity.ok(new MedBookApiVoidResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now()));
    }
}
