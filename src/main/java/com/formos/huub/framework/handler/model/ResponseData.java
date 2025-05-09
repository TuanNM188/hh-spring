/**
 * ***************************************************
 * * Description :
 * * File        : ResponseData
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.handler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseData {

    private Boolean isSuccess;
    private String message;
    private List<String> params;

    private String messageCode;

    @JsonProperty("validationErrors")
    private List<FieldError> fieldErrors;

    private Object data;
}
