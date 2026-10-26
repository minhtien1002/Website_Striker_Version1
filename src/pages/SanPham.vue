<template>
  <div class="st-page">
    <!-- Header Section -->
    <header class="st-hero">
      <span class="st-hero__jersey" aria-hidden="true">10</span>
      <div class="st-container">
        <div class="st-eyebrow">
          <span class="st-eyebrow__bar"></span>
          <span>Kho áo STRIKER</span>
        </div>
        <h1 class="st-display">SẢN PHẨM</h1>
        <p class="st-lead">
          Áo thể thao cho sân cỏ, đường chạy và phòng tập. Lọc theo môn, size và mức giá để tìm đúng chiếc áo bạn cần.
        </p>
      </div>
    </header>

    <div class="st-container st-body">
      <div class="st-layout">
        <!-- Filter Sidebar -->
        <aside :class="['st-sidebar', { 'is-open': sidebarOpen }]">
          <div class="st-sidebar__inner">
            <!-- Filter Header -->
            <div class="st-sidebar__head">
              <h2 class="st-sidebar__title">Bộ lọc</h2>
              <button @click="clearAllFilters" class="st-clear">Xóa tất cả</button>
            </div>

            <!-- Search Filter -->
            <div class="st-group">
              <label class="st-label" for="st-search">Tìm kiếm</label>
              <div class="st-field">
                <svg class="st-field__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                </svg>
                <input id="st-search" v-model="searchQuery" @input="filterProducts" class="st-input" type="text"
                  placeholder="Tên áo hoặc mã sản phẩm..." />
              </div>
            </div>

            <!-- Sport Filter -->
            <div v-if="availableSports.length > 0" class="st-group">
              <span class="st-label">Môn thể thao</span>
              <div class="st-chips">
                <button v-for="sport in availableSports" :key="sport" @click="toggleSportFilter(sport)"
                  :class="['st-chip', { 'is-active': selectedSports.includes(sport) }]">
                  {{ sport }}
                </button>
              </div>
            </div>

            <!-- Brand Filter -->
            <div class="st-group">
              <span class="st-label">Thương hiệu</span>
              <div class="st-checklist">
                <label class="st-check">
                  <input type="checkbox" v-model="selectedBrands" value="" @change="filterProducts" />
                  <span>Tất cả thương hiệu</span>
                </label>
                <label v-for="brand in brands" :key="brand" class="st-check">
                  <input type="checkbox" :value="brand" v-model="selectedBrands" @change="filterProducts" />
                  <span>{{ brand }}</span>
                </label>
              </div>
            </div>

            <!-- Price Range Filter -->
            <div class="st-group">
              <span class="st-label">Khoảng giá</span>
              <div class="st-checklist">
                <label v-for="range in priceRanges" :key="range.value" class="st-check">
                  <input type="radio" name="st-price" :value="range.value" v-model="priceRange"
                    @change="filterProducts" />
                  <span>{{ range.label }}</span>
                </label>
              </div>
            </div>

            <!-- Color Filter -->
            <div class="st-group">
              <span class="st-label">Màu sắc</span>
              <div class="st-swatches">
                <button v-for="color in availableColors" :key="color.code" @click="toggleColorFilter(color.code)"
                  :class="['st-swatch', { 'is-active': selectedColors.includes(color.code) }]"
                  :style="{ backgroundColor: color.code }" :title="color.name" :aria-label="color.name"></button>
              </div>
              <div v-if="selectedColors.length > 0" class="st-tags">
                <span v-for="colorCode in selectedColors" :key="colorCode" class="st-tag">
                  {{ getColorName(colorCode) }}
                  <button @click="toggleColorFilter(colorCode)" :aria-label="`Bỏ lọc màu ${getColorName(colorCode)}`">×</button>
                </span>
              </div>
            </div>

            <!-- Size Filter -->
            <div class="st-group">
              <span class="st-label">Size áo</span>
              <div class="st-sizes">
                <button v-for="size in availableSizes" :key="size" @click="toggleSizeFilter(size)"
                  :class="['st-size', { 'is-active': selectedSizes.includes(size) }]">
                  {{ size }}
                </button>
              </div>
            </div>

            <!-- Sort Options -->
            <div class="st-group st-group--last">
              <label class="st-label" for="st-sort">Sắp xếp theo</label>
              <div class="st-field st-field--select">
                <select id="st-sort" v-model="sortBy" @change="filterProducts" class="st-select">
                  <option value="name">Tên sản phẩm</option>
                  <option value="price-asc">Giá tăng dần</option>
                  <option value="price-desc">Giá giảm dần</option>
                  <option value="newest">Mới nhất</option>
                </select>
                <svg class="st-field__caret" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 9l-7 7-7-7"></path>
                </svg>
              </div>
            </div>
          </div>
        </aside>

        <!-- Products Grid -->
        <div class="st-main">
          <!-- Results Header -->
          <div class="st-results">
            <p class="st-results__text">
              <strong>{{ Math.min((currentPage - 1) * itemsPerPage + 1, filteredProducts.length) }}–{{ Math.min(currentPage * itemsPerPage, filteredProducts.length) }}</strong>
              trên <strong>{{ filteredProducts.length }}</strong> áo
            </p>
            <button @click="toggleSidebar" class="st-filter-toggle">
              {{ sidebarOpen ? 'Đóng bộ lọc' : 'Mở bộ lọc' }}
            </button>
          </div>

          <!-- Empty state -->
          <div v-if="filteredProducts.length === 0" class="st-empty">
            <p class="st-empty__title">Không có áo nào khớp bộ lọc</p>
            <p class="st-empty__hint">Bỏ bớt một vài điều kiện, hoặc xóa tất cả để xem lại toàn bộ kho áo.</p>
            <button @click="clearAllFilters" class="st-btn st-btn--primary">Xóa tất cả bộ lọc</button>
          </div>

          <!-- Grid -->
          <div v-else class="st-grid">
            <article v-for="product in paginatedProducts" :key="product.id" class="st-card"
              @click="goToProductDetail(product.id)">
              <!-- Product Image -->
              <div class="st-card__media">
                <img :src="product.imageUrl" :alt="product.name" class="st-card__img" @error="handleImageError" />

                <span :class="['st-badge', product.status === 'Đang kinh doanh' ? 'st-badge--live' : 'st-badge--off']">
                  {{ product.status }}
                </span>

                <span v-if="product.hasDiscount && product.campaignName" class="st-badge st-badge--deal">
                  {{ product.campaignName }}
                </span>
                <span v-else-if="product.discount" class="st-badge st-badge--deal">
                  -{{ product.discount }}%
                </span>
              </div>

              <!-- Product Info -->
              <div class="st-card__body">
                <p class="st-card__brand">{{ product.brand }}</p>
                <h3 class="st-card__name">{{ product.name }}</h3>

                <!-- Price -->
                <div class="st-price">
                  <span class="st-price__now">{{ formatPrice(product.price) }}</span>
                  <span v-if="product.originalPrice && product.originalPrice !== product.price" class="st-price__was">
                    {{ formatPrice(product.originalPrice) }}
                  </span>
                </div>

                <!-- Colors -->
                <div v-if="product.colors && product.colors.length > 0" class="st-card__colors">
                  <span v-for="(color, index) in product.colors.slice(0, 5)" :key="index"
                    class="st-card__color" :style="{ backgroundColor: color }" :title="getColorName(color)"></span>
                  <span v-if="product.colors.length > 5" class="st-card__more">
                    +{{ product.colors.length - 5 }}
                  </span>
                </div>

                <!-- Stock Info -->
                <p :class="['st-stock', stockClass(product.stock)]">
                  {{ product.stock > 0 ? `Còn ${product.stock} áo` : 'Hết hàng' }}
                </p>

                <!-- Actions -->
                <div class="st-card__actions">
                  <template v-if="product.stock === 0">
                    <button disabled class="st-btn st-btn--muted">Hết hàng</button>
                  </template>
                  <template v-else>
                    <button @click.stop="addToCart(product)" class="st-btn st-btn--icon" title="Thêm vào giỏ hàng"
                      aria-label="Thêm vào giỏ hàng">
                      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-1.5 6M7 13l-1.5-6m0 0h15M17 21a2 2 0 100-4 2 2 0 000 4zM9 21a2 2 0 100-4 2 2 0 000 4z">
                        </path>
                      </svg>
                    </button>
                    <button @click.stop="buyNow(product)" class="st-btn st-btn--primary">Mua ngay</button>
                  </template>

                  <button @click.stop="toggleFavorite(product)"
                    :class="['st-btn', 'st-btn--icon', { 'is-fav': product.isFavorite }]"
                    :aria-label="product.isFavorite ? 'Bỏ khỏi yêu thích' : 'Thêm vào yêu thích'">
                    <svg :fill="product.isFavorite ? 'currentColor' : 'none'" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z">
                      </path>
                    </svg>
                  </button>
                </div>
              </div>
            </article>
          </div>

          <!-- Pagination -->
          <nav v-if="totalPages > 1" class="st-pager" aria-label="Phân trang sản phẩm">
            <button @click="goToPage(currentPage - 1)" :disabled="currentPage === 1" class="st-page-num st-page-num--step">
              Trước
            </button>
            <button v-for="page in visiblePages" :key="page" @click="goToPage(page)"
              :class="['st-page-num', { 'is-active': page === currentPage }]">
              {{ page }}
            </button>
            <button @click="goToPage(currentPage + 1)" :disabled="currentPage === totalPages"
              class="st-page-num st-page-num--step">
              Sau
            </button>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { productAPI, mapBackendToFrontend } from '../services/productAPI.js'

const router = useRouter()

// Reactive data
const products = ref([])
const filteredProducts = ref([])
const searchQuery = ref('')
const selectedBrands = ref([])
const priceRange = ref('')
const selectedColors = ref([])
const selectedSizes = ref([])
const selectedSports = ref([])
const sidebarOpen = ref(false)
const sortBy = ref('name')
const currentPage = ref(1)
const itemsPerPage = 12
const brands = ref([])
// Mã màu (hex) -> tên màu THẬT lấy từ DB, gom lại từ dữ liệu sản phẩm khi tải trang.
const colorNameByCode = ref({})
const availableColors = ref([])
const availableSizes = ref([])
const availableSports = ref([])

// Khoảng giá theo mặt bằng giá áo thể thao
const priceRanges = ref([
  { value: '', label: 'Tất cả giá' },
  { value: '0-300000', label: 'Dưới 300.000đ' },
  { value: '300000-500000', label: '300.000đ - 500.000đ' },
  { value: '500000-800000', label: '500.000đ - 800.000đ' },
  { value: '800000-1200000', label: '800.000đ - 1.200.000đ' },
  { value: '1200000-999999999', label: 'Trên 1.200.000đ' }
])

// Thứ tự size áo chuẩn STRIKER
const SIZE_ORDER = ['XS', 'S', 'M', 'L', 'XL', 'XXL', '2XL', '3XL', '4XL', '5XL']

const sortSizes = (a, b) => {
  const indexA = SIZE_ORDER.indexOf(String(a).toUpperCase())
  const indexB = SIZE_ORDER.indexOf(String(b).toUpperCase())
  if (indexA !== -1 && indexB !== -1) return indexA - indexB
  if (indexA !== -1) return -1
  if (indexB !== -1) return 1
  return String(a).localeCompare(String(b))
}

// Computed properties
const totalPages = computed(() => Math.ceil(filteredProducts.value.length / itemsPerPage))

const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filteredProducts.value.slice(start, end)
})

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

const stockClass = (stock) => {
  if (stock > 10) return 'is-good'
  if (stock > 0) return 'is-low'
  return 'is-out'
}

// Methods
const fetchProducts = async () => {
  try {
    const response = await productAPI.getProducts({ page: 0, size: 9999 })

    // Try to enrich with discount information
    try {
      const allProductDetails = await productAPI.getAllProductDetailsWithDiscount()

      const discountMap = new Map()
      allProductDetails.forEach(detail => {
        const productId = detail.idSanPham
        if (productId && detail.hasDiscount && detail.giaGiamGia) {
          if (!discountMap.has(productId)) {
            discountMap.set(productId, [])
          }
          discountMap.get(productId).push({
            giaGiamGia: detail.giaGiamGia,
            tenDotGiamGia: detail.tenDotGiamGia,
            hasDiscount: detail.hasDiscount
          })
        }
      })

      response.content.forEach(product => {
        const discountInfo = discountMap.get(product.id)
        if (discountInfo && discountInfo.length > 0) {
          const variantsWithDiscount = discountInfo.filter(info => info.hasDiscount && info.giaGiamGia)
          if (variantsWithDiscount.length > 0) {
            if (!product.chiTietSanPhams) product.chiTietSanPhams = []
            variantsWithDiscount.forEach(discount => {
              product.chiTietSanPhams.push({
                giaGiamGia: discount.giaGiamGia,
                tenDotGiamGia: discount.tenDotGiamGia,
                hasDiscount: discount.hasDiscount
              })
            })
          }
        }
      })

      console.log('Products enriched with discount info')
    } catch (discountError) {
      console.warn('Failed to enrich products with discount, using regular products:', discountError)
    }

    products.value = response.content
      .filter(item => item.idTrangThai !== 4)
      .map((obj) => {
        const mappedProduct = mapBackendToFrontend.product(obj)
        return {
          ...mappedProduct,
          originalPrice: null,
          discount: null,
          isFavorite: false
        }
      })

    // Thương hiệu
    brands.value = [...new Set(products.value.map(p => p.brand).filter(Boolean))].sort()

    // Môn thể thao (chỉ hiện khi backend có trả về)
    const sportSet = new Set()
    products.value.forEach(product => {
      const sport = product.monTheThao || product.tenMonTheThao
      if (sport) sportSet.add(sport)
    })
    availableSports.value = Array.from(sportSet).sort((a, b) => a.localeCompare(b))

    // Màu sắc - dùng tên màu THẬT lấy từ DB (colorDetails) thay vì đoán qua bảng tra cứu
    // hex cứng, tránh hiển thị nhầm thành "Màu khác" cho các màu không có trong bảng đó.
    const colorSet = new Set()
    products.value.forEach(product => {
      (product.colors || []).forEach(color => colorSet.add(color));
      (product.colorDetails || []).forEach(c => { if (c.code) colorNameByCode.value[c.code] = c.name })
    })
    availableColors.value = Array.from(colorSet).map(colorCode => ({
      name: getColorName(colorCode),
      code: colorCode
    })).sort((a, b) => a.name.localeCompare(b.name))

    // Size áo
    const sizeSet = new Set()
    products.value.forEach(product => {
      (product.sizes || []).forEach(size => sizeSet.add(size))
    })
    availableSizes.value = Array.from(sizeSet).sort(sortSizes)

    filterProducts()
  } catch (err) {
    console.error('Lỗi khi lấy dữ liệu sản phẩm từ API:', err)
    products.value = []
    brands.value = []
    availableSports.value = []
    filterProducts()
  }
}

const filterProducts = () => {
  let filtered = [...products.value]

  // Filter by search query
  if (searchQuery.value) {
    const keyword = searchQuery.value.toLowerCase()
    filtered = filtered.filter(product =>
      (product.tenSanPham || product.name || '').toLowerCase().includes(keyword) ||
      (product.brand || '').toLowerCase().includes(keyword) ||
      (product.ma || '').toLowerCase().includes(keyword)
    )
  }

  // Filter by sports
  if (selectedSports.value.length > 0) {
    filtered = filtered.filter(product =>
      selectedSports.value.includes(product.monTheThao || product.tenMonTheThao)
    )
  }

  // Filter by brands (multiple selection)
  if (selectedBrands.value.length > 0 && !selectedBrands.value.includes('')) {
    filtered = filtered.filter(product => selectedBrands.value.includes(product.brand))
  }

  // Filter by colors
  if (selectedColors.value.length > 0) {
    filtered = filtered.filter(product =>
      (product.colors || []).some(color => selectedColors.value.includes(color))
    )
  }

  // Filter by sizes
  if (selectedSizes.value.length > 0) {
    filtered = filtered.filter(product =>
      (product.sizes || []).some(size => selectedSizes.value.includes(size))
    )
  }

  // Filter by price range
  if (priceRange.value) {
    const [min, max] = priceRange.value.split('-').map(Number)
    filtered = filtered.filter(product => product.price >= min && product.price <= max)
  }

  // Sort products
  switch (sortBy.value) {
    case 'price-asc':
      filtered.sort((a, b) => a.price - b.price)
      break
    case 'price-desc':
      filtered.sort((a, b) => b.price - a.price)
      break
    case 'newest':
      filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      break
    default:
      filtered.sort((a, b) =>
        (a.name || a.tenSanPham || '').localeCompare(b.name || b.tenSanPham || '')
      )
  }

  filteredProducts.value = filtered
  currentPage.value = 1
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const addToCart = (product) => {
  if (product.stock > 0) {
    // Chuyển sang trang chi tiết để khách chọn màu và size trước khi thêm vào giỏ
    router.push(`/product/${product.id}?action=add-to-cart`)
  }
}

const buyNow = (product) => {
  if (product.stock > 0) {
    router.push(`/product/${product.id}?action=buy-now`)
  }
}

const toggleFavorite = (product) => {
  product.isFavorite = !product.isFavorite
}

// Ảnh dự phòng khi link ảnh áo bị hỏng
const FALLBACK_IMAGE =
  'data:image/svg+xml;charset=UTF-8,' +
  encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 300">
      <rect width="400" height="300" fill="#10141a"/>
      <path d="M150 90 L120 105 L132 140 L148 134 L148 215 L252 215 L252 134 L268 140 L280 105 L250 90 L225 100 A28 20 0 0 1 175 100 Z"
            fill="none" stroke="#ff3b18" stroke-width="4" stroke-linejoin="round"/>
      <text x="200" y="262" fill="#8c98a8" font-family="Arial, sans-serif" font-size="17"
            font-weight="bold" letter-spacing="4" text-anchor="middle">STRIKER</text>
    </svg>
  `)

const handleImageError = (event) => {
  if (event.target.dataset.fallbackApplied) return
  event.target.dataset.fallbackApplied = 'true'
  event.target.src = FALLBACK_IMAGE
}

const goToProductDetail = (productId) => {
  router.push(`/product/${productId}`)
}

const getColorName = (color) => {
  const colorNames = {
    '#000000': 'Đen',
    '#FFFFFF': 'Trắng',
    '#FF0000': 'Đỏ',
    '#0066CC': 'Xanh dương',
    '#FFD700': 'Vàng',
    '#32CD32': 'Xanh lá',
    '#FF69B4': 'Hồng',
    '#800080': 'Tím',
    '#FFA500': 'Cam',
    '#8B4513': 'Nâu',
    '#708090': 'Xám',
    '#DC143C': 'Đỏ đậm',
    '#000080': 'Xanh navy',
    '#FF1493': 'Hồng đậm',
    '#00CED1': 'Xanh ngọc',
    '#228B22': 'Xanh lá đậm',
    '#FF4500': 'Đỏ cam',
    '#4B0082': 'Tím đậm',
    '#CD853F': 'Nâu cát',
    '#20B2AA': 'Xanh lam',
    '#B22222': 'Đỏ gạch'
  }
  // Ưu tiên tên màu THẬT lấy từ DB (bắt cả khi hex viết hoa/thường khác nhau), chỉ dùng
  // bảng tra cứu cứng và nhãn chung "Màu khác" khi không có dữ liệu thật cho mã màu này.
  const realName = colorNameByCode.value[color]
    || Object.entries(colorNameByCode.value).find(([code]) => code?.toLowerCase() === color?.toLowerCase())?.[1]
  return realName || colorNames[color] || colorNames[color?.toUpperCase()] || 'Màu khác'
}

// Toggle functions for filters
const toggleColorFilter = (colorCode) => {
  const index = selectedColors.value.indexOf(colorCode)
  if (index > -1) {
    selectedColors.value.splice(index, 1)
  } else {
    selectedColors.value.push(colorCode)
  }
  filterProducts()
}

const toggleSizeFilter = (size) => {
  const index = selectedSizes.value.indexOf(size)
  if (index > -1) {
    selectedSizes.value.splice(index, 1)
  } else {
    selectedSizes.value.push(size)
  }
  filterProducts()
}

const toggleSportFilter = (sport) => {
  const index = selectedSports.value.indexOf(sport)
  if (index > -1) {
    selectedSports.value.splice(index, 1)
  } else {
    selectedSports.value.push(sport)
  }
  filterProducts()
}

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const clearAllFilters = () => {
  searchQuery.value = ''
  selectedBrands.value = []
  selectedColors.value = []
  selectedSizes.value = []
  selectedSports.value = []
  priceRange.value = ''
  sortBy.value = 'name'
  filterProducts()
}

onMounted(() => {
  fetchProducts()
})
</script>

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
  background: var(--chalk);
  min-height: 100vh;
}

.st-container {
  width: 100%;
  max-width: 1360px;
  margin: 0 auto;
  padding: 0 1.25rem;
}

/* ===== Hero ===== */
.st-hero {
  position: relative;
  overflow: hidden;
  background: var(--ink);
  background-image: repeating-linear-gradient(115deg, rgba(255, 255, 255, 0.022) 0 2px, transparent 2px 74px);
  padding: 3.5rem 0 3rem;
  margin-bottom: 2.5rem;
}

.st-hero__jersey {
  position: absolute;
  top: 50%;
  right: 3rem;
  transform: translateY(-50%);
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: clamp(8rem, 18vw, 15rem);
  line-height: 0.8;
  color: transparent;
  -webkit-text-stroke: 2px rgba(255, 255, 255, 0.09);
  pointer-events: none;
  user-select: none;
}

.st-eyebrow {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.9rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.9rem;
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
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  line-height: 0.92;
  text-transform: uppercase;
  color: var(--chalk);
  margin: 0 0 0.9rem;
}

.st-lead {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 1.05rem;
  line-height: 1.6;
  color: var(--steel);
  max-width: 40rem;
  margin: 0;
}

/* ===== Layout ===== */
.st-body { padding-bottom: 4rem; }

.st-layout {
  display: grid;
  grid-template-columns: 17rem 1fr;
  gap: 2rem;
  align-items: start;
}

/* ===== Sidebar ===== */
.st-sidebar__inner {
  position: sticky;
  top: 1.5rem;
  background: var(--chalk);
  border: 1px solid var(--line);
  border-top: 4px solid var(--ink);
  padding: 1.5rem 1.25rem;
}

.st-sidebar__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding-bottom: 1rem;
  margin-bottom: 1.25rem;
  border-bottom: 1px solid var(--line);
}

.st-sidebar__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.4rem;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0;
}

.st-clear {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--strike);
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.st-clear:hover { text-decoration: underline; }

.st-group {
  padding-bottom: 1.25rem;
  margin-bottom: 1.25rem;
  border-bottom: 1px solid var(--line);
}

.st-group--last {
  padding-bottom: 0;
  margin-bottom: 0;
  border-bottom: none;
}

.st-label {
  display: block;
  margin-bottom: 0.7rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.85rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--ink);
}

/* Ô nhập */
.st-field {
  position: relative;
  display: flex;
  align-items: center;
}

.st-field__icon {
  position: absolute;
  left: 0.75rem;
  width: 1.05rem;
  height: 1.05rem;
  color: var(--steel);
  pointer-events: none;
}

.st-input,
.st-select {
  width: 100%;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.92rem;
  color: var(--ink);
  background: var(--mist);
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.65rem 0.75rem 0.65rem 2.4rem;
}

.st-select {
  padding-left: 0.75rem;
  padding-right: 2.25rem;
  appearance: none;
  cursor: pointer;
}

.st-input:focus,
.st-select:focus {
  outline: none;
  border-color: var(--strike);
  background: var(--chalk);
}

.st-field__caret {
  position: absolute;
  right: 0.7rem;
  width: 1rem;
  height: 1rem;
  color: var(--strike);
  pointer-events: none;
}

/* Chip môn thể thao */
.st-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
}

.st-chip {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.85rem;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--ink);
  background: var(--mist);
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.4rem 0.7rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.st-chip:hover { border-color: var(--ink); }

.st-chip.is-active {
  background: var(--ink);
  border-color: var(--ink);
  color: var(--chalk);
}

/* Checkbox / radio */
.st-checklist {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-height: 13rem;
  overflow-y: auto;
}

.st-check {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.9rem;
  color: var(--slate);
  cursor: pointer;
}

.st-check input {
  width: 1rem;
  height: 1rem;
  accent-color: var(--strike);
  cursor: pointer;
  flex-shrink: 0;
}

.st-check:hover { color: var(--ink); }

/* Ô màu */
.st-swatches {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 0.4rem;
}

.st-swatch {
  aspect-ratio: 1;
  border: 1px solid var(--line);
  border-radius: 2px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.st-swatch:hover { transform: scale(1.12); }

.st-swatch.is-active {
  box-shadow: 0 0 0 2px var(--chalk), 0 0 0 4px var(--strike);
}

.st-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.3rem;
  margin-top: 0.7rem;
}

.st-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.78rem;
  color: var(--ink);
  background: var(--mist);
  border: 1px solid var(--line);
  padding: 0.15rem 0.45rem;
}

.st-tag button {
  background: none;
  border: none;
  color: var(--strike);
  font-size: 1rem;
  line-height: 1;
  cursor: pointer;
  padding: 0;
}

/* Size */
.st-sizes {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.4rem;
}

.st-size {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.9rem;
  color: var(--ink);
  background: var(--chalk);
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.5rem 0;
  cursor: pointer;
  transition: all 0.2s ease;
}

.st-size:hover { border-color: var(--ink); }

.st-size.is-active {
  background: var(--strike);
  border-color: var(--strike);
  color: var(--chalk);
}

/* ===== Kết quả ===== */
.st-results {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding-bottom: 1rem;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid var(--ink);
}

.st-results__text {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 1rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--slate);
  margin: 0;
}

.st-results__text strong { color: var(--strike); }

.st-filter-toggle {
  display: none;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.9rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--chalk);
  background: var(--ink);
  border: none;
  border-radius: 2px;
  padding: 0.55rem 1rem;
  cursor: pointer;
}

/* ===== Lưới sản phẩm ===== */
.st-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(15rem, 1fr));
  gap: 1.25rem;
}

.st-card {
  display: flex;
  flex-direction: column;
  background: var(--chalk);
  border: 1px solid var(--line);
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.st-card:hover {
  border-color: var(--ink);
  transform: translateY(-3px);
}

.st-card__media {
  position: relative;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: var(--mist);
}

.st-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.st-card:hover .st-card__img { transform: scale(1.06); }

.st-badge {
  position: absolute;
  top: 0.6rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.72rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  padding: 0.25rem 0.55rem;
  max-width: 60%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.st-badge--live { right: 0.6rem; background: #0f7a3d; color: var(--chalk); }
.st-badge--off { right: 0.6rem; background: var(--slate); color: var(--chalk); }
.st-badge--deal { left: 0.6rem; background: var(--strike); color: var(--chalk); }

.st-card__body {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 0.9rem 1rem 1rem;
}

.st-card__brand {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.78rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--steel);
  margin: 0 0 0.3rem;
}

.st-card__name {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 1rem;
  line-height: 1.35;
  color: var(--ink);
  margin: 0 0 0.6rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.st-price {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.7rem;
}

.st-price__now {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.35rem;
  color: var(--ink);
}

.st-price__was {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.85rem;
  color: var(--steel);
  text-decoration: line-through;
}

.st-card__colors {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  margin-bottom: 0.6rem;
}

.st-card__color {
  width: 1.05rem;
  height: 1.05rem;
  border: 1px solid var(--line);
  border-radius: 2px;
}

.st-card__more {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.75rem;
  color: var(--steel);
}

.st-stock {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.85rem;
  font-weight: 500;
  margin: 0 0 0.85rem;
}

.st-stock.is-good { color: #0f7a3d; }
.st-stock.is-low { color: #b45309; }
.st-stock.is-out { color: var(--strike); }

.st-card__actions {
  display: flex;
  gap: 0.4rem;
  margin-top: auto;
}

/* ===== Nút ===== */
.st-btn {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.95rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.6rem 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
  background: var(--chalk);
  color: var(--ink);
}

.st-btn--primary {
  flex: 1;
  background: var(--ink);
  border-color: var(--ink);
  color: var(--chalk);
}

.st-btn--primary:hover {
  background: var(--strike);
  border-color: var(--strike);
}

.st-btn--muted {
  flex: 1;
  background: var(--mist);
  color: var(--steel);
  cursor: not-allowed;
}

.st-btn--icon {
  display: grid;
  place-items: center;
  padding: 0.6rem;
  flex-shrink: 0;
}

.st-btn--icon svg {
  width: 1.2rem;
  height: 1.2rem;
}

.st-btn--icon:hover { border-color: var(--ink); }

.st-btn--icon.is-fav {
  color: var(--strike);
  border-color: var(--strike);
}

/* ===== Trạng thái rỗng ===== */
.st-empty {
  text-align: center;
  padding: 4rem 1rem;
  border: 1px dashed var(--line);
}

.st-empty__title {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.6rem;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 0.5rem;
}

.st-empty__hint {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  color: var(--slate);
  margin: 0 0 1.5rem;
}

/* ===== Phân trang ===== */
.st-pager {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.4rem;
  margin-top: 2.5rem;
}

.st-page-num {
  min-width: 2.75rem;
  height: 2.75rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 1rem;
  letter-spacing: 0.06em;
  color: var(--ink);
  background: var(--chalk);
  border: 1px solid var(--line);
  border-radius: 2px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.st-page-num--step {
  padding: 0 1rem;
  text-transform: uppercase;
}

.st-page-num:hover:not(:disabled) { border-color: var(--ink); }

.st-page-num.is-active {
  background: var(--ink);
  border-color: var(--ink);
  color: var(--chalk);
}

.st-page-num:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* ===== Focus bàn phím ===== */
.st-btn:focus-visible,
.st-page-num:focus-visible,
.st-chip:focus-visible,
.st-size:focus-visible,
.st-swatch:focus-visible,
.st-input:focus-visible,
.st-select:focus-visible,
.st-clear:focus-visible,
.st-filter-toggle:focus-visible {
  outline: 3px solid var(--cobalt);
  outline-offset: 2px;
}

/* ===== Responsive ===== */
@media (max-width: 1024px) {
  .st-layout { grid-template-columns: 1fr; }

  .st-sidebar {
    display: none;
  }

  .st-sidebar.is-open {
    display: block;
  }

  .st-sidebar__inner { position: static; }

  .st-filter-toggle { display: block; }

  .st-hero__jersey { opacity: 0.5; }
}

@media (max-width: 520px) {
  .st-grid { grid-template-columns: repeat(auto-fill, minmax(13rem, 1fr)); }
  .st-results { flex-direction: column; align-items: flex-start; }
}

/* ===== Giảm chuyển động ===== */
@media (prefers-reduced-motion: reduce) {
  .st-card,
  .st-card__img,
  .st-btn,
  .st-page-num,
  .st-chip,
  .st-size,
  .st-swatch {
    transition: none;
  }
  .st-card:hover { transform: none; }
  .st-card:hover .st-card__img { transform: none; }
}
</style>