package com.formos.huub.domain.request.account;

import com.formos.huub.framework.validation.constraints.PasswordValid;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestUpdatePassword {

    @PasswordValid
    private String newPassword;

}
