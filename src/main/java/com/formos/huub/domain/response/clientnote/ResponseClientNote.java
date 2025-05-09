package com.formos.huub.domain.response.clientnote;

import com.formos.huub.domain.response.attachment.ResponseAttachment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseClientNote {
    private UUID id;
    private String note;
    private List<ResponseAttachment> attachments;
}
