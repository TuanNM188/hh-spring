/**
 * ***************************************************
 * * Description :
 * * File        : LocalStorageService
 * * Author      : Hung Tran
 * * Date        : Jan 09, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;

@Slf4j
@Service
@Qualifier(value = "LocalStorageService")
public class LocalStorageService implements IStorageService {

    @Override
    public String uploadFile(String keyName, MultipartFile file, boolean isPublic) {
        return "";
    }

    @Override
    public String uploadFile(String keyName, byte[] bytes) {
        return "";
    }

    @Override
    public String getPreSignedUrl(String keyName) {
        return "";
    }

    @Override
    public ByteArrayOutputStream download(String keyName) {
        return null;
    }

}
