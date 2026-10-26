package com.example.sd_24_striker_be.common.bill;

import java.util.List;
import java.util.Set;

/**
 * Nguồn duy nhất cho các mã trạng thái (trangThai) của HoaDon và luồng chuyển
 * trạng thái hợp lệ. Trước đây mỗi tầng (HoaDonServiceImpl, StatisticsServiceImpl,
 * KhachHangClientService, các query thống kê trong HoaDonRepository) tự định nghĩa
 * một bảng mã khác nhau và không khớp nhau — gây sai lệch số liệu và nhãn hiển thị.
 * Toàn bộ code nên tham chiếu tới lớp này thay vì hard-code số.
 *
 * <h3>Luồng nghiệp vụ hiện hành</h3>
 * <pre>
 *   Chờ xác nhận (1) → Đã xác nhận (2) → Đang vận chuyển (4) → Đã hoàn thành (5)
 *                                     ↘ Đã hủy (6)
 * </pre>
 * Mã 3 ("Chờ vận chuyển") đã bị loại khỏi luồng nhưng VẪN được giữ lại để dữ liệu
 * cũ trong DB không bị mất nhãn; không bao giờ set mới mã này nữa.
 *
 * <h3>Quy ước tồn kho</h3>
 * Tồn kho CHỈ bị trừ đúng một lần, tại thời điểm đơn chuyển sang "Đã xác nhận" (2)
 * hoặc nhảy thẳng sang "Đã hoàn thành" (5) khi bán tại quầy. Xem
 * {@link #isStockDeducted(Short)} — đây là hàm quyết định duy nhất, thay cho việc
 * trừ kho rải rác lúc thêm vào giỏ hàng như trước.
 */
public final class HoaDonStatus {

    private HoaDonStatus() {
    }

    public static final short HOA_DON_CHO = 0;       // Giỏ hàng / hóa đơn nháp tại quầy, chưa thực sự đặt hàng
    public static final short CHO_XAC_NHAN = 1;       // Khách đã đặt/thanh toán xong, chờ admin xác nhận
    public static final short DA_XAC_NHAN = 2;        // Admin đã xác nhận đơn -> TỒN KHO ĐƯỢC TRỪ TẠI ĐÂY
    /** @deprecated Đã bỏ khỏi luồng, chỉ giữ để map nhãn cho dữ liệu cũ. Không set mới. */
    @Deprecated
    public static final short CHO_VAN_CHUYEN = 3;
    public static final short DANG_VAN_CHUYEN = 4;    // Đang giao hàng
    public static final short DA_HOAN_THANH = 5;      // Giao thành công
    public static final short DA_HUY = 6;             // Đơn đã bị hủy (khách hoặc admin)
    public static final short YEU_CAU_HOAN_HANG = 7;  // Khách yêu cầu trả hàng sau khi đã hoàn thành
    public static final short DA_HOAN_HANG = 8;       // Admin đã nhận hàng hoàn về kho, tồn kho đã được cộng lại

    public static final short MIN = HOA_DON_CHO;
    public static final short MAX = DA_HOAN_HANG;

    /**
     * Luồng tiến tuần tự chính thức, theo đúng thứ tự hiển thị trên timeline.
     * Không chứa mã 3 (đã bỏ) và không chứa các nhánh rẽ (hủy / hoàn hàng).
     */
    public static final List<Short> FLOW = List.of(CHO_XAC_NHAN, DA_XAC_NHAN, DANG_VAN_CHUYEN, DA_HOAN_THANH);

    /**
     * Thứ tự dùng để CHẶN LÙI trạng thái. Khác FLOW ở chỗ có cả HOA_DON_CHO (0) và mã cũ
     * CHO_VAN_CHUYEN (3): thiếu chúng thì indexOf() = -1 nên phép kiểm tra bị bỏ qua - đơn đã
     * đặt (1) bị đẩy ngược về nháp (0), hoặc đơn cũ ở mã 3 (đã trừ kho) lùi về 1 rồi xác
     * nhận lại sẽ trừ kho lần hai.
     */
    public static final List<Short> ORDER = List.of(HOA_DON_CHO, CHO_XAC_NHAN, DA_XAC_NHAN, CHO_VAN_CHUYEN, DANG_VAN_CHUYEN, DA_HOAN_THANH);

    /** Các trạng thái admin được phép chọn qua endpoint cập nhật trạng thái thông thường. */
    public static final Set<Short> GENERIC_UPDATABLE = Set.of(HOA_DON_CHO, CHO_XAC_NHAN, DA_XAC_NHAN, DANG_VAN_CHUYEN, DA_HOAN_THANH);

    /** Khách hàng chỉ được tự hủy khi đơn chưa được giao cho đơn vị vận chuyển. */
    public static final Set<Short> CUSTOMER_CANCELLABLE_FROM = Set.of(CHO_XAC_NHAN, DA_XAC_NHAN, CHO_VAN_CHUYEN);

    /** Admin có thể hủy muộn hơn khách (kể cả khi đang vận chuyển, do có thể liên hệ đơn vị vận chuyển). */
    public static final Set<Short> ADMIN_CANCELLABLE_FROM = Set.of(CHO_XAC_NHAN, DA_XAC_NHAN, CHO_VAN_CHUYEN, DANG_VAN_CHUYEN);

    /** Chỉ đơn đã hoàn thành mới được yêu cầu hoàn hàng. */
    public static final Set<Short> RETURN_REQUESTABLE_FROM = Set.of(DA_HOAN_THANH);

    /** Admin xác nhận đã nhận hàng hoàn: từ trạng thái đã yêu cầu, hoặc trực tiếp từ đã hoàn thành (khách trả tại quầy/offline). */
    public static final Set<Short> RETURN_CONFIRMABLE_FROM = Set.of(YEU_CAU_HOAN_HANG, DA_HOAN_THANH);

    /**
     * Tồn kho của hóa đơn ĐANG bị trừ khi đơn ở một trong các trạng thái này.
     * <ul>
     *   <li>0 (nháp) và 1 (chờ xác nhận): hàng vẫn còn trong kho, mới chỉ là đặt chỗ trên giấy.</li>
     *   <li>2, 3, 4, 5, 7: hàng đã xuất kho.</li>
     *   <li>6 (đã hủy) và 8 (đã hoàn hàng): hàng đã được cộng trả lại kho.</li>
     * </ul>
     * Nhờ suy ra từ trạng thái, ta không cần thêm cột cờ vào bảng hoa_don mà vẫn
     * đảm bảo trừ/hoàn kho đúng một lần.
     */
    public static boolean isStockDeducted(Short trangThai) {
        if (trangThai == null) return false;
        return trangThai == DA_XAC_NHAN
                || trangThai == CHO_VAN_CHUYEN
                || trangThai == DANG_VAN_CHUYEN
                || trangThai == DA_HOAN_THANH
                || trangThai == YEU_CAU_HOAN_HANG;
    }

    /** Những trạng thái không được tính vào doanh thu: đơn nháp, đơn đã hủy, đơn đã hoàn trả. */
    public static boolean isExcludedFromRevenue(Short trangThai) {
        return trangThai != null && (trangThai == HOA_DON_CHO || trangThai == DA_HUY || trangThai == DA_HOAN_HANG);
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
            default: return "Không xác định";
        }
    }
}
