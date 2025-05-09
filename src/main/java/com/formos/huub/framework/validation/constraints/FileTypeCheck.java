/**
 * ***************************************************
 * * Description :
 * * File        : FileTypeCheck
 * * Author      : Hung Tran
 * * Date        : Jan 11, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.validation.constraints;

import static com.formos.huub.framework.enums.FileTypeEnum.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.formos.huub.framework.enums.FileTypeEnum;
import com.formos.huub.framework.validation.FileTypeCheckValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ TYPE_USE, FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = FileTypeCheckValidator.class)
public @interface FileTypeCheck {
    FileTypeEnum[] allowedFileTypes() default {
        PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, PNG, JPEG, JPG, BMP, MP4, SRT, VTT, HEIF, HEIC, WEBP,
    };

    /** messsage */
    String message() default "{com.formos.huub.validation.fileTypeCheck}";

    /** */
    Class<?>[] groups() default {};

    /** */
    Class<? extends Payload>[] payload() default {};
}
