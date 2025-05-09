package com.formos.huub.service.tokenmanager.provider;

import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.AuthProviderEnum;
import com.formos.huub.domain.request.authenticate.SignInRequest;
import com.formos.huub.domain.response.account.ResponseSocialUserInfo;
import com.formos.huub.security.DomainUserDetailsService;
import com.formos.huub.service.tokenmanager.OAuth2LoginStrategy;
import com.formos.huub.service.usersociallink.UserSocialLinkService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class FacebookLoginStrategy implements OAuth2LoginStrategy {

    private DomainUserDetailsService domainUserDetailsService;
    private UserSocialLinkService userSocialLinkService;

    @Override
    public Authentication doAuthenticate(SignInRequest requestLogin) {
        ResponseSocialUserInfo fbUser = userSocialLinkService.getSocialUserInfo(AuthProviderEnum.FACEBOOK, requestLogin.getAccessToken());
        User user = userSocialLinkService.handleUserBySocialAccount(AuthProviderEnum.FACEBOOK, fbUser.getId(), fbUser.getEmail(), requestLogin.getAccessToken());
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(user.getEmail());
        return new UsernamePasswordAuthenticationToken(user.getEmail(), null, userDetails.getAuthorities());
    }
}
