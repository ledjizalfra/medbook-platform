package it.pegaso.projectwork.medbook.commons.context;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolver che inietta MedBookContext nei parametri dei metodi controller.
 * Intercetta i parametri di tipo MedBookContext (generati dalla spec OpenAPI
 * con @RequestHeader) e restituisce il contesto dal ThreadLocal
 * invece di leggere l'header HTTP (che non contiene un oggetto serializzato).
 *
 * Deve essere inserito in posizione 0 nella lista dei resolver
 * tramite MedBookContextResolverRegistrar (BeanPostProcessor)
 * per girare PRIMA del built-in @RequestHeader resolver.
 */
public class MedBookContextHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return MedBookContext.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return MedBookContextHolder.get();
    }
}
