package it.pegaso.projectwork.medbook.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 * Configurazione del template engine Thymeleaf dedicato alle email.
 * Usa un engine separato da quello Spring MVC per evitare interferenze.
 * I template HTML sono in classpath:/templates/mail/.
 */
@Configuration
public class MailConfig {

    /** Template engine dedicato alle email — qualificato come "mailTemplateEngine"
     * e iniettato in MailNotificationSender tramite @Qualifier. */
    @Bean("mailTemplateEngine")
    public SpringTemplateEngine mailTemplateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(mailTemplateResolver());
        return engine;
    }

    /** Resolver che carica i template HTML da classpath:/templates/mail/. */
    private ITemplateResolver mailTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/mail/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);
        resolver.setOrder(1);
        return resolver;
    }
}
