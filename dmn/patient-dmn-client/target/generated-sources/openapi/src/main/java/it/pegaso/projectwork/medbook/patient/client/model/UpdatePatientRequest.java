package it.pegaso.projectwork.medbook.patient.client.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Dati per l&#39;aggiornamento parziale del paziente. Solo i campi presenti nella richiesta vengono aggiornati. I campi assenti restano invariati. Il codice fiscale e il genere non sono modificabili. 
 */

@Schema(name = "UpdatePatientRequest", description = "Dati per l'aggiornamento parziale del paziente. Solo i campi presenti nella richiesta vengono aggiornati. I campi assenti restano invariati. Il codice fiscale e il genere non sono modificabili. ")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-24T19:06:26.867401900+01:00[Europe/Rome]", comments = "Generator version: 7.4.0")
public class UpdatePatientRequest {

  private String patientId;

  private String firstName;

  private String lastName;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate dateOfBirth;

  private String email;

  private String phone;

  private String address;

  private String city;

  private String postalCode;

  private String province;

  public UpdatePatientRequest patientId(String patientId) {
    this.patientId = patientId;
    return this;
  }

  /**
   * chiave business del patient
   * @return patientId
  */
  
  @Schema(name = "patientId", example = "PAT-", description = "chiave business del patient", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("patientId")
  public String getPatientId() {
    return patientId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public UpdatePatientRequest firstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  /**
   * Nome del paziente
   * @return firstName
  */
  @Size(max = 100) 
  @Schema(name = "firstName", example = "Mario", description = "Nome del paziente", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("firstName")
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public UpdatePatientRequest lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  /**
   * Cognome del paziente
   * @return lastName
  */
  @Size(max = 100) 
  @Schema(name = "lastName", example = "Rossi", description = "Cognome del paziente", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lastName")
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public UpdatePatientRequest dateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
    return this;
  }

  /**
   * Data di nascita nel formato ISO 8601 (YYYY-MM-DD)
   * @return dateOfBirth
  */
  @Valid 
  @Schema(name = "dateOfBirth", example = "Mon Jan 15 01:00:00 CET 1990", description = "Data di nascita nel formato ISO 8601 (YYYY-MM-DD)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dateOfBirth")
  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public UpdatePatientRequest email(String email) {
    this.email = email;
    return this;
  }

  /**
   * Indirizzo email del paziente — deve essere univoco nel sistema
   * @return email
  */
  @Size(max = 150) @jakarta.validation.constraints.Email 
  @Schema(name = "email", example = "mario.rossi@email.it", description = "Indirizzo email del paziente — deve essere univoco nel sistema", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public UpdatePatientRequest phone(String phone) {
    this.phone = phone;
    return this;
  }

  /**
   * Numero di telefono del paziente
   * @return phone
  */
  @Size(max = 20) 
  @Schema(name = "phone", example = "+393331234567", description = "Numero di telefono del paziente", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("phone")
  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public UpdatePatientRequest address(String address) {
    this.address = address;
    return this;
  }

  /**
   * Via e numero civico
   * @return address
  */
  @Size(max = 200) 
  @Schema(name = "address", example = "Via Roma 12", description = "Via e numero civico", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("address")
  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public UpdatePatientRequest city(String city) {
    this.city = city;
    return this;
  }

  /**
   * Città di residenza
   * @return city
  */
  @Size(max = 100) 
  @Schema(name = "city", example = "Napoli", description = "Città di residenza", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("city")
  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public UpdatePatientRequest postalCode(String postalCode) {
    this.postalCode = postalCode;
    return this;
  }

  /**
   * Codice postale italiano (CAP)
   * @return postalCode
  */
  @Size(max = 10) 
  @Schema(name = "postalCode", example = "80100", description = "Codice postale italiano (CAP)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("postalCode")
  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public UpdatePatientRequest province(String province) {
    this.province = province;
    return this;
  }

  /**
   * Codice provincia italiano (2 caratteri)
   * @return province
  */
  @Size(max = 5) 
  @Schema(name = "province", example = "NA", description = "Codice provincia italiano (2 caratteri)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("province")
  public String getProvince() {
    return province;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdatePatientRequest updatePatientRequest = (UpdatePatientRequest) o;
    return Objects.equals(this.patientId, updatePatientRequest.patientId) &&
        Objects.equals(this.firstName, updatePatientRequest.firstName) &&
        Objects.equals(this.lastName, updatePatientRequest.lastName) &&
        Objects.equals(this.dateOfBirth, updatePatientRequest.dateOfBirth) &&
        Objects.equals(this.email, updatePatientRequest.email) &&
        Objects.equals(this.phone, updatePatientRequest.phone) &&
        Objects.equals(this.address, updatePatientRequest.address) &&
        Objects.equals(this.city, updatePatientRequest.city) &&
        Objects.equals(this.postalCode, updatePatientRequest.postalCode) &&
        Objects.equals(this.province, updatePatientRequest.province);
  }

  @Override
  public int hashCode() {
    return Objects.hash(patientId, firstName, lastName, dateOfBirth, email, phone, address, city, postalCode, province);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdatePatientRequest {\n");
    sb.append("    patientId: ").append(toIndentedString(patientId)).append("\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    dateOfBirth: ").append(toIndentedString(dateOfBirth)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    city: ").append(toIndentedString(city)).append("\n");
    sb.append("    postalCode: ").append(toIndentedString(postalCode)).append("\n");
    sb.append("    province: ").append(toIndentedString(province)).append("\n");
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

