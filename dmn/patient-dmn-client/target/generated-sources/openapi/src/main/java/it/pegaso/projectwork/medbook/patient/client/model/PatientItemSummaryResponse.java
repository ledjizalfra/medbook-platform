package it.pegaso.projectwork.medbook.patient.client.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import it.pegaso.projectwork.medbook.patient.client.model.PatientStatusApiEnum;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Dati sintetici del paziente — utilizzato nella lista paginata
 */

@Schema(name = "PatientItemSummaryResponse", description = "Dati sintetici del paziente — utilizzato nella lista paginata")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-24T19:06:26.867401900+01:00[Europe/Rome]", comments = "Generator version: 7.4.0")
public class PatientItemSummaryResponse {

  private String patientId;

  private String firstName;

  private String lastName;

  private String email;

  private PatientStatusApiEnum status;

  public PatientItemSummaryResponse patientId(String patientId) {
    this.patientId = patientId;
    return this;
  }

  /**
   * Business key nel formato PAT-{seq}
   * @return patientId
  */
  
  @Schema(name = "patientId", example = "PAT-1", description = "Business key nel formato PAT-{seq}", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("patientId")
  public String getPatientId() {
    return patientId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public PatientItemSummaryResponse firstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  /**
   * Get firstName
   * @return firstName
  */
  
  @Schema(name = "firstName", example = "Mario", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("firstName")
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public PatientItemSummaryResponse lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  /**
   * Get lastName
   * @return lastName
  */
  
  @Schema(name = "lastName", example = "Rossi", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lastName")
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public PatientItemSummaryResponse email(String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
  */
  
  @Schema(name = "email", example = "mario.rossi@email.it", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public PatientItemSummaryResponse status(PatientStatusApiEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  */
  @Valid 
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public PatientStatusApiEnum getStatus() {
    return status;
  }

  public void setStatus(PatientStatusApiEnum status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PatientItemSummaryResponse patientItemSummaryResponse = (PatientItemSummaryResponse) o;
    return Objects.equals(this.patientId, patientItemSummaryResponse.patientId) &&
        Objects.equals(this.firstName, patientItemSummaryResponse.firstName) &&
        Objects.equals(this.lastName, patientItemSummaryResponse.lastName) &&
        Objects.equals(this.email, patientItemSummaryResponse.email) &&
        Objects.equals(this.status, patientItemSummaryResponse.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(patientId, firstName, lastName, email, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PatientItemSummaryResponse {\n");
    sb.append("    patientId: ").append(toIndentedString(patientId)).append("\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

