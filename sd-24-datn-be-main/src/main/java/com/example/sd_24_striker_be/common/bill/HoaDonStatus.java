package com.example.sd_24_striker_be.common.bill;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Nguồn duy nhất cho các mã trạng thái (trangThai) của HoaDon và luồng chuyển trạng thái hợp lệ.
 *
 * <h3>Luồng đơn online</h3>
 * <pre>
 *   Chờ xác nhận (1) → Đã xác nhận (2) → Chờ vận chuyển (3) → Đang vận chuyển (4) → Đã hoàn thành (5)
 *                                                               ↘ Giao thất bại (9) → Hoàn kho (10)
 *                                                                                  ↘ giao lại: Đang vận chuyển (4)
 *   Hủy (6): từ 1, 2, 3 (admin hủy được cả khi 4)
 *   Hoàn hàng: Đã hoàn thành (5) → Yêu cầu hoàn hàng (7) → Đã hoàn hàng (8)
 * </pre>
 * Đơn tại quầy: Hóa đơn chờ (0) → thanh toán → Đã hoàn thành (5), tự xác nhận luôn.
 *
 * <h3>Tồn kho</h3>
 * Trừ đúng một lần khi đơn vào nhóm "đã xuất kho" ({@link #isStockDeducted}): đơn online lúc
 * "Đã xác nhận", đơn tại quầy lúc thanh toán. Cộng lại khi hủy (nếu đã trừ), hoàn hàng, hoàn kho.
 */
public final class HoaDonStatus {

    private HoaDonStatus() {
    }

    public static final short HOA_DON_CHO = 0;
    public static final short CHO_XAC_NHAN = 1;
    public static final short DA_XAC_NHAN = 2;
    public static final short CHO_VAN_CHUYEN = 3;
    public static final short DANG_VAN_CHUYEN = 4;
    public static final short DA_HOAN_THANH = 5;
    public static final short DA_HUY = 6;
    public static final short YEU_CAU_HOAN_HANG = 7;
    public static final short DA_HOAN_HANG = 8;
    public static final short GIAO_THAT_BAI = 9;
    public static final short HOAN_KHO = 10;

    public static final short MIN = HOA_DON_CHO;
    public static final short MAX = HOAN_KHO;

    /** Luồng tiến chính, theo thứ tự hiển thị trên timeline. */
    public static final List<Short> FLOW = List.of(CHO_XAC_NHAN, DA_XAC_NHAN, CHO_VAN_CHUYEN, DANG_VAN_CHUYEN, DA_HOAN_THANH);

    /**
     * Các bước chuyển hợp lệ qua endpoint cập nhật trạng thái thông thường (chỉ tiến từng bước).
     * Hủy, giao thất bại, hoàn kho, hoàn hàng có endpoint riêng vì cần lý do / xử lý kho.
     */
    public static final Map<Short, Short> NEXT = Map.of(
            CHO_XAC_NHAN, DA_XAC_NHAN,
            DA_XAC_NHAN, CHO_VAN_CHUYEN,
            CHO_VAN_CHUYEN, DANG_VAN_CHUYEN,
            DANG_VAN_CHUYEN, DA_HOAN_THANH,
            GIAO_THAT_BAI, DANG_VAN_CHUYEN);

    public static final Set<Short> CUSTOMER_CANCELLABLE_FROM = Set.of(CHO_XAC_NHAN, DA_XAC_NHAN, CHO_VAN_CHUYEN);

    public static final Set<Short> ADMIN_CANCELLABLE_FROM = Set.of(CHO_XAC_NHAN, DA_XAC_NHAN, CHO_VAN_CHUYEN, DANG_VAN_CHUYEN);

    public static final Set<Short> RETURN_REQUESTABLE_FROM = Set.of(DA_HOAN_THANH);

    public static final Set<Short> RETURN_CONFIRMABLE_FROM = Set.of(YEU_CAU_HOAN_HANG, DA_HOAN_THANH);

    /** Hàng đang ở ngoài kho (đã trừ tồn) khi đơn ở các trạng thái này. */
    public static boolean isStockDeducted(Short trangThai) {
        if (trangThai == null) return false;
        return trangThai == DA_XAC_NHAN
                || trangThai == CHO_VAN_CHUYEN
                || trangThai == DANG_VAN_CHUYEN
                || trangThai == DA_HOAN_THANH
                || trangThai == YEU_CAU_HOAN_HANG
                || trangThai == GIAO_THAT_BAI;
    }

    /** Không tính doanh thu: nháp, hủy, đã hoàn hàng, giao thất bại, hoàn kho. */
    public static boolean isExcludedFromRevenue(Short trangThai) {
        return trangThai != null && (trangThai == HOA_DON_CHO || trangThai == DA_HUY || trangThai == DA_HOAN_HANG
                || trangThai == GIAO_THAT_BAI || trangThai == HOAN_KHO);
    }

    public static boolean isValid(Short trangThai) {
        return trangThai != null && trangThai >= MIN && trangThai <= MAX;
    }

    public static String toLabel(Short trangThai) {
        if (trangThai == null) return "Không xác định";
        switch (trangThai) {
            case HOA_DON_CHO: return "Hóa đơn chờ";
            case CHO_XAC_NHAN: return "Chờ xác nhận";
            case DA_XAC_NHAN: return "Đã xác nhận";
            case CHO_VAN_CHUYEN: return "Chờ vận chuyển";
            case DANG_VAN_CHUYEN: return "Đang vận chuyển";
            case DA_HOAN_THANH: return "Đã hoàn thành";
            case DA_HUY: return "Đã hủy";
            case YEU_CAU_HOAN_HANG: return "Yêu cầu hoàn hàng";
            case DA_HOAN_HANG: return "Đã hoàn hàng";
            case GIAO_THAT_BAI: return "Giao thất bại";
            case HOAN_KHO: return "Đã hoàn kho";
            default: return "Không xác định";
        }
    }
}
