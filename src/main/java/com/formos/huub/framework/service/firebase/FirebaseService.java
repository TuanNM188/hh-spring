/**
 * ***************************************************
 * * Description :
 * * File        : FirebaseService
 * * Author      : Hung Tran
 * * Date        : Nov 29, 2024
 * ***************************************************
 **/
package com.formos.huub.framework.service.firebase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formos.huub.domain.entity.Authority;
import com.formos.huub.domain.entity.User;
import com.formos.huub.framework.properties.FirebaseProperties;
import com.formos.huub.framework.service.firebase.exception.RateLimitExceededException;
import com.formos.huub.framework.service.firebase.exception.TokenCreationException;
import com.formos.huub.framework.service.firebase.exception.TokenVerificationException;
import com.formos.huub.framework.utils.HeaderUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FirebaseService {

    private final FirebaseAuth firebaseAuth;
    private final RateLimiter rateLimiter;
    private final Cache<UUID, Integer> tokenRequestCount;
    private final ObjectMapper objectMapper;
    private final int maxTokensPerHour;
    private final int maxClaimsSize;

    public FirebaseService(FirebaseProperties firebaseProperties) {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.maxTokensPerHour = firebaseProperties.getToken().getRateLimit().getTokens();
        this.maxClaimsSize = firebaseProperties.getToken().getMaxClaimsSize();
        this.rateLimiter = RateLimiter.create(maxTokensPerHour / 3600.0);
        this.tokenRequestCount = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
        this.objectMapper = new ObjectMapper();
    }

    public String createFirebaseCustomToken(UUID userId, Map<String, Object> claims) {
        try {
            String uid = userId.toString();
            validateInput(uid, claims);
            Map<String, Object> sanitizedClaims = sanitizeClaims(claims);

            String token = firebaseAuth.createCustomToken(uid, sanitizedClaims);
            recordTokenCreation(userId);

            log.debug("Created Firebase custom token for UID: {}", uid);
            return token;
        } catch (FirebaseAuthException e) {
            log.error("Failed to create Firebase custom token", e);
            throw new TokenCreationException("Failed to create Firebase token", e);
        }
    }

    public String createLoginToken(User user) {
        Map<String, Object> claims = new HashMap<>(getBaseClaims(user));
        claims.put("sessionId", generateSessionId());
        claims.put("lastLogin", Instant.now().toString());
        claims.put("deviceInfo", HeaderUtils.getCurrentDeviceInfo());
        claims.put("email", user.getEmail());
        return createFirebaseCustomToken(user.getId(), claims);
    }

    public FirebaseToken verifyFirebaseToken(String idToken) {
        try {
            return firebaseAuth.verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            log.error("Failed to verify Firebase token", e);
            throw new TokenVerificationException("Invalid Firebase token", e);
        }
    }

    private Map<String, Object> getBaseClaims(User user) {
        return Map.of(
            "email",
            user.getEmail(),
            "userId",
            user.getId().toString(),
            "roles",
            user.getAuthorities().stream().map(Authority::getName).toList()
        );
    }

    private void validateInput(String uid, Map<String, Object> claims) {
        if (uid == null || uid.trim().isEmpty()) {
            throw new IllegalArgumentException("UID cannot be null or empty");
        }
        if (uid.length() > 128) {
            throw new IllegalArgumentException("UID cannot be longer than 128 characters");
        }
        if (!uid.matches("^[A-Za-z0-9_-]+$")) {
            throw new IllegalArgumentException("UID contains invalid characters");
        }
        if (claims != null && calculateClaimsSize(claims) > maxClaimsSize) {
            throw new IllegalArgumentException("Claims size exceeds maximum allowed size");
        }
    }

    private Map<String, Object> sanitizeClaims(Map<String, Object> claims) {
        if (claims == null) return new HashMap<>();

        return claims
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> sanitizeClaimValue(entry.getValue())));
    }

    private Object sanitizeClaimValue(Object value) {
        if (value instanceof String) {
            return StringUtils.truncate((String) value, 1000); // Prevent too long strings
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).stream().map(this::sanitizeClaimValue).toList();
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> sanitizeClaimValue(entry.getValue())));
        }
        return value;
    }

    private void enforceRateLimit(UUID userId) {
        if (!rateLimiter.tryAcquire()) {
            throw new RateLimitExceededException("Token creation rate limit exceeded");
        }

        int count = tokenRequestCount.getIfPresent(userId) != null ? tokenRequestCount.getIfPresent(userId) : 0;

        if (count >= maxTokensPerHour) {
            throw new RateLimitExceededException("Token creation limit exceeded for user");
        }
    }

    private void recordTokenCreation(UUID userId) {
        Integer currentCount = tokenRequestCount.getIfPresent(userId);
        tokenRequestCount.put(userId, currentCount != null ? currentCount + 1 : 1);
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    private int calculateClaimsSize(Map<String, Object> claims) {
        try {
            return objectMapper.writeValueAsString(claims).getBytes().length;
        } catch (Exception e) {
            log.warn("Failed to calculate claims size", e);
            return Integer.MAX_VALUE; // Assume worst case
        }
    }
}
