package com.example.sd_24_striker_be.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response chung dạng {success, message} cho các API không cần trả thêm dữ liệu
 * (quên mật khẩu, đặt lại mật khẩu, ...).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiMessageResponse {
    private boolean success;
    private String message;
    // Chỉ có giá trị khi hệ thống CHƯA bật gửi mail thật (mail.enabled=false) - trả thẳng
    // mã OTP về để có thể kiểm thử luồng quên mật khẩu mà không cần cấu hình SMTP.
    // Khi mail.enabled=true, trường này luôn null và mã chỉ được gửi qua email.
    private String debugOtpCode;

    public static ApiMessageResponse ok(String message) {
        return new ApiMessageResponse(true, message, null);
    }

    public static ApiMessageResponse error(String message) {
        return new ApiMessageResponse(false, message, null);
    }
}
