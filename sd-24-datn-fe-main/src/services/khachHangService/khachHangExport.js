import * as XLSX from 'xlsx'
import { formatDate } from './khachHangFormatters.js'

// Export functionality for customers
export const exportToExcel = (filteredCustomers, toast) => {
  if (filteredCustomers.length === 0) {
    toast.info('Không có dữ liệu để xuất.')
    return
  }

  const dataToExport = filteredCustomers.map((customer, index) => ({
    'STT': index + 1,
    'Mã KH': customer.maKhachHang,
    'Tên Khách Hàng': customer.tenKhachHang,
    'Ngày Sinh': formatDate(customer.ngaySinh),
    'Giới Tính': customer.gioiTinh ? 'Nam' : 'Nữ',
    'SĐT': customer.soDienThoai,
    'Email': customer.email || '',
    'CCCD': customer.cccd || '',
    'Địa chỉ': `${customer.diaChiSoNhaTenDuong || ''}, ${customer.diaChiPhuongXa || ''}, ${customer.diaChiQuanHuyen || ''}, ${customer.diaChiTinhThanh || ''}`.replace(/^,\s*|,\s*$/g, '').replace(/,\s*,/g, ','),
    'Ngày Tạo': formatDate(customer.ngayTao),
    'Trạng Thái': customer.trangThai ? 'Hoạt động' : 'Ngừng hoạt động'
  }))

  const worksheet = XLSX.utils.json_to_sheet(dataToExport)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, 'DanhSachKhachHang')
  XLSX.writeFile(workbook, `danh_sach_khach_hang_${new Date().toISOString().split('T')[0]}.xlsx`)
  toast.success('Xuất file Excel thành công!')
}

export const exportCustomerDetail = (customer, toast) => {
  if (!customer) {
    toast.error('Không có dữ liệu khách hàng để xuất.')
    return
  }

  const customerDetail = {
    'Mã Khách Hàng': customer.maKhachHang,
    'Tên Khách Hàng': customer.tenKhachHang,
    'Ngày Sinh': formatDate(customer.ngaySinh),
    'Giới Tính': customer.gioiTinh ? 'Nam' : 'Nữ',
    'Số Điện Thoại': customer.soDienThoai,
    'Email': customer.email || '',
    'CCCD': customer.cccd || '',
    'Số Nhà, Tên Đường': customer.diaChiSoNhaTenDuong || '',
    'Phường/Xã': customer.diaChiPhuongXa || '',
    'Quận/Huyện': customer.diaChiQuanHuyen || '',
    'Tỉnh/Thành Phố': customer.diaChiTinhThanh || '',
    'Ngày Tạo': formatDate(customer.ngayTao),
    'Ngày Cập Nhật': formatDate(customer.ngayCapNhat),
    'Trạng Thái': customer.trangThai ? 'Hoạt động' : 'Ngừng hoạt động'
  }

  const worksheet = XLSX.utils.json_to_sheet([customerDetail])
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, 'ChiTietKhachHang')
  XLSX.writeFile(workbook, `chi_tiet_khach_hang_${customer.maKhachHang}_${new Date().toISOString().split('T')[0]}.xlsx`)
  toast.success(`Xuất chi tiết khách hàng ${customer.tenKhachHang} thành công!`)
}
