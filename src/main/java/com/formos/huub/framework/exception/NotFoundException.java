/**
 * ***************************************************
 * * Description :
 * * File        : NotFoundException
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.exception;

import com.formos.huub.framework.message.model.Message;
import javax.print.attribute.standard.Severity;

public class NotFoundException extends BaseException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(final Message message) {
        super(message);
    }

    public NotFoundException(final Message message, final Throwable rootCause) {
        super(message, Severity.WARNING, rootCause);
    }
}
