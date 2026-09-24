package com.fpt.preparefortraining.service;

import com.fpt.preparefortraining.config.MinioProperties;
import com.fpt.preparefortraining.exception.InternalServerException;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import org.springframework.stereotype.Service;

@Service
public class MinioStorageServiceImpl implements FileStorageService {

  private final MinioClient minioClient;
  private final MinioProperties properties;

  public MinioStorageServiceImpl(MinioClient minioClient, MinioProperties properties) {
    this.minioClient = minioClient;
    this.properties = properties;
  }

  @PostConstruct
  public void init() {
    try {
      boolean found =
          minioClient.bucketExists(
              BucketExistsArgs.builder().bucket(properties.getBucketName()).build());
      if (!found) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucketName()).build());
      }
    } catch (Exception e) {
      // Allow the application to start even if MinIO is not immediately available
      System.err.println("Warning: MinIO is not available on startup - " + e.getMessage());
    }
  }

  @Override
  public String uploadFile(String objectName, InputStream inputStream, String contentType, long size) {
    try {
      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(properties.getBucketName())
              .object(objectName)
              .stream(inputStream, size, -1)
              .contentType(contentType)
              .build());
      return objectName;
    } catch (Exception e) {
      throw new InternalServerException("Failed to upload file to MinIO: " + e.getMessage());
    }
  }

  @Override
  public InputStream downloadFile(String objectName) {
    try {
      return minioClient.getObject(
          GetObjectArgs.builder().bucket(properties.getBucketName()).object(objectName).build());
    } catch (Exception e) {
      throw new InternalServerException("Failed to download file from MinIO: " + e.getMessage());
    }
  }

  @Override
  public void deleteFile(String objectName) {
    try {
      minioClient.removeObject(
          RemoveObjectArgs.builder().bucket(properties.getBucketName()).object(objectName).build());
    } catch (Exception e) {
      throw new InternalServerException("Failed to delete file from MinIO: " + e.getMessage());
    }
  }
}
