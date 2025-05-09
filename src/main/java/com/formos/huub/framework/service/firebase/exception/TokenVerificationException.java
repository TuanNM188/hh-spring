package com.formos.huub.framework.service.firebase.exception;

import com.formos.huub.framework.exception.BaseException;
import com.formos.huub.framework.message.model.Message;
import javax.print.attribute.standard.Severity;

public class TokenVerificationException extends BaseException {

    public TokenVerificationException(Message msg) {
        super(msg);
    }

    public TokenVerificationException(Throwable rootCause) {
        super(rootCause);
    }

    public TokenVerificationException(Message msg, Throwable rootCause) {
        super(msg, Severity.ERROR, rootCause);
    }

    public TokenVerificationException(String message, Throwable rootCause) {
        super(message, Severity.ERROR, rootCause);
    }
}
