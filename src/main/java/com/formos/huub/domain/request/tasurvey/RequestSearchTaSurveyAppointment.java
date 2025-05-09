package com.formos.huub.domain.request.tasurvey;

import com.formos.huub.domain.request.common.SearchConditionRequest;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestSearchTaSurveyAppointment extends SearchConditionRequest {

    private String searchKeyword;
    private UUID portalId;
    private UUID communityPartnerId;
    private List<UUID> portalIds;

}
