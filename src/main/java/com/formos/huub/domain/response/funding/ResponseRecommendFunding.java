package com.formos.huub.domain.response.funding;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseRecommendFunding {

    private UUID id;

    private String title;

    private String description;

    private String imageUrl;

    private BigDecimal amount;
}
