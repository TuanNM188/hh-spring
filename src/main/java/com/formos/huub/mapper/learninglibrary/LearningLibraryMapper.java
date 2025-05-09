package com.formos.huub.mapper.learninglibrary;

import com.formos.huub.domain.entity.LearningLibrary;
import com.formos.huub.domain.entity.LearningLibraryStep;
import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.request.learninglibrary.RequestLearningLibraryAbout;
import com.formos.huub.domain.response.learninglibrary.*;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    uses = { LearningLibraryLessonMapper.class },
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LearningLibraryMapper {

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "portals", ignore = true)
    @Mapping(target = "category", ignore = true)
    LearningLibrary toEntity(RequestLearningLibraryAbout request);

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "portals", ignore = true)
    @Mapping(target = "category", ignore = true)
    void partialEntity(@MappingTarget LearningLibrary learningLibrary, RequestLearningLibraryAbout request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "portals", source = "portals")
    ResponseLearningLibraryAbout toResponse(LearningLibrary learningLibrary);

    ResponseLearningLibraryRegistration toResponseRegistration(LearningLibrary learningLibrary);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ResponseOverviewLearningLibrary toResponseOverview(LearningLibrary learningLibrary);

    ResponseDetailLearningLibrary toResponseDetail(LearningLibrary learningLibrary);

    List<ResponseLearningLibraryStep> toResponseSteps(Set<LearningLibraryStep> learningLibrarySteps);

    ResponseLearningLibraryStep toResponseStep(LearningLibraryStep learningLibraryStep);

    default List<UUID> mapPortals(Set<Portal> portals) {
        return portals.stream()
            .map(Portal::getId)
            .collect(Collectors.toList());
    }
}
