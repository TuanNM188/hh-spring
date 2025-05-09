package com.formos.huub.framework.service.mail;

import com.formos.huub.domain.entity.User;
import java.util.HashMap;

public interface IMailService {
    void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml);

    void sendEmailFromTemplate(User user, String templateName, String titleKey);

    <T extends Object> void sendEmailFromTemplate(
        String email,
        String locales,
        HashMap<String, Object> mapContents,
        String templateName,
        String title,
        Object... arguments
    );

    void sendActivationEmail(User user);

    void sendCreationEmail(User user);

    void sendPasswordResetMail(String email, String resetKey, String subDomain, String portalPrimaryLogo);

}
