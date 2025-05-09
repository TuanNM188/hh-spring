package com.formos.huub.service.file;

import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.service.storage.IStorageService;
import com.formos.huub.framework.utils.FileUtils;
import com.formos.huub.framework.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FileService extends BaseService {

    private final IStorageService storageService;

    /**
     * Upload avatar
     *
     * @param multipartFile MultipartFile
     * @return String
     */
    public String uploadAvatar(MultipartFile multipartFile) {
        return uploadFile(multipartFile, "Avatar");
    }


    /**
     * Upload logo
     *
     * @param multipartFile MultipartFile
     * @return String
     */
    public String uploadLogo(MultipartFile multipartFile) {
        return uploadFile(multipartFile, "Logo");
    }

    /**
     * Upload file
     *
     * @param multipartFile MultipartFile
     * @param fileType      String
     * @return String
     */
    public String uploadFile(MultipartFile multipartFile, String fileType, boolean isPublic) {
        // Generate a unique filename for the uploaded file
        String name = FileUtils.makeFileNameWithTime(multipartFile.getOriginalFilename());
        String id = UUID.randomUUID().toString();
        String keyName = String.format("%s/%s/%s", fileType, id, name);      // Upload the file to the storage service
        String url = storageService.uploadFile(keyName, multipartFile, isPublic);

        if (StringUtils.isBlank(url)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Failed to upload " + fileType.toLowerCase()));
        }
        return url;
    }

    public String uploadFile(MultipartFile multipartFile, String fileType) {
        return uploadFile(multipartFile, fileType, true);
    }

    public String uploadPrivateFile(MultipartFile multipartFile, String fileType) {
        return uploadFile(multipartFile, fileType, false);
    }

    public ByteArrayOutputStream download(String fileName) {
        String key = FileUtils.standardizedFileKey(fileName);
        return storageService.download(key);
    }

    public String getFileName(String keyPath) {
        String[] fileArrSplit = keyPath.split("\\.");
        String fileExtension = fileArrSplit[fileArrSplit.length - 1];

        String[] fileArrNames = keyPath.split("-");
        String fileName = fileArrNames[0].split("/")[1];
        return String.format("%s.%s", fileName, fileExtension);
    }

    public String uploadPdfFile(byte[] bytes, String fileType, String fileName) {
        String id = UUID.randomUUID().toString();
        String keyName = String.format("%s/%s/%s", fileType, id, fileName);      // Upload the file to the storage service
        String url = storageService.uploadFile(keyName, bytes);
        if (StringUtils.isBlank(url)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Failed to upload " + fileType.toLowerCase()));
        }
        return url;
    }

    public String getPreSignedUrl(String keyName) {
        String key = FileUtils.standardizedFileKey(keyName);
        return storageService.getPreSignedUrl(key);
    }

    /**
     *
     * @param multipartFile
     * @return
     */
    public String uploadImageConversation(MultipartFile multipartFile) {
        return uploadFile(multipartFile, "conversation");
    }

}
