package it.pegaso.projectwork.medbook.bff.context;

import org.junit.jupiter.api.Test;

// TODO: aggiungere test
// - resolvePatient(): mappattura corretta di tutti i campi da patient-dmn response
// - resolvePatient(): verifica corrispondenza email esatta (case-insensitive)
// - resolvePatient(): lancia MedBookBusinessException se nessun paziente trovato
// - resolvePatient(): lancia MedBookBusinessException se patient-dmn non risponde
// - resolveDoctor(): mappattura corretta di tutti i campi da doctor-dmn response
// - resolveDoctor(): verifica corrispondenza email esatta (case-insensitive)
// - resolveDoctor(): lancia MedBookBusinessException se nessun medico trovato
// - @Cacheable: seconda chiamata con stessa email non invoca patientClient
class ActorCacheServiceTest {
}
