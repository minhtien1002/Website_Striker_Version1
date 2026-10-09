/**
 * UI-related functions for HoaDonChiTiet
 */

import { orderStatuses } from './hoaDonChiTietConfig.js';

// Format date for short display
export const formatShortDate = (date) => {
  if (!date) return '';
  return new Date(date).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
  });
};

// Get order statuses based on invoice type
export const getOrderStatuses = (invoiceDetail) => {
  if (!invoiceDetail) return [];
  
  if (invoiceDetail.loaiDon === 'Tại quầy') {
    return orderStatuses.direct;
  }
  
  // Trạng thái 6/7/8 (Đã hủy / Yêu cầu hoàn hàng / Đã hoàn hàng) không thuộc luồng tiến
  // tuần tự bình thường nên chỉ hiện trên timeline khi đơn đang thực sự ở trạng thái đó,
  // tránh làm rối luồng chính.
  //
  // Mã 3 ("Chờ vận chuyển") đã bị bỏ khỏi hệ thống, timeline là:
  // Hóa đơn chờ -> Chờ xác nhận -> Đã xác nhận -> Đang vận chuyển -> Đã hoàn thành.
  const currentStatusId = invoiceDetail.trangThai.id;
  const branchIds = [6, 7, 8];

  return orderStatuses.online.filter(status => {
    if (branchIds.includes(status.id)) return status.id === currentStatusId;
    return true;
  });
};

// Calculate progress width for timeline
export const getProgressWidth = (invoiceDetail) => {
  if (!invoiceDetail) return 0;
  const statuses = getOrderStatuses(invoiceDetail);
  const currentIndex = statuses.findIndex(s => s.id === invoiceDetail.trangThai.id);
  if (currentIndex === -1) return 0;
  return ((currentIndex + 1) / statuses.length) * 100;
};

// Get status step class for timeline
export const getStatusStepClass = (status, index, invoiceDetail) => {
  if (!invoiceDetail) return 'bg-gray-200 border-gray-300 text-gray-400';
  
  const currentIndex = getOrderStatuses(invoiceDetail).findIndex(s => s.id === invoiceDetail.trangThai.id);
  
  if (index <= currentIndex) {
    // Completed or current status
    if (status.id === 6) {
      // Cancelled status
      return 'bg-red-500 border-red-500 text-white';
    }
    if (status.id === 7 || status.id === 8) {
      // Return flow
      return 'bg-amber-500 border-amber-500 text-white';
    }
    return 'bg-blue-500 border-blue-500 text-white';
  } else {
    // Future status
    return 'bg-gray-200 dark:bg-gray-600 border-gray-300 dark:border-gray-500 text-gray-400 dark:text-gray-300';
  }
};

// Get status date from history
export const getStatusDate = (status, invoiceDetail) => {
  const history = invoiceDetail?.lichSuHoaDonInfos?.find(h => h.trangThai === status.id);
  return history ? history.thoiGian : null;
};

// Check if can update to specific status
export const canUpdateToStatus = (status, invoiceDetail) => {
  if (!invoiceDetail || invoiceDetail.loaiDon === 'Tại quầy') return false; // đơn tại quầy không có bước vận chuyển
  
  const currentIndex = getOrderStatuses(invoiceDetail).findIndex(s => s.id === invoiceDetail.trangThai.id);
  const statusIndex = getOrderStatuses(invoiceDetail).findIndex(s => s.id === status.id);
  
  // Can only update to the next status in sequence
  return statusIndex === currentIndex + 1 && invoiceDetail.trangThai.id !== 6 && invoiceDetail.trangThai.id !== 5;
};

// Get available statuses for the generic "Cập nhật trạng thái" modal.
// LƯU Ý: chỉ liệt kê các trạng thái 0..5 (luồng tiến tuần tự bình thường) - backend
// (HoaDonStatus.GENERIC_UPDATABLE) từ chối mọi request set thẳng trạng thái 6/7/8 qua
// endpoint này, những trạng thái đó bắt buộc phải đi qua nút Hủy đơn / Xác nhận hoàn
// hàng / Từ chối hoàn hàng riêng (xem canCancelOrder/canConfirmReturn/canRejectReturn).
export const getAvailableStatuses = (invoiceDetail) => {
  if (!invoiceDetail) return [];

  if (invoiceDetail.loaiDon === 'Tại quầy') {
    return []; // No status changes for direct purchases
  }

  const currentStatusId = Number(invoiceDetail.trangThai.id);

  // Đơn đã hủy, đã hoàn thành, hoặc đang trong luồng hoàn hàng: không cập nhật qua đây.
  if ([5, 6, 7, 8].includes(currentStatusId)) {
    return [];
  }

  // Chỉ cho chọn các trạng thái ĐỨNG SAU trạng thái hiện tại trong luồng. Bản cũ liệt kê
  // toàn bộ 0..5 trừ trạng thái hiện tại, tức là cho phép chọn cả trạng thái lùi - backend
  // giờ từ chối việc lùi (vì sẽ làm trừ kho hai lần), nên hiện ra chỉ tổ báo lỗi.
  // Mã 3 bị loại vì đã không còn trong luồng.
  const flowOrder = [0, 1, 2, 4, 5];
  const currentIndex = flowOrder.indexOf(currentStatusId === 3 ? 2 : currentStatusId);
  if (currentIndex === -1) return [];

  const nextIds = flowOrder.slice(currentIndex + 1);
  return orderStatuses.online
    .filter(s => nextIds.includes(s.id))
    .sort((a, b) => a.id - b.id);
};

// Print invoice function - Download PDF from backend
export const printInvoice = async (invoiceDetail) => {
  if (!invoiceDetail) {
    console.error('No invoice detail provided for printing');
    return;
  }

  try {
    // Import API function dynamically
    const { downloadInvoicePDF } = await import('../api/APIHoaDon/HoaDonAPI.js');
    
    // Download PDF from backend using invoice ID
    const pdfBlob = await downloadInvoicePDF(invoiceDetail.id);
    
    // Create download link
    const url = window.URL.createObjectURL(pdfBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `HoaDon_${invoiceDetail.maHoaDon}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    console.log(`PDF downloaded for invoice: ${invoiceDetail.maHoaDon} (ID: ${invoiceDetail.id})`);
  } catch (error) {
    console.error('Error downloading PDF:', error);
    
    // Fallback to browser print if backend fails
    window.print();
  }
};
