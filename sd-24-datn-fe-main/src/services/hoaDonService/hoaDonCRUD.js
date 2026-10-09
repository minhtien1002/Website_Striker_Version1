import { getAllHoaDon, updateHoaDon, deleteHoaDon } from '@/services/api/APIHoaDon/HoaDonAPI.js';
import { STATUS_LABELS, normalizeLoaiDon } from './hoaDonStatus.js';

/**
 * Service for CRUD operations on invoices
 */

// Bidirectional status mapping - nguồn dữ liệu gốc là STATUS_LABELS (hoaDonStatus.js)
const STATUS_MAPPING = {
  numberToString: STATUS_LABELS,
  stringToNumber: Object.fromEntries(Object.entries(STATUS_LABELS).map(([id, name]) => [name, Number(id)]))
};

// Helper functions for status mapping
export const getStatusNameById = (statusId) => {
  return STATUS_MAPPING.numberToString[statusId] || 'Không xác định';
};

export const getStatusIdByName = (statusName) => {
  return STATUS_MAPPING.stringToNumber[statusName] ?? null;
};

export const getAllStatusOptions = () => {
  return Object.entries(STATUS_MAPPING.numberToString).map(([id, name]) => ({
    id: parseInt(id),
    name: name
  }));
};

// Map backend DTO to frontend format
const mapBackendToFrontend = (backendInvoice) => {
  return {
    id: backendInvoice.id,
    maHoaDon: backendInvoice.ma,
    nhanVien: {
      id: backendInvoice.idNhanVien,
      maNhanVien: backendInvoice.maNhanVien
    },
    khachHang: {
      tenKhachHang: backendInvoice.tenKhachHang || 'Khách lẻ',
      soDienThoai: backendInvoice.soDienThoaiKhachHang || 'N/A'
    },
    trangThai: {
      id: backendInvoice.trangThai,
      name: getStatusNameById(backendInvoice.trangThai)
    },
    tongTienThanhToan: backendInvoice.tongTienSauGiam || 0,
    ngayTao: backendInvoice.ngayTao,
    loaiDon: normalizeLoaiDon(backendInvoice.loaiDon),
    phiVanChuyen: backendInvoice.phiVanChuyen || 0,
    idNhanVien: backendInvoice.idNhanVien,
    deleted: backendInvoice.deleted
  };
};


// Fetch all invoices from API
export const fetchAllInvoices = async (route, toast, filters = {}) => {
  console.log('Bắt đầu tải hóa đơn từ API...');

  try {
    // Prepare API parameters
    const params = {
      page: filters.page || 0,
      size: filters.size || 10,
      sortBy: filters.sortBy || 'id',
      sortDir: filters.sortDir || 'DESC'
    };

    // Add optional filters if they exist
    if (filters.keyword) params.keyword = filters.keyword;
    if (filters.minAmount) params.minAmount = filters.minAmount;
    if (filters.maxAmount) params.maxAmount = filters.maxAmount;
    if (filters.trangThai) params.trangThai = filters.trangThai;
    if (filters.loaiDon) params.loaiDon = filters.loaiDon;

    // Convert dates to timestamps if they exist.
    // new Date('yyyy-mm-dd') is UTC midnight (07:00 in VN), which dropped orders created
    // before 7am and made "Đến ngày" stop at 7am - use local start/end of day instead.
    if (filters.startDate) {
      const [y, m, d] = filters.startDate.split('-').map(Number);
      params.startDate = new Date(y, m - 1, d, 0, 0, 0, 0).getTime();
    }
    if (filters.endDate) {
      const [y, m, d] = filters.endDate.split('-').map(Number);
      params.endDate = new Date(y, m - 1, d, 23, 59, 59, 999).getTime();
    }

    console.log('API Parameters:', params);

    // Call the API
    const response = await getAllHoaDon(params);

    // Handle paginated response
    const rawInvoices = response.content || response || [];
    const totalElements = response.totalElements || rawInvoices.length;
    const totalPages = response.totalPages || 1;

    // Map backend DTO to frontend format
    const invoices = rawInvoices.map(mapBackendToFrontend);

    // Invoices loaded silently

    console.log('Kết thúc tải hóa đơn từ API:', { invoices, totalElements, totalPages });
    return {
      invoices,
      totalElements,
      totalPages,
      error: null
    };
  } catch (error) {
    console.error('Lỗi khi lấy dữ liệu hóa đơn từ API:', error);

    const errorMessage = `Lỗi khi tải dữ liệu từ API: ${error.message}`;
    toast.error(errorMessage, { timeout: 5000 });

    return {
      invoices: [],
      totalElements: 0,
      totalPages: 0,
      error: errorMessage
    };
  }
};

// Update invoice
export const updateInvoice = async (invoiceData, toast, emitter) => {
  try {
    const updatedStatusId = parseInt(invoiceData.trangThai.id);
    await updateHoaDon(invoiceData.id, {
      maHoaDon: invoiceData.maHoaDon,
      khachHang: {
        tenKhachHang: invoiceData.khachHang.tenKhachHang || 'Khách lẻ',
        soDienThoai: invoiceData.khachHang.soDienThoai || '',
      },
      trangThai: { id: updatedStatusId },
      tongTienThanhToan: invoiceData.tongTienThanhToan,
    });

    // Invoice updated silently

    if (updatedStatusId === 21) {
      emitter.emit('invoice-completed', invoiceData);
      console.log('Đã phát ra sự kiện invoice-completed với hóa đơn:', invoiceData);
    }

    return { success: true, error: null };
  } catch (error) {
    console.error('Lỗi khi cập nhật hóa đơn:', error.response ? error.response.data : error.message);
    const errorMessage = `Lỗi khi cập nhật hóa đơn: ${error.response?.data?.message || error.message}`;
    toast.error(errorMessage);
    return { success: false, error: errorMessage };
  }
};

// Delete invoice
export const deleteInvoice = async (invoiceId, toast) => {
  try {
    await deleteHoaDon(invoiceId);
    // Invoice deleted silently
    return { success: true, error: null };
  } catch (error) {
    console.error('Lỗi khi xóa hóa đơn:', error.response ? error.response.data : error.message);
    const errorMessage = `Lỗi khi xóa hóa đơn: ${error.response?.data?.message || error.message}`;
    toast.error(errorMessage);
    return { success: false, error: errorMessage };
  }
};
