package com.formos.huub.domain.request.authenticate;

import com.formos.huub.framework.base.BaseDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * View Model object for storing a user's credentials.
 */
@Setter
@Getter
public class SignInRequest extends BaseDTO {

    @NotNull(groups = EmailLogin.class)
    @Size(min = 1, max = 50, groups = EmailLogin.class)
    private String username;

    @NotNull(groups = EmailLogin.class)
    @Size(min = 4, max = 100, groups = EmailLogin.class)
    private String password;

    private boolean rememberMe;

    private String language;

    @NotNull(groups = SocialLogin.class)
    private String provider;

    @NotNull(groups = SocialLogin.class)
    private String accessToken;

    // prettier-ignore
    @Override
    public String toString() {
        return "LoginVM{" +
            "username='" + username + '\'' +
            ", rememberMe=" + rememberMe +
            ", provider=" + provider +
            ", accessToken=" + accessToken +
            '}';
    }
}
