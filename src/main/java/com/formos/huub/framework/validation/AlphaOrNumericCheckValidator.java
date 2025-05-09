/**
 * ***************************************************
 * * Description :
 * * File        : AlphaOrNumericCheckValidator
 * * Author      : Hung Tran
 * * Date        : Jan 17, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.validation.constraints.AlphaNumericCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AlphaOrNumericCheckValidator
    extends BaseValidator<AlphaNumericCheck>
    implements ConstraintValidator<AlphaNumericCheck, String> {

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        boolean result = true;
        if (StringUtils.isNotEmpty(value)) {
            result = validationSupport.isAlphaOrNumeric(value);
        }

        return result;
    }
}
