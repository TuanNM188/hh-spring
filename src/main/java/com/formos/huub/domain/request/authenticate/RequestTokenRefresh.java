package com.formos.huub.domain.request.authenticate;

import com.formos.huub.framework.base.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestTokenRefresh extends BaseDTO {

    @NotBlank
    String refreshToken;
}
