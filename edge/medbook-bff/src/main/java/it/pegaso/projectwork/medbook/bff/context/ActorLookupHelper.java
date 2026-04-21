package it.pegaso.projectwork.medbook.bff.context;

import it.pegaso.projectwork.medbook.bff.model.MedBookActorData;
import it.pegaso.projectwork.medbook.bff.model.MedBookActorType;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Orchestratore per la risoluzione del contesto attore autenticato.
 *
 * <h3>Strategia di accesso a due livelli</h3>
 * <ol>
 *   <li><b>L1 — {@link ActorContextHolder} (@RequestScope)</b>: per-request, zero chiamate
 *       di rete. Dalla seconda invocazione nella stessa request HTTP, i dati sono già
 *       disponibili in memoria senza nessuna operazione aggiuntiva.</li>
 *   <li><b>L2 — {@link ActorCacheService} (@Cacheable, Caffeine)</b>: cross-request,
 *       in-process (~50 ns). Evita chiamate ai DMN per request successive dello stesso
 *       utente entro il TTL configurato (default 60 min, dati immutabili).</li>
 * </ol>
 * Solo su cache miss L2 viene effettuata una chiamata di rete al DMN competente.
 *
 * <h3>Risoluzione per tipo di attore</h3>
 * <ul>
 *   <li><b>PATIENT</b>: cerca in patient-dmn per email (email = username Keycloak) → cached in L2</li>
 *   <li><b>DOCTOR</b>: cerca in doctor-dmn per email → cached in L2</li>
 *   <li><b>RECEPTIONIST/ADMIN</b>: costruisce dai claim JWT standard (nessuna chiamata DMN)</li>
 * </ul>
 *
 * <h3>Utilizzo tipico</h3>
 * <pre>{@code
 * // Dati completi dell'attore (firstName, lastName, actorId, ecc.)
 * MedBookActorData actor = actorLookupHelper.requireActorData(context);
 *
 * // Solo l'actorId (shortcut — evita fetch del profilo completo se non serve)
 * String patientId = actorLookupHelper.requireActorId(context);
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActorLookupHelper {

    private final ActorContextHolder requestHolder;
    private final ActorCacheService  actorCacheService;

    // =========================================================================
    // API pubblica
    // =========================================================================

    /**
     * Restituisce i dati completi dell'attore autenticato.
     * Controlla L1 prima, poi L2, infine risolve dal DMN o dal JWT.
     *
     * @param context contesto BFF della request corrente
     * @return dati dell'attore (mai null)
     * @throws MedBookBusinessException se il profilo non è trovato nel DMN
     */
    public MedBookActorData requireActorData(MedBookContext context) {
        // L1: già risolto in questa request — zero operazioni
        if (requestHolder.isResolved()) {
            return requestHolder.getActorData();
        }

        String username = extractUsernameFromJwt();
        MedBookActorType type = resolveActorType();
        log.debug("Risoluzione attore: username={}, type={}", maskUsername(username), type);

        MedBookActorData data = switch (type) {
            // PATIENT: username = email → cerca in patient-dmn per email.
            case PATIENT     -> actorCacheService.resolvePatient(context, username);
            // DOCTOR: username = email → cerca in doctor-dmn per email.
            case DOCTOR      -> actorCacheService.resolveDoctor(context, username);
            // RECEPTIONIST/ADMIN: dati dal JWT, nessuna chiamata di rete.
            case RECEPTIONIST, ADMIN -> buildFromJwt(username, type);
            case UNKNOWN     -> throw new MedBookBusinessException(MedBookErrorCode.BUSINESS_ERROR,
                    "Tipo attore non riconosciuto per username=" + maskUsername(username));
        };

        // Salva in L1 per le chiamate successive nella stessa request.
        requestHolder.setActorData(data);
        log.debug("Attore risolto: {}", data);
        return data;
    }

    /**
     * Restituisce il business key dell'attore autenticato (es. PAT-0001, DOC-0001).
     * Shortcut di {@link #requireActorData(MedBookContext)} per i casi in cui
     * serve solo l'ID e non il profilo completo.
     *
     * @throws MedBookBusinessException se l'attore non ha un record DMN (RECEPTIONIST/ADMIN)
     *         o se il profilo non viene trovato
     */
    public String requireActorId(MedBookContext context) {
        String actorId = requireActorData(context).actorId();
        if (!StringUtils.hasText(actorId)) {
            throw new MedBookBusinessException(MedBookErrorCode.BUSINESS_ERROR,
                    "L'utente autenticato non ha un business key associato (ruolo: "
                    + requireActorData(context).actorType() + ").");
        }
        return actorId;
    }

    /**
     * Legge il claim {@code preferred_username} dal token JWT.
     * Per i PATIENT contiene l'email, per i DOCTOR l'email.
     *
     * @throws MedBookBusinessException se il claim non è presente o l'autenticazione non è JWT
     */
    public String extractUsernameFromJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String username = jwtAuth.getToken().getClaimAsString("preferred_username");
            if (StringUtils.hasText(username)) {
                return username;
            }
        }
        throw new MedBookBusinessException(MedBookErrorCode.BUSINESS_ERROR,
                "Claim 'preferred_username' non presente nel token JWT.");
    }

    // =========================================================================
    // Metodi privati
    // =========================================================================

    /**
     * Determina il tipo di attore leggendo i ruoli dal SecurityContextHolder.
     * L'ordine di controllo è: PATIENT > DOCTOR > RECEPTIONIST > ADMIN > UNKNOWN.
     */
    private MedBookActorType resolveActorType() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return MedBookActorType.UNKNOWN;
        for (var authority : auth.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_PATIENT".equals(role))      return MedBookActorType.PATIENT;
            if ("ROLE_DOCTOR".equals(role))       return MedBookActorType.DOCTOR;
            if ("ROLE_RECEPTIONIST".equals(role)) return MedBookActorType.RECEPTIONIST;
            if ("ROLE_ADMIN".equals(role))        return MedBookActorType.ADMIN;
        }
        return MedBookActorType.UNKNOWN;
    }

    /**
     * Costruisce un {@link MedBookActorData} dai claim standard del JWT Keycloak
     * ({@code given_name}, {@code family_name}) senza effettuare chiamate DMN.
     * Usato per RECEPTIONIST e ADMIN che non hanno un record nei DMN di dominio.
     */
    private MedBookActorData buildFromJwt(String username, MedBookActorType type) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String firstName = null;
        String lastName  = null;
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            firstName = jwtAuth.getToken().getClaimAsString("given_name");
            lastName  = jwtAuth.getToken().getClaimAsString("family_name");
        }
        return new MedBookActorData(
                null,      // actorId: nessun record DMN per RECEPTIONIST/ADMIN
                type,
                username,
                firstName,
                lastName,
                null,      // fiscalCode
                null,      // dateOfBirth
                null,      // gender
                null       // licenseNumber
        );
    }

    /** Maschera lo username per il logging sicuro (mostra solo i primi 3 caratteri). */
    private static String maskUsername(String username) {
        if (username == null || username.length() < 4) return "***";
        return username.substring(0, 3) + "***";
    }
}
