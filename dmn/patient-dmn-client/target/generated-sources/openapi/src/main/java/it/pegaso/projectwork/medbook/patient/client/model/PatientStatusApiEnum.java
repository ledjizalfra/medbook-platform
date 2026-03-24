package it.pegaso.projectwork.medbook.patient.client.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonValue;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Stato del ciclo di vita del paziente: - ACTIVE: il paziente può prenotare appuntamenti - INACTIVE: account disattivato 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-24T19:06:26.867401900+01:00[Europe/Rome]", comments = "Generator version: 7.4.0")
public enum PatientStatusApiEnum {
  
  ACTIVE("ACTIVE"),
  
  INACTIVE("INACTIVE");

  private String value;

  PatientStatusApiEnum(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static PatientStatusApiEnum fromValue(String value) {
    for (PatientStatusApiEnum b : PatientStatusApiEnum.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

