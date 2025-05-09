package com.formos.huub.domain.request.useranswerform;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestAnswerOption {

    public RequestAnswerOption(String answer) {
        this.answer = answer;
    }

    private UUID id;

    private String answer;
}
