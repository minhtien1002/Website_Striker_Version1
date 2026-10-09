import { nhanVienConfig } from './nhanVienConfig.js'

// Validation functions for employee data
export const validateEmployee = (employee, toast) => {
  const requiredFields = [
    { field: employee.tenNhanVien, label: 'Tên nhân viên' },
    { field: employee.ngaySinh, label: 'Ngày sinh' },
    { field: employee.soDienThoai, label: 'Số điện thoại' },
    { field: employee.cccd, label: 'CCCD' },
    { field: employee.diaChiSoNhaTenDuong, label: 'Số nhà, tên đường' },
    { field: employee.diaChiPhuongXa, label: 'Phường/Xã' },
    { field: employee.diaChiQuanHuyen, label: 'Quận/Huyện' },
    { field: employee.diaChiTinhThanh, label: 'Tỉnh/Thành phố' }
  ]

  for (const item of requiredFields) {
    if (!item.field || String(item.field).trim() === '') {
      toast.error(`Vui lòng nhập ${item.label}.`)
      return false
    }
  }

  if (!nhanVienConfig.validation.nameRegex.test(employee.tenNhanVien.trim())) {
    toast.error('Tên nhân viên không được chứa số.')
    return false
  }

  if (!nhanVienConfig.validation.phoneRegex.test(employee.soDienThoai)) {
    toast.error('Số điện thoại phải gồm đúng 10 chữ số.')
    return false
  }

  if (!nhanVienConfig.validation.cccdRegex.test(employee.cccd)) {
    toast.error('CCCD phải gồm đúng 12 chữ số.')
    return false
  }

  return true
}

export const validateEmployeeForm = (employee) => {
  const errors = []

  if (!employee.tenNhanVien || employee.tenNhanVien.trim() === '') {
    errors.push('Tên nhân viên là bắt buộc')
  } else if (!nhanVienConfig.validation.nameRegex.test(employee.tenNhanVien.trim())) {
    errors.push('Tên nhân viên không được chứa số')
  }

  if (!employee.ngaySinh) {
    errors.push('Ngày sinh là bắt buộc')
  }

  if (!employee.soDienThoai || employee.soDienThoai.trim() === '') {
    errors.push('Số điện thoại là bắt buộc')
  } else if (!nhanVienConfig.validation.phoneRegex.test(employee.soDienThoai)) {
    errors.push('Số điện thoại phải gồm đúng 10 chữ số')
  }

  if (!employee.cccd || employee.cccd.trim() === '') {
    errors.push('CCCD là bắt buộc')
  } else if (!nhanVienConfig.validation.cccdRegex.test(employee.cccd)) {
    errors.push('CCCD phải gồm đúng 12 chữ số')
  }

  if (!employee.diaChiSoNhaTenDuong || employee.diaChiSoNhaTenDuong.trim() === '') {
    errors.push('Số nhà, tên đường là bắt buộc')
  }

  if (!employee.diaChiPhuongXa || employee.diaChiPhuongXa.trim() === '') {
    errors.push('Phường/Xã là bắt buộc')
  }

  if (!employee.diaChiQuanHuyen || employee.diaChiQuanHuyen.trim() === '') {
    errors.push('Quận/Huyện là bắt buộc')
  }

  if (!employee.diaChiTinhThanh || employee.diaChiTinhThanh.trim() === '') {
    errors.push('Tỉnh/Thành phố là bắt buộc')
  }

  return errors
}
