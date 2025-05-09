package com.formos.huub.domain.response.termsandconditions;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResponseTermsAndConditions {

    private String markdownText;

    private Instant lastUpdated;

}
