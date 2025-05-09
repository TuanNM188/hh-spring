/**
 * ***************************************************
 * * Description :
 * * File        : AgeCheck
 * * Author      : Hung Tran
 * * Date        : Feb 15, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.validation.constraints;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.formos.huub.framework.validation.AgeCheckValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = AgeCheckValidator.class)
public @interface AgeCheck {
    /** message */
    String message() default "{com.formos.huub.validation.ageCheck}";

    int min() default 0;

    int max() default 1000;

    /** */
    Class<?>[] groups() default {};

    /** */
    Class<? extends Payload>[] payload() default {};
}
