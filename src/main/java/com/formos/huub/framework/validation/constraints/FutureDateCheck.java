package com.formos.huub.framework.validation.constraints;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.formos.huub.framework.validation.FutureDateCheckValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = FutureDateCheckValidator.class)
public @interface FutureDateCheck {
    /** message */
    String message() default "{com.formos.huub.validation.futureDateCheck}";

    /** */
    Class<?>[] groups() default {};

    /** */
    Class<? extends Payload>[] payload() default {};
}
