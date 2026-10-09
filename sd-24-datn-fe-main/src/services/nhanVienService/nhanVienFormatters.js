// Formatting utilities for NhanVien data
export const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return 'Invalid Date'
    return date.toLocaleDateString('vi-VN')
  } catch (e) {
    return 'Invalid Date'
  }
}

export const formatDateTime = (dateString) => {
  if (!dateString) return 'N/A'
  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return 'Invalid Date'
    return date.toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    return 'Invalid Date'
  }
}

export const formatGender = (gioiTinh) => {
  return gioiTinh ? 'Nam' : 'Nữ'
}

export const formatStatus = (trangThai) => {
  return trangThai ? 'Hoạt động' : 'Ngừng hoạt động'
}

export const formatAddress = (employee) => {
  if (!employee) return ''
  const parts = [
    employee.diaChiSoNhaTenDuong,
    employee.diaChiPhuongXa,
    employee.diaChiQuanHuyen,
    employee.diaChiTinhThanh
  ].filter(part => part && part.trim())
  
  return parts.join(', ')
}
