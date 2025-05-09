package com.formos.huub.framework.utils;

import com.formos.huub.framework.enums.HeaderEnum;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class HeaderUtils {

    private HeaderUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final String TIMEZONE_HEADER = "X-Timezone";

    public static String getTimezone(HttpServletRequest request) {
        return request.getHeader(TIMEZONE_HEADER);
    }

    public static String getHeader(HttpServletRequest request, HeaderEnum headerEnum) {
        if (headerEnum == null) {
            return null;
        }
        return request.getHeader(headerEnum.getValue());
    }

    public static Map<String, String> getCurrentDeviceInfo() {
        HttpServletRequest request = getCurrentRequest();
        Map<String, String> deviceInfo = new HashMap<>();

        deviceInfo.put("userAgent", getCurrentUserAgent());
        deviceInfo.put("ipAddress", getCurrentIpAddress());

        if (request != null) {
            // Add additional headers that might be useful
            String acceptLanguage = request.getHeader("Accept-Language");
            if (acceptLanguage != null) {
                deviceInfo.put("acceptLanguage", acceptLanguage);
            }

            String platform = request.getHeader("Sec-Ch-Ua-Platform");
            if (platform != null) {
                deviceInfo.put("platform", platform);
            }

            String mobile = request.getHeader("Sec-Ch-Ua-Mobile");
            if (mobile != null) {
                deviceInfo.put("mobile", mobile);
            }
        }

        return deviceInfo;
    }

    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest();
        }
        log.warn("No request context available");
        return null;
    }

    private static String getCurrentUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String userAgent = request.getHeader("User-Agent");
            return userAgent != null ? userAgent : "Unknown";
        }
        return "No Request Context";
    }

    private static String getCurrentIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "No Request Context";
        }

        // Check for proxy forwarded IP first
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // If there are multiple IPs (forwarded through multiple proxies)
        // get the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // Handle localhost IPv6
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }
}
