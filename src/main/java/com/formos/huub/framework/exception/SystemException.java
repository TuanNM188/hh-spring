/**
 * ***************************************************
 * * Description :
 * * File        : SystemException
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.exception;

import com.formos.huub.framework.message.model.Message;
import javax.print.attribute.standard.Severity;

public class SystemException extends BaseException {

    private static final long serialVersionUID = 1L;

    public SystemException(final Message message) {
        super(message);
    }

    public SystemException(final Message message, final Throwable rootCause) {
        super(message, Severity.ERROR, rootCause);
    }
}
