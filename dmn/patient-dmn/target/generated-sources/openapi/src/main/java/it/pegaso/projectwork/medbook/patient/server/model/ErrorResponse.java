package it.pegaso.projectwork.medbook.patient.server.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import it.pegaso.projectwork.medbook.patient.server.model.ErrorResponseMessage;
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
 * Formato standard delle risposte di errore della piattaforma MedBook
 */

@Schema(name = "ErrorResponse", description = "Formato standard delle risposte di errore della piattaforma MedBook")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-24T19:05:33.770195300+01:00[Europe/Rome]", comments = "Generator version: 7.4.0")
public class ErrorResponse {

  private Integer httpStatus;

  private String errorCode;

  private ErrorResponseMessage message;

  private String path;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime timestamp;

  public ErrorResponse httpStatus(Integer httpStatus) {
    this.httpStatus = httpStatus;
    return this;
  }

  /**
   * Codice HTTP della risposta di errore
   * @return httpStatus
  */
  
  @Schema(name = "httpStatus", example = "404", description = "Codice HTTP della risposta di errore", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("httpStatus")
  public Integer getHttpStatus() {
    return httpStatus;
  }

  public void setHttpStatus(Integer httpStatus) {
    this.httpStatus = httpStatus;
  }

  public ErrorResponse errorCode(String errorCode) {
    this.errorCode = errorCode;
    return this;
  }

  /**
   * Codice errore applicativo — corrisponde a ErrorCode enum in medbook-commons
   * @return errorCode
  */
  
  @Schema(name = "errorCode", example = "RESOURCE_NOT_FOUND", description = "Codice errore applicativo — corrisponde a ErrorCode enum in medbook-commons", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("errorCode")
  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public ErrorResponse message(ErrorResponseMessage message) {
    this.message = message;
    return this;
  }

  /**
   * Get message
   * @return message
  */
  @Valid 
  @Schema(name = "message", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("message")
  public ErrorResponseMessage getMessage() {
    return message;
  }

  public void setMessage(ErrorResponseMessage message) {
    this.message = message;
  }

  public ErrorResponse path(String path) {
    this.path = path;
    return this;
  }

  /**
   * Path dell'endpoint che ha generato l'errore
   * @return path
  */
  
  @Schema(name = "path", example = "/api/v1/patients/PAT-999", description = "Path dell'endpoint che ha generato l'errore", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("path")
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public ErrorResponse timestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * Timestamp dell'errore nel formato ISO 8601
   * @return timestamp
  */
  @Valid 
  @Schema(name = "timestamp", description = "Timestamp dell'errore nel formato ISO 8601", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("timestamp")
  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ErrorResponse errorResponse = (ErrorResponse) o;
    return Objects.equals(this.httpStatus, errorResponse.httpStatus) &&
        Objects.equals(this.errorCode, errorResponse.errorCode) &&
        Objects.equals(this.message, errorResponse.message) &&
        Objects.equals(this.path, errorResponse.path) &&
        Objects.equals(this.timestamp, errorResponse.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(httpStatus, errorCode, message, path, timestamp);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErrorResponse {\n");
    sb.append("    httpStatus: ").append(toIndentedString(httpStatus)).append("\n");
    sb.append("    errorCode: ").append(toIndentedString(errorCode)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
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

