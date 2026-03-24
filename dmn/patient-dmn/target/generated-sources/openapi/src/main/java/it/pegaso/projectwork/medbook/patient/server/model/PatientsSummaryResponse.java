package it.pegaso.projectwork.medbook.patient.server.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import it.pegaso.projectwork.medbook.patient.server.model.MedBookPageResponse;
import it.pegaso.projectwork.medbook.patient.server.model.PatientItemSummaryResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Risposta paginata della lista pazienti
 */

@Schema(name = "PatientsSummaryResponse", description = "Risposta paginata della lista pazienti")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-24T19:05:33.770195300+01:00[Europe/Rome]", comments = "Generator version: 7.4.0")
public class PatientsSummaryResponse {

  @Valid
  private List<@Valid PatientItemSummaryResponse> patients;

  private MedBookPageResponse page;

  public PatientsSummaryResponse patients(List<@Valid PatientItemSummaryResponse> patients) {
    this.patients = patients;
    return this;
  }

  public PatientsSummaryResponse addPatientsItem(PatientItemSummaryResponse patientsItem) {
    if (this.patients == null) {
      this.patients = new ArrayList<>();
    }
    this.patients.add(patientsItem);
    return this;
  }

  /**
   * Lista dei pazienti nella pagina corrente
   * @return patients
  */
  @Valid 
  @Schema(name = "patients", description = "Lista dei pazienti nella pagina corrente", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("patients")
  public List<@Valid PatientItemSummaryResponse> getPatients() {
    return patients;
  }

  public void setPatients(List<@Valid PatientItemSummaryResponse> patients) {
    this.patients = patients;
  }

  public PatientsSummaryResponse page(MedBookPageResponse page) {
    this.page = page;
    return this;
  }

  /**
   * Get page
   * @return page
  */
  @Valid 
  @Schema(name = "page", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("page")
  public MedBookPageResponse getPage() {
    return page;
  }

  public void setPage(MedBookPageResponse page) {
    this.page = page;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PatientsSummaryResponse patientsSummaryResponse = (PatientsSummaryResponse) o;
    return Objects.equals(this.patients, patientsSummaryResponse.patients) &&
        Objects.equals(this.page, patientsSummaryResponse.page);
  }

  @Override
  public int hashCode() {
    return Objects.hash(patients, page);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PatientsSummaryResponse {\n");
    sb.append("    patients: ").append(toIndentedString(patients)).append("\n");
    sb.append("    page: ").append(toIndentedString(page)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

