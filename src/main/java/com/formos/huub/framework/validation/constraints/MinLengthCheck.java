/**
 * ***************************************************
 * * Description :
 * * File        : MinLengthCheck
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.validation.constraints;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.formos.huub.framework.validation.MinLengthCheckValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = MinLengthCheckValidator.class)
public @interface MinLengthCheck {
    /** message */
    String message() default "{com.formos.huub.validation.minLengthCheck}";

    /** minimum length */
    long min();

    /** */
    Class<?>[] groups() default {};

    /** */
    Class<? extends Payload>[] payload() default {};
}
