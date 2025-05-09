package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.enums.ApprovalStatusEnum;
import com.formos.huub.domain.response.answerform.ResponseUserAnswerForm;
import com.formos.huub.domain.response.communitypartner.ResponseCommunityPartner;
import com.formos.huub.domain.response.technicaladvisor.ResponseBasicTechnicalAdvisor;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseApply11SupportScreen {

    private List<ResponseBasicTechnicalAdvisor> technicalAdvisors;

    private List<ResponseCommunityPartner> communityPartners;

    private List<String> topics;

    private List<ResponseUserAnswerForm> aboutScreenConfigurations;

    private String primaryContactName;

    private String primaryContactEmail;

    private String primaryContactPhone;

    private ApprovalStatusEnum submitStatus;

    private UUID programTermId;
}
