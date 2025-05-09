/**
 * ***************************************************
 * * Description :
 * * File        : AgeCheckValidator
 * * Author      : Hung Tran
 * * Date        : Feb 15, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.validation.constraints.AgeCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AgeCheckValidator extends BaseValidator<AgeCheck> implements ConstraintValidator<AgeCheck, String> {

    private int minAge;
    private int maxAge;

    @Override
    public void initialize(AgeCheck constraintAnnotation) {
        this.minAge = constraintAnnotation.min();
        this.maxAge = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return validationSupport.checkAge(value, this.minAge, this.maxAge);
    }
}
