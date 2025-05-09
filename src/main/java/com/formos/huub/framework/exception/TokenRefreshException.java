/**
 * ***************************************************
 * * Description :
 * * File        : TokenRefreshException
 * * Author      : Hung Tran
 * * Date        : Feb 10, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.exception;

import com.formos.huub.framework.message.model.Message;
import javax.print.attribute.standard.Severity;

public class TokenRefreshException extends BaseException {

    private static final long serialVersionUID = 1L;

    public TokenRefreshException(final Message message) {
        super(message);
    }

    public TokenRefreshException(final Message message, final Throwable rootCause) {
        super(message, Severity.ERROR, rootCause);
    }
}
