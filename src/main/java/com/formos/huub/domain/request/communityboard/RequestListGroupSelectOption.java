package com.formos.huub.domain.request.communityboard;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UUID;

@Getter
@Setter
public class RequestListGroupSelectOption {

    @UUID
    private String portalId;
}
