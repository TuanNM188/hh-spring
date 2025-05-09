/**
 * ***************************************************
 * * Description :
 * * File        : OAuth2Service
 * * Author      : Hung Tran
 * * Date        : Oct 28, 2024
 * ***************************************************
 **/
package com.formos.huub.service.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formos.huub.config.ApplicationProperties;
import com.formos.huub.domain.enums.SocialProviderEnum;
import com.formos.huub.domain.record.FacebookUser;
import com.formos.huub.domain.record.StateInfo;
import com.formos.huub.domain.response.oauth2.FacebookTokenResponse;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.SocialConnectException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.properties.FacebookProperties;
import com.formos.huub.framework.properties.GoogleProperties;
import com.formos.huub.framework.properties.MicrosoftProperties;
import com.formos.huub.framework.support.DomainSupport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OAuth2Service {

    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String FACEBOOK_GRAPH_URL = "https://graph.facebook.com/v18.0/";
    private static final String OAUTH_CACHE_NAME = "oauthStateCache";

    GoogleProperties googleProperties;
    FacebookProperties facebookProperties;
    MicrosoftProperties microsoftProperties;
    CacheManager cacheManager;
    DomainSupport domainSupport;
    ApplicationProperties applicationProperties;
    RestTemplate restTemplate;
    ObjectMapper objectMapper;

    /**
     * Handle OAuth callback
     * @param code String
     * @param state String
     * @param provider SocialProviderEnum
     * @return String
     */
    public String handleOAuthCallback(String code, String state, SocialProviderEnum provider) {
        try {
            StateInfo stateInfo = decodeState(state);
            boolean isCustomDomain = !domainSupport.isMainDomain(stateInfo.origin());
            boolean isRootDomain = isRootDomain(stateInfo.origin());
            Object tokenResponse = getTokenResponse(code, provider);
            String stateToken = cacheTokenResponse(tokenResponse);
            return buildRedirectUrl(isCustomDomain, isRootDomain, stateInfo, stateToken, provider);
        } catch (Exception e) {
            log.error("Error in {} callback processing: ", provider, e);
            throw new SocialConnectException(String.format("Failed to process %s callback", provider), e);
        }
    }

    /**
     * Get token response
     * @param code String
     * @param provider SocialProviderEnum
     * @return Object
     */
    private Object getTokenResponse(String code, SocialProviderEnum provider) throws IOException {
        return switch (provider) {
            case GOOGLE -> requestGoogleToken(code);
            case FACEBOOK -> requestFacebookToken(code);
            case MICROSOFT -> requestMicrosoftToken(code);
        };
    }

    /**
     * Get token info from cache
     * @param stateToken String
     * @return Map<String, Object>
     */
    public Map<String, Object> getTokenInfoFromCache(String stateToken) {
        Cache cache = getCache();
        Cache.ValueWrapper valueWrapper = cache.get(stateToken);

        if (valueWrapper == null) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "OAuth token"));
        }

        Object cachedValue = valueWrapper.get();
        Map<String, Object> data = convertToMap(cachedValue);
        cache.evict(stateToken);
        return data;
    }

    /**
     * Convert cached value to map
     * @param value Object
     * @return Map<String, Object>
     */
    private Map<String, Object> convertToMap(Object value) {
        try {
            if (value instanceof FacebookTokenResponse response) {
                return Map.of(
                    "access_token",
                    response.getAccessToken(),
                    "token_type",
                    response.getTokenType(),
                    "expires_in",
                    response.getExpiresIn()
                );
            }
            if (value instanceof Map) {
                return (Map<String, Object>) value;
            }
            throw new IllegalStateException("Unexpected value type in cache: " + value.getClass());
        } catch (Exception e) {
            log.error("Error converting cached value to map", e);
            throw new SocialConnectException("Failed to process cached token data", e);
        }
    }

    /**
     * Decode state
     * @param state String
     * @return StateInfo
     */
    private StateInfo decodeState(String state) throws IOException {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(state);
            String decodedTenant = new String(decodedBytes, StandardCharsets.UTF_8);

            Map<String, String> stateMap = objectMapper.readValue(decodedTenant, Map.class);

            String origin = stateMap.get("origin");
            Optional<String> subdomain = domainSupport.extractSubdomain(origin);

            return new StateInfo(origin, subdomain);
        } catch (Exception e) {
            log.error("Error decoding state: ", e);
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "state"));
        }
    }

    private boolean isRootDomain(String origin) {
        return domainSupport.extractSubdomain(origin).isEmpty();
    }

    /**
     * Request Google token
     * @param code String
     * @return GoogleTokenResponse
     */
    private GoogleTokenResponse requestGoogleToken(String code) throws IOException {
        return new GoogleAuthorizationCodeTokenRequest(
            new NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            GOOGLE_TOKEN_URL,
            googleProperties.getClientId(),
            googleProperties.getClientSecret(),
            code,
            googleProperties.getRedirectUri()
        ).execute();
    }

    /**
     * Request Facebook token
     * @param code String
     * @return FacebookTokenResponse
     */
    private FacebookTokenResponse requestFacebookToken(String code) {
        try {
            String tokenUrl = UriComponentsBuilder.fromHttpUrl(FACEBOOK_GRAPH_URL + "oauth/access_token")
                .queryParam("client_id", facebookProperties.getClientId())
                .queryParam("client_secret", facebookProperties.getClientSecret())
                .queryParam("code", code)
                .queryParam("redirect_uri", facebookProperties.getRedirectUri())
                .build()
                .toUriString();

            return restTemplate.getForObject(tokenUrl, FacebookTokenResponse.class);
        } catch (Exception e) {
            log.error("Error getting Facebook access token", e);
            throw new SocialConnectException("Failed to get Facebook access token", e);
        }
    }

    /**
     * Get Facebook user info
     * @param accessToken String
     * @return FacebookUser
     */
    public FacebookUser getFacebookUserInfo(String accessToken) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(FACEBOOK_GRAPH_URL + "me")
                .queryParam("fields", "id,name,email,picture")
                .queryParam("access_token", accessToken)
                .build()
                .toUriString();

            return restTemplate.getForObject(url, FacebookUser.class);
        } catch (Exception e) {
            log.error("Error getting Facebook user info", e);
            throw new SocialConnectException("Failed to get Facebook user info", e);
        }
    }

    /**
     * Request Microsoft token
     * @param authorizationCode String
     * @return Map<String, Object>
     */
    private Map<String, Object> requestMicrosoftToken(String authorizationCode) {
        try {
            ConfidentialClientApplication app = ConfidentialClientApplication.builder(
                microsoftProperties.getClientId(),
                ClientCredentialFactory.createFromSecret(microsoftProperties.getClientSecret())
            )
                .authority(microsoftProperties.getAuthority())
                .build();

            IAuthenticationResult result = app
                .acquireToken(
                    AuthorizationCodeParameters.builder(authorizationCode, new URI(microsoftProperties.getRedirectUri()))
                        .scopes(Collections.singleton(microsoftProperties.getScopes()))
                        .build()
                )
                .get();

            // This way is not good (dirty), but there is no other way. LoL ^^
            final Field refreshTokenField = result.getClass().getDeclaredField("refreshToken");
            refreshTokenField.setAccessible(true);
            String refreshToken = refreshTokenField.get(result).toString();

            return Map.of(
                "access_token",
                result.accessToken(),
                "expires_on",
                result.expiresOnDate(),
                "id_token",
                result.idToken(),
                "refresh_token",
                refreshToken,
                "username",
                result.account().username()
            );
        } catch (Exception e) {
            log.error("Error exchanging code for token", e);
            throw new SocialConnectException("Failed to exchange code for token", e);
        }
    }

    /**
     * Cache token response
     * @param tokenResponse Object
     * @return String
     */
    private String cacheTokenResponse(Object tokenResponse) {
        String stateToken = UUID.randomUUID().toString();
        getCache().put(stateToken, tokenResponse);
        return stateToken;
    }

    /**
     * Get cache
     * @return Cache
     */
    private Cache getCache() {
        Cache cache = cacheManager.getCache(OAUTH_CACHE_NAME);
        if (cache == null) {
            throw new IllegalStateException("Cache '" + OAUTH_CACHE_NAME + "' is not configured.");
        }
        return cache;
    }

    /**
     * Build redirect URL for OAuth2 callback
     * @param isRootDomain boolean
     * @param stateInfo StateInfo
     * @param stateToken String
     * @param provider SocialProviderEnum
     * @return String
     */
    private String buildRedirectUrl(
        boolean isCustomDomain,
        boolean isRootDomain,
        StateInfo stateInfo,
        String stateToken,
        SocialProviderEnum provider
    ) {
        if (isCustomDomain) {
            String urlTemplate = stateInfo.origin() + "/oauth2/callback?state-token=%s";
            return String.format(urlTemplate, stateToken) + "&provider=" + provider.getValue();
        }

        String baseUrl = isRootDomain ? applicationProperties.clientApp.getBaseUrl() : applicationProperties.clientApp.getBaseUrlPortal();

        String urlTemplate = baseUrl + "/oauth2/callback?state-token=%s";
        String finalURL = isRootDomain
            ? String.format(urlTemplate, stateToken)
            : String.format(urlTemplate, stateInfo.subdomain().get(), stateToken);

        return finalURL + "&provider=" + provider.getValue();
    }

    public void writeClosePopupResponse(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        writer.println("<script>");
        writer.println("window.opener.postMessage('error', '*');");
        writer.println("window.close();");
        writer.println("</script>");
        writer.flush();
    }
}
