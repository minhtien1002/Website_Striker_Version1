<template>
  <div class="st-page">
    <!-- Header -->
    <header class="st-head">
      <span class="st-head__jersey" aria-hidden="true">03</span>
      <div class="st-container st-head__inner">
        <div class="st-eyebrow">
          <span class="st-eyebrow__bar"></span>
          <span>Theo dõi đơn hàng</span>
        </div>
        <h1 class="st-display">TRA CỨU ĐƠN HÀNG</h1>
        <p class="st-lead">Nhập mã đơn hàng để kiểm tra tình trạng và thông tin chi tiết đơn hàng của bạn.</p>
      </div>
    </header>

    <div class="st-container st-body">
      <!-- Logged-in Customer: Order History -->
      <div v-if="isLoggedIn" class="st-panel">
        <div class="st-panel__head">
          <h2 class="st-panel__title">Lịch sử đơn hàng của bạn</h2>
          <p class="st-panel__sub">Tất cả đơn hàng bạn đã đặt</p>
        </div>

        <div v-if="loading" class="st-loading">
          <span class="st-spinner" aria-hidden="true"></span>
          <p>Đang tải đơn hàng...</p>
        </div>

        <div v-else-if="customerOrders.length > 0" class="st-orders">
          <article
            v-for="(order, index) in customerOrders"
            :key="order.id"
            :id="`order-${order.id}`"
            :class="['st-order', { 'st-order--newest': index === 0 && customerOrders.length > 1, 'st-order--expanded': expandedOrder === order.id }]"
          >
            <div v-if="index === 0 && customerOrders.length > 1" class="st-order__flag">Mới nhất</div>

            <!-- Order Header -->
            <div class="st-order__top">
              <div>
                <p class="st-order__code">{{ order.orderNumber || order.ma }}</p>
                <p class="st-order__meta">{{ order.orderDate }} · {{ order.customerName || order.tenKhachHang }}</p>
              </div>
              <div class="st-order__top-right">
                <span :class="['st-status', getStatusClass(order.status)]">{{ getStatusText(order.status) }}</span>
                <p class="st-order__price">{{ formatCurrency(order.total || order.tongTienSauGiam) }}</p>
              </div>
            </div>

            <!-- Order Items Preview -->
            <div v-if="order.items && order.items.length > 0" class="st-order__preview">
              <p class="st-order__preview-title">Sản phẩm ({{ order.items.length }})</p>
              <div class="st-preview-grid">
                <div v-for="item in order.items.slice(0, 2)" :key="item.id" class="st-preview-item">
                  <img :src="item.anhSanPham || FALLBACK_IMAGE" :alt="item.tenSanPham" class="st-preview-item__img" @error="onImageError">
                  <div class="st-preview-item__info">
                    <p class="st-preview-item__name">{{ item.tenSanPham }}</p>
                    <p class="st-preview-item__variant">
                      <span v-if="item.mauSac">{{ item.mauSac }}</span>
                      <span v-if="item.mauSac && item.kichCo"> · </span>
                      <span v-if="item.kichCo">Size {{ item.kichCo }}</span>
                    </p>
                    <p class="st-preview-item__qty">SL: {{ item.soLuong }} - {{ formatCurrency(item.giaBan) }}</p>
                  </div>
                </div>
              </div>
              <p v-if="order.items.length > 2" class="st-order__more">và {{ order.items.length - 2 }} sản phẩm khác</p>
            </div>

            <!-- Order Actions -->
            <div class="st-order__actions">
              <span class="st-order__addr">Địa chỉ: {{ order.shippingAddress || order.diaChiKhachHang }}</span>
              <button @click="toggleOrderDetails(order.id)" class="st-expand-btn">
                <svg :class="{ 'is-flipped': expandedOrder === order.id }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                </svg>
                {{ expandedOrder === order.id ? 'Thu gọn' : 'Xem chi tiết' }}
              </button>
            </div>

            <!-- Expanded Order Details -->
            <div v-if="expandedOrder === order.id" class="st-order__expanded">
              <div class="st-grid-2">
                <div class="st-detail-block">
                  <h4 class="st-detail-block__title">Thông tin khách hàng</h4>
                  <div class="st-rows">
                    <div class="st-row"><span class="st-row__label">Tên khách hàng:</span><span class="st-row__value">{{ order.tenKhachHang || order.customerName }}</span></div>
                    <div class="st-row"><span class="st-row__label">Số điện thoại:</span><span class="st-row__value">{{ order.soDienThoaiKhachHang || order.phoneNumber }}</span></div>
                    <div class="st-row"><span class="st-row__label">Địa chỉ:</span><span class="st-row__value">{{ order.diaChiKhachHang || order.shippingAddress }}</span></div>
                  </div>
                </div>

                <div class="st-detail-block">
                  <h4 class="st-detail-block__title">Tóm tắt đơn hàng</h4>
                  <div class="st-rows">
                    <div class="st-row"><span class="st-row__label">Mã đơn hàng:</span><span class="st-row__value">{{ order.ma || order.orderNumber }}</span></div>
                    <div class="st-row"><span class="st-row__label">Ngày đặt:</span><span class="st-row__value">{{ order.orderDate }}</span></div>
                    <div class="st-row">
                      <span class="st-row__label">Trạng thái:</span>
                      <span :class="['st-status', getStatusClass(order.status || order.trangThai)]">{{ getStatusText(order.status || order.trangThai) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Order Items -->
              <div v-if="order.items && order.items.length > 0" class="st-detail-items">
                <h4 class="st-detail-block__title">Sản phẩm đã đặt</h4>
                <div class="st-items">
                  <div v-for="item in order.items" :key="item.id" class="st-item">
                    <img :src="item.anhSanPham || FALLBACK_IMAGE" :alt="item.tenSanPham" class="st-item__img" @error="onImageError">
                    <div class="st-item__info">
                      <p class="st-item__name">{{ item.tenSanPham }}</p>
                      <p class="st-item__code">Mã: {{ item.maSanPham }}</p>
                      <div class="st-item__tags">
                        <span v-if="item.mauSac" class="st-tag st-tag--cobalt">{{ item.mauSac }}</span>
                        <span v-if="item.kichCo" class="st-tag st-tag--done">Size {{ item.kichCo }}</span>
                        <span v-if="item.thuongHieu" class="st-tag">{{ item.thuongHieu }}</span>
                      </div>
                    </div>
                    <div class="st-item__price">
                      <p class="st-item__qty">SL: {{ item.soLuong }}</p>
                      <p class="st-item__unit">{{ formatCurrency(item.giaBan) }}</p>
                      <p class="st-item__total">{{ formatCurrency(item.thanhTien) }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div class="st-order__total-row">
                <span>Tổng cộng:</span>
                <strong>{{ formatCurrency(order.total || order.tongTienSauGiam) }}</strong>
              </div>
            </div>
          </article>
        </div>

        <div v-else class="st-empty">
          <svg class="st-empty__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
          </svg>
          <h3 class="st-empty__title">Chưa có đơn hàng nào</h3>
          <p class="st-empty__hint">Bạn chưa có đơn hàng nào.</p>
          <router-link to="/products" class="st-btn st-btn--primary">Mua sắm ngay</router-link>
        </div>
      </div>

      <!-- Guest User: Search Section -->
      <div v-else class="st-panel">
        <form @submit.prevent="searchOrder" class="st-form-stack">
          <div class="st-form__field">
            <label class="st-label">Mã đơn hàng *</label>
            <div class="st-field">
              <svg class="st-field__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
              </svg>
              <input v-model="orderCode" type="text" required class="st-input" placeholder="Nhập mã đơn hàng (VD: #DH001234)">
            </div>
            <p class="st-form__hint">Mã đơn hàng được gửi qua email sau khi đặt hàng thành công</p>
          </div>

          <div class="st-form__center">
            <button type="submit" :disabled="isSearching || !orderCode.trim()" class="st-btn st-btn--primary st-btn--lg">
              <span v-if="isSearching">Đang tìm kiếm...</span>
              <span v-else>Tra cứu đơn hàng</span>
            </button>
          </div>
        </form>
      </div>

      <!-- Search Results -->
      <div v-if="searchResult" class="st-panel">
        <div v-if="searchResult.found" class="st-stack">
          <!-- Order Header -->
          <div class="st-result-head">
            <div>
              <h2 class="st-result-head__code">{{ searchResult.order.orderNumber }}</h2>
              <p class="st-result-head__date">Đặt hàng ngày {{ searchResult.order.orderDate }}</p>
            </div>
            <span :class="['st-status', getStatusClass(searchResult.order.status)]">{{ getStatusText(searchResult.order.status) }}</span>
          </div>

          <!-- Order Progress -->
          <div class="st-detail-block">
            <h3 class="st-detail-block__title">Tiến trình đơn hàng</h3>
            <div class="st-timeline">
              <div v-for="(step, index) in searchResult.order.timeline" :key="index" class="st-timeline__step">
                <div :class="['st-timeline__dot', { 'is-done': step.completed }]">
                  <svg v-if="step.completed" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path>
                  </svg>
                </div>
                <div class="st-timeline__body">
                  <div class="st-timeline__head">
                    <h4 :class="{ 'is-done': step.completed }">{{ step.title }}</h4>
                    <span v-if="step.date">{{ step.date }}</span>
                  </div>
                  <p :class="{ 'is-done': step.completed }">{{ step.description }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Order Items -->
          <div class="st-detail-block">
            <h3 class="st-detail-block__title">Sản phẩm đã đặt</h3>
            <div class="st-items">
              <div v-for="item in searchResult.order.items" :key="item.id" class="st-item st-item--lg">
                <img :src="item.image || FALLBACK_IMAGE" :alt="item.name" class="st-item__img" @error="handleImageError" @load="handleImageLoad">
                <div class="st-item__info">
                  <h4 class="st-item__name">{{ item.name }}</h4>
                  <p v-if="item.productCode" class="st-item__code">Mã: {{ item.productCode }}</p>
                  <p v-if="item.details" class="st-item__variant">{{ item.details }}</p>
                  <p v-if="item.description" class="st-item__desc">{{ item.description }}</p>
                  <p v-if="item.note" class="st-item__note"><span>Ghi chú:</span> {{ item.note }}</p>
                  <p class="st-item__qty">Số lượng: {{ item.quantity }}</p>
                </div>
                <div class="st-item__price">
                  <p class="st-item__unit">{{ formatCurrency(item.price) }} x {{ item.quantity }}</p>
                  <p class="st-item__total">{{ formatCurrency(item.price * item.quantity) }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Order Summary -->
          <div class="st-summary">
            <h3 class="st-detail-block__title">Thông tin đơn hàng</h3>
            <div class="st-grid-2">
              <div>
                <h4 class="st-summary__label">Thông tin khách hàng</h4>
                <p class="st-summary__line"><span>Tên:</span> {{ searchResult.order.customerName }}</p>
                <p class="st-summary__line"><span>SĐT:</span> {{ searchResult.order.phoneNumber }}</p>
                <p v-if="searchResult.order.email" class="st-summary__line"><span>Email:</span> {{ searchResult.order.email }}</p>
              </div>
              <div>
                <h4 class="st-summary__label">Địa chỉ giao hàng</h4>
                <p class="st-summary__line">{{ searchResult.order.shippingAddress }}</p>
              </div>
            </div>
            <div class="st-grid-2">
              <div>
                <h4 class="st-summary__label">Chi tiết đơn hàng</h4>
                <p class="st-summary__line"><span>Loại đơn:</span> {{ searchResult.order.orderType }}</p>
                <p v-if="searchResult.order.paymentDate" class="st-summary__line"><span>Ngày thanh toán:</span> {{ searchResult.order.paymentDate }}</p>
                <p v-if="searchResult.order.trackingNumber" class="st-summary__line"><span>Mã vận đơn:</span> {{ searchResult.order.trackingNumber }}</p>
                <p v-if="searchResult.order.note" class="st-summary__line"><span>Ghi chú:</span> {{ searchResult.order.note }}</p>
              </div>
              <div>
                <h4 class="st-summary__label">Thông tin thanh toán</h4>
                <p class="st-summary__line"><span>Tiền sản phẩm:</span> {{ formatCurrency(searchResult.order.productAmount) }}</p>
                <p class="st-summary__line"><span>Phí vận chuyển:</span> {{ formatCurrency(searchResult.order.shippingFee) }}</p>
                <p v-if="searchResult.order.discountCode" class="st-summary__line"><span>Mã giảm giá:</span> {{ searchResult.order.discountCode }}</p>
                <p class="st-summary__line st-summary__line--total"><span>Tổng tiền:</span> {{ formatCurrency(searchResult.order.totalAfterDiscount) }}</p>
              </div>
            </div>
            <div v-if="searchResult.order.staffName" class="st-summary__staff">
              <h4 class="st-summary__label">Thông tin nhân viên xử lý</h4>
              <p class="st-summary__line">{{ searchResult.order.staffName }} ({{ searchResult.order.staffCode }})</p>
            </div>
          </div>
        </div>

        <!-- Not Found State -->
        <div v-else class="st-empty">
          <div class="st-empty__icon st-empty__icon--danger">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"></path>
            </svg>
          </div>
          <h3 class="st-empty__title">Không tìm thấy đơn hàng</h3>
          <p class="st-empty__hint">Mã đơn hàng "{{ orderCode }}" không tồn tại hoặc đã bị xóa</p>
          <button @click="resetSearch" class="st-btn st-btn--primary">Thử lại</button>
        </div>
      </div>

      <!-- Help Section -->
      <div class="st-help">
        <h3 class="st-help__title">Cần hỗ trợ?</h3>
        <p class="st-help__desc">Nếu bạn không tìm thấy mã đơn hàng hoặc cần hỗ trợ thêm, vui lòng liên hệ với chúng tôi</p>
        <div class="st-help__actions">
          <a href="tel:1900123456" class="st-btn">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path>
            </svg>
            Hotline: 1900 123 456
          </a>
          <a href="mailto:support@striker.vn" class="st-btn">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
            </svg>
            Email hỗ trợ
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { customerAPI } from '@/api/customerAPI'
import authService from '@/services/authService.js'
import { useToast } from 'vue-toastification'

const route = useRoute()
const orderCode = ref('')
const isSearching = ref(false)
const searchResult = ref(null)
const customerOrders = ref([])
const loading = ref(false)
const expandedOrder = ref(null)
const toast = useToast()

// Hiện cả ngày lẫn giờ theo múi giờ Việt Nam (trước đây chỉ hiện ngày, và không chỉ rõ
// timeZone nên có thể lệch giờ so với thực tế do backend trả thời gian dạng UTC).
const formatOrderDateTime = (value) => {
  if (!value) return 'N/A'
  return new Date(value).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit', hour12: false,
    timeZone: 'Asia/Ho_Chi_Minh'
  })
}

// Check if user is logged in
const isLoggedIn = computed(() => authService.isAuthenticated())

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

// Load customer orders for logged-in users (same logic as LichSuMuaHang)
const loadCustomerOrders = async () => {
  try {
    loading.value = true

    // Check authentication
    if (!authService.isAuthenticated()) {
      loading.value = false
      return
    }

    const response = await customerAPI.getOrderHistory(0, 50) // Load all orders

    if (response && response.success && response.data) {
      // Handle paginated response
      let orderData = response.data.data?.content || response.data.content || []

      // Filter out "Hóa đơn chờ" (status 0)
      orderData = orderData.filter(order => order.trangThai !== 0)

      if (Array.isArray(orderData) && orderData.length > 0) {
        // Process orders with same logic as LichSuMuaHang
        customerOrders.value = orderData.map(order => {

          return {
            id: order.id,
            ma: order.ma,
            orderNumber: order.ma,
            orderDate: formatOrderDateTime(order.ngayTao),
            status: order.trangThai,
            total: order.tongTienSauGiam || order.tongTien || 0,
            customerName: order.tenKhachHang || 'N/A',
            tenKhachHang: order.tenKhachHang,
            shippingAddress: order.diaChiKhachHang || 'N/A',
            diaChiKhachHang: order.diaChiKhachHang,
            phoneNumber: order.soDienThoaiKhachHang || 'N/A',
            soDienThoaiKhachHang: order.soDienThoaiKhachHang,
            tongTienSauGiam: order.tongTienSauGiam,
            tongTien: order.tongTien,
            ngayTao: order.ngayTao,
            trangThai: order.trangThai,
            items: order.items || []
          }
        })

        // Sort orders by creation date (newest first) - multiple fallbacks for date comparison
        customerOrders.value.sort((a, b) => {
          // Try multiple date fields for robust sorting
          const getOrderDate = (order) => {
            // Priority: ngayTao > ngayThanhToan > orderDate > id (as fallback)
            if (order.ngayTao) return new Date(order.ngayTao)
            if (order.ngayThanhToan) return new Date(order.ngayThanhToan)
            if (order.orderDate) return new Date(order.orderDate)
            return new Date(0) // Fallback to epoch if no date found
          }

          const dateA = getOrderDate(a)
          const dateB = getOrderDate(b)

          // If dates are equal, sort by ID (higher ID = newer)
          if (dateA.getTime() === dateB.getTime()) {
            return (b.id || 0) - (a.id || 0)
          }

          return dateB - dateA // Descending order (newest first)
        })

        console.log('Orders sorted by date (newest first):', customerOrders.value.map(o => ({
          id: o.id,
          ma: o.ma,
          ngayTao: o.ngayTao,
          orderDate: o.orderDate
        })))

        console.log('Processed customer orders:', customerOrders.value)
        toast.success(`Đã tải ${customerOrders.value.length} đơn hàng`)
      } else {
        customerOrders.value = []
        console.log('No orders found for customer')
        toast.info('Bạn chưa có đơn hàng nào')
      }
    } else {
      console.error('Invalid API response structure:', response)
      customerOrders.value = []
      toast.error('Không thể tải dữ liệu đơn hàng')
    }
  } catch (err) {
    console.error('Error loading customer orders:', err)
    toast.error('Không thể tải lịch sử đơn hàng: ' + (err.response?.data?.message || err.message))

    // QUAN TRỌNG: không được hiện dữ liệu mẫu (sampleOrders) khi API lỗi - trước đây làm
    // vậy khiến khách hàng thấy các đơn hàng GIẢ (#DH001234, #DH001235...) trong lịch sử
    // mua hàng thật của họ, tưởng nhầm là đơn thật. Chỉ hiện danh sách rỗng + thông báo lỗi.
    customerOrders.value = []
  } finally {
    loading.value = false
  }
}

// Map backend status to frontend status (keep numeric for consistency)
const mapBackendStatus = (backendStatus) => {
  return backendStatus
}

// Generate timeline based on status
const generateOrderTimeline = (status) => {
  const timeline = [
    { title: 'Đặt hàng thành công', description: 'Đơn hàng đã được tạo', date: new Date().toLocaleDateString('vi-VN'), completed: true }
  ]

  if (status >= 2) {
    timeline.push({ title: 'Xác nhận đơn hàng', description: 'Đơn hàng đã được xác nhận', date: new Date().toLocaleDateString('vi-VN'), completed: true })
  }

  if (status >= 3) {
    timeline.push({ title: 'Đang chuẩn bị hàng', description: 'Sản phẩm đang được đóng gói', date: new Date().toLocaleDateString('vi-VN'), completed: true })
  }

  if (status >= 4) {
    timeline.push({ title: 'Đang vận chuyển', description: 'Đơn hàng đang được giao', date: new Date().toLocaleDateString('vi-VN'), completed: true })
  }

  if (status === 5) {
    timeline.push({ title: 'Giao hàng thành công', description: 'Đơn hàng đã được giao thành công', date: new Date().toLocaleDateString('vi-VN'), completed: true })
  }

  return timeline
}

// Toggle order details expansion with smooth scroll
const toggleOrderDetails = (orderId) => {
  if (expandedOrder.value === orderId) {
    expandedOrder.value = null
  } else {
    expandedOrder.value = orderId

    // Scroll to the order after a short delay to allow DOM update
    setTimeout(() => {
      const orderElement = document.getElementById(`order-${orderId}`)
      if (orderElement) {
        orderElement.scrollIntoView({
          behavior: 'smooth',
          block: 'start',
          inline: 'nearest'
        })
      }
    }, 100)
  }
}

// Search order by code (for guest users)
const searchOrder = async () => {
  isSearching.value = true

  try {
    // For guest users, search by order code
    const response = await customerAPI.searchOrderByCode(orderCode.value.trim())

    if (response && response.success && response.data) {
      const order = response.data
      console.log('Order data received:', order)
      console.log('Product details:', order.sanPhamChiTietInfos)
      searchResult.value = {
        found: true,
        order: {
          id: order.id,
          orderNumber: order.maHoaDon,
          orderDate: formatOrderDateTime(order.ngayTao),
          paymentDate: order.ngayThanhToan ? formatOrderDateTime(order.ngayThanhToan) : null,
          status: mapBackendStatus(order.trangThai),
          orderType: order.loaiDon || 'Online',
          customerName: order.tenKhachHang,
          phoneNumber: order.soDienThoaiKhachHang || '',
          email: order.email || '',
          shippingAddress: order.diaChiKhachHang || 'Chưa có thông tin',
          note: order.ghiChu || '',
          productAmount: order.tienSanPham || 0,
          shippingFee: order.phiVanChuyen || 0,
          total: order.tongTien || 0,
          totalAfterDiscount: order.tongTienSauGiam || 0,
          discountCode: order.maGiamGia || null,
          staffCode: order.maNhanVien || null,
          staffName: order.tenNhanVien || null,
          trackingNumber: order.maVanDon || null,
          items: order.sanPhamChiTietInfos ? order.sanPhamChiTietInfos.map(item => {
            const imageUrl = getImageUrl(item.duongDan)
            console.log('Item duongDan:', item.duongDan, 'Final imageUrl:', imageUrl)
            return {
              id: item.hoaDonChiTietId,
              name: item.tenSanPham || 'Sản phẩm',
              details: `${item.mauSac ? 'Màu: ' + item.mauSac : ''} ${item.kichCo ? '| Size: ' + item.kichCo : ''} ${item.chatLieu ? '| Chất liệu: ' + item.chatLieu : ''}`.trim(),
              quantity: item.soLuong,
              price: item.giaBan,
              image: imageUrl,
              productCode: item.maSanPham,
              note: item.ghiChu,
              description: item.moTaChiTiet
            }
          }) : [{
            id: 1,
            name: 'Thông tin sản phẩm đang được cập nhật',
            details: 'Loại đơn: ' + (order.loaiDon || 'Online'),
            quantity: 1,
            price: order.tongTienSauGiam || 0,
            image: FALLBACK_IMAGE
          }],
          timeline: generateOrderTimeline(order.trangThai)
        }
      }
      toast.success('Tìm thấy đơn hàng thành công!')
    } else {
      // Không tìm thấy đơn hàng thật trên server - báo đúng như vậy, KHÔNG dò trong dữ
      // liệu mẫu giả (sampleOrders) rồi hiện nhầm thành đơn thật.
      searchResult.value = { found: false, order: null }
      toast.error('Không tìm thấy đơn hàng với mã: ' + orderCode.value.trim())
    }
  } catch (err) {
    console.error('Error searching order:', err)
    toast.error('Không tìm thấy đơn hàng với mã: ' + orderCode.value.trim())
    searchResult.value = { found: false, order: null }
  } finally {
    isSearching.value = false
  }
}

const resetSearch = () => {
  orderCode.value = ''
  searchResult.value = null
}

// Check authentication status and load data
const checkAuthAndLoadData = () => {
  // Check if user is logged in using authService
  if (isLoggedIn.value) {
    console.log('User is logged in, loading customer orders...')
    loadCustomerOrders()
  } else {
    console.log('User not logged in, showing search form...')
  }
}

// Initialize
onMounted(() => {
  // Check for query params from checkout redirect
  const queryOrderCode = route.query.orderCode
  const autoSearch = route.query.autoSearch === 'true'
  const paymentMethod = route.query.payment
  const paymentStatus = route.query.status

  if (queryOrderCode) {
    orderCode.value = queryOrderCode
    console.log('Auto-filled order code from query:', queryOrderCode)

    // Show payment success message for VNPay
    if (paymentMethod === 'vnpay' && paymentStatus === 'success') {
      setTimeout(() => {
        toast.success('Thanh toán VNPay thành công!')
      }, 100)
    }

    // Auto search if requested
    if (autoSearch && !isLoggedIn.value) {
      setTimeout(() => {
        searchOrder()
      }, 500) // Small delay to let UI settle
    }

    // If user is logged in and has new order from payment, refresh order list
    if (isLoggedIn.value && (paymentMethod === 'vnpay' || paymentMethod === 'cod')) {
      setTimeout(() => {
        console.log('Refreshing customer orders after payment...')
        loadCustomerOrders()
      }, 1000) // Delay to ensure order is processed
    }
  }

  checkAuthAndLoadData()
})

const getStatusClass = (status) => {
  // Handle both numeric and string status
  const numericStatus = typeof status === 'number' ? status : parseInt(status)

  switch (numericStatus) {
    case 0: return 'st-status--wait' // Hóa đơn chờ
    case 1: return 'st-status--wait' // Chờ xác nhận
    case 2: return 'st-status--work' // Đã xác nhận
    case 3: return 'st-status--work' // Chờ vận chuyển
    case 4: return 'st-status--ship' // Đang vận chuyển
    case 5: return 'st-status--done' // Đã hoàn thành
    case 6: return 'st-status--stop' // Đã hủy
    default: {
      // Fallback for string status
      const classes = {
        'pending': 'st-status--wait',
        'processing': 'st-status--work',
        'shipping': 'st-status--ship',
        'delivered': 'st-status--done',
        'cancelled': 'st-status--stop'
      }
      return classes[status] || 'st-status--wait'
    }
  }
}

const getStatusText = (status) => {
  // Handle both numeric and string status
  const numericStatus = typeof status === 'number' ? status : parseInt(status)

  switch (numericStatus) {
    case 0: return 'Hóa đơn chờ'
    case 1: return 'Chờ xác nhận'
    case 2: return 'Đã xác nhận'
    case 3: return 'Chờ vận chuyển'
    case 4: return 'Đang vận chuyển'
    case 5: return 'Đã hoàn thành'
    case 6: return 'Đã hủy'
    default: {
      // Fallback for string status
      const texts = {
        'pending': 'Chờ xác nhận',
        'processing': 'Chờ xử lý',
        'shipping': 'Đang vận chuyển',
        'delivered': 'Đã hoàn thành',
        'cancelled': 'Đã hủy'
      }
      return texts[status] || 'Không xác định'
    }
  }
}

const formatCurrency = (amount) => {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const getImageUrl = (imagePath) => {
  if (!imagePath) {
    return FALLBACK_IMAGE
  }

  // If it's already a complete URL
  if (imagePath.startsWith('http')) {
    return imagePath
  }

  // If it starts with /, it's relative to domain
  if (imagePath.startsWith('/')) {
    return `http://localhost:8080${imagePath}`
  }

  // Otherwise, assume it's a filename and prepend the uploads path
  return `http://localhost:8080/uploads/${imagePath}`
}

// Handle image loading events
const handleImageError = (event) => {
  console.log('Image failed to load:', event.target.src)
  event.target.src = FALLBACK_IMAGE
}

const handleImageLoad = (event) => {
  console.log('Image loaded successfully:', event.target.src)
}

</script>

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

.st-container { width: 100%; max-width: 1100px; margin: 0 auto; padding: 0 1.25rem; }

/* ===== Header ===== */
.st-head {
  position: relative;
  overflow: hidden;
  background: var(--ink);
  background-image: repeating-linear-gradient(115deg, rgba(255, 255, 255, 0.022) 0 2px, transparent 2px 74px);
  padding: 3rem 0 2.5rem;
}
.st-head__jersey {
  position: absolute; top: 50%; right: 3rem; transform: translateY(-50%);
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800;
  font-size: clamp(7rem, 16vw, 13rem); line-height: 0.8;
  color: transparent; -webkit-text-stroke: 2px rgba(255, 255, 255, 0.08);
  pointer-events: none; user-select: none;
}
.st-head__inner { position: relative; z-index: 1; }
.st-eyebrow { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.8rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.85rem; letter-spacing: 0.22em; text-transform: uppercase; color: var(--strike); }
.st-eyebrow__bar { width: 2.5rem; height: 3px; background: var(--strike); }
.st-display { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: clamp(2.25rem, 5vw, 3.5rem); line-height: 0.95; text-transform: uppercase; color: var(--chalk); margin: 0 0 0.6rem; }
.st-lead { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 1rem; color: var(--steel); margin: 0; max-width: 34rem; }

/* ===== Body ===== */
.st-body { padding: 2.5rem 1.25rem 4rem; display: flex; flex-direction: column; gap: 1.5rem; }

.st-panel { background: var(--chalk); border: 1px solid var(--line); padding: 1.75rem; }
.st-panel__head { margin-bottom: 1.5rem; }
.st-panel__title { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.5rem; text-transform: uppercase; color: var(--ink); margin: 0 0 0.2rem; }
.st-panel__sub { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; color: var(--slate); margin: 0; font-size: 0.9rem; }

.st-loading { display: flex; flex-direction: column; align-items: center; gap: 1rem; padding: 3rem 1rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; color: var(--slate); }
.st-spinner { width: 2.5rem; height: 2.5rem; border: 3px solid var(--line); border-top-color: var(--strike); border-radius: 50%; animation: st-spin 0.8s linear infinite; }
@keyframes st-spin { to { transform: rotate(360deg); } }

/* Orders list */
.st-orders { display: flex; flex-direction: column; gap: 1rem; }
.st-order { position: relative; background: var(--mist); border: 1px solid var(--line); border-left: 4px solid var(--ink); padding: 1.25rem; }
.st-order--newest { border-left-color: var(--done); padding-top: 2.65rem; }
.st-order--expanded { background: var(--chalk); }
.st-order__flag { position: absolute; top: 1rem; right: 1rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.72rem; letter-spacing: 0.08em; text-transform: uppercase; background: var(--done); color: var(--chalk); padding: 0.25rem 0.7rem; border-radius: 999px; }

.st-order__top { display: flex; align-items: flex-start; justify-content: space-between; gap: 1rem; padding-bottom: 0.9rem; margin-bottom: 0.9rem; border-bottom: 1px solid var(--line); }
.st-order__code { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.2rem; color: var(--ink); margin: 0 0 0.15rem; }
.st-order__meta { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.85rem; color: var(--steel); margin: 0; }
.st-order__top-right { text-align: right; }
.st-order__price { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.15rem; color: var(--ink); margin: 0.35rem 0 0; }

.st-status { display: inline-block; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.72rem; letter-spacing: 0.1em; text-transform: uppercase; padding: 0.28rem 0.65rem; white-space: nowrap; color: var(--chalk); background: var(--steel); }
.st-status--done { background: var(--done); }
.st-status--ship { background: var(--ship); }
.st-status--work { background: var(--work); }
.st-status--stop { background: var(--strike); }
.st-status--wait { background: var(--steel); }

.st-order__preview-title { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.9rem; text-transform: uppercase; color: var(--slate); margin: 0 0 0.75rem; }
.st-preview-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(13rem, 1fr)); gap: 0.75rem; }
.st-preview-item { display: flex; align-items: center; gap: 0.75rem; background: var(--chalk); border: 1px solid var(--line); padding: 0.6rem; }
.st-preview-item__img { width: 2.75rem; height: 2.75rem; object-fit: cover; border: 1px solid var(--line); flex-shrink: 0; }
.st-preview-item__name { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 600; font-size: 0.85rem; color: var(--ink); margin: 0; }
.st-preview-item__variant { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.72rem; letter-spacing: 0.05em; text-transform: uppercase; color: var(--steel); margin: 0.1rem 0; }
.st-preview-item__qty { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.75rem; color: var(--slate); margin: 0; }
.st-order__more { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.82rem; color: var(--steel); text-align: center; margin: 0.75rem 0 0; }

.st-order__actions { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 0.75rem; margin-top: 1rem; padding-top: 0.9rem; border-top: 1px solid var(--line); }
.st-order__addr { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.85rem; color: var(--slate); }
.st-expand-btn { display: inline-flex; align-items: center; gap: 0.4rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.85rem; letter-spacing: 0.05em; text-transform: uppercase; color: var(--cobalt); background: transparent; border: none; cursor: pointer; padding: 0; }
.st-expand-btn svg { width: 1rem; height: 1rem; transition: transform 0.2s ease; }
.st-expand-btn svg.is-flipped { transform: rotate(180deg); }

.st-order__expanded { margin-top: 1.25rem; padding-top: 1.25rem; border-top: 2px solid var(--ink); }
.st-grid-2 { display: grid; grid-template-columns: repeat(auto-fit, minmax(14rem, 1fr)); gap: 1.25rem; margin-bottom: 1.25rem; }
.st-detail-block__title { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 1rem; letter-spacing: 0.03em; text-transform: uppercase; color: var(--ink); margin: 0 0 0.9rem; }
.st-rows { display: flex; flex-direction: column; gap: 0.6rem; }
.st-row { display: flex; justify-content: space-between; gap: 1rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.88rem; }
.st-row__label { color: var(--slate); }
.st-row__value { color: var(--ink); font-weight: 600; text-align: right; }

.st-detail-items { margin-bottom: 1.25rem; }
.st-items { display: flex; flex-direction: column; gap: 0.75rem; }
.st-item { display: flex; align-items: center; gap: 1rem; padding: 0.9rem; background: var(--mist); border: 1px solid var(--line); }
.st-item--lg { align-items: flex-start; background: var(--chalk); }
.st-item__img { width: 3.5rem; height: 3.5rem; object-fit: cover; border: 1px solid var(--line); flex-shrink: 0; }
.st-item--lg .st-item__img { width: 5rem; height: 5rem; }
.st-item__info { flex: 1; min-width: 0; }
.st-item__name { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 600; color: var(--ink); margin: 0 0 0.2rem; }
.st-item__code { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.8rem; color: var(--steel); margin: 0 0 0.35rem; }
.st-item__tags { display: flex; flex-wrap: wrap; gap: 0.4rem; margin-bottom: 0.3rem; }
.st-tag { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.7rem; letter-spacing: 0.05em; text-transform: uppercase; padding: 0.2rem 0.6rem; border-radius: 999px; background: var(--mist); color: var(--slate); }
.st-tag--cobalt { background: #eef0ff; color: var(--cobalt); }
.st-tag--done { background: #eafbf1; color: var(--done); }
.st-item__variant { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.85rem; color: var(--slate); margin: 0 0 0.3rem; }
.st-item__desc { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.82rem; color: var(--steel); margin: 0 0 0.3rem; }
.st-item__note { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.82rem; color: var(--work); margin: 0 0 0.3rem; }
.st-item__note span { font-weight: 700; }
.st-item__qty { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.82rem; color: var(--slate); margin: 0; }
.st-item__price { text-align: right; white-space: nowrap; }
.st-item__unit { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.78rem; color: var(--steel); margin: 0; }
.st-item__total { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.05rem; color: var(--ink); margin: 0.2rem 0 0; }

.st-order__total-row { display: flex; align-items: baseline; justify-content: space-between; padding-top: 0.9rem; border-top: 2px solid var(--ink); font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; color: var(--slate); }
.st-order__total-row strong { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.4rem; letter-spacing: 0; color: var(--strike); }

/* Empty */
.st-empty { text-align: center; padding: 3.5rem 1.5rem; }
.st-empty__icon { width: 3.5rem; height: 3.5rem; margin: 0 auto 1.25rem; color: var(--steel); }
.st-empty__icon--danger { display: grid; place-items: center; background: #fff1ee; color: var(--strike); border-radius: 50%; }
.st-empty__icon--danger svg { width: 1.75rem; height: 1.75rem; }
.st-empty__title { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.4rem; text-transform: uppercase; color: var(--ink); margin: 0 0 0.5rem; }
.st-empty__hint { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; color: var(--slate); margin: 0 0 1.5rem; }

/* Guest search form */
.st-form-stack { display: flex; flex-direction: column; gap: 1.5rem; }
.st-form__field { display: flex; flex-direction: column; gap: 0.5rem; }
.st-label { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.85rem; letter-spacing: 0.06em; text-transform: uppercase; color: var(--slate); }
.st-field { position: relative; display: flex; align-items: center; }
.st-field__icon { position: absolute; left: 1rem; width: 1.15rem; height: 1.15rem; color: var(--steel); pointer-events: none; }
.st-input { width: 100%; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 1rem; color: var(--ink); background: var(--mist); border: 1px solid var(--line); border-radius: 2px; padding: 0.9rem 1rem 0.9rem 2.9rem; }
.st-input:focus { outline: none; border-color: var(--strike); }
.st-form__hint { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.82rem; color: var(--steel); margin: 0; }
.st-form__center { display: flex; justify-content: center; }

/* Result panel */
.st-stack { display: flex; flex-direction: column; gap: 1.75rem; }
.st-result-head { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 1rem; padding-bottom: 1.25rem; border-bottom: 1px solid var(--line); }
.st-result-head__code { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.6rem; color: var(--ink); margin: 0 0 0.2rem; }
.st-result-head__date { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; color: var(--slate); margin: 0; }

/* Timeline */
.st-timeline { display: flex; flex-direction: column; gap: 1.1rem; }
.st-timeline__step { display: flex; align-items: flex-start; gap: 1rem; }
.st-timeline__dot { flex-shrink: 0; width: 1.1rem; height: 1.1rem; border-radius: 50%; background: var(--line); display: grid; place-items: center; margin-top: 0.1rem; }
.st-timeline__dot.is-done { background: var(--done); }
.st-timeline__dot svg { width: 0.7rem; height: 0.7rem; color: var(--chalk); }
.st-timeline__body { flex: 1; }
.st-timeline__head { display: flex; align-items: center; justify-content: space-between; gap: 1rem; }
.st-timeline__head h4 { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.9rem; color: var(--steel); margin: 0; }
.st-timeline__head h4.is-done { color: var(--ink); font-weight: 600; }
.st-timeline__head span { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.78rem; color: var(--steel); }
.st-timeline__body p { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.8rem; color: var(--steel); margin: 0.2rem 0 0; }
.st-timeline__body p.is-done { color: var(--slate); }

/* Summary */
.st-summary { background: var(--mist); padding: 1.5rem; }
.st-summary__label { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.85rem; letter-spacing: 0.04em; text-transform: uppercase; color: var(--ink); margin: 0 0 0.7rem; }
.st-summary__line { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.85rem; color: var(--slate); margin: 0 0 0.4rem; }
.st-summary__line span { font-weight: 600; color: var(--ink); }
.st-summary__line--total { font-weight: 700; color: var(--ink); }
.st-summary__staff { border-top: 1px solid var(--line); padding-top: 1rem; margin-top: 0.5rem; }

/* Help */
.st-help { text-align: center; background: var(--chalk); border: 1px solid var(--line); padding: 2.5rem 1.5rem; }
.st-help__title { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.4rem; text-transform: uppercase; color: var(--ink); margin: 0 0 0.6rem; }
.st-help__desc { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; color: var(--slate); margin: 0 0 1.5rem; }
.st-help__actions { display: flex; flex-wrap: wrap; justify-content: center; gap: 0.75rem; }

/* Buttons */
.st-btn { display: inline-flex; align-items: center; gap: 0.5rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.92rem; letter-spacing: 0.06em; text-transform: uppercase; border: 1px solid var(--line); border-radius: 2px; padding: 0.75rem 1.5rem; cursor: pointer; text-decoration: none; background: var(--chalk); color: var(--ink); transition: all 0.2s ease; }
.st-btn svg { width: 1.1rem; height: 1.1rem; }
.st-btn:hover { border-color: var(--ink); }
.st-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.st-btn--primary { background: var(--ink); border-color: var(--ink); color: var(--chalk); }
.st-btn--primary:hover { background: var(--strike); border-color: var(--strike); }
.st-btn--lg { padding: 0.95rem 2.5rem; font-size: 1rem; }

/* Focus */
.st-btn:focus-visible, .st-input:focus-visible, .st-expand-btn:focus-visible { outline: 3px solid var(--cobalt); outline-offset: 2px; }

/* Responsive */
@media (max-width: 640px) {
  .st-head__jersey { opacity: 0.5; }
  .st-order__top { flex-direction: column; }
  .st-order__top-right { text-align: left; }
}

@media (prefers-reduced-motion: reduce) {
  .st-btn, .st-expand-btn svg { transition: none; }
  .st-spinner { animation: none; }
}
</style>
