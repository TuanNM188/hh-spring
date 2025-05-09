/**
 * ***************************************************
 * * Description :
 * * File        : DateCheckValidator
 * * Author      : Hung Tran
 * * Date        : Dec 23, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.validation.constraints.DateCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateCheckValidator extends BaseValidator<DateCheck> implements ConstraintValidator<DateCheck, String> {

    private DateTimeFormat format;

    private boolean strict;

    @Override
    public void initialize(final DateCheck constraintAnnotation) {
        this.format = constraintAnnotation.format();
        this.strict = constraintAnnotation.strict();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean result = true;
        if (!StringUtils.isEmpty(value)) {
            return DateUtils.isDate(value, format, strict);
        }
        return result;
    }
}
