package com.formos.huub.framework.exception;

public class ActiveCampaignException extends BaseException {

    public ActiveCampaignException(String message) {
        super(message);
    }

    public ActiveCampaignException(Throwable rootCause) {
        super(rootCause);
    }

    public ActiveCampaignException(String message, Throwable rootCause) {
        super(message, rootCause);
    }
}
