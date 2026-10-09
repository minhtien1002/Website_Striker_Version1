package com.example.sd_24_striker_be.controller.auth;

import com.example.sd_24_striker_be.dto.auth.ApiMessageResponse;
import com.example.sd_24_striker_be.dto.auth.ForgotPasswordRequest;
import com.example.sd_24_striker_be.dto.auth.LoginRequest;
import com.example.sd_24_striker_be.dto.auth.LoginResponse;
import com.example.sd_24_striker_be.dto.auth.RegisterRequest;
import com.example.sd_24_striker_be.dto.auth.RegisterResponse;
import com.example.sd_24_striker_be.dto.auth.ResetPasswordRequest;
import com.example.sd_24_striker_be.service.auth.AuthClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth-client")
@CrossOrigin(origins = "*")
public class AuthClientController {

    @Autowired
    private AuthClientService authClientService;

    /**
     * Customer login endpoint - allows customers (role ID 3) to login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = authClientService.login(loginRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Customer logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(@RequestParam(required = false) Integer userId) {
        LoginResponse response = authClientService.logout(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Customer registration endpoint
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = authClientService.register(registerRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Bước 1 của luồng quên mật khẩu: gửi mã OTP tới email đã đăng ký.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiMessageResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        ApiMessageResponse response = authClientService.forgotPassword(request);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    /**
     * Bước 2 của luồng quên mật khẩu: xác nhận mã OTP và đặt mật khẩu mới.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiMessageResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        ApiMessageResponse response = authClientService.resetPassword(request);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
}
