package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.enums.ApprovalStatusEnum;
import com.formos.huub.domain.response.answerform.ResponseQuestionForm;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseApplyTechnicalAssistance {

    private UUID portalId;

    private String platformName;

    private String primaryLogo;

    private String primaryColor;

    private String secondaryColor;

    private Integer numStep;

    private ApprovalStatusEnum submitStatus;

    private UUID programTermId;

    private Map<String, List<ResponseQuestionForm>> steps;
}
