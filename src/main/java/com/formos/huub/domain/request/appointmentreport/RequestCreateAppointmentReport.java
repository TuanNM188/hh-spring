package com.formos.huub.domain.request.appointmentreport;

import com.formos.huub.domain.request.common.RequestAttachmentFile;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import java.util.List;
import lombok.*;

@Getter
@Setter
public class RequestCreateAppointmentReport {

    @UUIDCheck
    private String appointmentId;

    @RequireCheck
    private String description;

    private List<String> serviceOutcomes;

    @RequireCheck
    private String feedback;

    private List<RequestAttachmentFile> attachments;
}
