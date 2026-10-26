/**
 * Validate thông tin khách hàng - cùng quy tắc với backend (ThongTinKhachHangValidator).
 * Mỗi hàm trả về câu báo lỗi, hoặc '' nếu hợp lệ.
 */

const HO_TEN = /^[\p{L} ]{2,50}$/u
const SO_DIEN_THOAI = /^0\d{9}$/
const EMAIL = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/

export const validateHoTen = (value) => {
  const v = (value || '').trim()
  if (!v) return 'Họ và tên không được để trống'
  if (!HO_TEN.test(v)) return 'Họ và tên chỉ gồm chữ cái và khoảng trắng, từ 2 đến 50 ký tự'
  return ''
}

export const validateSoDienThoai = (value) => {
  const v = (value || '').trim()
  if (!v) return 'Số điện thoại không được để trống'
  if (!SO_DIEN_THOAI.test(v)) return 'Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0'
  return ''
}

export const validateEmail = (value) => {
  const v = (value || '').trim()
  if (!v) return 'Email không được để trống'
  if (v.length > 100 || !EMAIL.test(v)) return 'Email không đúng định dạng (ví dụ: tenban@gmail.com)'
  return ''
}

export const validateDiaChi = (value) => {
  const v = (value || '').trim()
  if (!v) return 'Địa chỉ không được để trống'
  if (v.length < 5 || v.length > 255) return 'Địa chỉ phải từ 5 đến 255 ký tự'
  return ''
}

/** Kiểm tra không rỗng cho các ô chọn tỉnh/quận/phường... */
export const validateBatBuoc = (value, tenTruong) => ((value || '').toString().trim() ? '' : `${tenTruong} không được để trống`)
