<script setup>
import { Icon } from '@iconify/vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import DataTable from '@/components/DataTable.vue'

// Import the main composable
import { useNhanVienLogic } from '@/services/nhanVienService/nhanVienComposable.js'

// Use the composable to get all the logic and state
const {
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

  // UI state
  showAddModal,
  showEditModal,
  showDetailModal,
  isUpdating,
  isDeleting,
  viewingEmployee,
  editingEmployee,
  highlightedEmployee,
  errorMessage,

  // UI actions
  openAddModal,
  openEditModal,
  openDetailModal,
  closeAllModals,

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
} = useNhanVienLogic()

// Debounce function for search input
let searchTimeout = null
const debouncedSearch = () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    handleApplyFilters()
  }, 500) // 500ms delay
}

// Alias functions for template compatibility
const fetchAllEmployees = handleFetchAllEmployees
const navigateToAddEmployee = handleNavigateToAddEmployee
const editNhanVien = handleEditNhanVien
const handleAddEmployee = handleCreateEmployee
const toggleEmployeeStatus = handleToggleEmployeeStatus
const switchTab = handleSwitchTab
const filterEmployees = handleApplyFilters
const resetFilters = handleResetFilters
const changePage = handleChangePage
const exportToExcel = handleExportToExcel
const getTabCount = getTabCountForTab
</script>

<template>
  <div
    class="min-h-screen flex flex-col font-roboto transition-colors duration-300">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" :actions="breadcrumbActions" :show-page-info="true"
      page-title="Quản lý Nhân Viên" page-description="Quản lý và theo dõi tất cả nhân viên trong hệ thống"
      page-icon="solar:users-group-two-rounded-bold-duotone" :page-stats="pageStats" />

    <div v-if="errorMessage"
      class="card bg-red-50 dark:bg-red-950 p-4 mb-6 rounded-3xl shadow-lg animate__animated animate__fadeIn">
      <p class="text-red-600 dark:text-red-300 font-medium flex items-center">
        <Icon icon="solar:danger-triangle-bold-duotone" class="text-2xl mr-3" />
        {{ errorMessage }}
      </p>
    </div>

    <!-- Filter Section -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-6 mb-6">
      <div class="filter-header mb-4">
        <h3 class="filter-title">
          <Icon icon="solar:filter-bold-duotone" class="text-xl" />
          Bộ Lọc Nhân Viên
        </h3>
        <button class="reset-filter-btn" @click="handleResetFilters">
          Đặt lại bộ lọc
        </button>
      </div>

      <div class="filter-content">
        <!-- First Row: Search and Status -->
        <div class="filter-row mb-4">
          <div class="filter-group">
            <label class="filter-label">Tìm kiếm</label>
            <input v-model="tempFilters.search" type="text" placeholder="Tìm kiếm mã NV, tên nhân viên, SĐT..."
              class="filter-input" @input="debouncedSearch" />
          </div>
          <div class="filter-group">
            <label class="filter-label">Trạng thái</label>
            <div class="radio-group">
              <label class="radio-option">
                <input type="radio" v-model="tempFilters.trangThai" value="" @change="handleApplyFilters"
                  class="radio-input" />
                <span class="radio-label">Tất cả</span>
              </label>
              <label class="radio-option">
                <input type="radio" v-model="tempFilters.trangThai" value="true" @change="handleApplyFilters"
                  class="radio-input" />
                <span class="radio-label">Hoạt động</span>
              </label>
              <label class="radio-option">
                <input type="radio" v-model="tempFilters.trangThai" value="false" @change="handleApplyFilters"
                  class="radio-input" />
                <span class="radio-label">Ngừng hoạt động</span>
              </label>
            </div>
          </div>
        </div>

        <!-- Second Row: Gender and Sort -->
        <div class="filter-row">
          <div class="filter-group">
            <label class="filter-label">Giới tính</label>
            <select v-model="tempFilters.gioiTinh" class="filter-input" @change="handleApplyFilters">
              <option value="">Tất cả giới tính</option>
              <option value="true">Nam</option>
              <option value="false">Nữ</option>
            </select>
          </div>
          <div class="filter-group">
            <label class="filter-label">Sắp xếp theo</label>
            <select v-model="tempFilters.sortBy" class="filter-select" @change="handleApplyFilters">
              <option value="">Sắp xếp mặc định</option>
              <option value="newest">Mới nhất</option>
              <option value="oldest">Cũ nhất</option>
              <option value="name_asc">Tên A-Z</option>
              <option value="name_desc">Tên Z-A</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-lg">
      <div class="p-6">

        <!-- Employee List Section -->
        <div class="employees-section">
          <div class="section-header">
            <h3 class="section-title">
              <Icon icon="solar:users-group-two-rounded-bold-duotone" class="text-xl" />
              Danh Sách Nhân Viên ({{ filteredEmployees.length }})
            </h3>
          </div>

          <!-- Loading State -->
          <div v-if="isLoadingData" class="flex justify-center items-center py-12">
            <div class="flex flex-col items-center gap-4">
              <Icon icon="solar:loading-bold" class="text-4xl text-blue-500 animate-spin" />
              <p class="text-gray-600 dark:text-gray-400">Đang tải dữ liệu nhân viên...</p>
            </div>
          </div>

          <!-- DataTable Component -->
          <DataTable v-else :data="paginatedEmployees" :columns="tableColumns" item-label="nhân viên"
            empty-message="Không có dữ liệu nhân viên nào được tìm thấy." key-field="id" :current-page="currentPage"
            :total-pages="totalPages" :page-size="pageSize" @page-change="handleChangePage">
            <!-- Custom column templates -->
            <template #stt="{ rowIndex }">
              {{ rowIndex }}
            </template>

            <template #maNhanVien="{ item }">
              <span class="font-bold text-gray-800">{{ item.maNhanVien }}</span>
            </template>

            <template #tenNhanVien="{ item }">
              {{ item.tenNhanVien }}
            </template>

            <template #ngaySinh="{ item }">
              {{ formatDate(item.ngaySinh) }}
            </template>

            <template #gioiTinh="{ item }">
              <span
                :class="item.gioiTinh ? 'inline-flex items-center text-blue-700 bg-blue-100 px-2.5 py-1 rounded-full font-medium text-xs' : 'inline-flex items-center text-pink-700 bg-pink-100 px-2.5 py-1 rounded-full font-medium text-xs'">
                <Icon :icon="item.gioiTinh ? 'solar:men-bold' : 'solar:women-bold'" class="w-3 h-3 mr-1" />
                {{ item.gioiTinh ? 'Nam' : 'Nữ' }}
              </span>
            </template>

            <template #soDienThoai="{ item }">
              {{ item.soDienThoai }}
            </template>

            <template #cccd="{ item }">
              {{ item.cccd }}
            </template>

            <template #diaChi="{ item }">
              {{ `${item.diaChiSoNhaTenDuong}, ${item.diaChiPhuongXa}, ${item.diaChiQuanHuyen}, ${item.diaChiTinhThanh}`
              }}
            </template>

            <template #trangThai="{ item }">
              <span
                :class="item.trangThai ? 'inline-flex items-center text-green-700 bg-green-100 px-2.5 py-1 rounded-full font-medium text-xs' : 'inline-flex items-center text-red-700 bg-red-100 px-2.5 py-1 rounded-full font-medium text-xs'">
                <Icon :icon="item.trangThai ? 'solar:check-circle-bold' : 'solar:close-circle-bold'"
                  class="w-3 h-3 mr-1" />
                {{ item.trangThai ? 'Hoạt động' : 'Ngừng hoạt động' }}
              </span>
            </template>

            <template #actions="{ item }">
              <div class="flex justify-center items-center gap-3">
                <button @click="editNhanVien(item)" class="action-btn edit" title="Sửa">
                  <Icon icon="solar:pen-bold" />
                </button>
                <label class="relative inline-flex items-center cursor-pointer">
                  <input type="checkbox" :checked="item.trangThai" @change="toggleEmployeeStatus(item, $event)"
                    class="sr-only peer" />
                  <div
                    class="relative w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600">
                  </div>
                </label>
              </div>
            </template>
          </DataTable>
        </div>
      </div>
    </div>

    <!-- Add Employee Modal -->
    <div v-if="showAddModal"
      class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-50 animate__animated animate__fadeIn">
      <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 w-full max-w-4xl shadow-2xl max-h-[90vh] overflow-y-auto">
        <h3 class="text-xl font-bold text-gray-900 dark:text-gray-100 mb-4 flex items-center">
          <Icon icon="solar:user-plus-bold" class="text-2xl mr-2 text-green-600" />
          Thêm Nhân Viên Mới
        </h3>
        <form @submit.prevent="handleAddEmployee">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Tên nhân viên <span
                  class="text-red-500">*</span></label>
              <input v-model="newEmployee.tenNhanVien" type="text"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-green-400 focus:border-green-400 transition duration-300"
                placeholder="Nguyễn Văn A" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Ngày sinh <span
                  class="text-red-500">*</span></label>
              <input v-model="newEmployee.ngaySinh" type="date"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-green-400 focus:border-green-400 transition duration-300"
                required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Giới tính</label>
              <select v-model="newEmployee.gioiTinh"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-green-400 focus:border-green-400 transition duration-300">
                <option :value="true">Nam</option>
                <option :value="false">Nữ</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Số điện thoại <span
                  class="text-red-500">*</span></label>
              <input v-model="newEmployee.soDienThoai" type="text"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-green-400 focus:border-green-400 transition duration-300"
                placeholder="0123456789" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">CCCD <span
                  class="text-red-500">*</span></label>
              <input v-model="newEmployee.cccd" type="text"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-green-400 focus:border-green-400 transition duration-300"
                placeholder="123456789012" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Số nhà, tên đường <span
                  class="text-red-500">*</span></label>
              <input v-model="newEmployee.diaChiSoNhaTenDuong" type="text"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-green-400 focus:border-green-400 transition duration-300"
                placeholder="123 Nguyễn Văn Linh" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Phường/Xã <span
                  class="text-red-500">*</span></label>
              <input v-model="newEmployee.diaChiPhuongXa" type="text"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-green-400 focus:border-green-400 transition duration-300"
                placeholder="Phường ABC" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Quận/Huyện <span
                  class="text-red-500">*</span></label>
              <input v-model="newEmployee.diaChiQuanHuyen" type="text"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-green-400 focus:border-green-400 transition duration-300"
                placeholder="Quận XYZ" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Tỉnh/Thành phố <span
                  class="text-red-500">*</span></label>
              <input v-model="newEmployee.diaChiTinhThanh" type="text"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-green-400 focus:border-green-400 transition duration-300"
                placeholder="Hà Nội" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Trạng thái</label>
              <select v-model="newEmployee.trangThai"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-green-400 focus:border-green-400 transition duration-300">
                <option :value="true">Hoạt động</option>
                <option :value="false">Ngừng hoạt động</option>
              </select>
            </div>
          </div>
          <div class="flex gap-3 mt-6">
            <button type="submit" :disabled="isUpdating"
              class="btn-primary px-6 py-3 rounded-xl bg-emerald-500 text-white hover:bg-emerald-600 transition duration-300 shadow-lg hover:shadow-xl flex-1 flex items-center justify-center">
              <Icon v-if="isUpdating" icon="solar:loading-bold" class="animate-spin mr-2" />
              Lưu
            </button>
            <button type="button" @click="showAddModal = false" :disabled="isUpdating"
              class="btn-secondary px-6 py-3 rounded-xl bg-rose-500 text-white hover:bg-rose-600 transition duration-300 shadow-lg hover:shadow-xl flex-1">
              Hủy
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Edit Employee Modal -->
    <div v-if="showEditModal"
      class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-50 animate__animated animate__fadeIn">
      <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 w-full max-w-4xl shadow-2xl max-h-[90vh] overflow-y-auto">
        <h3 class="text-xl font-bold text-gray-900 dark:text-gray-100 mb-4 flex items-center">
          <Icon icon="solar:pen-bold" class="text-2xl mr-2 text-yellow-600" />
          Sửa Thông Tin Nhân Viên
        </h3>
        <form @submit.prevent="handleUpdateEmployee">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Tên nhân viên <span
                  class="text-red-500">*</span></label>
              <input v-model="editingEmployee.tenNhanVien" type="text"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 transition duration-300"
                required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Ngày sinh <span
                  class="text-red-500">*</span></label>
              <input v-model="editingEmployee.ngaySinh" type="date"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 transition duration-300"
                required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Giới tính</label>
              <select v-model="editingEmployee.gioiTinh"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 transition duration-300">
                <option :value="true">Nam</option>
                <option :value="false">Nữ</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Số điện thoại <span
                  class="text-red-500">*</span></label>
              <input v-model="editingEmployee.soDienThoai" type="text"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 transition duration-300"
                required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">CCCD <span
                  class="text-red-500">*</span></label>
              <input v-model="editingEmployee.cccd" type="text"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 transition duration-300"
                required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Số nhà, tên đường <span
                  class="text-red-500">*</span></label>
              <input v-model="editingEmployee.diaChiSoNhaTenDuong" type="text"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 transition duration-300"
                required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Phường/Xã <span
                  class="text-red-500">*</span></label>
              <input v-model="editingEmployee.diaChiPhuongXa" type="text"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 transition duration-300"
                required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Quận/Huyện <span
                  class="text-red-500">*</span></label>
              <input v-model="editingEmployee.diaChiQuanHuyen" type="text"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 transition duration-300"
                required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Tỉnh/Thành phố <span
                  class="text-red-500">*</span></label>
              <input v-model="editingEmployee.diaChiTinhThanh" type="text"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 transition duration-300"
                required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">Trạng thái</label>
              <select v-model="editingEmployee.trangThai"
                class="w-full border border-gray-300 dark:border-gray-600 rounded-xl p-3 bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-200 focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 transition duration-300">
                <option :value="true">Hoạt động</option>
                <option :value="false">Ngừng hoạt động</option>
              </select>
            </div>
          </div>
          <div class="flex gap-3 mt-6">
            <button type="submit" :disabled="isUpdating"
              class="btn-primary px-6 py-3 rounded-xl bg-emerald-500 text-white hover:bg-emerald-600 transition duration-300 shadow-lg hover:shadow-xl flex-1 flex items-center justify-center">
              <Icon v-if="isUpdating" icon="solar:loading-bold" class="animate-spin mr-2" />
              Lưu thay đổi
            </button>
            <button type="button" @click="showEditModal = false" :disabled="isUpdating"
              class="btn-secondary px-6 py-3 rounded-xl bg-rose-500 text-white hover:bg-rose-600 transition duration-300 shadow-lg hover:shadow-xl flex-1">
              Hủy
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Detail Employee Modal -->
    <div v-if="showDetailModal"
      class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-50 animate__animated animate__fadeIn">
      <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 w-full max-w-2xl shadow-2xl">
        <h3 class="text-xl font-bold text-gray-900 dark:text-gray-100 mb-4 flex items-center">
          <Icon icon="solar:eye-bold" class="text-2xl mr-2 text-blue-600" />
          Chi Tiết Nhân Viên
        </h3>
        <div v-if="viewingEmployee" class="space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <span class="text-sm font-medium text-gray-500 dark:text-gray-400">Mã nhân viên:</span>
              <p class="font-semibold text-gray-900 dark:text-gray-100">{{ viewingEmployee.maNhanVien }}</p>
            </div>
            <div>
              <span class="text-sm font-medium text-gray-500 dark:text-gray-400">Tên nhân viên:</span>
              <p class="font-semibold text-gray-900 dark:text-gray-100">{{ viewingEmployee.tenNhanVien }}</p>
            </div>
            <div>
              <span class="text-sm font-medium text-gray-500 dark:text-gray-400">Ngày sinh:</span>
              <p class="font-semibold text-gray-900 dark:text-gray-100">{{ formatDate(viewingEmployee.ngaySinh) }}</p>
            </div>
            <div>
              <span class="text-sm font-medium text-gray-500 dark:text-gray-400">Giới tính:</span>
              <p class="font-semibold text-gray-900 dark:text-gray-100">{{ viewingEmployee.gioiTinh ? 'Nam' : 'Nữ' }}
              </p>
            </div>
            <div>
              <span class="text-sm font-medium text-gray-500 dark:text-gray-400">Số điện thoại:</span>
              <p class="font-semibold text-gray-900 dark:text-gray-100">{{ viewingEmployee.soDienThoai }}</p>
            </div>
            <div>
              <span class="text-sm font-medium text-gray-500 dark:text-gray-400">CCCD:</span>
              <p class="font-semibold text-gray-900 dark:text-gray-100">{{ viewingEmployee.cccd }}</p>
            </div>
            <div class="md:col-span-2">
              <span class="text-sm font-medium text-gray-500 dark:text-gray-400">Địa chỉ:</span>
              <p class="font-semibold text-gray-900 dark:text-gray-100">
                {{ `${viewingEmployee.diaChiSoNhaTenDuong}, ${viewingEmployee.diaChiPhuongXa},
                ${viewingEmployee.diaChiQuanHuyen}, ${viewingEmployee.diaChiTinhThanh}` }}
              </p>
            </div>
            <div>
              <span class="text-sm font-medium text-gray-500 dark:text-gray-400">Ngày tạo:</span>
              <p class="font-semibold text-gray-900 dark:text-gray-100">{{ formatDateTime(viewingEmployee.ngayTao) }}
              </p>
            </div>
            <div>
              <span class="text-sm font-medium text-gray-500 dark:text-gray-400">Ngày cập nhật:</span>
              <p class="font-semibold text-gray-900 dark:text-gray-100">{{ formatDateTime(viewingEmployee.ngayCapNhat)
              }}
              </p>
            </div>
            <div class="md:col-span-2">
              <span class="text-sm font-medium text-gray-500 dark:text-gray-400">Trạng thái:</span>
              <span :class="viewingEmployee.trangThai ? 'status-badge active' : 'status-badge inactive'">
                <Icon :icon="viewingEmployee.trangThai ? 'solar:check-circle-bold' : 'solar:close-circle-bold'"
                  class="w-3 h-3 mr-1" />
                {{ viewingEmployee.trangThai ? 'Hoạt động' : 'Ngừng hoạt động' }}
              </span>
            </div>
          </div>
        </div>
        <div class="flex justify-end mt-6">
          <button @click="showDetailModal = false"
            class="btn-secondary px-6 py-3 rounded-xl bg-gray-500 text-white hover:bg-gray-600 transition duration-300 shadow-lg hover:shadow-xl">
            Đóng
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import 'vue-toastification/dist/index.css';
@import url('https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700;900&display=swap');

:root {
  --primary-blue: #3b82f6;
  --primary-blue-dark: #2563eb;
  --card-bg-light: #ffffff;
  --card-bg-dark: #1f2937;
  --text-color-light: #1f2937;
  --text-color-dark: #e5e7eb;
}

.font-roboto {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
}

.card {
  border-radius: 24px;
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  background: var(--card-bg-light);
}

.dark .card {
  background: var(--card-bg-dark);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.3);
}

.btn-primary {
  background: #10b981;
  color: white;
  font-weight: 600;
  transition: background 0.3s ease, transform 0.2s ease, box-shadow 0.3s ease;
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-primary:hover:not(:disabled) {
  background: #059669;
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(16, 185, 129, 0.45);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.btn-secondary {
  background: #ef4444;
  color: white;
  font-weight: 600;
  transition: background 0.3s ease, transform 0.2s ease, box-shadow 0.3s ease;
  box-shadow: 0 4px 15px rgba(239, 68, 68, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-secondary:hover:not(:disabled) {
  background: #dc2626;
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(239, 68, 68, 0.45);
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

/* Filter Section Styling */
.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.reset-filter-btn {
  padding: 10px 16px;
  background: #f1f5f9;
  color: #64748b;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
}

.reset-filter-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(107, 114, 128, 0.3);
}

.filter-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.25rem;
}

.filter-input,
.filter-select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #ffffff;
  color: #1f2937;
  font-size: 0.875rem;
  transition: all 0.2s ease;
}

.filter-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* Radio Button Styles */
.radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.25rem;
}

.radio-option {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0.375rem 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: #ffffff;
  transition: all 0.2s ease;
  min-width: fit-content;
  font-size: 0.875rem;
}

.radio-option:hover {
  border-color: #3b82f6;
  background: #f8fafc;
}

.radio-option:has(.radio-input:checked) {
  border-color: #3b82f6;
  background: #eff6ff;
  color: #1e40af;
}

.radio-input {
  margin: 0;
  margin-right: 0.5rem;
  accent-color: #3b82f6;
  cursor: pointer;
}

.radio-label {
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  user-select: none;
}

.action-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
}

.action-btn.edit {
  background: #dcfce7;
  color: #166534;
}

.action-btn:hover {
  transform: scale(1.1);
}

.action-btn:focus {
  outline: 2px solid #ff3b18;
  outline-offset: 2px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.dark .section-header {
  border-bottom-color: #4b5563;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.25rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0;
}

@media (max-width: 640px) {
  .filter-row {
    grid-template-columns: 1fr;
  }

  .table th,
  .table td {
    padding: 10px;
    font-size: 0.8rem;
  }

  .tabs {
    gap: 0.5rem;
    padding-bottom: 0.5rem;
  }

  .tab {
    padding: 0.5rem 0.8rem;
    font-size: 0.75rem;
  }

  .pagination {
    flex-direction: column;
    align-items: center;
  }

  .pagination button {
    width: 100%;
    margin-bottom: 8px;
  }
}

/* Order Type Badge Styles */
.order-type-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.online-order {
  background: #dbeafe;
  color: #1e40af;
  border: 1px solid #93c5fd;
}

.online-order:hover {
  background: #bfdbfe;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.2);
}

.offline-order {
  background: #d1fae5;
  color: #065f46;
  border: 1px solid #86efac;
}

.offline-order:hover {
  background: #bbf7d0;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.2);
}
</style>
