package com.formos.huub.service.usersociallink;

import com.formos.huub.domain.constant.FacebookConstant;
import com.formos.huub.domain.constant.GoogleConstant;
import com.formos.huub.domain.constant.MicrosoftConstant;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.entity.UserSocialLink;
import com.formos.huub.domain.enums.AuthProviderEnum;
import com.formos.huub.domain.request.account.RequestLinkSocialAccount;
import com.formos.huub.domain.response.account.ResponseSocialUserInfo;
import com.formos.huub.domain.response.account.ResponseUserSocialLink;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.context.PortalContext;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.helper.member.MemberHelper;
import com.formos.huub.mapper.usersociallink.UserSocialLinkMapper;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.repository.UserSocialLinkRepository;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.resttemplate.RestTemplateService;
import com.google.gson.Gson;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserSocialLinkService extends BaseService {

    UserSocialLinkRepository userSocialLinkRepository;

    RestTemplateService restTemplateService;

    UserRepository userRepository;

    UserSocialLinkMapper userSocialLinkMapper;

    MemberHelper memberHelper;

    private static final String ACCESS_TOKEN = "{accessToken}";

    /**
     * Link Social Account
     *
     * @param request RequestLinkSocialAccount
     * @param userId UUID
     * @return List<ResponseUserSocialLink>
     */
    public List<ResponseUserSocialLink> linkSocialAccount(RequestLinkSocialAccount request, UUID userId) {
        User user = Objects.isNull(userId) ? getCurrentUser() : memberHelper.getUserById(userId);
        AuthProviderEnum provider = AuthProviderEnum.valueOf(request.getProvider());
        ResponseSocialUserInfo socialUserInfo = getSocialUserInfo(provider, request.getAccessToken());

        if (Objects.isNull(socialUserInfo)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0045));
        }

        String providerUserId = provider.equals(AuthProviderEnum.GOOGLE) ? socialUserInfo.getSub() : socialUserInfo.getId();

        userSocialLinkRepository
            .findByProviderAndProviderUserId(provider, providerUserId)
            .ifPresent(userSocialLink -> {
                throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0044, request.getProvider()));
            });

        Gson gson = new Gson();
        String attributes = gson.toJson(socialUserInfo);

        UserSocialLink userSocialLink = UserSocialLink.builder()
            .userId(user.getId())
            .provider(provider)
            .providerUserId(providerUserId)
            .attributes(attributes)
            .build();
        userSocialLinkRepository.save(userSocialLink);

        return getListUserSocialLinkByUserId(user.getId());
    }

    /**
     * Link Social Account Manually
     *
     * @param request RequestLinkSocialAccount
     * @return List<ResponseUserSocialLink>
     */
    public List<ResponseUserSocialLink> linkSocialAccount(RequestLinkSocialAccount request) {
        return linkSocialAccount(request, null);
    }

    /**
     * Get Social User Info
     *
     * @param provider AuthProviderEnum
     * @param accessToken String
     * @return ResponseSocialUserInfo
     */
    public ResponseSocialUserInfo getSocialUserInfo(AuthProviderEnum provider, String accessToken) {
        HttpHeaders headers = AuthProviderEnum.MICROSOFT.equals(provider) ? initializeHeaders(accessToken) : new HttpHeaders();
        String url = buildSocialUserInfoUrl(provider, accessToken);
        ResponseEntity<ResponseSocialUserInfo> responseEntity = restTemplateService.sendGetRequest(
            url,
            headers,
            ResponseSocialUserInfo.class
        );

        if (!responseEntity.getStatusCode().is2xxSuccessful() || Objects.isNull(responseEntity.getBody())) {
            log.warn("Failed to retrieve social user info from provider: {}", provider);
            return null;
        }
        return responseEntity.getBody();
    }

    /**
     * Handle User By Social Account
     *
     * @param provider AuthProviderEnum
     * @param providerId String
     * @param email String
     * @param accessToken String
     * @return User
     */
    public User handleUserBySocialAccount(AuthProviderEnum provider, String providerId, String email, String accessToken) {
        UserSocialLink userSocial = userSocialLinkRepository
            .findByProviderAndProviderUserId(provider, providerId)
            .orElse(null);

        if (Objects.nonNull(userSocial)) {
            return userRepository.findById(userSocial.getUserId())
                .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User")));
        }

        Optional<User> userOpt = userRepository.findOneByEmailIgnoreCase(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            RequestLinkSocialAccount linkSocialAccount = RequestLinkSocialAccount.builder()
                .provider(provider.name())
                .accessToken(accessToken)
                .build();

            linkSocialAccount(linkSocialAccount, user.getId());

            return user;
        }

        PortalContext portalContext = PortalContextHolder.getContext();
        var messageKey = portalContext.isAdminAccess() ? Message.Keys.E0099 : Message.Keys.E0098;
        throw new BadRequestException(MessageHelper.getMessage(messageKey));
    }

    /**
     * initialize Headers
     *
     * @param accessToken String
     * @return HttpHeaders
     */
    public HttpHeaders initializeHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Build Social User Info Url
     *
     * @param provider AuthProviderEnum
     * @param accessToken String
     * @return String
     */
    private String buildSocialUserInfoUrl(AuthProviderEnum provider, String accessToken) {
        return switch (provider) {
            case FACEBOOK -> String.join("", FacebookConstant.BASE_URL, FacebookConstant.USER_INFO_URL).replace(ACCESS_TOKEN, accessToken);
            case GOOGLE -> String.join("", GoogleConstant.BASE_URL, GoogleConstant.USER_INFO_URL).replace(ACCESS_TOKEN, accessToken);
            case MICROSOFT -> String.join("", MicrosoftConstant.BASE_URL, MicrosoftConstant.USER_INFO_URL);
        };
    }

    /**
     * Unlink Social Account
     *
     * @param provider AuthProviderEnum
     * @return List<ResponseUserSocialLink>
     */
    public List<ResponseUserSocialLink> unlinkSocialAccount(AuthProviderEnum provider) {
        User user = getCurrentUser();
        userSocialLinkRepository.findByProviderAndUserId(provider, user.getId()).ifPresent(userSocialLinkRepository::delete);
        return getListUserSocialLinkByUserId(user.getId());
    }

    /**
     * Get List UserSocialLink By UserId
     *
     * @param userId UUID
     * @return List<ResponseUserSocialLink>
     */
    public List<ResponseUserSocialLink> getListUserSocialLinkByUserId(UUID userId) {
        List<UserSocialLink> userSocialLinks = userSocialLinkRepository.findByUserId(userId);
        return userSocialLinkMapper.toListResponse(userSocialLinks);
    }

    /**
     * Get current User
     *
     * @return User
     */
    private User getCurrentUser() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneWithAuthoritiesByLogin)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User")));
    }
}
