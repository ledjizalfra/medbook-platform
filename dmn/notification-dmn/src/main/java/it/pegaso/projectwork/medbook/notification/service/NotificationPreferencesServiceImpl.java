package it.pegaso.projectwork.medbook.notification.service;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.notification.entity.NotificationPreferencesEntity;
import it.pegaso.projectwork.medbook.notification.mapper.NotificationPreferencesMapper;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationActorTypeEnum;
import it.pegaso.projectwork.medbook.notification.repository.NotificationPreferencesRepository;
import it.pegaso.projectwork.medbook.notification.server.model.NotificationActorTypeApiEnum;
import it.pegaso.projectwork.medbook.notification.server.model.NotificationPreferencesOutput;
import it.pegaso.projectwork.medbook.notification.server.model.SaveNotificationPreferencesRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementazione del service per le preferenze di notifica.
 * Gestisce l'upsert (crea o aggiorna) e la lettura delle preferenze
 * per i pazienti e i medici della piattaforma.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferencesServiceImpl implements NotificationPreferencesService {

    private final NotificationPreferencesRepository preferencesRepository;
    private final NotificationPreferencesMapper preferencesMapper;

    @Override
    @Transactional
    public NotificationPreferencesOutput savePreferences(MedBookContext context,
            SaveNotificationPreferencesRequest request) {

        NotificationActorTypeEnum actorType = preferencesMapper.mapActorTypeFromApi(request.getActorType());

        // Upsert: aggiorna se esiste, crea se non esiste
        NotificationPreferencesEntity entity = preferencesRepository
                .findByActorIdAndActorType(request.getActorId(), actorType)
                .orElseGet(NotificationPreferencesEntity::new);

        entity.setActorId(request.getActorId());
        entity.setActorType(actorType);
        entity.setEmailEnabled(Boolean.TRUE.equals(request.getEmailEnabled()));
        entity.setSmsEnabled(Boolean.TRUE.equals(request.getSmsEnabled()));

        NotificationPreferencesEntity saved = preferencesRepository.save(entity);
        log.info("Preferenze di notifica salvate per actorId={} actorType={} email={} sms={}",
                saved.getActorId(), saved.getActorType(), saved.isEmailEnabled(), saved.isSmsEnabled());

        return preferencesMapper.mapToNotificationPreferencesOutput(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferencesOutput getPreferencesByActorId(MedBookContext context,
            String actorId, String actorType) {

        NotificationActorTypeEnum type = NotificationActorTypeEnum.valueOf(actorType.toUpperCase());
        NotificationPreferencesEntity entity = preferencesRepository
                .findByActorIdAndActorType(actorId, type)
                .orElseThrow(() -> new MedBookNotFoundException(
                        "preferenze di notifica non trovate per actorId=" + actorId));

        return preferencesMapper.mapToNotificationPreferencesOutput(entity);
    }
}
