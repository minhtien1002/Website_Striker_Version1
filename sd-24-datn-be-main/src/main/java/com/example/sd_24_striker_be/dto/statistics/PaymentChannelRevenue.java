package com.example.sd_24_striker_be.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Doanh thu của MỘT kênh bán hàng. Ba kênh chính theo yêu cầu nghiệp vụ:
 * Tại quầy, COD (thu tiền khi giao), Chuyển khoản (bao gồm ví điện tử / VNPay).
 * Xem {@code StatisticsServiceImpl#phanLoaiKenh} để biết quy tắc phân loại.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentChannelRevenue {

    /** Mã kênh, ổn định để FE so sánh: TAI_QUAY | COD | CHUYEN_KHOAN | KHAC. */
    private String maKenh;

    /** Nhãn tiếng Việt để hiển thị. */
    private String tenKenh;

    /** Tổng tiền sau giảm của các hóa đơn thuộc kênh này. */
    private BigDecimal doanhThu;

    /** Số hóa đơn thuộc kênh này. */
    private Long soHoaDon;

    /** Tỉ trọng doanh thu trên tổng, đơn vị phần trăm, làm tròn 2 chữ số. */
    private BigDecimal tiTrong;
}
