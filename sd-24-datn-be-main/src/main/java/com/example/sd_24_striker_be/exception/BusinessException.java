package com.example.sd_24_striker_be.exception;

import org.springframework.http.HttpStatus;

/** Lỗi nghiệp vụ có thông báo tiếng Việt hiển thị thẳng cho người dùng. */
public class BusinessException extends RuntimeException {
    private final HttpStatus status;

    public BusinessException(String message) {
        this(HttpStatus.BAD_REQUEST, message);
    }

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
