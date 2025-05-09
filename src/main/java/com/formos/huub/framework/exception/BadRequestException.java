/**
 * ***************************************************
 * * Description :
 * * File        : BadRequestException
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.exception;

import com.formos.huub.framework.message.model.Message;
import javax.print.attribute.standard.Severity;

public class BadRequestException extends BaseException {

    private static final long serialVersionUID = 1L;

    public BadRequestException(Message message) {
        super(message, Severity.WARNING);
    }

    public BadRequestException(Message message, Throwable rootCause) {
        super(message, Severity.WARNING, rootCause);
    }
}
