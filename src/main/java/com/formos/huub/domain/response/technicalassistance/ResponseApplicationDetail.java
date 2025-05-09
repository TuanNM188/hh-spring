package com.formos.huub.domain.response.technicalassistance;

import com.formos.huub.domain.enums.ApprovalStatusEnum;
import com.formos.huub.domain.response.answerform.ResponseViewAnswer;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseApplicationDetail {

    private UUID id;

    private UUID portalId;

    private ApprovalStatusEnum status;

    private Instant approvalDate;

    private Float assignAwardHours;

    private Float hoursRemaining;

    private String deniedReason;

    private String communityPartnerName;

    private UUID programTermId;

    private ResponseBasicInfoBusinessOwner basicInformation;

    private List<ResponseViewAnswer> technicalAssistance;

    private List<ResponseViewAnswer> businessInformation;

    private List<ResponseViewAnswer> demographics;

    private List<ResponseViewAnswer> businessNeeds;

    private List<ResponseViewAnswer> additionalQuestions;
}
