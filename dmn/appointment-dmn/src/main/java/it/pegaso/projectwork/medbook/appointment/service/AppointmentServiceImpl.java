package it.pegaso.projectwork.medbook.appointment.service;

import it.pegaso.projectwork.medbook.appointment.helper.AppointmentDomainHelper;
import it.pegaso.projectwork.medbook.appointment.kafka.AppointmentEventPublisher;
import it.pegaso.projectwork.medbook.appointment.mapper.AppointmentMapper;
import it.pegaso.projectwork.medbook.appointment.model.entity.AppointmentEntity;
import it.pegaso.projectwork.medbook.appointment.model.enums.AppointmentStatusEnum;
import it.pegaso.projectwork.medbook.appointment.model.enums.CancelledByEnum;
import it.pegaso.projectwork.medbook.appointment.repository.AppointmentRepository;
import it.pegaso.projectwork.medbook.appointment.server.model.*;
import it.pegaso.projectwork.medbook.appointment.validator.AppointmentValidator;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementazione del service per la gestione delle prenotazioni.
 * La creazione è atomica: verifica unicità slot + crea appointment in un'unica transazione.
 * Il UNIQUE constraint DB gestisce le race condition.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentDomainHelper appointmentDomainHelper;
    private final AppointmentValidator appointmentValidator;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentEventPublisher eventPublisher;

    @Override
    @Transactional
    public AppointmentResponse bookAppointment(MedBookContext context, BookAppointmentRequest request) {
        // Verifica vincoli di prenotazione (duplicati specializzazione, medico/data, slot, max attivi)
        appointmentValidator.validateBookingConstraints(request);

        // Verifica applicativa pre-scrittura (safety net: il UNIQUE constraint DB gestisce le race condition)
        appointmentValidator.validateSlotAvailableForBooking(
                request.getDoctorId(), request.getSlotDate(), appointmentMapper.mapStringToLocalTime(request.getStartTime()));

        AppointmentEntity appointment = appointmentMapper.mapToAppointmentEntity(request);
        appointment.setAppointmentId(appointmentDomainHelper.generateAppointmentId());
        appointment.setStatus(AppointmentStatusEnum.PRENOTATO);
        appointment.setBookingDate(LocalDateTime.now());

        AppointmentEntity saved = appointmentRepository.save(appointment);

        // Pubblica evento Kafka (best-effort — non blocca la transazione)
        try {
            eventPublisher.publishAppointmentBooked(saved, request);
        } catch (Exception e) {
            log.warn("Errore pubblicazione AppointmentBookedEvent per appointmentId={}: {}",
                    saved.getAppointmentId(), e.getMessage());
        }

        return appointmentMapper.mapToAppointmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentListOutput getAllAppointments(MedBookContext context,
                                                    String patientId, String doctorId,
                                                    String clinicId, AppointmentStatusApiEnum status,
                                                    LocalDate dateFrom, LocalDate dateTo,
                                                    Integer page, Integer size, String sort) {
        AppointmentStatusEnum domainStatus = Optional.ofNullable(status)
                .map(s -> AppointmentStatusEnum.valueOf(s.name()))
                .orElse(null);

        Pageable pageable = appointmentMapper.mapToPageable(page, size, sort);
        Page<AppointmentEntity> pageResult = appointmentRepository.getAllAppointmentsWithFilters(
                patientId, doctorId, clinicId, domainStatus, dateFrom, dateTo, pageable);

        return appointmentMapper.mapToAppointmentListOutput(pageResult);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(MedBookContext context, String appointmentId) {
        AppointmentEntity appointment = appointmentDomainHelper.retrieveOrThrow(appointmentId);
        return appointmentMapper.mapToAppointmentResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(MedBookContext context, String appointmentId,
                                                  CancelAppointmentRequest request) {
        AppointmentEntity appointment = appointmentDomainHelper.retrieveOrThrow(appointmentId);
        appointmentValidator.validateCancelAppointmentRequest(appointment, request);

        appointment.setStatus(AppointmentStatusEnum.CANCELLATO);
        appointment.setCancellationReason(request.getCancellationReason());
        appointment.setCancelledBy(
                request.getCancelledBy() != null
                        ? CancelledByEnum.valueOf(request.getCancelledBy().name())
                        : null);

        AppointmentEntity saved = appointmentRepository.save(appointment);

        // Pubblica evento Kafka (best-effort)
        try {
            eventPublisher.publishAppointmentCancelled(saved, request);
        } catch (Exception e) {
            log.warn("Errore pubblicazione AppointmentCancelledEvent per appointmentId={}: {}",
                    saved.getAppointmentId(), e.getMessage());
        }

        return appointmentMapper.mapToAppointmentResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse restoreAppointment(MedBookContext context, String appointmentId) {
        AppointmentEntity appointment = appointmentDomainHelper.retrieveOrThrow(appointmentId);
        appointmentValidator.validateRestoreAppointment(
                appointment, appointment.getDoctorId(), appointment.getSlotDate(), appointment.getStartTime());

        appointment.setStatus(AppointmentStatusEnum.PRENOTATO);
        appointment.setCancellationReason(null);
        appointment.setCancelledBy(null);

        AppointmentEntity saved = appointmentRepository.save(appointment);
        return appointmentMapper.mapToAppointmentResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse completeAppointment(MedBookContext context, String appointmentId) {
        AppointmentEntity appointment = appointmentDomainHelper.retrieveOrThrow(appointmentId);
        appointmentValidator.validateTransitionFromInCorso(appointment);
        appointment.setStatus(AppointmentStatusEnum.COMPLETATO);
        AppointmentEntity saved = appointmentRepository.save(appointment);
        return appointmentMapper.mapToAppointmentResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse noShowAppointment(MedBookContext context, String appointmentId) {
        AppointmentEntity appointment = appointmentDomainHelper.retrieveOrThrow(appointmentId);
        appointmentValidator.validateTransitionFromBooked(appointment, "NON_PRESENTATO");
        appointment.setStatus(AppointmentStatusEnum.NON_PRESENTATO);
        AppointmentEntity saved = appointmentRepository.save(appointment);
        return appointmentMapper.mapToAppointmentResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse startAppointment(MedBookContext context, String appointmentId) {
        AppointmentEntity appointment = appointmentDomainHelper.retrieveOrThrow(appointmentId);
        appointmentValidator.validateTransitionToInCorso(appointment);
        appointment.setStatus(AppointmentStatusEnum.IN_CORSO);
        AppointmentEntity saved = appointmentRepository.save(appointment);
        return appointmentMapper.mapToAppointmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDailyAppointments(MedBookContext context,
                                                           LocalDate date, String doctorId, String clinicId) {
        return appointmentRepository.findDailyAppointments(date, doctorId, clinicId)
                .stream()
                .map(appointmentMapper::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }
}
