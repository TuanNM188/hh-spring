package com.formos.huub.framework.service.sms.impl;

import com.formos.huub.framework.service.sms.ISmsService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service("SmsService")
@Profile({ "dev" })
@EnableAsync(proxyTargetClass = true)
public class SmsService implements ISmsService {

    @Override
    public boolean sendSms(String toNumberPhone, String message) {
        System.out.println("Send sms to " + toNumberPhone + "successfully");
        return Boolean.TRUE;
    }
}
