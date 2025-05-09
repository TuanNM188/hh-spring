package com.formos.huub.framework.utils;

import com.formos.huub.framework.handler.model.FieldError;
import com.formos.huub.framework.handler.model.ResponseData;
import jakarta.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationUtils {

    public static ResponseData buildValidationErrors(Set<? extends ConstraintViolation<?>> violations) {
        List<FieldError> fieldErrors = violations
            .stream()
            .map(v -> new FieldError(v.getPropertyPath().toString(), v.getMessage()))
            .collect(Collectors.toList());
        return ResponseData.builder().isSuccess(false).message("Validation failed").fieldErrors(fieldErrors).build();
    }
}
