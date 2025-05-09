package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.enums.ApprovalStatusEnum;
import com.formos.huub.domain.enums.StatusEnum;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseProgramTermInfo {

    private UUID id;

    private String name;

    private Instant startDate;

    private Instant endDate;

    private StatusEnum status;

    private Boolean isCurrent;
}
