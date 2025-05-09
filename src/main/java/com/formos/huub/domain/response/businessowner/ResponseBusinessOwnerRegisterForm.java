package com.formos.huub.domain.response.businessowner;

import com.formos.huub.domain.response.answerform.ResponseQuestionForm;
import jakarta.persistence.Column;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBusinessOwnerRegisterForm {

    private UUID portalId;

    private String platformName;

    private String primaryLogo;

    private String primaryColor;

    private String secondaryColor;

    private Integer numStep;

    private Map<String, List<ResponseQuestionForm>> steps;
}
