/**
 * Configuration for HoaDonChiTiet functionality
 */

// Order status definitions (updated to match backend mapping)
export const orderStatuses = {
  direct: [
    { id: 5, name: 'Đã hoàn thành', color: 'text-green-600 bg-green-100', icon: 'solar:check-circle-bold' }
  ],
  online: [
    { id: 0, name: 'Hóa đơn chờ', color: 'text-gray-600 bg-gray-100', icon: 'solar:clock-circle-bold' },
    { id: 1, name: 'Chờ xác nhận', color: 'text-orange-600 bg-orange-100', icon: 'solar:clock-circle-bold' },
    { id: 2, name: 'Đã xác nhận', color: 'text-yellow-600 bg-yellow-100', icon: 'solar:check-square-bold' },
    { id: 4, name: 'Đang vận chuyển', color: 'text-purple-600 bg-purple-100', icon: 'solar:delivery-bold' },
    { id: 5, name: 'Đã hoàn thành', color: 'text-green-600 bg-green-100', icon: 'solar:check-circle-bold' },
    { id: 6, name: 'Đã hủy', color: 'text-red-600 bg-red-100', icon: 'solar:close-circle-bold' },
    { id: 7, name: 'Yêu cầu hoàn hàng', color: 'text-amber-600 bg-amber-100', icon: 'solar:undo-left-round-bold' },
    { id: 8, name: 'Đã hoàn hàng', color: 'text-slate-600 bg-slate-100', icon: 'solar:box-minimalistic-bold' }
  ]
};

// Breadcrumb configuration
export const breadcrumbItems = [
  { label: 'Quản lý bán hàng', path: '/hoa-don' },
  { label: 'Hóa đơn', path: '/hoa-don' },
  { label: 'Chi tiết hóa đơn' }
];
