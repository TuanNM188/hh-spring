package com.formos.huub.service.tokenmanager;

import com.formos.huub.config.Constants;
import com.formos.huub.domain.request.authenticate.SignInRequest;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import org.springframework.security.core.Authentication;

public interface OAuth2LoginStrategy {
    default Authentication authenticate(SignInRequest requestLogin) {
        if (
            !Constants.EMAIL_LOGIN.equalsIgnoreCase(requestLogin.getProvider()) &&
            (requestLogin.getAccessToken() == null || requestLogin.getAccessToken().isBlank())
        ) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0097));
        }
        return doAuthenticate(requestLogin);
    }

    Authentication doAuthenticate(SignInRequest requestLogin);
}
