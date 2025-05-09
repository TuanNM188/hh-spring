package com.formos.huub.domain.response.project;


import com.formos.huub.domain.enums.ProjectStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResponseProjectHeader {

    private String advisorId;

    private String advisorAvatar;

    private String advisorFirstName;

    private String advisorLastName;

    private String communityPartnerName;

    private String businessOwnerId;

    private String businessOwnerAvatar;

    private String businessOwnerFirstName;

    private String businessOwnerLastName;

    private String businessName;

    private Integer estimatedHoursNeeded;

    private Instant proposedStartDate;

    private Instant estimatedCompletionDate;

    private String categoryName;

    private Float remainingAwardHours;

    private ProjectStatusEnum status;

}
