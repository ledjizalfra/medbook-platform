package it.pegaso.projectwork.medbook.bff.helper;

import it.pegaso.projectwork.medbook.bff.server.model.CreatePatientBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdatePatientBffRequest;
import it.pegaso.projectwork.medbook.commons.formatter.MedBookFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Helper BFF per la normalizzazione delle request paziente.
 * Centralizza le chiamate al MedBookFormatter per i DTO del BFF,
 * evitando di disperdere la logica di formattazione nel service.
 */
@Component
@RequiredArgsConstructor
public class PatientBffHelper {

    private final MedBookFormatter formatter;

    /** Normalizza in-place tutti i campi stringa di una request di creazione paziente. */
    public void formatRequest(CreatePatientBffRequest createRequest) {
        createRequest.setFirstName(formatter.formatFirstName(createRequest.getFirstName()));
        createRequest.setLastName(formatter.formatLastName(createRequest.getLastName()));
        createRequest.setEmail(formatter.formatEmail(createRequest.getEmail()));
        createRequest.setPhone(formatter.formatPhone(createRequest.getPhone()));
        createRequest.setAddress(formatter.trim(createRequest.getAddress()));
        createRequest.setCity(formatter.formatCity(createRequest.getCity()));
        createRequest.setPostalCode(formatter.formatPostalCode(createRequest.getPostalCode()));
        createRequest.setProvince(formatter.formatProvince(createRequest.getProvince()));
        createRequest.setFiscalCode(formatter.formatFiscalCode(createRequest.getFiscalCode()));
        // Luogo di nascita
        createRequest.setComuneNascita(formatter.formatCity(createRequest.getComuneNascita()));
        createRequest.setProvinciaNascita(formatter.formatProvince(createRequest.getProvinciaNascita()));
        createRequest.setRegioneNascita(formatter.formatFirstName(createRequest.getRegioneNascita()));
    }

    /** Normalizza in-place tutti i campi stringa di una request di aggiornamento paziente. */
    public void formatRequest(UpdatePatientBffRequest updateRequest) {
        updateRequest.setFirstName(formatter.formatFirstName(updateRequest.getFirstName()));
        updateRequest.setLastName(formatter.formatLastName(updateRequest.getLastName()));
        updateRequest.setEmail(formatter.formatEmail(updateRequest.getEmail()));
        updateRequest.setPhone(formatter.formatPhone(updateRequest.getPhone()));
        updateRequest.setAddress(formatter.trim(updateRequest.getAddress()));
        updateRequest.setCity(formatter.formatCity(updateRequest.getCity()));
        updateRequest.setPostalCode(formatter.formatPostalCode(updateRequest.getPostalCode()));
        updateRequest.setProvince(formatter.formatProvince(updateRequest.getProvince()));
    }
}
