/**
 * Service for filtering and sorting invoice data
 */

// Filter invoices based on various criteria
export const filterInvoices = (allInvoices, filters, activeTab, tabs) => {
  let result = allInvoices;

  const activeTabObj = tabs.find((tab) => tab.id === activeTab);
  if (activeTabObj?.statusId) {
    result = result.filter((invoice) => invoice.trangThai?.id === activeTabObj.statusId);
  } else if (filters.trangThaiId) {
    result = result.filter((invoice) => invoice.trangThai?.id === parseInt(filters.trangThaiId));
  }

  if (filters.search) {
    result = result.filter((invoice) =>
      invoice.maHoaDon?.toLowerCase().includes(filters.search.toLowerCase()) ||
      invoice.khachHang?.tenKhachHang?.toLowerCase().includes(filters.search.toLowerCase()) ||
      invoice.khachHang?.soDienThoai?.includes(filters.search)
    );
  }

  if (filters.loaiDon) {
    result = result.filter((invoice) => invoice.loaiDon === filters.loaiDon);
  }

  // 'yyyy-mm-dd' must be parsed as a LOCAL date (new Date(str) would give UTC midnight)
  if (filters.startDate) {
    const [y, m, d] = filters.startDate.split('-').map(Number);
    const start = new Date(y, m - 1, d, 0, 0, 0, 0);
    result = result.filter((invoice) => new Date(invoice.ngayTao) >= start);
  }
  if (filters.endDate) {
    const [y, m, d] = filters.endDate.split('-').map(Number);
    const end = new Date(y, m - 1, d, 23, 59, 59, 999);
    result = result.filter((invoice) => new Date(invoice.ngayTao) <= end);
  }

  if (filters.minPrice) {
    result = result.filter((invoice) => invoice.tongTienSauGiam >= parseFloat(filters.minPrice));
  }
  if (filters.maxPrice) {
    result = result.filter((invoice) => invoice.tongTienSauGiam <= parseFloat(filters.maxPrice));
  }

  if (filters.sortBy) {
    result = [...result];
    if (filters.sortBy === 'newest') {
      result.sort((a, b) => new Date(b.ngayTao) - new Date(a.ngayTao));
    } else if (filters.sortBy === 'most_expensive') {
      result.sort((a, b) => b.tongTienSauGiam - a.tongTienSauGiam);
    } else if (filters.sortBy === 'cheapest') {
      result.sort((a, b) => a.tongTienSauGiam - b.tongTienSauGiam);
    }
  }

  return result;
};

// Apply filters and show toast messages
export const applyFilters = (tempFilters, filters, toast, getStatusText, formatDate) => {
  filters.search = tempFilters.search;
  filters.trangThaiId = tempFilters.trangThaiId;
  filters.startDate = tempFilters.startDate;
  filters.endDate = tempFilters.endDate;
  filters.minPrice = tempFilters.minPrice;
  filters.maxPrice = tempFilters.maxPrice;
  filters.loaiDon = tempFilters.loaiDon;
  filters.sortBy = tempFilters.sortBy;

  let message = 'Đã áp dụng bộ lọc: ';
  let filterApplied = false;

  if (filters.search) {
    message += `Mã hóa đơn, tên khách hàng hoặc SĐT: "${filters.search}". `;
    filterApplied = true;
  }
  if (filters.trangThaiId) {
    const statusText = getStatusText(parseInt(filters.trangThaiId));
    message += `Trạng thái: "${statusText}". `;
    filterApplied = true;
  }
  if (filters.startDate) {
    message += `Từ ngày: "${formatDate(filters.startDate)}". `;
    filterApplied = true;
  }
  if (filters.endDate) {
    message += `Đến ngày: "${formatDate(filters.endDate)}". `;
    filterApplied = true;
  }
  if (filters.loaiDon) {
    message += `Loại đơn: "${filters.loaiDon}". `;
    filterApplied = true;
  }
  if (filters.sortBy) {
    const sortByText = {
      newest: 'Mới nhất',
      most_expensive: 'Đắt nhất',
      cheapest: 'Rẻ nhất',
    }[filters.sortBy];
    message += `Sắp xếp theo: "${sortByText}". `;
    filterApplied = true;
  }

  // Filters applied silently
};

// Reset all filters
export const resetFilters = (filters, tempFilters, toast) => {
  const defaultFilters = { 
    search: '', 
    trangThaiId: '', 
    loaiDon: '', 
    startDate: '', 
    endDate: '', 
    minPrice: '', 
    maxPrice: '', 
    sortBy: '' 
  };
  
  Object.assign(filters, defaultFilters);
  Object.assign(tempFilters, defaultFilters);
  
  // Filters reset silently
};

// Get count for each tab
export const getTabCount = (tabId, allInvoices, tabs) => {
  if (!allInvoices.length) return 0;
  if (tabId === 'all') return allInvoices.length;
  const tab = tabs.find((t) => t.id === tabId);
  if (!tab?.statusId) return 0;
  return allInvoices.filter((invoice) => invoice.trangThai?.id === tab.statusId).length;
};

// Switch active tab
export const switchTab = (tabId, tabs, filters, toast) => {
  const tab = tabs.find((t) => t.id === tabId);
  filters.trangThaiId = tab.statusId ? String(tab.statusId) : '';
  
  // Tab switched silently
};
