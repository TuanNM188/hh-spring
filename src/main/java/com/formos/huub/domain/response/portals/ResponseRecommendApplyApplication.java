package com.formos.huub.domain.response.portals;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.formos.huub.domain.enums.ApprovalStatusEnum;
import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseRecommendApplyApplication {

    @JsonProperty("imageUrl")
    private String primaryLogo;

    private String title;

    private String subTitle;

    private ApprovalStatusEnum status;

    private Float assignAwardHours;

    private Float remainingAwardHours;
}
