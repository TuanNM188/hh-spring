/**
 * ***************************************************
 * * Description :
 * * File        : BoolCheckValidator
 * * Author      : Hung Tran
 * * Date        : Jan 15, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.validation.constraints.BoolCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BoolCheckValidator extends BaseValidator<BoolCheck> implements ConstraintValidator<BoolCheck, String> {

    @Override
    public void initialize(BoolCheck constraintAnnotation) {
        super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) return true;

        return validationSupport.isBooleanSymbol(value);
    }
}
