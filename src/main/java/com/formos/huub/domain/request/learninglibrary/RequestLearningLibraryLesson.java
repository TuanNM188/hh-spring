package com.formos.huub.domain.request.learninglibrary;

import com.formos.huub.framework.validation.constraints.NumericCheck;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestLearningLibraryLesson {

    @UUIDCheck
    private String id;

    @RequireCheck
    private String title;

    @RequireCheck
    @NumericCheck
    private String priorityOrder;

    @Valid
    private List<RequestLearningLibrarySection> sections;

}
