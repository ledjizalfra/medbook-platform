package it.pegaso.projectwork.medbook.patient.server.model;

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
 * Metadati di paginazione standard della piattaforma MedBook. Riutilizzato da tutti i DMN come campo \&quot;page\&quot; nelle response paginate. 
 */

@Schema(name = "MedBookPageResponse", description = "Metadati di paginazione standard della piattaforma MedBook. Riutilizzato da tutti i DMN come campo \"page\" nelle response paginate. ")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-24T19:05:33.770195300+01:00[Europe/Rome]", comments = "Generator version: 7.4.0")
public class MedBookPageResponse {

  private Long totalElements;

  private Integer totalPages;

  private Integer size;

  private Integer number;

  private Boolean first;

  private Boolean last;

  private Boolean empty;

  public MedBookPageResponse totalElements(Long totalElements) {
    this.totalElements = totalElements;
    return this;
  }

  /**
   * Numero totale di elementi in tutte le pagine
   * @return totalElements
  */
  
  @Schema(name = "totalElements", example = "150", description = "Numero totale di elementi in tutte le pagine", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalElements")
  public Long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(Long totalElements) {
    this.totalElements = totalElements;
  }

  public MedBookPageResponse totalPages(Integer totalPages) {
    this.totalPages = totalPages;
    return this;
  }

  /**
   * Numero totale di pagine
   * @return totalPages
  */
  
  @Schema(name = "totalPages", example = "8", description = "Numero totale di pagine", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalPages")
  public Integer getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(Integer totalPages) {
    this.totalPages = totalPages;
  }

  public MedBookPageResponse size(Integer size) {
    this.size = size;
    return this;
  }

  /**
   * Numero di elementi per pagina
   * @return size
  */
  
  @Schema(name = "size", example = "20", description = "Numero di elementi per pagina", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("size")
  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public MedBookPageResponse number(Integer number) {
    this.number = number;
    return this;
  }

  /**
   * Numero della pagina corrente (0-based)
   * @return number
  */
  
  @Schema(name = "number", example = "0", description = "Numero della pagina corrente (0-based)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("number")
  public Integer getNumber() {
    return number;
  }

  public void setNumber(Integer number) {
    this.number = number;
  }

  public MedBookPageResponse first(Boolean first) {
    this.first = first;
    return this;
  }

  /**
   * True se è la prima pagina
   * @return first
  */
  
  @Schema(name = "first", example = "true", description = "True se è la prima pagina", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("first")
  public Boolean getFirst() {
    return first;
  }

  public void setFirst(Boolean first) {
    this.first = first;
  }

  public MedBookPageResponse last(Boolean last) {
    this.last = last;
    return this;
  }

  /**
   * True se è l'ultima pagina
   * @return last
  */
  
  @Schema(name = "last", example = "false", description = "True se è l'ultima pagina", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("last")
  public Boolean getLast() {
    return last;
  }

  public void setLast(Boolean last) {
    this.last = last;
  }

  public MedBookPageResponse empty(Boolean empty) {
    this.empty = empty;
    return this;
  }

  /**
   * True se la pagina non contiene elementi
   * @return empty
  */
  
  @Schema(name = "empty", example = "false", description = "True se la pagina non contiene elementi", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("empty")
  public Boolean getEmpty() {
    return empty;
  }

  public void setEmpty(Boolean empty) {
    this.empty = empty;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MedBookPageResponse medBookPageResponse = (MedBookPageResponse) o;
    return Objects.equals(this.totalElements, medBookPageResponse.totalElements) &&
        Objects.equals(this.totalPages, medBookPageResponse.totalPages) &&
        Objects.equals(this.size, medBookPageResponse.size) &&
        Objects.equals(this.number, medBookPageResponse.number) &&
        Objects.equals(this.first, medBookPageResponse.first) &&
        Objects.equals(this.last, medBookPageResponse.last) &&
        Objects.equals(this.empty, medBookPageResponse.empty);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalElements, totalPages, size, number, first, last, empty);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MedBookPageResponse {\n");
    sb.append("    totalElements: ").append(toIndentedString(totalElements)).append("\n");
    sb.append("    totalPages: ").append(toIndentedString(totalPages)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    number: ").append(toIndentedString(number)).append("\n");
    sb.append("    first: ").append(toIndentedString(first)).append("\n");
    sb.append("    last: ").append(toIndentedString(last)).append("\n");
    sb.append("    empty: ").append(toIndentedString(empty)).append("\n");
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

