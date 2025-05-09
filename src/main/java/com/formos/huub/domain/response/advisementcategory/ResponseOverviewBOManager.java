package com.formos.huub.domain.response.advisementcategory;

import com.formos.huub.domain.response.technicaladvisor.IResponseAssignNavigator;
import com.formos.huub.domain.response.technicaladvisor.IResponseRecommendAdvisor;
import com.formos.huub.domain.response.technicalassistance.ResponseTechnicalAssistanceRemainHours;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResponseOverviewBOManager {
    ResponseTechnicalAssistanceRemainHours remainingHour;
    List<IResponseRecommendAdvisor> recommendAdvisors;
    IResponseAssignNavigator assignNavigator;

}
