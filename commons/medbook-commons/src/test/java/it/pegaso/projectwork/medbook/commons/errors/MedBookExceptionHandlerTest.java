package it.pegaso.projectwork.medbook.commons.errors;

import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiErrorResponse;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MedBookExceptionHandlerTest {

    private MedBookExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new MedBookExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    // =========================================================================
    // VALIDAZIONE
    // =========================================================================

    @Test
    void handleValidation_methodArgumentNotValid_returns422WithFieldErrors() throws Exception {
        // Costruisce un BindingResult realistico con due field error
        BindingResult bindingResult = new BindException(new Object(), "target");
        bindingResult.addError(new FieldError("target", "email", "non puo' essere vuoto"));
        bindingResult.addError(new FieldError("target", "phone", "formato non valido"));

        MethodParameter mp = new MethodParameter(
                MedBookExceptionHandlerTest.class.getDeclaredMethod("dummyMethod"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mp, bindingResult);

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(MedBookErrorCode.VALIDATION_ERROR.getHttpStatus());
        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).asInstanceOf(
                org.assertj.core.api.InstanceOfAssertFactories.list(String.class))
                .containsExactlyInAnyOrder(
                        "email: non puo' essere vuoto",
                        "phone: formato non valido");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
    }

    @Test
    void handleConstraintViolation_returnsValidationStatusWithErrorList() {
        ConstraintViolation<?> v1 = mock(ConstraintViolation.class);
        Path p1 = mock(Path.class);
        when(p1.toString()).thenReturn("createPatient.email");
        when(v1.getPropertyPath()).thenReturn(p1);
        when(v1.getMessage()).thenReturn("formato email non valido");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(v1));

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleConstraintViolation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(MedBookErrorCode.CONSTRAINT_VIOLATION.getHttpStatus());
        assertThat(response.getBody().getErrorCode()).isEqualTo("CONSTRAINT_VIOLATION");
        assertThat(response.getBody().getMessage()).asInstanceOf(
                org.assertj.core.api.InstanceOfAssertFactories.list(String.class))
                .contains("createPatient.email: formato email non valido");
    }

    // =========================================================================
    // DOMINIO / APPLICAZIONE
    // =========================================================================

    @Test
    void handleResourceNotFound_returns404WithMessage() {
        MedBookNotFoundException ex = new MedBookNotFoundException("paziente PAT-99 non trovato");

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleResourceNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("paziente PAT-99 non trovato");
    }

    @Test
    void handleBusinessException_usesErrorCodeFromException() {
        MedBookBusinessException ex = new MedBookBusinessException(
                MedBookErrorCode.SLOT_ALREADY_BOOKED, "slot 09:00 occupato");

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleBusinessException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(MedBookErrorCode.SLOT_ALREADY_BOOKED.getHttpStatus());
        assertThat(response.getBody().getErrorCode()).isEqualTo("SLOT_ALREADY_BOOKED");
        assertThat(response.getBody().getMessage()).isEqualTo("slot 09:00 occupato");
    }

    @Test
    void handleBusinessValidationException_returnsErrorList() {
        List<String> errors = List.of("nome: obbligatorio", "email: non valida");
        MedBookBusinessValidationException ex = new MedBookBusinessValidationException(errors);

        ResponseEntity<MedBookApiErrorResponse> response =
                handler.handleBusinessValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(MedBookErrorCode.VALIDATION_ERROR.getHttpStatus());
        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).asInstanceOf(
                org.assertj.core.api.InstanceOfAssertFactories.list(String.class))
                .containsExactlyElementsOf(errors);
    }

    // =========================================================================
    // HTTP
    // =========================================================================

    @Test
    void handleMethodNotSupported_returns405WithMethodInMessage() {
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("DELETE");

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleMethodNotSupported(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody().getErrorCode()).isEqualTo("METHOD_NOT_ALLOWED");
        assertThat(response.getBody().getMessage().toString()).contains("DELETE");
    }

    @Test
    void handleMediaTypeNotSupported_returns415() {
        HttpMediaTypeNotSupportedException ex =
                new HttpMediaTypeNotSupportedException(MediaType.APPLICATION_XML, List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleMediaTypeNotSupported(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody().getErrorCode()).isEqualTo("UNSUPPORTED_MEDIA_TYPE");
        assertThat(response.getBody().getMessage().toString()).contains("application/xml");
    }

    @Test
    void handleNoHandlerFound_returns404WithRequestUri() throws Exception {
        when(request.getRequestURI()).thenReturn("/inesistente");
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/inesistente", new HttpHeaders());

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleNoHandlerFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(MedBookErrorCode.ENDPOINT_NOT_FOUND.getHttpStatus());
        assertThat(response.getBody().getErrorCode()).isEqualTo("ENDPOINT_NOT_FOUND");
        assertThat(response.getBody().getMessage().toString()).contains("/inesistente");
    }

    @Test
    void handleMessageNotReadable_returnsMalformedRequestStatus() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("JSON malformato",
                        new org.springframework.http.converter.HttpMessageNotReadableException("x"));

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleMessageNotReadable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(MedBookErrorCode.MALFORMED_REQUEST.getHttpStatus());
        assertThat(response.getBody().getErrorCode()).isEqualTo("MALFORMED_REQUEST");
    }

    @Test
    void handleMissingParameter_includesParameterNameInMessage() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("dateFrom", "LocalDate");

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleMissingParameter(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(MedBookErrorCode.MISSING_PARAMETER.getHttpStatus());
        assertThat(response.getBody().getMessage().toString()).contains("dateFrom");
    }

    @Test
    void handleMissingPathVariable_includesVariableNameInMessage() throws Exception {
        MethodParameter mp = new MethodParameter(
                MedBookExceptionHandlerTest.class.getDeclaredMethod("dummyMethod"), -1);
        MissingPathVariableException ex = new MissingPathVariableException("patientId", mp);

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleMissingPathVariable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(MedBookErrorCode.MISSING_PATH_VARIABLE.getHttpStatus());
        assertThat(response.getBody().getMessage().toString()).contains("patientId");
    }

    @Test
    void handleTypeMismatch_includesValueAndParameterNameInMessage() throws Exception {
        MethodParameter mp = new MethodParameter(
                MedBookExceptionHandlerTest.class.getDeclaredMethod("dummyMethod"), -1);
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "id", mp, new IllegalArgumentException("not a number"));

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleTypeMismatch(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(MedBookErrorCode.TYPE_MISMATCH.getHttpStatus());
        assertThat(response.getBody().getMessage().toString()).contains("abc").contains("id");
    }

    // =========================================================================
    // SICUREZZA
    // =========================================================================

    @Test
    void handleAuthentication_returns401() {
        AuthenticationException ex = new AuthenticationException("token scaduto") {};

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleAuthentication(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getErrorCode()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    void handleAccessDenied_returns403() {
        AccessDeniedException ex = new AccessDeniedException("ROLE_ADMIN richiesto");

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getErrorCode()).isEqualTo("ACCESS_DENIED");
    }

    // =========================================================================
    // FEIGN
    // =========================================================================

    @Test
    void handleFeignException_returnsFeignClientErrorStatus() {
        Request feignReq = Request.create(Request.HttpMethod.GET, "/x", new HashMap<>(),
                Request.Body.empty(), new feign.RequestTemplate());
        FeignException ex = new FeignException.NotFound(
                "not found", feignReq, "body".getBytes(StandardCharsets.UTF_8), null);

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleFeignException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(MedBookErrorCode.FEIGN_CLIENT_ERROR.getHttpStatus());
        assertThat(response.getBody().getErrorCode()).isEqualTo("FEIGN_CLIENT_ERROR");
    }

    @Test
    void handleFeignRetryable_returnsServiceUnavailable() {
        Request feignReq = Request.create(Request.HttpMethod.GET, "/x", new HashMap<>(),
                Request.Body.empty(), new feign.RequestTemplate());
        RetryableException ex = new RetryableException(
                503, "downstream giù", Request.HttpMethod.GET,
                new RuntimeException("connect refused"), (Long) null, feignReq);

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleFeignRetryable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(MedBookErrorCode.SERVICE_UNAVAILABLE.getHttpStatus());
        assertThat(response.getBody().getErrorCode()).isEqualTo("SERVICE_UNAVAILABLE");
    }

    // =========================================================================
    // FALLBACK GENERICO
    // =========================================================================

    @Test
    void handleGeneric_returns500() {
        Exception ex = new RuntimeException("qualcosa di imprevisto");

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    void handleGeneric_nullPointerException_returns500() {
        // Verifica che NPE non sia gestita in modo speciale ma cada nel fallback generico
        NullPointerException ex = new NullPointerException("oggetto null");

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // =========================================================================
    // VERIFICA STRUTTURA RISPOSTA
    // =========================================================================

    @Test
    void allHandlers_setPathFromRequest() {
        when(request.getRequestURI()).thenReturn("/specifico/path");
        MedBookNotFoundException ex = new MedBookNotFoundException("test");

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleResourceNotFound(ex, request);

        assertThat(response.getBody().getPath()).isEqualTo("/specifico/path");
    }

    @Test
    void allHandlers_setHttpStatusInBody() {
        MedBookBusinessException ex = new MedBookBusinessException(
                MedBookErrorCode.BUSINESS_ERROR, "test");

        ResponseEntity<MedBookApiErrorResponse> response = handler.handleBusinessException(ex, request);

        assertThat(response.getBody().getHttpStatus())
                .isEqualTo(MedBookErrorCode.BUSINESS_ERROR.getHttpStatus().value());
    }

    // Metodo dummy usato per costruire MethodParameter nei test
    @SuppressWarnings("unused")
    private void dummyMethod() {}
}
