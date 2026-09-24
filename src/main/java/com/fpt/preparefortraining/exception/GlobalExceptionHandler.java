package com.fpt.preparefortraining.exception;

import com.fpt.preparefortraining.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BadRequestException.class)
  ResponseEntity<ApiResponse<Void>> bad(BadRequestException e) {
    return response(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(UnauthorizedException.class)
  ResponseEntity<ApiResponse<Void>> unauthorized(UnauthorizedException e) {
    return response(HttpStatus.UNAUTHORIZED, e.getMessage());
  }

  @ExceptionHandler(ForbiddenException.class)
  ResponseEntity<ApiResponse<Void>> forbidden(ForbiddenException e) {
    return response(HttpStatus.FORBIDDEN, e.getMessage());
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  ResponseEntity<ApiResponse<Void>> notFound(ResourceNotFoundException e) {
    return response(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiResponse<Void>> validationMethodArg(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
        .map(err -> err.getField() + ": " + err.getDefaultMessage())
        .collect(Collectors.joining("; "));
    return response(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ApiResponse<Void>> validationConstraint(ConstraintViolationException e) {
    String message = e.getConstraintViolations().stream()
        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
        .collect(Collectors.joining("; "));
    return response(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(InternalServerException.class)
  ResponseEntity<ApiResponse<Void>> internalServer(InternalServerException e) {
    return response(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
  }

  private ResponseEntity<ApiResponse<Void>> response(HttpStatus s, String m) {
    return ResponseEntity.status(s).body(ApiResponse.failure(m));
  }
}
