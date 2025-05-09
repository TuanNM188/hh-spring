package com.formos.huub.mapper.learninglibrary;

import com.formos.huub.domain.entity.LearningLibraryStep;
import com.formos.huub.domain.request.learninglibrary.RequestLearningLibraryStep;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LearningLibraryStepMapper {

    @Mapping(target = "learningLibraryLessons", ignore = true)
    LearningLibraryStep toEntity(RequestLearningLibraryStep request);

    @Mapping(target = "learningLibraryLessons", ignore = true)
    LearningLibraryStep partialEntity(@MappingTarget LearningLibraryStep learningLibraryStep, RequestLearningLibraryStep request);

}
