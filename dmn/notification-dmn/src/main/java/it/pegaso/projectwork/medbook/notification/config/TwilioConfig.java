package it.pegaso.projectwork.medbook.notification.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Configurazione del Twilio SDK per l'invio SMS.
 * Inizializza il client Twilio con le credenziali del config-server.
 * L'inizializzazione viene saltata se mock-enabled = true per risparmiare
 * i crediti Twilio in sviluppo.
 */
@Slf4j
@Configuration
public class TwilioConfig {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${notification.sms.mock-enabled:false}")
    private boolean mockEnabled;

    /** Inizializza il client Twilio all'avvio del contesto Spring.
     * Saltato se mock-enabled = true o se le credenziali non sono configurate. */
    @PostConstruct
    public void initTwilio() {
        if (mockEnabled) {
            log.info("Twilio in modalita MOCK — SDK non inizializzato");
            return;
        }
        if (!StringUtils.hasText(accountSid) || !StringUtils.hasText(authToken)) {
            log.warn("Credenziali Twilio non configurate — invio SMS disabilitato");
            return;
        }
        Twilio.init(accountSid, authToken);
        log.info("Twilio SDK inizializzato con account SID: {}***", accountSid.substring(0, 6));
    }
}
