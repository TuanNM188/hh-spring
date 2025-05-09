package com.formos.huub.domain.request.invitemember;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestInviteMember {

    @RequireCheck
    @Size(max = 50)
    String firstName;

    @RequireCheck
    @Size(max = 50)
    String lastName;

    @RequireCheck
    @Size(max = 50)
    @Email
    String email;

    private String inviteToken;

    private UUID communityPartnerId;

    private UUID portalId;

    private String invitationOrganization;

    private List<UUID> portalIds;

    private Boolean isNavigator;

    private Boolean isPrimary;

    public String toFullName() {
        return String.join(" ", this.firstName, this.lastName);
    }
}
