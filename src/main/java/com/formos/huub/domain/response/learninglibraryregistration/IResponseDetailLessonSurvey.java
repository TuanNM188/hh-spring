package com.formos.huub.domain.response.learninglibraryregistration;

import com.google.gson.JsonElement;

import java.time.Instant;
import java.util.UUID;

public interface IResponseDetailLessonSurvey {

    UUID getId();

    String getBusinessOwnerName();

    String getPhoneNumber();

    String getEmail();

    String getBusinessName();

    String getCourseName();

    String getLessonName();

    Instant getSubmissionDate();

    JsonElement getContents();

    String getSurveyData();

    UUID getUserId();

}
