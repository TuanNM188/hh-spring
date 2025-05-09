package com.formos.huub.domain.response.advisementcategory;

import com.formos.huub.domain.enums.ApprovalStatusEnum;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCountAdvisorUsing {

    private Map<String, Integer> usingCategories;

    private Map<String, Integer> usingServices;

    private Map<String, Integer> usingSpecialties;

    private Map<String, Integer> usingAreas;

    private Map<String, Integer> usingCommunityPartners;

    private Map<String, Integer> usingLanguages;

    private ApprovalStatusEnum applicationStatus;

    private UUID assignVendorId;


}
