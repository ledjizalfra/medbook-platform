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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPreferencesServiceImplTest {

    @Mock
    private NotificationPreferencesRepository preferencesRepository;

    @Mock
    private NotificationPreferencesMapper preferencesMapper;

    @InjectMocks
    private NotificationPreferencesServiceImpl service;

    private final MedBookContext context = new MedBookContext();

    @Test
    void savePreferences_existingActor_updatesAndPersists() {
        SaveNotificationPreferencesRequest request = new SaveNotificationPreferencesRequest()
                .actorId("PAT-1")
                .actorType(NotificationActorTypeApiEnum.PAZIENTE)
                .emailEnabled(true)
                .smsEnabled(false);

        when(preferencesMapper.mapActorTypeFromApi(NotificationActorTypeApiEnum.PAZIENTE))
                .thenReturn(NotificationActorTypeEnum.PAZIENTE);

        NotificationPreferencesEntity existing = new NotificationPreferencesEntity();
        existing.setEmailEnabled(false);
        existing.setSmsEnabled(true);
        when(preferencesRepository.findByActorIdAndActorType("PAT-1", NotificationActorTypeEnum.PAZIENTE))
                .thenReturn(Optional.of(existing));
        when(preferencesRepository.save(any(NotificationPreferencesEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(preferencesMapper.mapToNotificationPreferencesOutput(any()))
                .thenReturn(new NotificationPreferencesOutput().actorId("PAT-1"));

        service.savePreferences(context, request);

        ArgumentCaptor<NotificationPreferencesEntity> captor =
                ArgumentCaptor.forClass(NotificationPreferencesEntity.class);
        verify(preferencesRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existing);
        assertThat(captor.getValue().isEmailEnabled()).isTrue();
        assertThat(captor.getValue().isSmsEnabled()).isFalse();
    }

    @Test
    void savePreferences_actorMissing_createsNewEntity() {
        SaveNotificationPreferencesRequest request = new SaveNotificationPreferencesRequest()
                .actorId("PAT-2")
                .actorType(NotificationActorTypeApiEnum.PAZIENTE)
                .emailEnabled(true)
                .smsEnabled(true);

        when(preferencesMapper.mapActorTypeFromApi(NotificationActorTypeApiEnum.PAZIENTE))
                .thenReturn(NotificationActorTypeEnum.PAZIENTE);
        when(preferencesRepository.findByActorIdAndActorType("PAT-2", NotificationActorTypeEnum.PAZIENTE))
                .thenReturn(Optional.empty());
        when(preferencesRepository.save(any(NotificationPreferencesEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(preferencesMapper.mapToNotificationPreferencesOutput(any()))
                .thenReturn(new NotificationPreferencesOutput());

        service.savePreferences(context, request);

        ArgumentCaptor<NotificationPreferencesEntity> captor =
                ArgumentCaptor.forClass(NotificationPreferencesEntity.class);
        verify(preferencesRepository).save(captor.capture());
        assertThat(captor.getValue().getActorId()).isEqualTo("PAT-2");
        assertThat(captor.getValue().getActorType()).isEqualTo(NotificationActorTypeEnum.PAZIENTE);
    }

    @Test
    void savePreferences_nullFlags_storedAsFalse() {
        SaveNotificationPreferencesRequest request = new SaveNotificationPreferencesRequest()
                .actorId("PAT-3")
                .actorType(NotificationActorTypeApiEnum.PAZIENTE);

        when(preferencesMapper.mapActorTypeFromApi(NotificationActorTypeApiEnum.PAZIENTE))
                .thenReturn(NotificationActorTypeEnum.PAZIENTE);
        when(preferencesRepository.findByActorIdAndActorType("PAT-3", NotificationActorTypeEnum.PAZIENTE))
                .thenReturn(Optional.empty());
        when(preferencesRepository.save(any(NotificationPreferencesEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(preferencesMapper.mapToNotificationPreferencesOutput(any()))
                .thenReturn(new NotificationPreferencesOutput());

        service.savePreferences(context, request);

        ArgumentCaptor<NotificationPreferencesEntity> captor =
                ArgumentCaptor.forClass(NotificationPreferencesEntity.class);
        verify(preferencesRepository).save(captor.capture());
        assertThat(captor.getValue().isEmailEnabled()).isFalse();
        assertThat(captor.getValue().isSmsEnabled()).isFalse();
    }

    @Test
    void getPreferences_existing_returnsMapped() {
        NotificationPreferencesEntity entity = new NotificationPreferencesEntity();
        when(preferencesRepository.findByActorIdAndActorType("PAT-1", NotificationActorTypeEnum.PAZIENTE))
                .thenReturn(Optional.of(entity));
        NotificationPreferencesOutput dto = new NotificationPreferencesOutput();
        when(preferencesMapper.mapToNotificationPreferencesOutput(entity)).thenReturn(dto);

        assertThat(service.getPreferencesByActorId(context, "PAT-1", "PAZIENTE")).isSameAs(dto);
    }

    @Test
    void getPreferences_lowercaseType_isAccepted() {
        NotificationPreferencesEntity entity = new NotificationPreferencesEntity();
        when(preferencesRepository.findByActorIdAndActorType("PAT-1", NotificationActorTypeEnum.PAZIENTE))
                .thenReturn(Optional.of(entity));
        when(preferencesMapper.mapToNotificationPreferencesOutput(entity))
                .thenReturn(new NotificationPreferencesOutput());

        assertThat(service.getPreferencesByActorId(context, "PAT-1", "paziente")).isNotNull();
    }

    @Test
    void getPreferences_missing_throwsNotFound() {
        when(preferencesRepository.findByActorIdAndActorType("PAT-X", NotificationActorTypeEnum.PAZIENTE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPreferencesByActorId(context, "PAT-X", "PAZIENTE"))
                .isInstanceOf(MedBookNotFoundException.class);
    }
}
