package com.example.sd_24_striker_be.exception;

public class CustomPaymentException extends RuntimeException {
    public CustomPaymentException(String message) {
        super(message);
    }
}