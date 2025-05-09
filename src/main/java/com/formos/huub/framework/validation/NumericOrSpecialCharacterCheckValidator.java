/**
 * ***************************************************
 * * Description :
 * * File        : NumericOrSpecialCharacterCheckValidator
 * * Author      : Hung Tran
 * * Date        : Jan 16, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.validation.constraints.NumericOrSpecialCharacterCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NumericOrSpecialCharacterCheckValidator
    extends BaseValidator<NumericOrSpecialCharacterCheck>
    implements ConstraintValidator<NumericOrSpecialCharacterCheck, String> {

    private boolean isFloat;

    private String validSpecialCharacter;

    @Override
    public void initialize(NumericOrSpecialCharacterCheck constraintAnnotation) {
        validSpecialCharacter = constraintAnnotation.allowedSpecialCharacter();
        isFloat = constraintAnnotation.isFloat();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) return true;
        if (StringUtils.isBlank(validSpecialCharacter)) {
            return validationSupport.isNumeric(value, isFloat);
        }
        boolean isNumeric = validationSupport.isNumeric(value, isFloat);
        boolean isMatchSpecialCharacter = StringUtils.compare(value, validSpecialCharacter) == 0;
        return isNumeric || isMatchSpecialCharacter;
    }
}
