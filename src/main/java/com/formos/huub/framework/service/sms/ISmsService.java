package com.formos.huub.framework.service.sms;

public interface ISmsService {
    boolean sendSms(String toNumberPhone, String message);
}
