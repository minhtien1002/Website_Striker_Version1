package com.example.sd_24_striker_be.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private boolean success;
    private String message;
    private Integer customerId;
    private String customerCode;
    // Tên đăng nhập thực tế được hệ thống tạo ra (từ họ và tên) - FE cần biết giá trị
    // này để hiển thị cho khách và dùng khi đăng nhập, vì có thể khác với tên khách gõ
    // (bỏ dấu, viết liền, hoặc thêm số phía sau nếu trùng).
    private String tenDangNhap;

    public static RegisterResponse success(String message, Integer customerId, String customerCode, String tenDangNhap) {
        return new RegisterResponse(true, message, customerId, customerCode, tenDangNhap);
    }

    public static RegisterResponse error(String message) {
        return new RegisterResponse(false, message, null, null, null);
    }
}
