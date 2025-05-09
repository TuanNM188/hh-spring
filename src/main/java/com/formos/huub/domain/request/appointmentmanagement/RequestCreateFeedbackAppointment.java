/**
 * ***************************************************
 * * Description :
 * * File        : RequestCreateFeedbackAppointment
 * * Author      : Hung Tran
 * * Date        : Jan 20, 2025
 * ***************************************************
 **/
package com.formos.huub.domain.request.appointmentmanagement;

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
public class RequestCreateFeedbackAppointment {

    private UUID appointmentId;

    @RequireCheck
    @NumericCheck
    @Min(1)
    @Max(5)
    private String rating;

    private String feedback;
}
