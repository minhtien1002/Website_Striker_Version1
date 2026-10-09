import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const voucherAPI = {
  // Lấy tất cả phiếu giảm giá
  getAllVouchers: async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/phieu-giam-gia`);
      return response;
    } catch (error) {
      console.error('Error fetching vouchers:', error);
      throw error;
    }
  },

  // Lấy phiếu giảm giá công khai còn hoạt động
  getActivePublicVouchers: async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/phieu-giam-gia/active/public`);
      return response;
    } catch (error) {
      console.error('Error fetching active public vouchers:', error);
      throw error;
    }
  },

  // Lấy phiếu giảm giá còn hoạt động cho khách hàng cụ thể (bao gồm cả công khai và cá nhân)
  getActiveVouchersForCustomer: async (customerId) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/phieu-giam-gia/active/customer/${customerId}`);
      return response;
    } catch (error) {
      console.error('Error fetching vouchers for customer:', error);
      throw error;
    }
  },

  // Lấy chi tiết phiếu giảm giá
  getVoucherDetail: async (id) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/phieu-giam-gia/detail/${id}`);
      return response;
    } catch (error) {
      console.error('Error fetching voucher detail:', error);
      throw error;
    }
  },

  // Thêm phiếu giảm giá mới
  createVoucher: async (voucherData) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/phieu-giam-gia/add`, voucherData);
      return response;
    } catch (error) {
      console.error('Error creating voucher:', error);
      throw error;
    }
  },

  // Cập nhật phiếu giảm giá
  updateVoucher: async (id, voucherData) => {
    try {
      const response = await axios.put(`${API_BASE_URL}/phieu-giam-gia/update/${id}`, voucherData);
      return response;
    } catch (error) {
      console.error('Error updating voucher:', error);
      throw error;
    }
  },

  // Xóa/Toggle trạng thái phiếu giảm giá
  deleteVoucher: async (id) => {
    try {
      const response = await axios.put(`${API_BASE_URL}/phieu-giam-gia/delete/${id}`);
      return response;
    } catch (error) {
      console.error('Error deleting voucher:', error);
      throw error;
    }
  },

  // Toggle khách hàng cho phiếu giảm giá cá nhân
  toggleCustomer: async (voucherId, customerId) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/phieu-giam-gia/${voucherId}/toggle-customer/${customerId}`);
      return response;
    } catch (error) {
      console.error('Error toggling customer:', error);
      throw error;
    }
  },

  // Áp dụng phiếu giảm giá cho hóa đơn
  applyVoucherToInvoice: async (voucherId, invoiceId) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/phieu-giam-gia/apply/${voucherId}/invoice/${invoiceId}`);
      return response;
    } catch (error) {
      console.error('Error applying voucher to invoice:', error);
      throw error;
    }
  },

  // Xóa phiếu giảm giá khỏi hóa đơn
  removeVoucherFromInvoice: async (invoiceId) => {
    try {
      const response = await axios.delete(`${API_BASE_URL}/phieu-giam-gia/remove/invoice/${invoiceId}`);
      return response;
    } catch (error) {
      console.error('Error removing voucher from invoice:', error);
      throw error;
    }
  },

  // Kiểm tra phiếu giảm giá có thể áp dụng cho khách hàng không
  canApplyVoucherForCustomer: async (voucherId, customerId) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/phieu-giam-gia/can-apply/${voucherId}/customer/${customerId}`);
      return response;
    } catch (error) {
      console.error('Error checking voucher applicability for customer:', error);
      throw error;
    }
  },

  // Kiểm tra phiếu giảm giá có thể áp dụng (phiếu công khai)
  canApplyVoucher: async (voucherId) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/phieu-giam-gia/can-apply/${voucherId}`);
      return response;
    } catch (error) {
      console.error('Error checking voucher applicability:', error);
      throw error;
    }
  }
};

export default voucherAPI;
