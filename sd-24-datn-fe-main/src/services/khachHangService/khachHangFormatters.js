// Formatting utilities for KhachHang data
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

export const formatPhone = (soDienThoai) => {
  if (!soDienThoai) return 'N/A'
  // Format phone number: 0123456789 -> 0123 456 789
  return soDienThoai.replace(/(\d{4})(\d{3})(\d{3})/, '$1 $2 $3')
}

export const formatAddress = (diaChi) => {
  if (!diaChi || diaChi.trim() === '') return 'Chưa cập nhật'
  return diaChi
}
