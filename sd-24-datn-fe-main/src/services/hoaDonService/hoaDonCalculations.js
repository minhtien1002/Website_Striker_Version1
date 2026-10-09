/**
 * Service for invoice calculations and statistics
 */

// Calculate total revenue from completed invoices
export const calculateTotalRevenue = (allInvoices) => {
  return allInvoices
    .filter((invoice) => invoice.trangThai?.id === 5)
    .reduce((sum, invoice) => sum + (invoice.tongTienThanhToan || 0), 0);
};

// Calculate pagination data
export const calculatePagination = (filteredInvoices, currentPage, pageSize) => {
  const totalPages = Math.ceil(filteredInvoices.length / pageSize) || 1;
  const start = currentPage * pageSize;
  const end = start + pageSize;
  const paginatedInvoices = filteredInvoices.slice(start, end);
  
  return {
    totalPages,
    paginatedInvoices,
    start,
    end
  };
};

// Calculate displayed pages for pagination
export const calculateDisplayedPages = (currentPage, totalPages) => {
  const maxPagesToShow = 5;
  const current = currentPage + 1;
  let startPage = Math.max(1, current - Math.floor(maxPagesToShow / 2));
  let endPage = Math.min(totalPages, startPage + maxPagesToShow - 1);
  if (endPage - startPage + 1 < maxPagesToShow) {
    startPage = Math.max(1, endPage - maxPagesToShow + 1);
  }
  return Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);
};

// Change page with validation
export const changePage = (page, totalPages, toast) => {
  if (page >= 0 && page < totalPages) {
    // Page changed silently
    return page;
  }
  return null;
};
