package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.validation.constraints.SSNCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class SSNValidator extends BaseValidator<SSNCheck> implements ConstraintValidator<SSNCheck, String> {

    @Override
    public void initialize(SSNCheck constraintAnnotation) {
        super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) return true;
        return Pattern.compile(AppConstants.SSN_REGEX).matcher(value).matches();
    }
}
