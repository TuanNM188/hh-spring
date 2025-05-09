/**
 * ***************************************************
 * * Description :
 * * File        : FileTypeCheckValidator
 * * Author      : Hung Tran
 * * Date        : Jan 11, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.utils.FileUtils;
import com.formos.huub.framework.validation.constraints.FileTypeCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class FileTypeCheckValidator extends BaseValidator<FileTypeCheck> implements ConstraintValidator<FileTypeCheck, MultipartFile> {

    private List<String> validFileTypes;

    @Override
    public void initialize(FileTypeCheck fileType) {
        validFileTypes = new ArrayList<>();
        Arrays.stream(fileType.allowedFileTypes()).forEach(file_type -> validFileTypes.add(file_type.getValue()));
    }

    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
        String extension = FileUtils.getExtensionName(value.getOriginalFilename()).toUpperCase();
        return validFileTypes.contains(extension);
    }
}
