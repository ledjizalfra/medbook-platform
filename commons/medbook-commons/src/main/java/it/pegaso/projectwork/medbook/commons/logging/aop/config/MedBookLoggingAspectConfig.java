package it.pegaso.projectwork.medbook.commons.logging.aop.config;

import it.pegaso.projectwork.medbook.commons.logging.utils.MedBookLogUtils;
import it.pegaso.projectwork.medbook.commons.utils.MedBookJsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Aspect per il logging automatico del tempo di esecuzione
 * di tutti i metodi nei layer controller e service dei DMN.
 * Si applica automaticamente a tutti i DMN che importano medbook-commons.
 */
@Slf4j
@Aspect
@Component
@ConditionalOnProperty(
        prefix = "medbook.logging-aspect",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MedBookLoggingAspectConfig {

    // =========================================================================
    // POINTCUT — definisce dove applicare il logging
    // =========================================================================

    // Tutti i metodi di tutti i controller dei MS
    @Pointcut("execution(* it.pegaso.projectwork.medbook.*.controller.*.*(..))")
    public void controllerPointcut() {}

    // Tutti i metodi di tutti i service dei MS
    @Pointcut("execution(* it.pegaso.projectwork.medbook.*.service.*.*(..))")
    public void servicePointcut() {}


    // =========================================================================
    // ADVICE — logica di logging
    // =========================================================================

    /**
     * Logga automaticamente inizio, fine e tempo di esecuzione
     * di tutti i metodi nei controller dei MS.
     */
    @Around("controllerPointcut()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, "Controller");
    }

    /**
     * Logga automaticamente inizio, fine e tempo di esecuzione
     * di tutti i metodi nei service dei MS.
     */
    @Around("servicePointcut()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, "Service");
    }


    // =========================================================================
    // METODO PRIVATO DI UTILITÀ
    // =========================================================================

    /**
     * Esegue il logging comune per controller e service.
     * Logga: nome metodo, argomenti, risultato e tempo di esecuzione.
     */
    private Object logExecution(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        long startTime = MedBookLogUtils.startTimer();

        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // Logga i parametri in solo DEBUG level.
        // In ambiente di produzione, logga solo in INFO senza i parametri per evitare overhead e rischi di sicurezza.
        // I parametri importanti per il debugging saranno inseriti nel MDC
        if (log.isDebugEnabled()) {
            log.debug("\n>>> START [{}.{}] in {} - params: {}\n",
                    className, methodName, layer,
                    MedBookJsonUtils.toJson(joinPoint.getArgs(), false));
        } else {
            log.info("\n>>> START [{}.{}] in {}\n", className, methodName, layer);
        }

        try {
            Object result = joinPoint.proceed();

            if (log.isDebugEnabled()) {
                log.debug("\n<<< END [{}.{}] in {} - proceed in: {} - result: {}\n",
                        className, methodName, layer,
                        MedBookLogUtils.elapsedTimeFormatted(startTime),
                        MedBookJsonUtils.toJson(result, false));
            } else {
                log.info("\n<<< END [{}.{}] in {} - proceed in: {}\n",
                        className, methodName, layer,
                        MedBookLogUtils.elapsedTimeFormatted(startTime));
            }

            return result;

        } catch (Exception ex) {
            log.error("\n<<< ERROR [{}.{}] in {} - proceed in: {} — exception: {}\n",
                    className, methodName, layer,
                    MedBookLogUtils.elapsedTimeFormatted(startTime),
                    ex.getMessage());
            throw ex;
        }
    }
}