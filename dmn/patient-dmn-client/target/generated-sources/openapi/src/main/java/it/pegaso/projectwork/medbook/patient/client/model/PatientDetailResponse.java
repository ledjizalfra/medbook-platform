package it.pegaso.projectwork.medbook.patient.client.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import it.pegaso.projectwork.medbook.patient.client.model.GenderApiEnum;
import it.pegaso.projectwork.medbook.patient.client.model.PatientStatusApiEnum;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Dati completi del paziente — restituiti da GET /patients/{patientId}
 */

@Schema(name = "PatientDetailResponse", description = "Dati completi del paziente — restituiti da GET /patients/{patientId}")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-24T19:06:26.867401900+01:00[Europe/Rome]", comments = "Generator version: 7.4.0")
public class PatientDetailResponse {

  private String patientId;

  private String firstName;

  private String lastName;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate dateOfBirth;

  private String fiscalCode;

  private GenderApiEnum gender;

  private String email;

  private String phone;

  private String address;

  private String city;

  private String postalCode;

  private String province;

  private PatientStatusApiEnum status;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime updatedAt;

  public PatientDetailResponse patientId(String patientId) {
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

  public PatientDetailResponse firstName(String firstName) {
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

  public PatientDetailResponse lastName(String lastName) {
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

  public PatientDetailResponse dateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
    return this;
  }

  /**
   * Get dateOfBirth
   * @return dateOfBirth
  */
  @Valid 
  @Schema(name = "dateOfBirth", example = "Mon Jan 15 01:00:00 CET 1990", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dateOfBirth")
  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public PatientDetailResponse fiscalCode(String fiscalCode) {
    this.fiscalCode = fiscalCode;
    return this;
  }

  /**
   * Get fiscalCode
   * @return fiscalCode
  */
  
  @Schema(name = "fiscalCode", example = "RSSMRA90A15H501Z", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("fiscalCode")
  public String getFiscalCode() {
    return fiscalCode;
  }

  public void setFiscalCode(String fiscalCode) {
    this.fiscalCode = fiscalCode;
  }

  public PatientDetailResponse gender(GenderApiEnum gender) {
    this.gender = gender;
    return this;
  }

  /**
   * Get gender
   * @return gender
  */
  @Valid 
  @Schema(name = "gender", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("gender")
  public GenderApiEnum getGender() {
    return gender;
  }

  public void setGender(GenderApiEnum gender) {
    this.gender = gender;
  }

  public PatientDetailResponse email(String email) {
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

  public PatientDetailResponse phone(String phone) {
    this.phone = phone;
    return this;
  }

  /**
   * Get phone
   * @return phone
  */
  
  @Schema(name = "phone", example = "+393331234567", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("phone")
  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public PatientDetailResponse address(String address) {
    this.address = address;
    return this;
  }

  /**
   * Get address
   * @return address
  */
  
  @Schema(name = "address", example = "Via Roma 12", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("address")
  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public PatientDetailResponse city(String city) {
    this.city = city;
    return this;
  }

  /**
   * Get city
   * @return city
  */
  
  @Schema(name = "city", example = "Napoli", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("city")
  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public PatientDetailResponse postalCode(String postalCode) {
    this.postalCode = postalCode;
    return this;
  }

  /**
   * Get postalCode
   * @return postalCode
  */
  
  @Schema(name = "postalCode", example = "80100", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("postalCode")
  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public PatientDetailResponse province(String province) {
    this.province = province;
    return this;
  }

  /**
   * Get province
   * @return province
  */
  
  @Schema(name = "province", example = "NA", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("province")
  public String getProvince() {
    return province;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  public PatientDetailResponse status(PatientStatusApiEnum status) {
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

  public PatientDetailResponse createdAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Timestamp di creazione del record
   * @return createdAt
  */
  @Valid 
  @Schema(name = "createdAt", description = "Timestamp di creazione del record", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("createdAt")
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public PatientDetailResponse updatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   * Timestamp dell'ultimo aggiornamento
   * @return updatedAt
  */
  @Valid 
  @Schema(name = "updatedAt", description = "Timestamp dell'ultimo aggiornamento", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("updatedAt")
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PatientDetailResponse patientDetailResponse = (PatientDetailResponse) o;
    return Objects.equals(this.patientId, patientDetailResponse.patientId) &&
        Objects.equals(this.firstName, patientDetailResponse.firstName) &&
        Objects.equals(this.lastName, patientDetailResponse.lastName) &&
        Objects.equals(this.dateOfBirth, patientDetailResponse.dateOfBirth) &&
        Objects.equals(this.fiscalCode, patientDetailResponse.fiscalCode) &&
        Objects.equals(this.gender, patientDetailResponse.gender) &&
        Objects.equals(this.email, patientDetailResponse.email) &&
        Objects.equals(this.phone, patientDetailResponse.phone) &&
        Objects.equals(this.address, patientDetailResponse.address) &&
        Objects.equals(this.city, patientDetailResponse.city) &&
        Objects.equals(this.postalCode, patientDetailResponse.postalCode) &&
        Objects.equals(this.province, patientDetailResponse.province) &&
        Objects.equals(this.status, patientDetailResponse.status) &&
        Objects.equals(this.createdAt, patientDetailResponse.createdAt) &&
        Objects.equals(this.updatedAt, patientDetailResponse.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(patientId, firstName, lastName, dateOfBirth, fiscalCode, gender, email, phone, address, city, postalCode, province, status, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PatientDetailResponse {\n");
    sb.append("    patientId: ").append(toIndentedString(patientId)).append("\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    dateOfBirth: ").append(toIndentedString(dateOfBirth)).append("\n");
    sb.append("    fiscalCode: ").append(toIndentedString(fiscalCode)).append("\n");
    sb.append("    gender: ").append(toIndentedString(gender)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    city: ").append(toIndentedString(city)).append("\n");
    sb.append("    postalCode: ").append(toIndentedString(postalCode)).append("\n");
    sb.append("    province: ").append(toIndentedString(province)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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

