/**
 * ***************************************************
 * * Description :
 * * File        : IStorageService
 * * Author      : Hung Tran
 * * Date        : Dec 22, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;

public interface IStorageService {

    String uploadFile(final String keyName, final MultipartFile file, boolean isPublic);

    String uploadFile(final String keyName, final byte[] bytes);

    String getPreSignedUrl(String keyName);

    ByteArrayOutputStream download(final String keyName);

}
