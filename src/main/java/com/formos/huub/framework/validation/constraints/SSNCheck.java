package com.formos.huub.framework.validation.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.formos.huub.framework.validation.SSNValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ TYPE_USE, FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = SSNValidator.class)
public @interface SSNCheck {
    String message() default "{com.formos.huub.validation.ssnCheck}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
