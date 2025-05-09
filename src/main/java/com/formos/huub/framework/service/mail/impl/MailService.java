package com.formos.huub.framework.service.mail.impl;

import com.formos.huub.config.ApplicationProperties;
import com.formos.huub.domain.entity.User;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.service.mail.IMailService;
import com.formos.huub.framework.service.storage.model.CloudProperties;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.helper.file.FileHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import tech.jhipster.config.JHipsterProperties;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.formos.huub.framework.constant.AppConstants.BASE_URL;
import static com.formos.huub.framework.constant.AppConstants.LOGO_IMAGE;

/**
 * Service for sending emails asynchronously.
 * <p>
 * We use the {@link Async} annotation to send emails asynchronously.
 */
@Service("MailService")
@Profile({ "dev", "docker" })
@EnableAsync(proxyTargetClass = true)
public class MailService implements IMailService {

    private final Logger log = LoggerFactory.getLogger(MailService.class);

    private final ApplicationProperties applicationProperties;
    private final CloudProperties config;

    private static final String USER = "user";

    private static final String TOKEN = "token";

    private final JHipsterProperties jHipsterProperties;

    private final JavaMailSender javaMailSender;

    private final MessageSource messageSource;

    private final SpringTemplateEngine templateEngine;

    private final FileHelper fileHelper;

    public MailService(
        ApplicationProperties applicationProperties,
        CloudProperties config,
        JHipsterProperties jHipsterProperties,
        JavaMailSender javaMailSender,
        MessageSource messageSource,
        SpringTemplateEngine templateEngine,
        FileHelper fileHelper
    ) {
        this.applicationProperties = applicationProperties;
        this.config = config;
        this.jHipsterProperties = jHipsterProperties;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
        this.fileHelper = fileHelper;
    }

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        this.sendEmailSync(to, subject, content, isMultipart, isHtml);
    }

    private void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        log.info(
            "Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
            isMultipart,
            isHtml,
            to,
            subject,
            content
        );

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(jHipsterProperties.getMail().getFrom());
            message.setSubject(subject);
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);
            log.info("Sent email to User '{}'", to);
        } catch (MailException | MessagingException e) {
            log.error("Email could not be sent to user '{}'", to, e);
        }
    }

    @Async
    public <T extends Object> void sendEmailFromTemplate(
        String email,
        String locales,
        HashMap<String, Object> mapContents,
        String templateName,
        String title,
        Object... arguments
    ) {
        if (Objects.isNull(email)) {
            log.info("Email doesn't exist for '{}'", email);
            return;
        }
        if (StringUtils.isBlank(locales)) {
            locales = AppConstants.DEFAULT_LANGUAGE;
        }
        Locale locale = Locale.forLanguageTag(locales);
        Context context = new Context(locale);
        for (Map.Entry<String, Object> entry : mapContents.entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue());
        }
        String content = templateEngine.process(templateName, context);
        String subject = MessageHelper.getMessage(title, arguments);
        sendEmail(email, subject, content, false, true);
    }

    @Async
    public void sendEmailFromTemplate(User user, String templateName, String titleKey) {
        this.sendEmailFromTemplateSync(user, templateName, titleKey);
    }

    private void sendEmailFromTemplateSync(User user, String templateName, String titleKey) {
        if (user.getEmail() == null) {
            log.info("Email doesn't exist for user '{}'", user.getLogin());
            return;
        }
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        this.sendEmailSync(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendActivationEmail(User user) {
        log.info("Sending activation email to '{}'", user.getEmail());
        this.sendEmailFromTemplateSync(user, "mail/activationEmail", "email.activation.title");
    }

    @Async
    public void sendCreationEmail(User user) {
        log.info("Sending creation email to '{}'", user.getEmail());
        this.sendEmailFromTemplateSync(user, "mail/creationEmail", "email.activation.title");
    }

    @Async
    public void sendPasswordResetMail(User user) {
        log.info("Sending password reset email to '{}'", user.getEmail());
        this.sendEmailFromTemplateSync(user, "mail/passwordResetEmail", "email.reset.title");
    }

    @Async
    public void sendPasswordResetMail(String email, String token, String subDomain, String portalPrimaryLogo) {
        log.info("Sending password reset email to '{}'", email);
        String title = MessageHelper.getMessage("email.reset.title", List.of());
        var clientAppUrl = getClientUrl(subDomain);
        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(TOKEN, token);
        mapContents.put(BASE_URL, clientAppUrl);
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(portalPrimaryLogo));
        sendEmailFromTemplate(email, AppConstants.DEFAULT_LANGUAGE, mapContents, "mail/account/passwordResetEmail", title);
    }

    private String getClientUrl(String subDomain) {
        if (!ObjectUtils.isEmpty(subDomain)) {
            return String.format(applicationProperties.getClientApp().getBaseUrlPortal(), subDomain);
        }
        return applicationProperties.getClientApp().getBaseUrl();
    }

}
