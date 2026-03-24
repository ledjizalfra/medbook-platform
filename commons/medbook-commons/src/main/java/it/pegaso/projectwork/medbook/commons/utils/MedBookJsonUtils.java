package it.pegaso.projectwork.medbook.commons.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MedBookJsonUtils {
    private MedBookJsonUtils() {
        // Private method to avoid instantiation
    }


    // ObjectMapper condiviso e configurato — thread-safe
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Converte un oggetto Java in una stringa JSON.
     *
     * @param object l'oggetto da serializzare
     * @param pretty se true formatta il JSON con indentazione leggibile
     * @return la stringa JSON oppure null in caso di errore di serializzazione
     */
    public static String toJson(Object object, boolean pretty) {
        if (object == null) {
            return null;
        }
        try {
            if (pretty) {
                return OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(object);
            }
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Errore durante la serializzazione dell'oggetto in JSON: {}",
                    e.getMessage(), e);
            return null;
        }
    }
}
