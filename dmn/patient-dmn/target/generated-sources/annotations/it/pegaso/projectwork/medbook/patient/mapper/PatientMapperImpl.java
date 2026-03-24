package it.pegaso.projectwork.medbook.patient.mapper;

import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import it.pegaso.projectwork.medbook.patient.entity.PatientEntity;
import it.pegaso.projectwork.medbook.patient.entity.enums.GenderEnum;
import it.pegaso.projectwork.medbook.patient.server.model.CreatePatientRequest;
import it.pegaso.projectwork.medbook.patient.server.model.GenderApiEnum;
import it.pegaso.projectwork.medbook.patient.server.model.GetAllPatientsFilter;
import it.pegaso.projectwork.medbook.patient.server.model.MedBookPageResponse;
import it.pegaso.projectwork.medbook.patient.server.model.PatientDetailResponse;
import it.pegaso.projectwork.medbook.patient.server.model.PatientItemSummaryResponse;
import it.pegaso.projectwork.medbook.patient.server.model.PatientStatusApiEnum;
import it.pegaso.projectwork.medbook.patient.server.model.UpdatePatientRequest;
import it.pegaso.projectwork.medbook.patient.validator.dto.ValidationRequest;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-24T19:06:17+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class PatientMapperImpl implements PatientMapper {

    @Override
    public ValidationRequest mapToValidationRequest(CreatePatientRequest request) {
        if ( request == null ) {
            return null;
        }

        ValidationRequest.ValidationRequestBuilder validationRequest = ValidationRequest.builder();

        validationRequest.fiscalCode( request.getFiscalCode() );
        validationRequest.email( request.getEmail() );

        validationRequest.validationRequestType( ValidationRequestTypeEnum.IS_CREATE );

        return validationRequest.build();
    }

    @Override
    public ValidationRequest mapToValidationRequest(UpdatePatientRequest request) {
        if ( request == null ) {
            return null;
        }

        ValidationRequest.ValidationRequestBuilder validationRequest = ValidationRequest.builder();

        validationRequest.patientId( request.getPatientId() );
        validationRequest.email( request.getEmail() );

        validationRequest.validationRequestType( ValidationRequestTypeEnum.IS_UPADTE );

        return validationRequest.build();
    }

    @Override
    public PatientEntity mapToPatientEntity(CreatePatientRequest request) {
        if ( request == null ) {
            return null;
        }

        PatientEntity.PatientEntityBuilder patientEntity = PatientEntity.builder();

        patientEntity.firstName( request.getFirstName() );
        patientEntity.lastName( request.getLastName() );
        patientEntity.dateOfBirth( request.getDateOfBirth() );
        patientEntity.fiscalCode( request.getFiscalCode() );
        patientEntity.gender( genderApiEnumToGenderEnum( request.getGender() ) );
        patientEntity.email( request.getEmail() );
        patientEntity.phone( request.getPhone() );
        patientEntity.address( request.getAddress() );
        patientEntity.city( request.getCity() );
        patientEntity.postalCode( request.getPostalCode() );
        patientEntity.province( request.getProvince() );

        return patientEntity.build();
    }

    @Override
    public PatientDetailResponse mapToPatientDetailResponse(PatientEntity entity) {
        if ( entity == null ) {
            return null;
        }

        PatientDetailResponse patientDetailResponse = new PatientDetailResponse();

        patientDetailResponse.setPatientId( entity.getPatientId() );
        patientDetailResponse.setFirstName( entity.getFirstName() );
        patientDetailResponse.setLastName( entity.getLastName() );
        patientDetailResponse.setDateOfBirth( entity.getDateOfBirth() );
        patientDetailResponse.setFiscalCode( entity.getFiscalCode() );
        patientDetailResponse.setGender( genderEnumToGenderApiEnum( entity.getGender() ) );
        patientDetailResponse.setEmail( entity.getEmail() );
        patientDetailResponse.setPhone( entity.getPhone() );
        patientDetailResponse.setAddress( entity.getAddress() );
        patientDetailResponse.setCity( entity.getCity() );
        patientDetailResponse.setPostalCode( entity.getPostalCode() );
        patientDetailResponse.setProvince( entity.getProvince() );
        patientDetailResponse.setStatus( mapToApiStatusEnum( entity.getStatus() ) );
        patientDetailResponse.setCreatedAt( entity.getCreatedAt() );
        patientDetailResponse.setUpdatedAt( entity.getUpdatedAt() );

        return patientDetailResponse;
    }

    @Override
    public GetAllPatientsFilter mapToGetAllPatientsFilter(PatientStatusApiEnum status, String lastName, String city, String email, String fiscalCode) {
        if ( status == null && lastName == null && city == null && email == null && fiscalCode == null ) {
            return null;
        }

        GetAllPatientsFilter getAllPatientsFilter = new GetAllPatientsFilter();

        getAllPatientsFilter.setStatus( status );
        getAllPatientsFilter.setLastName( lastName );
        getAllPatientsFilter.setCity( city );
        getAllPatientsFilter.setEmail( email );
        getAllPatientsFilter.setFiscalCode( fiscalCode );

        return getAllPatientsFilter;
    }

    @Override
    public List<PatientItemSummaryResponse> mapToPatientSummaryResponse(List<PatientEntity> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<PatientItemSummaryResponse> list = new ArrayList<PatientItemSummaryResponse>( entityList.size() );
        for ( PatientEntity patientEntity : entityList ) {
            list.add( mapToPatientItemSummaryResponse( patientEntity ) );
        }

        return list;
    }

    @Override
    public PatientItemSummaryResponse mapToPatientItemSummaryResponse(PatientEntity entity) {
        if ( entity == null ) {
            return null;
        }

        PatientItemSummaryResponse patientItemSummaryResponse = new PatientItemSummaryResponse();

        patientItemSummaryResponse.setPatientId( entity.getPatientId() );
        patientItemSummaryResponse.setFirstName( entity.getFirstName() );
        patientItemSummaryResponse.setLastName( entity.getLastName() );
        patientItemSummaryResponse.setEmail( entity.getEmail() );
        patientItemSummaryResponse.setStatus( mapToApiStatusEnum( entity.getStatus() ) );

        return patientItemSummaryResponse;
    }

    @Override
    public void updatePatientEntity(PatientEntity entity, UpdatePatientRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getPatientId() != null ) {
            entity.setPatientId( request.getPatientId() );
        }
        if ( request.getFirstName() != null ) {
            entity.setFirstName( request.getFirstName() );
        }
        if ( request.getLastName() != null ) {
            entity.setLastName( request.getLastName() );
        }
        if ( request.getDateOfBirth() != null ) {
            entity.setDateOfBirth( request.getDateOfBirth() );
        }
        if ( request.getEmail() != null ) {
            entity.setEmail( request.getEmail() );
        }
        if ( request.getPhone() != null ) {
            entity.setPhone( request.getPhone() );
        }
        if ( request.getAddress() != null ) {
            entity.setAddress( request.getAddress() );
        }
        if ( request.getCity() != null ) {
            entity.setCity( request.getCity() );
        }
        if ( request.getPostalCode() != null ) {
            entity.setPostalCode( request.getPostalCode() );
        }
        if ( request.getProvince() != null ) {
            entity.setProvince( request.getProvince() );
        }
    }

    @Override
    public MedBookPageResponse mapToMedBookPageResponse(long totalElements, int totalPages, int size, int number, boolean first, boolean last, boolean empty) {

        MedBookPageResponse medBookPageResponse = new MedBookPageResponse();

        medBookPageResponse.setTotalElements( totalElements );
        medBookPageResponse.setTotalPages( totalPages );
        medBookPageResponse.setSize( size );
        medBookPageResponse.setNumber( number );
        medBookPageResponse.setLast( first );
        medBookPageResponse.setFirst( last );
        medBookPageResponse.setEmpty( empty );

        return medBookPageResponse;
    }

    protected GenderEnum genderApiEnumToGenderEnum(GenderApiEnum genderApiEnum) {
        if ( genderApiEnum == null ) {
            return null;
        }

        GenderEnum genderEnum;

        switch ( genderApiEnum ) {
            case MALE: genderEnum = GenderEnum.MALE;
            break;
            case FEMALE: genderEnum = GenderEnum.FEMALE;
            break;
            default: throw new IllegalArgumentException( "Unexpected enum constant: " + genderApiEnum );
        }

        return genderEnum;
    }

    protected GenderApiEnum genderEnumToGenderApiEnum(GenderEnum genderEnum) {
        if ( genderEnum == null ) {
            return null;
        }

        GenderApiEnum genderApiEnum;

        switch ( genderEnum ) {
            case MALE: genderApiEnum = GenderApiEnum.MALE;
            break;
            case FEMALE: genderApiEnum = GenderApiEnum.FEMALE;
            break;
            default: throw new IllegalArgumentException( "Unexpected enum constant: " + genderEnum );
        }

        return genderApiEnum;
    }
}
