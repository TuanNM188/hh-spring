package com.formos.huub.framework.service.storage;

import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.service.exception.AmazonClientException;
import com.formos.huub.framework.service.exception.AmazonServiceException;
import com.formos.huub.framework.service.exception.DownloadException;
import com.formos.huub.framework.service.exception.UploadException;
import com.formos.huub.framework.service.storage.model.CloudProperties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

import com.formos.huub.framework.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Primary
@Service
@Slf4j
@RequiredArgsConstructor
public class S3StorageService implements IStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final CloudProperties config;

    /**
     * Upload file using amazon S3 client from S3 bucket
     *
     * @param keyName String
     * @param file MultipartFile
     * @return String
     */
    public String uploadFile(String keyName, MultipartFile file, boolean isPublic) {
        try {
            return uploadToS3(keyName, file.getInputStream(), file.getSize(), file.getContentType(), isPublic);
        } catch (IOException e) {
            log.error("Error reading MultipartFile", e);
            throw new UploadException("Error while reading file for upload", e);
        }
    }

    /**
     * Upload object to S3
     *
     * @param bucketName String
     * @param keyName String
     * @param inputStream InputStream
     * @param contentLength long
     * @param contentType String
     * @param isPublic boolean
     */
    private void uploadObjectToS3(String bucketName, String keyName, InputStream inputStream, long contentLength, String contentType, boolean isPublic) {
        var putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(keyName).contentType(contentType);
        if (isPublic) {
            putObjectRequest.acl(ObjectCannedACL.PUBLIC_READ);
        }
        s3Client.putObject(putObjectRequest.build(), RequestBody.fromInputStream(inputStream, contentLength));
    }

    /**
     *  Upload To S3
     *
     * @param keyName String
     * @param inputStream InputStream
     * @param contentLength long
     * @param contentType String
     * @param isPublic boolean
     * @return String
     */
    private String uploadToS3(String keyName, InputStream inputStream, long contentLength, String contentType, boolean isPublic) {
        try {
            String key = FileUtils.standardizedFileKey(keyName);
            uploadObjectToS3(config.getBucket().getName(), key, inputStream, contentLength, contentType, isPublic);
            return isPublic ? key : keyName;
        } catch (SdkServiceException e) {
            log.error("AWS service error during upload", e);
            throw new AmazonServiceException(e);
        } catch (SdkClientException e) {
            log.error("AWS client error during upload", e);
            throw new AmazonClientException(e);
        }
    }

    /**
     * Upload File PDF
     *
     * @param keyName String
     * @param bytes byte[]
     * @return String
     */
    public String uploadFile(String keyName, byte[] bytes) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        return uploadToS3(keyName, inputStream, bytes.length, "application/pdf", true);
    }

    /**
     * Get url from S3 client
     * @param keyName String
     * @return String
     */
    public String getUrlFromS3Client(String keyName) {
        final URL reportUrl = s3Client
            .utilities()
            .getUrl(GetUrlRequest.builder().bucket(config.getBucket().getName()).key(keyName).build());
        return reportUrl.toString();
    }

    /**
     * Generate url download file
     * @param keyName String
     * @return String
     */
    public String getPreSignedUrl(String keyName) {
        String bucketName = config.getBucket().getName();
        long duration = 5;

        // Check file exist
        boolean fileExist = checkKeyExist(keyName);
        if (!fileExist) {
            log.error("File not found");
            throw new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "File"));
        }

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(duration))
            .getObjectRequest(request -> request
                .bucket(bucketName)
                .key(keyName))
            .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        return presignedGetObjectRequest.url().toString();
    }

    /**
     * Check key exist
     * @param keyName String
     * @return boolean
     */
    private boolean checkKeyExist(String keyName){
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(config.getBucket().getName())
                .key(keyName)
                .build();
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (S3Exception e) {
            log.error(e.awsErrorDetails().errorMessage());
            return false;
        }
    }

    /**
     * Downloads file using amazon S3 client from S3 bucket
     *
     * @param keyName String
     * @return ByteArrayOutputStream
     */
    @Override
    public ByteArrayOutputStream download(String keyName) {
        try {
            byte[] bytes = getObject(keyName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
            baos.writeBytes(bytes);
            return baos;
        } catch (AmazonServiceException e) {
            log.error(e.toString());
            throw new DownloadException("Error while downloading object from Amazon service", e);
        } catch (AmazonClientException e) {
            log.error(e.toString());
            throw new DownloadException("Error while downloading object from Amazon client", e);
        }
    }

    /**
     * Get object from S3
     * @param keyName String
     * @return byte[]
     */
    public byte[] getObject(String keyName) {
        try {
            log.info("Retrieving file from S3 for key: {}/{}", config.getBucket().getName(), keyName);
            ResponseBytes<GetObjectResponse> s3Object = s3Client.getObject(
                GetObjectRequest.builder().bucket(config.getBucket().getName()).key(keyName).build(),
                ResponseTransformer.toBytes()
            );
            final byte[] bytes = s3Object.asByteArray();
            return bytes;
        } catch (SdkClientException e) {
            log.error(e.toString());
            throw new AmazonClientException(e);
        } catch (SdkServiceException e) {
            log.error(e.toString());
            throw new AmazonServiceException(e);
        }
    }
}
