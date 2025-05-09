package com.formos.huub.domain.response.learninglibrary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.formos.huub.domain.enums.SectionTypeEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseLearningLibrarySection {

    private UUID id;

    private SectionTypeEnum sectionType;

    private Integer position;

    @JsonIgnore
    private ResponseSectionContent contents;

    private String heading;

    private String description;

    private String photoCredit;

    private String mediaUrl;

    private String textContent;

    private String surveyContent;

    private List<ResponseSectionFile> sectionFiles;

    public void setContents(ResponseSectionContent contents) {
        this.contents = contents;
        BeanUtils.copyProperties(this.contents, this);
    }
}
