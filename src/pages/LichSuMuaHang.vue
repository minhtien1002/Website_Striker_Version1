<template>
  <div class="st-page">
    <!-- Header -->
    <header class="st-head">
      <span class="st-head__jersey" aria-hidden="true">04</span>
      <div class="st-container st-head__inner">
        <div>
          <div class="st-eyebrow">
            <span class="st-eyebrow__bar"></span>
            <span>Tài khoản STRIKER</span>
          </div>
          <h1 class="st-display">LỊCH SỬ MUA HÀNG</h1>
          <p class="st-lead">Theo dõi và quản lý tất cả đơn hàng của bạn.</p>
        </div>

        <div class="st-head__actions">
          <div class="st-head__count">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
            </svg>
            {{ filteredOrders.length }} đơn hàng
          </div>
          <button @click="refreshOrders" :disabled="isRefreshing" class="st-refresh">
            <svg :class="{ 'is-spinning': isRefreshing }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
            </svg>
            {{ isRefreshing ? 'Đang tải...' : 'Làm mới' }}
          </button>
          <span v-if="lastRefreshTime" class="st-head__time">Cập nhật: {{ lastRefreshTime }}</span>
        </div>
      </div>
    </header>

    <div class="st-container st-body">
      <!-- Quick Stats -->
      <div class="st-stats">
        <div class="st-stat">
          <div class="st-stat__icon">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
            </svg>
          </div>
          <div>
            <p class="st-stat__label">Tổng đơn</p>
            <p class="st-stat__value">{{ quickStats.total }}</p>
          </div>
        </div>

        <div class="st-stat">
          <div class="st-stat__icon st-stat__icon--done">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
            </svg>
          </div>
          <div>
            <p class="st-stat__label">Hoàn thành</p>
            <p class="st-stat__value st-stat__value--done">{{ quickStats.completed }}</p>
          </div>
        </div>

        <div class="st-stat">
          <div class="st-stat__icon st-stat__icon--work">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <div>
            <p class="st-stat__label">Đang xử lý</p>
            <p class="st-stat__value st-stat__value--work">{{ quickStats.processing }}</p>
          </div>
        </div>

        <div class="st-stat">
          <div class="st-stat__icon st-stat__icon--stop">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </div>
          <div>
            <p class="st-stat__label">Đã hủy</p>
            <p class="st-stat__value st-stat__value--stop">{{ quickStats.cancelled }}</p>
          </div>
        </div>
      </div>

      <!-- Compact Filter & Search -->
      <div class="st-toolbar">
        <div class="st-tabs">
          <button v-for="status in filterOptions.slice(0, 5)" :key="status.value" @click="activeFilter = status.value"
            :class="['st-tab', { 'is-active': activeFilter === status.value }]">
            {{ status.label }}
            <span v-if="getOrderCountByStatus(status.value) > 0" class="st-tab__count">{{ getOrderCountByStatus(status.value) }}</span>
          </button>
        </div>

        <div class="st-toolbar__right">
          <div class="st-field">
            <svg class="st-field__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>
            <input v-model="searchQuery" type="text" placeholder="Tìm kiếm..." class="st-input">
          </div>

          <select v-model="sortBy" class="st-select">
            <option value="date-desc">Mới nhất</option>
            <option value="date-asc">Cũ nhất</option>
            <option value="amount-desc">Giá cao</option>
            <option value="amount-asc">Giá thấp</option>
          </select>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="st-orders">
        <div v-for="i in 3" :key="i" class="st-order st-order--skeleton">
          <div class="st-skeleton-row">
            <div class="st-skeleton st-skeleton--icon"></div>
            <div class="st-skeleton-lines">
              <div class="st-skeleton st-skeleton--line-lg"></div>
              <div class="st-skeleton st-skeleton--line-sm"></div>
            </div>
          </div>
          <div class="st-skeleton st-skeleton--block"></div>
        </div>
      </div>

      <!-- Orders List -->
      <div v-else class="st-orders">
        <article
          v-for="(order, index) in paginatedOrders" :key="order.id"
          :class="['st-order', { 'st-order--newest': index === 0 && currentPage === 1 && paginatedOrders.length > 1 }]"
        >
          <div v-if="index === 0 && currentPage === 1 && paginatedOrders.length > 1" class="st-order__flag">Mới nhất</div>

          <div class="st-order__top">
            <div class="st-order__top-left">
              <div class="st-order__icon">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
                </svg>
              </div>
              <div>
                <div class="st-order__title-row">
                  <h3 class="st-order__code">Đơn hàng #{{ order.ma || order.id }}</h3>
                  <span class="st-chip">{{ order.orderType || 'Online' }}</span>
                </div>
                <div class="st-order__meta">
                  <span><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3a4 4 0 118 0v4m-4 0v4m-4 4h8m-4-8h4m-4 4h4"></path></svg>{{ order.date }}</span>
                  <span><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>{{ order.customerName }}</span>
                  <span><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"></path></svg>{{ order.items.length }} sản phẩm</span>
                </div>
              </div>
            </div>

            <div class="st-order__top-right">
              <span :class="['st-status', getStatusClass(order.status)]">{{ getStatusText(order.status) }}</span>
              <p class="st-order__price">{{ formatCurrency(order.total) }}</p>
              <p v-if="order.discount > 0" class="st-order__price--old">{{ formatCurrency(order.subtotal + (order.shippingFee || 0)) }}</p>
            </div>
          </div>

          <!-- Compact Items Preview -->
          <div class="st-preview">
            <div class="st-preview__head">
              <h6>Sản phẩm ({{ order.items.length }})</h6>
              <button v-if="order.items.length > 3" @click="toggleOrderDetails(order.id)" class="st-link">Xem tất cả</button>
            </div>

            <div class="st-preview-grid">
              <div v-for="item in order.items.slice(0, 3)" :key="item.id" class="st-preview-item">
                <div class="st-preview-item__img-wrap">
                  <img :src="item.anhSanPham || item.image || FALLBACK_IMAGE" :alt="item.tenSanPham || item.name" @error="onImageError">
                  <span class="st-preview-item__qty-badge">{{ item.soLuong || item.quantity }}</span>
                </div>
                <div class="st-preview-item__info">
                  <p class="st-preview-item__name">{{ item.tenSanPham || item.name }}</p>
                  <div class="st-preview-item__tags">
                    <span v-if="item.mauSac" class="st-color-dot" :style="{ backgroundColor: getColorCode(item.mauSac) }" :title="item.mauSac"></span>
                    <span v-if="item.kichCo" class="st-preview-item__size">{{ item.kichCo }}</span>
                  </div>
                  <p class="st-preview-item__price">{{ formatCurrency(item.thanhTien || item.price) }}</p>
                </div>
              </div>

              <button v-if="order.items.length > 3" class="st-preview-more" @click="toggleOrderDetails(order.id)">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
                </svg>
                <span>+{{ order.items.length - 3 }}</span>
              </button>
            </div>
          </div>

          <!-- Progress -->
          <div class="st-progress">
            <div class="st-progress__label">
              <span>Tiến độ</span>
              <span class="st-progress__value">{{ getProgressText(order.status) }}</span>
            </div>
            <div class="st-progress__track">
              <div :class="['st-progress__bar', getProgressBarClass(order.status)]" :style="{ width: getProgressWidth(order.status) }"></div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="st-order__actions">
            <button @click="toggleOrderDetails(order.id)" class="st-expand-btn">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
              </svg>
              {{ expandedOrders.includes(order.id) ? 'Thu gọn chi tiết' : 'Xem chi tiết' }}
            </button>

            <div class="st-order__buttons">
              <button v-if="order.status === 'delivered' || order.status === 5" class="st-btn st-btn--primary" @click="reorderItems(order)">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg>
                Mua lại
              </button>
              <button v-else-if="order.status === 'shipped' || order.status === 4" class="st-btn st-btn--done" @click="trackOrder(order)">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                Theo dõi
              </button>
              <button v-else-if="order.status === 6" class="st-btn" disabled>Đã hủy</button>
              <!-- Nhãn phải khớp trạng thái thật của đơn, không chốt cứng "Chờ xử lý" -->
              <button v-else class="st-btn st-btn--work" disabled>{{ getStatusText(order.status) }}</button>
              <button class="st-btn" @click="contactSupport(order)">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path></svg>
                Hỗ trợ
              </button>
            </div>
          </div>

          <!-- Expanded Order Details -->
          <div v-if="expandedOrders.includes(order.id)" class="st-order__expanded">
            <!-- All Items -->
            <div class="st-detail-section">
              <h5 class="st-detail-section__title">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"></path></svg>
                Tất cả sản phẩm ({{ order.items.length }})
              </h5>
              <div class="st-items">
                <div v-for="item in order.items" :key="item.id" class="st-item">
                  <img :src="item.anhSanPham || item.image || FALLBACK_IMAGE" :alt="item.tenSanPham || item.name" class="st-item__img" @error="onImageError">
                  <div class="st-item__info">
                    <h6 class="st-item__name">{{ item.tenSanPham || item.name }}</h6>
                    <p class="st-item__code">Mã: {{ item.maSanPham || item.code || 'N/A' }}</p>
                    <div class="st-item__tags">
                      <span v-if="item.mauSac" class="st-tag st-tag--cobalt">{{ item.mauSac }}</span>
                      <span v-if="item.kichCo" class="st-tag st-tag--done">Size {{ item.kichCo }}</span>
                      <span v-if="item.thuongHieu" class="st-tag">{{ item.thuongHieu }}</span>
                      <span v-if="item.chatLieu" class="st-tag">{{ item.chatLieu }}</span>
                    </div>
                    <p class="st-item__qty">Số lượng: {{ item.soLuong || item.quantity }}</p>
                  </div>
                  <div class="st-item__price">
                    <p class="st-item__unit">{{ formatCurrency(item.giaBan || item.price) }} x {{ item.soLuong || item.quantity }}</p>
                    <p class="st-item__total">{{ formatCurrency(item.thanhTien || (item.price * item.quantity)) }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Order Summary -->
            <div class="st-grid-2">
              <div class="st-detail-block">
                <h6 class="st-detail-block__title">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                  Thông tin giao hàng
                </h6>
                <p class="st-detail-block__hint">Khách hàng:</p>
                <p class="st-detail-block__value">{{ order.customerName }}</p>
                <p class="st-detail-block__sub">{{ order.customerPhone }}</p>
                <template v-if="order.shipping">
                  <p class="st-detail-block__hint">Địa chỉ:</p>
                  <p class="st-detail-block__value">{{ order.shipping.address }}</p>
                </template>
                <template v-if="order.shipping?.trackingCode">
                  <p class="st-detail-block__hint">Mã vận đơn:</p>
                  <p class="st-detail-block__value st-detail-block__value--accent">{{ order.shipping.trackingCode }}</p>
                </template>
              </div>

              <div class="st-detail-block st-detail-block--accent">
                <h6 class="st-detail-block__title">Chi tiết thanh toán</h6>
                <div class="st-rows">
                  <div class="st-row"><span class="st-row__label">Tạm tính:</span><span class="st-row__value">{{ formatCurrency(order.subtotal) }}</span></div>
                  <div v-if="order.shippingFee > 0" class="st-row"><span class="st-row__label">Phí vận chuyển:</span><span class="st-row__value">{{ formatCurrency(order.shippingFee) }}</span></div>
                  <div v-if="order.discount > 0" class="st-row"><span class="st-row__label">Giảm giá:</span><span class="st-row__value st-row__value--done">-{{ formatCurrency(order.discount) }}</span></div>
                  <div class="st-row st-row--total"><span>Tổng thanh toán:</span><span class="st-row__grand">{{ formatCurrency(order.total) }}</span></div>
                  <div v-if="order.note" class="st-row"><span class="st-row__label">Ghi chú:</span><span class="st-row__value">{{ order.note }}</span></div>
                </div>
              </div>
            </div>

            <!-- Order Timeline -->
            <div class="st-detail-section">
              <h5 class="st-detail-section__title">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                Lịch sử đơn hàng
              </h5>
              <div class="st-timeline">
                <div v-for="(timeline, index) in order.timeline" :key="index" class="st-timeline__step">
                  <div class="st-timeline__rail">
                    <div :class="['st-timeline__dot', { 'is-done': timeline.completed }]"></div>
                    <div v-if="index !== order.timeline.length - 1" :class="['st-timeline__line', { 'is-done': timeline.completed }]"></div>
                  </div>
                  <div class="st-timeline__body">
                    <div class="st-timeline__head">
                      <h6 :class="{ 'is-done': timeline.completed }">{{ timeline.title }}</h6>
                      <span>{{ timeline.time }}</span>
                    </div>
                    <p>{{ timeline.description }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Shipping Info -->
            <div v-if="order.shipping" class="st-detail-block">
              <h5 class="st-detail-block__title">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                Thông tin giao hàng
              </h5>
              <div class="st-grid-2">
                <div><p class="st-detail-block__hint">Địa chỉ giao hàng:</p><p class="st-detail-block__value">{{ order.shipping.address }}</p></div>
                <div><p class="st-detail-block__hint">Phương thức vận chuyển:</p><p class="st-detail-block__value">{{ order.shipping.method }}</p></div>
                <div v-if="order.shipping.trackingCode"><p class="st-detail-block__hint">Mã vận đơn:</p><p class="st-detail-block__value st-detail-block__value--accent">{{ order.shipping.trackingCode }}</p></div>
                <div><p class="st-detail-block__hint">Phí vận chuyển:</p><p class="st-detail-block__value">{{ formatCurrency(order.shipping.fee) }}</p></div>
              </div>
            </div>
          </div>
        </article>
      </div>

      <!-- Pagination -->
      <nav v-if="totalPages > 1" class="st-pager">
        <button @click="currentPage = Math.max(1, currentPage - 1)" :disabled="currentPage === 1" class="st-page-btn">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg>
        </button>

        <div class="st-pager__list">
          <button v-for="page in visiblePages" :key="page" @click="currentPage = page" :class="['st-page-btn', { 'is-active': page === currentPage }]">
            {{ page }}
          </button>
        </div>

        <button @click="currentPage = Math.min(totalPages, currentPage + 1)" :disabled="currentPage === totalPages" class="st-page-btn">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
        </button>
      </nav>

      <!-- Enhanced Empty State -->
      <div v-if="!loading && filteredOrders.length === 0" class="st-empty">
        <svg class="st-empty__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
        </svg>
        <h3 class="st-empty__title">{{ activeFilter === 'all' ? 'Chưa có đơn hàng nào' : 'Không tìm thấy đơn hàng' }}</h3>
        <p class="st-empty__hint">
          {{activeFilter === 'all'
            ? 'Bạn chưa có đơn hàng nào. Hãy khám phá các sản phẩm tuyệt vời của chúng tôi!'
            : `Không có đơn hàng nào với trạng thái "${filterOptions.find(f => f.value === activeFilter)?.label}".`
          }}
        </p>
        <div class="st-empty__actions">
          <router-link to="/products" class="st-btn st-btn--primary">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path></svg>
            Mua sắm ngay
          </router-link>
          <button v-if="activeFilter !== 'all'" @click="activeFilter = 'all'" class="st-link">Xem tất cả đơn hàng</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useToast } from 'vue-toastification'
import { customerAPI } from '@/api/customerAPI'
import authService from '@/services/authService.js'
import { vnpayService } from '@/services/vnpayAPI.js'

// Filter and sort options
const activeFilter = ref('all')
const sortBy = ref('date-desc')
const expandedOrders = ref([])
const loading = ref(false)
const error = ref('')
const searchQuery = ref('')

// Pagination
const currentPage = ref(1)
const itemsPerPage = ref(5)

// Advanced features
const isRefreshing = ref(false)
const lastRefreshTime = ref(null)

// Route and toast for success messages
const route = useRoute()
const toast = useToast()

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

// Khớp với bảng mã trạng thái của backend (HoaDonStatus).
// Mã 3 ("Chờ vận chuyển") đã bị bỏ khỏi luồng nên không còn là lựa chọn lọc; đơn cũ
// đang ở mã 3 vẫn xem được ở mục "Tất cả" và vẫn hiện đúng nhãn.
const filterOptions = ref([
  { value: 'all', label: 'Tất cả' },
  { value: 0, label: 'Hóa đơn chờ' },
  { value: 1, label: 'Chờ xác nhận' },
  { value: 2, label: 'Đã xác nhận' },
  { value: 4, label: 'Đang vận chuyển' },
  { value: 5, label: 'Đã hoàn thành' },
  { value: 6, label: 'Đã hủy' }
])

// Orders data from API
const orders = ref([])

// Load customer order history
const loadOrderHistory = async () => {
  try {
    loading.value = true
    error.value = ''

    // Check authentication
    if (!authService.isAuthenticated()) {
      error.value = 'Vui lòng đăng nhập để xem lịch sử đơn hàng'
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
        // Process orders with real items from backend
        orders.value = orderData.map(order => {

          return {
            id: order.id,
            ma: order.ma,
            // Hiện cả ngày lẫn giờ (trước đây chỉ có ngày) theo múi giờ Việt Nam - backend trả
            // thời gian dạng UTC nên phải chỉ rõ timeZone, không để trình duyệt tự đoán.
            date: order.ngayTao ? new Date(order.ngayTao).toLocaleString('vi-VN', {
              day: '2-digit', month: '2-digit', year: 'numeric',
              hour: '2-digit', minute: '2-digit', hour12: false,
              timeZone: 'Asia/Ho_Chi_Minh'
            }) : 'N/A',
            rawDate: order.ngayTao ? new Date(order.ngayTao) : new Date(0), // Raw date for sorting
            status: mapBackendStatus(order.trangThai),
            total: order.tongTienSauGiam || order.tongTien || 0,
            subtotal: order.tongTien || 0,
            shippingFee: order.phiVanChuyen || 0,
            discount: (order.tongTien || 0) - (order.tongTienSauGiam || 0),
            customerName: order.tenKhachHang || 'N/A',
            customerPhone: order.soDienThoaiKhachHang || 'N/A',
            customerEmail: order.email || 'N/A',
            note: order.ghiChu || '',
            orderType: order.loaiDon || 'Online',
            // Use real items from backend or fallback
            items: order.items && order.items.length > 0 ? order.items : [
              {
                id: 1,
                name: `Sản phẩm trong đơn hàng ${order.ma}`,
                tenSanPham: `Sản phẩm trong đơn hàng ${order.ma}`,
                details: `Tổng tiền: ${formatCurrency(order.tongTienSauGiam || order.tongTien || 0)}`,
                image: FALLBACK_IMAGE,
                anhSanPham: FALLBACK_IMAGE,
                quantity: 1,
                soLuong: 1,
                price: order.tongTienSauGiam || order.tongTien || 0,
                giaBan: order.tongTienSauGiam || order.tongTien || 0,
                thanhTien: order.tongTienSauGiam || order.tongTien || 0
              }
            ],
            timeline: generateTimeline(order.trangThai),
            shipping: {
              address: order.diaChiKhachHang || 'Chưa có thông tin',
              method: 'Giao hàng tiêu chuẩn',
              trackingCode: order.maVanDon || null,
              fee: order.phiVanChuyen || 0
            }
          }
        })

        console.log('Processed orders:', orders.value)
        toast.success(`Đã tải ${orders.value.length} đơn hàng`)
      } else {
        orders.value = []
        console.log('No orders found in response')
        toast.info('Bạn chưa có đơn hàng nào')
      }
    } else {
      console.error('Unexpected API response format:', response)
      error.value = 'Không thể tải dữ liệu đơn hàng'
      orders.value = []
    }
  } catch (err) {
    console.error('Error loading order history:', err)
    error.value = 'Không thể tải lịch sử đơn hàng: ' + (err.response?.data?.message || err.message)

    // Không hiện đơn hàng giả khi lỗi API - khách sẽ tưởng là đơn thật của mình
    orders.value = []
    toast.error('Không thể tải lịch sử đơn hàng. Vui lòng thử lại.')
  } finally {
    loading.value = false
  }
}

// Load order items details
const loadOrderItems = async (orderId) => {
  try {
    console.log('Loading detailed items for order:', orderId)
    const response = await customerAPI.getOrderDetails(orderId)
    console.log('Order details response:', response)

    if (response.success && response.data) {
      // Check if items exist in response
      if (response.data.items && Array.isArray(response.data.items) && response.data.items.length > 0) {
        return response.data.items.map(item => ({
          id: item.id,
          name: item.tenSanPham || item.name || 'Sản phẩm',
          details: `${item.tenMauSac || item.mauSac || 'N/A'} - ${item.tenKichCo || item.kichCo || 'N/A'}`,
          image: item.urlAnh || item.anhSanPham || FALLBACK_IMAGE,
          quantity: item.soLuong || 1,
          price: item.donGia || item.giaBan || 0
        }))
      } else {
        // If no items in response, return enhanced default item
        const order = response.data.order
        return [
          {
            id: 1,
            name: `Đơn hàng ${orderId}`,
            details: order ? `Trạng thái: ${getStatusText(order.trangThai)}` : 'Chi tiết sản phẩm',
            image: FALLBACK_IMAGE,
            quantity: 1,
            price: order ? (order.tongTienSauGiam || order.tongTien || 0) : 0
          }
        ]
      }
    }
  } catch (err) {
    console.error('Error loading order items:', err)
    toast.warning('Không thể tải chi tiết sản phẩm từ server')
  }

  // Fallback item
  return [
    {
      id: 1,
      name: `Đơn hàng ${orderId}`,
      details: 'Chi tiết sản phẩm tạm thời không khả dụng',
      image: FALLBACK_IMAGE,
      quantity: 1,
      price: 0
    }
  ]
}

// Map backend status to frontend status
const mapBackendStatus = (backendStatus) => {
  // Keep numeric status for proper filtering
  return backendStatus
}

// Generate timeline based on status
const generateTimeline = (status) => {
  const baseTimeline = [
    {
      title: 'Đơn hàng đã được tạo',
      description: 'Đơn hàng của bạn đã được xác nhận',
      time: new Date().toLocaleDateString('vi-VN'),
      completed: true
    }
  ]

  if (status >= 2) {
    baseTimeline.push({
      title: 'Đang chuẩn bị hàng',
      description: 'Sản phẩm đang được đóng gói',
      time: new Date().toLocaleDateString('vi-VN'),
      completed: true
    })
  }

  if (status >= 4) {
    baseTimeline.push({
      title: 'Đang vận chuyển',
      description: 'Đơn hàng đang được giao',
      time: new Date().toLocaleDateString('vi-VN'),
      completed: true
    })
  }

  if (status === 5) {
    baseTimeline.push({
      title: 'Đã giao thành công',
      description: 'Đơn hàng đã được giao đến địa chỉ',
      time: new Date().toLocaleDateString('vi-VN'),
      completed: true
    })
  }

  return baseTimeline
}

// Handle VNPay return from URL parameters
const handleVNPayReturn = () => {
  const urlParams = new URLSearchParams(window.location.search)
  const vnpayResult = vnpayService.handleVNPayReturn(urlParams)

  if (vnpayResult) {
    if (vnpayResult.success) {
      toast.success(vnpayResult.message, {
        timeout: 5000,
        position: 'top-center'
      })

      // Reload order history to show the new order
      setTimeout(() => {
        loadOrderHistory()
      }, 1000)
    } else {
      toast.error(vnpayResult.message, {
        timeout: 5000,
        position: 'top-center'
      })
    }

    // Clean up URL parameters
    const cleanUrl = window.location.pathname
    window.history.replaceState({}, document.title, cleanUrl)
  }
}

// Initialize data
onMounted(() => {
  // Handle VNPay return callback
  handleVNPayReturn()

  // Check for payment success from redirect
  const paymentMethod = route.query.payment
  const paymentStatus = route.query.status

  if (paymentMethod && paymentStatus === 'success') {
    console.log('Payment success detected, will refresh orders after loading...')

    // Show payment success message
    setTimeout(() => {
      if (paymentMethod === 'vnpay') {
        toast.success('Thanh toán VNPay thành công!')
      } else if (paymentMethod === 'cod') {
        toast.success('Đặt hàng COD thành công!')
      }
    }, 500)

    // Auto-refresh orders after initial load to show new order
    setTimeout(() => {
      console.log('Auto-refreshing orders after payment success...')
      refreshOrders()
    }, 2000) // Delay to ensure order is processed
  }

  // Load order history
  loadOrderHistory()
})

// Computed properties
const filteredOrders = computed(() => {
  let filtered = orders.value

  // Apply search filter
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase().trim()
    filtered = filtered.filter(order =>
      order.id.toString().toLowerCase().includes(query) ||
      order.customerName?.toLowerCase().includes(query) ||
      order.customerPhone?.toLowerCase().includes(query)
    )
  }

  // Apply status filter
  if (activeFilter.value !== 'all') {
    filtered = filtered.filter(order => order.status === activeFilter.value)
  }

  // Sort orders with robust date comparison and ID fallback
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'date-desc':
        // If rawDates are equal, sort by ID (higher ID = newer)
        if (b.rawDate - a.rawDate === 0) {
          return (b.id || 0) - (a.id || 0)
        }
        return b.rawDate - a.rawDate // Newest first
      case 'date-asc':
        // If rawDates are equal, sort by ID (lower ID = older)
        if (a.rawDate - b.rawDate === 0) {
          return (a.id || 0) - (b.id || 0)
        }
        return a.rawDate - b.rawDate // Oldest first
      case 'amount-desc':
        // If amounts are equal, sort by date (newest first)
        if (b.total - a.total === 0) {
          return b.rawDate - a.rawDate
        }
        return b.total - a.total
      case 'amount-asc':
        // If amounts are equal, sort by date (newest first)
        if (a.total - b.total === 0) {
          return b.rawDate - a.rawDate
        }
        return a.total - b.total
      default:
        // Default to newest first with ID fallback
        if (b.rawDate - a.rawDate === 0) {
          return (b.id || 0) - (a.id || 0)
        }
        return b.rawDate - a.rawDate
    }
  })

  console.log('Orders sorted (newest first):', filtered.slice(0, 3).map(o => ({
    id: o.id,
    date: o.date,
    rawDate: o.rawDate,
    sortBy: sortBy.value
  })))

  return filtered
})

// Pagination computed properties
const totalPages = computed(() => Math.ceil(filteredOrders.value.length / itemsPerPage.value))

const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredOrders.value.slice(start, end)
})

const visiblePages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  const delta = 2
  const range = []

  for (let i = Math.max(2, current - delta); i <= Math.min(total - 1, current + delta); i++) {
    range.push(i)
  }

  if (current - delta > 2) {
    range.unshift('...')
  }
  if (current + delta < total - 1) {
    range.push('...')
  }

  range.unshift(1)
  if (total > 1) {
    range.push(total)
  }

  return range.filter((item, index, arr) => arr.indexOf(item) === index)
})

// Methods
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

// Get color code for color display
const getColorCode = (colorName) => {
  const colorMap = {
    'Đỏ': '#ef4444',
    'Xanh': '#3b82f6',
    'Vàng': '#eab308',
    'Đen': '#1f2937',
    'Trắng': '#f9fafb',
    'Xám': '#6b7280',
    'Hồng': '#ec4899',
    'Tím': '#8b5cf6',
    'Cam': '#f97316',
    'Nâu': '#a3a3a3',
    'Xanh lá': '#22c55e',
    'Xanh dương': '#3b82f6'
  }
  return colorMap[colorName] || '#9ca3af'
}

const getStatusClass = (status) => {
  const classes = {
    0: 'st-status--wait',
    1: 'st-status--wait',
    2: 'st-status--work',
    3: 'st-status--work',
    4: 'st-status--ship',
    5: 'st-status--done',
    6: 'st-status--stop',
    // Legacy support for old string-based statuses
    'delivered': 'st-status--done',
    'shipped': 'st-status--ship',
    'processing': 'st-status--work',
    'cancelled': 'st-status--stop'
  }
  return classes[status] || 'st-status--wait'
}

const getStatusText = (status) => {
  const texts = {
    0: 'Hóa đơn chờ',
    1: 'Chờ xác nhận',
    2: 'Đã xác nhận',
    3: 'Chờ vận chuyển',
    4: 'Đang vận chuyển',
    5: 'Đã hoàn thành',
    6: 'Đã hủy',
    // Legacy support
    'delivered': 'Đã hoàn thành',
    'shipped': 'Đang vận chuyển',
    'processing': 'Chờ xử lý',
    'cancelled': 'Đã hủy'
  }
  return texts[status] || 'Không xác định'
}

const getProgressText = (status) => {
  const texts = {
    // Numeric status from backend (HoaDonStatus)
    0: 'Hóa đơn chờ',
    1: 'Chờ xác nhận',
    2: 'Đã xác nhận',
    // Mã 3 đã bỏ khỏi luồng, chỉ giữ nhãn cho đơn cũ
    3: 'Chờ vận chuyển',
    4: 'Đang vận chuyển',
    5: 'Hoàn thành',
    6: 'Đã hủy',
    // Legacy string support
    'delivered': 'Hoàn thành',
    'shipped': 'Đang vận chuyển',
    'processing': 'Đang chuẩn bị',
    'cancelled': 'Đã hủy'
  }
  return texts[status] || 'Không xác định'
}

const getProgressBarClass = (status) => {
  const classes = {
    // Numeric status (0-6) from backend
    0: 'st-progress__bar--wait',
    1: 'st-progress__bar--wait',
    2: 'st-progress__bar--work',
    3: 'st-progress__bar--work',
    4: 'st-progress__bar--ship',
    5: 'st-progress__bar--done',
    6: 'st-progress__bar--stop',
    // Legacy string support
    'delivered': 'st-progress__bar--done',
    'shipped': 'st-progress__bar--ship',
    'processing': 'st-progress__bar--work',
    'cancelled': 'st-progress__bar--stop'
  }
  return classes[status] || 'st-progress__bar--wait'
}

const getProgressWidth = (status) => {
  const widths = {
    // Luồng 4 bước: Chờ xác nhận -> Đã xác nhận -> Đang vận chuyển -> Đã hoàn thành,
    // nên các mốc chia đều 25% thay vì 20/40/60/80 của luồng 5 bước cũ.
    0: '10%',     // Hóa đơn chờ - chưa thực sự đặt hàng
    1: '25%',     // Chờ xác nhận
    2: '50%',     // Đã xác nhận
    3: '50%',     // Chờ vận chuyển - mã cũ, coi như tương đương Đã xác nhận
    4: '75%',     // Đang vận chuyển
    5: '100%',    // Đã hoàn thành
    6: '0%',      // Đã hủy
    // Legacy string support
    'delivered': '100%',
    'shipped': '75%',
    'processing': '25%',
    'cancelled': '0%'
  }
  return widths[status] || '0%'
}

const getOrderCountByStatus = (status) => {
  if (status === 'all') return orders.value.length
  return orders.value.filter(order => order.status === status).length
}

const toggleOrderDetails = async (orderId) => {
  const index = expandedOrders.value.indexOf(orderId)
  if (index > -1) {
    expandedOrders.value.splice(index, 1)
  } else {
    expandedOrders.value.push(orderId)

    // Load detailed items when expanding for the first time
    const order = orders.value.find(o => o.id === orderId)
    if (order && order.items.length === 1 && order.items[0].name.includes('Sản phẩm trong đơn hàng')) {
      try {
        console.log('Loading detailed items for order:', orderId)
        const detailedItems = await loadOrderItems(orderId)
        order.items = detailedItems
      } catch (err) {
        console.error('Error loading detailed items:', err)
        toast.error('Không thể tải chi tiết sản phẩm')
      }
    }
  }
}

const reorderItems = (order) => {
  toast.info('Chức năng mua lại đang được phát triển')
  // TODO: Implement reorder functionality
  console.log('Reordering items for order:', order.id)
}

const trackOrder = (order) => {
  if (order.shipping?.trackingCode) {
    toast.info(`Mã vận đơn: ${order.shipping.trackingCode}`)
  } else {
    toast.info('Chưa có thông tin vận chuyển')
  }
  // TODO: Implement order tracking
  console.log('Tracking order:', order.id)
}

const contactSupport = (order) => {
  toast.info('Đang chuyển hướng đến trang hỗ trợ...')
  // TODO: Implement support contact
  console.log('Contacting support for order:', order.id)
}

// Refresh orders
const refreshOrders = async () => {
  isRefreshing.value = true
  try {
    await loadOrderHistory()
    lastRefreshTime.value = new Date().toLocaleTimeString('vi-VN')
    toast.success('Đã cập nhật danh sách đơn hàng')
  } catch (err) {
    toast.error('Không thể làm mới danh sách đơn hàng')
  } finally {
    isRefreshing.value = false
  }
}

// Quick stats
const quickStats = computed(() => {
  const total = orders.value.length
  const completed = orders.value.filter(o => o.status === 'delivered' || o.status === 5).length
  const processing = orders.value.filter(o => ['processing', 1, 2, 3, 4].includes(o.status)).length
  const cancelled = orders.value.filter(o => o.status === 'cancelled' || o.status === 6).length

  return { total, completed, processing, cancelled }
})

// Watch for filter changes to reset pagination
watch([activeFilter, searchQuery], () => {
  currentPage.value = 1
})

// Auto refresh every 5 minutes
let refreshInterval = null
onMounted(() => {
  refreshInterval = setInterval(() => {
    if (!loading.value && !isRefreshing.value) {
      refreshOrders()
    }
  }, 5 * 60 * 1000) // 5 minutes
})

// Cleanup interval on unmount
onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
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

.st-container { width: 100%; max-width: 1200px; margin: 0 auto; padding: 0 1.25rem; }

/* ===== Header ===== */
.st-head { position: relative; overflow: hidden; background: var(--ink); background-image: repeating-linear-gradient(115deg, rgba(255, 255, 255, 0.022) 0 2px, transparent 2px 74px); padding: 3rem 0 2.5rem; }
.st-head__jersey { position: absolute; top: 50%; right: 3rem; transform: translateY(-50%); font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: clamp(7rem, 16vw, 13rem); line-height: 0.8; color: transparent; -webkit-text-stroke: 2px rgba(255, 255, 255, 0.08); pointer-events: none; user-select: none; }
.st-head__inner { position: relative; z-index: 1; display: flex; flex-wrap: wrap; align-items: flex-end; justify-content: space-between; gap: 1.25rem; }
.st-eyebrow { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.8rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.85rem; letter-spacing: 0.22em; text-transform: uppercase; color: var(--strike); }
.st-eyebrow__bar { width: 2.5rem; height: 3px; background: var(--strike); }
.st-display { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: clamp(2.25rem, 5vw, 3.5rem); line-height: 0.95; text-transform: uppercase; color: var(--chalk); margin: 0 0 0.6rem; }
.st-lead { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 1rem; color: var(--steel); margin: 0; }

.st-head__actions { display: flex; flex-wrap: wrap; align-items: center; gap: 1rem; }
.st-head__count { display: flex; align-items: center; gap: 0.4rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.9rem; color: var(--steel); }
.st-head__count svg { width: 1.1rem; height: 1.1rem; }
.st-refresh { display: inline-flex; align-items: center; gap: 0.5rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.85rem; letter-spacing: 0.05em; text-transform: uppercase; color: var(--chalk); background: transparent; border: 1px solid rgba(255,255,255,0.25); border-radius: 2px; padding: 0.6rem 1.1rem; cursor: pointer; transition: all 0.2s ease; }
.st-refresh svg { width: 1rem; height: 1rem; }
.st-refresh svg.is-spinning { animation: st-spin 0.8s linear infinite; }
.st-refresh:hover:not(:disabled) { background: var(--strike); border-color: var(--strike); }
.st-refresh:disabled { opacity: 0.5; cursor: not-allowed; }
.st-head__time { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.78rem; color: var(--steel); }

/* ===== Body ===== */
.st-body { padding: 2rem 1.25rem 4rem; display: flex; flex-direction: column; gap: 1.5rem; }

/* Stats */
.st-stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(11rem, 1fr)); gap: 1rem; }
.st-stat { display: flex; align-items: center; gap: 0.9rem; background: var(--chalk); border: 1px solid var(--line); padding: 1.1rem; }
.st-stat__icon { width: 2.5rem; height: 2.5rem; flex-shrink: 0; display: grid; place-items: center; background: #eef0ff; color: var(--cobalt); border-radius: 4px; }
.st-stat__icon svg { width: 1.2rem; height: 1.2rem; }
.st-stat__icon--done { background: #eafbf1; color: var(--done); }
.st-stat__icon--work { background: #fff6e5; color: var(--work); }
.st-stat__icon--stop { background: #fff1ee; color: var(--strike); }
.st-stat__label { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.8rem; color: var(--slate); margin: 0; }
.st-stat__value { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.4rem; color: var(--ink); margin: 0; }
.st-stat__value--done { color: var(--done); }
.st-stat__value--work { color: var(--work); }
.st-stat__value--stop { color: var(--strike); }

/* Toolbar */
.st-toolbar { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 1rem; background: var(--chalk); border: 1px solid var(--line); padding: 1rem; }
.st-tabs { display: flex; flex-wrap: wrap; gap: 0.4rem; }
.st-tab { display: inline-flex; align-items: center; gap: 0.4rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.8rem; letter-spacing: 0.04em; text-transform: uppercase; color: var(--slate); background: var(--mist); border: 1px solid transparent; border-radius: 2px; padding: 0.45rem 0.75rem; cursor: pointer; transition: all 0.2s ease; }
.st-tab:hover { color: var(--ink); }
.st-tab.is-active { background: var(--ink); color: var(--chalk); }
.st-tab__count { font-size: 0.7rem; padding: 0.05rem 0.4rem; border-radius: 999px; background: rgba(255,255,255,0.2); }
.st-tab:not(.is-active) .st-tab__count { background: var(--line); }

.st-toolbar__right { display: flex; align-items: center; gap: 0.6rem; }
.st-field { position: relative; display: flex; align-items: center; }
.st-field__icon { position: absolute; left: 0.65rem; width: 0.9rem; height: 0.9rem; color: var(--steel); pointer-events: none; }
.st-input, .st-select { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.82rem; color: var(--ink); background: var(--mist); border: 1px solid var(--line); border-radius: 2px; padding: 0.5rem 0.7rem; }
.st-input { padding-left: 2.1rem; width: 10rem; }
.st-input:focus, .st-select:focus { outline: none; border-color: var(--strike); }

/* Orders */
.st-orders { display: flex; flex-direction: column; gap: 1.25rem; }
.st-order { position: relative; background: var(--chalk); border: 1px solid var(--line); border-left: 4px solid var(--ink); padding: 1.5rem; }
.st-order--newest { border-left-color: var(--done); padding-top: 2.85rem; }
.st-order__flag { position: absolute; top: 1rem; right: 1rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.72rem; letter-spacing: 0.08em; text-transform: uppercase; background: var(--done); color: var(--chalk); padding: 0.25rem 0.7rem; border-radius: 999px; }

.st-order__top { display: flex; flex-wrap: wrap; justify-content: space-between; gap: 1rem; margin-bottom: 1.25rem; }
.st-order__top-left { display: flex; align-items: flex-start; gap: 1rem; }
.st-order__icon { width: 2.75rem; height: 2.75rem; flex-shrink: 0; display: grid; place-items: center; background: var(--mist); color: var(--cobalt); border-radius: 4px; }
.st-order__icon svg { width: 1.3rem; height: 1.3rem; }
.st-order__title-row { display: flex; align-items: center; gap: 0.6rem; margin-bottom: 0.4rem; }
.st-order__code { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.15rem; color: var(--ink); margin: 0; }
.st-chip { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.68rem; letter-spacing: 0.06em; text-transform: uppercase; padding: 0.2rem 0.55rem; background: #eef0ff; color: var(--cobalt); border-radius: 999px; }
.st-order__meta { display: flex; flex-wrap: wrap; gap: 1rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.82rem; color: var(--slate); }
.st-order__meta span { display: inline-flex; align-items: center; gap: 0.35rem; }
.st-order__meta svg { width: 1rem; height: 1rem; color: var(--steel); }

.st-order__top-right { text-align: right; }
.st-order__price { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.4rem; color: var(--ink); margin: 0.4rem 0 0; }
.st-order__price--old { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.78rem; color: var(--steel); text-decoration: line-through; margin: 0.15rem 0 0; }

.st-status { display: inline-block; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.72rem; letter-spacing: 0.1em; text-transform: uppercase; padding: 0.28rem 0.65rem; white-space: nowrap; color: var(--chalk); background: var(--steel); }
.st-status--done { background: var(--done); }
.st-status--ship { background: var(--ship); }
.st-status--work { background: var(--work); }
.st-status--stop { background: var(--strike); }
.st-status--wait { background: var(--steel); }

/* Preview */
.st-preview { background: var(--mist); padding: 1rem; margin-bottom: 1.1rem; }
.st-preview__head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; }
.st-preview__head h6 { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.88rem; text-transform: uppercase; color: var(--slate); margin: 0; }
.st-link { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.8rem; letter-spacing: 0.04em; text-transform: uppercase; color: var(--cobalt); background: none; border: none; cursor: pointer; padding: 0; }
.st-preview-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(10rem, 1fr)); gap: 0.6rem; }
.st-preview-item { display: flex; align-items: center; gap: 0.6rem; background: var(--chalk); border: 1px solid var(--line); padding: 0.5rem; }
.st-preview-item__img-wrap { position: relative; flex-shrink: 0; }
.st-preview-item__img-wrap img { width: 2.5rem; height: 2.5rem; object-fit: cover; border: 1px solid var(--line); }
.st-preview-item__qty-badge { position: absolute; top: -0.4rem; right: -0.4rem; background: var(--ink); color: var(--chalk); font-size: 0.62rem; font-weight: 700; min-width: 1.1rem; height: 1.1rem; border-radius: 999px; display: grid; place-items: center; }
.st-preview-item__name { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.8rem; font-weight: 600; color: var(--ink); margin: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 8rem; }
.st-preview-item__tags { display: flex; align-items: center; gap: 0.3rem; margin: 0.2rem 0; }
.st-color-dot { width: 0.7rem; height: 0.7rem; border-radius: 50%; border: 1px solid var(--line); display: inline-block; }
.st-preview-item__size { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.65rem; font-weight: 700; background: var(--mist); padding: 0.05rem 0.35rem; border-radius: 2px; color: var(--slate); }
.st-preview-item__price { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.78rem; color: var(--strike); margin: 0; }
.st-preview-more { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 0.2rem; background: var(--chalk); border: 2px dashed var(--line); cursor: pointer; padding: 0.5rem; color: var(--slate); transition: all 0.2s ease; }
.st-preview-more:hover { border-color: var(--cobalt); color: var(--cobalt); }
.st-preview-more svg { width: 1rem; height: 1rem; }
.st-preview-more span { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.72rem; }

/* Progress */
.st-progress { margin-bottom: 1.1rem; }
.st-progress__label { display: flex; justify-content: space-between; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.78rem; color: var(--steel); margin-bottom: 0.4rem; }
.st-progress__value { color: var(--cobalt); font-weight: 600; }
.st-progress__track { width: 100%; height: 0.4rem; background: var(--mist); border-radius: 999px; overflow: hidden; }
.st-progress__bar { height: 100%; border-radius: 999px; transition: width 0.5s ease; background: var(--steel); }
.st-progress__bar--wait { background: var(--steel); }
.st-progress__bar--work { background: var(--work); }
.st-progress__bar--ship { background: var(--ship); }
.st-progress__bar--done { background: var(--done); }
.st-progress__bar--stop { background: var(--strike); }

/* Actions */
.st-order__actions { display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 0.75rem; padding-top: 1.1rem; border-top: 1px solid var(--line); }
.st-expand-btn { display: inline-flex; align-items: center; gap: 0.4rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.85rem; letter-spacing: 0.04em; text-transform: uppercase; color: var(--cobalt); background: transparent; border: none; cursor: pointer; padding: 0.4rem 0.6rem; }
.st-expand-btn svg { width: 1rem; height: 1rem; }
.st-order__buttons { display: flex; flex-wrap: wrap; gap: 0.5rem; }

.st-btn { display: inline-flex; align-items: center; gap: 0.4rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.82rem; letter-spacing: 0.04em; text-transform: uppercase; border: 1px solid var(--line); border-radius: 2px; padding: 0.55rem 1rem; cursor: pointer; text-decoration: none; background: var(--mist); color: var(--ink); transition: all 0.2s ease; }
.st-btn svg { width: 1rem; height: 1rem; }
.st-btn:hover { border-color: var(--ink); }
.st-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.st-btn--primary { background: var(--ink); border-color: var(--ink); color: var(--chalk); }
.st-btn--primary:hover { background: var(--strike); border-color: var(--strike); }
.st-btn--done { background: var(--done); border-color: var(--done); color: var(--chalk); }
.st-btn--work { background: #fff6e5; border-color: #fbd38d; color: var(--work); }

/* Expanded */
.st-order__expanded { margin-top: 1.25rem; padding-top: 1.25rem; border-top: 2px solid var(--ink); display: flex; flex-direction: column; gap: 1.5rem; }
.st-detail-section__title { display: flex; align-items: center; gap: 0.5rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 1rem; text-transform: uppercase; color: var(--ink); margin: 0 0 1rem; }
.st-detail-section__title svg { width: 1.2rem; height: 1.2rem; color: var(--steel); }

.st-items { display: flex; flex-direction: column; gap: 0.75rem; }
.st-item { display: flex; align-items: center; gap: 1rem; padding: 0.9rem; background: var(--mist); border: 1px solid var(--line); }
.st-item__img { width: 3.75rem; height: 3.75rem; object-fit: cover; border: 1px solid var(--line); flex-shrink: 0; }
.st-item__info { flex: 1; min-width: 0; }
.st-item__name { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 600; color: var(--ink); margin: 0 0 0.2rem; }
.st-item__code { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.8rem; color: var(--steel); margin: 0 0 0.4rem; }
.st-item__tags { display: flex; flex-wrap: wrap; gap: 0.35rem; margin-bottom: 0.3rem; }
.st-tag { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.68rem; letter-spacing: 0.04em; text-transform: uppercase; padding: 0.2rem 0.6rem; border-radius: 999px; background: var(--chalk); color: var(--slate); }
.st-tag--cobalt { background: #eef0ff; color: var(--cobalt); }
.st-tag--done { background: #eafbf1; color: var(--done); }
.st-item__qty { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.82rem; color: var(--slate); margin: 0; }
.st-item__price { text-align: right; white-space: nowrap; }
.st-item__unit { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.78rem; color: var(--steel); margin: 0; }
.st-item__total { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.05rem; color: var(--ink); margin: 0.2rem 0 0; }

.st-grid-2 { display: grid; grid-template-columns: repeat(auto-fit, minmax(14rem, 1fr)); gap: 1.25rem; }
.st-detail-block { background: var(--mist); padding: 1.25rem; }
.st-detail-block--accent { background: #fff1ee; }
.st-detail-block__title { display: flex; align-items: center; gap: 0.5rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.95rem; text-transform: uppercase; color: var(--ink); margin: 0 0 0.9rem; }
.st-detail-block__title svg { width: 1.1rem; height: 1.1rem; color: var(--steel); }
.st-detail-block__hint { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.78rem; color: var(--steel); margin: 0.5rem 0 0.1rem; }
.st-detail-block__value { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 600; color: var(--ink); margin: 0; }
.st-detail-block__value--accent { color: var(--strike); }
.st-detail-block__sub { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.85rem; color: var(--slate); margin: 0; }

.st-rows { display: flex; flex-direction: column; gap: 0.5rem; }
.st-row { display: flex; justify-content: space-between; gap: 1rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.85rem; }
.st-row__label { color: var(--slate); }
.st-row__value { color: var(--ink); font-weight: 600; }
.st-row__value--done { color: var(--done); }
.st-row--total { padding-top: 0.6rem; border-top: 1px solid rgba(16,20,26,0.15); font-weight: 700; color: var(--ink); }
.st-row__grand { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.25rem; color: var(--strike); }

/* Timeline */
.st-timeline { display: flex; flex-direction: column; }
.st-timeline__step { display: flex; align-items: flex-start; gap: 1rem; }
.st-timeline__rail { display: flex; flex-direction: column; align-items: center; }
.st-timeline__dot { width: 0.75rem; height: 0.75rem; border-radius: 50%; background: var(--line); }
.st-timeline__dot.is-done { background: var(--done); }
.st-timeline__line { width: 2px; height: 2rem; margin-top: 0.3rem; background: var(--line); }
.st-timeline__line.is-done { background: var(--done); }
.st-timeline__body { flex: 1; padding-bottom: 1rem; }
.st-timeline__head { display: flex; justify-content: space-between; gap: 1rem; }
.st-timeline__head h6 { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 600; color: var(--steel); margin: 0; }
.st-timeline__head h6.is-done { color: var(--done); }
.st-timeline__head span { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.8rem; color: var(--steel); }
.st-timeline__body p { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.82rem; color: var(--slate); margin: 0.2rem 0 0; }

/* Pager */
.st-pager { display: flex; justify-content: center; align-items: center; gap: 0.5rem; }
.st-pager__list { display: flex; gap: 0.35rem; }
.st-page-btn { min-width: 2.5rem; height: 2.5rem; display: grid; place-items: center; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.9rem; color: var(--slate); background: var(--chalk); border: 1px solid var(--line); border-radius: 2px; cursor: pointer; }
.st-page-btn svg { width: 1rem; height: 1rem; }
.st-page-btn.is-active { background: var(--ink); border-color: var(--ink); color: var(--chalk); }
.st-page-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* Empty */
.st-empty { text-align: center; background: var(--chalk); border: 1px dashed var(--line); padding: 4rem 1.5rem; }
.st-empty__icon { width: 4rem; height: 4rem; margin: 0 auto 1.25rem; color: var(--steel); }
.st-empty__title { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.5rem; text-transform: uppercase; color: var(--ink); margin: 0 0 0.6rem; }
.st-empty__hint { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; color: var(--slate); margin: 0 0 1.5rem; max-width: 30rem; margin-left: auto; margin-right: auto; }
.st-empty__actions { display: flex; flex-direction: column; align-items: center; gap: 0.75rem; }

@keyframes st-spin { to { transform: rotate(360deg); } }

/* Skeleton */
.st-order--skeleton { display: flex; flex-direction: column; gap: 1.25rem; }
.st-skeleton-row { display: flex; align-items: center; gap: 1rem; }
.st-skeleton { background: var(--line); border-radius: 4px; animation: st-pulse 1.4s ease-in-out infinite; }
.st-skeleton--icon { width: 3rem; height: 3rem; flex-shrink: 0; }
.st-skeleton-lines { flex: 1; display: flex; flex-direction: column; gap: 0.5rem; }
.st-skeleton--line-lg { height: 1.1rem; width: 60%; }
.st-skeleton--line-sm { height: 0.9rem; width: 40%; }
.st-skeleton--block { height: 6rem; }
@keyframes st-pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }

/* Focus */
.st-btn:focus-visible, .st-input:focus-visible, .st-select:focus-visible, .st-tab:focus-visible, .st-expand-btn:focus-visible, .st-page-btn:focus-visible, .st-refresh:focus-visible {
  outline: 3px solid var(--cobalt);
  outline-offset: 2px;
}

/* Responsive */
@media (max-width: 768px) {
  .st-head__jersey { opacity: 0.4; }
  .st-order__top { flex-direction: column; }
  .st-order__top-right { text-align: left; }
}

@media (prefers-reduced-motion: reduce) {
  .st-btn, .st-refresh, .st-progress__bar { transition: none; }
  .st-refresh svg.is-spinning, .st-skeleton { animation: none; }
}
</style>
