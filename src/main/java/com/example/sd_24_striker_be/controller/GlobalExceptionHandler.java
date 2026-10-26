package com.example.sd_24_striker_be.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

/**
 * Bắt các exception phát sinh TRƯỚC khi vào được try/catch của controller
 * (vd: lỗi bind tham số của Spring MVC), tránh trả về 500 kèm leak stack
 * trace ra ngoài cho client. Không ảnh hưởng tới các @ExceptionHandler khác
 * đã có sẵn trong từng controller (chỉ bắt những gì chưa ai bắt).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<?> handleMultipart(MultipartException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Yêu cầu tải file không hợp lệ (thiếu file hoặc sai định dạng multipart)");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Thiếu tham số bắt buộc: " + e.getParameterName());
    }

    /** Không tìm thấy bản ghi (vd hóa đơn đã bị xóa) là 404 - trước đây lọt ra thành 500. */
    @ExceptionHandler(com.example.sd_24_striker_be.exception.ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(com.example.sd_24_striker_be.exception.ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("message", e.getMessage()));
    }

    /** Lỗi @Valid: trả câu báo lỗi của trường đầu tiên thay vì cả stack trace. */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleInvalid(org.springframework.web.bind.MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getDefaultMessage())
                .findFirst()
                .orElse("Dữ liệu không hợp lệ");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}
