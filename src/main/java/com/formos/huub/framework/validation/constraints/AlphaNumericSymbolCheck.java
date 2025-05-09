/**
 * ***************************************************
 * * Description :
 * * File        : AlphaNumericSymbolCheck
 * * Author      : Hung Tran
 * * Date        : Jan 17, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.validation.constraints;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.formos.huub.framework.validation.AlphaNumericSymbolCheckValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = AlphaNumericSymbolCheckValidator.class)
public @interface AlphaNumericSymbolCheck {
    /** message */
    String message() default "{com.formos.huub.validation.alphaNumericSymbolCheck}";

    /** */
    Class<?>[] groups() default {};

    /** */
    Class<? extends Payload>[] payload() default {};
}
