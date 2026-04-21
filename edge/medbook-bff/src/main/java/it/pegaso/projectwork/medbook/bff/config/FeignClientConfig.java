package it.pegaso.projectwork.medbook.bff.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Configurazione Feign per il BFF.
 * Propaga automaticamente il token JWT verso tutti i DMN downstream.
 * Propaga le business error 4xx dei DMN al client invece di avvolgerle in 502. */
@Slf4j
@Configuration
public class FeignClientConfig {

    @Bean
    public ErrorDecoder feignErrorDecoder(ObjectMapper objectMapper) {
        return new MedBookFeignErrorDecoder(objectMapper);
    }

    /** Interceptor che copia il Bearer token dall'header Authorization della
     * richiesta corrente e lo aggiunge a ogni chiamata Feign verso i DMN.
     * Garantisce che l'identita dell'utente sia propagata a valle. */
    @Bean
    public RequestInterceptor jwtRelayInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String auth = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (StringUtils.hasText(auth)) {
                    requestTemplate.header(HttpHeaders.AUTHORIZATION, auth);
                }
            }
            // X-MedBook-Context viene dichiarato nell'OpenAPI spec come header opzionale
            // ma i DMN lo ignorano — usano MedBookContextHolder popolato dal JWT.
            // L'oggetto MedBookContext serializzato con toString() produce newline
            // che sono caratteri illegali negli header HTTP (RFC 7230).
            requestTemplate.removeHeader("X-MedBook-Context");
        };
    }
}
