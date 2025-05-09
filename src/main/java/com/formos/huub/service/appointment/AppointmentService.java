package com.formos.huub.service.appointment;

import com.formos.huub.domain.constant.ActiveCampaignConstant;
import com.formos.huub.domain.entity.Appointment;
import com.formos.huub.domain.entity.AppointmentDetail;
import com.formos.huub.domain.entity.TechnicalAssistanceSubmit;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.AppointmentStatusEnum;
import com.formos.huub.domain.response.appointment.ResponseAppointment;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.helper.member.MemberHelper;
import com.formos.huub.mapper.appointment.AppointmentMapper;
import com.formos.huub.repository.AppointmentDetailRepository;
import com.formos.huub.repository.AppointmentRepository;
import com.formos.huub.repository.TechnicalAssistanceSubmitRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.activecampaign.ActiveCampaignStrategy;
import com.formos.huub.service.pushnotification.PushNotificationService;
import java.time.Instant;
import java.util.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentService {

    AppointmentRepository appointmentRepository;

    TechnicalAssistanceSubmitRepository technicalAssistanceSubmitRepository;

    AppointmentMapper appointmentMapper;

    UserRepository userRepository;

    PushNotificationService pushNotificationService;

    ActiveCampaignStrategy activeCampaignStrategy;

    MemberHelper memberHelper;

    private static final int CANCEL_TIME_FRAME_HOURS = 24;

    private final AppointmentDetailRepository appointmentDetailRepository;

    private static final Float ZERO = 0.0f;

    /**
     * get appointment entity
     *
     * @param appointmentId UUID
     */
    private Appointment getAppointment(UUID appointmentId) {
        return appointmentRepository
            .findById(appointmentId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "appointment")));
    }

    /**
     * get appointment detail
     *
     * @param appointmentId UUID
     */
    public ResponseAppointment getDetail(UUID appointmentId) {
        var appointment = getAppointment(appointmentId);
        return appointmentMapper.toResponse(appointment);
    }

    /**
     * check Cancellation Allowed hours
     *
     * @param date     date in UTC timezone
     * @param timezone current timezone
     * @param maxHours max Hours
     */
    private void checkCancellationAllowedHours(Instant date, String timezone, int maxHours, String fieldName) {
        if (
            DateUtils.compareToSpecialDateTime(date, timezone) <= DateUtils.hoursToMillis(maxHours) &&
            SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.BUSINESS_OWNER, AuthoritiesConstants.TECHNICAL_ADVISOR)
        ) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0074, String.valueOf(maxHours), fieldName));
        }
    }

    /**
     * check Cancellation Allowed owner
     *
     * @param appointment Appointment
     */
    private void checkCancellationAllowed(Appointment appointment) {
        var user = SecurityUtils.getCurrentUser(userRepository);
        var businessOwner = appointment.getUser();
        var technicalAdvisor = appointment.getTechnicalAdvisor().getUser();
        if (
            !user.getId().equals(businessOwner.getId()) &&
            !user.getId().equals(technicalAdvisor.getId()) &&
            !SecurityUtils.hasCurrentUserAnyOfAuthorities(
                AuthoritiesConstants.SYSTEM_ADMINISTRATOR,
                AuthoritiesConstants.PORTAL_HOST,
                AuthoritiesConstants.COMMUNITY_PARTNER
            )
        ) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0075));
        }
    }

    /**
     * cancel appointment api
     *
     * @param appointmentId UUID
     */
    public ResponseAppointment cancelAppointment(UUID appointmentId) {
        var appointment = getAppointment(appointmentId);
        TechnicalAssistanceSubmit technicalAssistanceSubmit = appointment.getTechnicalAssistanceSubmit();
        checkCancellationAllowed(appointment);
        checkCancellationAllowedHours(appointment.getAppointmentDate(), appointment.getTimezone(), CANCEL_TIME_FRAME_HOURS, "hours");
        appointment.setStatus(AppointmentStatusEnum.CANCELED);
        appointmentRepository.save(appointment);
        updateAppointmentDetail(appointment.getAppointmentDetail(), technicalAssistanceSubmit);
        pushNotificationService.sendCancelAppointmentMailForBusinessOwner(appointment);
        pushNotificationService.sendCancelAppointmentMailApprovedForAdvisor(appointment);
        syncCancelAppointmentToActiveCampaign(technicalAssistanceSubmit.getUser().getId(), appointment.getPortal().getId());
        return appointmentMapper.toResponse(appointment);
    }

    /**
     * Sync FIELD_TA_STATUS_V2, FIELD_SCHEDULED_APPOINTMENT_COUNT_V2 To ActiveCampaign
     *
     * @param userId UUID
     * @param portalId UUID
     */
    private void syncCancelAppointmentToActiveCampaign(UUID userId, UUID portalId) {
        User user = memberHelper.getUserById(userId);
        if (Objects.isNull(user)) {
            return;
        }
        Integer booking = appointmentRepository.countByUserIdAndPortalId(userId, portalId);

        Map<String, String> campaignValueMap = new HashMap<>();
        campaignValueMap.put(ActiveCampaignConstant.FIELD_TA_STATUS_V2, ActiveCampaignConstant.APPROVED);
        campaignValueMap.put(ActiveCampaignConstant.FIELD_SCHEDULED_APPOINTMENT_COUNT_V2, String.valueOf(booking));
        activeCampaignStrategy.syncValueActiveCampaignApplication(user, campaignValueMap);
    }

    private void updateAppointmentDetail(AppointmentDetail appointmentDetail, TechnicalAssistanceSubmit technicalAssistanceSubmit) {
        var timeUseForAppointment = Optional.ofNullable(appointmentDetail.getUseAwardHours()).orElse(ZERO);
        appointmentDetail.setUseAwardHours(ZERO);
        appointmentDetailRepository.save(appointmentDetail);
        updateRemainingHoursApplications(technicalAssistanceSubmit, timeUseForAppointment);
    }

    private void updateRemainingHoursApplications(TechnicalAssistanceSubmit technicalAssistanceSubmit, Float timeUseForAppointment) {
        float remainingHours;
        if (Objects.nonNull(technicalAssistanceSubmit.getRemainingAwardHours())) {
            remainingHours = technicalAssistanceSubmit.getRemainingAwardHours() + timeUseForAppointment;
        } else {
            remainingHours = technicalAssistanceSubmit.getAssignAwardHours();
        }
        technicalAssistanceSubmit.setRemainingAwardHours(remainingHours);
        technicalAssistanceSubmitRepository.save(technicalAssistanceSubmit);
    }
}
