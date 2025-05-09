/**
 * ***************************************************
 * * Description :
 * * File        : IORuntimeException
 * * Author      : Hung Tran
 * * Date        : Jan 09, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.exception;

import com.formos.huub.framework.message.model.Message;
import javax.print.attribute.standard.Severity;

public class IORuntimeException extends BaseException {

    private static final long serialVersionUID = 1L;

    public IORuntimeException(final Message message) {
        super(message);
    }

    public IORuntimeException(final Throwable rootCause) {
        super(rootCause.getMessage(), rootCause);
    }

    public IORuntimeException(final Message message, final Throwable rootCause) {
        super(message, Severity.WARNING, rootCause);
    }
}
