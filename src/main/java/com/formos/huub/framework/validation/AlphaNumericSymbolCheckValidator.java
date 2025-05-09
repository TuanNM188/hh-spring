/**
 * ***************************************************
 * * Description :
 * * File        : AlphaNumericSymbolCheckValidator
 * * Author      : Hung Tran
 * * Date        : Jan 17, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.validation.constraints.AlphaNumericSymbolCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AlphaNumericSymbolCheckValidator
    extends BaseValidator<AlphaNumericSymbolCheck>
    implements ConstraintValidator<AlphaNumericSymbolCheck, String> {

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        return validationSupport.isAlphaNumericSymbol(value);
    }
}
