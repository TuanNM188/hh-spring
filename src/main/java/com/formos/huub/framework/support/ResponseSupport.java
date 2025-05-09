/**
 * ***************************************************
 * * Description :
 * * File        : ResponseSupport
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.support;

import com.formos.huub.framework.handler.model.FieldError;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseSupport {

    public ResponseEntity<ResponseData> success() {
        return response(HttpStatus.OK, MessageHelper.getMessage(Message.Keys.I0001), new ResponseData(), null);
    }

    public ResponseEntity<ResponseData> success(Message message) {
        return response(HttpStatus.OK, message, new ResponseData(), null);
    }

    public ResponseEntity<ResponseData> success(ResponseData data) {
        return response(HttpStatus.OK, MessageHelper.getMessage(Message.Keys.I0001), data, null);
    }

    public ResponseEntity<ResponseData> success(Message message, ResponseData data) {
        return response(HttpStatus.OK, message, data, null);
    }

    public ResponseEntity<ResponseData> failed() {
        return response(HttpStatus.BAD_REQUEST, MessageHelper.getMessage(Message.Keys.E0001), new ResponseData(), null);
    }

    public ResponseEntity<ResponseData> failed(Message message) {
        return response(HttpStatus.BAD_REQUEST, message, new ResponseData(), null);
    }

    public ResponseEntity<ResponseData> failed(String message) {
        Message msg = new Message();
        msg.setContent(message);
        return response(HttpStatus.BAD_REQUEST, msg, new ResponseData(), null);
    }

    public ResponseEntity<ResponseData> failed(HttpStatus httpStatus, Message message) {
        return response(httpStatus, message, new ResponseData(), null);
    }

    public ResponseEntity<ResponseData> failed(HttpStatus httpStatus, Message message, List<FieldError> filedErrs) {
        return response(httpStatus, message, new ResponseData(), filedErrs);
    }

    private ResponseEntity<ResponseData> response(HttpStatus httpStatus, Message message, ResponseData data, List<FieldError> fieldErrors) {
        if (data == null) {
            data = new ResponseData();
        }
        if (httpStatus.equals(HttpStatus.OK)) {
            data.setIsSuccess(true);
        } else {
            data.setIsSuccess(false);
            if (CollectionUtils.isNotEmpty(fieldErrors)) {
                data.setFieldErrors(fieldErrors);
            }
            data.setData(null);
            data.setParams(message.getParams());
        }
        if (Objects.nonNull(message)) {
            data.setMessage(message.getContent());
            //            data.setMessageCode(message.getId().toString());
            data.setMessageCode(message.getId() != null ? message.getId().toString() : null);
        }
        return ResponseEntity.status(httpStatus).body(data);
    }
}
