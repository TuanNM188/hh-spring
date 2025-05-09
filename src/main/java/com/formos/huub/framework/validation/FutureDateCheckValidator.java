package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.validation.constraints.FutureDateCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FutureDateCheckValidator extends BaseValidator<FutureDateCheck> implements ConstraintValidator<FutureDateCheck, String> {

    @Override
    public void initialize(final FutureDateCheck constraintAnnotation) {}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean result = true;
        if (!StringUtils.isEmpty(value)) {
            return validationSupport.isFutureInstant(value);
        }
        return result;
    }
}
