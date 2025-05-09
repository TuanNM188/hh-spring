/**
 * ***************************************************
 * * Description :
 * * File        : EnumCheckValidator
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.enums.CodeEnum;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.validation.constraints.EnumCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumCheckValidator extends BaseValidator<EnumCheck> implements ConstraintValidator<EnumCheck, String> {

    private Class<? extends CodeEnum> enumClass;

    @Override
    public void initialize(final EnumCheck constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean result = true;
        if (StringUtils.isNotBlank(value)) {
            return validationSupport.isValidEnum(enumClass, value);
        }

        return result;
    }
}
