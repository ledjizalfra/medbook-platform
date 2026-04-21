package it.pegaso.projectwork.medbook.commons.context;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;

/**
 * Holder statico del contesto di richiesta MedBook.
 * Segue il pattern ThreadLocal (come SecurityContextHolder).
 * Viene popolato da MedBookContextInterceptor all'inizio di ogni request
 * e svuotato nella afterCompletion per evitare memory leak.
 */
public class MedBookContextHolder {

    private static final ThreadLocal<MedBookContext> CONTEXT = new ThreadLocal<>();

    private MedBookContextHolder() {}

    public static void set(MedBookContext context) {
        CONTEXT.set(context);
    }

    public static MedBookContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
