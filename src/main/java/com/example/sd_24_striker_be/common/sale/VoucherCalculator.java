package com.example.sd_24_striker_be.common.sale;

import com.example.sd_24_striker_be.modal.sale.PhieuGiamGia;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Tính số tiền giảm của phiếu giảm giá ở SERVER - dùng chung cho API kiểm tra voucher và
 * bước thanh toán, để số tiền giảm lưu vào hóa đơn không phụ thuộc số client gửi lên.
 */
public final class VoucherCalculator {

    private VoucherCalculator() {
    }

    /** Dữ liệu dùng "PHAN_TRAM"; code cũ so với "PERCENTAGE" nên voucher % luôn bị tính như giảm tiền cố định. */
    public static boolean isPercentage(PhieuGiamGia voucher) {
        String loai = voucher.getLoaiPhieuGiamGia();
        return "PHAN_TRAM".equalsIgnoreCase(loai) || "PERCENTAGE".equalsIgnoreCase(loai);
    }

    public static BigDecimal minOrderAmount(PhieuGiamGia voucher) {
        return voucher.getHoaDonToiThieu() != null ? BigDecimal.valueOf(voucher.getHoaDonToiThieu()) : BigDecimal.ZERO;
    }

    /** Lý do voucher không dùng được cho đơn này, hoặc null nếu hợp lệ. */
    public static String validate(PhieuGiamGia voucher, BigDecimal orderAmount) {
        if (Boolean.FALSE.equals(voucher.getTrangThai())) {
            return "Phiếu giảm giá không còn hoạt động";
        }
        if (voucher.getSoLuongDung() != null && voucher.getSoLuongDung() <= 0) {
            return "Phiếu giảm giá đã hết lượt sử dụng";
        }
        if (Boolean.TRUE.equals(voucher.getDeleted())) {
            return "Phiếu giảm giá không còn hoạt động";
        }
        Instant now = Instant.now();
        if (voucher.getNgayBatDau() != null && now.isBefore(voucher.getNgayBatDau())) {
            return "Phiếu giảm giá chưa đến thời gian áp dụng";
        }
        if (voucher.getNgayKetThuc() != null && now.isAfter(voucher.getNgayKetThuc())) {
            return "Phiếu giảm giá đã hết hạn";
        }
        if (orderAmount.compareTo(minOrderAmount(voucher)) < 0) {
            return "Đơn hàng chưa đạt giá trị tối thiểu để dùng phiếu giảm giá";
        }
        return null;
    }

    /**
     * Số tiền giảm cố định. Dữ liệu cũ (loại TIEN_MAT) lưu ở soTienGiamToiDa, còn form admin
     * (loại SO_TIEN_CO_DINH) lại lưu vào phanTramGiamGia và để trống soTienGiamToiDa.
     */
    public static BigDecimal fixedAmount(PhieuGiamGia voucher) {
        if (voucher.getSoTienGiamToiDa() != null) {
            return BigDecimal.valueOf(voucher.getSoTienGiamToiDa());
        }
        return voucher.getPhanTramGiamGia() != null ? BigDecimal.valueOf(voucher.getPhanTramGiamGia()) : BigDecimal.ZERO;
    }

    /** Số tiền giảm (không vượt quá giá trị đơn), làm tròn xuống như màn hình bán hàng. */
    public static BigDecimal discountAmount(PhieuGiamGia voucher, BigDecimal orderAmount) {
        BigDecimal discount;
        if (isPercentage(voucher)) {
            double percent = voucher.getPhanTramGiamGia() != null ? voucher.getPhanTramGiamGia() : 0;
            discount = orderAmount.multiply(BigDecimal.valueOf(percent))
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);
            if (voucher.getSoTienGiamToiDa() != null) {
                discount = discount.min(BigDecimal.valueOf(voucher.getSoTienGiamToiDa()));
            }
        } else {
            discount = fixedAmount(voucher);
        }
        return discount.min(orderAmount).max(BigDecimal.ZERO);
    }
}
