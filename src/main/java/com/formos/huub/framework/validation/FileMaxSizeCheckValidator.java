/**
 * ***************************************************
 * * Description :
 * * File        : FileMaxSizeCheckValidator
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.validation;

import com.formos.huub.framework.base.BaseValidator;
import com.formos.huub.framework.service.storage.model.FileProperties;
import com.formos.huub.framework.utils.FileUtils;
import com.formos.huub.framework.validation.constraints.FileMaxSizeCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class FileMaxSizeCheckValidator
    extends BaseValidator<FileMaxSizeCheck>
    implements ConstraintValidator<FileMaxSizeCheck, MultipartFile> {

    private long max;

    private boolean isAvatar;
    private final FileProperties fileProperties;

    public FileMaxSizeCheckValidator(FileProperties fileProperties) {
        this.fileProperties = fileProperties;
    }

    @Override
    public void initialize(FileMaxSizeCheck annotation) {
        this.isAvatar = annotation.isAvatar();
        if (annotation.max() > 0) {
            this.max = annotation.max();
            return;
        }
        max = isAvatar ? fileProperties.getAvatarMaxSize() : fileProperties.getMaxSize();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        return !FileUtils.checkSize(max, file.getSize());
    }
}
