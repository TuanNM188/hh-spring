package com.formos.huub.framework.exception;

import com.formos.huub.framework.message.model.Message;
import javax.print.attribute.standard.Severity;

/**
 * ***************************************************
 * * Description :
 * * File        : TokenException
 * * Author      : Hung Tran
 * * Date        : May 01, 2024
 * ***************************************************
 **/
public class TokenException extends BaseException {

    private static final long serialVersionUID = 1L;

    public TokenException(final Message message) {
        super(message);
    }

    public TokenException(final Message message, final Throwable rootCause) {
        super(message, Severity.ERROR, rootCause);
    }
}
