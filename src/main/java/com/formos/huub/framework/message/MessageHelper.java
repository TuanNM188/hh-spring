/**
 * ***************************************************
 * * Description :
 * * File        : MessageHelper
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.message;

import com.formos.huub.framework.message.model.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.formos.huub.framework.utils.ObjectUtils;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MessageHelper implements InitializingBean {

    @Getter
    private static MessageHelper instance = null;

    final MessageSource messageSource;

    public MessageHelper(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public static Message getMessage(Message.Keys key, Object... arguments) {
        Message message = new Message();
        message.setId(key);
        try {
            List<String> strings = new ArrayList<>();
            for (Object argument : arguments) {
                strings.add(argument == null ? null : argument.toString());
                message.setParams(ObjectUtils.isEmpty(argument) ? null : Collections.singletonList(argument.toString()));
            }
            Locale locale = LocaleContextHolder.getLocale();
            message.setContent(instance.messageSource.getMessage(key.name(), strings.toArray(), locale));
            return message;
        } catch (Exception ex) {
            return message;
        }
    }

    public static String getMessage(String key, Object... arguments) {
        try {
            List<String> strings = new ArrayList<>();
            for (Object argument : arguments) {
                strings.add(argument == null ? null : argument.toString());
            }
            Locale locale = LocaleContextHolder.getLocale();
            return instance.messageSource.getMessage(key, strings.toArray(), locale);
        } catch (Exception ex) {
            return key;
        }
    }

    public static String getMessage(String message, String fileName) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            return instance.messageSource.getMessage(null, new Object[] { fileName }, message, locale);
        } catch (Exception ex) {
            return message;
        }
    }

    public static String getMessageWithCode(String messageCode) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            return instance.messageSource.getMessage(messageCode, null, locale);
        } catch (Exception ex) {
            return messageCode;
        }
    }

    public static Message getMessage(String message) {
        Message messageContent = new Message();
        messageContent.setContent(message);
        return messageContent;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (instance == null) {
            instance = this;
        }
    }
}
