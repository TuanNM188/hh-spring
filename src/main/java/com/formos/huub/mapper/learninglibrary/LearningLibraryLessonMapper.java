package com.formos.huub.mapper.learninglibrary;

import com.formos.huub.domain.entity.LearningLibraryLesson;
import com.formos.huub.domain.request.learninglibrary.RequestLearningLibraryLesson;
import com.formos.huub.domain.response.learninglibrary.ResponseLearningLibraryLesson;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    uses = { LearningLibrarySectionMapper.class },
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LearningLibraryLessonMapper {

    @Mapping(target = "sections", ignore = true)
    LearningLibraryLesson toEntity(RequestLearningLibraryLesson request);

    @Mapping(target = "sections", ignore = true)
    LearningLibraryLesson partialEntity(@MappingTarget LearningLibraryLesson learningLibraryLesson, RequestLearningLibraryLesson request);

    ResponseLearningLibraryLesson toResponse(LearningLibraryLesson learningLibraryLesson);

}
