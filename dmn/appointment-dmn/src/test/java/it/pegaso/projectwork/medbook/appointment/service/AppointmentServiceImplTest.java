package it.pegaso.projectwork.medbook.appointment.service;

import it.pegaso.projectwork.medbook.appointment.helper.AppointmentDomainHelper;
import it.pegaso.projectwork.medbook.appointment.kafka.AppointmentEventPublisher;
import it.pegaso.projectwork.medbook.appointment.mapper.AppointmentMapper;
import it.pegaso.projectwork.medbook.appointment.model.entity.AppointmentEntity;
import it.pegaso.projectwork.medbook.appointment.model.enums.AppointmentStatusEnum;
import it.pegaso.projectwork.medbook.appointment.model.enums.CancelledByEnum;
import it.pegaso.projectwork.medbook.appointment.repository.AppointmentRepository;
import it.pegaso.projectwork.medbook.appointment.server.model.AppointmentResponse;
import it.pegaso.projectwork.medbook.appointment.server.model.BookAppointmentRequest;
import it.pegaso.projectwork.medbook.appointment.server.model.CancelAppointmentRequest;
import it.pegaso.projectwork.medbook.appointment.server.model.CancelledByApiEnum;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentDomainHelper appointmentDomainHelper;

    @Mock
    private it.pegaso.projectwork.medbook.appointment.validator.AppointmentValidator appointmentValidator;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private AppointmentEventPublisher eventPublisher;

    @InjectMocks
    private AppointmentServiceImpl service;

    private MedBookContext context;
    private AppointmentEntity entity;

    @BeforeEach
    void setUp() {
        context = new MedBookContext();
        context.setUsername("admin@medbook.it");

        entity = new AppointmentEntity();
        entity.setAppointmentId("APT-1");
        entity.setPatientId("PAT-1");
        entity.setDoctorId("DOC-1");
        entity.setStatus(AppointmentStatusEnum.PRENOTATO);
        entity.setSlotDate(LocalDate.of(2026, 6, 1));
        entity.setStartTime(LocalTime.of(9, 0));
    }

    @Test
    void bookAppointment_validRequest_persistsAndPublishesEvent() {
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setStartTime("09:00");
        request.setSlotDate(LocalDate.of(2026, 6, 1));
        request.setDoctorId("DOC-1");

        when(appointmentMapper.mapStringToLocalTime("09:00")).thenReturn(LocalTime.of(9, 0));

        AppointmentEntity newEntity = new AppointmentEntity();
        when(appointmentMapper.mapToAppointmentEntity(request)).thenReturn(newEntity);
        when(appointmentDomainHelper.generateAppointmentId()).thenReturn("APT-100");
        when(appointmentRepository.save(any(AppointmentEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(appointmentMapper.mapToAppointmentResponse(any())).thenReturn(new AppointmentResponse().appointmentId("APT-100"));

        AppointmentResponse response = service.bookAppointment(context, request);

        assertThat(response.getAppointmentId()).isEqualTo("APT-100");
        verify(appointmentValidator).validateBookingConstraints(request);
        verify(appointmentValidator).validateSlotAvailableForBooking(
                "DOC-1", LocalDate.of(2026, 6, 1), LocalTime.of(9, 0));

        ArgumentCaptor<AppointmentEntity> captor = ArgumentCaptor.forClass(AppointmentEntity.class);
        verify(appointmentRepository).save(captor.capture());
        assertThat(captor.getValue().getAppointmentId()).isEqualTo("APT-100");
        assertThat(captor.getValue().getStatus()).isEqualTo(AppointmentStatusEnum.PRENOTATO);
        assertThat(captor.getValue().getBookingDate()).isNotNull();

        verify(eventPublisher).publishAppointmentBooked(any(), any());
    }

    @Test
    void bookAppointment_constraintsFail_doesNotPersistOrPublish() {
        BookAppointmentRequest request = new BookAppointmentRequest();
        org.mockito.Mockito.doThrow(new RuntimeException("conflict"))
                .when(appointmentValidator).validateBookingConstraints(request);

        assertThatThrownBy(() -> service.bookAppointment(context, request))
                .isInstanceOf(RuntimeException.class);

        verify(appointmentRepository, never()).save(any());
        verify(eventPublisher, never()).publishAppointmentBooked(any(), any());
    }

    @Test
    void bookAppointment_publisherThrows_doesNotPropagate() {
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setStartTime("09:00");
        request.setSlotDate(LocalDate.of(2026, 6, 1));
        request.setDoctorId("DOC-1");

        when(appointmentMapper.mapStringToLocalTime("09:00")).thenReturn(LocalTime.of(9, 0));
        when(appointmentMapper.mapToAppointmentEntity(request)).thenReturn(new AppointmentEntity());
        when(appointmentDomainHelper.generateAppointmentId()).thenReturn("APT-100");
        when(appointmentRepository.save(any(AppointmentEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(appointmentMapper.mapToAppointmentResponse(any())).thenReturn(new AppointmentResponse());

        org.mockito.Mockito.doThrow(new RuntimeException("kafka down"))
                .when(eventPublisher).publishAppointmentBooked(any(), any());

        // L'errore del publisher è swallowato dal service
        service.bookAppointment(context, request);

        verify(appointmentRepository).save(any());
    }

    @Test
    void getAppointmentById_existing_returnsResponse() {
        when(appointmentDomainHelper.retrieveOrThrow("APT-1")).thenReturn(entity);
        AppointmentResponse dto = new AppointmentResponse().appointmentId("APT-1");
        when(appointmentMapper.mapToAppointmentResponse(entity)).thenReturn(dto);

        assertThat(service.getAppointmentById(context, "APT-1")).isSameAs(dto);
    }

    @Test
    void cancelAppointment_validRequest_setsStatusAndPublishesEvent() {
        CancelAppointmentRequest request = new CancelAppointmentRequest()
                .cancelledBy(CancelledByApiEnum.PAZIENTE)
                .cancellationReason("imprevisto");
        when(appointmentDomainHelper.retrieveOrThrow("APT-1")).thenReturn(entity);
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentMapper.mapToAppointmentResponse(entity)).thenReturn(new AppointmentResponse());

        service.cancelAppointment(context, "APT-1", request);

        verify(appointmentValidator).validateCancelAppointmentRequest(entity, request);
        assertThat(entity.getStatus()).isEqualTo(AppointmentStatusEnum.CANCELLATO);
        assertThat(entity.getCancellationReason()).isEqualTo("imprevisto");
        assertThat(entity.getCancelledBy()).isEqualTo(CancelledByEnum.PAZIENTE);
        verify(eventPublisher).publishAppointmentCancelled(entity, request);
    }

    @Test
    void cancelAppointment_publisherThrows_doesNotPropagate() {
        CancelAppointmentRequest request = new CancelAppointmentRequest()
                .cancelledBy(CancelledByApiEnum.PAZIENTE);
        when(appointmentDomainHelper.retrieveOrThrow("APT-1")).thenReturn(entity);
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentMapper.mapToAppointmentResponse(entity)).thenReturn(new AppointmentResponse());

        org.mockito.Mockito.doThrow(new RuntimeException("kafka down"))
                .when(eventPublisher).publishAppointmentCancelled(any(), any());

        service.cancelAppointment(context, "APT-1", request);

        verify(appointmentRepository).save(entity);
    }

    @Test
    void restoreAppointment_validRequest_resetsStatusAndClearsCancellationFields() {
        entity.setStatus(AppointmentStatusEnum.CANCELLATO);
        entity.setCancellationReason("imprevisto");
        entity.setCancelledBy(CancelledByEnum.PAZIENTE);
        when(appointmentDomainHelper.retrieveOrThrow("APT-1")).thenReturn(entity);
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentMapper.mapToAppointmentResponse(entity)).thenReturn(new AppointmentResponse());

        service.restoreAppointment(context, "APT-1");

        verify(appointmentValidator).validateRestoreAppointment(
                entity, "DOC-1", LocalDate.of(2026, 6, 1), LocalTime.of(9, 0));
        assertThat(entity.getStatus()).isEqualTo(AppointmentStatusEnum.PRENOTATO);
        assertThat(entity.getCancellationReason()).isNull();
        assertThat(entity.getCancelledBy()).isNull();
    }

    @Test
    void completeAppointment_inCorso_setsCompletato() {
        entity.setStatus(AppointmentStatusEnum.IN_CORSO);
        when(appointmentDomainHelper.retrieveOrThrow("APT-1")).thenReturn(entity);
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentMapper.mapToAppointmentResponse(entity)).thenReturn(new AppointmentResponse());

        service.completeAppointment(context, "APT-1");

        verify(appointmentValidator).validateTransitionFromInCorso(entity);
        assertThat(entity.getStatus()).isEqualTo(AppointmentStatusEnum.COMPLETATO);
    }

    @Test
    void noShowAppointment_booked_setsNonPresentato() {
        when(appointmentDomainHelper.retrieveOrThrow("APT-1")).thenReturn(entity);
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentMapper.mapToAppointmentResponse(entity)).thenReturn(new AppointmentResponse());

        service.noShowAppointment(context, "APT-1");

        verify(appointmentValidator).validateTransitionFromBooked(entity, "NON_PRESENTATO");
        assertThat(entity.getStatus()).isEqualTo(AppointmentStatusEnum.NON_PRESENTATO);
    }

    @Test
    void startAppointment_booked_setsInCorso() {
        when(appointmentDomainHelper.retrieveOrThrow("APT-1")).thenReturn(entity);
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentMapper.mapToAppointmentResponse(entity)).thenReturn(new AppointmentResponse());

        service.startAppointment(context, "APT-1");

        verify(appointmentValidator).validateTransitionToInCorso(entity);
        assertThat(entity.getStatus()).isEqualTo(AppointmentStatusEnum.IN_CORSO);
    }

    @Test
    void getDailyAppointments_mapsRepoResults() {
        when(appointmentRepository.findDailyAppointments(LocalDate.of(2026, 6, 1), "DOC-1", "CLN-1"))
                .thenReturn(java.util.List.of(entity));
        when(appointmentMapper.mapToAppointmentResponse(entity))
                .thenReturn(new AppointmentResponse().appointmentId("APT-1"));

        var list = service.getDailyAppointments(context, LocalDate.of(2026, 6, 1), "DOC-1", "CLN-1");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getAppointmentId()).isEqualTo("APT-1");
    }
}
