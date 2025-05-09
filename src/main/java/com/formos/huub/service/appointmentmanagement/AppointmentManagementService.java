/**
 * ***************************************************
 * * Description :
 * * File        : AppointmentService
 * * Author      : Hung Tran
 * * Date        : Jan 20, 2025
 * ***************************************************
 **/
package com.formos.huub.service.appointmentmanagement;

import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.entity.Appointment;
import com.formos.huub.domain.entity.CommunityPartner;
import com.formos.huub.domain.entity.TechnicalAdvisor;
import com.formos.huub.domain.enums.AppointmentStatusEnum;
import com.formos.huub.domain.request.appointmentmanagement.RequestCreateFeedbackAppointment;
import com.formos.huub.domain.request.appointmentmanagement.RequestSearchAppointment;
import com.formos.huub.domain.request.appointmentmanagement.RequestUpdateAppointmentDetail;
import com.formos.huub.domain.response.appointment.IResponseHeaderAppointmentDetail;
import com.formos.huub.domain.response.appointment.ResponseAppointmentReportDetail;
import com.formos.huub.domain.response.appointment.ResponseDetailAppointmentDetail;
import com.formos.huub.domain.response.technicaladvisor.ResponseBookingAppointmentForm;
import com.formos.huub.domain.response.technicalassistance.ResponseOverviewAppointmentOfTerm;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.mapper.appointment.AppointmentDetailMapper;
import com.formos.huub.mapper.appointment.AppointmentMapper;
import com.formos.huub.mapper.bookingsetting.BookingSettingMapper;
import com.formos.huub.repository.AppointmentDetailRepository;
import com.formos.huub.repository.AppointmentRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.applicationmanagement.ApplicationManagementService;
import com.formos.huub.service.portals.PortalFormService;
import com.formos.huub.service.pushnotification.PushNotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentManagementService {

    AppointmentRepository appointmentRepository;
    AppointmentDetailRepository appointmentDetailRepository;
    UserRepository userRepository;
    PortalFormService portalFormService;
    AppointmentMapper appointmentMapper;
    BookingSettingMapper bookingSettingMapper;
    AppointmentDetailMapper appointmentDetailMapper;
    PushNotificationService pushNotificationService;
    ApplicationManagementService applicationManagementService;

    private static final int TWO_HOURS = 2;
    private static final int HOURS_OF_DAY = 24;

    /**
     * search Appointments by term and condition
     *
     * @param request RequestSearchAppointment
     * @return Map<String, Object> appointment
     */
    public Map<String, Object> searchAppointments(RequestSearchAppointment request) {
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "a.appointment_date,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));

        if (Objects.isNull(request.getTimezone())) {
            request.setTimezone("America/Los_Angeles");
        }
        request.setPortalIds(applicationManagementService.getListPortalByRole(request.getPortalId()));
        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put("appointmentDate", "a.appointment_date");
        sortMap.put("businessOwnerName", "u.normalized_full_name");
        sortMap.put("advisorName", "tau.normalized_full_name");
        sortMap.put("navigatorName", "una.normalized_full_name");
        sortMap.put("vendorName", "cp.name");
        sortMap.put("categoryName", "c.name");
        sortMap.put("serviceName", "so.name");
        sortMap.put("status", "a.status");
        sortMap.put(BusinessConstant.TIMEZONE_KEY, request.getTimezone());
        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap));

        if (!ObjectUtils.isEmpty(request.getStatus())) {
            var status = Arrays.stream(request.getStatus().split(",")).toList();
            request.setAppointmentStatus(status);
        }
        if (Objects.nonNull(request.getStartDate())) {
            request.setStartDate(DateUtils.convertTimeToUtcToString(request.getStartDate(), request.getTimezone()));
        }
        if (Objects.nonNull(request.getEndDate())) {
            request.setEndDate(DateUtils.convertTimeToUtcToString(request.getEndDate(), request.getTimezone()));
        }
        if (Objects.isNull(request.getIsCurrent())) {
            request.setIsCurrent(true);
        }
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.PORTAL_HOST)) {
            request.setEndDate(null);
            request.setTimezone(null);
        }
        request.setCommunityPartnerId(getCurrentCommunityPartnerId());
        request.setTechnicalAdvisorId(getCurrentTechnicalAdvisorId());
        var data = appointmentRepository.searchByTermAndCondition(request, pageable);
        return PageUtils.toPage(data);
    }

    /**
     * get Overview Applications By Term
     *
     * @param portalId  UUID
     * @param startDate String
     * @param endDate   String
     * @return ResponseOverviewAppointmentOfTerm
     */
    public ResponseOverviewAppointmentOfTerm getOverviewApplicationsByTerm(UUID portalId, String startDate, String endDate, String timezone) {
        var portalIds = applicationManagementService.getListPortalByRole(portalId);
        var response = new ResponseOverviewAppointmentOfTerm();
        var communityPartnerId = getCurrentCommunityPartnerId();
        var technicalAdvisorId = getCurrentTechnicalAdvisorId();
        var numByStatus = appointmentRepository.getOverviewAppointmentOfTerm(portalIds, startDate,
            endDate, communityPartnerId, technicalAdvisorId, timezone);
        BeanUtils.copyProperties(numByStatus, response);
        if (Objects.nonNull(portalId)) {
            Optional.ofNullable(portalFormService.getCurrentTerm(portalId)).ifPresent(term -> {
                response.setProgramTermId(term.getId());
                response.setStartDate(term.getStartDate());
                response.setEndDate(term.getEndDate());
            });
        }
        return response;
    }

    public IResponseHeaderAppointmentDetail getHeaderAppointmentDetail(UUID appointmentId) {
        return appointmentRepository.getHeaderAppointmentDetailById(appointmentId)
            .orElse(null);
    }

    public ResponseDetailAppointmentDetail getAppointmentDetail(UUID appointmentId) {
        var appointmentDetail = appointmentRepository.getAppointmentDetailById(appointmentId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Appointment Detail")));

        var result = appointmentDetailMapper.toResponse(appointmentDetail);

        var appointmentDateTime = appointmentDetail.getAppointmentDate();
        var currentTime = Instant.now();

        result.setIsDisplaySubmitReportButton(isDisplaySubmitReportButton(currentTime, appointmentDateTime, appointmentDetail.getStatus()));
        result.setIsDisplayRescheduleButton(false);
        result.setIsDisplayCancelButton(false);
        result.setAppointmentReport(getAppointmentReportDetail(appointmentId));
        if (Objects.nonNull(appointmentDetail.getStatus()) && AppointmentStatusEnum.SCHEDULED.equals(appointmentDetail.getStatus())) {
            result.setIsDisplayRescheduleButton(isDisplayRescheduleButton(currentTime, appointmentDateTime));
            if (SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.BUSINESS_OWNER, AuthoritiesConstants.TECHNICAL_ADVISOR)) {
                result.setIsDisplayCancelButton(isDisplayCancelButton(currentTime, appointmentDateTime));
            } else {
                result.setIsDisplayCancelButton(isDisplayCancelButtonForOtherRole(currentTime, appointmentDateTime));
            }
        }

        return result;
    }

    public boolean hasAdvisorAccessToAppointment(UUID appointmentId) {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var appointment = getAppointment(appointmentId);
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.TECHNICAL_ADVISOR)) {
            return Objects.nonNull(currentUser.getTechnicalAdvisor()) &&
                   Objects.nonNull(appointment.getTechnicalAdvisor()) &&
                   currentUser.getTechnicalAdvisor().getId().equals(appointment.getTechnicalAdvisor().getId());
        }
        return false;
    }

    /**
     * Get Appointment Report Detail
     *
     * @param appointmentId UUID
     * @return ResponseAppointmentReportDetail
     */
    public ResponseAppointmentReportDetail getAppointmentReportDetail(UUID appointmentId) {
        return appointmentRepository.getAppointmentReportDetailById(appointmentId)
            .map(appointmentDetailMapper::toResponse)
            .orElse(null);
    }

    public void updateAppointmentDetail(UUID appointmentId, RequestUpdateAppointmentDetail request) {
        var appointment = getAppointment(appointmentId);
        var appointmentDetail = appointment.getAppointmentDetail();
        appointmentDetailMapper.partialUpdate(appointmentDetail, request);
        appointmentDetailRepository.save(appointmentDetail);
        pushNotificationService.sendUpdateAppointmentMailForBusinessOwner(appointment);
        pushNotificationService.sendUpdateAppointmentMailApprovedForAdvisor(appointment);
    }

    public ResponseBookingAppointmentForm getAdvisorBookingSettingToReschedule(UUID appointmentId) {
        var appointment = getAppointment(appointmentId);
        var technicalAdvisor = appointment.getTechnicalAdvisor();
        var bookingSetting = technicalAdvisor.getUser().getBookingSetting();
        if (Objects.isNull(bookingSetting)) {
            return null;
        }
        var appointmentBooked = appointmentRepository.getAppointmentAdvisor(Instant.now(), technicalAdvisor.getId()).stream()
            .map(appointmentMapper::toResponseAppointmentBooked).toList();
        var response = bookingSettingMapper.toResponseSetting(bookingSetting);
        response.setAppointmentBooked(appointmentBooked);
        return response;
    }

    public void createFeedback(RequestCreateFeedbackAppointment request) {
        // Validate request
        if (
            request == null ||
                request.getAppointmentId() == null ||
                StringUtils.isBlank(request.getRating())
        ) {
            throw new IllegalArgumentException("Invalid feedback request");
        }

        // Get current user and validate
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        if (Objects.isNull(currentUser)) {
            throw new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "User"));
        }

        // Get appointment and validate
        // Check if appointment exist for current user (user make appointment, Business Owner)
        var appointment = appointmentRepository
            .findByIdAndUserId(request.getAppointmentId(), currentUser.getId())
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Appointment")));

        try {
            // Update appointment feedback
            var appointmentDetail = appointment.getAppointmentDetail();
            appointmentDetail.setFeedback(request.getFeedback());
            appointmentDetail.setRating(Math.round(Float.parseFloat(request.getRating())));
            appointmentRepository.save(appointment);
            pushNotificationService.sendNotifyWhenBOCompleteSurveyPostAppointment(appointment);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid rating format");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to update appointment feedback", e);
        }
    }

    public boolean existsByAppointmentId(UUID appointmentId) {
        return appointmentRepository.existsById(appointmentId);
    }

    public boolean checkExistingAppointmentWithUserId(UUID appointmentId) {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        if (Objects.isNull(currentUser)) {
            throw new AccessDeniedException("Role Business Owner");
        }

        var appointment = appointmentRepository
            .findByIdAndUserId(appointmentId, currentUser.getId())
            .orElseThrow(() -> new AccessDeniedException("Role Business Owner"));
        return ObjectUtils.isEmpty(appointment);
    }

    private UUID getCurrentCommunityPartnerId() {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.COMMUNITY_PARTNER)) {
            return null;
        }
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        return Optional.ofNullable(currentUser.getCommunityPartner()).map(CommunityPartner::getId).orElse(null);
    }

    private UUID getCurrentTechnicalAdvisorId() {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.TECHNICAL_ADVISOR)) {
            return null;
        }
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        return Optional.ofNullable(currentUser.getTechnicalAdvisor()).map(TechnicalAdvisor::getId).orElse(null);
    }

    private Boolean isDisplaySubmitReportButton(Instant currentTime, Instant appointmentDateTime, AppointmentStatusEnum status) {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.TECHNICAL_ADVISOR)) {
            return false;
        }

        List<AppointmentStatusEnum> closedStatuses = List.of(AppointmentStatusEnum.CANCELED);

        if (closedStatuses.contains(status)) {
            return false;
        }

        return !currentTime.isBefore(appointmentDateTime);

    }

    private Boolean isDisplayRescheduleButton(Instant currentTime, Instant appointmentDateTime) {
        return currentTime.isBefore(appointmentDateTime.minusSeconds(TWO_HOURS * AppConstants.SECONDS_OF_A_HOUR));

    }

    private Boolean isDisplayCancelButton(Instant currentTime, Instant appointmentDateTime) {
        return currentTime.isBefore(appointmentDateTime.minusSeconds(HOURS_OF_DAY * AppConstants.SECONDS_OF_A_HOUR));
    }

    private Boolean isDisplayCancelButtonForOtherRole(Instant currentTime, Instant appointmentDateTime) {
        return currentTime.isAfter(appointmentDateTime.minusSeconds(HOURS_OF_DAY * AppConstants.SECONDS_OF_A_HOUR))
            && currentTime.isBefore(appointmentDateTime);
    }

    private Appointment getAppointment(UUID id) {
        return appointmentRepository.findById(id)
            .orElseThrow(() -> new BadRequestException(
                MessageHelper.getMessage(Message.Keys.E0010, "Appointment")));
    }
}
