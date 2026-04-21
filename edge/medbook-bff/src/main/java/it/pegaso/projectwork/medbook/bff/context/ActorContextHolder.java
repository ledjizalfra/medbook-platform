package it.pegaso.projectwork.medbook.bff.context;

import it.pegaso.projectwork.medbook.bff.model.MedBookActorData;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Cache L1 per i dati dell'attore autenticato — scoped alla singola request HTTP.
 *
 * Istanziato una volta per ogni request (Spring {@code @RequestScope}): dopo la prima
 * risoluzione dell'attore, tutti i bean BFF che accedono ad {@link ActorLookupHelper}
 * leggono direttamente da qui senza effettuare ulteriori chiamate al DMN né al
 * livello di cache L2 (Caffeine).
 *
 * <h3>Flusso di accesso</h3>
 * <ol>
 *   <li>{@link ActorLookupHelper} controlla {@code isResolved()}</li>
 *   <li>Se false → risolve tramite {@link ActorCacheService} (L2) o JWT, poi salva qui</li>
 *   <li>Se true → restituisce direttamente {@code actorData} (zero network calls)</li>
 * </ol>
 */
@Component
@RequestScope
@Getter
@Setter
public class ActorContextHolder {

    /**
     * Dati dell'attore per la request corrente.
     * Null fino alla prima chiamata ad {@link ActorLookupHelper#requireActorData}.
     */
    private MedBookActorData actorData;

    /** @return true se i dati sono già stati risolti in questa request */
    public boolean isResolved() {
        return actorData != null;
    }
}
