/**
 * ***************************************************
 * * Description :
 * * File        : NumericOrSpecialCharaterCheck
 * * Author      : Hung Tran
 * * Date        : Jan 16, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.validation.constraints;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.formos.huub.framework.validation.NumericOrSpecialCharacterCheckValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = NumericOrSpecialCharacterCheckValidator.class)
public @interface NumericOrSpecialCharacterCheck {
    /** isFloat */
    boolean isFloat() default true;

    /** allowedSpecialCharacters */
    String allowedSpecialCharacter() default "";

    /** message */
    String message() default "{com.formos.huub.validation.numericOrSpecialCharacterCheck}";

    /** */
    Class<?>[] groups() default {};

    /** */
    Class<? extends Payload>[] payload() default {};
}
