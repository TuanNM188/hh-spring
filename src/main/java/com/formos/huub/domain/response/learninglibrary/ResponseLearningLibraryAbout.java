package com.formos.huub.domain.response.learninglibrary;

import com.formos.huub.domain.enums.AccessTypeEnum;
import com.formos.huub.domain.enums.ContentTypeEnum;
import com.formos.huub.domain.enums.LearningLibraryStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseLearningLibraryAbout {

    private UUID id;

    private String name;

    private ContentTypeEnum contentType;

    private AccessTypeEnum accessType;

    private Integer expiresIn;

    private Instant enrollmentDeadline;

    private Integer enrolleeLimit;

    private Instant startDate;

    private Instant endDate;

    private BigDecimal price;

    private LearningLibraryStatusEnum status;


    private String description;


    private String heroImage;


    private UUID categoryId;

    private List<ResponseLearningLibraryTag> tags;

    private List<UUID> portals;

    private String createdBy;

}
