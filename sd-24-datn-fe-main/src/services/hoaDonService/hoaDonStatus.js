/**
 * Nguồn duy nhất cho mã trạng thái hóa đơn phía admin FE - phải khớp với
 * com.example.sd_24_striker_be.common.bill.HoaDonStatus bên backend.
 *
 * Luồng đơn online: Chờ xác nhận (1) → Đã xác nhận (2) → Chờ vận chuyển (3) → Đang vận chuyển (4) → Đã hoàn thành (5)
 * Nhánh: Đang vận chuyển (4) → Giao thất bại (9, có lý do) → Đã hoàn kho (10) hoặc giao lại (4)
 * Đơn tại quầy: thanh toán xong là Đã hoàn thành (5).
 */
export const HOA_DON_STATUS = {
  HOA_DON_CHO: 0,
  CHO_XAC_NHAN: 1,
  DA_XAC_NHAN: 2,
  CHO_VAN_CHUYEN: 3,
  DANG_VAN_CHUYEN: 4,
  DA_HOAN_THANH: 5,
  DA_HUY: 6,
  YEU_CAU_HOAN_HANG: 7,
  DA_HOAN_HANG: 8,
  GIAO_THAT_BAI: 9,
  HOAN_KHO: 10
};

export const STATUS_LABELS = {
  0: 'Hóa đơn chờ',
  1: 'Chờ xác nhận',
  2: 'Đã xác nhận',
  3: 'Chờ vận chuyển',
  4: 'Đang vận chuyển',
  5: 'Đã hoàn thành',
  6: 'Đã hủy',
  7: 'Yêu cầu hoàn hàng',
  8: 'Đã hoàn hàng',
  9: 'Giao thất bại',
  10: 'Đã hoàn kho'
};

export const STATUS_FLOW = [1, 2, 3, 4, 5];

/** Bước kế tiếp hợp lệ (khớp HoaDonStatus.NEXT): 1→2→3→4→5, giao lại 9→4. */
const NEXT = { 1: 2, 2: 3, 3: 4, 4: 5, 9: 4 };
export const getNextStatus = (statusId) => NEXT[Number(statusId)] ?? null;

/** Hàng đang ở ngoài kho - khớp HoaDonStatus.isStockDeducted. */
export const isStockDeducted = (statusId) => [2, 3, 4, 5, 7, 9].includes(Number(statusId));

export const getStatusName = (statusId) => STATUS_LABELS[statusId] || 'Không xác định';

export const ADMIN_CANCELLABLE_FROM = new Set([1, 2, 3, 4]);
export const RETURN_CONFIRMABLE_FROM = new Set([5, 7]);
export const RETURN_REJECTABLE_FROM = new Set([7]);

export const canCancelOrder = (statusId) => ADMIN_CANCELLABLE_FROM.has(Number(statusId));
export const canConfirmReturn = (statusId) => RETURN_CONFIRMABLE_FROM.has(Number(statusId));
export const canRejectReturn = (statusId) => RETURN_REJECTABLE_FROM.has(Number(statusId));
export const canMarkDeliveryFailed = (statusId) => Number(statusId) === 4;
export const canRestock = (statusId) => Number(statusId) === 9;

/** Chuẩn hóa loại đơn: dữ liệu cũ 'TAI_QUAY' / 'ONLINE', code mới 'Tại quầy' / 'Online'. */
export const normalizeLoaiDon = (loaiDon) => {
  if (!loaiDon) return 'Tại quầy';
  const v = String(loaiDon).trim().toUpperCase();
  if (v === 'TAI_QUAY' || v === 'TẠI QUẦY') return 'Tại quầy';
  if (v === 'ONLINE') return 'Online';
  return loaiDon;
};
