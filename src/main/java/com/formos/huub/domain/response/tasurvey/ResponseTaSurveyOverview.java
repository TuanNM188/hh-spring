package com.formos.huub.domain.response.tasurvey;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseTaSurveyOverview {
    private Float averageAdvisor;
    private Float averageProject;
    private Float averageTapSurvey;
}
