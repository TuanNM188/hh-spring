/**
 * ***************************************************
 * * Description :
 * * File        : SocialConnectException
 * * Author      : Hung Tran
 * * Date        : Nov 07, 2024
 * ***************************************************
 **/
package com.formos.huub.framework.exception;

import com.formos.huub.framework.message.model.Message;

public class SocialConnectException extends BaseException {

    public SocialConnectException(Message msg) {
        super(msg);
    }

    public SocialConnectException(String message, Throwable rootCause) {
        super(message, rootCause);
    }
}
