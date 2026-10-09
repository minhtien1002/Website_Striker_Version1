import { ref, computed, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import { useRouter } from 'vue-router'

// Import all service modules
import { nhanVienConfig, getBreadcrumbItems, getBreadcrumbActions } from './nhanVienConfig.js'
import { formatDate, formatDateTime, formatGender, formatStatus, formatAddress } from './nhanVienFormatters.js'
import { validateEmployee, validateEmployeeForm } from './nhanVienValidation.js'
import { 
  buildAPIParams,
  filterEmployees, 
  paginateEmployees, 
  calculateTotalPages, 
  getTabCount, 
  switchTab, 
  applyFilters, 
  resetFilters, 
  changePage 
} from './nhanVienFilters.js'
import { 
  fetchAllEmployees, 
  createEmployee, 
  updateEmployee, 
  toggleEmployeeStatus, 
  resetEmployeeForm,
  getEmployeeById 
} from './nhanVienCRUD.js'
import { exportToExcel, exportEmployeeDetail } from './nhanVienExport.js'
import { useNhanVienUI } from './nhanVienUI.js'
import { useConfirm } from '@/composables/useConfirm.js'

/**
 * Main composable function that encapsulates all NhanVien logic
 */
export function useNhanVienLogic() {
  const toast = useToast()
  const router = useRouter()
  const { confirm } = useConfirm()

  // UI composable
  const ui = useNhanVienUI()

  // Core data
  const allEmployees = ref([])
  const apiResponse = ref(null)
  const currentPage = ref(0)
  const pageSize = ref(nhanVienConfig.pagination.defaultPageSize)
  const activeTab = ref('all')
  const isLoadingData = ref(false)

  // Form data
  const newEmployee = ref(resetEmployeeForm())
  
  // Filter states
  const filters = ref({ ...nhanVienConfig.defaultFilters })
  const tempFilters = ref({ ...nhanVienConfig.defaultFilters })

  // Configuration
  const tabs = ref(nhanVienConfig.tabs)
  const tableColumns = ref(nhanVienConfig.tableColumns)

  // Computed properties
  const filteredEmployees = computed(() => {
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
    return calculateTotalPages(filteredEmployees.value, pageSize.value)
  })

  const paginatedEmployees = computed(() => {
    // If using API data, return content directly (already paginated by backend)
    if (apiResponse.value) {
      return apiResponse.value.content || []
    }
    // Fallback to client-side pagination
    return paginateEmployees(filteredEmployees.value, currentPage.value, pageSize.value)
  })

  const pageStats = computed(() => {
    const totalEmployees = apiResponse.value?.totalElements || allEmployees.value.length
    const currentEmployees = filteredEmployees.value
    
    return [
      {
        icon: 'solar:users-group-two-rounded-bold',
        value: totalEmployees,
        label: 'Tổng nhân viên'
      },
      {
        icon: 'solar:check-circle-bold',
        value: currentEmployees.filter(emp => emp.trangThai).length,
        label: 'Hoạt động'
      },
      {
        icon: 'solar:close-circle-bold',
        value: currentEmployees.filter(emp => !emp.trangThai).length,
        label: 'Ngừng hoạt động'
      }
    ]
  })

  // Breadcrumb configuration
  const breadcrumbItems = ref(getBreadcrumbItems())
  const breadcrumbActions = computed(() => getBreadcrumbActions({
    fetchAllEmployees: handleFetchAllEmployees,
    exportToExcel: handleExportToExcel,
    navigateToAddEmployee: handleNavigateToAddEmployee
  }))

  // Core functions
  const handleFetchAllEmployees = async () => {
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
      
      const response = await fetchAllEmployees(toast, params)
      
      if (response.content) {
        // Using real API data
        apiResponse.value = response
        allEmployees.value = response.content
      } else {
        // No fallback - clear data if API fails
        apiResponse.value = null
        allEmployees.value = []
      }

      // Adjust current page if needed
      if (currentPage.value >= totalPages.value) {
        currentPage.value = Math.max(0, totalPages.value - 1)
      }
    } catch (error) {
      ui.setError(`Lỗi khi tải dữ liệu nhân viên: ${error.message}`)
    } finally {
      isLoadingData.value = false
    }
  }

  const handleCreateEmployee = async () => {
    ui.setLoading('updating', true)
    
    try {
      const createdEmployee = await createEmployee(newEmployee.value, toast)
      if (createdEmployee) {
        allEmployees.value.unshift(createdEmployee)
        ui.closeAllModals()
        newEmployee.value = resetEmployeeForm()
        ui.highlightEmployee(createdEmployee.id)
      }
    } catch (error) {
      console.error('Error in handleCreateEmployee:', error)
    } finally {
      ui.setLoading('updating', false)
    }
  }

  const handleUpdateEmployee = async () => {
    ui.setLoading('updating', true)
    
    try {
      const updatedEmployee = await updateEmployee(
        ui.editingEmployee.value.id, 
        ui.editingEmployee.value, 
        toast
      )
      if (updatedEmployee) {
        // Refresh data from API to get latest state
        await handleFetchAllEmployees()
        ui.closeAllModals()
        ui.highlightEmployee(updatedEmployee.id)
      }
    } catch (error) {
      console.error('Error in handleUpdateEmployee:', error)
    } finally {
      ui.setLoading('updating', false)
    }
  }

  const handleToggleEmployeeStatus = async (employee, event = null) => {
    // Vô hiệu hóa nhân viên thực chất là xóa mềm tài khoản (nhanVienAPI.deleteNhanVien)
    // -> bắt buộc hỏi lại; bản cũ gạt công tắc là chạy thẳng.
    const dangHoatDong = Boolean(employee.trangThai)
    const dongY = await confirm({
      title: dangHoatDong ? 'Chuyển nhân viên sang Ngừng hoạt động?' : 'Kích hoạt lại nhân viên?',
      message: dangHoatDong
        ? 'Nhân viên sẽ không đăng nhập được vào hệ thống nữa. Dữ liệu cũ vẫn được giữ nguyên.'
        : 'Nhân viên sẽ đăng nhập và làm việc trở lại được.',
      details: [`${employee.maNhanVien || ''} ${employee.tenNhanVien || ''}`.trim()],
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
      const updatedEmployee = await toggleEmployeeStatus(employee, toast)
      if (updatedEmployee) {
        // Refresh data from API to get latest state
        await handleFetchAllEmployees()
        ui.highlightEmployee(updatedEmployee.id)
      }
    } catch (error) {
      console.error('Error in handleToggleEmployeeStatus:', error)
    } finally {
      ui.setLoading('updating', false)
    }
  }

  // Navigation functions
  const handleNavigateToAddEmployee = () => {
    router.push('/them-nhan-vien')
  }

  const handleEditNhanVien = (employee) => {
    router.push(`/nhan-vien/edit/${employee.id}`)
  }

  // Filter functions
  const handleSwitchTab = (tabId) => {
    activeTab.value = tabId
    currentPage.value = 0
    switchTab(tabId, tabs.value, filters.value, toast, handleFetchAllEmployees)
  }

  const handleApplyFilters = () => {
    currentPage.value = 0
    applyFilters(tempFilters.value, filters.value, toast, handleFetchAllEmployees)
  }

  const handleResetFilters = () => {
    activeTab.value = 'all'
    currentPage.value = 0
    resetFilters(filters.value, tempFilters.value, toast, handleFetchAllEmployees)
  }

  const handleChangePage = (page) => {
    const newPage = changePage(page, totalPages.value)
    if (newPage !== null) {
      currentPage.value = newPage
      // Fetch new page data from API
      handleFetchAllEmployees()
    }
  }

  // Export functions
  const handleExportToExcel = () => {
    exportToExcel(filteredEmployees.value, toast)
  }

  const handleExportEmployeeDetail = (employee) => {
    exportEmployeeDetail(employee, toast)
  }

  // Utility functions
  const getTabCountForTab = (tabId) => {
    return getTabCount(allEmployees.value, tabId, tabs.value)
  }

  const resetNewEmployeeForm = () => {
    newEmployee.value = resetEmployeeForm()
  }

  // Lifecycle
  onMounted(() => {
    handleFetchAllEmployees()
  })

  // Get employee by ID function
  const handleGetEmployeeById = async (id) => {
    try {
      return await getEmployeeById(id, toast)
    } catch (error) {
      console.error('Error getting employee by ID:', error)
      throw error
    }
  }

  return {
    // Data
    allEmployees,
    filteredEmployees,
    paginatedEmployees,
    currentPage,
    pageSize,
    totalPages,
    activeTab,
    newEmployee,
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
    handleFetchAllEmployees,
    handleCreateEmployee,
    handleUpdateEmployee,
    handleToggleEmployeeStatus,

    // Navigation
    handleNavigateToAddEmployee,
    handleEditNhanVien,

    // Filters
    handleSwitchTab,
    handleApplyFilters,
    handleResetFilters,
    handleChangePage,

    // Export
    handleExportToExcel,
    handleExportEmployeeDetail,

    // Utilities
    getTabCountForTab,
    resetNewEmployeeForm,
    handleGetEmployeeById,
    formatDate,
    formatDateTime,
    formatGender,
    formatStatus,
    formatAddress,
    validateEmployee,
    validateEmployeeForm
  }
}
