<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import { productAPI } from '../services/productAPI.js'

const router = useRouter()
const toast = useToast()

const loading = ref(false)
const error = ref('')
const discountedDetails = ref([])
const copiedCode = ref('')

// Mã giảm giá hiển thị cố định.
// Khi có API phiếu giảm giá, thay mảng này bằng dữ liệu từ bảng phieu_giam_gia.
const promoCodes = ref([
  { code: 'STRIKER20', desc: 'Giảm 20% cho đơn hàng đầu tiên' },
  { code: 'FREESHIP', desc: 'Miễn phí vận chuyển cho đơn từ 500.000đ' }
])

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

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price || 0)
}

const discountPercent = (original, discounted) => {
  if (!original || !discounted || discounted >= original) return 0
  return Math.round(((original - discounted) / original) * 100)
}

// Gom các sản phẩm đang giảm theo từng đợt giảm giá
const campaigns = computed(() => {
  const map = new Map()

  discountedDetails.value.forEach(detail => {
    const name = detail.tenDotGiamGia || 'Ưu đãi khác'
    const percent = discountPercent(detail.giaBan, detail.giaGiamGia)

    if (!map.has(name)) {
      map.set(name, { name, items: [], maxPercent: 0 })
    }

    const campaign = map.get(name)
    campaign.items.push(detail)
    if (percent > campaign.maxPercent) campaign.maxPercent = percent
  })

  return Array.from(map.values()).sort((a, b) => b.maxPercent - a.maxPercent)
})

// Mỗi sản phẩm chỉ hiện một lần, lấy biến thể giảm sâu nhất
const bestDeals = computed(() => {
  const map = new Map()

  discountedDetails.value.forEach(detail => {
    const productId = detail.idSanPham
    if (!productId) return

    const percent = discountPercent(detail.giaBan, detail.giaGiamGia)
    const existing = map.get(productId)

    if (!existing || percent > existing.percent) {
      map.set(productId, {
        productId,
        name: detail.tenSanPham,
        image: detail.anhSanPham || detail.urlAnh || FALLBACK_IMAGE,
        originalPrice: detail.giaBan,
        salePrice: detail.giaGiamGia,
        campaign: detail.tenDotGiamGia,
        percent
      })
    }
  })

  return Array.from(map.values()).sort((a, b) => b.percent - a.percent)
})

const topPercent = computed(() => {
  return bestDeals.value.reduce((max, deal) => Math.max(max, deal.percent), 0)
})

const loadDiscounts = async () => {
  try {
    loading.value = true
    error.value = ''

    const details = await productAPI.getAllProductDetailsWithDiscount()

    discountedDetails.value = (details || []).filter(
      detail => detail.hasDiscount && detail.giaGiamGia
    )
  } catch (err) {
    console.error('Error loading discounts:', err)
    error.value = 'Không tải được danh sách khuyến mãi. Bấm Tải lại để thử lại.'
    discountedDetails.value = []
  } finally {
    loading.value = false
  }
}

const copyCode = async (code) => {
  try {
    await navigator.clipboard.writeText(code)
    copiedCode.value = code
    toast.success(`Đã sao chép mã ${code}`)
    setTimeout(() => {
      if (copiedCode.value === code) copiedCode.value = ''
    }, 2000)
  } catch (err) {
    toast.error('Trình duyệt không cho sao chép. Bạn chép tay mã này nhé.')
  }
}

const goToProduct = (productId) => {
  router.push(`/product/${productId}`)
}

const goToProducts = () => {
  router.push('/products')
}

onMounted(() => {
  loadDiscounts()
})
</script>

<template>
  <div class="st-page">
    <!-- Hero -->
    <header class="st-hero">
      <div class="st-container">
        <div class="st-eyebrow">
          <span class="st-eyebrow__bar"></span>
          <span>Ưu đãi đang chạy</span>
        </div>

        <div class="st-hero__grid">
          <div>
            <h1 class="st-display">GIẢM GIÁ</h1>
            <p class="st-lead">
              Toàn bộ áo thể thao STRIKER đang được giảm giá, cập nhật theo các đợt khuyến mãi hiện hành.
            </p>
          </div>

          <div v-if="topPercent > 0" class="st-hero__badge">
            <span class="st-hero__num">{{ topPercent }}%</span>
            <span class="st-hero__label">Giảm sâu nhất</span>
          </div>
        </div>
      </div>
    </header>

    <div class="st-container st-body">
      <!-- Lỗi -->
      <div v-if="error" class="st-alert" role="alert">
        <span>{{ error }}</span>
        <button @click="loadDiscounts" class="st-btn">Tải lại</button>
      </div>

      <!-- Đang tải -->
      <div v-if="loading" class="st-loading">
        <span class="st-spinner" aria-hidden="true"></span>
        <p>Đang tải khuyến mãi...</p>
      </div>

      <template v-else>
        <!-- Các đợt giảm giá -->
        <section v-if="campaigns.length > 0" class="st-section">
          <h2 class="st-section__title">Đợt giảm giá</h2>
          <div class="st-campaigns">
            <article v-for="campaign in campaigns" :key="campaign.name" class="st-campaign">
              <span class="st-campaign__percent">-{{ campaign.maxPercent }}%</span>
              <h3 class="st-campaign__name">{{ campaign.name }}</h3>
              <p class="st-campaign__count">{{ campaign.items.length }} phiên bản áo đang giảm</p>
              <button @click="goToProducts" class="st-btn st-btn--primary">Xem sản phẩm</button>
            </article>
          </div>
        </section>

        <!-- Áo đang giảm -->
        <section class="st-section">
          <h2 class="st-section__title">Áo đang giảm giá</h2>

          <div v-if="bestDeals.length === 0" class="st-empty">
            <p class="st-empty__title">Chưa có đợt giảm giá nào</p>
            <p class="st-empty__hint">
              Khi shop mở đợt giảm giá mới, sản phẩm sẽ tự động hiện ở đây. Trong lúc chờ, ghé xem toàn bộ kho áo.
            </p>
            <button @click="goToProducts" class="st-btn st-btn--primary">Xem áo thể thao</button>
          </div>

          <div v-else class="st-deals">
            <article v-for="deal in bestDeals" :key="deal.productId" class="st-deal" @click="goToProduct(deal.productId)">
              <div class="st-deal__media">
                <img :src="deal.image" :alt="deal.name" @error="onImageError" />
                <span class="st-deal__tag">-{{ deal.percent }}%</span>
              </div>
              <div class="st-deal__body">
                <p v-if="deal.campaign" class="st-deal__campaign">{{ deal.campaign }}</p>
                <h3 class="st-deal__name">{{ deal.name }}</h3>
                <div class="st-deal__price">
                  <span class="st-deal__now">{{ formatPrice(deal.salePrice) }}</span>
                  <span class="st-deal__was">{{ formatPrice(deal.originalPrice) }}</span>
                </div>
              </div>
            </article>
          </div>
        </section>

        <!-- Mã giảm giá -->
        <section class="st-section">
          <h2 class="st-section__title">Mã giảm giá</h2>
          <div class="st-codes">
            <div v-for="promo in promoCodes" :key="promo.code" class="st-code">
              <div class="st-code__info">
                <p class="st-code__value">{{ promo.code }}</p>
                <p class="st-code__desc">{{ promo.desc }}</p>
              </div>
              <button @click="copyCode(promo.code)" class="st-btn st-btn--primary">
                {{ copiedCode === promo.code ? 'Đã chép' : 'Sao chép' }}
              </button>
            </div>
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
  --strike: #ff3b18;
  --cobalt: #1b3bff;
  --chalk: #ffffff;
  --mist: #eef1f5;
  --line: #dde3ea;
  --steel: #8c98a8;
  --slate: #4a5563;
  min-height: 100vh;
  background: var(--chalk);
}

.st-container {
  width: 100%;
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 1.25rem;
}

/* ===== Hero ===== */
.st-hero {
  background: var(--strike);
  color: var(--chalk);
  padding: 3rem 0 3.25rem;
  border-bottom: 5px solid var(--ink);
}

.st-eyebrow {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.85rem;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: var(--ink);
}

.st-eyebrow__bar {
  width: 2.5rem;
  height: 3px;
  background: var(--ink);
}

.st-hero__grid {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 2rem;
}

.st-display {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: clamp(2.75rem, 7vw, 5rem);
  line-height: 0.9;
  text-transform: uppercase;
  color: var(--chalk);
  margin: 0 0 0.7rem;
}

.st-lead {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 1.05rem;
  line-height: 1.55;
  color: rgba(255, 255, 255, 0.88);
  max-width: 34rem;
  margin: 0;
}

.st-hero__badge {
  text-align: center;
  background: var(--ink);
  padding: 1.1rem 1.75rem;
}

.st-hero__num {
  display: block;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: clamp(2.5rem, 6vw, 3.75rem);
  line-height: 1;
  color: var(--chalk);
}

.st-hero__label {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.78rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--strike);
}

/* ===== Body ===== */
.st-body { padding: 3rem 1.25rem 4rem; }

.st-alert {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  background: #fff1ee;
  border-left: 4px solid var(--strike);
  padding: 0.9rem 1.1rem;
  margin-bottom: 1.5rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.92rem;
  color: #9a2410;
}

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

/* ===== Section ===== */
.st-section { margin-bottom: 3rem; }

.st-section__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.6rem;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 1.25rem;
  padding-bottom: 0.7rem;
  border-bottom: 2px solid var(--ink);
}

/* ===== Đợt giảm giá ===== */
.st-campaigns {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(16rem, 1fr));
  gap: 1rem;
}

.st-campaign {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.5rem;
  background: var(--mist);
  border: 1px solid var(--line);
  border-top: 4px solid var(--strike);
  padding: 1.4rem 1.25rem;
}

.st-campaign__percent {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 2.75rem;
  line-height: 1;
  color: var(--strike);
}

.st-campaign__name {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.25rem;
  letter-spacing: 0.03em;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0;
}

.st-campaign__count {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.88rem;
  color: var(--slate);
  margin: 0 0 0.5rem;
}

/* ===== Áo đang giảm ===== */
.st-deals {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(14rem, 1fr));
  gap: 1.25rem;
}

.st-deal {
  background: var(--chalk);
  border: 1px solid var(--line);
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.st-deal:hover {
  border-color: var(--ink);
  transform: translateY(-3px);
}

.st-deal__media {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
  background: var(--mist);
}

.st-deal__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease;
}

.st-deal:hover .st-deal__media img { transform: scale(1.06); }

.st-deal__tag {
  position: absolute;
  top: 0;
  left: 0;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1rem;
  letter-spacing: 0.04em;
  color: var(--chalk);
  background: var(--strike);
  padding: 0.3rem 0.65rem;
}

.st-deal__body { padding: 0.9rem 1rem 1.1rem; }

.st-deal__campaign {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.75rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--strike);
  margin: 0 0 0.3rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.st-deal__name {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 0.98rem;
  line-height: 1.35;
  color: var(--ink);
  margin: 0 0 0.6rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.st-deal__price {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.st-deal__now {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.3rem;
  color: var(--strike);
}

.st-deal__was {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.85rem;
  color: var(--steel);
  text-decoration: line-through;
}

/* ===== Mã giảm giá ===== */
.st-codes {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(19rem, 1fr));
  gap: 1rem;
}

.st-code {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  background: var(--chalk);
  border: 2px dashed var(--line);
  border-left: 4px solid var(--ink);
  padding: 1.1rem 1.25rem;
}

.st-code__value {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.35rem;
  letter-spacing: 0.1em;
  color: var(--ink);
  margin: 0 0 0.2rem;
}

.st-code__desc {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.85rem;
  color: var(--slate);
  margin: 0;
}

/* ===== Trạng thái rỗng ===== */
.st-empty {
  text-align: center;
  border: 1px dashed var(--line);
  padding: 3.5rem 1.5rem;
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
  max-width: 30rem;
  margin: 0 auto 1.5rem;
}

/* ===== Nút ===== */
.st-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.92rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.6rem 1.25rem;
  cursor: pointer;
  background: var(--chalk);
  color: var(--ink);
  transition: all 0.2s ease;
  white-space: nowrap;
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
.st-deal:focus-visible {
  outline: 3px solid var(--cobalt);
  outline-offset: 2px;
}

/* ===== Responsive ===== */
@media (max-width: 640px) {
  .st-hero__grid { flex-direction: column; align-items: flex-start; }
  .st-code { flex-direction: column; align-items: flex-start; }
  .st-deals { grid-template-columns: repeat(auto-fill, minmax(12rem, 1fr)); }
}

/* ===== Giảm chuyển động ===== */
@media (prefers-reduced-motion: reduce) {
  .st-deal,
  .st-deal__media img,
  .st-btn { transition: none; }
  .st-deal:hover { transform: none; }
  .st-deal:hover .st-deal__media img { transform: none; }
  .st-spinner { animation: none; }
}
</style>