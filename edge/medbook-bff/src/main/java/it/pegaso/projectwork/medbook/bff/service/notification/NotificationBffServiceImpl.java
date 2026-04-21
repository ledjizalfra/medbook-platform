package it.pegaso.projectwork.medbook.bff.service.notification;

import it.pegaso.projectwork.medbook.bff.context.ActorLookupHelper;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.notification.client.api.NotificationFeignClient;
import it.pegaso.projectwork.medbook.notification.client.model.NotificationChannelApiEnum;
import it.pegaso.projectwork.medbook.notification.client.model.NotificationStatusApiEnum;
import it.pegaso.projectwork.medbook.notification.client.model.NotificationTypeApiEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/** Implementazione proxy di NotificationBffService.
 * ROLE_PATIENT: inietta automaticamente il proprio patientId come filtro.
 * ROLE_ADMIN: vede tutte le notifiche. */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationBffServiceImpl implements NotificationBffService {

    private final NotificationFeignClient notificationClient;
    private final ActorLookupHelper actorLookupHelper;

    @Override
    public ResponseEntity<MedBookApiResponse> getListNotifications(MedBookContext context, Integer page,
            Integer size, String sort, String appointmentId, String patientId,
            String type, String channel, String status, LocalDate dateFrom, LocalDate dateTo) {

        // ROLE_PATIENT: filtra automaticamente per il proprio patientId
        String effectivePatientId = patientId;
        if (!StringUtils.hasText(effectivePatientId) && isPatient()) {
            effectivePatientId = actorLookupHelper.requireActorId(context);
            log.debug("Notifiche filtrate per patientId={} (auto-inject)", effectivePatientId);
        }

        NotificationTypeApiEnum typeEnum = parseEnum(type, NotificationTypeApiEnum.class, "tipo notifica");
        NotificationChannelApiEnum channelEnum = parseEnum(channel, NotificationChannelApiEnum.class, "canale");
        NotificationStatusApiEnum statusEnum = parseEnum(status, NotificationStatusApiEnum.class, "stato notifica");

        return notificationClient.getListNotifications(context, page, size, sort,
                appointmentId, effectivePatientId, typeEnum, channelEnum, statusEnum, dateFrom, dateTo);
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getNotificationById(MedBookContext context,
            String notificationId) {
        return notificationClient.getNotificationById(context, notificationId);
    }

    private boolean isPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_PATIENT".equals(a.getAuthority()));
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumClass, String label) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            log.warn("Valore {} non riconosciuto: {}", label, value);
            return null;
        }
    }
}
