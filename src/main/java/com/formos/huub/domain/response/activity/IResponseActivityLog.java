package com.formos.huub.domain.response.activity;

import java.time.Instant;

public interface IResponseActivityLog {
    String getId();
    String getLogin();
    String getActivityType();
    String getDeviceName();
    String getDeviceType();
    String getDeviceInfo();
    String getUserAgent();
    String getOperatingSystem();
    Instant getCreatedDate();
}
