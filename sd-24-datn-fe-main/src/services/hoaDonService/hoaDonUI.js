/**
 * Service for UI-related functions and modal handling
 */

// Edit invoice modal handling
export const prepareEditInvoice = (invoice) => {
  return {
    id: invoice.id,
    maHoaDon: invoice.maHoaDon,
    khachHang: {
      tenKhachHang: invoice.khachHang?.tenKhachHang || 'Khách lẻ',
      soDienThoai: invoice.khachHang?.soDienThoai || '',
    },
    trangThai: { id: invoice.trangThai?.id || 6 },
    tongTienThanhToan: invoice.tongTienThanhToan || 0,
  };
};

// Validate invoice data before update
export const validateInvoiceData = (invoiceData, toast) => {
  if (!invoiceData.maHoaDon) {
    toast.error('Mã hóa đơn không được để trống!');
    return false;
  }
  if (!invoiceData.tongTienThanhToan) {
    toast.error('Tổng tiền thanh toán không hợp lệ!');
    return false;
  }
  return true;
};

// Handle invoice highlighting from URL query
export const handleInvoiceHighlight = (maHoaDon) => {
  setTimeout(() => {
    const element = document.querySelector(`tr[data-ma-hoa-don="${maHoaDon}"]`);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'center' });
      element.classList.add('highlight');
      setTimeout(() => element.classList.remove('highlight'), 3000);
    }
  }, 500);
};

// Navigation helper
export const navigateToSalesCounter = (router) => {
  router.push({ name: 'BanTaiQuay' });
};
