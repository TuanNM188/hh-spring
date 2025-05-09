/**
 * ***************************************************
 * * Description :
 * * File        : FileRequireCheckValidator
 * * Author      : Hung Tran
 * * Date        : Jan 10, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.validation.constraints.FileRequireCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class FileRequireCheckValidator
    extends BaseValidator<FileRequireCheck>
    implements ConstraintValidator<FileRequireCheck, MultipartFile> {

    @Override
    public void initialize(final FileRequireCheck constraintAnnotation) {}

    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
        return !value.isEmpty();
    }
}
