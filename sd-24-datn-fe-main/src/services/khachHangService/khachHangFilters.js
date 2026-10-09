import { khachHangConfig } from './khachHangConfig.js'

// Build API parameters for backend filtering
export const buildAPIParams = (filters, activeTab, tabs, currentPage, pageSize) => {
  const params = {
    page: currentPage,
    size: pageSize
  }

  // Add search keyword
  if (filters.search && filters.search.trim()) {
    params.keyword = filters.search.trim()
  }

  // Add status filter based on active tab
  const activeTabObj = tabs.find(tab => tab.id === activeTab)
  if (activeTabObj?.status !== undefined) {
    params.trangThai = activeTabObj.status
  } else if (filters.trangThai !== '') {
    params.trangThai = filters.trangThai === 'true'
  }

  // Add gender filter
  if (filters.gioiTinh !== '') {
    params.gioiTinh = Number(filters.gioiTinh)
  }

  // Add sorting
  if (filters.sortBy) {
    switch (filters.sortBy) {
      case 'newest':
        params.sortBy = 'createdAt' // Fixed: use correct field name
        params.sortDir = 'desc'
        break
      case 'oldest':
        params.sortBy = 'createdAt' // Fixed: use correct field name
        params.sortDir = 'asc'
        break
      case 'name_asc':
        params.sortBy = 'ten' // Fixed: use correct field name
        params.sortDir = 'asc'
        break
      case 'name_desc':
        params.sortBy = 'ten' // Fixed: use correct field name
        params.sortDir = 'desc'
        break
      default:
        params.sortBy = 'id'
        params.sortDir = 'desc'
    }
  } else {
    params.sortBy = 'id'
    params.sortDir = 'desc'
  }

  return params
}

// Client-side filtering (removed - using API filtering only)
export const filterCustomers = (allCustomers, filters, activeTab, tabs) => {
  // This function is deprecated - all filtering is now done by the API
  return []
}

export const paginateCustomers = (filteredCustomers, currentPage, pageSize) => {
  const start = currentPage * pageSize
  const end = start + pageSize
  return filteredCustomers.slice(start, end)
}

export const calculateTotalPages = (filteredCustomers, pageSize) => {
  return Math.ceil(filteredCustomers.length / pageSize) || 1
}

export const getTabCount = (allCustomers, tabId, tabs) => {
  if (!allCustomers.length) return 0
  if (tabId === 'all') return allCustomers.length
  const tab = tabs.find(t => t.id === tabId)
  if (tab?.status === undefined) return 0
  return allCustomers.filter(customer => customer.trangThai === tab.status).length
}

export const switchTab = (tabId, tabs, filters, toast, refreshCallback) => {
  const tab = tabs.find(t => t.id === tabId)
  filters.trangThai = tab.status !== undefined ? String(tab.status) : ''
  
  // Trigger data refresh with new tab filter
  if (refreshCallback) {
    refreshCallback()
  }
  
  if (tabId === 'all') {
    toast.info('Đang hiển thị tất cả khách hàng.', { timeout: 2000 })
  } else {
    toast.info(`Đang hiển thị khách hàng trạng thái: ${tab.label}.`, { timeout: 2000 })
  }
}

export const applyFilters = (tempFilters, filters, toast, refreshCallback) => {
  filters.search = tempFilters.search
  filters.trangThai = tempFilters.trangThai
  filters.gioiTinh = tempFilters.gioiTinh
  filters.sortBy = tempFilters.sortBy
  
  // Trigger data refresh with new filters
  if (refreshCallback) {
    refreshCallback()
  }
}

export const resetFilters = (filters, tempFilters, toast, refreshCallback) => {
  const defaultFilters = khachHangConfig.defaultFilters
  Object.assign(filters, { ...defaultFilters })
  Object.assign(tempFilters, { ...defaultFilters })
  
  // Trigger data refresh with reset filters
  if (refreshCallback) {
    refreshCallback()
  }
  
  toast.info('Đã đặt lại tất cả bộ lọc!', { timeout: 2000 })
}

export const changePage = (page, totalPages) => {
  if (page >= 0 && page < totalPages) {
    return page
  }
  return null
}
