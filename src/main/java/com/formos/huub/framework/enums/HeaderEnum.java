package com.formos.huub.framework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ***************************************************
 * * Description :
 * * File        : HeaderEnum
 * * Author      : Hung Tran
 * * Date        : May 01, 2024
 * ***************************************************
 **/

@AllArgsConstructor
@Getter
public enum HeaderEnum implements CodeEnum {
    X_DEVICE_NAME("X-Device-Name", ""),
    X_DEVICE_TYPE("X-Device-Type", ""),
    X_DEVICE_INFO("X-Device-Info", ""),
    X_DEVICE_TOKEN("X-Device-Token", ""),
    X_OPERATING_SYSTEM("X-Operating-System", ""),
    X_USER_AGENT("X-User-Agent", ""),
    X_BROWSER("X-Browser", ""),
    X_BROWSER_VERSION("X-Browser-Version", ""),
    X_ADDITIONAL_INFO("X-Additional-Info", ""),
    AUTHORIZATION("Authorization", "");

    private final String value;
    private final String name;
}
