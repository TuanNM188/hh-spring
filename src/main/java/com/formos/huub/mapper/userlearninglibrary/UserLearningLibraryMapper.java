package com.formos.huub.mapper.userlearninglibrary;

import com.formos.huub.domain.entity.LearningLibrary;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.entity.UserLearningLibrary;
import com.formos.huub.domain.enums.LearningStatusEnum;
import com.formos.huub.domain.response.learninglibrary.ResponseBookmarkLearningLibrary;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    builder = @Builder(disableBuilder = true),
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserLearningLibraryMapper {

    @Mapping(target = "id.user", source = "user")
    @Mapping(target = "id.learningLibrary", source = "learningLibrary")
    @Mapping(target = "isBookmark", source = "isBookmark")
    UserLearningLibrary toEntityBookmark(User user, LearningLibrary learningLibrary, Boolean isBookmark);

    @Mapping(target = "learningLibraryId", source = "learningLibraryId")
    @Mapping(target = "isBookmark", source = "request.isBookmark")
    ResponseBookmarkLearningLibrary toResponseBookmark(UserLearningLibrary request, UUID learningLibraryId);

    @Mapping(target = "id.user", source = "user")
    @Mapping(target = "id.learningLibrary", source = "learningLibrary")
    @Mapping(target = "rating", source = "rating")
    UserLearningLibrary toEntityRating(User user, LearningLibrary learningLibrary, Integer rating);

    @Mapping(target = "id.user", source = "user")
    @Mapping(target = "id.learningLibrary", source = "learningLibrary")
    UserLearningLibrary toCompletionStatusEntity(User user, LearningLibrary learningLibrary, LearningStatusEnum completionStatus);

}
