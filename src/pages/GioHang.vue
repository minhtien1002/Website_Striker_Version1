<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCart } from '@/composables/useCart'
import { useCheckout } from '@/composables/useCheckout'
import { useNotification } from '@/composables/useNotification'

const router = useRouter()
const { cart, loading, loadCart, updateQuantity, removeItem, formatCurrency } = useCart()
const { getCurrentInvoiceId } = useCheckout()
const { error } = useNotification()

// Giữ trùng với trang Thanh toán
const FREE_SHIPPING_FROM = 500000
const SHIPPING_FEE = 30000

const cartItems = computed(() => cart.value?.items || [])

const subtotal = computed(() => {
  return cartItems.value.reduce((sum, item) => {
    const price = item.giaBanHienTai || item.gia || 0
    return sum + price * item.soLuong
  }, 0)
})

const totalQuantity = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + (item.soLuong || 0), 0)
})

const shippingFee = computed(() => {
  return subtotal.value >= FREE_SHIPPING_FROM ? 0 : SHIPPING_FEE
})

const amountToFreeShipping = computed(() => {
  return Math.max(0, FREE_SHIPPING_FROM - subtotal.value)
})

const total = computed(() => subtotal.value + shippingFee.value)

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

const proceedToCheckout = () => {
  if (cartItems.value.length === 0) {
    error('Giỏ hàng đang trống, chưa thanh toán được.')
    return
  }

  const invoiceId = getCurrentInvoiceId()
  if (!invoiceId) {
    error('Phiên giỏ hàng đã hết hạn. Thêm áo vào giỏ lại giúp bạn.')
    return
  }

  router.push('/checkout')
}

onMounted(async () => {
  await loadCart()
})
</script>

<template>
  <div class="st-page">
    <!-- Header -->
    <header class="st-head">
      <span class="st-head__jersey" aria-hidden="true">00</span>
      <div class="st-container">
        <div class="st-eyebrow">
          <span class="st-eyebrow__bar"></span>
          <span>Giỏ hàng</span>
        </div>
        <h1 class="st-display">GIỎ HÀNG</h1>
        <p class="st-lead">
          {{ totalQuantity }} áo đang chờ trong giỏ
        </p>
      </div>
    </header>

    <div class="st-container st-body">
      <!-- Đang tải -->
      <div v-if="loading" class="st-loading">
        <span class="st-spinner" aria-hidden="true"></span>
        <p>Đang tải giỏ hàng...</p>
      </div>

      <!-- Giỏ trống -->
      <div v-else-if="cartItems.length === 0" class="st-empty">
        <p class="st-empty__title">Giỏ hàng đang trống</p>
        <p class="st-empty__hint">Chọn vài chiếc áo rồi quay lại đây để đặt hàng.</p>
        <router-link to="/products" class="st-btn st-btn--primary">Xem áo thể thao</router-link>
      </div>

      <!-- Nội dung giỏ -->
      <div v-else class="st-layout">
        <!-- Danh sách áo -->
        <ul class="st-items">
          <li v-for="item in cartItems" :key="item.id" class="st-item">
            <img :src="item.urlAnhSanPham || FALLBACK_IMAGE" :alt="item.tenSanPham" class="st-item__img"
              @error="onImageError" />

            <div class="st-item__info">
              <p class="st-item__brand">{{ item.tenThuongHieu }}</p>
              <h2 class="st-item__name">{{ item.tenSanPham }}</h2>
              <p class="st-item__variant">
                <span v-if="item.tenKichCo">Size {{ item.tenKichCo }}</span>
                <span v-if="item.tenKichCo && item.tenMauSac"> · </span>
                <span v-if="item.tenMauSac">{{ item.tenMauSac }}</span>
              </p>

              <div class="st-item__row">
                <div class="st-qty">
                  <button @click="updateQuantity(item.id, item.soLuong - 1)" :disabled="item.soLuong <= 1"
                    aria-label="Giảm số lượng">−</button>
                  <span>{{ item.soLuong }}</span>
                  <button @click="updateQuantity(item.id, item.soLuong + 1)" aria-label="Tăng số lượng">+</button>
                </div>

                <div class="st-item__money">
                  <p class="st-item__sum">
                    {{ formatCurrency((item.giaBanHienTai || item.gia) * item.soLuong) }}
                  </p>
                  <p class="st-item__unit">
                    {{ formatCurrency(item.giaBanHienTai || item.gia) }} / áo
                  </p>
                </div>
              </div>
            </div>

            <button @click="removeItem(item.id)" class="st-remove" :aria-label="`Xóa ${item.tenSanPham} khỏi giỏ`">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16">
                </path>
              </svg>
            </button>
          </li>
        </ul>

        <!-- Tóm tắt -->
        <aside class="st-summary">
          <div class="st-summary__inner">
            <h2 class="st-summary__title">Tóm tắt đơn hàng</h2>

            <dl class="st-totals">
              <div>
                <dt>Tạm tính</dt>
                <dd>{{ formatCurrency(subtotal) }}</dd>
              </div>
              <div>
                <dt>Phí vận chuyển</dt>
                <dd :class="{ 'is-free': shippingFee === 0 }">
                  {{ shippingFee === 0 ? 'Miễn phí' : formatCurrency(shippingFee) }}
                </dd>
              </div>
            </dl>

            <p v-if="amountToFreeShipping > 0" class="st-freeship">
              Mua thêm {{ formatCurrency(amountToFreeShipping) }} để được miễn phí vận chuyển.
            </p>

            <div class="st-grand">
              <span>Tổng cộng</span>
              <span class="st-grand__num">{{ formatCurrency(total) }}</span>
            </div>

            <p class="st-note">Mã giảm giá được áp dụng ở bước thanh toán.</p>

            <button @click="proceedToCheckout" class="st-btn st-btn--primary st-btn--block">
              Thanh toán
            </button>

            <router-link to="/products" class="st-btn st-btn--block">
              Tiếp tục mua sắm
            </router-link>

            <p class="st-secure">Thanh toán an toàn và bảo mật</p>
          </div>
        </aside>
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

.st-eyebrow {
  position: relative;
  z-index: 1;
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
  position: relative;
  z-index: 1;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: clamp(2.25rem, 5vw, 3.5rem);
  line-height: 0.95;
  text-transform: uppercase;
  color: var(--chalk);
  margin: 0 0 0.6rem;
}

.st-lead {
  position: relative;
  z-index: 1;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 1rem;
  color: var(--steel);
  margin: 0;
}

/* ===== Body ===== */
.st-body { padding: 2rem 1.25rem 4rem; }

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

@keyframes st-spin { to { transform: rotate(360deg); } }

.st-loading p { margin: 0; }

.st-layout {
  display: grid;
  grid-template-columns: 1fr 21rem;
  gap: 1.5rem;
  align-items: start;
}

/* ===== Danh sách áo ===== */
.st-items {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.st-item {
  position: relative;
  display: flex;
  gap: 1rem;
  background: var(--chalk);
  border: 1px solid var(--line);
  border-left: 4px solid var(--ink);
  padding: 1rem;
}

.st-item__img {
  width: 6rem;
  height: 6rem;
  object-fit: cover;
  border: 1px solid var(--line);
  flex-shrink: 0;
}

.st-item__info { flex: 1; min-width: 0; }

.st-item__brand {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.75rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--strike);
  margin: 0 0 0.2rem;
}

.st-item__name {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 1.05rem;
  color: var(--ink);
  margin: 0 0 0.2rem;
  padding-right: 2rem;
}

.st-item__variant {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 0.82rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--steel);
  margin: 0 0 0.9rem;
}

.st-item__row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

/* Bộ chỉnh số lượng */
.st-qty {
  display: inline-flex;
  align-items: stretch;
  border: 1px solid var(--line);
}

.st-qty button {
  width: 2.2rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 1.15rem;
  color: var(--ink);
  background: var(--chalk);
  border: none;
  cursor: pointer;
  transition: background 0.2s ease;
}

.st-qty button:hover:not(:disabled) { background: var(--ink); color: var(--chalk); }

.st-qty button:disabled { color: var(--line); cursor: not-allowed; }

.st-qty span {
  min-width: 2.6rem;
  display: grid;
  place-items: center;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 1rem;
  color: var(--ink);
  background: var(--mist);
  border-left: 1px solid var(--line);
  border-right: 1px solid var(--line);
}

.st-item__money { text-align: right; }

.st-item__sum {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.35rem;
  color: var(--ink);
  margin: 0;
}

.st-item__unit {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.8rem;
  color: var(--steel);
  margin: 0;
}

.st-remove {
  position: absolute;
  top: 0.9rem;
  right: 0.9rem;
  display: grid;
  place-items: center;
  width: 1.9rem;
  height: 1.9rem;
  color: var(--steel);
  background: none;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.st-remove svg { width: 1.1rem; height: 1.1rem; }

.st-remove:hover {
  color: var(--strike);
  border-color: var(--strike);
}

/* ===== Tóm tắt ===== */
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

.st-totals {
  margin: 0 0 0.9rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.92rem;
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

.st-note {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.8rem;
  color: var(--steel);
  margin: 0.9rem 0 1.25rem;
}

.st-secure {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 0.78rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--steel);
  text-align: center;
  margin: 1.25rem 0 0;
  padding-top: 1rem;
  border-top: 1px solid var(--line);
}

/* ===== Trạng thái rỗng ===== */
.st-empty {
  text-align: center;
  background: var(--chalk);
  border: 1px dashed var(--line);
  padding: 4.5rem 1.5rem;
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

/* ===== Nút ===== */
.st-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 1rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.8rem 1.5rem;
  cursor: pointer;
  text-decoration: none;
  background: var(--chalk);
  color: var(--ink);
  transition: all 0.2s ease;
}

.st-btn:hover { border-color: var(--ink); }

.st-btn--block { width: 100%; }
.st-btn--block + .st-btn--block { margin-top: 0.5rem; }

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
.st-qty button:focus-visible,
.st-remove:focus-visible {
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
  .st-item { flex-direction: column; }
  .st-item__img { width: 100%; height: 11rem; }
  .st-item__row { flex-direction: column; align-items: flex-start; }
  .st-item__money { text-align: left; }
}

/* ===== Giảm chuyển động ===== */
@media (prefers-reduced-motion: reduce) {
  .st-btn, .st-qty button, .st-remove { transition: none; }
  .st-spinner { animation: none; }
}
</style>