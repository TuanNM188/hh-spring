/**
 * ***************************************************
 * * Description :
 * * File        : LengthCheckValidator
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.validation.constraints.LengthCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LengthCheckValidator extends BaseValidator<LengthCheck> implements ConstraintValidator<LengthCheck, String> {

    private long length;

    @Override
    public void initialize(final LengthCheck constraintAnnotation) {
        this.length = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        boolean result = true;
        if (StringUtils.isEmpty(value)) {
            return validationSupport.checkLength(value, length);
        }

        return result;
    }
}
