package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.response.calendarevent.ResponseRecommendEvent;
import com.formos.huub.domain.response.funding.ResponseRecommendFunding;
import com.formos.huub.domain.response.learninglibrary.ResponseRecommendCourse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMyPortalForBusinessOwner {

    private ResponseRecommendFeature recommendFeature;

    private List<ResponseNewlyFeature> newlyEvents;

    private List<ResponseNewlyFeature> newlyFunding;

    private List<ResponseNewlyFeature> newlyCourses;
}
