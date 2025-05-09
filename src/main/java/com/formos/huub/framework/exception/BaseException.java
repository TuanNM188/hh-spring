/**
 * ***************************************************
 * * Description :
 * * File        : BaseException
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.formos.huub.framework.message.model.Message;
import javax.print.attribute.standard.Severity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Message msg;
    private Severity severity;

    @JsonIgnore
    private Throwable rootCause;

    public BaseException(Message msg) {
        super(msg.getContent());
        this.msg = msg;
    }

    public BaseException(Message msg, Throwable rootCause) {
        super(msg.getContent(), rootCause);
        this.msg = msg;
    }

    public BaseException(Message msg, Severity severity) {
        super(msg.getContent());
        this.msg = msg;
        this.severity = severity;
    }

    public BaseException(String string, String code, Object[] args, Object object) {}

    public BaseException(Message message, Severity warning, Throwable rootCause) {}

    public BaseException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public BaseException(Throwable rootCause) {}

    public BaseException(String message, Severity warning, Throwable rootCause) {}

    public BaseException(String message) {}
}
