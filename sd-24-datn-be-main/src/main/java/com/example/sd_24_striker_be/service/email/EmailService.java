package com.example.sd_24_striker_be.service.email;

import com.example.sd_24_striker_be.modal.bill.HoaDon;
import com.example.sd_24_striker_be.modal.bill.HoaDonChiTiet;

import java.util.List;

/**
 * Gửi email thông báo cho khách hàng theo các mốc của vòng đời đơn hàng.
 * Mọi implementation phải tự bắt lỗi gửi mail bên trong (không throw ra ngoài) -
 * gọi qua các hàm *Safely trong HoaDonServiceImpl để đảm bảo lỗi SMTP không bao giờ
 * làm hỏng giao dịch bán hàng.
 */
public interface EmailService {

    void sendOrderConfirmationEmail(HoaDon hoaDon, List<HoaDonChiTiet> items);

    void sendOrderCancelledEmail(HoaDon hoaDon, String lyDo, boolean byCustomer);

    void sendReturnRequestedEmail(HoaDon hoaDon, String lyDo);

    void sendReturnConfirmedEmail(HoaDon hoaDon);

    void sendReturnRejectedEmail(HoaDon hoaDon, String lyDo);

    /**
     * Gửi mã OTP đặt lại mật khẩu tới email khách hàng/tài khoản.
     */
    void sendPasswordResetEmail(String toEmail, String otpCode);

    /**
     * true nếu hệ thống đang cấu hình gửi mail thật (mail.enabled=true và có mailSender).
     * Dùng để quyết định có được phép trả OTP thẳng về response (chế độ dev/test) hay không.
     */
    boolean isMailEnabled();
}
