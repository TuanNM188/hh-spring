/**
 * ***************************************************
 * * Description :
 * * File        : MaxLengthCheckValidator
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.validation.constraints.MaxLengthCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxLengthCheckValidator extends BaseValidator<MaxLengthCheck> implements ConstraintValidator<MaxLengthCheck, String> {

    private long max;

    @Override
    public void initialize(MaxLengthCheck annotation) {
        this.max = annotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean result = true;
        if (StringUtils.isEmpty(value)) {
            return validationSupport.checkMax(value, max);
        }

        return result;
    }
}
