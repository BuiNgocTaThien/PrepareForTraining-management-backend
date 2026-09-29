package com.fpt.preparefortraining.controller;

import com.fpt.preparefortraining.dto.response.ApiResponse;
import com.fpt.preparefortraining.dto.response.DocumentResponse;
import com.fpt.preparefortraining.entity.Document;
import com.fpt.preparefortraining.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import java.io.InputStream;
import java.util.List;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/documents")
@Tag(name = "Documents", description = "API Quản lý Tài liệu & Tệp tin đính kèm của Dự án")
public class DocumentController {

  private final DocumentService documents;

  public DocumentController(DocumentService documents) {
    this.documents = documents;
  }

  @GetMapping
  @Operation(summary = "List documents in a project")
  public ApiResponse<List<DocumentResponse>> list(
      @PathVariable Long projectId, Authentication auth) {
    return ApiResponse.success(documents.listByProject(projectId, auth.getName()));
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload a document to a project")
  public ResponseEntity<ApiResponse<DocumentResponse>> upload(
      @PathVariable Long projectId,
      @RequestParam("file") MultipartFile file,
      Authentication auth) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(documents.upload(projectId, file, auth.getName())));
  }

  @GetMapping("/{documentId}/download")
  @Operation(summary = "Download a document")
  public ResponseEntity<InputStreamResource> download(
      @PathVariable Long projectId, @PathVariable Long documentId, Authentication auth) {
    
    Document doc = documents.getDocumentDetail(documentId, auth.getName());
    InputStream inputStream = documents.download(documentId, auth.getName());

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"");

    return ResponseEntity.ok()
        .headers(headers)
        .contentType(MediaType.parseMediaType(doc.getContentType()))
        .body(new InputStreamResource(inputStream));
  }

  @DeleteMapping("/{documentId}")
  @Operation(summary = "Delete a document")
  public ApiResponse<Void> delete(
      @PathVariable Long projectId, @PathVariable Long documentId, Authentication auth) {
    documents.delete(documentId, auth.getName());
    return ApiResponse.success(null);
  }

  @PutMapping("/{documentId}/rename")
  @Operation(summary = "Rename a document")
  public ApiResponse<DocumentResponse> rename(
      @PathVariable Long projectId, 
      @PathVariable Long documentId, 
      @RequestBody com.fpt.preparefortraining.dto.request.RenameDocumentRequest request, 
      Authentication auth) {
    return ApiResponse.success(documents.rename(documentId, request.getNewName(), auth.getName()));
  }
}
