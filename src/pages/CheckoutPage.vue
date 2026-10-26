<script setup>
import { ref, computed, inject, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import StepIndicator from '../components/checkout/StepIndicator.vue'
import CustomerInfoStep from '../components/checkout/CustomerInfoStep.vue'
import VoucherStep from '../components/checkout/VoucherStep.vue'
import PaymentStep from '../components/checkout/PaymentStep.vue'
import { customerAPI } from '../api/customerAPI.js'
import { useCheckout } from '@/composables/useCheckout'
import { useCart } from '@/composables/useCart'

const router = useRouter()
const toast = useToast()

// Quy tắc phí vận chuyển dùng chung cho toàn bộ FE khách - xem src/utils/shipping.js
import { FREE_SHIPPING_FROM, calcShippingFee, amountToFreeShipping as tinhConThieu, calcFinalTotal } from '@/utils/shipping'

// Use real cart and checkout composables
const { cart: realCart, formatCurrency: realFormatCurrency, loadCart } = useCart()
const {
  updateCustomerInfo,
  updatePaymentMethod,
  applyVoucher,
  removeVoucher,
  processCheckout,
  getCurrentInvoiceId
} = useCheckout()

// Inject cart data from parent (App.vue) as fallback
const cartActions = inject('cartActions', {
  cart: ref([]),
  updateQuantity: () => {},
  removeFromCart: () => {},
  formatCurrency: (amount) => amount,
  totalPrice: ref(0),
  clearCart: () => {}
})

const cart = computed(() => realCart.value)

const cartItems = computed(() => cart.value?.items || [])

// Step management
const currentStep = ref(1)
const steps = [
  { label: 'Thông tin khách hàng', description: 'Thông tin giao hàng', icon: 'fas fa-user' },
  { label: 'Mã giảm giá', description: 'Áp dụng voucher ưu đãi', icon: 'fas fa-tag' },
  { label: 'Thanh toán', description: 'Chọn phương thức thanh toán', icon: 'fas fa-credit-card' }
]

// Form data
const customerInfo = ref({
  ten: '',
  soDienThoai: '',
  email: '',
  ghiChu: ''
})

const selectedAddress = ref(null)
const customerId = ref(null)
const voucherCode = ref('')
const discountAmount = ref(0)
const voucherId = ref(null)
const paymentMethod = ref('')
const isLoading = ref(false)

// Authentication check
const isAuthenticated = computed(() => Boolean(customerAPI.isAuthenticated()))

// Price calculations
const totalPrice = computed(() => {
  if (cartItems.value.length === 0) return 0

  return cartItems.value.reduce((sum, item) => {
    const price = item.giaBanHienTai || item.gia || 0
    return sum + (price * item.soLuong)
  }, 0)
})

const shippingFee = computed(() => calcShippingFee(totalPrice.value))

const amountToFreeShipping = computed(() => tinhConThieu(totalPrice.value))

const finalTotalPrice = computed(() => calcFinalTotal(totalPrice.value, discountAmount.value))

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

// Địa chỉ máy chủ ảnh - đổi trong file .env khi deploy
const API_BASE = import.meta.env?.VITE_API_BASE_URL || 'http://localhost:8080'

const getImageUrl = (item) => {
  const imageUrl = item.urlAnhSanPham || item.urlAnhDaiDien

  if (!imageUrl) return FALLBACK_IMAGE
  if (imageUrl.startsWith('http') || imageUrl.startsWith('data:')) return imageUrl
  if (imageUrl.startsWith('/')) return `${API_BASE}${imageUrl}`

  return `${API_BASE}/uploads/${imageUrl}`
}

const handleImageError = (event) => {
  if (event.target.dataset.fallbackApplied) return
  event.target.dataset.fallbackApplied = 'true'
  event.target.src = FALLBACK_IMAGE
}

// Format currency
const formatCurrency = computed(() => realFormatCurrency)

// Handle customer info updates
const handleCustomerInfoUpdate = (info) => {
  customerInfo.value = { ...info }
}

const handleAddressUpdate = (address) => {
  selectedAddress.value = address
}

const handleCustomerIdUpdate = (id) => {
  customerId.value = id
}

// Step navigation
const nextStep = () => {
  if (currentStep.value < steps.length) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

// Complete order
const completeOrder = async (orderData) => {
  isLoading.value = true
  try {
      const customerName = orderData.selectedAddress?.tenKhachHang || orderData.selectedAddress?.ten || orderData.customerInfo?.ten || customerInfo.value.ten
      const customerPhone = orderData.selectedAddress?.soDienThoai || orderData.customerInfo?.soDienThoai || customerInfo.value.soDienThoai
      const customerEmail = orderData.customerInfo?.email || customerInfo.value.email || ''
      const customerAddress = orderData.selectedAddress
        ? `${orderData.selectedAddress.diaChiCuThe}, ${orderData.selectedAddress.phuong}, ${orderData.selectedAddress.quan}, ${orderData.selectedAddress.thanhPho}`
        : (orderData.customerInfo?.diaChi || customerInfo.value.diaChi)

      // Kiểm tra dữ liệu bắt buộc
      if (!customerName?.trim()) {
        toast.error('Nhập tên người nhận để tiếp tục')
        return
      }
      if (!customerPhone?.trim()) {
        toast.error('Nhập số điện thoại để tiếp tục')
        return
      }
      if (!customerAddress?.trim()) {
        toast.error('Nhập địa chỉ giao hàng để tiếp tục')
        return
      }

      updateCustomerInfo({
        name: customerName,
        phone: customerPhone,
        email: customerEmail,
        address: customerAddress
      })

      updatePaymentMethod(orderData.paymentMethod)

      const result = await processCheckout()

      toast.success('Đặt hàng thành công!')

      if (customerAPI.isAuthenticated()) {
        router.push({
          path: '/account/purchase-history',
          query: {
            orderId: result.id || result.ma,
            paymentMethod: paymentMethod.value || 'COD',
            status: 'success'
          }
        })
      } else {
        setTimeout(() => {
          toast.info('Chuyển đến trang tra cứu đơn hàng...')
          router.push({
            path: '/orders',
            query: {
              orderCode: result.maHoaDon || result.ma || result.id,
              autoSearch: 'true'
            }
          })
        }, 1500)
      }
  } catch (err) {
    console.error('Error completing order:', err)
    toast.error('Đặt hàng chưa thành công. Kiểm tra lại thông tin rồi thử lại.')
  } finally {
    isLoading.value = false
  }
}

onMounted(async () => {
  currentStep.value = 1

  await loadCart()

  const invoiceId = getCurrentInvoiceId()

  if (!invoiceId) {
    if (cartItems.value.length > 0) {
      toast.error('Phiên giỏ hàng đã hết hạn. Thêm áo vào giỏ lại giúp bạn.')
    } else {
      toast.info('Giỏ hàng đang trống. Thêm áo để tiếp tục.')
    }
    router.push('/cart')
  }
})

// Watch for voucher changes to update checkout state
watch([voucherId, discountAmount], ([newVoucherId, newDiscountAmount]) => {
  if (newVoucherId && newDiscountAmount > 0) {
    let numericVoucherId = newVoucherId

    if (typeof newVoucherId === 'string') {
      numericVoucherId = parseInt(newVoucherId)
    }

    if (isNaN(numericVoucherId) || numericVoucherId === null || numericVoucherId === undefined) {
      console.error('Invalid voucher ID:', newVoucherId)
      removeVoucher()
      return
    }

    applyVoucher(numericVoucherId, newDiscountAmount)
  } else {
    removeVoucher()
  }
})

// Watch for route changes to reset step
watch(
  () => router.currentRoute.value.path,
  (newPath) => {
    if (newPath === '/checkout') {
      currentStep.value = 1
    }
  }
)
</script>

<template>
  <div class="st-page">
    <!-- Header + Breadcrumb -->
    <header class="st-head">
      <span class="st-head__jersey" aria-hidden="true">03</span>
      <div class="st-container">
        <nav class="st-crumb" aria-label="Đường dẫn">
          <router-link to="/">Trang chủ</router-link>
          <span aria-hidden="true">/</span>
          <router-link to="/cart">Giỏ hàng</router-link>
          <span aria-hidden="true">/</span>
          <span class="is-current">Thanh toán</span>
        </nav>

        <div class="st-eyebrow">
          <span class="st-eyebrow__bar"></span>
          <span>Hoàn tất đơn hàng</span>
        </div>
        <h1 class="st-display">THANH TOÁN</h1>
      </div>
    </header>

    <div class="st-container st-body">
      <!-- Step Progress Indicator -->
      <StepIndicator :current-step="currentStep" :steps="steps" class="st-steps" />

      <!-- Empty Cart -->
      <div v-if="cartItems.length === 0" class="st-empty">
        <p class="st-empty__title">Giỏ hàng đang trống</p>
        <p class="st-empty__hint">Chọn vài chiếc áo rồi quay lại đây để thanh toán.</p>
        <div class="st-empty__actions">
          <router-link to="/products" class="st-btn st-btn--primary">Xem áo thể thao</router-link>
          <router-link to="/" class="st-btn">Về trang chủ</router-link>
        </div>
      </div>

      <!-- Main Checkout -->
      <div v-else class="st-layout">
        <!-- Left: Step Content -->
        <div class="st-panel">
          <transition name="fade" mode="out-in">
            <CustomerInfoStep v-if="currentStep === 1" key="step1" v-model:customer-info="customerInfo"
              v-model:selected-address="selectedAddress" :is-authenticated="isAuthenticated"
              @next-step="nextStep" @prev-step="prevStep" @update:customer-info="handleCustomerInfoUpdate"
              @update:selected-address="handleAddressUpdate" @update:customer-id="handleCustomerIdUpdate" />

            <VoucherStep v-else-if="currentStep === 2" key="step2" v-model:voucher-code="voucherCode"
              v-model:discount-amount="discountAmount" v-model:voucher-id="voucherId" :total-price="totalPrice"
              :customer-id="customerId"
              @next-step="nextStep" @prev-step="prevStep" />

            <PaymentStep v-else-if="currentStep === 3" key="step3" v-model:payment-method="paymentMethod"
              :customer-info="customerInfo" :selected-address="selectedAddress" :cart-items="cartItems"
              :total-price="finalTotalPrice" :discount-amount="discountAmount" :voucher-code="voucherCode"
              :voucher-id="voucherId" @prev-step="prevStep" @complete-order="completeOrder" />
          </transition>
        </div>

        <!-- Right: Order Summary -->
        <aside class="st-summary">
          <div class="st-summary__inner">
            <h2 class="st-summary__title">Đơn hàng</h2>

            <!-- Items -->
            <ul class="st-items">
              <li v-for="item in cartItems" :key="item.id" class="st-item">
                <div class="st-item__thumb">
                  <img :src="getImageUrl(item)" :alt="item.tenSanPham" @error="handleImageError" />
                  <span class="st-item__qty">{{ item.soLuong }}</span>
                </div>
                <div class="st-item__info">
                  <p class="st-item__name">{{ item.tenSanPham }}</p>
                  <p class="st-item__variant">
                    <span v-if="item.tenKichCo">Size {{ item.tenKichCo }}</span>
                    <span v-if="item.tenKichCo && item.tenMauSac"> · </span>
                    <span v-if="item.tenMauSac">{{ item.tenMauSac }}</span>
                  </p>
                </div>
                <p class="st-item__price">
                  {{ formatCurrency((item.giaBanHienTai || item.gia) * item.soLuong) }}
                </p>
              </li>
            </ul>

            <!-- Totals -->
            <dl class="st-totals">
              <div>
                <dt>Tạm tính</dt>
                <dd>{{ formatCurrency(totalPrice) }}</dd>
              </div>
              <div>
                <dt>Phí vận chuyển</dt>
                <dd :class="{ 'is-free': shippingFee === 0 }">
                  {{ shippingFee === 0 ? 'Miễn phí' : formatCurrency(shippingFee) }}
                </dd>
              </div>
              <div v-if="discountAmount > 0" class="is-discount">
                <dt>Giảm giá</dt>
                <dd>-{{ formatCurrency(discountAmount) }}</dd>
              </div>
            </dl>

            <p v-if="amountToFreeShipping > 0" class="st-freeship">
              Mua thêm {{ formatCurrency(amountToFreeShipping) }} để được miễn phí vận chuyển.
            </p>

            <div class="st-grand">
              <span>Tổng cộng</span>
              <span class="st-grand__num">{{ formatCurrency(finalTotalPrice) }}</span>
            </div>

            <!-- Cam kết -->
            <div class="st-promise">
              <p class="st-promise__title">Cam kết STRIKER</p>
              <ul>
                <li>Thanh toán bảo mật</li>
                <li>Đổi trả trong 30 ngày</li>
                <li>Đổi size miễn phí lần đầu</li>
              </ul>
            </div>
          </div>
        </aside>
      </div>
    </div>

    <!-- Loading Overlay -->
    <div v-if="isLoading" class="st-overlay" role="status" aria-live="polite">
      <div class="st-overlay__box">
        <span class="st-spinner" aria-hidden="true"></span>
        <p class="st-overlay__title">Đang xử lý đơn hàng</p>
        <p class="st-overlay__hint">Giữ nguyên trang này cho đến khi xong.</p>
      </div>
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
  min-height: 100vh;
  background: var(--mist);
}

.st-container {
  width: 100%;
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 1.25rem;
}

/* ===== Header ===== */
.st-head {
  position: relative;
  overflow: hidden;
  background: var(--ink);
  background-image: repeating-linear-gradient(115deg, rgba(255, 255, 255, 0.022) 0 2px, transparent 2px 74px);
  padding: 1.5rem 0 2.25rem;
}

.st-head__jersey {
  position: absolute;
  top: 50%;
  right: 3rem;
  transform: translateY(-50%);
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: clamp(6rem, 14vw, 11rem);
  line-height: 0.8;
  color: transparent;
  -webkit-text-stroke: 2px rgba(255, 255, 255, 0.08);
  pointer-events: none;
  user-select: none;
}

.st-crumb {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.88rem;
  color: var(--steel);
}

.st-crumb a {
  color: var(--steel);
  text-decoration: none;
}

.st-crumb a:hover { color: var(--chalk); }
.st-crumb .is-current { color: var(--strike); font-weight: 600; }

.st-eyebrow {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.7rem;
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
  position: relative;
  z-index: 1;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: clamp(2.25rem, 5vw, 3.5rem);
  line-height: 0.95;
  text-transform: uppercase;
  color: var(--chalk);
  margin: 0;
}

/* ===== Body ===== */
.st-body { padding: 2rem 1.25rem 4rem; }

.st-steps { margin-bottom: 1.75rem; }

/* ===== Layout ===== */
.st-layout {
  display: grid;
  grid-template-columns: 1fr 21rem;
  gap: 1.5rem;
  align-items: start;
}

.st-panel {
  background: var(--chalk);
  border: 1px solid var(--line);
  border-top: 4px solid var(--ink);
  padding: 1.75rem;
}

/* ===== Tóm tắt đơn ===== */
.st-summary__inner {
  position: sticky;
  top: 1.5rem;
  background: var(--chalk);
  border: 1px solid var(--line);
  border-top: 4px solid var(--strike);
  padding: 1.5rem 1.25rem;
}

.st-summary__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.35rem;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 1.1rem;
  padding-bottom: 0.8rem;
  border-bottom: 2px solid var(--ink);
}

.st-items {
  list-style: none;
  margin: 0 0 1.25rem;
  padding: 0;
  max-height: 17rem;
  overflow-y: auto;
}

.st-item {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.6rem 0;
  border-bottom: 1px solid var(--line);
}

.st-item__thumb { position: relative; flex-shrink: 0; }

.st-item__thumb img {
  width: 2.8rem;
  height: 2.8rem;
  object-fit: cover;
  border: 1px solid var(--line);
  display: block;
}

.st-item__qty {
  position: absolute;
  top: -0.4rem;
  right: -0.4rem;
  min-width: 1.15rem;
  height: 1.15rem;
  display: grid;
  place-items: center;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.72rem;
  color: var(--chalk);
  background: var(--strike);
  border-radius: 50%;
  padding: 0 0.2rem;
}

.st-item__info { flex: 1; min-width: 0; }

.st-item__name {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 0.88rem;
  color: var(--ink);
  margin: 0 0 0.1rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.st-item__variant {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 0.78rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--steel);
  margin: 0;
}

.st-item__price {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 0.98rem;
  color: var(--ink);
  margin: 0;
  white-space: nowrap;
}

/* Tổng tiền */
.st-totals {
  margin: 0 0 0.9rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.9rem;
}

.st-totals > div {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.35rem 0;
}

.st-totals dt { color: var(--slate); margin: 0; }
.st-totals dd { color: var(--ink); font-weight: 600; margin: 0; }
.st-totals dd.is-free { color: var(--done); }
.st-totals .is-discount dt,
.st-totals .is-discount dd { color: var(--done); font-weight: 700; }

.st-freeship {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.82rem;
  color: var(--slate);
  background: var(--mist);
  border-left: 3px solid var(--strike);
  padding: 0.55rem 0.7rem;
  margin: 0 0 1rem;
}

.st-grand {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  padding-top: 0.9rem;
  border-top: 2px solid var(--ink);
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.95rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--slate);
}

.st-grand__num {
  font-weight: 800;
  font-size: 1.65rem;
  letter-spacing: 0;
  color: var(--strike);
}

/* Cam kết */
.st-promise {
  margin-top: 1.5rem;
  padding-top: 1.1rem;
  border-top: 1px solid var(--line);
}

.st-promise__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.82rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 0.6rem;
}

.st-promise ul {
  list-style: none;
  margin: 0;
  padding: 0;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.85rem;
  color: var(--slate);
}

.st-promise li {
  position: relative;
  padding-left: 1rem;
  margin-bottom: 0.35rem;
}

.st-promise li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0.5rem;
  width: 5px;
  height: 5px;
  background: var(--strike);
  transform: rotate(45deg);
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
  font-size: 1.7rem;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 0.4rem;
}

.st-empty__hint {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  color: var(--slate);
  margin: 0 0 1.5rem;
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

/* ===== Overlay ===== */
.st-overlay {
  position: fixed;
  inset: 0;
  display: grid;
  place-items: center;
  background: rgba(16, 20, 26, 0.75);
  z-index: 50;
  padding: 1rem;
}

.st-overlay__box {
  background: var(--chalk);
  border-top: 4px solid var(--strike);
  padding: 2.5rem 2rem;
  text-align: center;
  max-width: 22rem;
  width: 100%;
}

.st-spinner {
  display: block;
  width: 2.75rem;
  height: 2.75rem;
  margin: 0 auto 1.25rem;
  border: 3px solid var(--line);
  border-top-color: var(--strike);
  border-radius: 50%;
  animation: st-spin 0.8s linear infinite;
}

@keyframes st-spin {
  to { transform: rotate(360deg); }
}

.st-overlay__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.3rem;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 0.35rem;
}

.st-overlay__hint {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.88rem;
  color: var(--slate);
  margin: 0;
}

/* ===== Chuyển bước ===== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

/* ===== Focus bàn phím ===== */
.st-btn:focus-visible,
.st-crumb a:focus-visible {
  outline: 3px solid var(--cobalt);
  outline-offset: 2px;
}

/* ===== Responsive ===== */
@media (max-width: 1024px) {
  .st-layout { grid-template-columns: 1fr; }
  .st-summary__inner { position: static; }
  .st-head__jersey { opacity: 0.5; }
}

@media (max-width: 560px) {
  .st-panel { padding: 1.25rem 1rem; }
}

/* ===== Giảm chuyển động ===== */
@media (prefers-reduced-motion: reduce) {
  .fade-enter-active,
  .fade-leave-active,
  .st-btn { transition: none; }
  .st-spinner { animation: none; }
}
</style>