/**
 * ***************************************************
 * * Description :
 * * File        : RequireCheckValidator
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RequireCheckValidator extends BaseValidator<RequireCheck> implements ConstraintValidator<RequireCheck, String> {

    @Override
    public void initialize(final RequireCheck constraintAnnotation) {}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !validationSupport.isBlank(value);
    }
}
