// Configuration for NhanVien management
export const nhanVienConfig = {
  // Tabs configuration
  tabs: [
    { id: 'all', label: 'Tất cả' },
    { id: 'active', label: 'Hoạt động', status: true },
    { id: 'inactive', label: 'Ngừng hoạt động', status: false }
  ],

  // DataTable columns configuration
  tableColumns: [
    { key: 'stt', label: 'STT', class: 'text-center' },
    { key: 'maNhanVien', label: 'Mã NV', class: 'font-weight-bold' },
    { key: 'tenNhanVien', label: 'Tên Nhân Viên' },
    { key: 'ngaySinh', label: 'Ngày Sinh', class: 'text-center' },
    { key: 'gioiTinh', label: 'Giới Tính', class: 'text-center' },
    { key: 'soDienThoai', label: 'SĐT' },
    { key: 'cccd', label: 'CCCD' },
    { key: 'diaChi', label: 'Địa chỉ' },
    { key: 'trangThai', label: 'Trạng Thái', class: 'text-center' },
    { key: 'actions', label: 'Hành Động', class: 'text-center' }
  ],

  // Default employee form structure
  defaultEmployee: {
    chucVuID: null,
    taiKhoanID: null,
    tenNhanVien: '',
    ngaySinh: '',
    gioiTinh: true,
    soDienThoai: '',
    cccd: '',
    diaChiSoNhaTenDuong: '',
    diaChiPhuongXa: '',
    diaChiQuanHuyen: '',
    diaChiTinhThanh: '',
    ngayTao: '',
    ngayCapNhat: '',
    trangThai: false  
  },

  // Default filters
  defaultFilters: {
    search: '',
    trangThai: '',
    gioiTinh: '',
    sortBy: ''
  },

  // Pagination settings
  pagination: {
    defaultPageSize: 10,
    pageSizeOptions: [5, 10, 20, 50]
  },

  // Validation rules
  validation: {
    nameRegex: /^[^\d]+$/,
    phoneRegex: /^\d{10}$/,
    cccdRegex: /^\d{12}$/
  }
}

// Breadcrumb configuration
export const getBreadcrumbItems = () => [
  { label: 'Quản lý nhân sự', path: '/nhan-vien' },
  { label: 'Nhân viên' }
]

// Breadcrumb actions configuration
export const getBreadcrumbActions = (callbacks) => [
  {
    label: 'Làm mới',
    type: 'default',
    handler: callbacks.fetchAllEmployees
  },
  {
    label: 'Xuất Excel',
    type: 'primary',
    handler: callbacks.exportToExcel
  },
  {
    label: 'Thêm nhân viên',
    type: 'primary',
    handler: callbacks.navigateToAddEmployee
  }
]
