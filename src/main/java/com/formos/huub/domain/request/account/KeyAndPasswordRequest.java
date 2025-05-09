package com.formos.huub.domain.request.account;

import com.formos.huub.framework.validation.constraints.PasswordValid;
import lombok.Getter;
import lombok.Setter;

/**
 * View Model object for storing the user's key and password.
 */
@Setter
@Getter
public class KeyAndPasswordRequest {

    private String key;

    @PasswordValid
    private String newPassword;
}
