package com.formos.huub.framework.service.firebase.exception;

import com.formos.huub.framework.exception.BaseException;
import com.formos.huub.framework.message.model.Message;
import javax.print.attribute.standard.Severity;

public class TokenCreationException extends BaseException {

    public TokenCreationException(Message msg) {
        super(msg);
    }

    public TokenCreationException(Throwable rootCause) {
        super(rootCause);
    }

    public TokenCreationException(Message msg, Throwable rootCause) {
        super(msg, Severity.ERROR, rootCause);
    }

    public TokenCreationException(String message, Throwable rootCause) {
        super(message, Severity.ERROR, rootCause);
    }
}
