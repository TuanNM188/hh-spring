package com.formos.huub.framework.validation.constraints;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.validation.PastDateCheckValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = PastDateCheckValidator.class)
public @interface PastDateCheck {
    /** message */
    String message() default "{com.formos.huub.validation.pastDateCheck}";

    /** */
    Class<?>[] groups() default {};

    /** */
    Class<? extends Payload>[] payload() default {};

    /** format date */
    DateTimeFormat format() default DateTimeFormat.YYYY_MM_DD;

    /** verify specify date */
    boolean strict() default false;
}
