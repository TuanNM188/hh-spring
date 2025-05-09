/**
 * ***************************************************
 * * Description :
 * * File        : MaxLengthCheck
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.validation.constraints;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.formos.huub.framework.validation.MaxLengthCheckValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = MaxLengthCheckValidator.class)
public @interface MaxLengthCheck {
    /** message */
    String message() default "{com.formos.huub.validation.maxLengthCheck}";

    /**
     * maximum length */

    long max();

    /**
     * group.
     * @return
     */
    Class<?>[] groups() default {};

    /**
     * payload.
     * @return Class<? extends Payload>[]
     */
    Class<? extends Payload>[] payload() default {};
}
