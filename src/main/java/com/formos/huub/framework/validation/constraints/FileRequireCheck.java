/**
 * ***************************************************
 * * Description :
 * * File        : FileRequireCheck
 * * Author      : Hung Tran
 * * Date        : Jan 10, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.validation.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.formos.huub.framework.validation.FileRequireCheckValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ TYPE_USE, FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = FileRequireCheckValidator.class)
public @interface FileRequireCheck {
    /** messsage */
    String message() default "{com.formos.huub.validation.fileRequireCheck}";

    /** */
    Class<?>[] groups() default {};

    /** */
    Class<? extends Payload>[] payload() default {};
}
