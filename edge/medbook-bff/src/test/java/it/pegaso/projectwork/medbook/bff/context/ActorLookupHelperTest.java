package it.pegaso.projectwork.medbook.bff.context;

import org.junit.jupiter.api.Test;

// TODO: aggiungere test
// - requireActorData(): L1 hit (actorData già in requestHolder → nessuna chiamata a actorCacheService)
// - requireActorData(): L2 hit (L1 miss, actorCacheService restituisce da cache → salva in L1)
// - requireActorData(): PATIENT risolto da patient-dmn e salvato in L1 + L2
// - requireActorData(): DOCTOR risolto da doctor-dmn e salvato in L1 + L2
// - requireActorData(): RECEPTIONIST costruito da JWT senza chiamate DMN
// - requireActorData(): ADMIN costruito da JWT senza chiamate DMN
// - requireActorId(): restituisce actorId se presente, lancia eccezione per RECEPTIONIST/ADMIN
// - extractEmailFromJwt(): restituisce preferred_username dal JWT
// - extractEmailFromJwt(): lancia MedBookBusinessException se claim assente
// - resolveActorType(): mappa correttamente ROLE_PATIENT, ROLE_DOCTOR, ROLE_RECEPTIONIST, ROLE_ADMIN
class ActorLookupHelperTest {
}
