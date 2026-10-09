/**
 * Service for formatting data display in invoice management
 */
import { getStatusName } from './hoaDonStatus.js';

// Format date for display
// LƯU Ý: backend trả thời gian dạng UTC (Instant/Date được Jackson serialize theo UTC -
// xem JacksonConfig). Trước đây hàm này ép hiển thị theo timeZone: 'UTC' khiến giờ hiển
// thị lệch 7 tiếng (và có thể lệch cả ngày) so với giờ Việt Nam thực tế -> hóa đơn hiện
// sai ngày/giờ. Ép về múi giờ Việt Nam cụ thể thay vì UTC hoặc để trình duyệt tự suy ra.
export const formatDate = (date) => {
  if (!date) return 'N/A';
  try {
    const d = new Date(date);
    if (isNaN(d.getTime())) return 'Invalid Date';
    return d.toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
      timeZone: 'Asia/Ho_Chi_Minh',
    });
  } catch (e) {
    console.error('Error formatting date:', date, e);
    return 'Invalid Date';
  }
};

// Format currency for display
export const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
  }).format(value || 0);
};

// Get status text by ID
export const getStatusText = (statusId) => getStatusName(statusId);

// Get status color classes
// Giao diện tối đa 3 màu: trạng thái đang xử lý dùng màu nhấn, còn lại dùng xám (xem assets/theme.css)
export const getStatusColor = (statusId) =>
  [1, 2, 3, 4, 9].includes(Number(statusId)) ? 'ui-badge on' : 'ui-badge';
