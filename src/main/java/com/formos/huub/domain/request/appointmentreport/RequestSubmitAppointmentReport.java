package com.formos.huub.domain.request.appointmentreport;

import com.formos.huub.domain.request.common.RequestAttachmentFile;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestSubmitAppointmentReport {

    @UUIDCheck
    private String appointmentId;

    private String description;

    private List<String> serviceOutcomes;

    private String feedback;

    private Boolean businessOwnerAttended;

    private List<RequestAttachmentFile> attachments;
}
