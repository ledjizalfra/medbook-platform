package it.pegaso.projectwork.medbook.commons.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * BeanPostProcessor che inserisce MedBookContextHandlerMethodArgumentResolver
 * in posizione 0 nella lista dei resolver di RequestMappingHandlerAdapter.
 *
 * Posizione 0 è necessaria per girare PRIMA del built-in @RequestHeader resolver:
 * i parametri MedBookContext nell'interfaccia generata da OpenAPI hanno
 * l'annotazione @RequestHeader, ma il nostro resolver deve intercettarli
 * per primo e restituire il contesto dal ThreadLocal.
 */
public class MedBookContextResolverRegistrar implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RequestMappingHandlerAdapter adapter) {
            List<HandlerMethodArgumentResolver> existing = adapter.getArgumentResolvers();
            List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
            resolvers.add(new MedBookContextHandlerMethodArgumentResolver());
            if (existing != null) {
                resolvers.addAll(existing);
            }
            adapter.setArgumentResolvers(resolvers);
        }
        return bean;
    }
}
