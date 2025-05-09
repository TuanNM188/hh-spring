package com.formos.huub.domain.response.learninglibraryregistration;

import com.formos.huub.domain.enums.AccessTypeEnum;
import com.formos.huub.domain.enums.RegistrationStatusEnum;

import java.time.Instant;
import java.util.UUID;

public interface IResponseDetailRegistration {

    UUID getId();

    String getBusinessOwnerName();

    String getPhoneNumber();

    String getEmail();

    String getBusinessName();

    String getCourseName();

    RegistrationStatusEnum getRegistrationStatus();

    Instant getDecisionDate();

    Integer getEnrolleeLimit();

    Integer getNumberOfRegistered();

    Instant getRegistrationDate();

    Instant getEnrollmentDeadline();

    Instant getStartDate();

    Instant getEndDate();

    String getSurveyJson();

    String getSurveyData();

    Boolean getIsRegistrationFormRequired();

    UUID getUserId();

    AccessTypeEnum getCourseType();

}
