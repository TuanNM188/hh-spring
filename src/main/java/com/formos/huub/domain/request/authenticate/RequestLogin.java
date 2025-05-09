package com.formos.huub.domain.request.authenticate;

import com.formos.huub.framework.base.BaseDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;



/**
 * View Model object for storing a user's credentials.
 */
@Getter
@Setter
public class RequestLogin extends BaseDTO {

    @NotNull
    @Email
    @Size(min = 1, max = 50)
    private String username;

    @NotNull
    @Size(min = 4, max = 100)
    private String password;

    private boolean rememberMe;

    // prettier-ignore
    @Override
    public String toString() {
        return "LoginRequest{" +
            "username='" + username + '\'' +
            ", rememberMe=" + rememberMe +
            '}';
    }
}
