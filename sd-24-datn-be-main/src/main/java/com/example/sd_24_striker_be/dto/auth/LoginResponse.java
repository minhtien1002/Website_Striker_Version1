package com.example.sd_24_striker_be.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Integer id;
    private String ma;
    private String tenDangNhap;
    private String email;
    private String soDienThoai;
    private String tenQuyen;
    private Integer capQuyenHan;
    /** Nhân viên gắn với tài khoản (admin cũng có thể có) - để ghi nhân viên tạo/thanh toán hóa đơn. */
    private Integer nhanVienId;
    private String maNhanVien;
    private String tenNhanVien;
    private Integer customerId; // ID khách hàng (chỉ có khi role = 3)
    private String tenKhachHang; // Tên khách hàng (chỉ có khi role = 3)
    private boolean success;
    private String message;
    
    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
