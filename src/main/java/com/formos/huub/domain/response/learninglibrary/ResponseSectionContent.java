package com.formos.huub.domain.response.learninglibrary;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseSectionContent {

    private String heading;

    private String imagePath;

    private String description;

    private String photoCredit;

    private String mediaUrl;

    private String textContent;

    private String surveyContent;

    private List<ResponseSectionFile> sectionFiles;
}
