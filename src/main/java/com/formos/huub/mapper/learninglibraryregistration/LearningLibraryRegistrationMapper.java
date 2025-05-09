package com.formos.huub.mapper.learninglibraryregistration;

import com.formos.huub.domain.response.learninglibraryregistration.IResponseDetailLessonSurvey;
import com.formos.huub.domain.response.learninglibraryregistration.IResponseDetailRegistration;
import com.formos.huub.domain.response.learninglibraryregistration.ResponseDetailLessonSurvey;
import com.formos.huub.domain.response.learninglibraryregistration.ResponseDetailRegistration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LearningLibraryRegistrationMapper {

    ResponseDetailRegistration interfaceToResponseDetailRegistration(IResponseDetailRegistration registration);

    ResponseDetailLessonSurvey interfaceToResponseDetailLessonSurvey(IResponseDetailLessonSurvey lessonSurvey);

}
