package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WebhookEventStatusEnum {
    SUCCESS("SUCCESS", "Success"),
    PENDING("PENDING", "Pending"),
    PROCESSING("PROCESSING", "Processing"),
    FAILED("FAILED", "Failed"),
    FAILED_PERMANENTLY("FAILED_PERMANENTLY", "Failed permanently");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
