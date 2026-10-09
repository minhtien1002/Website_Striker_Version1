<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue';
import { Icon } from '@iconify/vue';
import Breadcrumb from '@/components/Breadcrumb.vue';
import DataTable from '@/components/DataTable.vue';
import QRScannerModal from '@/components/QRScannerModal.vue';
import { useHoaDonLogic } from '@/services/hoaDonService/hoaDonComposable.js';

// Use the composable to get all reactive state and functions
const {
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

  // Service functions
  formatDate,
  formatCurrency,
  getStatusText,
  getStatusColor,
  printInvoicePDF,
  downloadQRCode,
  exportToExcel,
  scanQRInvoice,

  // Status counts
  statusCounts,
  isLoadingStatusCounts,
  getStatusCount,
  statusOptions,

  // Price range
  priceRange,
  isLoadingPriceRange,
  loadPriceRange,

  // Search debounce
  isSearching,

  // QR Scanner modal
  showQRScannerModal,
  openQRScannerModal,
  closeQRScannerModal,
  handleInvoiceFound,
  
  // API pagination
  apiTotalPages,
  apiTotalElements
} = useHoaDonLogic();

// Reactive variables for formatted price inputs
const formattedMinPrice = ref('');
const formattedMaxPrice = ref('');

// Debounce timeout for price inputs
const priceDebounceTimeout = ref(null);

// Currency formatting methods
const formatCurrencyInput = (value) => {
  if (!value) return '';
  const numericValue = value.toString().replace(/[^0-9]/g, '');
  if (!numericValue) return '';
  return new Intl.NumberFormat('vi-VN').format(parseInt(numericValue)) + ' ₫';
};

const parseCurrencyInput = (value) => {
  if (!value) return '';
  return value.replace(/[^0-9]/g, '');
};

// Debounced filter function for price inputs
const debouncedPriceFilter = () => {
  if (priceDebounceTimeout.value) {
    clearTimeout(priceDebounceTimeout.value);
  }
  
  priceDebounceTimeout.value = setTimeout(() => {
    // Only call API if both values are valid or empty
    const minPrice = tempFilters.value.minPrice;
    const maxPrice = tempFilters.value.maxPrice;
    
    // Skip API call if minPrice > maxPrice (invalid state)
    if (minPrice && maxPrice && parseInt(minPrice) > parseInt(maxPrice)) {
      return; // Don't call API with invalid price range
    }
    
    filterInvoicesLocal();
  }, 800); // 800ms delay to allow user to finish typing
};

// Input handlers for price fields
const onMinPriceInput = (event) => {
  const rawValue = event.target.value;
  const numericValue = parseCurrencyInput(rawValue);
  tempFilters.value.minPrice = numericValue;
  formattedMinPrice.value = formatCurrencyInput(numericValue);
  debouncedPriceFilter();
};

const onMaxPriceInput = (event) => {
  const rawValue = event.target.value;
  const numericValue = parseCurrencyInput(rawValue);
  tempFilters.value.maxPrice = numericValue;
  formattedMaxPrice.value = formatCurrencyInput(numericValue);
  debouncedPriceFilter();
};

const onMinPriceBlur = () => {
  formattedMinPrice.value = formatCurrencyInput(tempFilters.value.minPrice);
};

const onMaxPriceBlur = () => {
  formattedMaxPrice.value = formatCurrencyInput(tempFilters.value.maxPrice);
};

// Initialize formatted values
const initializeFormattedValues = () => {
  formattedMinPrice.value = formatCurrencyInput(tempFilters.value.minPrice);
  formattedMaxPrice.value = formatCurrencyInput(tempFilters.value.maxPrice);
};

// Watch for changes in tempFilters to update formatted values
watch(() => tempFilters.value.minPrice, (newValue) => {
  if (!newValue) formattedMinPrice.value = '';
});

watch(() => tempFilters.value.maxPrice, (newValue) => {
  if (!newValue) formattedMaxPrice.value = '';
});

// Initialize on mount
onMounted(() => {
  initializeFormattedValues();
});

// Watch for reset filters to clear formatted values
watch(() => [tempFilters.value.minPrice, tempFilters.value.maxPrice], ([newMinPrice, newMaxPrice]) => {
  if (!newMinPrice && !newMaxPrice) {
    formattedMinPrice.value = '';
    formattedMaxPrice.value = '';
  }
});

// Cleanup timeout on unmount
onUnmounted(() => {
  if (priceDebounceTimeout.value) {
    clearTimeout(priceDebounceTimeout.value);
  }
});

// Pagination methods for DataTable integration
const handlePageChange = (newPage) => {
  // DataTable uses 1-based indexing, backend uses 0-based
  currentPage.value = newPage - 1;
  loadInvoices();
};

const handlePageSizeChange = (newPageSize) => {
  pageSize.value = newPageSize;
  currentPage.value = 0; // Reset to first page when page size changes
  loadInvoices();
};
</script>

<template>
  <div
    class="min-h-screen flex flex-col font-roboto transition-colors duration-300">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" :actions="breadcrumbActions" :show-page-info="true"
      page-title="Quản lý Hóa đơn" page-description="Quản lý và theo dõi tất cả hóa đơn bán hàng trong hệ thống"
      page-icon="solar:bill-list-bold-duotone" :page-stats="pageStats" />

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
          Bộ Lọc Hóa Đơn
        </h3>
        <button class="reset-filter-btn" @click="resetFiltersLocal">
          Đặt lại bộ lọc
        </button>
      </div>

      <div class="filter-content">
        <div class="filter-row mb-4">
          <div class="filter-group">
            <label class="filter-label">
              Tìm kiếm
              <span v-if="isSearching" class="text-xs text-blue-500 ml-1">(Đang tìm...)</span>
            </label>
            <input v-model="tempFilters.search" type="text" placeholder="Tìm kiếm mã hóa đơn, tên khách hàng..."
              class="filter-input" @input="filterInvoicesLocal(true)" />
          </div>
          <div class="filter-group">
            <label class="filter-label">
              Giá từ
              <span v-if="isLoadingPriceRange" class="text-xs text-gray-500 ml-1">(Đang tải...)</span>
            </label>
            <input v-model="formattedMinPrice" type="text"
              :placeholder="isLoadingPriceRange ? 'Đang tải...' : `Từ ${formatCurrency(priceRange.minPrice || 0)}`"
              class="filter-input" @input="onMinPriceInput" @blur="onMinPriceBlur" />
          </div>
          <div class="filter-group">
            <label class="filter-label">
              Giá đến
              <span v-if="isLoadingPriceRange" class="text-xs text-gray-500 ml-1">(Đang tải...)</span>
            </label>
            <input v-model="formattedMaxPrice" type="text"
              :placeholder="isLoadingPriceRange ? 'Đang tải...' : `Đến ${formatCurrency(priceRange.maxPrice || 0)}`"
              class="filter-input" @input="onMaxPriceInput" @blur="onMaxPriceBlur" />
          </div>
        </div>

        <div class="filter-row">
          <div class="filter-group">
            <label class="filter-label">Loại đơn</label>
            <select v-model="tempFilters.loaiDon" class="filter-input" @change="filterInvoicesLocal(false)">
              <option value="">Tất cả loại đơn</option>
              <option value="Online">Online</option>
              <option value="Tại quầy">Tại quầy</option>
            </select>
          </div>
          <div class="filter-group">
            <label class="filter-label">Từ ngày</label>
            <input v-model="tempFilters.startDate" type="date" class="filter-input"
              @change="filterInvoicesLocal(false)" />
          </div>
          <div class="filter-group">
            <label class="filter-label">Đến ngày</label>
            <input v-model="tempFilters.endDate" type="date" class="filter-input"
              @change="filterInvoicesLocal(false)" />
          </div>
          <div class="filter-group">
            <label class="filter-label">Sắp xếp theo</label>
            <select v-model="tempFilters.sortBy" class="filter-select" @change="filterInvoicesLocal(false)">
              <option value="">Sắp xếp mặc định</option>
              <option value="newest">Mới nhất</option>
              <option value="most_expensive">Đắt nhất</option>
              <option value="cheapest">Rẻ nhất</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-lg">
      <div class="p-6">
        <!-- Status Filter Tabs -->
        <div class="flex flex-wrap gap-2 mb-6 p-1 bg-gray-100 dark:bg-gray-700 rounded-lg">
          <button v-for="tab in tabs" :key="tab.id" :class="[
            'px-4 py-2 rounded-md text-sm font-medium transition-all duration-200 flex items-center gap-2',
            activeTab === tab.id
              ? 'bg-white dark:bg-gray-600 text-blue-800 dark:text-blue-400 shadow-sm'
              : 'text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white hover:bg-white/50 dark:hover:bg-gray-600/50'
          ]" @click="switchTabLocal(tab.id)">
            <span>{{ tab.label }}</span>
            <span class="bg-blue-200 dark:bg-blue-500 text-xs px-2 py-0.5 rounded-full">{{ getTabCountLocal(tab.id)
            }}</span>
          </button>
        </div>

        <!-- Invoice List Section -->
        <div class="invoices-section">
          <div class="section-header">
            <h3 class="section-title">
              <Icon icon="solar:bill-list-bold-duotone" class="text-xl" />
              Danh Sách Hóa Đơn ({{ filteredInvoices.length }})
            </h3>
          </div>

          <!-- DataTable Component -->
          <DataTable 
            :data="filteredInvoices" 
            :columns="tableColumns" 
            item-label="hóa đơn"
            empty-message="Không có dữ liệu hóa đơn nào được tìm thấy." 
            key-field="id"
            :total-items="apiTotalElements"
            :current-page="currentPage + 1"
            :items-per-page="pageSize"
            @update:current-page="handlePageChange"
            @update:items-per-page="handlePageSizeChange"
            :server-side="true">
            <!-- Custom column templates -->
            <template #stt="{ rowIndex }">
              {{ rowIndex }}
            </template>

            <template #maHoaDon="{ item }">
              <span class="font-bold text-gray-800">{{ item.maHoaDon }}</span>
            </template>

            <template #nhanVien="{ item }">
              {{ item.nhanVien?.maNhanVien || 'N/A' }}
            </template>

            <template #khachHang="{ item }">
              {{ item.khachHang?.tenKhachHang || 'Khách lẻ' }}
            </template>

            <template #soDienThoai="{ item }">
              {{ item.khachHang?.soDienThoai || 'N/A' }}
            </template>

            <template #loaiDon="{ item }">
              <span v-if="item.loaiDon === 'Online'" class="order-type-badge online-order">
                <Icon icon="solar:global-bold-duotone" class="w-3 h-3 mr-1" />
                Online
              </span>
              <span v-else class="order-type-badge offline-order">
                <Icon icon="solar:shop-bold-duotone" class="w-3 h-3 mr-1" />
                Tại quầy
              </span>
            </template>

            <template #phiVanChuyen="{ item }">
              <span class="font-medium text-gray-800">{{ formatCurrency(item.phiVanChuyen) }}</span>
            </template>

            <template #ngayTao="{ item }">
              {{ formatDate(item.ngayTao) }}
            </template>

            <template #tongTien="{ item }">
              <span class="font-bold text-gray-800">{{ formatCurrency(item.tongTienThanhToan)
              }}</span>
            </template>

            <template #trangThai="{ item }">
              <span :class="getStatusColor(item.trangThai?.id)">
                {{ getStatusText(item.trangThai?.id) }}
              </span>
            </template>

            <template #actions="{ item }">
              <div class="flex justify-center gap-2">
                <router-link :to="{ name: 'HoaDonChiTiet', params: { id: item.id } }" class="action-btn view"
                  title="Xem chi tiết">
                  <Icon icon="solar:eye-bold" />
                </router-link>
                <button @click="printInvoicePDF(item)" class="action-btn print" title="In hóa đơn PDF">
                  <Icon icon="solar:printer-bold" />
                </button>
                <button @click="downloadQRCode(item)" class="action-btn qr" title="Tải mã QR hóa đơn">
                  <Icon icon="solar:qr-code-bold" />
                </button>
              </div>
            </template>
          </DataTable>
        </div>
      </div>
    </div>

    <!-- Hóa đơn không sửa / xóa trực tiếp ở danh sách: mọi thao tác trạng thái nằm ở màn chi tiết. -->
    <QRScannerModal 
      :is-visible="showQRScannerModal" 
      @close="closeQRScannerModal"
      @invoice-found="handleInvoiceFound"
    />
  </div>
</template>

<style scoped>
@import 'vue-toastification/dist/index.css';
@import url('https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700;900&display=block');

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

.card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
}

.card-body {
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(145deg, #f0f4f7, #ffffff);
}

.dark .card-body {
  background: linear-gradient(145deg, #2a3442, #1f2937);
}

.table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

.table th,
.table td {
  padding: 18px 16px;
  text-align: left;
}

.table thead th {
  background: #e0f2f7;
  color: #212121;
  font-weight: 800;
  font-size: 0.875rem;
  letter-spacing: 0.08em;
}

.dark .table thead th {
  background: #374151;
  color: #e5e7eb;
}

.table tbody tr {
  border-bottom: 1px dashed #e5e7eb;
  transition: background 0.3s ease;
}

.dark .table tbody tr {
  border-bottom: 1px dashed #4b5563;
}

.table tbody tr:last-child {
  border-bottom: none;
}

.table tbody tr:hover {
  background: rgba(59, 130, 246, 0.07);
}

.dark .table tbody tr:hover {
  background: #374151;
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

.no-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 250px;
  color: #6b7280;
  font-size: 1.4rem;
  font-weight: 500;
}

.dark .no-data {
  color: #9ca3af;
}

.pagination button {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  font-weight: 600;
  transition: background 0.3s ease, transform 0.2s ease, box-shadow 0.3s ease;
  min-width: 40px;
  text-align: center;
}

.dark .pagination button {
  background: #374151;
  color: #d1d5db;
  border: 1px solid #4b5563;
}

.pagination button:hover:not(:disabled) {
  background: rgba(59, 130, 246, 0.12);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.dark .pagination button:hover:not(:disabled) {
  background: #4b5563;
}

.pagination button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.pagination button.bg-blue-600 {
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
}

.tab {
  transition: background 0.3s ease, transform 0.2s ease, box-shadow 0.3s ease;
}

.tab:hover:not(.bg-gradient-to-r) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.tab.bg-gradient-to-r {
  box-shadow: 0 5px 18px rgba(59, 130, 246, 0.35);
}

@media (max-width: 1024px) {
  .filter-group {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .p-6 {
    padding: 1.25rem;
  }

  .md\:p-8 {
    padding: 1.5rem;
  }

  .table th,
  .table td {
    padding: 14px;
  }
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

.action-btn.view {
  background: #dbeafe;
  color: #1d4ed8;
}

.action-btn.print {
  background: #dcfce7;
  color: #166534;
}

.action-btn.qr {
  background: #fee2e2;
  color: #dc2626;
}

.action-btn:hover {
  transform: scale(1.1);
}

.action-btn:focus {
  outline: 2px solid #ff3b18;
  outline-offset: 2px;
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
