package it.pegaso.projectwork.medbook.doctor.model.enums;

import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.doctor.server.model.DoctorStatusApiEnum;

/**
 * Enum che rappresenta lo stato operativo del medico nella struttura.
 */
public enum DoctorStatusEnum {

    /** Medico operativo nella struttura. */
    ATTIVO,

    /** Medico non più attivo nella struttura. */
    DISATTIVO,

    /** Medico temporaneamente sospeso. */
    SOSPESO,

    /** Medico in ferie o congedo temporaneo. */
    IN_FERIE;

    /**
     * Converte l'enum API OpenAPI nell'enum del dominio.
     */
    public static DoctorStatusEnum fromApiEnum(DoctorStatusApiEnum apiEnum) {
        if (apiEnum == null) return null;
        try {
            return DoctorStatusEnum.valueOf(apiEnum.name());
        } catch (IllegalArgumentException e) {
            throw new MedBookBusinessException(
                    MedBookErrorCode.VALIDATION_ERROR,
                    "Valore non valido per lo stato medico: " + apiEnum.name());
        }
    }
}
