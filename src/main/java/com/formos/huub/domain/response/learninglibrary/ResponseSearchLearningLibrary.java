package com.formos.huub.domain.response.learninglibrary;

import com.formos.huub.domain.enums.AccessTypeEnum;
import com.formos.huub.domain.enums.ContentTypeEnum;
import com.formos.huub.domain.enums.LearningLibraryStatusEnum;
import lombok.*;

import java.util.UUID;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSearchLearningLibrary {

    private UUID id;

    private String name;

    private ContentTypeEnum contentType;

    private AccessTypeEnum accessType;

    private Integer numSteps;

    private LearningLibraryStatusEnum status;

    private UUID userCreatedId;
}
