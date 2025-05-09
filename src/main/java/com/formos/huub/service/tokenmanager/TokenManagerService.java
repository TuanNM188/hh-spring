/**
 * ***************************************************
 * * Description :
 * * File        : TokenManagerService
 * * Author      : Hung Tran
 * * Date        : Feb 09, 2023
 * ***************************************************
 **/
package com.formos.huub.service.tokenmanager;

import com.formos.huub.config.ApplicationProperties;
import com.formos.huub.config.Constants;
import com.formos.huub.domain.entity.CommunityPartner;
import com.formos.huub.domain.entity.DeviceToken;
import com.formos.huub.domain.entity.TokenManager;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.PortalStatusEnum;
import com.formos.huub.domain.request.authenticate.RequestSignInAsUser;
import com.formos.huub.domain.request.authenticate.RequestSwitchPortal;
import com.formos.huub.domain.request.authenticate.RequestTokenRefresh;
import com.formos.huub.domain.request.authenticate.SignInRequest;
import com.formos.huub.domain.request.directmessage.RequestSendMessageToConversation;
import com.formos.huub.domain.response.authenticate.ResponseHeader;
import com.formos.huub.domain.response.authenticate.ResponseLogin;
import com.formos.huub.domain.response.authenticate.ResponseLoginAsUser;
import com.formos.huub.domain.response.authenticate.ResponseTokenRefresh;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.context.PortalContext;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.enums.HeaderEnum;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.exception.SocialConnectException;
import com.formos.huub.framework.exception.TokenRefreshException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.service.firebase.FirebaseService;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.framework.utils.HeaderUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.repository.*;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.DomainUserDetailsService;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.security.jwt.TokenProvider;
import com.formos.huub.service.activecampaign.ActiveCampaignStrategy;
import com.formos.huub.service.communitypartner.CommunityPartnerService;
import com.formos.huub.service.directmessage.DirectMessageService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.config.JHipsterProperties;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TokenManagerService {

    private static final String TOKEN_MANAGER_CACHE_NAME = "tokenManagerStateCache";

    TokenManagerRepository tokenManagerRepository;
    TokenProvider tokenProvider;
    OAuth2LoginFactory oAuth2LoginFactory;
    DomainUserDetailsService domainUserDetailsService;
    JHipsterProperties jHipsterProperties;
    UserRepository userRepository;
    DeviceTokenRepository deviceTokenRepository;
    PortalHostRepository portalHostRepository;
    TechnicalAdvisorRepository technicalAdvisorRepository;
    BusinessOwnerRepository businessOwnerRepository;
    PortalRepository portalRepository;
    CacheManager cacheManager;
    ApplicationProperties applicationProperties;
    FirebaseService firebaseService;
    DirectMessageService directMessageService;
    CommunityPartnerService communityPartnerService;
    ActiveCampaignStrategy activeCampaignStrategy;

    @Transactional
    public ResponseLogin signIn(SignInRequest requestLogin) {
        String provider = Optional.ofNullable(requestLogin.getProvider()).orElse(Constants.EMAIL_LOGIN);
        requestLogin.setProvider(provider);
        OAuth2LoginStrategy strategy = oAuth2LoginFactory.getStrategy(provider);
        Authentication authentication = strategy.authenticate(requestLogin);
        return processSignIn(authentication, requestLogin);
    }

    private ResponseLogin processSignIn(Authentication authentication, SignInRequest requestLogin) {
        String username = authentication.getName();
        User user = userRepository.findOneByEmailIgnoreCase(username).get();
        boolean isEmailLogin = Constants.EMAIL_LOGIN.equals(requestLogin.getProvider());
        validateUserAccess(username, isEmailLogin);
        var tokens = authenticateAndGenerateTokens(authentication, username, requestLogin.isRememberMe());
        user.setLangKey(requestLogin.getLanguage());
        handleCommunityPartner(user);
        handleFirstLogin(user);
        activeCampaignStrategy.handleBusinessOwnerLastLogin(user);
        userRepository.save(user);
        var firebaseToken = firebaseService.createLoginToken(user);
        return ResponseLogin.builder()
            .accessToken(tokens.jwt)
            .refreshToken(tokens.refreshToken)
            .firebaseToken(firebaseToken)
            .username(username)
            .userId(user.getId())
            .build();
    }

    private static class Tokens {

        String jwt;
        String refreshToken;

        Tokens(String jwt, String refreshToken) {
            this.jwt = jwt;
            this.refreshToken = refreshToken;
        }
    }

    private Tokens authenticateAndGenerateTokens(Authentication authentication, String username, boolean rememberMe) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication, rememberMe);
        String refreshToken = rememberMe ? StringUtils.generateRandomAlphanumericString() : AppConstants.KEY_EMPTY;
        saveTokenManager(getHeader(), username, jwt, refreshToken);
        return new Tokens(jwt, refreshToken);
    }

    private void validateUserAccess(String username, boolean isEmailLogin) {
        var portalContext = PortalContextHolder.getContext();
        if (Objects.nonNull(portalContext) && !portalContext.isAdminAccess()) {
            validateLoginWithPortalUrl(username, portalContext.getPortalId(), isEmailLogin);
        } else {
            validateLoginWithAdminUrl(username, isEmailLogin);
        }
    }

    private void handleCommunityPartner(User user) {
        CommunityPartner communityPartner = user.getCommunityPartner();
        if (Objects.nonNull(communityPartner)) {
            communityPartnerService.handleActivateCommunityPartner(user, communityPartner);
        }
    }

    @Transactional
    public String signInAsUser(RequestSignInAsUser request) {
        var username = request.getUserName();
        var portalId = request.getPortalId();
        if (Objects.nonNull(portalId)) {
            validateLoginWithPortalUrl(username, portalId, true);
        } else {
            validateLoginWithAdminUrl(username, true);
        }
        // Get header
        var adminUser = SecurityUtils.getCurrentUserLogin().get();
        ResponseHeader header = getHeader();
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createTokenSignInAsUser(authentication, adminUser, portalId);
        String refreshToken = StringUtils.generateRandomAlphanumericString();

        // Save token manager
        saveTokenManager(header, username, jwt, refreshToken);
        var originUrl = Objects.nonNull(PortalContextHolder.getOriginUrl())
            ? PortalContextHolder.getOriginUrl()
            : applicationProperties.getClientApp().getBaseUrl();
        User user = userRepository.findOneByEmailIgnoreCase(username).get();
        var firebaseToken = firebaseService.createLoginToken(user);
        var dataToken = ResponseLoginAsUser.builder()
            .accessToken(jwt)
            .refreshToken(refreshToken)
            .username(username)
            .adminUser(adminUser)
            .adminUrl(originUrl)
            .firebaseToken(firebaseToken)
            .build();

        return cacheTokenResponse(dataToken);
    }

    /**
     * Switch portal
     *
     * @param request RequestSwitchPortal
     * @return String
     */
    @Transactional
    public String switchPortal(RequestSwitchPortal request) {
        String username = request.getUserName();
        UUID portalId = request.getPortalId();
        String adminUserReq = request.getAdminUser();
        String adminUrl = request.getAdminUrl();
        String adminUser = Objects.nonNull(adminUserReq) ? adminUserReq : SecurityUtils.getCurrentUserLogin().get();
        validateLoginWithPortalUrl(username, portalId, true);

        // Get header
        ResponseHeader header = getHeader();
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createTokenSignInAsUser(authentication, adminUser, portalId);
        String refreshToken = StringUtils.generateRandomAlphanumericString();

        // Save token manager
        saveTokenManager(header, username, jwt, refreshToken);
        String originUrl = Objects.nonNull(adminUrl) ? adminUrl : applicationProperties.getClientApp().getBaseUrl();
        User user = userRepository.findOneByEmailIgnoreCase(username).get();
        String firebaseToken = firebaseService.createLoginToken(user);
        ResponseLoginAsUser dataToken = ResponseLoginAsUser.builder()
            .accessToken(jwt)
            .refreshToken(refreshToken)
            .username(username)
            .adminUser(adminUser)
            .adminUrl(originUrl)
            .firebaseToken(firebaseToken)
            .build();

        return cacheTokenResponse(dataToken);
    }

    /**
     * Get token info from cache
     *
     * @param stateToken String
     * @return Map<String, Object>
     */
    public Map<String, Object> getDataTokenFromCache(String stateToken) {
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

    private Map<String, Object> convertToMap(Object value) {
        try {
            if (value instanceof Map) {
                return (Map<String, Object>) value;
            }

            if (value instanceof ResponseLoginAsUser response) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("accessToken", response.getAccessToken());
                resultMap.put("refreshToken", response.getRefreshToken());
                resultMap.put("username", response.getUsername());
                resultMap.put("adminUser", response.getAdminUser());
                resultMap.put("adminUrl", response.getAdminUrl());
                resultMap.put("firebaseToken", response.getFirebaseToken());
                return resultMap;
            }
            throw new IllegalStateException("Unexpected value type in cache: " + value.getClass().getName());
        } catch (Exception e) {
            log.error("Error converting cached value to map: {}", value, e);
            throw new SocialConnectException("Failed to process cached token data", e);
        }
    }

    /**
     * Cache token response
     *
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
     *
     * @return Cache
     */
    private Cache getCache() {
        Cache cache = cacheManager.getCache(TOKEN_MANAGER_CACHE_NAME);
        if (cache == null) {
            throw new IllegalStateException("Cache '" + TOKEN_MANAGER_CACHE_NAME + "' is not configured.");
        }
        return cache;
    }

    private ResponseHeader getHeader() {
        HttpServletRequest request = HeaderUtils.getCurrentRequest();
        return ResponseHeader.builder()
            .deviceName(HeaderUtils.getHeader(request, HeaderEnum.X_DEVICE_NAME))
            .deviceType(HeaderUtils.getHeader(request, HeaderEnum.X_DEVICE_TYPE))
            .deviceInfo(HeaderUtils.getHeader(request, HeaderEnum.X_DEVICE_INFO))
            .deviceToken(HeaderUtils.getHeader(request, HeaderEnum.X_DEVICE_TOKEN))
            .build();
    }

    private void saveTokenManager(ResponseHeader header, String username, String jwt, String refreshToken) {
        String deviceName = header.getDeviceName();
        String deviceType = header.getDeviceType();
        String deviceInfo = header.getDeviceInfo();
        String deviceToken = header.getDeviceToken();
        String accessTokenKey = StringUtils.generateRandomAlphanumericString(50);

        // check accessTokenKey is existed
        while (tokenManagerRepository.existsByAccessTokenKey(accessTokenKey)) {
            accessTokenKey = StringUtils.generateRandomAlphanumericString(50);
        }

        long milliseconds = 1000 * jHipsterProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSecondsForRememberMe();
        Instant expiredTime = Instant.now().plusMillis(milliseconds);

        TokenManager tokenManager;
        List<TokenManager> tokenManagerDB = tokenManagerRepository.findByLoginAndDeviceToken(username, deviceToken);
        if (tokenManagerDB.size() > 1) {
            tokenManagerRepository.deleteAll(tokenManagerDB);
            tokenManagerDB.clear();
        }
        tokenManager = tokenManagerDB.stream().findFirst().orElseGet(TokenManager::new);
        tokenManager.setLogin(username);
        tokenManager.setExpiredTime(expiredTime);
        tokenManager.setAccessTokenKey(accessTokenKey);
        tokenManager.setAccessToken(jwt);
        tokenManager.setRefreshToken(refreshToken);
        tokenManager.setDeviceToken(deviceToken);
        tokenManager.setDeviceName(deviceName);
        tokenManager.setDeviceType(deviceType);
        tokenManager.setDeviceInfo(deviceInfo);
        tokenManagerRepository.save(tokenManager);

        if (Objects.nonNull(username) && Objects.nonNull(deviceToken)) {
            saveDeviceToken(username, deviceToken);
        }
    }

    private void saveDeviceToken(String login, String deviceToken) {
        Optional<User> user = userRepository.findOneByLogin(login);
        if (user.isPresent()) {
            UUID userId = user.get().getId();
            var currentDeviceToken = deviceTokenRepository.findByTokenAndUserId(deviceToken, userId);
            if (currentDeviceToken.isEmpty()) {
                var deviceTokenBuilder = DeviceToken.builder().userId(userId).token(deviceToken).build();
                deviceTokenRepository.save(deviceTokenBuilder);
            }
        }
    }

    public ResponseTokenRefresh refreshToken(RequestTokenRefresh request) {
        return tokenManagerRepository
            .findByRefreshToken(request.getRefreshToken())
            .map(this::verifyExpiration)
            .map(this::createRefreshToken)
            .orElseThrow(() -> new TokenRefreshException(MessageHelper.getMessage(Message.Keys.E0030)));
    }

    public TokenManager verifyExpiration(TokenManager token) {
        if (DateUtils.compareInstant(token.getExpiredTime(), Instant.now())) {
            tokenManagerRepository.delete(token);
            tokenManagerRepository.flush();
            throw new TokenRefreshException(MessageHelper.getMessage(Message.Keys.E0013));
        }
        return token;
    }

    @Transactional
    public int signOut() {
        // Get header
        HttpServletRequest request = HeaderUtils.getCurrentRequest();
        String deviceToken = HeaderUtils.getHeader(request, HeaderEnum.X_DEVICE_TOKEN);

        var loginOptional = SecurityUtils.getCurrentUserLogin();
        if (loginOptional.isPresent()) {
            var loginEmail = loginOptional.get();
            if (deviceToken != null) {
                removeDeviceToken(loginEmail, deviceToken);
                return tokenManagerRepository.deleteByLoginAndDeviceToken(loginEmail, deviceToken);
            }
            return tokenManagerRepository.deleteByLogin(loginEmail);
        }
        throw new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Device Token"));
    }

    private void removeDeviceToken(String login, String deviceToken) {
        var user = userRepository.findOneByLogin(login);
        user.ifPresent(value -> deviceTokenRepository.deleteTokenDevice(value.getId(), deviceToken));
    }

    private ResponseTokenRefresh createRefreshToken(TokenManager tokenManager) {
        var userDetails = domainUserDetailsService.loadUserByUsername(tokenManager.getLogin());
        var authentication = tokenProvider.getAuthentication(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.createToken(authentication, false);
        tokenManager.setAccessToken(token);
        return ResponseTokenRefresh.builder().accessToken(token).build();
    }

    public void validateLoginWithPortalUrl(String userName, UUID portalId, boolean isEmailLogin) {
        validatePortalIsActive(portalId);
        if (portalHostRepository.existsByPortalIdAndEmail(portalId, userName)) {
            return;
        }
        Optional<User> userOpt = userRepository.findOneByEmailIgnoreCase(userName);
        if (userOpt.isPresent()) {
            UUID userId = userOpt.get().getId();
            boolean hasValidRole =
                technicalAdvisorRepository.existsByUserIdAndPortalId(userId, portalId) ||
                businessOwnerRepository.existsByPortalIdAndUserId(portalId, userId) ||
                userRepository.existsMemberCommunityPartnerByUserIdAndPortalId(userId, portalId);

            if (hasValidRole) {
                return;
            }
        }
        var messageKey = isEmailLogin ? Message.Keys.E0009 : Message.Keys.E0099;
        throw new BadRequestException(MessageHelper.getMessage(messageKey));
    }

    public void validateLoginWithAdminUrl(String userName, boolean isEmailLogin) {
        Optional<User> userOpt = userRepository.findOneByEmailIgnoreCase(userName);
        boolean isSystemAdmin = userOpt
            .map(user -> user.getAuthorities().stream().anyMatch(auth -> AuthoritiesConstants.SYSTEM_ADMINISTRATOR.equals(auth.getName())))
            .orElse(false);
        if (!isSystemAdmin) {
            var messageKey = isEmailLogin ? Message.Keys.E0009 : Message.Keys.E0099;
            throw new BadRequestException(MessageHelper.getMessage(messageKey));
        }
    }

    private void validatePortalIsActive(UUID portalId) {
        portalRepository
            .findByIdAndStatus(portalId, PortalStatusEnum.ACTIVE)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0017, "Portal")));
    }

    /**
     * Handle with case first login
     *
     * @param user User
     */
    private void handleFirstLogin(User user) {
        PortalContext portalContext = PortalContextHolder.getContext();
        String COMMUNITY_BOARD_URL = "/#/community-boards/posts";

        if (!Objects.equals(user.getFirstLoggedIn(), Boolean.TRUE) && !portalContext.isAdminAccess()) {
            user.setFirstLoggedIn(true);

            String communityBoardUrl = portalContext.getUrl() + COMMUNITY_BOARD_URL;
            String senderFirstName = portalContext.getSenderFirstName();
            String portalName = portalContext.getPlatformName();
            String userFirstName = user.getFirstName();

            String welcomeMessage = portalContext.getWelcomeMessage();
            welcomeMessage = welcomeMessage
                .replace("(link)", Objects.toString(communityBoardUrl, ""))
                .replace("{Sender First Name}", Objects.toString(senderFirstName, ""))
                .replace("{Portal Name}", Objects.toString(portalName, ""))
                .replace("{User First Name}", Objects.toString(userFirstName, ""));

            var request = new RequestSendMessageToConversation();
            request.setSenderId(portalContext.getUserSendMessageId());
            request.setMessage(welcomeMessage);
            request.setUserIds(List.of(user.getId(), portalContext.getUserSendMessageId()));

            directMessageService.sendMessageToConversation(request);
        }
    }
}
