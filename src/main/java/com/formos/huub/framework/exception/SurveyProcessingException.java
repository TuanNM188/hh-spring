package com.formos.huub.framework.exception;

import com.formos.huub.framework.message.model.Message;

public class SurveyProcessingException extends BaseException {

    public SurveyProcessingException(Message msg) {
        super(msg);
    }

    public SurveyProcessingException(String message, Throwable rootCause) {
        super(message, rootCause);
    }
}
