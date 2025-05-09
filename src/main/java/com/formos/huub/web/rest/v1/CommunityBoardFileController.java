package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.enums.CommunityBoardFileTypeEnum;
import com.formos.huub.framework.base.BaseController;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.*;
import com.formos.huub.service.communityboard.CommunityBoardFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static com.formos.huub.framework.enums.FileTypeEnum.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community-board-media")
@Validated
public class CommunityBoardFileController extends BaseController {

    private final CommunityBoardFileService fileService;

    private final ResponseSupport responseSupport;

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<ResponseData> uploadFileCommunityBoardPost(
        @RequestPart(value = "file") @Valid @FileRequireCheck @FileMaxSizeCheck() @FileTypeCheck(
            allowedFileTypes = { PNG, JPEG, JPG, HEIF, HEIC, SVG, WEBP, BMP, ICO, MP4, MOV, AVI, PDF, DOC, DOCX, XLS, XLSX, TXT }
        ) MultipartFile file,
        @RequestPart(value = "type") @RequireCheck @EnumCheck(enumClass = CommunityBoardFileTypeEnum.class) String fileType
    ) {
        ResponseData responseData = ResponseData.builder().data(fileService.uploadFileCommunityBoard(file, fileType)).build();
        return responseSupport.success(responseData);
    }

    @GetMapping("/groups/{groupId}")
    @PreAuthorize("hasPermission(null, 'GET_FILES_IN_GROUP')")
    public ResponseEntity<ResponseData> findFileInGroup(
        @PathVariable @UUIDCheck String groupId,
        @RequestParam String type,
        Pageable pageable
    ) {
        ResponseData result = ResponseData.builder().data(fileService.findFilesInGroup(UUID.fromString(groupId), type, pageable)).build();
        return responseSupport.success(result);
    }

    @PostMapping(value = "/groups/{groupId}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasPermission(null, 'UPLOAD_FILE_IN_GROUP')")
    public ResponseEntity<ResponseData> uploadFile(
        @PathVariable @UUIDCheck String groupId,
        @RequestPart(value = "file") @Valid @FileRequireCheck @FileMaxSizeCheck() @FileTypeCheck(
            allowedFileTypes = { PNG, JPEG, JPG, HEIF, HEIC, SVG, WEBP, BMP, ICO, MP4, MOV, AVI, PDF, DOC, DOCX, XLS, XLSX, TXT }
        ) MultipartFile file,
        @RequestPart(value = "type") @RequireCheck @EnumCheck(enumClass = CommunityBoardFileTypeEnum.class) String fileType
    ) {
        var result = fileService.uploadFile(UUID.fromString(groupId), file, fileType);
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'DELETE_FILE_IN_GROUP')")
    public ResponseEntity<ResponseData> deleteFiles(@PathVariable @UUIDCheck String id) {
        fileService.deleteFile(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().build());
    }

}
