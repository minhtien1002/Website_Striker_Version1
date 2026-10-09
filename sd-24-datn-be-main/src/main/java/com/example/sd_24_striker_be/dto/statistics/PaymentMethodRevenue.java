package com.example.sd_24_striker_be.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Doanh thu chi tiết theo từng phương thức thanh toán cụ thể
 * (phuong_thuc_thanh_toan.kieu_thanh_toan), dùng để soi kỹ bên trong mỗi kênh của
 * {@link PaymentChannelRevenue}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodRevenue {

    /** Giá trị thô lấy từ phuong_thuc_thanh_toan.kieu_thanh_toan. */
    private String phuongThuc;

    /** Kênh mà phương thức này được gom vào: TAI_QUAY | COD | CHUYEN_KHOAN | KHAC. */
    private String maKenh;

    private BigDecimal doanhThu;

    private Long soHoaDon;
}
