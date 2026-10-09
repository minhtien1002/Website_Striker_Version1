import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { useToast } from 'vue-toastification';
import { useRoute, useRouter } from 'vue-router';
import mitt from 'mitt';

import {
  fetchAllInvoices,
  updateInvoice,
  deleteInvoice,
  filterInvoices,
  applyFilters,
  resetFilters as resetFiltersService,
  getTabCount,
  switchTab as switchTabService,
  formatDate,
  formatCurrency,
  getStatusText,
  getStatusColor,
  printInvoicePDF,
  downloadQRCode,
  exportToExcel,
  scanQRInvoice,
  calculateTotalRevenue,
  calculatePagination,
  calculateDisplayedPages,
  changePage as changePageService,
  prepareEditInvoice,
  validateInvoiceData,
  handleInvoiceHighlight,
  navigateToSalesCounter as navigateToSalesCounterService,
  getTabsConfig,
  getTableColumns,
  getBreadcrumbItems,
  getBreadcrumbActions,
  getDefaultFilters,
  getPageStats
} from './hoaDonGetAll.js';

import { getHoaDonDetail, mapHoaDonDetailToFrontend } from '../api/APIHoaDon/HoaDonChiTietAPI.js';
import { getStatusCounts, getPriceRange, getCompletedRevenue } from '../api/APIHoaDon/HoaDonAPI.js';
import { getAllStatusOptions } from './hoaDonCRUD.js';

/**
 * Composable function that encapsulates all HoaDon logic
 */
export function useHoaDonLogic() {
  const emitter = mitt();
  const toast = useToast();
  const route = useRoute();
  const router = useRouter();

  // Reactive state
  const errorMessage = ref('');
  const allInvoices = ref([]);
  const totalRevenue = ref(0);
  const currentPage = ref(0);
  const pageSize = ref(10);
  const filters = ref(getDefaultFilters());
  const tempFilters = ref(getDefaultFilters());
  const activeTab = ref('all');
  const showEditModal = ref(false);
  const showDeleteModal = ref(false);
  const editInvoiceData = ref({});
  const deleteInvoiceId = ref(null);
  const isUpdating = ref(false);
  const isDeleting = ref(false);
  const highlightedInvoice = ref(null);

  // Detail invoice state
  const invoiceDetail = ref(null);
  const isLoadingDetail = ref(false);
  const detailError = ref('');

  // Status counts state
  const statusCounts = ref({});
  const completedRevenue = ref(0);
  const isLoadingStatusCounts = ref(false);

  // Price range state
  const priceRange = ref({ minPrice: 0, maxPrice: 0 });
  const isLoadingPriceRange = ref(false);

  // Search debounce state
  const searchTimeout = ref(null);
  const isSearching = ref(false);

  // Status options for dropdown
  const statusOptions = ref(getAllStatusOptions());

  // Configuration
  const tabs = ref(getTabsConfig());
  const tableColumns = ref(getTableColumns());
  const breadcrumbItems = ref(getBreadcrumbItems());

  // Update total revenue using service function
  const updateTotalRevenue = () => {
    totalRevenue.value = calculateTotalRevenue(allInvoices.value);
  };

  // Navigation
  const navigateToSalesCounter = () => {
    navigateToSalesCounterService(router);
  };

  // Reactive state for API pagination
  const apiTotalPages = ref(1);
  const apiTotalElements = ref(0);

  // Computed properties - now using API data directly
  const filteredInvoices = computed(() => {
    // Since filtering is now done by API, return all invoices as they are already filtered
    return allInvoices.value;
  });

  // QR Scanner modal state
  const showQRScannerModal = ref(false);

  // Open QR scanner modal
  const openQRScannerModal = () => {
    showQRScannerModal.value = true;
  };

  // Close QR scanner modal
  const closeQRScannerModal = () => {
    showQRScannerModal.value = false;
  };

  // Handle invoice found from QR scan
  const handleInvoiceFound = (invoiceData) => {
    toast.success(`Tìm thấy hóa đơn: ${invoiceData.ma}`);
    // Optionally refresh the invoice list or highlight the found invoice
    loadInvoices();
  };

  // "Làm mới" phải tải lại cả số liệu thống kê, không chỉ danh sách
  const refreshAll = () => {
    loadInvoices();
    loadStatusCounts();
  };

  const breadcrumbActions = computed(() =>
    getBreadcrumbActions(refreshAll, exportToExcel, () => scanQRInvoice(openQRScannerModal), navigateToSalesCounter, filteredInvoices)
  );

  const pageStats = computed(() =>
    getPageStats(statusCounts.value, completedRevenue.value, formatCurrency)
  );

  const paginatedInvoices = computed(() => {
    // API already returns paginated data, so return as is
    return allInvoices.value;
  });

  const totalPages = computed(() => {
    // Use API total pages
    return apiTotalPages.value;
  });

  const displayedPages = computed(() => {
    return calculateDisplayedPages(currentPage.value, totalPages.value);
  });

  // Load invoices with API filters
  const loadInvoices = async () => {
    errorMessage.value = '';

    try {
      // Prepare filters for API call
      const apiFilters = {
        page: currentPage.value,
        size: pageSize.value,
        keyword: filters.value.search,
        minAmount: filters.value.minPrice ? parseFloat(filters.value.minPrice) : null,
        maxAmount: filters.value.maxPrice ? parseFloat(filters.value.maxPrice) : null,
        trangThai: filters.value.trangThaiId ? parseInt(filters.value.trangThaiId) : null,
        loaiDon: filters.value.loaiDon || null,
        startDate: filters.value.startDate || null,
        endDate: filters.value.endDate || null,
        sortBy: getSortField(filters.value.sortBy),
        sortDir: getSortDirection(filters.value.sortBy)
      };

      const { invoices, totalElements, totalPages: responseTotalPages, error } = await fetchAllInvoices(route, toast, apiFilters);

      if (error) {
        errorMessage.value = error;
        return;
      }

      allInvoices.value = invoices;
      apiTotalElements.value = totalElements || invoices.length;
      apiTotalPages.value = responseTotalPages || 1;
      updateTotalRevenue();

      if (route.query.maHoaDon) {
        highlightedInvoice.value = route.query.maHoaDon;
        handleInvoiceHighlight(highlightedInvoice.value);
      }
    } finally {
      if (currentPage.value >= totalPages.value) {
        currentPage.value = Math.max(0, totalPages.value - 1);
      }
    }
  };

  // Helper function to convert frontend sort to API sort field
  const getSortField = (sortBy) => {
    switch (sortBy) {
      case 'newest': return 'ngayTao';
      case 'most_expensive':
      case 'cheapest': return 'tongTienSauGiam'; // Fixed: use correct field name
      default: return 'id';
    }
  };

  // Helper function to convert frontend sort to API sort direction
  const getSortDirection = (sortBy) => {
    switch (sortBy) {
      case 'newest':
      case 'most_expensive': return 'DESC';
      case 'cheapest': return 'ASC';
      default: return 'DESC';
    }
  };

  // Edit invoice
  const editInvoice = (invoice) => {
    editInvoiceData.value = prepareEditInvoice(invoice);
    showEditModal.value = true;
  };

  const handleUpdateInvoice = async () => {
    if (!validateInvoiceData(editInvoiceData.value, toast)) {
      return;
    }

    isUpdating.value = true;
    try {
      const { success } = await updateInvoice(editInvoiceData.value, toast, emitter);
      if (success) {
        showEditModal.value = false;
        await loadInvoices();
        await loadStatusCounts(); // Refresh status counts after update
      }
    } finally {
      isUpdating.value = false;
    }
  };

  // Delete invoice
  const confirmDelete = (id) => {
    deleteInvoiceId.value = id;
    showDeleteModal.value = true;
  };

  const handleDeleteInvoice = async () => {
    isDeleting.value = true;
    try {
      const { success } = await deleteInvoice(deleteInvoiceId.value, toast);
      if (success) {
        showDeleteModal.value = false;
        deleteInvoiceId.value = null;
        await loadInvoices();
        await loadStatusCounts(); // Refresh status counts after delete
      }
    } finally {
      isDeleting.value = false;
    }
  };

  // Tab and filter functions - use global status counts from API
  const getTabCountLocal = (tabId) => {
    if (tabId === 'all') {
      // Sum all status counts for "Tất cả" tab
      if (!statusCounts.value || Object.keys(statusCounts.value).length === 0) {
        return 0;
      }
      return Object.values(statusCounts.value).reduce((sum, count) => sum + count, 0);
    }

    // Find tab configuration to get statusId
    const tab = tabs.value.find((t) => t.id === tabId);
    if (!tab?.statusId && tab?.statusId !== 0) return 0;

    // Get count from API status counts
    return getStatusCount(tab.statusId);
  };


  // Debounced search function
  const debouncedSearch = (callback, delay = 500) => {
    if (searchTimeout.value) {
      clearTimeout(searchTimeout.value);
    }

    isSearching.value = true;
    searchTimeout.value = setTimeout(async () => {
      isSearching.value = false;
      await callback();
    }, delay);
  };

  const filterInvoicesLocal = async (isSearchInput = false) => {
    applyFilters(tempFilters.value, filters.value, toast, getStatusText, formatDate);
    currentPage.value = 0;

    // If it's a search input, use debounce
    if (isSearchInput) {
      debouncedSearch(async () => {
        await loadInvoices();
      });
    } else {
      // For other filters, execute immediately
      await loadInvoices();
    }
  };

  const resetFiltersLocal = async () => {
    resetFiltersService(filters.value, tempFilters.value, toast);
    activeTab.value = 'all';
    currentPage.value = 0;
    await loadInvoices(); // Reload data after reset
  };

  const changePageLocal = async (page) => {
    const newPage = changePageService(page, totalPages.value, toast);
    if (newPage !== null) {
      currentPage.value = newPage;
      await loadInvoices(); // Reload data for new page
    }
  };

  const switchTabLocal = async (tabId) => {
    activeTab.value = tabId;
    switchTabService(tabId, tabs.value, filters.value, toast);
    currentPage.value = 0;
    await loadInvoices(); // Reload data for new tab
  };

  // Load invoice detail
  const loadInvoiceDetail = async (id) => {
    if (!id) {
      detailError.value = 'ID hóa đơn không hợp lệ';
      return null;
    }

    isLoadingDetail.value = true;
    detailError.value = '';

    try {
      const backendData = await getHoaDonDetail(id);
      const mappedData = mapHoaDonDetailToFrontend(backendData);
      invoiceDetail.value = mappedData;

      return mappedData;
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'Không thể tải chi tiết hóa đơn';
      detailError.value = errorMsg;
      toast.error(errorMsg);
      return null;
    } finally {
      isLoadingDetail.value = false;
    }
  };

  // Clear invoice detail
  const clearInvoiceDetail = () => {
    invoiceDetail.value = null;
    detailError.value = '';
  };

  // Load status counts
  const loadStatusCounts = async () => {
    isLoadingStatusCounts.value = true;
    try {
      const [counts, revenue] = await Promise.all([getStatusCounts(), getCompletedRevenue()]);
      statusCounts.value = counts;
      completedRevenue.value = revenue;
    } catch (error) {
      console.error('Error loading status counts:', error);
    } finally {
      isLoadingStatusCounts.value = false;
    }
  };

  // Load price range from database
  const loadPriceRange = async () => {
    isLoadingPriceRange.value = true;
    try {
      const range = await getPriceRange();
      priceRange.value = range;

      // Update filter placeholders with actual min/max values
      if (!tempFilters.value.minPrice) {
        tempFilters.value.minPrice = '';
      }
      if (!tempFilters.value.maxPrice) {
        tempFilters.value.maxPrice = '';
      }
    } catch (error) {
      console.error('Error loading price range:', error);
      // Set default values on error
      priceRange.value = { minPrice: 0, maxPrice: 10000000 };
    } finally {
      isLoadingPriceRange.value = false;
    }
  };

  // Get status count by status ID
  const getStatusCount = (statusId) => {
    if (!statusCounts.value) return 0;

    // Convert statusId to string key as backend returns string keys
    const key = statusId.toString();
    return statusCounts.value[key] || 0;
  };

  // Event listeners and lifecycle
  emitter.on('invoice-completed', (invoice) => {
    console.log('Nhận được sự kiện invoice-completed:', invoice);
    updateTotalRevenue();
    // Revenue updated silently
  });

  onMounted(() => {
    loadInvoices();
    loadStatusCounts();
    loadPriceRange();
  });

  watch(allInvoices, () => {
    updateTotalRevenue();
  });

  // Return all reactive state and functions
  return {
    // State
    errorMessage,
    allInvoices,
    totalRevenue,
    currentPage,
    pageSize,
    filters,
    tempFilters,
    activeTab,
    showEditModal,
    showDeleteModal,
    editInvoiceData,
    deleteInvoiceId,
    isUpdating,
    isDeleting,
    highlightedInvoice,
    apiTotalPages,
    apiTotalElements,
    invoiceDetail,
    isLoadingDetail,
    detailError,
    statusCounts,
    isLoadingStatusCounts,
    priceRange,
    isLoadingPriceRange,
    searchTimeout,
    isSearching,
    statusOptions,
    showQRScannerModal,

    // Configuration
    tabs,
    tableColumns,
    breadcrumbItems,
    breadcrumbActions,

    // Computed
    filteredInvoices,
    pageStats,
    paginatedInvoices,
    totalPages,
    displayedPages,

    // Functions
    loadInvoices,
    editInvoice,
    handleUpdateInvoice,
    confirmDelete,
    handleDeleteInvoice,
    getTabCountLocal,
    switchTabLocal,
    filterInvoicesLocal,
    resetFiltersLocal,
    changePageLocal,
    navigateToSalesCounter,
    loadInvoiceDetail,
    clearInvoiceDetail,
    loadStatusCounts,
    loadPriceRange,
    getStatusCount,
    openQRScannerModal,
    closeQRScannerModal,
    handleInvoiceFound,

    // Service functions (re-exported for template use)
    formatDate,
    formatCurrency,
    getStatusText,
    getStatusColor,
    printInvoicePDF,
    downloadQRCode,
    exportToExcel,
    scanQRInvoice
  };
}
