/**
 * CRUD operations for HoaDonChiTiet
 */

import { updateHoaDonStatus, cancelHoaDon, confirmReturnHoaDon, rejectReturnHoaDon } from '@/services/api/APIHoaDon/HoaDonChiTietAPI.js';
import { getStatusName, isStockDeducted } from '@/services/hoaDonService/hoaDonStatus.js';

// Fetch invoice detail from API
export const fetchInvoiceDetail = async (invoiceId, loadInvoiceDetail) => {
  try {
    await loadInvoiceDetail(invoiceId);
  } catch (error) {
    console.error('Error loading invoice detail:', error);
    throw error;
  }
};

// Update order status via API
export const updateOrderStatusAPI = async (invoiceId, statusId, note = '') => {
  try {
    // Call the real API with note parameter
    const response = await updateHoaDonStatus(invoiceId, parseInt(statusId), null, note);
    
    return {
      success: true,
      status: {
        id: response.trangThai,
        name: getStatusName(response.trangThai)
      },
      message: 'Cập nhật trạng thái thành công',
      data: response
    };
  } catch (error) {
    console.error('Error updating order status:', error);
    throw error;
  }
};

// Update to specific status
export const updateToStatusAPI = async (invoiceId, statusValue) => {
  try {
    // Call the real API with status ID
    const statusId = typeof statusValue === 'object' ? statusValue.id : statusValue;
    const response = await updateHoaDonStatus(invoiceId, statusId);
    
    return {
      success: true,
      status: {
        id: response.trangThai,
        name: getStatusName(response.trangThai)
      },
      message: `Đã cập nhật trạng thái thành: ${getStatusName(response.trangThai)}`,
      data: response
    };
  } catch (error) {
    console.error('Error updating to status:', error);
    throw error;
  }
};

// Cancel an order
export const cancelOrderAPI = async (invoiceId, idNhanVien, lyDo, trangThaiTruocKhiHuy = null) => {
  try {
    const response = await cancelHoaDon(invoiceId, idNhanVien, lyDo);
    // Tồn kho chỉ được cộng trả nếu đơn ĐÃ từng bị trừ kho (tức đã qua bước "Đã xác nhận").
    // Đơn còn ở "Chờ xác nhận" chưa hề đụng tới kho nên thông báo phải nói đúng như vậy,
    // nếu không người dùng sẽ tưởng kho vừa tăng lên và đi đối soát nhầm.
    const daTruKho = trangThaiTruocKhiHuy !== null && isStockDeducted(trangThaiTruocKhiHuy);
    return {
      success: true,
      status: { id: response.trangThai, name: getStatusName(response.trangThai) },
      message: daTruKho
        ? 'Đã hủy đơn hàng và cộng lại tồn kho'
        : 'Đã hủy đơn hàng (đơn chưa xác nhận nên tồn kho không thay đổi)',
      data: response
    };
  } catch (error) {
    console.error('Error cancelling order:', error);
    throw error;
  }
};

// Confirm returned goods received back at the store (adds stock back)
export const confirmReturnAPI = async (invoiceId, idNhanVien, ghiChu) => {
  try {
    const response = await confirmReturnHoaDon(invoiceId, idNhanVien, ghiChu);
    return {
      success: true,
      status: { id: response.trangThai, name: getStatusName(response.trangThai) },
      message: 'Đã xác nhận hoàn hàng và cộng lại tồn kho',
      data: response
    };
  } catch (error) {
    console.error('Error confirming return:', error);
    throw error;
  }
};

// Reject a customer's return request
export const rejectReturnAPI = async (invoiceId, idNhanVien, lyDo) => {
  try {
    const response = await rejectReturnHoaDon(invoiceId, idNhanVien, lyDo);
    return {
      success: true,
      status: { id: response.trangThai, name: getStatusName(response.trangThai) },
      message: 'Đã từ chối yêu cầu hoàn hàng',
      data: response
    };
  } catch (error) {
    console.error('Error rejecting return:', error);
    throw error;
  }
};
