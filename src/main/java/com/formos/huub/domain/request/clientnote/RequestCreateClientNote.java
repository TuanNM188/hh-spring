package com.formos.huub.domain.request.clientnote;

import com.formos.huub.domain.request.common.RequestAttachmentFile;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestCreateClientNote {

    @UUIDCheck
    private String userId;

    private String note;

    private List<RequestAttachmentFile> attachments;
}
