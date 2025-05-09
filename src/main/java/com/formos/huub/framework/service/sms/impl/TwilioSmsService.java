package com.formos.huub.framework.service.sms.impl;

import com.formos.huub.framework.properties.TwilioProperties;
import com.formos.huub.framework.service.sms.ISmsService;
import com.formos.huub.framework.utils.StringUtils;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service("TwilioSmsService")
@Profile({ "stg", "uat", "prod", "huub", "docker" })
@EnableAsync(proxyTargetClass = true)
public class TwilioSmsService implements ISmsService {

    private final TwilioProperties twilioProperties;

    public TwilioSmsService(TwilioProperties twilioProperties) {
        this.twilioProperties = twilioProperties;
    }

    @PostConstruct
    public void init() {
        validateAndInitializeTwilio();
    }

    private void validateAndInitializeTwilio() {
        String accountSid = twilioProperties.getAccountSid();
        String authToken = twilioProperties.getAuthToken();
        String phoneNumber = twilioProperties.getPhoneNumber();
        //Validate Twilio
        if (StringUtils.isNullOrEmpty(accountSid)) {
            System.err.println("Account SID must be configured.");
        }
        if (StringUtils.isNullOrEmpty(authToken)) {
            System.err.println("Auth Token must be configured.");
        }
        if (StringUtils.isNullOrEmpty(phoneNumber)) {
            System.err.println("Twilio phone number must be configured.");
        }
        //Initialize Twilio
        if (!StringUtils.isNullOrEmpty(accountSid) && !StringUtils.isNullOrEmpty(authToken) && !StringUtils.isNullOrEmpty(phoneNumber)) {
            Twilio.init(accountSid, authToken);
        }
    }

    @Override
    public boolean sendSms(String toPhoneNumber, String messageBody) {
        if (StringUtils.isNullOrEmpty(toPhoneNumber)) {
            System.err.println("Recipient phone number must be provided.");
            return false;
        }
        if (StringUtils.isNullOrEmpty(messageBody)) {
            System.err.println("Message body cannot be empty.");
            return false;
        }
        try {
            Message message = Message.creator(
                new PhoneNumber(toPhoneNumber),
                new PhoneNumber(twilioProperties.getPhoneNumber()),
                messageBody
            ).create();
            System.out.println("Send sms to " + toPhoneNumber + "successfully with SID:" + message.getSid());
            return message.getStatus() != Message.Status.FAILED;
        } catch (ApiException e) {
            System.err.println("Error sending SMS: " + e.getMessage());
            return false;
        }
    }
}
