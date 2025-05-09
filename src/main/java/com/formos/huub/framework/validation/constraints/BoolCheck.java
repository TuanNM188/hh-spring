/**
 * ***************************************************
 * * Description :
 * * File        : BoolCheck
 * * Author      : Hung Tran
 * * Date        : Jan 15, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.validation.constraints;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.formos.huub.framework.validation.BoolCheckValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = BoolCheckValidator.class)
public @interface BoolCheck {
    /** message */
    String message() default "{com.formos.huub.validation.boolCheck}";

    /** */
    Class<?>[] groups() default {};

    /** */
    Class<? extends Payload>[] payload() default {};
}
