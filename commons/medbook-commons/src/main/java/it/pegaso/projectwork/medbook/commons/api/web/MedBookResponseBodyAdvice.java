package it.pegaso.projectwork.medbook.commons.api.web;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiErrorResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.LocalDateTime;

/**
 * Advice che arricchisce automaticamente tutte le response MedBook
 * con i campi comuni: success, httpStatus, timestamp, traceId.
 * Evita la duplicazione di queste assegnazioni in ogni controller ed exception handler.
 *
 * L'ordine degli instanceof è importante:
 * MedBookApiErrorResponse deve essere controllato PRIMA di MedBookApiVoidResponse
 * perché in Java MedBookApiErrorResponse extends MedBookApiVoidResponse.
 */
@ControllerAdvice
public class MedBookResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {

        if (body instanceof MedBookApiVoidResponse medbookResponse) {
            medbookResponse.setSuccess(!(body instanceof MedBookApiErrorResponse));
            medbookResponse.setTimestamp(LocalDateTime.now());
            medbookResponse.setTraceId(MDC.get("traceId"));
            if (medbookResponse.getHttpStatus() == null) {
                medbookResponse.setHttpStatus(resolveStatus(response));
            }
        }

        return body;
    }

    private Integer resolveStatus(ServerHttpResponse serverHttpResponse) {
        if (serverHttpResponse instanceof ServletServerHttpResponse servletResponse) {
            return servletResponse.getServletResponse().getStatus();
        }
        return null;
    }
}
