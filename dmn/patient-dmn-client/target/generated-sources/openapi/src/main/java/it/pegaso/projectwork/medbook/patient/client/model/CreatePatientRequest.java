package it.pegaso.projectwork.medbook.patient.client.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import it.pegaso.projectwork.medbook.patient.client.model.GenderApiEnum;
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
 * Dati richiesti per la registrazione di un nuovo paziente
 */

@Schema(name = "CreatePatientRequest", description = "Dati richiesti per la registrazione di un nuovo paziente")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-24T19:06:26.867401900+01:00[Europe/Rome]", comments = "Generator version: 7.4.0")
public class CreatePatientRequest {

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

  public CreatePatientRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreatePatientRequest(String firstName, String lastName, LocalDate dateOfBirth, String fiscalCode, GenderApiEnum gender, String email, String phone) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.dateOfBirth = dateOfBirth;
    this.fiscalCode = fiscalCode;
    this.gender = gender;
    this.email = email;
    this.phone = phone;
  }

  public CreatePatientRequest firstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  /**
   * Nome del paziente
   * @return firstName
  */
  @NotNull @Size(max = 100) 
  @Schema(name = "firstName", example = "Mario", description = "Nome del paziente", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("firstName")
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public CreatePatientRequest lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  /**
   * Cognome del paziente
   * @return lastName
  */
  @NotNull @Size(max = 100) 
  @Schema(name = "lastName", example = "Rossi", description = "Cognome del paziente", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("lastName")
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public CreatePatientRequest dateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
    return this;
  }

  /**
   * Data di nascita nel formato ISO 8601 (YYYY-MM-DD)
   * @return dateOfBirth
  */
  @NotNull @Valid 
  @Schema(name = "dateOfBirth", example = "Mon Jan 15 01:00:00 CET 1990", description = "Data di nascita nel formato ISO 8601 (YYYY-MM-DD)", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("dateOfBirth")
  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public CreatePatientRequest fiscalCode(String fiscalCode) {
    this.fiscalCode = fiscalCode;
    return this;
  }

  /**
   * Codice fiscale italiano — esattamente 16 caratteri — non modificabile dopo la creazione
   * @return fiscalCode
  */
  @NotNull @Size(min = 16, max = 16) 
  @Schema(name = "fiscalCode", example = "RSSMRA90A15H501Z", description = "Codice fiscale italiano — esattamente 16 caratteri — non modificabile dopo la creazione", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("fiscalCode")
  public String getFiscalCode() {
    return fiscalCode;
  }

  public void setFiscalCode(String fiscalCode) {
    this.fiscalCode = fiscalCode;
  }

  public CreatePatientRequest gender(GenderApiEnum gender) {
    this.gender = gender;
    return this;
  }

  /**
   * Get gender
   * @return gender
  */
  @NotNull @Valid 
  @Schema(name = "gender", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("gender")
  public GenderApiEnum getGender() {
    return gender;
  }

  public void setGender(GenderApiEnum gender) {
    this.gender = gender;
  }

  public CreatePatientRequest email(String email) {
    this.email = email;
    return this;
  }

  /**
   * Indirizzo email del paziente — deve essere univoco nel sistema
   * @return email
  */
  @NotNull @Size(max = 150) @jakarta.validation.constraints.Email 
  @Schema(name = "email", example = "mario.rossi@email.it", description = "Indirizzo email del paziente — deve essere univoco nel sistema", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public CreatePatientRequest phone(String phone) {
    this.phone = phone;
    return this;
  }

  /**
   * Numero di telefono del paziente
   * @return phone
  */
  @NotNull @Size(max = 20) 
  @Schema(name = "phone", example = "+393331234567", description = "Numero di telefono del paziente", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("phone")
  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public CreatePatientRequest address(String address) {
    this.address = address;
    return this;
  }

  /**
   * Via e numero civico — facoltativo alla registrazione
   * @return address
  */
  @Size(max = 200) 
  @Schema(name = "address", example = "Via Roma 12", description = "Via e numero civico — facoltativo alla registrazione", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("address")
  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public CreatePatientRequest city(String city) {
    this.city = city;
    return this;
  }

  /**
   * Città di residenza — facoltativo alla registrazione
   * @return city
  */
  @Size(max = 100) 
  @Schema(name = "city", example = "Napoli", description = "Città di residenza — facoltativo alla registrazione", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("city")
  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public CreatePatientRequest postalCode(String postalCode) {
    this.postalCode = postalCode;
    return this;
  }

  /**
   * Codice postale italiano (CAP) — facoltativo
   * @return postalCode
  */
  @Size(max = 10) 
  @Schema(name = "postalCode", example = "80100", description = "Codice postale italiano (CAP) — facoltativo", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("postalCode")
  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public CreatePatientRequest province(String province) {
    this.province = province;
    return this;
  }

  /**
   * Codice provincia italiano (2 caratteri) — facoltativo
   * @return province
  */
  @Size(max = 5) 
  @Schema(name = "province", example = "NA", description = "Codice provincia italiano (2 caratteri) — facoltativo", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
    CreatePatientRequest createPatientRequest = (CreatePatientRequest) o;
    return Objects.equals(this.firstName, createPatientRequest.firstName) &&
        Objects.equals(this.lastName, createPatientRequest.lastName) &&
        Objects.equals(this.dateOfBirth, createPatientRequest.dateOfBirth) &&
        Objects.equals(this.fiscalCode, createPatientRequest.fiscalCode) &&
        Objects.equals(this.gender, createPatientRequest.gender) &&
        Objects.equals(this.email, createPatientRequest.email) &&
        Objects.equals(this.phone, createPatientRequest.phone) &&
        Objects.equals(this.address, createPatientRequest.address) &&
        Objects.equals(this.city, createPatientRequest.city) &&
        Objects.equals(this.postalCode, createPatientRequest.postalCode) &&
        Objects.equals(this.province, createPatientRequest.province);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstName, lastName, dateOfBirth, fiscalCode, gender, email, phone, address, city, postalCode, province);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreatePatientRequest {\n");
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

