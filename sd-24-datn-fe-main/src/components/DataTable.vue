<template>
  <div class="data-table-container">
    <!-- Table View -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th v-for="column in columns" :key="column.key" :class="column.class">
              <slot 
                :name="`header-${column.key}`"
                :column="column"
              >
                {{ column.label }}
              </slot>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in paginatedData" :key="getItemKey(item, index)">
            <td v-for="column in columns" :key="column.key" :class="column.class">
              <slot 
                :name="column.key" 
                :item="item" 
                :index="index"
                :rowIndex="(currentPage - 1) * itemsPerPage + index + 1"
              >
                {{ getColumnValue(item, column.key) }}
              </slot>
            </td>
          </tr>
          <tr v-if="filteredData.length === 0">
            <td :colspan="columns.length" class="empty-message">
              {{ emptyMessage }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="filteredData.length > 0 && !disablePagination" class="pagination-container">
      <div class="pagination-info-left">
        <span class="pagination-summary">
          Hiển thị {{ startItem }} - {{ endItem }} của {{ serverSide ? totalItems : filteredData.length }} {{ itemLabel }}
        </span>
        <div class="page-size-selector">
          <label>Hiển thị:</label>
          <!-- Danh sách cỡ trang cho phép tùy biến: bảng biến thể sản phẩm thường chỉ có
               3-7 dòng, nếu ép cỡ trang nhỏ nhất là 10 thì không bao giờ có trang thứ hai
               và thanh phân trang trở nên vô dụng. -->
          <select v-model="itemsPerPage" @change="handlePageSizeChange" class="page-size-select">
            <option v-for="option in pageSizeOptions" :key="option" :value="option">{{ option }}</option>
          </select>
          <span>{{ itemLabel }}/trang</span>
        </div>
      </div>
      
      <div v-if="totalPages > 1" class="pagination">
        <button 
          class="pagination-btn" 
          @click="handlePageClick(1)"
          :disabled="currentPage === 1"
          title="Trang đầu"
        >
          <iconify-icon icon="solar:double-alt-arrow-left-bold"></iconify-icon>
        </button>
        <button 
          class="pagination-btn" 
          @click="handlePageClick(currentPage - 1)"
          :disabled="currentPage === 1"
          title="Trang trước"
        >
          <iconify-icon icon="solar:alt-arrow-left-bold"></iconify-icon>
        </button>
        
        <!-- Page Numbers -->
        <div class="page-numbers">
          <button
            v-for="page in visiblePages"
            :key="page"
            class="page-number-btn"
            :class="{ active: page === currentPage, ellipsis: page === '...' }"
            @click="page !== '...' && handlePageClick(page)"
            :disabled="page === '...'"
          >
            {{ page }}
          </button>
        </div>
        
        <button 
          class="pagination-btn" 
          @click="handlePageClick(currentPage + 1)"
          :disabled="currentPage === totalPages"
          title="Trang sau"
        >
          <iconify-icon icon="solar:alt-arrow-right-bold"></iconify-icon>
        </button>
        <button 
          class="pagination-btn" 
          @click="handlePageClick(totalPages)"
          :disabled="currentPage === totalPages"
          title="Trang cuối"
        >
          <iconify-icon icon="solar:double-alt-arrow-right-bold"></iconify-icon>
        </button>
      </div>
      
      <div class="pagination-info-right">
        <span class="pagination-current">
          Trang {{ currentPage }} / {{ totalPages }}
        </span>
        <div class="goto-page">
          <label>Đi tới:</label>
          <input 
            type="number" 
            v-model.number="gotoPage" 
            @keyup.enter="goToPage"
            :min="1" 
            :max="totalPages"
            class="goto-input"
            placeholder="Trang"
          />
          <button @click="goToPage" class="goto-btn" :disabled="!isValidGotoPage">
            <iconify-icon icon="solar:arrow-right-bold"></iconify-icon>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue';

export default {
  name: 'DataTable',
  props: {
    data: {
      type: Array,
      required: true
    },
    columns: {
      type: Array,
      required: true
    },
    itemLabel: {
      type: String,
      default: 'mục'
    },
    emptyMessage: {
      type: String,
      default: 'Không tìm thấy dữ liệu nào.'
    },
    keyField: {
      type: String,
      default: 'id'
    },
    disablePagination: {
      type: Boolean,
      default: false
    },
    serverSide: {
      type: Boolean,
      default: false
    },
    totalItems: {
      type: Number,
      default: 0
    },
    currentPage: {
      type: Number,
      default: 1
    },
    itemsPerPage: {
      type: Number,
      default: 10
    },
    /** Các cỡ trang cho người dùng chọn. Bảng ít dòng nên truyền thêm cỡ nhỏ (vd 5). */
    pageSizeOptions: {
      type: Array,
      default: () => [10, 20, 50]
    }
  },
  emits: ['update:currentPage', 'update:itemsPerPage'],
  setup(props, { emit }) {
    // Pagination state
    const currentPage = ref(props.currentPage || 1);
    const itemsPerPage = ref(props.itemsPerPage || 10);
    const gotoPage = ref('');

    // Computed properties
    const filteredData = computed(() => props.data);

    const paginatedData = computed(() => {
      if (props.disablePagination || props.serverSide) {
        // For server-side pagination, data is already paginated
        return filteredData.value;
      }
      // Client-side pagination
      const start = (currentPage.value - 1) * itemsPerPage.value;
      const end = start + itemsPerPage.value;
      return filteredData.value.slice(start, end);
    });

    const totalPages = computed(() => {
      if (props.serverSide) {
        // For server-side, calculate from totalItems prop
        return Math.ceil(props.totalItems / itemsPerPage.value);
      }
      // Client-side calculation
      return Math.ceil(filteredData.value.length / itemsPerPage.value);
    });

    const startItem = computed(() => {
      return (currentPage.value - 1) * itemsPerPage.value + 1;
    });

    const endItem = computed(() => {
      if (props.serverSide) {
        const end = currentPage.value * itemsPerPage.value;
        return end > props.totalItems ? props.totalItems : end;
      }
      const end = currentPage.value * itemsPerPage.value;
      return end > filteredData.value.length ? filteredData.value.length : end;
    });

    const visiblePages = computed(() => {
      const pages = [];
      const total = totalPages.value;
      const current = currentPage.value;
      
      if (total <= 7) {
        for (let i = 1; i <= total; i++) {
          pages.push(i);
        }
      } else {
        if (current <= 4) {
          for (let i = 1; i <= 5; i++) {
            pages.push(i);
          }
          pages.push('...');
          pages.push(total);
        } else if (current >= total - 3) {
          pages.push(1);
          pages.push('...');
          for (let i = total - 4; i <= total; i++) {
            pages.push(i);
          }
        } else {
          pages.push(1);
          pages.push('...');
          for (let i = current - 1; i <= current + 1; i++) {
            pages.push(i);
          }
          pages.push('...');
          pages.push(total);
        }
      }
      
      return pages;
    });

    const isValidGotoPage = computed(() => {
      return gotoPage.value >= 1 && gotoPage.value <= totalPages.value;
    });

    /**
     * Cha đang tự lo việc phân trang khi bật `serverSide` (gọi API theo trang) HOẶC
     * `disablePagination` (cha đã tự cắt mảng và tự vẽ thanh phân trang riêng).
     * Trong cả hai trường hợp, state trang bên trong component chỉ đóng vai trò phản chiếu
     * lại state của cha - dùng để tính số thứ tự dòng (STT) cho đúng.
     */
    const parentControlsPagination = computed(() => props.serverSide || props.disablePagination);

    // Watch for prop changes (khi cha điều khiển phân trang)
    // Trước đây chỉ đồng bộ khi serverSide, nên màn nào tự cắt mảng rồi truyền
    // disablePagination sẽ bị kẹt currentPage = 1 -> cột STT lặp lại 1..10 ở mọi trang.
    watch(() => props.currentPage, (newVal) => {
      if (parentControlsPagination.value && newVal) {
        currentPage.value = newVal;
      }
    }, { immediate: true });

    watch(() => props.itemsPerPage, (newVal) => {
      if (parentControlsPagination.value && newVal) {
        itemsPerPage.value = newVal;
      }
    }, { immediate: true });

    // Watch for data changes and reset to first page (chỉ khi component tự phân trang)
    watch(() => props.data, () => {
      if (!parentControlsPagination.value) {
        currentPage.value = 1;
      }
    });
    
    // Watch internal state changes and emit to parent.
    // Khi cha điều khiển phân trang, state bên trong chỉ là bản sao của cha - emit ngược lại
    // sẽ tạo vòng lặp cha <-> con, nên im lặng.
    watch(currentPage, (newVal) => {
      if (!parentControlsPagination.value) {
        emit('update:currentPage', newVal);
      }
    });

    watch(itemsPerPage, (newVal) => {
      if (!parentControlsPagination.value) {
        emit('update:itemsPerPage', newVal);
        currentPage.value = 1; // Reset to first page when items per page changes (client-side only)
      }
    });

    // Methods
    const getItemKey = (item, index) => {
      return item[props.keyField] || index;
    };

    const getColumnValue = (item, key) => {
      return item[key] || '';
    };

    const goToPage = () => {
      if (isValidGotoPage.value) {
        if (props.serverSide) {
          emit('update:currentPage', gotoPage.value);
        } else {
          currentPage.value = gotoPage.value;
        }
        gotoPage.value = '';
      }
    };
    
    // Override pagination methods for server-side
    const handlePageClick = (page) => {
      if (props.serverSide) {
        emit('update:currentPage', page);
      } else {
        currentPage.value = page;
      }
    };
    
    const handlePageSizeChange = () => {
      if (props.serverSide) {
        emit('update:itemsPerPage', itemsPerPage.value);
      } else {
        currentPage.value = 1;
      }
    };

    return {
      currentPage,
      itemsPerPage,
      gotoPage,
      filteredData,
      paginatedData,
      totalPages,
      startItem,
      endItem,
      visiblePages,
      isValidGotoPage,
      getItemKey,
      getColumnValue,
      goToPage,
      handlePageClick,
      handlePageSizeChange
    };
  }
};
</script>

<style scoped>
/* ===== TABLE STYLES ===== */
.data-table-container {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.table-container {
  overflow-x: auto;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  background: #f8fafc;
  padding: 16px 12px;
  text-align: center;
  font-weight: 600;
  color: #64748b;
  font-size: 0.875rem;
  border-bottom: 1px solid #e2e8f0;
  white-space: nowrap;
}

.data-table th > * {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
}

.data-table td {
  padding: 16px 12px;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
  text-align: center;
}

.data-table td > * {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  margin: 0 auto;
}

/* Đảm bảo các button, badge, icon được căn giữa */
.data-table td .btn,
.data-table td .badge,
.data-table td .status-badge,
.data-table td .action-buttons,
.data-table td iconify-icon {
  margin: 0 auto;
}

.data-table tbody tr:hover {
  background: #f8fafc;
}

/* ===== PAGINATION STYLES ===== */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 32px;
  padding: 24px;
  background: #f8fafc;
  border-radius: 16px;
  gap: 16px;
  flex-wrap: wrap;
  min-height: 80px;
}

.pagination-info-left {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 160px;
  flex-shrink: 1;
}

.pagination-summary {
  font-size: 0.9rem;
  color: #64748b;
  font-weight: 500;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: #64748b;
}

.page-size-select {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
  background: white;
  cursor: pointer;
}

.page-size-select:focus {
  outline: none;
  border-color: #ff3b18;
  box-shadow: 0 0 0 2px rgba(255, 59, 24, 0.1);
}

.pagination {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 1;
  min-width: 0;
}

.pagination-btn {
  width: 40px;
  height: 40px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #64748b;
  font-size: 16px;
}

.pagination-btn:hover:not(:disabled) {
  border-color: #ff3b18;
  color: #ff3b18;
  background: #f0f8ff;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 0 8px;
}

.page-number-btn {
  min-width: 36px;
  height: 36px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #64748b;
  font-size: 0.9rem;
  font-weight: 500;
  padding: 0 8px;
}

.page-number-btn:hover:not(:disabled):not(.ellipsis) {
  border-color: #ff3b18;
  color: #ff3b18;
  background: #f0f8ff;
}

.page-number-btn.active {
  background: #ff3b18;
  border-color: #ff3b18;
  color: white;
}

.page-number-btn.ellipsis {
  border: none;
  background: transparent;
  cursor: default;
  color: #9ca3af;
}

.pagination-info-right {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-end;
  min-width: 160px;
  flex-shrink: 1;
}

.pagination-current {
  font-size: 0.9rem;
  color: #64748b;
  font-weight: 500;
}

.goto-page {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: #64748b;
}

.goto-input {
  width: 60px;
  padding: 6px 8px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
  text-align: center;
  background: white;
}

.goto-input:focus {
  outline: none;
  border-color: #ff3b18;
  box-shadow: 0 0 0 2px rgba(255, 59, 24, 0.1);
}

.goto-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #64748b;
}

.goto-btn:hover:not(:disabled) {
  border-color: #ff3b18;
  color: #ff3b18;
  background: #f0f8ff;
}

.goto-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ===== EMPTY STATE ===== */
.empty-message {
  text-align: center;
  color: #64748b;
  font-style: italic;
  padding: 40px 20px;
}

/* ===== RESPONSIVE DESIGN ===== */
/* When sidebar is open or container is narrow */
@media (max-width: 1200px) {
  .pagination-container {
    flex-wrap: wrap;
    gap: 12px;
    padding: 20px 16px;
  }
  
  .pagination-info-left,
  .pagination-info-right {
    min-width: 140px;
  }
  
  .pagination {
    gap: 4px;
  }
  
  .pagination-btn {
    width: 36px;
    height: 36px;
    font-size: 14px;
  }
  
  .page-number-btn {
    min-width: 32px;
    height: 32px;
    font-size: 0.85rem;
  }
}

/* Tablet and smaller screens */
@media (max-width: 768px) {
  .pagination-container {
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }
  
  .pagination-info-left,
  .pagination-info-right {
    min-width: auto;
    width: 100%;
    align-items: center;
    text-align: center;
  }
  
  .pagination {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .page-size-selector,
  .goto-page {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .data-table th,
  .data-table td {
    padding: 12px 8px;
    font-size: 0.85rem;
  }
}

/* Mobile phones */
@media (max-width: 480px) {
  .pagination-container {
    padding: 12px;
    gap: 12px;
  }
  
  .pagination-btn {
    width: 32px;
    height: 32px;
    font-size: 12px;
  }
  
  .page-number-btn {
    min-width: 28px;
    height: 28px;
    font-size: 0.75rem;
  }
  
  .page-numbers {
    gap: 2px;
  }
  
  /* Hide first/last buttons on very small screens */
  .pagination-btn:first-child,
  .pagination-btn:last-child {
    display: none;
  }
  
  .data-table th,
  .data-table td {
    padding: 8px 4px;
    font-size: 0.8rem;
  }
}
</style>
