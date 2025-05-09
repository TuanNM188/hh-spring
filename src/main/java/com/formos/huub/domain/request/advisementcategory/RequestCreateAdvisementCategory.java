package com.formos.huub.domain.request.advisementcategory;

import com.formos.huub.domain.entity.AdvisementCategory;
import com.formos.huub.domain.entity.User;
import lombok.Builder;
import lombok.Setter;

import java.util.UUID;

@Builder
@Setter
public class RequestCreateAdvisementCategory {
    private UUID categoryId;
    private UUID serviceId;
    private Integer yearsOfExperience;
    private User user;

    public AdvisementCategory toAdvisementCategory() {
        var advisementCategory = AdvisementCategory.builder()
            .categoryId(categoryId)
            .serviceId(serviceId)
            .yearsOfExperience(yearsOfExperience)
            .user(user);

        return advisementCategory.build();
    }
}
