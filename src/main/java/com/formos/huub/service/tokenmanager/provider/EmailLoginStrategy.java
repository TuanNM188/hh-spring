package com.formos.huub.service.tokenmanager.provider;

import com.formos.huub.domain.request.authenticate.SignInRequest;
import com.formos.huub.service.tokenmanager.OAuth2LoginStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
@Slf4j
public class EmailLoginStrategy implements OAuth2LoginStrategy {

    AuthenticationManagerBuilder authenticationManagerBuilder;

    @Override
    public Authentication doAuthenticate(SignInRequest requestLogin) {
        log.info("Request to sign in: {}", requestLogin);
        String username = requestLogin.getUsername();
        String password = requestLogin.getPassword();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    }
}
