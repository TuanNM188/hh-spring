package com.formos.huub.web.rest.v1;

import com.formos.huub.framework.base.BaseController;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.FileUtils;
import com.formos.huub.framework.validation.constraints.FileMaxSizeCheck;
import com.formos.huub.framework.validation.constraints.FileRequireCheck;
import com.formos.huub.framework.validation.constraints.FileTypeCheck;
import com.formos.huub.service.file.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;

import static com.formos.huub.framework.enums.FileTypeEnum.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
@Validated
public class FileController extends BaseController {

    private final FileService fileService;

    private final ResponseSupport responseSupport;

    @PostMapping(value = "/avatar", consumes = { "multipart/form-data" })
    @PreAuthorize("hasPermission(null, 'UPLOAD_AVATAR')")
    public ResponseEntity<ResponseData> createAvatar(
        @RequestPart(value = "file") @Valid @FileRequireCheck @FileMaxSizeCheck() @FileTypeCheck(
            allowedFileTypes = { PNG, JPEG, JPG, SVG, HEIF, HEIC, SVG, WEBP, BMP, ICO }
        ) MultipartFile file
    ) {
        ResponseData responseData = ResponseData.builder().data(fileService.uploadAvatar(file)).build();
        return responseSupport.success(responseData);
    }

    @PostMapping(value = "/logo", consumes = { "multipart/form-data" })
    @PreAuthorize("hasPermission(null, 'UPLOAD_LOGO')")
    public ResponseEntity<ResponseData> createLogo(
        @RequestPart(value = "file") @Valid @FileRequireCheck @FileMaxSizeCheck() @FileTypeCheck(
            allowedFileTypes = { PNG, JPEG, JPG, SVG, HEIF, HEIC, SVG, WEBP, BMP, ICO }
        ) MultipartFile file
    ) {
        ResponseData responseData = ResponseData.builder().data(fileService.uploadLogo(file)).build();
        return responseSupport.success(responseData);
    }

    @PostMapping(value = "/conversation", consumes = { "multipart/form-data" })
    public ResponseEntity<ResponseData> uploadConversation(
        @RequestPart(value = "file") @Valid @FileRequireCheck @FileMaxSizeCheck() @FileTypeCheck(
            allowedFileTypes = { PNG, JPEG, JPG, SVG, HEIF, HEIC, SVG, WEBP, BMP, ICO }
        ) MultipartFile file
    ) {
        ResponseData responseData = ResponseData.builder().data(fileService.uploadImageConversation(file)).build();
        return responseSupport.success(responseData);
    }

    @GetMapping(value = "/pre-signed-url")
    public ResponseEntity<ResponseData> getPreSignedUrl(@RequestParam String keyName) {
        return responseSupport.success(ResponseData.builder().data(fileService.getPreSignedUrl(keyName)).build());
    }

    @GetMapping(value = "/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam String keyName) {
        ByteArrayOutputStream downloadInputStream = fileService.download(keyName);
        return ResponseEntity
            .ok()
            .contentType(FileUtils.contentType(keyName))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileService.getFileName(keyName) + "\"")
            .body(downloadInputStream.toByteArray());
    }
}
