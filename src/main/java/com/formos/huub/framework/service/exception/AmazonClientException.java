package com.formos.huub.framework.service.exception;

import com.formos.huub.framework.exception.BaseException;
import com.formos.huub.framework.message.model.Message;

import javax.print.attribute.standard.Severity;

public class AmazonClientException extends BaseException {

    public AmazonClientException(Message msg) {
        super(msg);
    }

    public AmazonClientException(Throwable rootCause) {
        super(rootCause);
    }

    public AmazonClientException(Message msg, Throwable rootCause) {
        super(msg, Severity.ERROR, rootCause);
    }

    public AmazonClientException(String message, Throwable rootCause) {
        super(message, Severity.ERROR, rootCause);
    }
}
