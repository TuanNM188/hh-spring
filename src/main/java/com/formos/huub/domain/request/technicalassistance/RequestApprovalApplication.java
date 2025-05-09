package com.formos.huub.domain.request.technicalassistance;

import com.formos.huub.domain.enums.ApprovalStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestApprovalApplication {

    @NotNull
    private UUID applicationId;

    private Float assignAwardHours;

    private UUID assignVendorId;

    private String reason;

    @NotNull
    private ApprovalStatusEnum status;
}
