package com.formos.huub.domain.request.outsidereport;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCreateOutsideReport {

    @UUIDCheck
    private String portalId;

    @Min(1)
    @Max(12)
    private Integer month;

    private Integer year;

    @RequireCheck
    private String summary;

    @RequireCheck
    private String pdfUrl;
}
