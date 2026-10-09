// Configuration for KhachHang management
export const khachHangConfig = {
  // Tabs configuration
  tabs: [
    { id: 'all', label: 'Tất cả' },
    { id: 'active', label: 'Hoạt động', status: true },
    { id: 'inactive', label: 'Ngừng hoạt động', status: false }
  ],

  // DataTable columns configuration
  tableColumns: [
    { key: 'stt', label: 'STT', class: 'text-center' },
    { key: 'maKhachHang', label: 'Mã KH', class: 'font-weight-bold' },
    { key: 'tenKhachHang', label: 'Tên Khách Hàng' },
    { key: 'soDienThoai', label: 'SĐT' },
    { key: 'gioiTinh', label: 'Giới Tính', class: 'text-center' },
    { key: 'ngaySinh', label: 'Ngày Sinh', class: 'text-center' },
    { key: 'ngayTao', label: 'Ngày Tạo', class: 'text-center' },
    { key: 'ngayCapNhat', label: 'Ngày Cập Nhật', class: 'text-center' },
    { key: 'trangThai', label: 'Trạng Thái', class: 'text-center' },
    { key: 'actions', label: 'Hành Động', class: 'text-center' }
  ],

  // Default customer form structure
  defaultCustomer: {
    tenKhachHang: '',
    soDienThoai: '',
    gioiTinh: true,
    ngaySinh: '',
    cccd: '',
    email: '',
    diaChi: '',
    ngayTao: '',
    ngayCapNhat: '',
    trangThai: true
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
    cccdRegex: /^\d{12}$/,
    emailRegex: /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  }
}

// Breadcrumb configuration
export const getBreadcrumbItems = () => [
  { label: 'Quản lý khách hàng', path: '/khach-hang' },
  { label: 'Khách hàng' }
]

// Breadcrumb actions configuration
export const getBreadcrumbActions = (callbacks) => [
  {
    label: 'Làm mới',
    type: 'default',
    handler: callbacks.fetchAllCustomers
  },
  {
    label: 'Xuất Excel',
    type: 'primary',
    handler: callbacks.exportToExcel
  },
  {
    label: 'Thêm khách hàng',
    type: 'primary',
    handler: callbacks.navigateToAddCustomer
  }
]
