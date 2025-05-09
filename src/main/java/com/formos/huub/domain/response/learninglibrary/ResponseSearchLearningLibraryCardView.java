package com.formos.huub.domain.response.learninglibrary;

import com.formos.huub.domain.enums.LearningStatusEnum;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSearchLearningLibraryCardView {

    private UUID id;

    private String name;

    private String description;

    private String thumbnail;

    private Integer lessons;

    private LearningStatusEnum completionStatus;

    private Boolean isBookmark;

    private UUID categoryId;

    private String categoryName;

    private String categoryIcon;

    private Integer rating;

    private UUID userCreatedId;

}
