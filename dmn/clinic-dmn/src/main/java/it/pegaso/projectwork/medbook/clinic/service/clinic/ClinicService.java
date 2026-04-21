package it.pegaso.projectwork.medbook.clinic.service.clinic;

import it.pegaso.projectwork.medbook.clinic.server.model.*;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;

import java.time.LocalDate;

public interface ClinicService {

    CreateClinicOutput createClinic(MedBookContext context, CreateClinicRequest request);

    ClinicListOutput getAllClinics(MedBookContext context, ClinicStatusApiEnum status, String city,
                                   String name, String email, String province,
                                   String phone, String address, String postalCode,
                                   LocalDate createdFrom, LocalDate createdTo,
                                   LocalDate updatedFrom, LocalDate updatedTo,
                                   Integer page, Integer size, String sort);

    ClinicDetailOutput getClinicById(MedBookContext context, String clinicId);

    void updateClinic(MedBookContext context, String clinicId, UpdateClinicRequest request);

    void deleteClinic(MedBookContext context, String clinicId);

    void restoreClinic(MedBookContext context, String clinicId);
}
