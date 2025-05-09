/**
 * ***************************************************
 * * Description :
 * * File        : AttachmentService
 * * Author      : Hung Tran
 * * Date        : Feb 07, 2025
 * ***************************************************
 **/
package com.formos.huub.service.attachment;

import com.formos.huub.domain.enums.ResourceFileTypeEnum;
import com.formos.huub.service.file.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttachmentService {

    FileService fileService;

    /**
     * upload attachment
     *
     * @param multipartFile MultipartFile
     * @return String
     */
    public String uploadAttachment(ResourceFileTypeEnum fileType, MultipartFile multipartFile, String key) {
        String fileKey = key.concat(fileType.getName());
        return fileService.uploadFile(multipartFile, fileKey);
    }
}
