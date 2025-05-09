package com.formos.huub.framework.service.firebase.exception;

import com.formos.huub.framework.exception.BaseException;
import com.formos.huub.framework.message.model.Message;
import javax.print.attribute.standard.Severity;

public class RateLimitExceededException extends BaseException {

    public RateLimitExceededException(Message msg) {
        super(msg);
    }

    public RateLimitExceededException(Throwable rootCause) {
        super(rootCause);
    }

    public RateLimitExceededException(Message msg, Throwable rootCause) {
        super(msg, Severity.ERROR, rootCause);
    }

    public RateLimitExceededException(String message, Throwable rootCause) {
        super(message, Severity.ERROR, rootCause);
    }

    public RateLimitExceededException(String msg) {
        super(msg);
    }
}
