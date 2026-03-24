package it.pegaso.projectwork.medbook.patient.entity.enums;

import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.patient.server.model.GenderApiEnum;

/**
 * Enum che rappresenta il genere del paziente.
 */
public enum GenderEnum {

    MALE,
    FEMALE;

    // =========================================================================
    // METODI DI UTILITÀ
    // =========================================================================

    /**
     * Recupera un GenderEnum dalla stringa corrispondente.
     * Lancia BusinessException se il valore non è valido.
     *
     * @param value stringa da convertire
     * @return GenderEnum corrispondente
     */
    public static GenderEnum fromString(String value) {
        if (value == null) {
            throw new MedBookBusinessException(
                    MedBookErrorCode.VALIDATION_ERROR,
                    "Il genere non può essere null");
        }
        try {
            return GenderEnum.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MedBookBusinessException(
                    MedBookErrorCode.VALIDATION_ERROR,
                    "Valore non valido per il genere: " + value);
        }
    }

    /**
     * Converte l'enum generato da OpenAPI nell'enum del dominio.
     * Lancia BusinessException se il valore non è mappabile.
     *
     * @param apiEnum enum generato da OpenAPI Generator
     * @return GenderEnum del dominio
     */
    public static GenderEnum fromApiEnum(GenderApiEnum apiEnum) {
        if (apiEnum == null) return null;
        return fromString(apiEnum.name());
    }
}
