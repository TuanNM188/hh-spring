package com.formos.huub.service.schedule;

import com.formos.huub.domain.entity.Appointment;
import com.formos.huub.domain.enums.AppointmentStatusEnum;
import com.formos.huub.repository.AppointmentRepository;
import com.formos.huub.service.pushnotification.PushNotificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AppointmentScheduleService {

    @Value("${schedule.timezone}")
    private String timeZoneId;

    private ZoneId configuredZone;

    private final AppointmentRepository appointmentRepository;
    private final PushNotificationService pushNotificationService;

    @PostConstruct
    public void init() {
        this.configuredZone = ZoneId.of(timeZoneId);
    }

    // Implement automated pre-appointment notifications to remind both the Business Owner and Technical Advisor of their upcoming scheduled appointment.
    // Notifications should be sent via email and SMS to ensure both parties are prepared in advance.
    // Notifications should be sent at a predefined interval before the appointment (e.g., 24 hours before the appointment time).
    public void jobScheduleReminderPreAppointment() {

        ZonedDateTime pstNow = ZonedDateTime.now(configuredZone);
        Instant start = pstNow.plusDays(1).minusMinutes(30).toInstant();
        Instant end = pstNow.plusDays(1).toInstant();

        List<Appointment> appointments = appointmentRepository.findByStatusAndAppointmentDateBefore(AppointmentStatusEnum.SCHEDULED, start, end);
        try {
            appointments.forEach(appointment -> {
                log.info("Send mail reminder to the {} project is not approved within 3 days", appointment.getId());
                pushNotificationService.buildTemplateReminderPreAppointment(
                    appointment,
                    "email.common.preAppointment.title",
                    false
                );
                pushNotificationService.buildTemplateReminderPreAppointment(
                    appointment,
                    "email.common.preAppointment.title",
                    true
                );
            });
        } catch (Exception e) {
            log.error("Error occurred during schedule Send mail reminder to the {} project is not approved within 3 days process: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute schedule reminder project is not approved within 3 days job", e);
        }
    }
}
