package com.fpt.preparefortraining.dto.response;

import com.fpt.preparefortraining.entity.Document;
import java.time.LocalDateTime;

public record DocumentResponse(
    Long id,
    String fileName,
    Long fileSize,
    String contentType,
    String uploaderName,
    LocalDateTime createdAt) {
  
  public static DocumentResponse from(Document doc) {
    return new DocumentResponse(
        doc.getId(),
        doc.getFileName(),
        doc.getFileSize(),
        doc.getContentType(),
        doc.getUploader().getFullName(),
        doc.getCreatedAt());
  }
}
