package com.formos.huub.domain.response.technicaladvisor;

import com.formos.huub.domain.enums.UserStatusEnum;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ResponseTechnicalAdvisor {

    private UUID id;

    private String normalizedFullName;

    private String email;

    private String organization;

    private String phoneNumber;

    private UserStatusEnum status;

    private Instant startDate;

    private UUID userId;

}
