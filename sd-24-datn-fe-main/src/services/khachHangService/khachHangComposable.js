import { ref, computed, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import { useRouter } from 'vue-router'

// Import all service modules
import { khachHangConfig, getBreadcrumbItems, getBreadcrumbActions } from './khachHangConfig.js'
import { formatDate, formatDateTime, formatGender, formatStatus, formatPhone, formatAddress } from './khachHangFormatters.js'
import { validateCustomer, validateCustomerForm } from './khachHangValidation.js'
import { 
  buildAPIParams,
  filterCustomers, 
  paginateCustomers, 
  calculateTotalPages, 
  getTabCount, 
  switchTab, 
  applyFilters, 
  resetFilters, 
  changePage 
} from './khachHangFilters.js'
import { 
  fetchAllCustomers, 
  fetchCustomerStatusTotals,
  createCustomer, 
  updateCustomer, 
  toggleCustomerStatus, 
  resetCustomerForm,
  getCustomerById 
} from './khachHangCRUD.js'
import { useKhachHangUI } from './khachHangUI.js'
import { useConfirm } from '@/composables/useConfirm.js'

/**
 * Main composable function that encapsulates all KhachHang logic
 */
export function useKhachHangLogic() {
  const toast = useToast()
  const router = useRouter()
  const { confirm } = useConfirm()

  // UI composable
  const ui = useKhachHangUI()

  // Core data
  const allCustomers = ref([])
  const apiResponse = ref(null)
  const currentPage = ref(0)
  const pageSize = ref(khachHangConfig.pagination.defaultPageSize)
  const activeTab = ref('all')
  const isLoadingData = ref(false)
  const statusTotals = ref({ total: 0, active: 0, inactive: 0 })

  // Form data
  const newCustomer = ref(resetCustomerForm())
  
  // Filter states
  const filters = ref({ ...khachHangConfig.defaultFilters })
  const tempFilters = ref({ ...khachHangConfig.defaultFilters })

  // Configuration
  const tabs = ref(khachHangConfig.tabs)
  const tableColumns = ref(khachHangConfig.tableColumns)

  // Computed properties
  const filteredCustomers = computed(() => {
    // If using API data, return content directly (already filtered by backend)
    if (apiResponse.value) {
      return apiResponse.value.content || []
    }
    // No fallback - return empty array if no API response
    return []
  })

  const totalPages = computed(() => {
    // If using API data, get total pages from API response
    if (apiResponse.value) {
      return apiResponse.value.totalPages || 1
    }
    // Fallback to client-side calculation
    return calculateTotalPages(filteredCustomers.value, pageSize.value)
  })

  const paginatedCustomers = computed(() => {
    // If using API data, return content directly (already paginated by backend)
    if (apiResponse.value) {
      return apiResponse.value.content || []
    }
    // Fallback to client-side pagination
    return paginateCustomers(filteredCustomers.value, currentPage.value, pageSize.value)
  })

  const pageStats = computed(() => {
    return [
      {
        icon: 'solar:users-group-two-rounded-bold',
        value: statusTotals.value.total,
        label: 'Tổng khách hàng'
      },
      {
        icon: 'solar:check-circle-bold',
        value: statusTotals.value.active,
        label: 'Hoạt động'
      },
      {
        icon: 'solar:close-circle-bold',
        value: statusTotals.value.inactive,
        label: 'Ngừng hoạt động'
      }
    ]
  })

  // Breadcrumb configuration
  const breadcrumbItems = ref(getBreadcrumbItems())
  const breadcrumbActions = computed(() => getBreadcrumbActions({
    fetchAllCustomers: handleFetchAllCustomers,
    exportToExcel: handleExportToExcel,
    navigateToAddCustomer: handleNavigateToAddCustomer
  }))

  // Core functions
  const handleFetchAllCustomers = async () => {
    ui.clearError()
    isLoadingData.value = true
    
    try {
      // Build API parameters based on current filters and pagination
      const params = buildAPIParams(
        filters.value, 
        activeTab.value, 
        tabs.value, 
        currentPage.value, 
        pageSize.value
      )
      
      const response = await fetchAllCustomers(toast, params)
      statusTotals.value = await fetchCustomerStatusTotals()
      
      if (response.content) {
        // Using real API data
        apiResponse.value = response
        allCustomers.value = response.content
      } else {
        // No fallback - clear data if API fails
        apiResponse.value = null
        allCustomers.value = []
      }

      // Adjust current page if needed
      if (currentPage.value >= totalPages.value) {
        currentPage.value = Math.max(0, totalPages.value - 1)
      }
    } catch (error) {
      ui.setError(`Lỗi khi tải dữ liệu khách hàng: ${error.message}`)
    } finally {
      isLoadingData.value = false
    }
  }

  const handleCreateCustomer = async () => {
    ui.setLoading('updating', true)
    
    try {
      const createdCustomer = await createCustomer(newCustomer.value, toast)
      if (createdCustomer) {
        allCustomers.value.unshift(createdCustomer)
        ui.closeAllModals()
        newCustomer.value = resetCustomerForm()
        ui.highlightCustomer(createdCustomer.id)
      }
    } catch (error) {
      console.error('Error in handleCreateCustomer:', error)
    } finally {
      ui.setLoading('updating', false)
    }
  }

  const handleUpdateCustomer = async () => {
    ui.setLoading('updating', true)
    
    try {
      const updatedCustomer = await updateCustomer(
        ui.editingCustomer.value.id, 
        ui.editingCustomer.value, 
        toast
      )
      if (updatedCustomer) {
        // Refresh data from API to get latest state
        await handleFetchAllCustomers()
        ui.closeAllModals()
        ui.highlightCustomer(updatedCustomer.id)
      }
    } catch (error) {
      console.error('Error in handleUpdateCustomer:', error)
    } finally {
      ui.setLoading('updating', false)
    }
  }

  const handleToggleCustomerStatus = async (customer, event = null) => {
    // Vô hiệu hóa khách hàng thực chất là xóa mềm tài khoản (khachHangAPI.deleteKhachHang)
    // -> bắt buộc hỏi lại; bản cũ gạt công tắc là chạy thẳng.
    const dangHoatDong = Boolean(customer.trangThai)
    const dongY = await confirm({
      title: dangHoatDong ? 'Chuyển khách hàng sang Ngừng hoạt động?' : 'Kích hoạt lại khách hàng?',
      message: dangHoatDong
        ? 'Khách hàng sẽ không đăng nhập và đặt hàng được nữa. Lịch sử đơn hàng vẫn được giữ.'
        : 'Khách hàng sẽ đăng nhập và mua hàng trở lại được.',
      details: [`${customer.maKhachHang || ''} ${customer.tenKhachHang || ''}`.trim()],
      confirmText: dangHoatDong ? 'Ngừng hoạt động' : 'Kích hoạt',
      tone: dangHoatDong ? 'danger' : 'info'
    })
    if (!dongY) {
      // Trình duyệt đã tự lật ô checkbox; trả công tắc về đúng trạng thái dữ liệu.
      if (event?.target) event.target.checked = dangHoatDong
      return
    }

    ui.setLoading('updating', true)

    try {
      const updatedCustomer = await toggleCustomerStatus(customer, toast)
      if (updatedCustomer) {
        // Refresh data from API to get latest state
        await handleFetchAllCustomers()
        ui.highlightCustomer(updatedCustomer.id)
      }
    } catch (error) {
      console.error('Error in handleToggleCustomerStatus:', error)
    } finally {
      ui.setLoading('updating', false)
    }
  }

  // Navigation functions
  const handleNavigateToAddCustomer = () => {
    router.push('/them-khach-hang')
  }

  const handleEditKhachHang = (customer) => {
    router.push(`/khach-hang/edit/${customer.id}`)
  }

  // Filter functions
  const handleSwitchTab = (tabId) => {
    activeTab.value = tabId
    currentPage.value = 0
    switchTab(tabId, tabs.value, filters.value, toast, handleFetchAllCustomers)
  }

  const handleApplyFilters = () => {
    currentPage.value = 0
    applyFilters(tempFilters.value, filters.value, toast, handleFetchAllCustomers)
  }

  const handleResetFilters = () => {
    activeTab.value = 'all'
    currentPage.value = 0
    resetFilters(filters.value, tempFilters.value, toast, handleFetchAllCustomers)
  }

  const handleChangePage = (page) => {
    const newPage = changePage(page, totalPages.value)
    if (newPage !== null) {
      currentPage.value = newPage
      // Fetch new page data from API
      handleFetchAllCustomers()
    }
  }

  // Export functions
  const handleExportToExcel = () => {
    // Export functionality can be implemented later
    toast.info('Chức năng xuất Excel sẽ được triển khai sau.')
  }

  const handleExportCustomerDetail = (customer) => {
    // Export detail functionality can be implemented later
    toast.info('Chức năng xuất chi tiết sẽ được triển khai sau.')
  }

  // Utility functions
  const getTabCountForTab = (tabId) => {
    return getTabCount(allCustomers.value, tabId, tabs.value)
  }

  const resetNewCustomerForm = () => {
    newCustomer.value = resetCustomerForm()
  }

  // Lifecycle
  onMounted(() => {
    handleFetchAllCustomers()
  })

  // Get customer by ID function
  const handleGetCustomerById = async (id) => {
    try {
      return await getCustomerById(id, toast)
    } catch (error) {
      console.error('Error getting customer by ID:', error)
      throw error
    }
  }

  return {
    // Data
    allCustomers,
    filteredCustomers,
    paginatedCustomers,
    currentPage,
    pageSize,
    totalPages,
    activeTab,
    newCustomer,
    filters,
    tempFilters,
    tabs,
    tableColumns,
    pageStats,
    breadcrumbItems,
    breadcrumbActions,
    apiResponse,
    isLoadingData,

    // UI state from composable
    ...ui,

    // Core functions
    handleFetchAllCustomers,
    handleCreateCustomer,
    handleUpdateCustomer,
    handleToggleCustomerStatus,

    // Navigation
    handleNavigateToAddCustomer,
    handleEditKhachHang,

    // Filters
    handleSwitchTab,
    handleApplyFilters,
    handleResetFilters,
    handleChangePage,

    // Export
    handleExportToExcel,
    handleExportCustomerDetail,

    // Utilities
    getTabCountForTab,
    resetNewCustomerForm,
    handleGetCustomerById,
    formatDate,
    formatDateTime,
    formatGender,
    formatStatus,
    formatPhone,
    formatAddress,
    validateCustomer,
    validateCustomerForm
  }
}
