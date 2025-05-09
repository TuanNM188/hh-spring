package com.formos.huub.domain.response.clientnote;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseSearchClientNote implements Serializable {
    private UUID id;

    private String note;

    private Instant createdDate;

    private String hasAttachments;

    private String createdBy;

}
