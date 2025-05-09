package com.formos.huub.domain.response.technicalassistance;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseTechnicalAssistanceRemainHours {

    private Float assignAwardHours;

    private Float remainingAwardHours;
}
