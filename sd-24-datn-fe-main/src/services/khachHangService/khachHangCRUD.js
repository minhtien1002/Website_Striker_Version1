import { khachHangConfig } from './khachHangConfig.js'
import { validateCustomer } from './khachHangValidation.js'
import khachHangAPI from '@/services/api/APIKhachHang/KhachHangAPI.js'

// Map backend response to frontend format
const mapBackendToFrontend = (backendCustomer) => {
  // Convert Instant to date string if needed
  const formatDate = (instant) => {
    if (!instant) return ''
    if (typeof instant === 'string' && instant.includes('T')) {
      return instant.split('T')[0] // Extract date part from ISO string
    }
    return instant
  }

  // Convert gender from Short to Boolean (0/1 to false/true)
  const formatGender = (gioiTinh) => {
    if (gioiTinh === null || gioiTinh === undefined) return true
    return gioiTinh === 1 || gioiTinh === true
  }

  return {
    id: backendCustomer.id,
    maKhachHang: backendCustomer.ma || backendCustomer.maKhachHang,
    tenKhachHang: backendCustomer.ten || backendCustomer.tenKhachHang,
    email: backendCustomer.email || '',
    soDienThoai: backendCustomer.soDienThoai || '',
    gioiTinh: formatGender(backendCustomer.gioiTinh),
    ngaySinh: formatDate(backendCustomer.ngaySinh),
    cccd: backendCustomer.cccd || '',
    ngayTao: backendCustomer.ngayTao || backendCustomer.createdAt,
    ngayCapNhat: backendCustomer.ngayCapNhat || backendCustomer.updatedAt,
    trangThai: backendCustomer.deleted !== undefined ? backendCustomer.deleted : true,
    taiKhoanID: backendCustomer.taiKhoanId || backendCustomer.taiKhoanID,
    
    // Address information - map from backend fields
    diaChiSoNhaTenDuong: backendCustomer.diaChiCuThe || '',
    diaChiPhuongXa: backendCustomer.phuong || '',
    diaChiQuanHuyen: backendCustomer.quan || '',
    diaChiTinhThanh: backendCustomer.thanhPho || '',
    
    // Store original address data for reference
    diaChi: backendCustomer.diaChi || null,
    
    // Additional fields that might be needed
    tenDangNhap: backendCustomer.tenDangNhap || '',
    createdBy: backendCustomer.createdBy,
    updatedBy: backendCustomer.updatedBy
  }
}

// CRUD operations for customers
export const fetchAllCustomers = async (toast, params = {}) => {
  console.log('Bắt đầu tải khách hàng từ API...', params)

  try {
    const response = await khachHangAPI.getAllKhachHang(params)
    const backendCustomers = response.content || []

    // Map backend format to frontend format
    const customers = backendCustomers.map(mapBackendToFrontend)

    if (customers.length === 0) {
      toast.info('Không có khách hàng nào trong hệ thống.', { timeout: 4000 })
    }

    return {
      content: customers,
      totalElements: response.totalItems || 0,
      totalPages: response.totalPages || 0,
      currentPage: response.currentPage || 0,
      size: response.size || 10,
      hasNext: response.hasNext || false,
      hasPrevious: response.hasPrevious || false
    }
  } catch (error) {
    console.error('Lỗi khi lấy dữ liệu khách hàng từ API:', error)

    // No fallback to fake data - show error and return empty result
    const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi tải dữ liệu khách hàng'
    toast.error(errorMessage)

    return {
      content: [],
      totalElements: 0,
      totalPages: 0,
      currentPage: 0,
      size: params.size || 10,
      hasNext: false,
      hasPrevious: false
    }
  }
}

export const createCustomer = async (customerData, toast) => {
  if (!validateCustomer(customerData, toast)) return null

  try {
    // Map frontend data to backend format
    const backendData = {
      // KhachHang fields
      ten: customerData.tenKhachHang,
      ngaySinh: customerData.ngaySinh,
      gioiTinh: customerData.gioiTinh,
      cccd: customerData.cccd,

      // TaiKhoan fields
      email: customerData.email || `${customerData.tenKhachHang.toLowerCase().replace(/\s+/g, '')}@customer.com`,
      soDienThoai: customerData.soDienThoai,
      matKhau: '123456', // Default password

      // Address fields
      thanhPho: customerData.diaChiTinhThanh || '',
      quan: customerData.diaChiQuanHuyen || '',
      phuong: customerData.diaChiPhuongXa || '',
      diaChiCuThe: customerData.diaChiSoNhaTenDuong || ''
    }

    const response = await khachHangAPI.createKhachHang(backendData)

    if (response.data) {
      toast.success(response.message || 'Thêm khách hàng thành công!')
      // Map backend response to frontend format
      return mapBackendToFrontend(response.data)
    }

    throw new Error('Không nhận được dữ liệu từ server')
  } catch (error) {
    console.error('Lỗi khi thêm khách hàng:', error)
    const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi thêm khách hàng'
    toast.error(errorMessage)
    throw error
  }
}

export const updateCustomer = async (customerId, customerData, toast) => {
  if (!validateCustomer(customerData, toast)) return null

  try {
    // Map frontend data to backend format (matching backend DTO fields)
    const backendData = {
      // KhachHang fields - use backend field names
      ten: customerData.tenKhachHang,
      ngaySinh: customerData.ngaySinh,
      gioiTinh: customerData.gioiTinh ? 1 : 0, // Convert Boolean to Short (1/0)
      cccd: customerData.cccd,

      // TaiKhoan fields
      email: customerData.email,
      soDienThoai: customerData.soDienThoai,

      // Address fields - use backend field names
      thanhPho: customerData.diaChiTinhThanh || '',
      quan: customerData.diaChiQuanHuyen || '',
      phuong: customerData.diaChiPhuongXa || '',
      diaChiCuThe: customerData.diaChiSoNhaTenDuong || ''
    }

    const response = await khachHangAPI.updateKhachHang(customerId, backendData)

    if (response.data) {
      toast.success(response.message || 'Cập nhật khách hàng thành công!')
      // Map backend response to frontend format
      return mapBackendToFrontend(response.data)
    }

    throw new Error('Không nhận được dữ liệu từ server')
  } catch (error) {
    console.error('Lỗi khi cập nhật khách hàng:', error)
    const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi cập nhật khách hàng'
    toast.error(errorMessage)
    throw error
  }
}

export const toggleCustomerStatus = async (customer, toast) => {
  try {
    if (customer.trangThai) {
      // Currently active (switch ON) -> Turn OFF (delete)
      const response = await khachHangAPI.deleteKhachHang(customer.id)
      toast.success(response.message || `Đã vô hiệu hóa khách hàng ${customer.tenKhachHang}!`)
    } else {
      // Currently inactive (switch OFF) -> Turn ON (restore)
      const response = await khachHangAPI.restoreKhachHang(customer.id)
      toast.success(response.message || `Đã kích hoạt khách hàng ${customer.tenKhachHang}!`)
    }

    return {
      ...customer,
      trangThai: !customer.trangThai
    }
  } catch (error) {
    console.error('Lỗi khi cập nhật trạng thái khách hàng:', error)
    const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi cập nhật trạng thái khách hàng'
    toast.error(errorMessage)
    throw error
  }
}

export const resetCustomerForm = () => {
  return { ...khachHangConfig.defaultCustomer }
}

// Get customer by ID
export const getCustomerById = async (id, toast) => {
  try {
    const response = await khachHangAPI.getKhachHangById(id)

    if (response.data) {
      // Map backend response to frontend format
      return mapBackendToFrontend(response.data)
    }

    throw new Error('Không tìm thấy thông tin khách hàng')
  } catch (error) {
    console.error('Lỗi khi lấy thông tin khách hàng:', error)
    const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi lấy thông tin khách hàng'
    toast.error(errorMessage)
    throw error
  }
}
