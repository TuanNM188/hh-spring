package com.formos.huub.domain.response.funding;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseFundingCategory {

    private UUID categoryId;

    private String categoryName;
}
