package com.formos.huub.domain.request.useranswerform;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestAnswerUnsupportedLocation {

    private String stepName;

    private List<Question> questions;

    @Getter
    @Setter
    public static class Question{
        private String questionCode;

        private String answersSelected;
    }

}
