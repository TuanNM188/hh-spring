package com.formos.huub.domain.response.communitypartner;

import com.formos.huub.domain.enums.UserStatusEnum;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseExistsCommunityPartnerStaff {

    private Boolean isExist;

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private UserStatusEnum status;

    private String normalizedFullName;

    private String imageUrl;

    private String communityPartnerName;

}
