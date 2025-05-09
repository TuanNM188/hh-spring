package com.formos.huub.service.pushnotification;

import com.formos.huub.domain.entity.User;
import com.formos.huub.framework.service.sms.ISmsService;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.repository.UserRepository;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

/**
 * ***************************************************
 * * Description :
 * * File        : SmsStrategy
 * * Author      : Huy Truong
 * * Date        : Jan 17, 2025
 * ***************************************************
 **/

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableAsync(proxyTargetClass = true)
public class SmsStrategy implements NotificationStrategy {

    ISmsService smsService;
    UserRepository userRepository;

    @Override
    public void processNotification(NotificationContext context) {
        if (ObjectUtils.isEmpty(context.getRecipientIds())) {
            return;
        }
        userRepository.findAllById(context.getRecipientIds()).forEach(recipient -> sendSmsToUser(recipient, context));
    }

    private void sendSmsToUser(User recipient, NotificationContext context) {
        if (Optional.ofNullable(context.getContent()).isPresent()) {
            smsService.sendSms(recipient.getPhoneNumber(), context.getContent());
        }
    }
}
