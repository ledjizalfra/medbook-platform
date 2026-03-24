package it.pegaso.projectwork.medbook.patient.client.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Risposta alla creazione del paziente — contiene solo la business key generata
 */

@Schema(name = "CreatePatientResponse", description = "Risposta alla creazione del paziente — contiene solo la business key generata")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-24T19:06:26.867401900+01:00[Europe/Rome]", comments = "Generator version: 7.4.0")
public class CreatePatientResponse {

  private String patientId;

  public CreatePatientResponse patientId(String patientId) {
    this.patientId = patientId;
    return this;
  }

  /**
   * Business key generata nel formato PAT-{seq}
   * @return patientId
  */
  
  @Schema(name = "patientId", example = "PAT-1", description = "Business key generata nel formato PAT-{seq}", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("patientId")
  public String getPatientId() {
    return patientId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreatePatientResponse createPatientResponse = (CreatePatientResponse) o;
    return Objects.equals(this.patientId, createPatientResponse.patientId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(patientId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreatePatientResponse {\n");
    sb.append("    patientId: ").append(toIndentedString(patientId)).append("\n");
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

