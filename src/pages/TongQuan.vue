<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useToast } from 'vue-toastification'
import { customerAPI } from '@/api/customerAPI'
import authService from '@/services/authService.js'

const route = useRoute()
const toast = useToast()
const loading = ref(false)
const error = ref('')

// Overview statistics
const overviewStats = ref({
  totalOrders: 0,
  completedOrders: 0,
  processingOrders: 0,
  cancelledOrders: 0,
  totalSpent: 0,
  averageOrderValue: 0,
  customerName: '',
  customerCode: '',
  membershipLevel: 'Thành viên',
  loyaltyPoints: 0,
  lastOrderDate: null,
  lastOrderStatus: null,
  lastOrderAmount: 0
})

// Recent orders from order history
const recentOrders = ref([])

// Expanded order for inline details
const expandedOrder = ref(null)

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

// Load customer overview data
const loadCustomerOverview = async () => {
  try {
    loading.value = true
    error.value = ''

    if (!authService.isAuthenticated()) {
      error.value = 'Đăng nhập để xem thống kê tài khoản của bạn.'
      return
    }

    const response = await customerAPI.getOverview()

    if (response.success && response.data) {
      const data = response.data
      overviewStats.value = {
        totalOrders: data.totalOrders || 0,
        completedOrders: data.completedOrders || 0,
        processingOrders: data.processingOrders || 0,
        cancelledOrders: data.cancelledOrders || 0,
        totalSpent: data.totalSpent || 0,
        averageOrderValue: data.averageOrderValue || 0,
        customerName: data.customerName || '',
        customerCode: data.customerCode || '',
        membershipLevel: data.membershipLevel || 'Thành viên',
        loyaltyPoints: data.loyaltyPoints || 0,
        lastOrderDate: data.lastOrderDate || null,
        lastOrderStatus: data.lastOrderStatus || null,
        lastOrderAmount: data.lastOrderAmount || 0
      }
    } else {
      overviewStats.value = {
        totalOrders: 0,
        completedOrders: 0,
        processingOrders: 0,
        cancelledOrders: 0,
        totalSpent: 0,
        averageOrderValue: 0,
        customerName: 'Khách hàng',
        customerCode: 'N/A',
        membershipLevel: 'Thành viên',
        loyaltyPoints: 0,
        lastOrderDate: null,
        lastOrderStatus: null,
        lastOrderAmount: 0
      }
    }
  } catch (err) {
    error.value = 'Không tải được thống kê tài khoản. Bấm Làm mới để thử lại.'
    console.error('Error loading overview:', err)

    overviewStats.value = {
      totalOrders: 0,
      completedOrders: 0,
      processingOrders: 0,
      cancelledOrders: 0,
      totalSpent: 0,
      averageOrderValue: 0,
      customerName: 'Khách hàng',
      customerCode: 'N/A',
      membershipLevel: 'Thành viên',
      loyaltyPoints: 0,
      lastOrderDate: null,
      lastOrderStatus: null,
      lastOrderAmount: 0
    }
  } finally {
    loading.value = false
  }
}

// Load recent orders
const loadRecentOrders = async () => {
  try {
    if (!authService.isAuthenticated()) {
      recentOrders.value = []
      return
    }

    const response = await customerAPI.getOrderHistory(0, 10)

    if (response && response.success) {
      let orders = []

      if (response.data?.data?.content) {
        orders = response.data.data.content
      } else if (response.data?.content) {
        orders = response.data.content
      } else if (Array.isArray(response.data)) {
        orders = response.data
      }

      // Bỏ hóa đơn chờ (trạng thái 0)
      orders = orders.filter(order => order.trangThai !== 0)

      // Mới nhất lên đầu, nếu trùng ngày thì xếp theo id
      orders = orders.sort((a, b) => {
        const dateA = new Date(a.ngayTao)
        const dateB = new Date(b.ngayTao)

        if (dateB.getTime() === dateA.getTime()) {
          return (b.id || 0) - (a.id || 0)
        }

        return dateB - dateA
      })

      orders = orders.slice(0, 3)

      if (Array.isArray(orders) && orders.length > 0) {
        recentOrders.value = orders.map(order => {
          return {
            id: order.id,
            ma: order.ma,
            tenKhachHang: order.tenKhachHang,
            soDienThoaiKhachHang: order.soDienThoaiKhachHang,
            diaChiKhachHang: order.diaChiKhachHang,
            tongTienSauGiam: order.tongTienSauGiam,
            tongTien: order.tongTien,
            ngayTao: order.ngayTao,
            trangThai: order.trangThai,
            items: order.items || [],
            productName: `Đơn hàng ${order.ma}`,
            total: order.tongTienSauGiam || order.tongTien || 0,
            date: order.ngayTao ? new Date(order.ngayTao).toLocaleDateString('vi-VN') : 'N/A',
            status: getOrderStatus(order.trangThai),
            customerName: order.tenKhachHang || 'N/A',
            phone: order.soDienThoaiKhachHang || 'N/A',
            address: order.diaChiKhachHang || 'N/A'
          }
        })
      } else {
        recentOrders.value = []
      }
    } else {
      recentOrders.value = []
    }
  } catch (err) {
    console.error('Error loading recent orders:', err)
    recentOrders.value = []
  }
}

// Convert backend status to frontend status
const getOrderStatus = (backendStatus) => {
  switch (backendStatus) {
    case 5: return 'delivered'
    case 4: return 'shipped'
    case 1:
    case 2:
    case 3: return 'processing'
    case 6: return 'cancelled'
    default: return 'processing'
  }
}

// Toggle order details expansion
const toggleOrderDetails = (orderId) => {
  expandedOrder.value = expandedOrder.value === orderId ? null : orderId
}

// Refresh all data
const refreshData = async () => {
  await Promise.all([
    loadCustomerOverview(),
    loadRecentOrders()
  ])
}

// Format currency
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount || 0)
}

// Format date
const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  try {
    return new Date(dateString).toLocaleDateString('vi-VN')
  } catch (err) {
    return 'N/A'
  }
}

// Nhãn trạng thái theo mã số của backend
const STATUS_MAP = {
  0: { text: 'Hóa đơn chờ', tone: 'wait' },
  1: { text: 'Chờ xác nhận', tone: 'wait' },
  2: { text: 'Đã xác nhận', tone: 'work' },
  3: { text: 'Chờ vận chuyển', tone: 'work' },
  4: { text: 'Đang vận chuyển', tone: 'ship' },
  5: { text: 'Đã hoàn thành', tone: 'done' },
  6: { text: 'Đã hủy', tone: 'stop' }
}

const STATUS_TEXT_MAP = {
  delivered: { text: 'Đã giao', tone: 'done' },
  shipped: { text: 'Đang giao', tone: 'ship' },
  processing: { text: 'Đang xử lý', tone: 'work' },
  cancelled: { text: 'Đã hủy', tone: 'stop' }
}

const resolveStatus = (status) => {
  const numericStatus = typeof status === 'number' ? status : parseInt(status)
  if (!Number.isNaN(numericStatus) && STATUS_MAP[numericStatus]) {
    return STATUS_MAP[numericStatus]
  }
  return STATUS_TEXT_MAP[status] || { text: 'Không xác định', tone: 'wait' }
}

const getStatusClass = (status) => `st-status--${resolveStatus(status).tone}`
const getStatusText = (status) => resolveStatus(status).text

// Tiến độ đơn hàng
const PROGRESS_MAP = {
  delivered: { text: 'Hoàn thành', width: '100%', tone: 'done' },
  shipped: { text: 'Đang vận chuyển', width: '75%', tone: 'ship' },
  processing: { text: 'Đang chuẩn bị', width: '30%', tone: 'work' },
  cancelled: { text: 'Đã hủy', width: '100%', tone: 'stop' }
}

const getProgress = (status) => PROGRESS_MAP[status] || { text: 'Không xác định', width: '0%', tone: 'wait' }
const getProgressText = (status) => getProgress(status).text
const getProgressWidth = (status) => getProgress(status).width
const getProgressBarClass = (status) => `st-bar--${getProgress(status).tone}`

// Initialize data
onMounted(() => {
  const paymentMethod = route.query.payment
  const paymentStatus = route.query.status

  if (paymentMethod && paymentStatus === 'success') {
    setTimeout(() => {
      if (paymentMethod === 'vnpay') {
        toast.success('Thanh toán VNPay thành công!')
      } else if (paymentMethod === 'cod') {
        toast.success('Đặt hàng COD thành công!')
      }
    }, 500)

    setTimeout(() => {
      refreshData()
    }, 2000)
  }

  refreshData()
})
</script>

<template>
  <div class="st-page">
    <!-- Header -->
    <header class="st-head">
      <span class="st-head__jersey" aria-hidden="true">01</span>
      <div class="st-container st-head__inner">
        <div>
          <div class="st-eyebrow">
            <span class="st-eyebrow__bar"></span>
            <span>Tài khoản STRIKER</span>
          </div>
          <h1 class="st-display">TỔNG QUAN</h1>
          <p class="st-lead">Thống kê đơn hàng và hoạt động mua sắm của bạn.</p>
        </div>

        <button @click="refreshData" :disabled="loading" class="st-refresh">
          <svg :class="{ 'is-spinning': loading }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
          {{ loading ? 'Đang tải' : 'Làm mới' }}
        </button>
      </div>
    </header>

    <div class="st-container st-body">
      <!-- Error Message -->
      <div v-if="error" class="st-alert" role="alert">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
        <p>{{ error }}</p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="st-loading">
        <span class="st-spinner" aria-hidden="true"></span>
        <p>Đang tải dữ liệu...</p>
      </div>

      <template v-else>
        <!-- Quick Stats -->
        <section class="st-stats">
          <article class="st-stat">
            <span class="st-stat__num">{{ overviewStats.totalOrders || 0 }}</span>
            <span class="st-stat__label">Tổng đơn hàng</span>
          </article>
          <article class="st-stat st-stat--done">
            <span class="st-stat__num">{{ overviewStats.completedOrders || 0 }}</span>
            <span class="st-stat__label">Hoàn thành</span>
          </article>
          <article class="st-stat st-stat--work">
            <span class="st-stat__num">{{ overviewStats.processingOrders || 0 }}</span>
            <span class="st-stat__label">Đang xử lý</span>
          </article>
          <article class="st-stat st-stat--stop">
            <span class="st-stat__num">{{ overviewStats.cancelledOrders || 0 }}</span>
            <span class="st-stat__label">Đã hủy</span>
          </article>
        </section>

        <!-- Recent Orders -->
        <section class="st-section">
          <div class="st-section__head">
            <h2 class="st-section__title">Đơn hàng gần đây</h2>
            <router-link to="/account/purchase-history" class="st-link">
              Xem tất cả
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9 5l7 7-7 7"></path>
              </svg>
            </router-link>
          </div>

          <!-- Empty State -->
          <div v-if="recentOrders.length === 0" class="st-empty">
            <p class="st-empty__title">Chưa có đơn hàng nào</p>
            <p class="st-empty__hint">Đơn đầu tiên của bạn sẽ hiện ở đây. Ghé xem áo mới của STRIKER.</p>
            <router-link to="/products" class="st-btn st-btn--primary">Xem áo thể thao</router-link>
          </div>

          <!-- Orders List -->
          <div v-else class="st-orders">
            <article v-for="(order, index) in recentOrders" :key="order.id"
              :class="['st-order', { 'is-latest': index === 0 && recentOrders.length > 1, 'is-open': expandedOrder === order.id }]">
              <span v-if="index === 0 && recentOrders.length > 1" class="st-latest">Mới nhất</span>

              <div class="st-order__top">
                <div class="st-order__id">
                  <p class="st-order__code">{{ order.ma || order.id }}</p>
                  <p class="st-order__meta">
                    {{ order.date || formatDate(order.ngayTao) }}
                    <span v-if="order.items && order.items.length"> · {{ order.items.length }} áo</span>
                  </p>
                </div>
                <span :class="['st-status', getStatusClass(order.trangThai)]">
                  {{ getStatusText(order.trangThai) }}
                </span>
              </div>

              <!-- Product Preview -->
              <div v-if="order.items && order.items.length > 0" class="st-preview">
                <div v-for="item in order.items.slice(0, 3)" :key="item.id" class="st-preview__item">
                  <img :src="item.anhSanPham || FALLBACK_IMAGE" :alt="item.tenSanPham" @error="onImageError" />
                  <div>
                    <p class="st-preview__name">{{ item.tenSanPham }}</p>
                    <p class="st-preview__qty">x{{ item.soLuong }}</p>
                  </div>
                </div>
                <span v-if="order.items.length > 3" class="st-preview__more">
                  +{{ order.items.length - 3 }} áo khác
                </span>
              </div>

              <div class="st-order__foot">
                <p class="st-order__addr">{{ order.diaChiKhachHang || order.address }}</p>
                <p class="st-order__total">{{ formatCurrency(order.tongTienSauGiam || order.total) }}</p>
              </div>

              <!-- Order Progress -->
              <div class="st-progress">
                <div class="st-progress__head">
                  <span>Tiến độ</span>
                  <span>{{ getProgressText(order.status) }}</span>
                </div>
                <div class="st-progress__track">
                  <div :class="['st-progress__bar', getProgressBarClass(order.status)]"
                    :style="{ width: getProgressWidth(order.status) }"></div>
                </div>
              </div>

              <div class="st-order__actions">
                <button @click="toggleOrderDetails(order.id)" class="st-btn st-btn--ghost">
                  {{ expandedOrder === order.id ? 'Thu gọn' : 'Xem chi tiết' }}
                </button>

                <button v-if="order.status === 'delivered'" class="st-btn st-btn--primary">Mua lại</button>
                <button v-else-if="order.status === 'shipped'" class="st-btn st-btn--outline">Theo dõi đơn</button>
              </div>

              <!-- Expanded Details -->
              <div v-if="expandedOrder === order.id" class="st-details">
                <div class="st-details__cols">
                  <div>
                    <h3 class="st-details__title">Người nhận</h3>
                    <dl class="st-dl">
                      <div><dt>Tên</dt><dd>{{ order.tenKhachHang }}</dd></div>
                      <div><dt>Điện thoại</dt><dd>{{ order.soDienThoaiKhachHang }}</dd></div>
                      <div><dt>Địa chỉ</dt><dd>{{ order.diaChiKhachHang }}</dd></div>
                    </dl>
                  </div>
                  <div>
                    <h3 class="st-details__title">Đơn hàng</h3>
                    <dl class="st-dl">
                      <div><dt>Mã đơn</dt><dd>{{ order.ma }}</dd></div>
                      <div><dt>Ngày đặt</dt><dd>{{ formatDate(order.ngayTao) }}</dd></div>
                      <div>
                        <dt>Trạng thái</dt>
                        <dd>
                          <span :class="['st-status', getStatusClass(order.trangThai)]">
                            {{ getStatusText(order.trangThai) }}
                          </span>
                        </dd>
                      </div>
                    </dl>
                  </div>
                </div>

                <!-- Order Items -->
                <div v-if="order.items && order.items.length > 0" class="st-items">
                  <h3 class="st-details__title">Áo đã đặt</h3>
                  <div v-for="item in order.items" :key="item.id" class="st-item">
                    <img :src="item.anhSanPham || FALLBACK_IMAGE" :alt="item.tenSanPham" @error="onImageError" />
                    <div class="st-item__info">
                      <p class="st-item__name">{{ item.tenSanPham }}</p>
                      <p class="st-item__code">Mã: {{ item.maSanPham }}</p>
                      <div class="st-item__tags">
                        <span v-if="item.mauSac" class="st-tag">{{ item.mauSac }}</span>
                        <span v-if="item.kichCo" class="st-tag">Size {{ item.kichCo }}</span>
                        <span v-if="item.thuongHieu" class="st-tag">{{ item.thuongHieu }}</span>
                      </div>
                    </div>
                    <div class="st-item__price">
                      <p class="st-item__qty">SL: {{ item.soLuong }}</p>
                      <p class="st-item__unit">{{ formatCurrency(item.giaBan) }}</p>
                      <p class="st-item__sum">{{ formatCurrency(item.thanhTien) }}</p>
                    </div>
                  </div>
                </div>

                <div class="st-total">
                  <span>Tổng cộng</span>
                  <span class="st-total__num">{{ formatCurrency(order.tongTienSauGiam || order.tongTien) }}</span>
                </div>
              </div>
            </article>
          </div>
        </section>

        <!-- Quick Actions -->
        <section class="st-section">
          <div class="st-section__head">
            <h2 class="st-section__title">Thao tác nhanh</h2>
          </div>
          <div class="st-actions">
            <router-link to="/account/profile" class="st-action">
              <span class="st-action__title">Cập nhật thông tin</span>
              <span class="st-action__desc">Sửa tên, số điện thoại và địa chỉ nhận hàng</span>
            </router-link>

            <router-link to="/account/purchase-history" class="st-action">
              <span class="st-action__title">Xem đơn hàng</span>
              <span class="st-action__desc">Theo dõi toàn bộ đơn đã đặt</span>
            </router-link>

            <router-link to="/account/feedback" class="st-action">
              <span class="st-action__title">Góp ý phản hồi</span>
              <span class="st-action__desc">Cho STRIKER biết bạn nghĩ gì về sản phẩm</span>
            </router-link>
          </div>
        </section>
      </template>
    </div>
  </div>
</template>

<style scoped>
/* Font Nunito đã được tải chung ở index.html, không cần @import font riêng ở đây nữa. */

.st-page {
  --ink: #10141a;
  --ink-soft: #1b212b;
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
  max-width: 1180px;
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
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.9rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--chalk);
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 2px;
  padding: 0.6rem 1.1rem;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.st-refresh:hover:not(:disabled) {
  background: var(--strike);
  border-color: var(--strike);
}

.st-refresh:disabled { opacity: 0.5; cursor: not-allowed; }

.st-refresh svg { width: 1rem; height: 1rem; }

.is-spinning { animation: st-spin 1s linear infinite; }

@keyframes st-spin {
  to { transform: rotate(360deg); }
}

/* ===== Body ===== */
.st-body { padding: 2rem 1.25rem 4rem; }

.st-alert {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  background: #fff1ee;
  border-left: 4px solid var(--strike);
  padding: 0.9rem 1.1rem;
  margin-bottom: 1.5rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  color: #9a2410;
}

.st-alert svg { width: 1.25rem; height: 1.25rem; flex-shrink: 0; }
.st-alert p { margin: 0; font-size: 0.92rem; }

.st-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 5rem 1rem;
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

.st-loading p { margin: 0; }

/* ===== Thống kê ===== */
.st-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(11rem, 1fr));
  gap: 1rem;
  margin-bottom: 2.5rem;
}

.st-stat {
  background: var(--chalk);
  border: 1px solid var(--line);
  border-top: 4px solid var(--ink);
  padding: 1.25rem 1.1rem;
}

.st-stat--done { border-top-color: var(--done); }
.st-stat--work { border-top-color: var(--work); }
.st-stat--stop { border-top-color: var(--strike); }

.st-stat__num {
  display: block;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 2.6rem;
  line-height: 1;
  color: var(--ink);
  margin-bottom: 0.35rem;
}

.st-stat__label {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.82rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--steel);
}

/* ===== Section ===== */
.st-section { margin-bottom: 2.5rem; }

.st-section__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding-bottom: 0.8rem;
  margin-bottom: 1.25rem;
  border-bottom: 2px solid var(--ink);
}

.st-section__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.5rem;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0;
}

.st-link {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.9rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--strike);
  text-decoration: none;
}

.st-link:hover { text-decoration: underline; }
.st-link svg { width: 0.9rem; height: 0.9rem; }

/* ===== Đơn hàng ===== */
.st-orders {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.st-order {
  position: relative;
  background: var(--chalk);
  border: 1px solid var(--line);
  border-left: 4px solid var(--line);
  padding: 1.25rem;
  transition: border-color 0.2s ease;
}

.st-order.is-latest { border-left-color: var(--strike); }
.st-order.is-open { border-color: var(--ink); border-left-color: var(--strike); }

.st-latest {
  position: absolute;
  top: 0;
  right: 0;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.72rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--chalk);
  background: var(--strike);
  padding: 0.2rem 0.6rem;
}

.st-order__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
  padding-right: 4.5rem;
}

.st-order__code {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.3rem;
  letter-spacing: 0.04em;
  color: var(--ink);
  margin: 0 0 0.2rem;
}

.st-order__meta {
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
  padding: 0.25rem 0.6rem;
  white-space: nowrap;
  color: var(--chalk);
  background: var(--steel);
}

.st-status--done { background: var(--done); }
.st-status--ship { background: var(--ship); }
.st-status--work { background: var(--work); }
.st-status--stop { background: var(--strike); }
.st-status--wait { background: var(--steel); }

/* Xem trước sản phẩm */
.st-preview {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.st-preview__item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: var(--mist);
  border: 1px solid var(--line);
  padding: 0.35rem 0.6rem 0.35rem 0.35rem;
}

.st-preview__item img {
  width: 2.2rem;
  height: 2.2rem;
  object-fit: cover;
  border: 1px solid var(--line);
  flex-shrink: 0;
}

.st-preview__name {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--ink);
  margin: 0;
  max-width: 8rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.st-preview__qty {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.75rem;
  color: var(--steel);
  margin: 0;
}

.st-preview__more {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.8rem;
  color: var(--steel);
}

.st-order__foot {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  padding-top: 0.9rem;
  border-top: 1px solid var(--line);
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
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.4rem;
  color: var(--ink);
  margin: 0;
  white-space: nowrap;
}

/* Tiến độ */
.st-progress { margin-top: 1rem; }

.st-progress__head {
  display: flex;
  justify-content: space-between;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 0.78rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--steel);
  margin-bottom: 0.4rem;
}

.st-progress__track {
  height: 4px;
  background: var(--line);
  overflow: hidden;
}

.st-progress__bar {
  height: 100%;
  transition: width 0.4s ease;
}

.st-bar--done { background: var(--done); }
.st-bar--ship { background: var(--ship); }
.st-bar--work { background: var(--work); }
.st-bar--stop { background: var(--strike); }
.st-bar--wait { background: var(--steel); }

.st-order__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 1.1rem;
}

/* ===== Nút ===== */
.st-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.9rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.55rem 1.1rem;
  cursor: pointer;
  text-decoration: none;
  background: var(--chalk);
  color: var(--ink);
  transition: all 0.2s ease;
}

.st-btn--primary {
  background: var(--ink);
  border-color: var(--ink);
  color: var(--chalk);
}

.st-btn--primary:hover {
  background: var(--strike);
  border-color: var(--strike);
}

.st-btn--outline {
  background: transparent;
  border-color: var(--ink);
}

.st-btn--outline:hover {
  background: var(--ink);
  color: var(--chalk);
}

.st-btn--ghost {
  background: var(--mist);
  border-color: var(--mist);
  color: var(--slate);
}

.st-btn--ghost:hover {
  border-color: var(--ink);
  color: var(--ink);
}

/* ===== Chi tiết mở rộng ===== */
.st-details {
  margin-top: 1.25rem;
  padding-top: 1.25rem;
  border-top: 1px solid var(--line);
  animation: st-reveal 0.25s ease-out;
}

@keyframes st-reveal {
  from { opacity: 0; transform: translateY(-6px); }
  to { opacity: 1; transform: translateY(0); }
}

.st-details__cols {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(15rem, 1fr));
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.st-details__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.9rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 0.7rem;
}

.st-dl {
  margin: 0;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.88rem;
}

.st-dl > div {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.35rem 0;
  border-bottom: 1px dashed var(--line);
}

.st-dl dt { color: var(--steel); margin: 0; }
.st-dl dd { color: var(--ink); font-weight: 600; margin: 0; text-align: right; }

/* Danh sách áo */
.st-items { margin-bottom: 1.25rem; }

.st-item {
  display: flex;
  align-items: flex-start;
  gap: 0.8rem;
  background: var(--mist);
  border: 1px solid var(--line);
  padding: 0.7rem;
  margin-bottom: 0.5rem;
}

.st-item img {
  width: 3rem;
  height: 3rem;
  object-fit: cover;
  border: 1px solid var(--line);
  flex-shrink: 0;
}

.st-item__info { flex: 1; min-width: 0; }

.st-item__name {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 0.92rem;
  color: var(--ink);
  margin: 0 0 0.15rem;
}

.st-item__code {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.78rem;
  color: var(--steel);
  margin: 0 0 0.35rem;
}

.st-item__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.st-tag {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.72rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--ink);
  background: var(--chalk);
  border: 1px solid var(--line);
  padding: 0.12rem 0.4rem;
}

.st-item__price { text-align: right; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; }

.st-item__qty { font-size: 0.8rem; color: var(--steel); margin: 0; }
.st-item__unit { font-size: 0.82rem; color: var(--slate); margin: 0; }

.st-item__sum {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.05rem;
  color: var(--strike);
  margin: 0.1rem 0 0;
}

.st-total {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding-top: 0.9rem;
  border-top: 2px solid var(--ink);
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.95rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--slate);
}

.st-total__num {
  font-weight: 800;
  font-size: 1.6rem;
  letter-spacing: 0;
  color: var(--ink);
}

/* ===== Trạng thái rỗng ===== */
.st-empty {
  text-align: center;
  background: var(--chalk);
  border: 1px dashed var(--line);
  padding: 3.5rem 1.5rem;
}

.st-empty__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.5rem;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 0.4rem;
}

.st-empty__hint {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  color: var(--slate);
  margin: 0 0 1.4rem;
}

/* ===== Thao tác nhanh ===== */
.st-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(15rem, 1fr));
  gap: 1rem;
}

.st-action {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  background: var(--chalk);
  border: 1px solid var(--line);
  border-left: 4px solid var(--ink);
  padding: 1.1rem 1.2rem;
  text-decoration: none;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.st-action:hover {
  border-left-color: var(--strike);
  transform: translateX(3px);
}

.st-action__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.1rem;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--ink);
}

.st-action__desc {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.85rem;
  color: var(--slate);
}

/* ===== Focus bàn phím ===== */
.st-btn:focus-visible,
.st-refresh:focus-visible,
.st-link:focus-visible,
.st-action:focus-visible {
  outline: 3px solid var(--cobalt);
  outline-offset: 2px;
}

/* ===== Responsive ===== */
@media (max-width: 640px) {
  .st-head__jersey { opacity: 0.5; }
  .st-order__top { flex-direction: column; padding-right: 0; }
  .st-order__foot { flex-direction: column; align-items: flex-start; gap: 0.4rem; }
  .st-item { flex-wrap: wrap; }
  .st-item__price { text-align: left; }
}

/* ===== Giảm chuyển động ===== */
@media (prefers-reduced-motion: reduce) {
  .st-details { animation: none; }
  .st-progress__bar,
  .st-btn,
  .st-action,
  .st-order { transition: none; }
  .st-action:hover { transform: none; }
  .is-spinning,
  .st-spinner { animation: none; }
}
</style>