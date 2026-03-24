package it.pegaso.projectwork.medbook.patient.entity.enums;

import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.patient.server.model.PatientStatusApiEnum;

/**
 * Enum che rappresenta lo stato del ciclo di vita di business del paziente.
 */
public enum PatientStatusEnum {

    // Paziente attivo - può prenotare appuntamenti
    ACTIVE("ACTIVE"),
    // Paziente disattivato - account sospeso
    INACTIVE("INACTIVE");

    PatientStatusEnum(String value) {}


    // =========================================================================
    // METODI DI UTILITÀ
    // =========================================================================

    /**
     * Recupera un PatientStatusEnum dalla stringa corrispondente.
     * Lancia BusinessException se il valore non è valido.
     *
     * @param value stringa da convertire
     * @return PatientStatusEnum corrispondente
     */
    public static PatientStatusEnum fromString(String value) {
        if (value == null) {
            throw new MedBookBusinessException(
                    MedBookErrorCode.VALIDATION_ERROR,
                    "Lo stato paziente non può essere null");
        }
        try {
            return PatientStatusEnum.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MedBookBusinessException(
                    MedBookErrorCode.VALIDATION_ERROR,
                    "Valore non valido per lo stato paziente: " + value);
        }
    }

    /**
     * Converte l'enum generato da OpenAPI nell'enum del dominio.
     * Lancia BusinessException se il valore non è mappabile.
     *
     * @param apiEnum enum generato da OpenAPI Generator
     * @return PatientStatusEnum del dominio
     */
    public static PatientStatusEnum fromApiEnum(PatientStatusApiEnum apiEnum) {
        if (apiEnum == null) return null;
        return fromString(apiEnum.name());
    }
}