/**
 * ***************************************************
 * * Description :
 * * File        : RequestCreateFeedbackAppointment
 * * Author      : Hung Tran
 * * Date        : Jan 20, 2025
 * ***************************************************
 **/
package com.formos.huub.domain.request.project;

import com.formos.huub.framework.validation.constraints.NumericCheck;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCreateFeedbackProject {

    private UUID projectId;

    @RequireCheck
    @NumericCheck
    @Min(1)
    @Max(5)
    private String rating;

    private boolean workAsExpected;

    private String feedback;
}
