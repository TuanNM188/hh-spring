package com.formos.huub.mapper.learninglibrary;

import com.formos.huub.domain.entity.LearningLibrarySection;
import com.formos.huub.domain.request.learninglibrary.RequestLearningLibrarySection;
import com.formos.huub.domain.response.learninglibrary.ResponseLearningLibrarySection;
import com.formos.huub.domain.response.learninglibrary.ResponseSectionContent;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LearningLibrarySectionMapper {

    @Mapping(target = "contents" , ignore = true)
    LearningLibrarySection toEntity(RequestLearningLibrarySection requestLearningLibrarySection);

    @Mapping(target = "contents" , ignore = true)
    void partialEntity(@MappingTarget LearningLibrarySection section, RequestLearningLibrarySection requestLearningLibrarySection);

    @Mapping(target = "contents", expression = "java(convertJsonElementToDto(section.getContents()))")
    ResponseLearningLibrarySection toResponse(LearningLibrarySection section);

    default ResponseSectionContent convertJsonElementToDto(JsonElement jsonElement) {
        var gson = new Gson();
        return gson.fromJson(jsonElement, ResponseSectionContent.class);
    }
}
