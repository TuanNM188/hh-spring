package com.formos.huub.domain.request.learninglibrary;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestLearningLibraryAbout {

    @RequireCheck
    private String name;

    @NotNull(groups = FinalCourse.class)
    private String contentType;

    @NotNull(groups = FinalCourse.class)
    private String accessType;

    private Integer expiresIn;

    private Instant enrollmentDeadline;

    private Integer enrolleeLimit;

    @NotNull(groups = FinalCourse.class)
    private Instant startDate;

    private Instant endDate;

    private BigDecimal price;

    @NotNull(groups = FinalCourse.class)
    private String status;


    private String description;


    private String heroImage;


    private UUID categoryId;

    private List<String> tags;

    private List<UUID> portals;
}
