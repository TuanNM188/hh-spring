package com.formos.huub.domain.response.answerform;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseAnswer {



    public ResponseAnswer(UUID id, String answer) {
        this.id = id;
        this.answer = answer;
    }

    private UUID id;

    private UUID questionId;

    private String answer;

    private Integer priorityOrder;

    private Boolean isExtra;

    private Boolean isOther;

}
