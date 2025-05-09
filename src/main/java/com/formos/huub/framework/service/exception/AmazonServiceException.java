package com.formos.huub.framework.service.exception;

import com.formos.huub.framework.exception.BaseException;
import com.formos.huub.framework.message.model.Message;

import javax.print.attribute.standard.Severity;

public class AmazonServiceException extends BaseException {

    public AmazonServiceException(Message msg) {
        super(msg);
    }

    public AmazonServiceException(Throwable rootCause) {
        super(rootCause);
    }

    public AmazonServiceException(Message msg, Throwable rootCause) {
        super(msg, Severity.ERROR, rootCause);
    }

    public AmazonServiceException(String message, Throwable rootCause) {
        super(message, Severity.ERROR, rootCause);
    }
}
