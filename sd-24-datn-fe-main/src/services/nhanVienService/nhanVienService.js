import nhanVienAPI from '../api/APINhanVien/NhanVienAPI.js'

const nhanVienService = {
  // Transform backend data to frontend format
  mapBackendToFrontend: (backendData) => {
    return {
      id: backendData.id,
      ma: backendData.ma,
      tenNhanVien: backendData.tenNhanVien,
      ngaySinh: backendData.ngaySinh,
      gioiTinh: backendData.gioiTinh,
      soDienThoai: backendData.soDienThoai,
      cccd: backendData.cccd,
      diaChiCuThe: backendData.diaChiCuThe,
      trangThai: !backendData.deletedTrangThai, // Convert deleted flag to active status
      deletedTrangThai: backendData.deletedTrangThai
    }
  },

  // Transform frontend data to backend format for create
  mapFrontendToBackendCreate: (frontendData) => {
    return {
      // NhanVien fields
      ma: frontendData.ma || null,
      tenNhanVien: frontendData.tenNhanVien,
      ngaySinh: frontendData.ngaySinh,
      anhNhanVien: frontendData.anhNhanVien || null,
      ghiChu: frontendData.ghiChu || null,
      thanhPho: frontendData.thanhPho || null,
      quan: frontendData.quan || null,
      phuong: frontendData.phuong || null,
      diaChiCuThe: frontendData.diaChiCuThe,
      cccd: frontendData.cccd,
      gioiTinh: frontendData.gioiTinh,
      
      // TaiKhoan fields
      idQuyenHan: frontendData.idQuyenHan || 2, // Default to employee role
      tenDangNhap: frontendData.tenDangNhap,
      email: frontendData.email,
      soDienThoai: frontendData.soDienThoai,
      matKhau: frontendData.matKhau
    }
  },

  // Transform frontend data to backend format for update
  mapFrontendToBackendUpdate: (frontendData) => {
    return {
      id: frontendData.id,
      tenNhanVien: frontendData.tenNhanVien,
      ngaySinh: frontendData.ngaySinh,
      anhNhanVien: frontendData.anhNhanVien,
      ghiChu: frontendData.ghiChu,
      thanhPho: frontendData.thanhPho,
      quan: frontendData.quan,
      phuong: frontendData.phuong,
      diaChiCuThe: frontendData.diaChiCuThe,
      cccd: frontendData.cccd,
      gioiTinh: frontendData.gioiTinh,
      
      // TaiKhoan fields
      email: frontendData.email,
      soDienThoai: frontendData.soDienThoai,
      deleted: frontendData.deleted
    }
  },

  // Get all employees with filtering and pagination
  getAllEmployees: async (filters = {}) => {
    try {
      const params = {
        page: filters.page || 0,
        size: filters.size || 10,
        keyword: filters.search || '',
        gioiTinh: filters.gioiTinh !== '' ? filters.gioiTinh : null,
        trangThai: filters.trangThai !== '' ? filters.trangThai === 'true' : null,
        sortBy: filters.sortBy || 'id',
        sortDir: filters.sortDir || 'desc'
      }

      const response = await nhanVienAPI.getAllNhanVien(params)
      
      // Transform backend data to frontend format
      if (response.content) {
        response.content = response.content.map(employee => 
          nhanVienService.mapBackendToFrontend(employee)
        )
      }

      return response
    } catch (error) {
      console.error('Service error getting employees:', error)
      throw error
    }
  },

  // Get employee by ID
  getEmployeeById: async (id) => {
    try {
      const response = await nhanVienAPI.getNhanVienById(id)
      return response.data
    } catch (error) {
      console.error('Service error getting employee by ID:', error)
      throw error
    }
  },

  // Create new employee
  createEmployee: async (employeeData) => {
    try {
      const backendData = nhanVienService.mapFrontendToBackendCreate(employeeData)
      const response = await nhanVienAPI.createNhanVien(backendData)
      return response
    } catch (error) {
      console.error('Service error creating employee:', error)
      throw error
    }
  },

  // Update employee
  updateEmployee: async (id, employeeData) => {
    try {
      const backendData = nhanVienService.mapFrontendToBackendUpdate(employeeData)
      const response = await nhanVienAPI.updateNhanVien(id, backendData)
      return response
    } catch (error) {
      console.error('Service error updating employee:', error)
      throw error
    }
  },

  // Delete employee (soft delete)
  deleteEmployee: async (id) => {
    try {
      const response = await nhanVienAPI.deleteNhanVien(id)
      return response
    } catch (error) {
      console.error('Service error deleting employee:', error)
      throw error
    }
  },

  // Restore employee
  restoreEmployee: async (id) => {
    try {
      const response = await nhanVienAPI.restoreNhanVien(id)
      return response
    } catch (error) {
      console.error('Service error restoring employee:', error)
      throw error
    }
  },

  // Calculate age from birth date
  calculateAge: (birthDate) => {
    if (!birthDate) return 0
    const today = new Date()
    const birth = new Date(birthDate)
    let age = today.getFullYear() - birth.getFullYear()
    const monthDiff = today.getMonth() - birth.getMonth()
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--
    }
    
    return age
  },

  // Validate employee data
  validateEmployeeData: (employeeData, isUpdate = false) => {
    const errors = []

    if (!employeeData.tenNhanVien || employeeData.tenNhanVien.trim() === '') {
      errors.push('Tên nhân viên là bắt buộc')
    }

    if (!employeeData.ngaySinh) {
      errors.push('Ngày sinh là bắt buộc')
    } else {
      // Validate age - employee must be 18 or older
      const age = nhanVienService.calculateAge(employeeData.ngaySinh)
      if (age < 18) {
        errors.push('Nhân viên phải từ 18 tuổi trở lên')
      }
      
      // Check if birth date is not in the future
      const today = new Date()
      const birthDate = new Date(employeeData.ngaySinh)
      if (birthDate > today) {
        errors.push('Ngày sinh không thể là ngày trong tương lai')
      }
    }

    if (!employeeData.soDienThoai || employeeData.soDienThoai.trim() === '') {
      errors.push('Số điện thoại là bắt buộc')
    } else if (!/^[0-9]{10,11}$/.test(employeeData.soDienThoai)) {
      errors.push('Số điện thoại không hợp lệ')
    }

    if (!employeeData.cccd || employeeData.cccd.trim() === '') {
      errors.push('CCCD là bắt buộc')
    } else if (!/^[0-9]{12}$/.test(employeeData.cccd)) {
      errors.push('CCCD phải có 12 chữ số')
    }

    if (!isUpdate) {
      if (!employeeData.tenDangNhap || employeeData.tenDangNhap.trim() === '') {
        errors.push('Tên đăng nhập là bắt buộc')
      }

      if (!employeeData.email || employeeData.email.trim() === '') {
        errors.push('Email là bắt buộc')
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(employeeData.email)) {
        errors.push('Email không hợp lệ')
      }

      // Removed password validation - password will be set/updated separately
      // No longer require password input during employee creation
    }

    return errors
  },

  // Format date for display
  formatDate: (dateString) => {
    if (!dateString) return ''
    const date = new Date(dateString)
    return date.toLocaleDateString('vi-VN')
  },

  // Format gender for display
  formatGender: (gioiTinh) => {
    return gioiTinh === true ? 'Nam' : 'Nữ'
  },

  // Format status for display
  formatStatus: (trangThai) => {
    return trangThai ? 'Hoạt động' : 'Ngừng hoạt động'
  }
}

export default nhanVienService

