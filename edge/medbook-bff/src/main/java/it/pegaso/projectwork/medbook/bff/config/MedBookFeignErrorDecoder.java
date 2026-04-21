package it.pegaso.projectwork.medbook.bff.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiErrorResponse;
import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * ErrorDecoder Feign per il BFF.
 *
 * Qualsiasi errore restituito da un DMN (4xx, 5xx) porta una MedBookApiErrorResponse.
 * Invece di avvolgerlo in un generico FeignException → 502, lo convertiamo
 * nell'eccezione MedBook corrispondente così che MedBookExceptionHandler lo
 * restituisca al client con il codice HTTP, il codice errore e il messaggio originali.
 *
 * Fallback al comportamento default solo se il body non è deserializzabile
 * come MedBookApiErrorResponse (es. errori di rete o risposte non strutturate).
 */
@Slf4j
@RequiredArgsConstructor
public class MedBookFeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            MedBookApiErrorResponse error = objectMapper.readValue(body, MedBookApiErrorResponse.class);

            String errorCodeStr = error.getErrorCode();
            Object message = error.getMessage();

            // Lista di errori di validazione
            if (message instanceof List<?> list) {
                List<String> errors = list.stream().map(Object::toString).toList();
                return new MedBookBusinessValidationException(errors);
            }

            String msg = message != null ? message.toString() : errorCodeStr;

            // 404 RESOURCE_NOT_FOUND
            if (MedBookErrorCode.RESOURCE_NOT_FOUND.name().equals(errorCodeStr)) {
                return new MedBookNotFoundException(msg);
            }

            // Qualsiasi altro errore — propaga errorCode e messaggio originali del DMN
            MedBookErrorCode code = parseErrorCode(errorCodeStr);
            return new MedBookBusinessException(code, msg);

        } catch (Exception parseEx) {
            log.warn("Impossibile deserializzare il corpo dell'errore Feign per [{}]: {}", methodKey, parseEx.getMessage());
        }

        return defaultDecoder.decode(methodKey, response);
    }

    private MedBookErrorCode parseErrorCode(String errorCodeStr) {
        try {
            return MedBookErrorCode.valueOf(errorCodeStr);
        } catch (IllegalArgumentException e) {
            return MedBookErrorCode.BUSINESS_ERROR;
        }
    }
}
