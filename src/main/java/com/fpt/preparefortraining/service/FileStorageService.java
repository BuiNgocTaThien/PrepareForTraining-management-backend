package com.fpt.preparefortraining.service;

import java.io.InputStream;

public interface FileStorageService {
    String uploadFile(String objectName, InputStream inputStream, String contentType, long size);
    InputStream downloadFile(String objectName);
    void deleteFile(String objectName);
}
