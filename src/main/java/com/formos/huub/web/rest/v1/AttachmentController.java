/**
 * ***************************************************
 * * Description :
 * * File        : AttachmentController
 * * Author      : Hung Tran
 * * Date        : Feb 07, 2025
 * ***************************************************
 **/
package com.formos.huub.web.rest.v1;

import static com.formos.huub.framework.enums.FileTypeEnum.*;
import static com.formos.huub.framework.enums.FileTypeEnum.CVS;

import com.formos.huub.domain.enums.ResourceFileTypeEnum;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.FileMaxSizeCheck;
import com.formos.huub.framework.validation.constraints.FileRequireCheck;
import com.formos.huub.framework.validation.constraints.FileTypeCheck;
import com.formos.huub.service.attachment.AttachmentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attachments")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttachmentController {

    ResponseSupport responseSupport;
    AttachmentService attachmentService;

    private static final String APPOINTMENT_REPORT_ATTACHMENT_KEY = "appointmentReport/attachment/";
    private static final String PROJECT_REPORT_ATTACHMENT_KEY = "projectReport/attachment/";
    private static final String PROJECT_ATTACHMENT_KEY = "project/attachment/";
    private static final String REPORT_ATTACHMENT_KEY = "report/attachment/";
    private static final String CLIENT_NOTE_ATTACHMENT_KEY = "clientNote/attachment/";

    @PreAuthorize("hasPermission(null, 'UPLOAD_APPOINTMENT_REPORT_ATTACHMENT')")
    @PostMapping(value = "/appointment-report", consumes = { "multipart/form-data" })
    public ResponseEntity<ResponseData> uploadFileAppointmentReport(
        @RequestPart(value = "file") @Valid @FileRequireCheck @FileMaxSizeCheck(max = 25) MultipartFile file
    ) {
        var response = attachmentService.uploadAttachment(ResourceFileTypeEnum.FILES, file, APPOINTMENT_REPORT_ATTACHMENT_KEY);
        ResponseData responseData = ResponseData.builder().data(response).build();
        return responseSupport.success(responseData);
    }

    @PreAuthorize("hasPermission(null, 'UPLOAD_PROJECT_REPORT_ATTACHMENT')")
    @PostMapping(value = "/project-report", consumes = { "multipart/form-data" })
    public ResponseEntity<ResponseData> uploadFileProjectReport(
        @RequestPart(value = "file") @Valid @FileRequireCheck @FileMaxSizeCheck(max = 25) @FileTypeCheck(
            allowedFileTypes = { PDF, DOC, DOCX, XLS, XLSX, PPT, JPG, PNG, TXT, CVS, CSV }
        ) MultipartFile file
    ) {
        var response = attachmentService.uploadAttachment(ResourceFileTypeEnum.FILES, file, PROJECT_REPORT_ATTACHMENT_KEY);
        ResponseData responseData = ResponseData.builder().data(response).build();
        return responseSupport.success(responseData);
    }

    @PreAuthorize("hasPermission(null, 'UPLOAD_PROJECT_ATTACHMENT')")
    @PostMapping(value = "/project", consumes = { "multipart/form-data" })
    public ResponseEntity<ResponseData> uploadFileProject(
        @RequestPart(value = "file") @Valid @FileRequireCheck @FileMaxSizeCheck(max = 25) @FileTypeCheck(
            allowedFileTypes = { PDF, DOC, DOCX, XLS, XLSX, PPT, JPG, PNG, TXT, CVS, CSV }
        ) MultipartFile file
    ) {
        var response = attachmentService.uploadAttachment(ResourceFileTypeEnum.FILES, file, PROJECT_ATTACHMENT_KEY);
        ResponseData responseData = ResponseData.builder().data(response).build();
        return responseSupport.success(responseData);
    }

    @PreAuthorize("hasPermission(null, 'UPLOAD_OUTSIDE_REPORT_ATTACHMENT')")
    @PostMapping(value = "/outside-report", consumes = { "multipart/form-data" })
    public ResponseEntity<ResponseData> uploadFileReport(
        @RequestPart(value = "file") @Valid @FileRequireCheck @FileMaxSizeCheck(max = 25) @FileTypeCheck(
            allowedFileTypes = { PDF }
        ) MultipartFile file
    ) {
        var response = attachmentService.uploadAttachment(ResourceFileTypeEnum.FILES, file, REPORT_ATTACHMENT_KEY);
        ResponseData responseData = ResponseData.builder().data(response).build();
        return responseSupport.success(responseData);
    }

    @PostMapping(value = "/client-note", consumes = { "multipart/form-data" })
    public ResponseEntity<ResponseData> uploadFileClientNote(
        @RequestPart(value = "file") @Valid @FileRequireCheck @FileMaxSizeCheck(max = 25) @FileTypeCheck(
            allowedFileTypes = { PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, JPG, PNG, TXT, CVS, CSV }
        ) MultipartFile file
    ) {
        var response = attachmentService.uploadAttachment(ResourceFileTypeEnum.FILES, file, CLIENT_NOTE_ATTACHMENT_KEY);
        ResponseData responseData = ResponseData.builder().data(response).build();
        return responseSupport.success(responseData);
    }
}
