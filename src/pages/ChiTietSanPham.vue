<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import { productAPI, mapBackendToFrontend } from '../services/productAPI.js'
import { cartService } from '../services/cartAPI.js'
import { useStockUpdates } from '@/composables/useStockUpdates'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { watchStock } = useStockUpdates()

// Ngưỡng miễn phí vận chuyển - giữ trùng với trang Thanh toán
const FREE_SHIPPING_FROM = 500000

const selectedImage = ref('')
const selectedSize = ref(null)
const selectedColor = ref(null)
const quantity = ref(1)
const loading = ref(true)
const isAdding = ref(false)

// Ảnh dự phòng khi ảnh áo bị lỗi
const FALLBACK_IMAGE =
  'data:image/svg+xml;charset=UTF-8,' +
  encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 400">
      <rect width="400" height="400" fill="#eef1f5"/>
      <path d="M150 130 L118 146 L130 184 L148 178 L148 268 L252 268 L252 178 L270 184 L282 146 L250 130 L224 141 A26 18 0 0 1 176 141 Z"
            fill="none" stroke="#ff3b18" stroke-width="5" stroke-linejoin="round"/>
      <text x="200" y="316" fill="#8c98a8" font-family="Arial, sans-serif" font-size="18"
            font-weight="bold" letter-spacing="5" text-anchor="middle">STRIKER</text>
    </svg>
  `)

const onImageError = (event) => {
  if (event.target.dataset.fallbackApplied) return
  event.target.dataset.fallbackApplied = 'true'
  event.target.src = FALLBACK_IMAGE
}

// Product data from API
const product = ref({
  id: null,
  name: '',
  brand: '',
  category: '',
  description: '',
  features: [],
  images: [],
  colors: [],
  sizes: [],
  isFavorite: false
})

// Product details from API
const productDetails = ref([])

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price || 0)
}

// Computed property for button text based on action
const addToCartButtonText = computed(() => {
  const action = route.query.action
  const baseText = action === 'buy-now' ? 'Mua ngay' : 'Thêm vào giỏ hàng'

  if (isAdding.value) return 'Đang thêm...'
  if (!selectedColor.value) return 'Chọn màu áo'
  if (!selectedSize.value) return 'Chọn size'
  if (selectedSize.value.stock === 0) return 'Hết hàng'
  return baseText
})

// Chỉ hiện size còn hàng theo màu đang chọn
const availableSizesForColor = computed(() => {
  if (!selectedColor.value || !productDetails.value.length) {
    return product.value.sizes || []
  }

  const colorDetails = productDetails.value.filter(detail => {
    const mappedDetail = mapBackendToFrontend.productDetail(detail)
    return mappedDetail.colorId === selectedColor.value.id
  })

  return colorDetails.map(detail => {
    const mappedDetail = mapBackendToFrontend.productDetail(detail)
    return {
      size: mappedDetail.size,
      sizeId: mappedDetail.sizeId,
      stock: mappedDetail.stock,
      price: mappedDetail.price
    }
  })
})

const selectedVariant = computed(() => {
  const selectedDetail = productDetails.value.find(detail => {
    const mappedDetail = mapBackendToFrontend.productDetail(detail)
    return mappedDetail.colorId === selectedColor.value?.id &&
           mappedDetail.sizeId === selectedSize.value?.sizeId
  })

  if (selectedDetail) {
    const mappedDetail = mapBackendToFrontend.productDetail(selectedDetail)
    return {
      price: mappedDetail.price,
      giaGiamGia: mappedDetail.giaGiamGia,
      tenDotGiamGia: mappedDetail.tenDotGiamGia,
      hasDiscount: mappedDetail.hasDiscount,
      stock: mappedDetail.stock
    }
  }

  return {
    price: selectedSize.value?.price || product.value.sizes[0]?.price || 0,
    giaGiamGia: null,
    tenDotGiamGia: null,
    hasDiscount: false,
    stock: 0
  }
})

const currentPrice = computed(() => {
  return selectedVariant.value.hasDiscount && selectedVariant.value.giaGiamGia
    ? selectedVariant.value.giaGiamGia
    : selectedVariant.value.price
})

// stayOnPage = true: chỉ thêm vào giỏ, ở lại trang (nút phụ)
const addToCart = async (stayOnPage = false) => {
  if (!selectedColor.value || !selectedSize.value || selectedSize.value.stock === 0) return

  try {
    isAdding.value = true

    const selectedDetail = productDetails.value.find(detail => {
      const mappedDetail = mapBackendToFrontend.productDetail(detail)
      return mappedDetail.colorId === selectedColor.value.id &&
             mappedDetail.sizeId === selectedSize.value.sizeId
    })

    if (!selectedDetail) {
      toast.error('Không tìm thấy phiên bản áo này. Chọn lại màu và size giúp bạn.')
      return
    }

    const mappedDetail = mapBackendToFrontend.productDetail(selectedDetail)

    const finalPrice = mappedDetail.hasDiscount && mappedDetail.giaGiamGia
      ? mappedDetail.giaGiamGia
      : mappedDetail.price

    await cartService.addToCart(mappedDetail.id, quantity.value, finalPrice)

    await new Promise(resolve => setTimeout(resolve, 500))

    const action = route.query.action
    if (stayOnPage === true) {
      toast.success('Đã thêm áo vào giỏ hàng', {
        position: 'top-center',
        timeout: 2000
      })
    } else if (action === 'buy-now') {
      toast.success('Đã thêm vào giỏ. Chuyển đến thanh toán...', {
        position: 'top-center',
        timeout: 2000
      })
      setTimeout(() => router.push('/checkout'), 1000)
    } else {
      toast.success('Đã thêm áo vào giỏ hàng', {
        position: 'top-center',
        timeout: 2000
      })
      setTimeout(() => router.push('/cart'), 1000)
    }
  } catch (error) {
    console.error('Lỗi khi thêm vào giỏ hàng:', error)
    toast.error('Chưa thêm được vào giỏ. Thử lại giúp bạn.', {
      position: 'top-center',
      timeout: 3000
    })
  } finally {
    isAdding.value = false
  }
}

// Handle color selection
const selectColor = (color) => {
  selectedColor.value = color
  selectedSize.value = null

  const availableSizes = availableSizesForColor.value.filter(size => size.stock > 0)
  if (availableSizes.length > 0) {
    selectSize(availableSizes[0])
  }
}

// Handle size selection
const selectSize = (size) => {
  selectedSize.value = size
  quantity.value = 1
}

// Chọn số lượng, không vượt quá tồn kho của size đang chọn
const increaseQuantity = () => {
  const stock = selectedSize.value?.stock || 0
  if (quantity.value < stock) quantity.value++
}

const decreaseQuantity = () => {
  if (quantity.value > 1) quantity.value--
}

const onQuantityInput = (event) => {
  const stock = selectedSize.value?.stock || 0
  let value = parseInt(event.target.value, 10)
  if (Number.isNaN(value) || value < 1) value = 1
  if (value > stock) value = stock
  quantity.value = value
}

// Load product data from API
const loadProductData = async () => {
  try {
    loading.value = true
    const productId = route.params.id

    const detailsResponse = await productAPI.getProductDetails(productId)
    productDetails.value = detailsResponse

    // Bổ sung thông tin giảm giá
    try {
      const discountDetails = await productAPI.getProductDetailsWithDiscount(productId)

      const discountMap = new Map()
      discountDetails.forEach(detail => {
        discountMap.set(detail.id, {
          giaGiamGia: detail.giaGiamGia,
          tenDotGiamGia: detail.tenDotGiamGia,
          hasDiscount: detail.hasDiscount
        })
      })

      productDetails.value.forEach(detail => {
        const discountInfo = discountMap.get(detail.id)
        if (discountInfo && discountInfo.hasDiscount && discountInfo.giaGiamGia) {
          detail.giaGiamGia = discountInfo.giaGiamGia
          detail.tenDotGiamGia = discountInfo.tenDotGiamGia
          detail.hasDiscount = discountInfo.hasDiscount
        }
      })
    } catch (discountError) {
      console.warn('Failed to enrich product details with discount:', discountError)
    }

    if (detailsResponse && detailsResponse.length > 0) {
      const firstDetail = detailsResponse[0]
      const mappedProduct = mapBackendToFrontend.productDetail(firstDetail)

      product.value = {
        id: mappedProduct.productId,
        name: mappedProduct.name,
        brand: mappedProduct.brand,
        category: mappedProduct.danhMuc || 'Chưa phân loại',
        description: mappedProduct.description || 'Áo thể thao STRIKER, form thoải mái, phù hợp cho cả tập luyện và thi đấu.',
        features: [
          'Vải thoáng khí, thấm hút mồ hôi nhanh',
          'Co giãn 4 chiều, không gò bó khi vận động',
          'Đường may chắc, hạn chế cọ xát khi chạy',
          'Giữ form và màu sau nhiều lần giặt'
        ],
        images: [mappedProduct.imageUrl],
        colors: [],
        sizes: [],
        isFavorite: false
      }

      const colorMap = new Map()
      const colorSizeVariants = new Map()

      detailsResponse.forEach(detail => {
        const mappedDetail = mapBackendToFrontend.productDetail(detail)

        if (!colorMap.has(mappedDetail.color)) {
          colorMap.set(mappedDetail.color, {
            name: mappedDetail.color,
            code: mappedDetail.colorCode,
            id: mappedDetail.colorId
          })
        }

        const variantKey = `${mappedDetail.color}-${mappedDetail.size}`
        colorSizeVariants.set(variantKey, {
          color: mappedDetail.color,
          colorCode: mappedDetail.colorCode,
          colorId: mappedDetail.colorId,
          size: mappedDetail.size,
          sizeId: mappedDetail.sizeId,
          stock: mappedDetail.stock,
          price: mappedDetail.price,
          detailId: mappedDetail.id
        })

        if (mappedDetail.imageUrl && !product.value.images.includes(mappedDetail.imageUrl)) {
          product.value.images.push(mappedDetail.imageUrl)
        }
      })

      product.value.colors = Array.from(colorMap.values())

      const allSizes = new Map()
      colorSizeVariants.forEach(variant => {
        if (!allSizes.has(variant.size)) {
          allSizes.set(variant.size, {
            size: variant.size,
            sizeId: variant.sizeId,
            stock: 0,
            price: variant.price
          })
        }
        allSizes.get(variant.size).stock += variant.stock
      })

      product.value.sizes = Array.from(allSizes.values())

      // Theo dõi thay đổi tồn kho
      productDetails.value.forEach(detail => {
        watchStock(detail.id, (productDetailId, newStock) => {
          const detailIndex = productDetails.value.findIndex(d => d.id === productDetailId)
          if (detailIndex !== -1) {
            productDetails.value[detailIndex].soLuongTonKho = newStock
            productDetails.value = [...productDetails.value]

            if (selectedSize.value && selectedSize.value.sizeId === productDetails.value[detailIndex].idKichCo?.id) {
              selectedSize.value.stock = newStock
            }
          }
        })
      })

      product.value.variants = Array.from(colorSizeVariants.values())

      selectedImage.value = product.value.images[0]
      selectedColor.value = product.value.colors[0]

      if (selectedColor.value && productDetails.value.length > 0) {
        const colorDetails = productDetails.value.filter(detail => {
          const mappedDetail = mapBackendToFrontend.productDetail(detail)
          return mappedDetail.colorId === selectedColor.value.id && mappedDetail.stock > 0
        })

        if (colorDetails.length > 0) {
          const firstColorDetail = mapBackendToFrontend.productDetail(colorDetails[0])
          selectSize({
            size: firstColorDetail.size,
            sizeId: firstColorDetail.sizeId,
            stock: firstColorDetail.stock,
            price: firstColorDetail.price
          })
        }
      } else {
        selectedSize.value = product.value.sizes.find(s => s.stock > 0) || product.value.sizes[0]
      }
    }
  } catch (error) {
    console.error('Error loading product data:', error)
    product.value = {
      id: route.params.id,
      name: 'Không tìm thấy áo này',
      brand: 'STRIKER',
      category: 'Chưa phân loại',
      description: 'Không tải được thông tin sản phẩm. Quay lại danh sách để chọn áo khác.',
      features: [],
      images: [FALLBACK_IMAGE],
      colors: [{ name: 'Đen', code: '#000000' }],
      sizes: [{ size: 'M', stock: 0, price: 0 }],
      isFavorite: false
    }
    selectedImage.value = product.value.images[0]
    selectedColor.value = product.value.colors[0]
  } finally {
    loading.value = false
  }
}

// Nhắc chọn màu và size khi vào từ nút Mua ngay / Thêm giỏ
const showActionMessage = (action) => {
  const actionText = action === 'add-to-cart' ? 'thêm vào giỏ hàng' : 'mua ngay'

  toast.info(`Chọn màu và size để ${actionText}`, {
    position: 'top-center',
    timeout: 4000
  })

  setTimeout(() => {
    const colorSection = document.querySelector('.st-picker')
    if (colorSection) {
      colorSection.scrollIntoView({ behavior: 'smooth', block: 'center' })
      colorSection.classList.add('is-highlight')
      setTimeout(() => colorSection.classList.remove('is-highlight'), 3000)
    }
  }, 500)
}

onMounted(async () => {
  await loadProductData()

  const action = route.query.action
  if (action === 'add-to-cart' || action === 'buy-now') {
    showActionMessage(action)
  }
})
</script>

<template>
  <div class="st-page">
    <!-- Breadcrumb -->
    <div class="st-crumb-bar">
      <div class="st-container">
        <nav class="st-crumb" aria-label="Đường dẫn">
          <router-link to="/">Trang chủ</router-link>
          <span aria-hidden="true">/</span>
          <router-link to="/products">Sản phẩm</router-link>
          <span aria-hidden="true">/</span>
          <span class="is-current">{{ product.name }}</span>
        </nav>
      </div>
    </div>

    <div class="st-container st-body">
      <!-- Loading -->
      <div v-if="loading" class="st-loading">
        <span class="st-spinner" aria-hidden="true"></span>
        <p>Đang tải thông tin áo...</p>
      </div>

      <div v-else class="st-layout">
        <!-- Images -->
        <div class="st-gallery">
          <div class="st-gallery__main">
            <img :src="selectedImage" :alt="product.name" @error="onImageError" />
          </div>

          <div v-if="product.images.length > 1" class="st-gallery__thumbs">
            <button v-for="(image, index) in product.images" :key="index" @click="selectedImage = image"
              :class="['st-thumb', { 'is-active': selectedImage === image }]"
              :aria-label="`Xem ảnh ${index + 1}`">
              <img :src="image" :alt="`${product.name} ${index + 1}`" @error="onImageError" />
            </button>
          </div>
        </div>

        <!-- Info -->
        <div class="st-info">
          <p class="st-brand">{{ product.brand }}</p>
          <h1 class="st-name">{{ product.name }}</h1>
          <p class="st-category">{{ product.category }}</p>

          <!-- Price -->
          <div class="st-price">
            <span class="st-price__now">{{ formatPrice(currentPrice) }}</span>
            <span v-if="selectedVariant.hasDiscount && selectedVariant.giaGiamGia" class="st-price__was">
              {{ formatPrice(selectedVariant.price) }}
            </span>
            <span v-if="selectedVariant.tenDotGiamGia" class="st-price__deal">
              {{ selectedVariant.tenDotGiamGia }}
            </span>
          </div>

          <!-- Chọn màu + size -->
          <div class="st-picker">
            <div class="st-picker__group">
              <div class="st-picker__head">
                <span class="st-label">Màu áo</span>
                <span v-if="selectedColor" class="st-picker__value">{{ selectedColor.name }}</span>
              </div>
              <div class="st-colors">
                <button v-for="(color, index) in product.colors" :key="index" @click="selectColor(color)"
                  :class="['st-color', { 'is-active': selectedColor === color }]"
                  :style="{ backgroundColor: color.code }" :title="color.name" :aria-label="color.name">
                  <svg v-if="selectedColor === color" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd"
                      d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                      clip-rule="evenodd"></path>
                  </svg>
                </button>
              </div>
            </div>

            <div class="st-picker__group">
              <div class="st-picker__head">
                <span class="st-label">Size</span>
                <span v-if="selectedSize" class="st-picker__value">Còn {{ selectedSize.stock }} áo</span>
              </div>
              <div class="st-sizes">
                <button v-for="size in availableSizesForColor" :key="size.size" @click="selectSize(size)"
                  :disabled="size.stock === 0"
                  :class="['st-size', { 'is-active': selectedSize?.size === size.size }]">
                  {{ size.size }}
                </button>
              </div>
            </div>

            <div v-if="selectedSize && selectedSize.stock > 0" class="st-picker__group">
              <div class="st-picker__head">
                <span class="st-label">Số lượng</span>
              </div>
              <div class="st-qty">
                <button type="button" @click="decreaseQuantity" :disabled="quantity <= 1"
                  class="st-qty__btn" aria-label="Giảm số lượng">−</button>
                <input type="number" class="st-qty__input" :value="quantity" @change="onQuantityInput"
                  min="1" :max="selectedSize.stock" aria-label="Số lượng" />
                <button type="button" @click="increaseQuantity" :disabled="quantity >= selectedSize.stock"
                  class="st-qty__btn" aria-label="Tăng số lượng">+</button>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="st-actions">
            <button @click="addToCart" :disabled="!selectedColor || !selectedSize || selectedSize.stock === 0 || isAdding"
              class="st-btn st-btn--primary">
              {{ addToCartButtonText }}
            </button>

            <button @click="addToCart(true)"
              :disabled="!selectedColor || !selectedSize || selectedSize.stock === 0 || isAdding"
              class="st-btn st-btn--cart" aria-label="Thêm vào giỏ hàng">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z">
                </path>
              </svg>
              Thêm vào giỏ hàng
            </button>
          </div>

          <!-- Chi tiết -->
          <section class="st-block">
            <h2 class="st-block__title">Chi tiết sản phẩm</h2>
            <p class="st-desc">{{ product.description }}</p>
            <ul v-if="product.features.length" class="st-features">
              <li v-for="feature in product.features" :key="feature">{{ feature }}</li>
            </ul>
          </section>

          <!-- Giao hàng -->
          <section class="st-shipping">
            <h2 class="st-block__title">Giao hàng và đổi trả</h2>
            <ul>
              <li>Miễn phí vận chuyển cho đơn từ {{ formatPrice(FREE_SHIPPING_FROM) }}</li>
              <li>Giao hàng trong 2–3 ngày làm việc</li>
              <li>Đổi trả trong 30 ngày, đổi size miễn phí lần đầu</li>
            </ul>
          </section>
        </div>
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
  background: var(--chalk);
}

.st-container {
  width: 100%;
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 1.25rem;
}

/* ===== Breadcrumb ===== */
.st-crumb-bar {
  background: var(--ink);
  padding: 0.9rem 0;
}

.st-crumb {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.85rem;
  color: var(--steel);
}

.st-crumb a { color: var(--steel); text-decoration: none; }
.st-crumb a:hover { color: var(--chalk); }

.st-crumb .is-current {
  color: var(--strike);
  font-weight: 600;
  max-width: 20rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== Body ===== */
.st-body { padding: 2.5rem 1.25rem 4rem; }

.st-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 6rem 1rem;
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
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 3rem;
  align-items: start;
}

/* ===== Ảnh ===== */
.st-gallery__main {
  aspect-ratio: 1;
  background: var(--mist);
  border: 1px solid var(--line);
  overflow: hidden;
}

.st-gallery__main img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.5s ease;
}

.st-gallery__main:hover img { transform: scale(1.05); }

.st-gallery__thumbs {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(4.5rem, 1fr));
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.st-thumb {
  aspect-ratio: 1;
  padding: 0;
  background: var(--mist);
  border: 1px solid var(--line);
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.2s ease;
}

.st-thumb img { width: 100%; height: 100%; object-fit: cover; display: block; }
.st-thumb:hover { border-color: var(--steel); }
.st-thumb.is-active { border: 2px solid var(--strike); }

/* ===== Thông tin ===== */
.st-brand {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.9rem;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: var(--strike);
  margin: 0 0 0.4rem;
}

.st-name {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: clamp(1.9rem, 4vw, 2.9rem);
  line-height: 1;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 0.5rem;
}

.st-category {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.95rem;
  color: var(--steel);
  margin: 0 0 1.5rem;
}

/* Giá */
.st-price {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 0.75rem;
  padding-bottom: 1.5rem;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid var(--ink);
}

.st-price__now {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 2.5rem;
  line-height: 1;
  color: var(--ink);
}

.st-price__was {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 1.05rem;
  color: var(--steel);
  text-decoration: line-through;
}

.st-price__deal {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.78rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--chalk);
  background: var(--strike);
  padding: 0.25rem 0.6rem;
}

/* ===== Chọn màu, size ===== */
.st-picker {
  transition: box-shadow 0.3s ease;
  margin-bottom: 1.75rem;
}

.st-picker.is-highlight {
  animation: st-pulse 3s ease-in-out;
  outline: 2px solid var(--strike);
  outline-offset: 12px;
}

@keyframes st-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 59, 24, 0); }
  30% { box-shadow: 0 0 0 14px rgba(255, 59, 24, 0.14); }
  60% { box-shadow: 0 0 0 6px rgba(255, 59, 24, 0.08); }
}

.st-picker__group { margin-bottom: 1.5rem; }

.st-picker__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.7rem;
}

.st-label {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.9rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--ink);
}

.st-picker__value {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.85rem;
  color: var(--slate);
}

.st-colors {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.st-color {
  position: relative;
  width: 2.75rem;
  height: 2.75rem;
  display: grid;
  place-items: center;
  border: 1px solid var(--line);
  border-radius: 2px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.st-color:hover { transform: scale(1.08); }

.st-color.is-active {
  box-shadow: 0 0 0 2px var(--chalk), 0 0 0 4px var(--ink);
}

.st-color svg {
  width: 1.2rem;
  height: 1.2rem;
  color: var(--chalk);
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.6));
}

.st-sizes {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(4rem, 1fr));
  gap: 0.5rem;
}

.st-size {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 1rem;
  letter-spacing: 0.06em;
  color: var(--ink);
  background: var(--chalk);
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.75rem 0;
  cursor: pointer;
  transition: all 0.2s ease;
}

.st-size:hover:not(:disabled) { border-color: var(--ink); }

.st-size.is-active {
  background: var(--ink);
  border-color: var(--ink);
  color: var(--chalk);
}

.st-size:disabled {
  color: var(--line);
  background: var(--mist);
  cursor: not-allowed;
  text-decoration: line-through;
}

/* ===== Số lượng ===== */
.st-qty {
  display: inline-flex;
  align-items: stretch;
  border: 1px solid var(--line);
  width: fit-content;
}

.st-qty__btn {
  width: 2.75rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 1.2rem;
  color: var(--ink);
  background: var(--chalk);
  border: none;
  cursor: pointer;
  transition: background 0.2s ease;
}

.st-qty__btn:hover:not(:disabled) { background: var(--mist); }

.st-qty__btn:disabled {
  color: var(--line);
  cursor: not-allowed;
}

.st-qty__input {
  width: 3.5rem;
  text-align: center;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 1.05rem;
  color: var(--ink);
  background: var(--chalk);
  border: none;
  border-left: 1px solid var(--line);
  border-right: 1px solid var(--line);
  -moz-appearance: textfield;
}

.st-qty__input::-webkit-outer-spin-button,
.st-qty__input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.st-qty__input:focus { outline: none; }

/* ===== Nút ===== */
.st-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  margin-bottom: 2rem;
}

.st-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 1.05rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.95rem 1.5rem;
  cursor: pointer;
  background: var(--chalk);
  color: var(--ink);
  transition: all 0.2s ease;
}

.st-btn--primary {
  flex: 1 1 14rem;
  background: var(--ink);
  border-color: var(--ink);
  color: var(--chalk);
}

.st-btn--primary:hover:not(:disabled) {
  background: var(--strike);
  border-color: var(--strike);
}

.st-btn--primary:disabled {
  background: var(--mist);
  border-color: var(--line);
  color: var(--steel);
  cursor: not-allowed;
}

.st-btn--cart svg { width: 1.15rem; height: 1.15rem; }
.st-btn--cart:hover:not(:disabled) { border-color: var(--ink); }

.st-btn--cart:disabled {
  color: var(--steel);
  cursor: not-allowed;
}

/* ===== Khối nội dung ===== */
.st-block { margin-bottom: 1.75rem; }

.st-block__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.2rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 0.8rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--line);
}

.st-desc {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.98rem;
  line-height: 1.65;
  color: var(--slate);
  margin: 0 0 0.9rem;
}

.st-features,
.st-shipping ul {
  list-style: none;
  margin: 0;
  padding: 0;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.92rem;
  color: var(--slate);
}

.st-features li,
.st-shipping li {
  position: relative;
  padding-left: 1.1rem;
  margin-bottom: 0.45rem;
}

.st-features li::before,
.st-shipping li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0.55rem;
  width: 5px;
  height: 5px;
  background: var(--strike);
  transform: rotate(45deg);
}

.st-shipping {
  background: var(--mist);
  border-left: 4px solid var(--ink);
  padding: 1.1rem 1.25rem;
}

.st-shipping .st-block__title { border-bottom: none; padding-bottom: 0; }

/* ===== Focus bàn phím ===== */
.st-btn:focus-visible,
.st-size:focus-visible,
.st-color:focus-visible,
.st-thumb:focus-visible,
.st-qty__btn:focus-visible,
.st-qty__input:focus-visible,
.st-crumb a:focus-visible {
  outline: 3px solid var(--cobalt);
  outline-offset: 3px;
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .st-layout { grid-template-columns: 1fr; gap: 2rem; }
}

@media (max-width: 520px) {
  .st-actions { flex-direction: column; }
  .st-btn--primary { flex: 1 1 auto; }
  .st-crumb .is-current { max-width: 10rem; }
}

/* ===== Giảm chuyển động ===== */
@media (prefers-reduced-motion: reduce) {
  .st-gallery__main img,
  .st-btn,
  .st-size,
  .st-color,
  .st-thumb { transition: none; }
  .st-gallery__main:hover img { transform: none; }
  .st-color:hover { transform: none; }
  .st-picker.is-highlight { animation: none; }
  .st-spinner { animation: none; }
}
</style>