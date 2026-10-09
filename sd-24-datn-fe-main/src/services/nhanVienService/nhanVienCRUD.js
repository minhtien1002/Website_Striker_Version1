import { nhanVienConfig } from './nhanVienConfig.js'
import { validateEmployee } from './nhanVienValidation.js'
import nhanVienAPI from '@/services/api/APINhanVien/NhanVienAPI.js'

// Map backend response to frontend format
const mapBackendToFrontend = (backendEmployee) => {
  return {
    id: backendEmployee.id,
    maNhanVien: backendEmployee.ma, // Backend uses 'ma', frontend expects 'maNhanVien'
    tenNhanVien: backendEmployee.tenNhanVien,
    ngaySinh: backendEmployee.ngaySinh,
    gioiTinh: backendEmployee.gioiTinh,
    soDienThoai: backendEmployee.soDienThoai,
    cccd: backendEmployee.cccd,
    diaChiSoNhaTenDuong: backendEmployee.diaChiCuThe || '', // Backend uses 'diaChiCuThe'
    diaChiPhuongXa: '', // Not provided by backend
    diaChiQuanHuyen: '', // Not provided by backend  
    diaChiTinhThanh: '', // Not provided by backend
    trangThai: backendEmployee.deletedTrangThai, // Backend: deletedTrangThai=true means active, false means inactive
    ngayTao: backendEmployee.ngayTao,
    ngayCapNhat: backendEmployee.ngayCapNhat
  }
}

// CRUD operations for employees
export const fetchAllEmployees = async (toast, params = {}) => {
  console.log('Bắt đầu tải nhân viên từ API...', params)
  
  try {
    const response = await nhanVienAPI.getAllNhanVien(params)
    const backendEmployees = response.content || []
    
    // Map backend format to frontend format
    const employees = backendEmployees.map(mapBackendToFrontend)
    
    if (employees.length === 0) {
      toast.info('Không có nhân viên nào trong hệ thống.', { timeout: 4000 })
    }
    
    return {
      content: employees,
      totalElements: response.totalItems || 0,
      totalPages: response.totalPages || 0,
      currentPage: response.currentPage || 0,
      size: response.size || 10,
      hasNext: response.hasNext || false,
      hasPrevious: response.hasPrevious || false
    }
  } catch (error) {
    console.error('Lỗi khi lấy dữ liệu nhân viên từ API:', error)
    
    // No fallback to fake data - show error and return empty result
    const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi tải dữ liệu nhân viên'
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

export const createEmployee = async (employeeData, toast) => {
  if (!validateEmployee(employeeData, toast)) return null
  
  try {
    // Map frontend data to backend format
    const backendData = {
      // NhanVien fields
      tenNhanVien: employeeData.tenNhanVien,
      ngaySinh: employeeData.ngaySinh,
      gioiTinh: employeeData.gioiTinh,
      cccd: employeeData.cccd,
      diaChiCuThe: employeeData.diaChiSoNhaTenDuong,
      thanhPho: employeeData.diaChiTinhThanh,
      quan: employeeData.diaChiQuanHuyen,
      phuong: employeeData.diaChiPhuongXa,
      
      // TaiKhoan fields - let backend auto-generate username
      tenDangNhap: null, // Let backend generate from name and birthdate
      email: employeeData.email || `${employeeData.tenNhanVien.toLowerCase().replace(/\s+/g, '')}@company.com`,
      soDienThoai: employeeData.soDienThoai,
      matKhau: '123456', // Default password
      idQuyenHan: 2 // Default role ID for employee
    }
    
    const response = await nhanVienAPI.createNhanVien(backendData)
    
    if (response.data) {
      toast.success(response.message || 'Thêm nhân viên thành công!')
      // Map backend response to frontend format
      return mapBackendToFrontend(response.data)
    }
    
    throw new Error('Không nhận được dữ liệu từ server')
  } catch (error) {
    console.error('Lỗi khi thêm nhân viên:', error)
    const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi thêm nhân viên'
    toast.error(errorMessage)
    throw error
  }
}

export const updateEmployee = async (employeeId, employeeData, toast) => {
  if (!validateEmployee(employeeData, toast)) return null
  
  try {
    // Map frontend data to backend format
    const backendData = {
      // NhanVien fields
      tenNhanVien: employeeData.tenNhanVien,
      ngaySinh: employeeData.ngaySinh,
      gioiTinh: employeeData.gioiTinh,
      cccd: employeeData.cccd,
      diaChiCuThe: employeeData.diaChiSoNhaTenDuong,
      thanhPho: employeeData.diaChiTinhThanh,
      quan: employeeData.diaChiQuanHuyen,
      phuong: employeeData.diaChiPhuongXa,
      
      // TaiKhoan fields
      email: employeeData.email,
      soDienThoai: employeeData.soDienThoai
      // Do not send deleted field during regular updates
    }
    
    const response = await nhanVienAPI.updateNhanVien(employeeId, backendData)
    
    if (response.data) {
      toast.success(response.message || 'Cập nhật nhân viên thành công!')
      // Map backend response to frontend format
      return mapBackendToFrontend(response.data)
    }
    
    throw new Error('Không nhận được dữ liệu từ server')
  } catch (error) {
    console.error('Lỗi khi cập nhật nhân viên:', error)
    const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi cập nhật nhân viên'
    toast.error(errorMessage)
    throw error
  }
}

export const toggleEmployeeStatus = async (employee, toast) => {
  try {
    const newStatus = !employee.trangThai
    
    if (newStatus) {
      // Restore employee
      const response = await nhanVienAPI.restoreNhanVien(employee.id)
      toast.success(response.message || `Đã kích hoạt nhân viên ${employee.tenNhanVien}!`)
    } else {
      // Delete employee (soft delete)
      const response = await nhanVienAPI.deleteNhanVien(employee.id)
      toast.success(response.message || `Đã vô hiệu hóa nhân viên ${employee.tenNhanVien}!`)
    }
    
    return {
      ...employee,
      trangThai: newStatus
    }
  } catch (error) {
    console.error('Lỗi khi cập nhật trạng thái nhân viên:', error)
    const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi cập nhật trạng thái nhân viên'
    toast.error(errorMessage)
    throw error
  }
}

export const resetEmployeeForm = () => {
  return { ...nhanVienConfig.defaultEmployee }
}

// Get employee by ID
export const getEmployeeById = async (id, toast) => {
  try {
    const response = await nhanVienAPI.getNhanVienById(id)
    
    if (response.data) {
      // Map backend response to frontend format
      return mapBackendToFrontend(response.data)
    }
    
    throw new Error('Không tìm thấy thông tin nhân viên')
  } catch (error) {
    console.error('Lỗi khi lấy thông tin nhân viên:', error)
    const errorMessage = error.response?.data?.error || error.message || 'Lỗi khi lấy thông tin nhân viên'
    toast.error(errorMessage)
    throw error
  }
}

