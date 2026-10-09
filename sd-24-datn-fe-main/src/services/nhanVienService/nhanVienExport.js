import * as XLSX from 'xlsx'
import { formatDate } from './nhanVienFormatters.js'

// Export functionality for employees
export const exportToExcel = (filteredEmployees, toast) => {
  if (filteredEmployees.length === 0) {
    toast.info('Không có dữ liệu để xuất.')
    return
  }

  const dataToExport = filteredEmployees.map((emp, index) => ({
    'STT': index + 1,
    'Mã NV': emp.maNhanVien,
    'Tên Nhân Viên': emp.tenNhanVien,
    'Ngày Sinh': formatDate(emp.ngaySinh),
    'Giới Tính': emp.gioiTinh ? 'Nam' : 'Nữ',
    'SĐT': emp.soDienThoai,
    'CCCD': emp.cccd,
    'Địa chỉ': `${emp.diaChiSoNhaTenDuong}, ${emp.diaChiPhuongXa}, ${emp.diaChiQuanHuyen}, ${emp.diaChiTinhThanh}`,
    'Ngày Tạo': formatDate(emp.ngayTao),
    'Trạng Thái': emp.trangThai ? 'Hoạt động' : 'Ngừng hoạt động'
  }))

  const worksheet = XLSX.utils.json_to_sheet(dataToExport)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, 'DanhSachNhanVien')
  XLSX.writeFile(workbook, `danh_sach_nhan_vien_${new Date().toISOString().split('T')[0]}.xlsx`)
  toast.success('Xuất file Excel thành công!')
}

export const exportEmployeeDetail = (employee, toast) => {
  if (!employee) {
    toast.error('Không có dữ liệu nhân viên để xuất.')
    return
  }

  const employeeDetail = {
    'Mã Nhân Viên': employee.maNhanVien,
    'Tên Nhân Viên': employee.tenNhanVien,
    'Ngày Sinh': formatDate(employee.ngaySinh),
    'Giới Tính': employee.gioiTinh ? 'Nam' : 'Nữ',
    'Số Điện Thoại': employee.soDienThoai,
    'CCCD': employee.cccd,
    'Số Nhà, Tên Đường': employee.diaChiSoNhaTenDuong,
    'Phường/Xã': employee.diaChiPhuongXa,
    'Quận/Huyện': employee.diaChiQuanHuyen,
    'Tỉnh/Thành Phố': employee.diaChiTinhThanh,
    'Ngày Tạo': formatDate(employee.ngayTao),
    'Ngày Cập Nhật': formatDate(employee.ngayCapNhat),
    'Trạng Thái': employee.trangThai ? 'Hoạt động' : 'Ngừng hoạt động'
  }

  const worksheet = XLSX.utils.json_to_sheet([employeeDetail])
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, 'ChiTietNhanVien')
  XLSX.writeFile(workbook, `chi_tiet_nhan_vien_${employee.maNhanVien}_${new Date().toISOString().split('T')[0]}.xlsx`)
  toast.success(`Xuất chi tiết nhân viên ${employee.tenNhanVien} thành công!`)
}
