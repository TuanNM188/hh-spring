package com.formos.huub.domain.request.learninglibrary;

import com.formos.huub.domain.enums.SectionTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestLearningLibrarySection {

    private UUID id;

    @NotNull
    private SectionTypeEnum sectionType;

    @NotNull
    private Integer position;

    private String heading;

    private String description;

    private String photoCredit;

    private String mediaUrl;

    private String textContent;

    private String surveyContent;

    private List<RequestSectionFile> sectionFiles;

}
