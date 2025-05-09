/**
 * ***************************************************
 * * Description :
 * * File        : FileMaxSizeCheck
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.validation.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.formos.huub.framework.validation.FileMaxSizeCheckValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ FIELD, PARAMETER, METHOD })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = FileMaxSizeCheckValidator.class)
public @interface FileMaxSizeCheck {
    /** message */
    String message() default "{com.formos.huub.validation.fileMaxSizeCheck}";

    /**
     * maximum length */

    long max() default 0;

    boolean isAvatar() default false;

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
