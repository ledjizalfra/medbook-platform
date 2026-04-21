package it.pegaso.projectwork.medbook.commons.api.web;

import it.pegaso.projectwork.medbook.commons.context.MedBookContextInterceptor;
import it.pegaso.projectwork.medbook.commons.context.MedBookContextResolverRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configurazione MVC condivisa per tutti i DMN MedBook.
 * Registra l'interceptor per il contesto e il BeanPostProcessor
 * per il resolver MedBookContext.
 * Auto-importato via spring.factories dai DMN che includono medbook-commons.
 */
@Configuration
public class MedBookWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(medBookContextInterceptor());
    }

    @Bean
    public MedBookContextInterceptor medBookContextInterceptor() {
        return new MedBookContextInterceptor();
    }

    @Bean
    public MedBookContextResolverRegistrar medBookContextResolverRegistrar() {
        return new MedBookContextResolverRegistrar();
    }
}
