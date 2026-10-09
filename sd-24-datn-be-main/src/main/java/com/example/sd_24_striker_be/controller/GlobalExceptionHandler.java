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

    /** Lỗi nghiệp vụ: trả thẳng câu thông báo để frontend hiển thị. */
    @ExceptionHandler(com.example.sd_24_striker_be.exception.BusinessException.class)
    public ResponseEntity<?> handleBusiness(com.example.sd_24_striker_be.exception.BusinessException e) {
        return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    }

    /** Dữ liệu không hợp lệ (vd không đủ tồn kho khi thêm vào giỏ) - trả câu thông báo. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    /** Vi phạm ràng buộc unique trong DB (vd hai người cùng thêm một tên cùng lúc). */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<?> handleIntegrity(org.springframework.dao.DataIntegrityViolationException e) {
        String msg = String.valueOf(e.getMostSpecificCause().getMessage());
        if (msg.contains("UX_")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên đã tồn tại (không phân biệt chữ hoa, chữ thường)");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Dữ liệu không hợp lệ hoặc đang được sử dụng");
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
