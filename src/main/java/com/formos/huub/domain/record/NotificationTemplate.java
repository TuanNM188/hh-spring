package com.formos.huub.domain.record;

public record NotificationTemplate(String templatePath, String titleKey, String smsContentKey, boolean hasSmsContent) {
}
