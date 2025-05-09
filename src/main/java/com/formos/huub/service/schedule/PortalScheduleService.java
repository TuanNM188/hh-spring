package com.formos.huub.service.schedule;

import com.formos.huub.domain.entity.Appointment;
import com.formos.huub.domain.entity.ProgramTerm;
import com.formos.huub.domain.enums.AppointmentStatusEnum;
import com.formos.huub.domain.enums.ApprovalStatusEnum;
import com.formos.huub.domain.enums.StatusEnum;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.repository.*;
import com.formos.huub.service.pushnotification.PushNotificationService;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PortalScheduleService {

    private final PortalActivityLogRepository portalActivityLogRepository;

    private final PortalRepository portalRepository;

    private final ProgramTermRepository programTermRepository;

    private final TechnicalAssistanceSubmitRepository technicalAssistanceSubmitRepository;

    private final AppointmentRepository appointmentRepository;

    private final PushNotificationService pushNotificationService;

    @Value("${schedule.timezone}")
    private String timezone;

    @Scheduled(cron = "0 0 1 * * *")
    public void cleanDataPortalActivityLog() {
        portalActivityLogRepository.deleteAllByDate(Instant.now().minus(1, ChronoUnit.DAYS));
    }

    /**
     * execute Update Current Program Term and Status for term
     */
    @Scheduled(cron = "${schedule.update-status-program-term.cron-job}", zone = "${schedule.timezone}")
    public void executeUpdateCurrentProgramTerm() {
        List<UUID> portalIds = portalRepository.getPortalIdHaveProgramTermActive();
        if (ObjectUtils.isEmpty(portalIds)) {
            return;
        }
        Instant currentDate = Instant.now();
        portalIds.forEach(portalId -> updateProgramTermsForPortal(portalId, currentDate));
    }

    @Scheduled(cron = "${schedule.update-status-program-term.cron-job}", zone = "${schedule.timezone}")
    public void updateAppointmentStatuses() {
        List<Appointment> appointments = appointmentRepository.getAllByStatusIn(
            List.of(AppointmentStatusEnum.SCHEDULED, AppointmentStatusEnum.REPORT_REQUIRED, AppointmentStatusEnum.OVERDUE)
        );
        Instant now = Instant.now();
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime currentDateTime = now.atZone(zoneId);

        for (Appointment appointment : appointments) {
            updateStatus(appointment, currentDateTime.toInstant(), zoneId);
        }
    }

    private boolean isInFinalState(Appointment appointment) {
        return List.of(AppointmentStatusEnum.CANCELED, AppointmentStatusEnum.INVOICED, AppointmentStatusEnum.COMPLETED).contains(
            appointment.getStatus()
        );
    }

    private void handleOverdueAppointment(Appointment appointment, long daysAfterAppointment) {
        appointment.setStatus(daysAfterAppointment > 3 ? AppointmentStatusEnum.OVERDUE : AppointmentStatusEnum.REPORT_REQUIRED);
        if (daysAfterAppointment == 1) {
            pushNotificationService.sendPostAppointmentMailForBusinessOwner(appointment);
            pushNotificationService.sendPostAppointmentReportReminderMailForAdvisor(appointment, daysAfterAppointment);
        } else if (daysAfterAppointment <= 30) {
            pushNotificationService.sendPostAppointmentReportReminderMailForAdvisor(appointment, daysAfterAppointment);
        }
    }

    private void updateStatus(Appointment appointment, Instant now, ZoneId zoneId) {
        // If the appointment is already in a final state, no need to update
        if (isInFinalState(appointment)) {
            return;
        }
        // Convert Instant to ZonedDateTime based on the provided time zone
        Instant appointmentInstant = appointment.getAppointmentDate();
        ZonedDateTime appointmentDateTime = appointmentInstant.atZone(zoneId);
        ZonedDateTime nowDateTime = now.atZone(zoneId);
        if (nowDateTime.toLocalDate().isAfter(appointmentDateTime.toLocalDate())) {
            long daysAfterAppointment = ChronoUnit.DAYS.between(appointmentDateTime.toLocalDate(), nowDateTime.toLocalDate());
            handleOverdueAppointment(appointment, daysAfterAppointment);
        } else {
            appointment.setStatus(AppointmentStatusEnum.SCHEDULED);
        }
        appointmentRepository.save(appointment);
    }

    private void updateProgramTermsForPortal(UUID portalId, Instant currentDate) {
        try {
            List<ProgramTerm> programTerms = programTermRepository
                .findAllTermByPortalId(portalId)
                .stream()
                .sorted(Comparator.comparing(ProgramTerm::getStartDate))
                .peek(term -> updateProgramTermStatus(term, currentDate))
                .toList();

            // Find the current program term
            ProgramTerm currentTerm = programTerms.stream().filter(term -> isTermCurrent(term, currentDate)).findFirst().orElse(null);

            // Update each term's isCurrent flag
            for (ProgramTerm term : programTerms) {
                term.setIsCurrent(Objects.nonNull(currentTerm) && term.equals(currentTerm));
            }
            programTermRepository.saveAll(programTerms);
        } catch (Exception e) {
            log.error("UPDATE status Program Term ERROR::{} for portalId:: {}", e.getMessage(), portalId.toString());
        }
    }

    private void updateProgramTermStatus(ProgramTerm term, Instant currentDate) {
        StatusEnum status = currentDate.isBefore(term.getEndDate()) ? StatusEnum.ACTIVE : StatusEnum.ENDED;
        term.setStatus(status);
        if (StatusEnum.ENDED.equals(status)) {
            updateStatusExpiredForApplication(term.getId());
        }
    }

    private void updateStatusExpiredForApplication(UUID programTermId) {
        var technicalAssistanceList = technicalAssistanceSubmitRepository.getAllByProgramTermId(programTermId);
        var technicalAssistanceUpdates = technicalAssistanceList.stream().peek(ele -> ele.setStatus(ApprovalStatusEnum.EXPIRED)).toList();
        technicalAssistanceSubmitRepository.saveAll(technicalAssistanceUpdates);
    }

    private boolean isTermCurrent(ProgramTerm term, Instant currentDate) {
        return term.getStartDate().isBefore(currentDate) && term.getEndDate().isAfter(currentDate);
    }
}
