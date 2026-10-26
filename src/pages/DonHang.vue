<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { customerAPI } from '@/api/customerAPI'
import authService from '@/services/authService.js'

const route = useRoute()

const loading = ref(false)
const error = ref('')
const orders = ref([])
const selectedStatus = ref(null)
const searchCode = ref('')

const isAuthenticated = computed(() => authService.isAuthenticated())

// Trạng thái đơn khớp với backend (HoaDonStatus).
// Luồng hiện hành: Chờ xác nhận -> Đã xác nhận -> Đang vận chuyển -> Đã hoàn thành.
// Mã 3 ("Chờ vận chuyển") đã bị bỏ khỏi luồng nên không còn là lựa chọn lọc; đơn cũ
// đang ở mã 3 vẫn xem được ở mục "Tất cả".
const orderStatuses = [
  { id: null, name: 'Tất cả' },
  { id: 1, name: 'Chờ xác nhận' },
  { id: 2, name: 'Đã xác nhận' },
  { id: 4, name: 'Đang vận chuyển' },
  { id: 5, name: 'Đã hoàn thành' },
  { id: 6, name: 'Đã hủy' }
]

const STATUS_TONE = {
  0: 'wait',
  1: 'wait',
  2: 'work',
  3: 'work',
  4: 'ship',
  5: 'done',
  6: 'stop'
}

// Ảnh dự phòng khi ảnh áo bị lỗi
const FALLBACK_IMAGE =
  'data:image/svg+xml;charset=UTF-8,' +
  encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 120 120">
      <rect width="120" height="120" fill="#eef1f5"/>
      <path d="M45 32 L34 38 L38 51 L44 49 L44 88 L76 88 L76 49 L82 51 L86 38 L75 32 L66 36 A10 7 0 0 1 54 36 Z"
            fill="none" stroke="#ff3b18" stroke-width="3" stroke-linejoin="round"/>
    </svg>
  `)

const onImageError = (event) => {
  if (event.target.dataset.fallbackApplied) return
  event.target.dataset.fallbackApplied = 'true'
  event.target.src = FALLBACK_IMAGE
}

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount || 0)
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  try {
    return new Date(dateString).toLocaleDateString('vi-VN')
  } catch (err) {
    return 'N/A'
  }
}

const getStatusText = (statusId) => {
  const status = orderStatuses.find(s => s.id === statusId)
  if (status) return status.name
  if (statusId === 0) return 'Hóa đơn chờ'
  return 'Không xác định'
}

const getStatusClass = (statusId) => `st-status--${STATUS_TONE[statusId] || 'wait'}`

// Đếm số đơn theo từng trạng thái để hiện trên nút lọc
const countByStatus = (statusId) => {
  if (statusId === null) return orders.value.length
  return orders.value.filter(order => order.trangThai === statusId).length
}

const filteredOrders = computed(() => {
  let list = [...orders.value]

  if (selectedStatus.value !== null) {
    list = list.filter(order => order.trangThai === selectedStatus.value)
  }

  const keyword = searchCode.value.trim().toLowerCase()
  if (keyword) {
    list = list.filter(order => (order.ma || '').toLowerCase().includes(keyword))
  }

  return list
})

const filterByStatus = (statusId) => {
  selectedStatus.value = statusId
}

const clearFilters = () => {
  selectedStatus.value = null
  searchCode.value = ''
}

const loadOrders = async () => {
  if (!isAuthenticated.value) {
    orders.value = []
    return
  }

  try {
    loading.value = true
    error.value = ''

    const response = await customerAPI.getOrderHistory(0, 50)

    let list = []
    if (response?.data?.data?.content) {
      list = response.data.data.content
    } else if (response?.data?.content) {
      list = response.data.content
    } else if (Array.isArray(response?.data)) {
      list = response.data
    }

    // Bỏ hóa đơn chờ, mới nhất lên đầu
    orders.value = list
      .filter(order => order.trangThai !== 0)
      .sort((a, b) => {
        const dateA = new Date(a.ngayTao)
        const dateB = new Date(b.ngayTao)
        if (dateB.getTime() === dateA.getTime()) {
          return (b.id || 0) - (a.id || 0)
        }
        return dateB - dateA
      })
  } catch (err) {
    console.error('Error loading orders:', err)
    error.value = 'Không tải được danh sách đơn hàng. Bấm Tải lại để thử lại.'
    orders.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // Checkout chuyển sang đây kèm mã đơn sau khi đặt hàng
  const orderCode = route.query.orderCode
  if (orderCode) {
    searchCode.value = String(orderCode)
  }

  loadOrders()
})
</script>

<template>
  <div class="st-page">
    <!-- Header -->
    <header class="st-head">
      <span class="st-head__jersey" aria-hidden="true">02</span>
      <div class="st-container st-head__inner">
        <div>
          <div class="st-eyebrow">
            <span class="st-eyebrow__bar"></span>
            <span>Tài khoản STRIKER</span>
          </div>
          <h1 class="st-display">ĐƠN HÀNG</h1>
          <p class="st-lead">Theo dõi trạng thái các đơn áo bạn đã đặt.</p>
        </div>

        <button @click="loadOrders" :disabled="loading" class="st-refresh">
          {{ loading ? 'Đang tải' : 'Tải lại' }}
        </button>
      </div>
    </header>

    <div class="st-container st-body">
      <!-- Chưa đăng nhập -->
      <div v-if="!isAuthenticated" class="st-empty">
        <p class="st-empty__title">Đăng nhập để xem đơn hàng</p>
        <p class="st-empty__hint">Danh sách đơn gắn với tài khoản của bạn. Đăng nhập để theo dõi trạng thái giao hàng.</p>
        <div class="st-empty__actions">
          <router-link to="/login" class="st-btn st-btn--primary">Đăng nhập</router-link>
          <router-link to="/products" class="st-btn">Xem áo thể thao</router-link>
        </div>
      </div>

      <template v-else>
        <!-- Thanh tìm và lọc -->
        <div class="st-toolbar">
          <div class="st-field">
            <svg class="st-field__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>
            <input v-model="searchCode" class="st-input" type="text" placeholder="Tìm theo mã đơn hàng..." />
          </div>

          <div class="st-tabs">
            <button v-for="status in orderStatuses" :key="String(status.id)" @click="filterByStatus(status.id)"
              :class="['st-tab', { 'is-active': selectedStatus === status.id }]">
              {{ status.name }}
              <span class="st-tab__count">{{ countByStatus(status.id) }}</span>
            </button>
          </div>
        </div>

        <!-- Lỗi -->
        <div v-if="error" class="st-alert" role="alert">{{ error }}</div>

        <!-- Đang tải -->
        <div v-if="loading" class="st-loading">
          <span class="st-spinner" aria-hidden="true"></span>
          <p>Đang tải đơn hàng...</p>
        </div>

        <!-- Chưa có đơn nào -->
        <div v-else-if="orders.length === 0" class="st-empty">
          <p class="st-empty__title">Chưa có đơn hàng nào</p>
          <p class="st-empty__hint">Đơn đầu tiên của bạn sẽ hiện ở đây.</p>
          <router-link to="/products" class="st-btn st-btn--primary">Xem áo thể thao</router-link>
        </div>

        <!-- Lọc không ra kết quả -->
        <div v-else-if="filteredOrders.length === 0" class="st-empty">
          <p class="st-empty__title">Không có đơn nào khớp</p>
          <p class="st-empty__hint">Thử bỏ bớt điều kiện lọc hoặc kiểm tra lại mã đơn.</p>
          <button @click="clearFilters" class="st-btn st-btn--primary">Xóa bộ lọc</button>
        </div>

        <!-- Danh sách đơn -->
        <div v-else class="st-orders">
          <article v-for="order in filteredOrders" :key="order.id" class="st-order">
            <div class="st-order__top">
              <div>
                <p class="st-order__code">{{ order.ma || `#${order.id}` }}</p>
                <p class="st-order__date">Đặt ngày {{ formatDate(order.ngayTao) }}</p>
              </div>
              <span :class="['st-status', getStatusClass(order.trangThai)]">
                {{ getStatusText(order.trangThai) }}
              </span>
            </div>

            <!-- Sản phẩm trong đơn -->
            <ul v-if="order.items && order.items.length" class="st-items">
              <li v-for="item in order.items" :key="item.id" class="st-item">
                <img :src="item.anhSanPham || FALLBACK_IMAGE" :alt="item.tenSanPham" @error="onImageError" />
                <div class="st-item__info">
                  <p class="st-item__name">{{ item.tenSanPham }}</p>
                  <p class="st-item__variant">
                    <span v-if="item.kichCo">Size {{ item.kichCo }}</span>
                    <span v-if="item.kichCo && item.mauSac"> · </span>
                    <span v-if="item.mauSac">{{ item.mauSac }}</span>
                  </p>
                  <p class="st-item__qty">Số lượng: {{ item.soLuong }}</p>
                </div>
                <p class="st-item__price">{{ formatCurrency(item.thanhTien || item.giaBan) }}</p>
              </li>
            </ul>

            <div class="st-order__foot">
              <p class="st-order__addr">{{ order.diaChiKhachHang }}</p>
              <p class="st-order__total">
                <span>Tổng cộng</span>
                <strong>{{ formatCurrency(order.tongTienSauGiam || order.tongTien) }}</strong>
              </p>
            </div>
          </article>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
/* Font Nunito đã được tải chung ở index.html, không cần @import font riêng ở đây nữa. */

.st-page {
  --ink: #10141a;
  --strike: #ff3b18;
  --cobalt: #1b3bff;
  --chalk: #ffffff;
  --mist: #eef1f5;
  --line: #dde3ea;
  --steel: #8c98a8;
  --slate: #4a5563;
  --done: #0f7a3d;
  --work: #b45309;
  --ship: #1b3bff;
  min-height: 100vh;
  background: var(--mist);
}

.st-container {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 1.25rem;
}

/* ===== Header ===== */
.st-head {
  position: relative;
  overflow: hidden;
  background: var(--ink);
  background-image: repeating-linear-gradient(115deg, rgba(255, 255, 255, 0.022) 0 2px, transparent 2px 74px);
  padding: 3rem 0 2.5rem;
}

.st-head__jersey {
  position: absolute;
  top: 50%;
  right: 3rem;
  transform: translateY(-50%);
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: clamp(7rem, 16vw, 13rem);
  line-height: 0.8;
  color: transparent;
  -webkit-text-stroke: 2px rgba(255, 255, 255, 0.08);
  pointer-events: none;
  user-select: none;
}

.st-head__inner {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1.25rem;
}

.st-eyebrow {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.8rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.85rem;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: var(--strike);
}

.st-eyebrow__bar {
  width: 2.5rem;
  height: 3px;
  background: var(--strike);
}

.st-display {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: clamp(2.25rem, 5vw, 3.5rem);
  line-height: 0.95;
  text-transform: uppercase;
  color: var(--chalk);
  margin: 0 0 0.6rem;
}

.st-lead {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 1rem;
  color: var(--steel);
  margin: 0;
}

.st-refresh {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.9rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--chalk);
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 2px;
  padding: 0.6rem 1.2rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.st-refresh:hover:not(:disabled) {
  background: var(--strike);
  border-color: var(--strike);
}

.st-refresh:disabled { opacity: 0.5; cursor: not-allowed; }

/* ===== Body ===== */
.st-body { padding: 2rem 1.25rem 4rem; }

/* Thanh công cụ */
.st-toolbar { margin-bottom: 1.75rem; }

.st-field {
  position: relative;
  display: flex;
  align-items: center;
  max-width: 22rem;
  margin-bottom: 1rem;
}

.st-field__icon {
  position: absolute;
  left: 0.8rem;
  width: 1.05rem;
  height: 1.05rem;
  color: var(--steel);
  pointer-events: none;
}

.st-input {
  width: 100%;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.92rem;
  color: var(--ink);
  background: var(--chalk);
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.7rem 0.8rem 0.7rem 2.5rem;
}

.st-input:focus {
  outline: none;
  border-color: var(--strike);
}

.st-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
}

.st-tab {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.88rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--slate);
  background: var(--chalk);
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.5rem 0.8rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.st-tab:hover { border-color: var(--ink); color: var(--ink); }

.st-tab.is-active {
  background: var(--ink);
  border-color: var(--ink);
  color: var(--chalk);
}

.st-tab__count {
  font-size: 0.78rem;
  color: var(--steel);
}

.st-tab.is-active .st-tab__count { color: var(--strike); }

/* Thông báo lỗi */
.st-alert {
  background: #fff1ee;
  border-left: 4px solid var(--strike);
  padding: 0.9rem 1.1rem;
  margin-bottom: 1.25rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.92rem;
  color: #9a2410;
}

/* Đang tải */
.st-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 4.5rem 1rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  color: var(--slate);
}

.st-spinner {
  width: 2.5rem;
  height: 2.5rem;
  border: 3px solid var(--line);
  border-top-color: var(--strike);
  border-radius: 50%;
  animation: st-spin 0.8s linear infinite;
}

@keyframes st-spin { to { transform: rotate(360deg); } }

.st-loading p { margin: 0; }

/* ===== Danh sách đơn ===== */
.st-orders {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.st-order {
  background: var(--chalk);
  border: 1px solid var(--line);
  border-left: 4px solid var(--ink);
  padding: 1.25rem;
}

.st-order__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  padding-bottom: 0.9rem;
  margin-bottom: 0.9rem;
  border-bottom: 1px solid var(--line);
}

.st-order__code {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.3rem;
  letter-spacing: 0.04em;
  color: var(--ink);
  margin: 0 0 0.15rem;
}

.st-order__date {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.85rem;
  color: var(--steel);
  margin: 0;
}

/* Nhãn trạng thái */
.st-status {
  display: inline-block;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.75rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  padding: 0.28rem 0.65rem;
  white-space: nowrap;
  color: var(--chalk);
  background: var(--steel);
}

.st-status--done { background: var(--done); }
.st-status--ship { background: var(--ship); }
.st-status--work { background: var(--work); }
.st-status--stop { background: var(--strike); }
.st-status--wait { background: var(--steel); }

/* Sản phẩm trong đơn */
.st-items {
  list-style: none;
  margin: 0 0 0.9rem;
  padding: 0;
}

.st-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.6rem 0;
}

.st-item + .st-item { border-top: 1px dashed var(--line); }

.st-item img {
  width: 3.5rem;
  height: 3.5rem;
  object-fit: cover;
  border: 1px solid var(--line);
  flex-shrink: 0;
}

.st-item__info { flex: 1; min-width: 0; }

.st-item__name {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 0.95rem;
  color: var(--ink);
  margin: 0 0 0.15rem;
}

.st-item__variant {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 0.8rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--steel);
  margin: 0 0 0.1rem;
}

.st-item__qty {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.82rem;
  color: var(--slate);
  margin: 0;
}

.st-item__price {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.05rem;
  color: var(--ink);
  margin: 0;
  white-space: nowrap;
}

.st-order__foot {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  padding-top: 0.9rem;
  border-top: 2px solid var(--ink);
}

.st-order__addr {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.85rem;
  color: var(--slate);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.st-order__total {
  display: flex;
  align-items: baseline;
  gap: 0.6rem;
  margin: 0;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.85rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--slate);
  white-space: nowrap;
}

.st-order__total strong {
  font-weight: 800;
  font-size: 1.4rem;
  letter-spacing: 0;
  color: var(--strike);
}

/* ===== Trạng thái rỗng ===== */
.st-empty {
  text-align: center;
  background: var(--chalk);
  border: 1px dashed var(--line);
  padding: 4rem 1.5rem;
}

.st-empty__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.6rem;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 0.4rem;
}

.st-empty__hint {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  color: var(--slate);
  margin: 0 0 1.5rem;
  max-width: 26rem;
  margin-left: auto;
  margin-right: auto;
}

.st-empty__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.5rem;
}

/* ===== Nút ===== */
.st-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.95rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.65rem 1.4rem;
  cursor: pointer;
  text-decoration: none;
  background: var(--chalk);
  color: var(--ink);
  transition: all 0.2s ease;
}

.st-btn:hover { border-color: var(--ink); }

.st-btn--primary {
  background: var(--ink);
  border-color: var(--ink);
  color: var(--chalk);
}

.st-btn--primary:hover {
  background: var(--strike);
  border-color: var(--strike);
}

/* ===== Focus bàn phím ===== */
.st-btn:focus-visible,
.st-tab:focus-visible,
.st-refresh:focus-visible,
.st-input:focus-visible {
  outline: 3px solid var(--cobalt);
  outline-offset: 2px;
}

/* ===== Responsive ===== */
@media (max-width: 640px) {
  .st-head__jersey { opacity: 0.5; }
  .st-order__top { flex-direction: column; }
  .st-order__foot { flex-direction: column; align-items: flex-start; gap: 0.5rem; }
  .st-item { flex-wrap: wrap; }
}

/* ===== Giảm chuyển động ===== */
@media (prefers-reduced-motion: reduce) {
  .st-btn, .st-tab, .st-refresh { transition: none; }
  .st-spinner { animation: none; }
}
</style>