/**
 * ***************************************************
 * * Description :
 * * File        : NumericCheckValidator
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.validation.constraints.NumericCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NumericCheckValidator extends BaseValidator<NumericCheck> implements ConstraintValidator<NumericCheck, String> {

    private boolean isFloat;

    @Override
    public void initialize(NumericCheck constraintAnnotation) {
        super.initialize(constraintAnnotation);
        this.isFloat = constraintAnnotation.isFloat();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) return true;
        return validationSupport.isNumeric(value, isFloat);
    }
}
