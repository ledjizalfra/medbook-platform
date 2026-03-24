package it.pegaso.projectwork.medbook.patient.server.model;

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
 * Genere del paziente
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-24T19:05:33.770195300+01:00[Europe/Rome]", comments = "Generator version: 7.4.0")
public enum GenderApiEnum {
  
  MALE("MALE"),
  
  FEMALE("FEMALE");

  private String value;

  GenderApiEnum(String value) {
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
  public static GenderApiEnum fromValue(String value) {
    for (GenderApiEnum b : GenderApiEnum.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

