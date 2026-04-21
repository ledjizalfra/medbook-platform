package it.pegaso.projectwork.medbook.commons.cache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import it.pegaso.projectwork.medbook.commons.cache.MedBookCacheNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configurazione Spring Cache con provider Caffeine.
 * Registra le cache dichiarate in {@link MedBookCacheNames} con TTL e dimensione massima
 * configurabili tramite YAML del modulo che include commons.
 * <p>
 * I moduli che importano commons non devono ridichiarare {@code @EnableCaching}.
 * <p>
 * Esempio di configurazione nel YAML del BFF:
 * <pre>
 * medbook:
 *   cache:
 *     doctor-availability-templates:
 *       ttl: PT30M
 *       max-size: 500
 * </pre>
 */
@Configuration
@EnableCaching
public class MedBookCacheConfig {

    @Bean
    public CacheManager cacheManager(
            @Value("${medbook.cache.doctor-availability-templates.ttl:PT30M}")
            Duration availabilityTemplatesTtl,
            @Value("${medbook.cache.doctor-availability-templates.max-size:500}")
            int availabilityTemplatesMaxSize,
            @Value("${medbook.cache.actor-profile.ttl:PT60M}")
            Duration actorProfileTtl,
            @Value("${medbook.cache.actor-profile.max-size:500}")
            int actorProfileMaxSize) {

        CaffeineCacheManager manager = new CaffeineCacheManager();

        // Cache template disponibilità medico — usata dal BFF durante la ricerca slot
        manager.registerCustomCache(
                MedBookCacheNames.DOCTOR_AVAILABILITY_TEMPLATES,
                Caffeine.newBuilder()
                        .expireAfterWrite(availabilityTemplatesTtl)
                        .maximumSize(availabilityTemplatesMaxSize)
                        .build()
        );

        // Cache profilo attore (paziente/medico) per email — usata dal BFF (Approccio A)
        // per evitare chiamate ripetute ai DMN durante la risoluzione dell'utente autenticato.
        // TTL lungo (default 60 min): businessKey, email, CF, licenseNumber, dateOfBirth, gender sono immutabili.
        manager.registerCustomCache(
                MedBookCacheNames.ACTOR_PROFILE,
                Caffeine.newBuilder()
                        .expireAfterWrite(actorProfileTtl)
                        .maximumSize(actorProfileMaxSize)
                        .build()
        );

        return manager;
    }
}
