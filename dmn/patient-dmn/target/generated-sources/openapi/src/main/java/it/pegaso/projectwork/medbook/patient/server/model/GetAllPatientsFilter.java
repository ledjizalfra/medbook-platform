package it.pegaso.projectwork.medbook.patient.server.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import it.pegaso.projectwork.medbook.patient.server.model.PatientStatusApiEnum;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Oggetto di filtro per la ricerca dei pazienti. Tutti i campi sono opzionali — se non specificati non viene applicato il filtro. Usato internamente nel service e repository per raggruppare i parametri. 
 */

@Schema(name = "GetAllPatientsFilter", description = "Oggetto di filtro per la ricerca dei pazienti. Tutti i campi sono opzionali — se non specificati non viene applicato il filtro. Usato internamente nel service e repository per raggruppare i parametri. ")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-24T19:05:33.770195300+01:00[Europe/Rome]", comments = "Generator version: 7.4.0")
public class GetAllPatientsFilter {

  private PatientStatusApiEnum status;

  private String lastName;

  private String city;

  private String email;

  private String fiscalCode;

  public GetAllPatientsFilter status(PatientStatusApiEnum status) {
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

  public GetAllPatientsFilter lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  /**
   * Ricerca parziale case-insensitive sul cognome
   * @return lastName
  */
  
  @Schema(name = "lastName", example = "Rossi", description = "Ricerca parziale case-insensitive sul cognome", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lastName")
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public GetAllPatientsFilter city(String city) {
    this.city = city;
    return this;
  }

  /**
   * Ricerca case-insensitive sulla città
   * @return city
  */
  
  @Schema(name = "city", example = "Napoli", description = "Ricerca case-insensitive sulla città", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("city")
  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public GetAllPatientsFilter email(String email) {
    this.email = email;
    return this;
  }

  /**
   * Ricerca parziale case-insensitive sull'email
   * @return email
  */
  
  @Schema(name = "email", example = "mario.rossi", description = "Ricerca parziale case-insensitive sull'email", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public GetAllPatientsFilter fiscalCode(String fiscalCode) {
    this.fiscalCode = fiscalCode;
    return this;
  }

  /**
   * Corrispondenza esatta sul codice fiscale
   * @return fiscalCode
  */
  
  @Schema(name = "fiscalCode", example = "RSSMRA90A15H501Z", description = "Corrispondenza esatta sul codice fiscale", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("fiscalCode")
  public String getFiscalCode() {
    return fiscalCode;
  }

  public void setFiscalCode(String fiscalCode) {
    this.fiscalCode = fiscalCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetAllPatientsFilter getAllPatientsFilter = (GetAllPatientsFilter) o;
    return Objects.equals(this.status, getAllPatientsFilter.status) &&
        Objects.equals(this.lastName, getAllPatientsFilter.lastName) &&
        Objects.equals(this.city, getAllPatientsFilter.city) &&
        Objects.equals(this.email, getAllPatientsFilter.email) &&
        Objects.equals(this.fiscalCode, getAllPatientsFilter.fiscalCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, lastName, city, email, fiscalCode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetAllPatientsFilter {\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    city: ").append(toIndentedString(city)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    fiscalCode: ").append(toIndentedString(fiscalCode)).append("\n");
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

