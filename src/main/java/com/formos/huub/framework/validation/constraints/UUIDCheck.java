/**
 * ***************************************************
 * * Description :
 * * File        : UUIDCheck
 * * Author      : Hung Tran
 * * Date        : Dec 24, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.validation.constraints;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.formos.huub.framework.constant.AppConstants;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Pattern(regexp = AppConstants.UUID_V4_REGEX)
@Target({ ElementType.TYPE_USE, FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {})
@ReportAsSingleViolation
public @interface UUIDCheck {
    /** messsage */
    String message() default "{com.formos.huub.validation.uuidCheck}";

    /** */
    Class<?>[] groups() default {};

    /** */
    Class<? extends Payload>[] payload() default {};
}
