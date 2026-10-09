package com.example.sd_24_striker_be.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Bóc tách doanh thu trong một khoảng ngày theo kênh bán và theo phương thức thanh toán.
 * <p>
 * Tổng của {@code theoKenh} luôn bằng {@code tongDoanhThu} vì mỗi hóa đơn chỉ được xếp
 * vào đúng một kênh - nhờ vậy con số trên dashboard không bị cộng trùng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueBreakdownResponse {

    private String tuNgay;
    private String denNgay;

    private BigDecimal tongDoanhThu;
    private Long tongSoHoaDon;

    /** Luôn gồm đủ 4 phần tử theo thứ tự TAI_QUAY, COD, CHUYEN_KHOAN, KHAC - kể cả khi bằng 0. */
    private List<PaymentChannelRevenue> theoKenh;

    /** Chi tiết tới từng kieu_thanh_toan, sắp xếp doanh thu giảm dần. */
    private List<PaymentMethodRevenue> theoPhuongThuc;
}
