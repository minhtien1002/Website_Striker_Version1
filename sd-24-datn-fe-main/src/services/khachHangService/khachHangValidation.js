import { khachHangConfig } from './khachHangConfig.js'

// Validation functions for customer data
export const validateCustomer = (customer, toast) => {
  const requiredFields = [
    { field: customer.tenKhachHang, label: 'Tên khách hàng' },
    { field: customer.ngaySinh, label: 'Ngày sinh' },
    { field: customer.soDienThoai, label: 'Số điện thoại' }
  ]

  for (const item of requiredFields) {
    if (!item.field || String(item.field).trim() === '') {
      toast.error(`Vui lòng nhập ${item.label}.`)
      return false
    }
  }

  if (!khachHangConfig.validation.nameRegex.test(customer.tenKhachHang.trim())) {
    toast.error('Tên khách hàng không được chứa số.')
    return false
  }

  if (!khachHangConfig.validation.phoneRegex.test(customer.soDienThoai)) {
    toast.error('Số điện thoại phải gồm đúng 10 chữ số.')
    return false
  }

  // Validate CCCD if provided
  if (customer.cccd && customer.cccd.trim() !== '') {
    if (!khachHangConfig.validation.cccdRegex.test(customer.cccd)) {
      toast.error('CCCD phải gồm đúng 12 chữ số.')
      return false
    }
  }

  // Validate email if provided
  if (customer.email && customer.email.trim() !== '') {
    if (!khachHangConfig.validation.emailRegex.test(customer.email)) {
      toast.error('Email không đúng định dạng.')
      return false
    }
  }

  // Validate age (must be 16+)
  if (customer.ngaySinh) {
    const birthDate = new Date(customer.ngaySinh)
    const today = new Date()
    let age = today.getFullYear() - birthDate.getFullYear()
    const monthDiff = today.getMonth() - birthDate.getMonth()
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--
    }
    
    if (age < 16) {
      toast.error('Khách hàng phải từ 16 tuổi trở lên.')
      return false
    }
  }

  return true
}

export const validateCustomerForm = (customer) => {
  const errors = []

  if (!customer.tenKhachHang || customer.tenKhachHang.trim() === '') {
    errors.push('Tên khách hàng là bắt buộc')
  } else if (!khachHangConfig.validation.nameRegex.test(customer.tenKhachHang.trim())) {
    errors.push('Tên khách hàng không được chứa số')
  }

  if (!customer.ngaySinh) {
    errors.push('Ngày sinh là bắt buộc')
  } else {
    // Validate age (must be 16+)
    const birthDate = new Date(customer.ngaySinh)
    const today = new Date()
    let age = today.getFullYear() - birthDate.getFullYear()
    const monthDiff = today.getMonth() - birthDate.getMonth()
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--
    }
    
    if (age < 16) {
      errors.push('Khách hàng phải từ 16 tuổi trở lên')
    }
  }

  if (!customer.soDienThoai || customer.soDienThoai.trim() === '') {
    errors.push('Số điện thoại là bắt buộc')
  } else if (!khachHangConfig.validation.phoneRegex.test(customer.soDienThoai)) {
    errors.push('Số điện thoại phải gồm đúng 10 chữ số')
  }

  // Optional CCCD validation
  if (customer.cccd && customer.cccd.trim() !== '') {
    if (!khachHangConfig.validation.cccdRegex.test(customer.cccd)) {
      errors.push('CCCD phải gồm đúng 12 chữ số')
    }
  }

  // Optional email validation
  if (customer.email && customer.email.trim() !== '') {
    if (!khachHangConfig.validation.emailRegex.test(customer.email)) {
      errors.push('Email không đúng định dạng')
    }
  }

  return errors
}
