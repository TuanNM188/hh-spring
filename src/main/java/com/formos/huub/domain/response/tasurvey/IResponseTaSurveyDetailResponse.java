package com.formos.huub.domain.response.tasurvey;

import java.util.UUID;

public interface IResponseTaSurveyDetailResponse {
    UUID getAppointmentId();
    UUID getAppointmentDetailId();
    String getBusinessOwnerName();
    String getBusinessOwnerEmail();
    String getAdvisorName();
    Integer getRating();
    String getFeedback();
}
