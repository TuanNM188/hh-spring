package com.formos.huub.domain.record;

import com.formos.huub.domain.entity.User;

import java.util.HashMap;
import java.util.UUID;

public record AppointmentNotificationData(
    UUID recipientId, User sender, String templatePath,
    String baseReferenceUrl, String contentSms,
    String title, HashMap<String, Object> contents) {}
