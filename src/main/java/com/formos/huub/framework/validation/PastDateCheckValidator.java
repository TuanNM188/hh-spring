package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.validation.constraints.PastDateCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PastDateCheckValidator extends BaseValidator<PastDateCheck> implements ConstraintValidator<PastDateCheck, String> {

    private DateTimeFormat format;

    private boolean strict;

    @Override
    public void initialize(final PastDateCheck constraintAnnotation) {
        this.format = constraintAnnotation.format();
        this.strict = constraintAnnotation.strict();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean result = true;
        if (!StringUtils.isEmpty(value)) {
            if (DateUtils.isDate(value, this.format, this.strict)) {
                return validationSupport.isPastInstant(value);
            }
        }
        return result;
    }
}
