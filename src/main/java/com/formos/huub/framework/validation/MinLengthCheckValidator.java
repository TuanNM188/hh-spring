/**
 * ***************************************************
 * * Description :
 * * File        : MinLengthCheckValidator
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.validation.constraints.MinLengthCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MinLengthCheckValidator extends BaseValidator<MinLengthCheck> implements ConstraintValidator<MinLengthCheck, String> {

    private long min;

    @Override
    public void initialize(final MinLengthCheck annotation) {
        this.min = annotation.min();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        return validationSupport.checkMin(value, min);
    }
}
