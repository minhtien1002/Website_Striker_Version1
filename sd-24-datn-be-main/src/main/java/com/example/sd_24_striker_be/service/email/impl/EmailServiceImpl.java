package com.example.sd_24_striker_be.service.email.impl;

import com.example.sd_24_striker_be.modal.bill.HoaDon;
import com.example.sd_24_striker_be.modal.bill.HoaDonChiTiet;
import com.example.sd_24_striker_be.service.email.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Gửi email thông báo đơn hàng qua SMTP (spring.mail.*, xem application.properties).
 * `mail.enabled=false` (mặc định) khi CHƯA cấu hình tài khoản SMTP thật - lúc đó chỉ log
 * lại nội dung thay vì cố gửi và báo lỗi xác thực liên tục. Đặt mail.enabled=true và điền
 * MAIL_USERNAME/MAIL_PASSWORD (biến môi trường) khi đã có tài khoản gửi mail thật.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${mail.from.address:no-reply@striker.local}")
    private String fromAddress;

    @Value("${mail.from.name:Striker Sports Store}")
    private String fromName;

    @Override
    public void sendOrderConfirmationEmail(HoaDon hoaDon, List<HoaDonChiTiet> items) {
        String subject = "Xác nhận đơn hàng " + hoaDon.getMa() + " - Striker Sports Store";
        String body = buildOrderEmailBody(
                "Cảm ơn bạn đã đặt hàng!",
                "Đơn hàng <b>" + escape(hoaDon.getMa()) + "</b> của bạn đã được ghi nhận và đang được xử lý. " +
                        "Chúng tôi sẽ thông báo khi đơn hàng được giao cho đơn vị vận chuyển.",
                hoaDon, items);
        send(hoaDon.getEmail(), subject, body);
    }

    @Override
    public void sendOrderCancelledEmail(HoaDon hoaDon, String lyDo, boolean byCustomer) {
        String subject = "Đơn hàng " + hoaDon.getMa() + " đã bị hủy";
        String actor = byCustomer ? "theo yêu cầu của bạn" : "bởi cửa hàng";
        String body = buildOrderEmailBody(
                "Đơn hàng đã bị hủy",
                "Đơn hàng <b>" + escape(hoaDon.getMa()) + "</b> đã được hủy " + actor + ".<br/>Lý do: " + escape(lyDo) +
                        "<br/>Nếu đã thanh toán trước, cửa hàng sẽ liên hệ để hoàn tiền theo chính sách hiện hành.",
                hoaDon, null);
        send(hoaDon.getEmail(), subject, body);
    }

    @Override
    public void sendReturnRequestedEmail(HoaDon hoaDon, String lyDo) {
        String subject = "Đã ghi nhận yêu cầu hoàn hàng cho đơn " + hoaDon.getMa();
        String body = buildOrderEmailBody(
                "Đã ghi nhận yêu cầu hoàn hàng",
                "Chúng tôi đã nhận được yêu cầu hoàn hàng cho đơn <b>" + escape(hoaDon.getMa()) + "</b>.<br/>Lý do: " + escape(lyDo) +
                        "<br/>Cửa hàng sẽ liên hệ để hướng dẫn gửi trả hàng về kho.",
                hoaDon, null);
        send(hoaDon.getEmail(), subject, body);
    }

    @Override
    public void sendReturnConfirmedEmail(HoaDon hoaDon) {
        String subject = "Đã hoàn tất hoàn hàng cho đơn " + hoaDon.getMa();
        String body = buildOrderEmailBody(
                "Hoàn hàng thành công",
                "Cửa hàng đã nhận được hàng hoàn của đơn <b>" + escape(hoaDon.getMa()) + "</b> và sẽ tiến hành hoàn tiền theo chính sách của cửa hàng.",
                hoaDon, null);
        send(hoaDon.getEmail(), subject, body);
    }

    @Override
    public void sendReturnRejectedEmail(HoaDon hoaDon, String lyDo) {
        String subject = "Yêu cầu hoàn hàng cho đơn " + hoaDon.getMa() + " không được chấp nhận";
        String body = buildOrderEmailBody(
                "Yêu cầu hoàn hàng bị từ chối",
                "Yêu cầu hoàn hàng cho đơn <b>" + escape(hoaDon.getMa()) + "</b> không được chấp nhận.<br/>Lý do: " + escape(lyDo),
                hoaDon, null);
        send(hoaDon.getEmail(), subject, body);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String otpCode) {
        String subject = "Mã xác nhận đặt lại mật khẩu - Striker Sports Store";
        String body = "<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:auto\">" +
                "<h2 style=\"color:#1a73e8\">Đặt lại mật khẩu</h2>" +
                "<p>Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản Striker Sports Store. Mã xác nhận của bạn là:</p>" +
                "<p style=\"font-size:28px;font-weight:bold;letter-spacing:6px;text-align:center;background:#f2f2f2;padding:12px;border-radius:8px\">" +
                escape(otpCode) + "</p>" +
                "<p>Mã có hiệu lực trong 15 phút. Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>" +
                "<p style=\"color:#888;font-size:12px\">Đây là email tự động, vui lòng không trả lời trực tiếp email này.</p>" +
                "</div>";
        send(toEmail, subject, body);
    }

    @Override
    public boolean isMailEnabled() {
        return mailEnabled && mailSender != null;
    }

    private void send(String to, String subject, String htmlBody) {
        if (to == null || to.isBlank()) {
            return;
        }
        if (!mailEnabled || mailSender == null) {
            logger.info("[EMAIL tắt - chỉ log] to={} subject={}", to, subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom(fromAddress, fromName);
            mailSender.send(message);
            logger.info("Đã gửi email '{}' tới {}", subject, to);
        } catch (Exception e) {
            // Không throw ra ngoài: lỗi gửi mail (SMTP sai cấu hình, timeout,...) không
            // được phép làm hỏng giao dịch bán hàng đang diễn ra.
            logger.warn("Gửi email tới {} thất bại: {}", to, e.getMessage());
        }
    }

    private String buildOrderEmailBody(String heading, String introHtml, HoaDon hoaDon, List<HoaDonChiTiet> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:auto\">");
        sb.append("<h2 style=\"color:#1a73e8\">").append(heading).append("</h2>");
        sb.append("<p>").append(introHtml).append("</p>");

        sb.append("<table style=\"width:100%;border-collapse:collapse;margin:12px 0\">");
        sb.append(infoRow("Mã đơn hàng", hoaDon.getMa()));
        sb.append(infoRow("Ngày tạo", hoaDon.getNgayTao() != null ? DATE_FORMAT.format(hoaDon.getNgayTao()) : ""));
        sb.append(infoRow("Người nhận", hoaDon.getTenKhachHang()));
        sb.append(infoRow("Địa chỉ", hoaDon.getDiaChiKhachHang()));
        sb.append(infoRow("Điện thoại", hoaDon.getSoDienThoaiKhachHang()));
        sb.append("</table>");

        if (items != null && !items.isEmpty()) {
            sb.append("<table style=\"width:100%;border-collapse:collapse\" border=\"1\" cellpadding=\"6\">");
            sb.append("<tr style=\"background:#f2f2f2\"><th>Sản phẩm</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th></tr>");
            for (HoaDonChiTiet item : items) {
                String tenSp = "Sản phẩm";
                if (item.getIdChiTietSp() != null && item.getIdChiTietSp().getIdSanPham() != null) {
                    tenSp = item.getIdChiTietSp().getIdSanPham().getTenSanPham();
                }
                BigDecimal gia = item.getGia() != null ? item.getGia() : BigDecimal.ZERO;
                int soLuong = item.getSoLuong() != null ? item.getSoLuong() : 0;
                BigDecimal thanhTien = gia.multiply(BigDecimal.valueOf(soLuong));
                sb.append("<tr>")
                        .append("<td>").append(escape(tenSp)).append("</td>")
                        .append("<td style=\"text-align:center\">").append(soLuong).append("</td>")
                        .append("<td style=\"text-align:right\">").append(formatCurrency(gia)).append("</td>")
                        .append("<td style=\"text-align:right\">").append(formatCurrency(thanhTien)).append("</td>")
                        .append("</tr>");
            }
            sb.append("</table>");
        }

        sb.append("<p style=\"margin-top:12px\"><b>Tổng tiền: ").append(formatCurrency(hoaDon.getTongTienSauGiam())).append("</b></p>");
        sb.append("<p style=\"color:#888;font-size:12px\">Đây là email tự động, vui lòng không trả lời trực tiếp email này.</p>");
        sb.append("</div>");
        return sb.toString();
    }

    private String infoRow(String label, String value) {
        return "<tr><td style=\"padding:4px 8px;color:#555\">" + label + "</td><td style=\"padding:4px 8px\"><b>"
                + (value != null ? escape(value) : "") + "</b></td></tr>";
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 đ";
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + " đ";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
