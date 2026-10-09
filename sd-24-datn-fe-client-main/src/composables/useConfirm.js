import { reactive, readonly } from 'vue';

/**
 * Hộp thoại xác nhận dùng chung cho toàn bộ trang admin.
 *
 * Trước đây mỗi màn tự dựng một modal xác nhận riêng (showDeleteModal ở SanPham,
 * ChatLieu, MauSac, ...) nên: giao diện mỗi nơi một kiểu, và rất nhiều thao tác quan
 * trọng - đổi trạng thái hóa đơn, bật/tắt bán sản phẩm, xóa biến thể - lại KHÔNG hỏi
 * gì mà chạy luôn. State ở đây là singleton (khai báo ngoài hàm) nên mọi component
 * dùng chung đúng một hộp thoại, chỉ cần mount <ConfirmDialog /> một lần ở App.vue.
 *
 * Cách dùng:
 * ```js
 * const { confirm } = useConfirm();
 * const ok = await confirm({
 *   title: 'Xác nhận đơn hàng',
 *   message: 'Tồn kho sẽ bị trừ ngay sau bước này.',
 *   confirmText: 'Xác nhận',
 *   tone: 'warning'
 * });
 * if (!ok) return;
 * ```
 */

const state = reactive({
  open: false,
  title: '',
  message: '',
  details: [],
  confirmText: 'Đồng ý',
  cancelText: 'Hủy bỏ',
  /** 'danger' | 'warning' | 'info' - quyết định màu nút và icon. */
  tone: 'info',
  /** Khi true, người dùng phải nhập lý do mới bấm được nút đồng ý. */
  requireReason: false,
  reasonLabel: 'Lý do',
  reasonPlaceholder: 'Nhập lý do...',
  reason: '',
  loading: false
});

// Hàm resolve của Promise đang chờ. Giữ ngoài reactive vì đây là hàm, không phải dữ liệu.
let resolvePromise = null;

/**
 * Mở hộp thoại và trả về Promise.
 * - Khi requireReason = false: resolve true/false.
 * - Khi requireReason = true: resolve chuỗi lý do (luôn khác rỗng) nếu đồng ý, false nếu hủy.
 */
const confirm = (options = {}) => {
  state.title = options.title || 'Xác nhận thao tác';
  state.message = options.message || 'Bạn có chắc chắn muốn thực hiện thao tác này?';
  state.details = options.details || [];
  state.confirmText = options.confirmText || 'Đồng ý';
  state.cancelText = options.cancelText || 'Hủy bỏ';
  state.tone = options.tone || 'info';
  state.requireReason = Boolean(options.requireReason);
  state.reasonLabel = options.reasonLabel || 'Lý do';
  state.reasonPlaceholder = options.reasonPlaceholder || 'Nhập lý do...';
  state.reason = '';
  state.loading = false;
  state.open = true;

  return new Promise((resolve) => {
    resolvePromise = resolve;
  });
};

const settle = (value) => {
  state.open = false;
  state.loading = false;
  const resolver = resolvePromise;
  resolvePromise = null;
  if (resolver) resolver(value);
};

const accept = () => {
  if (state.requireReason) {
    const reason = state.reason.trim();
    // Chặn ngay ở client để không phải đi một vòng lên server rồi mới báo thiếu lý do.
    if (!reason) return;
    settle(reason);
    return;
  }
  settle(true);
};

const cancel = () => settle(false);

export function useConfirm() {
  return {
    state: readonly(state),
    // Ghi vào ô lý do cần quyền ghi, nên expose riêng thay vì mở khóa cả state.
    setReason: (value) => { state.reason = value; },
    confirm,
    accept,
    cancel
  };
}
