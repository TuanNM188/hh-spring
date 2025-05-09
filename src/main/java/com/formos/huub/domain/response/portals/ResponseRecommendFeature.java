package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.response.calendarevent.ResponseRecommendEvent;
import com.formos.huub.domain.response.funding.ResponseRecommendFunding;
import com.formos.huub.domain.response.learninglibrary.ResponseRecommendCourse;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseRecommendFeature {

    private ResponseRecommendApplyApplication technicalAssistance;

    private ResponseRecommendFunding fundingFeature;

    private ResponseRecommendEvent eventFeature;

    private ResponseRecommendCourse courseFeature;
}
