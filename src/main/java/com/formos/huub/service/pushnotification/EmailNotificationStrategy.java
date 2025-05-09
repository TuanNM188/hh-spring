package com.formos.huub.service.pushnotification;

import com.formos.huub.config.ApplicationProperties;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.response.usersetting.IResponseValueUserSetting;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.service.mail.IMailService;
import com.formos.huub.framework.service.storage.model.CloudProperties;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.helper.file.FileHelper;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.repository.UserSettingRepository;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.text.WordUtils;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.formos.huub.framework.constant.AppConstants.*;

/**
 * ***************************************************
 * * Description :
 * * File        : EmailNotificationStrategy
 * * Author      : Hung Tran
 * * Date        : Jan 13, 2025
 * ***************************************************
 **/

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableAsync(proxyTargetClass = true)
public class EmailNotificationStrategy implements NotificationStrategy {

    private static final String URL = "url";
    private static final String SUPPORT_EMAIL = "supportEmail";
    private static final String LIVE_CHAT = "liveChat";
    private static final String DESCRIPTION = "description";
    private static final String FOOTER_LINK = "footerLink";
    private static final String RECEIVER_IMAGE = "receiverImage";

    CloudProperties config;
    IMailService mailService;
    UserSettingRepository userSettingRepository;
    UserRepository userRepository;
    ApplicationProperties applicationProperties;
    FileHelper fileHelper;

    @Override
    public void processNotification(NotificationContext context) {
        if (!CollectionUtils.isEmpty(context.getRecipientEmail())) {
            context.getRecipientEmail().forEach(recipientEmail -> sendEmailToSpecialEmail(recipientEmail, context));
            return;
        }
        if (ObjectUtils.isEmpty(context.getRecipientIds())) {
            return;
        }
        if (Optional.ofNullable(context.getSettingKeyCode()).isPresent()) {
            userSettingRepository
                .getValueByKeyCodeAndUserId(context.getSettingKeyCode(), context.getRecipientIds())
                .forEach(recipient -> sendEmail(recipient, context));
        } else {
            userRepository.findAllById(context.getRecipientIds()).forEach(recipient -> sendEmailToUser(recipient, context));
        }
    }

    private void sendEmail(IResponseValueUserSetting recipient, NotificationContext context) {
        String senderName = Objects.isNull(context.getSender()) ? null : WordUtils.capitalize(context.getSender().getNormalizedFullName());
        String receiverName = WordUtils.capitalize(recipient.getName());
        String receiverImage = getImageUrl(recipient.getImageUrl());
        String portalName = Objects.isNull(context.getPortalInfo()) ? null : context.getPortalInfo().get(PORTAL_NAME);
        var baseUrl = Objects.isNull(context.getPortalInfo()) ? applicationProperties.getClientApp().getBaseUrl() : context.getPortalInfo().get(PORTAL_URL);
        String supportEmail = applicationProperties.getCustomerCare().getJoinHuubSupport();
        String liveChat = applicationProperties.getCustomerCare().getJoinHuubLiveChat();
        String portalPrimaryLogo = context.getPortalInfo().get(PORTAL_LOGO);

        HashMap<String, Object> contents = new HashMap<>();
        contents.put(PREFERENCE_URL, context.getReferenceUrl());
        contents.put(RECEIVER_NAME, receiverName);
        contents.put(RECEIVER_IMAGE, receiverImage);
        contents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(portalPrimaryLogo));
        contents.put(PORTAL_NAME, portalName);
        contents.put(SENDER_NAME, senderName);
        contents.put(DESCRIPTION, context.getDescription());
        contents.put(BASE_URL, baseUrl);
        contents.put(SUPPORT_EMAIL, supportEmail);
        contents.put(LIVE_CHAT, liveChat);

        if (context.getAdditionalData() != null) {
            contents.putAll(context.getAdditionalData());
        }

        mailService.sendEmailFromTemplate(
            recipient.getEmail(),
            AppConstants.DEFAULT_LANGUAGE,
            contents,
            context.getTemplatePath(),
            context.getEmailTitle()
        );
    }

    private String getImageUrl(String imageUrl) {
        if (StringUtils.isNullOrEmpty(imageUrl)) {
            return config.getCloudFrontUrl() + "/" + AppConstants.DEFAULT_AVATAR;
        }
        if (imageUrl.startsWith("http")) {
            return imageUrl;
        }
        return config.getCloudFrontUrl() + "/" + imageUrl;
    }

    private void sendEmailToUser(User recipient, NotificationContext context) {
        String senderName = Objects.isNull(context.getSender()) ? null : WordUtils.capitalize(context.getSender().getNormalizedFullName());
        String receiverName = WordUtils.capitalize(recipient.getFirstName());
        String portalName = Objects.isNull(context.getPortalInfo()) ? null : context.getPortalInfo().get(PORTAL_NAME);
        var baseUrl = Objects.isNull(context.getPortalInfo()) ? applicationProperties.getClientApp().getBaseUrl() : context.getPortalInfo().get(PORTAL_URL);
        String supportEmail = applicationProperties.getCustomerCare().getJoinHuubSupport();
        String liveChat = applicationProperties.getCustomerCare().getJoinHuubLiveChat();
        String portalPrimaryLogo = context.getPortalInfo().get(PORTAL_LOGO);

        HashMap<String, Object> contents = new HashMap<>();
        contents.put(RECEIVER_NAME, receiverName);
        contents.put(SENDER_NAME, senderName);
        contents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(portalPrimaryLogo));
        contents.put(PORTAL_NAME, portalName);
        contents.put(BASE_URL, baseUrl);
        contents.put(SUPPORT_EMAIL, supportEmail);
        contents.put(LIVE_CHAT, liveChat);
        if (context.getAdditionalData() != null) {
            contents.putAll(context.getAdditionalData());
        }
        mailService.sendEmailFromTemplate(
            recipient.getEmail(),
            AppConstants.DEFAULT_LANGUAGE,
            contents,
            context.getTemplatePath(),
            context.getEmailTitle()
        );
    }

    private void sendEmailToSpecialEmail(String specialEmail, NotificationContext context) {
        HashMap<String, Object> contents = new HashMap<>();
        var baseUrl = Objects.isNull(context.getPortalInfo()) ? applicationProperties.getClientApp().getBaseUrl() : context.getPortalInfo().get(PORTAL_URL);
        String portalPrimaryLogo = context.getPortalInfo().get(PORTAL_LOGO);

        contents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(portalPrimaryLogo));
        contents.put(BASE_URL, baseUrl);
        if (context.getAdditionalData() != null) {
            contents.putAll(context.getAdditionalData());
        }
        mailService.sendEmailFromTemplate(
            specialEmail,
            AppConstants.DEFAULT_LANGUAGE,
            contents,
            context.getTemplatePath(),
            context.getEmailTitle()
        );
    }
}
