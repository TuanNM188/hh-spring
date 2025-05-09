package com.formos.huub.domain.request.account;

import com.formos.huub.domain.dto.AdminUserDTO;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * View Model extending the AdminUserDTO, which is meant to be used in the user management UI.
 */
@Setter
@Getter
@NoArgsConstructor
public class SignUpUserRequest extends AdminUserDTO {

    public static final int PASSWORD_MIN_LENGTH = 4;

    public static final int PASSWORD_MAX_LENGTH = 100;

    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String password;

    // prettier-ignore
    @Override
    public String toString() {
        return "SignUpUserRequest{" + super.toString() + "} ";
    }
}
