<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import { productAPI, imageUrl } from '../services/productAPI.js'
import { cartService } from '../services/cartAPI.js'
import { useConfirm } from '@/composables/useConfirm.js'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { confirm } = useConfirm()

const loading = ref(true)
const notFound = ref(false)
const info = ref(null)        // thông tin chung (SanPhamResponse)
const variants = ref([])      // mọi biến thể đang hoạt động, kể cả hết hàng
const selectedColor = ref(null)
const selectedSizeId = ref(null)
const quantity = ref(1)
const adding = ref(false)

const SIZE_ORDER = ['XS', 'S', 'M', 'L', 'XL', 'XXL', '2XL', '3XL', '4XL', '5XL']
const sizeRank = (s) => { const i = SIZE_ORDER.indexOf(String(s).toUpperCase()); return i === -1 ? 99 : i }
const money = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0)

/** Chỉ hiện màu còn ít nhất một size còn hàng; màu hết mọi size bị ẩn. */
const colors = computed(() => {
  const m = new Map()
  variants.value.forEach((v) => {
    if (!m.has(v.idMauSac)) m.set(v.idMauSac, { id: v.idMauSac, name: v.tenMauSac, stock: 0 })
    m.get(v.idMauSac).stock += v.soLuongTonKho || 0
  })
  return [...m.values()].filter((c) => c.stock > 0)
})
const hetHang = computed(() => !loading.value && colors.value.length === 0)

/** Mọi size của màu đang chọn; size hết hàng vẫn hiện nhưng không chọn được. */
const sizes = computed(() => variants.value
  .filter((v) => v.idMauSac === selectedColor.value?.id)
  .map((v) => ({ id: v.idKichCo, name: v.tenKichCo, stock: v.soLuongTonKho || 0, variant: v }))
  .sort((a, b) => sizeRank(a.name) - sizeRank(b.name)))

const selectedVariant = computed(() => sizes.value.find((s) => s.id === selectedSizeId.value)?.variant || null)
const price = computed(() => {
  const v = selectedVariant.value
  if (v) return { now: v.hasDiscount && v.giaGiamGia ? v.giaGiamGia : v.giaBan, was: v.hasDiscount ? v.giaBan : null, deal: v.tenDotGiamGia }
  const ps = variants.value.map((x) => (x.hasDiscount && x.giaGiamGia ? x.giaGiamGia : x.giaBan)).filter(Boolean)
  return { now: ps.length ? Math.min(...ps) : 0, was: null, deal: null, from: ps.length > 1 }
})
const image = computed(() => imageUrl(selectedVariant.value?.urlAnhSanPham || info.value?.urlAnhDaiDien))

const selectColor = (c) => {
  selectedColor.value = c
  const first = sizes.value.find((s) => s.stock > 0)
  selectedSizeId.value = first ? first.id : null
  quantity.value = 1
}
const selectSize = (s) => { if (s.stock > 0) { selectedSizeId.value = s.id; quantity.value = 1 } }
watch(quantity, (q) => {
  const max = selectedVariant.value?.soLuongTonKho || 1
  if (!Number.isInteger(q) || q < 1) quantity.value = 1
  else if (q > max) quantity.value = max
})

const addToCart = async (goToCart) => {
  const v = selectedVariant.value
  if (!v) { toast.info('Vui lòng chọn màu và size'); return }
  const ok = await confirm({
    title: goToCart ? 'Mua ngay sản phẩm này?' : 'Thêm vào giỏ hàng?',
    details: [`${info.value.tenSanPham}`, `Mã: ${v.ma} · ${v.tenMauSac} / ${v.tenKichCo}`, `Số lượng: ${quantity.value}`, `Đơn giá: ${money(price.value.now)}`],
    confirmText: goToCart ? 'Mua ngay' : 'Thêm vào giỏ'
  })
  if (!ok) return
  adding.value = true
  try {
    await cartService.addToCart(v.id, quantity.value, price.value.now)
    toast.success('Đã thêm vào giỏ hàng')
    if (goToCart) router.push('/cart')
  } catch (e) {
    toast.error(e?.response?.data || e.message || 'Chưa thêm được vào giỏ hàng')
  } finally {
    adding.value = false
  }
}

const load = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const [p, d] = await Promise.all([productAPI.getProductInfo(id), productAPI.getProductDetailsWithDiscount(id)])
    info.value = p
    variants.value = d
    if (colors.value.length) selectColor(colors.value[0])
  } catch (e) {
    notFound.value = true
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="cx-page">
    <p class="cx-hint" style="margin-bottom:12px">
      <router-link to="/">Trang chủ</router-link> / <router-link to="/products">Sản phẩm</router-link> / {{ info?.tenSanPham }}
    </p>

    <div v-if="loading" class="cx-panel cx-empty">Đang tải thông tin sản phẩm...</div>
    <div v-else-if="notFound" class="cx-panel cx-empty">
      Sản phẩm không tồn tại hoặc đã ngừng kinh doanh.
      <div style="margin-top:12px"><router-link class="cx-btn" to="/products">Xem sản phẩm khác</router-link></div>
    </div>

    <div v-else class="cx-grid">
      <div class="cx-panel">
        <img :src="image" :alt="info.tenSanPham" style="width:100%;aspect-ratio:1/1;object-fit:cover;border:1px solid var(--c-line)" />
      </div>

      <div>
        <div class="cx-panel">
          <p class="cx-muted" style="margin:0">{{ info.tenThuongHieu }} · {{ info.tenDanhMuc }}</p>
          <h1 class="cx-title">{{ info.tenSanPham }}</h1>
          <p class="cx-hint">Mã sản phẩm: {{ info.ma }}<span v-if="selectedVariant"> · Mã chi tiết: {{ selectedVariant.ma }}</span> · Đã bán {{ info.tongSoLuongDaBan || 0 }}</p>

          <div style="margin:12px 0;display:flex;gap:10px;align-items:baseline;flex-wrap:wrap">
            <span class="cx-accent" style="font-size:1.6rem;font-weight:800">{{ price.from ? 'Từ ' : '' }}{{ money(price.now) }}</span>
            <span v-if="price.was" class="cx-muted" style="text-decoration:line-through">{{ money(price.was) }}</span>
            <span v-if="price.deal" class="cx-badge on">{{ price.deal }}</span>
          </div>

          <div v-if="hetHang" class="cx-panel" style="background:var(--c-muted);margin:0">
            <strong>Hết hàng</strong> - sản phẩm tạm thời đã bán hết mọi màu và size.
          </div>

          <template v-else>
            <div class="cx-field">
              <label>Màu sắc: <span class="cx-muted">{{ selectedColor?.name }}</span></label>
              <div style="display:flex;gap:6px;flex-wrap:wrap">
                <button v-for="c in colors" :key="c.id" class="cx-chip" :class="{ on: selectedColor?.id === c.id }" @click="selectColor(c)">{{ c.name }}</button>
              </div>
            </div>

            <div class="cx-field">
              <label>Kích cỡ</label>
              <div style="display:flex;gap:6px;flex-wrap:wrap">
                <button v-for="s in sizes" :key="s.id" class="cx-chip" :class="{ on: selectedSizeId === s.id }" :disabled="s.stock === 0"
                        :title="s.stock === 0 ? 'Hết hàng' : `Còn ${s.stock}`" @click="selectSize(s)">
                  {{ s.name }}<span v-if="s.stock === 0" style="margin-left:4px;font-size:.75rem">(hết hàng)</span>
                </button>
              </div>
              <span class="cx-hint" v-if="selectedVariant">Còn {{ selectedVariant.soLuongTonKho }} sản phẩm</span>
            </div>

            <div class="cx-field" style="max-width:200px">
              <label>Số lượng</label>
              <div style="display:flex">
                <button class="cx-btn sm" :disabled="quantity <= 1" @click="quantity--">−</button>
                <input v-model.number="quantity" type="number" min="1" :max="selectedVariant?.soLuongTonKho || 1" class="cx-input" style="text-align:center" />
                <button class="cx-btn sm" :disabled="quantity >= (selectedVariant?.soLuongTonKho || 1)" @click="quantity++">+</button>
              </div>
            </div>

            <div style="display:flex;gap:8px;margin-top:8px">
              <button class="cx-btn primary" :disabled="!selectedVariant || adding" @click="addToCart(true)">Mua ngay</button>
              <button class="cx-btn" :disabled="!selectedVariant || adding" @click="addToCart(false)">Thêm vào giỏ hàng</button>
            </div>
          </template>
        </div>

        <div class="cx-panel">
          <h3>Thông tin sản phẩm</h3>
          <dl class="cx-sum" style="grid-template-columns:160px 1fr">
            <dt>Mã sản phẩm</dt><dd style="text-align:left">{{ info.ma }}</dd>
            <dt>Thương hiệu</dt><dd style="text-align:left">{{ info.tenThuongHieu || '—' }}</dd>
            <dt>Thể loại</dt><dd style="text-align:left">{{ info.tenDanhMuc || '—' }}</dd>
            <dt>Chất liệu</dt><dd style="text-align:left">{{ info.tenChatLieu || '—' }}</dd>
            <dt>Màu còn hàng</dt><dd style="text-align:left">{{ colors.map((c) => c.name).join(', ') || '—' }}</dd>
          </dl>
          <p style="white-space:pre-line;margin-top:12px">{{ info.moTa || 'Chưa có mô tả.' }}</p>
        </div>

        <div class="cx-panel">
          <h3>Giao hàng</h3>
          <p class="cx-hint" style="margin:0">Phí vận chuyển được tính theo địa chỉ giao hàng của bạn ở bước thanh toán.</p>
        </div>
      </div>
    </div>
  </div>
</template>
