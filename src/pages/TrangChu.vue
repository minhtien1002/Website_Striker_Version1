<script setup>
import { inject, ref, reactive, watch, onMounted, onUnmounted, computed, nextTick } from 'vue'
import CardList from '../components/CardList.vue'
import Banner from '../components/Banner.vue'
import { productAPI, mapBackendToFrontend } from '../services/productAPI.js'

// Bộ sưu tập theo danh mục áo thể thao.
// categoryName phải trùng với ten_danh_muc trong bảng danh_muc của database STRIKER.
const collections = ref([
  {
    title: 'ÁO BÓNG ĐÁ',
    categoryName: 'Áo bóng đá',
    jersey: '09',
    tagline: 'Sân cỏ · Thi đấu',
    interval: 6000
  },
  {
    title: 'ÁO CHẠY BỘ',
    categoryName: 'Áo chạy bộ',
    jersey: '21',
    tagline: 'Đường chạy · Bền bỉ',
    interval: 7000
  },
  {
    title: 'ÁO TẬP GYM',
    categoryName: 'Áo tập gym',
    jersey: '07',
    tagline: 'Phòng tập · Sức mạnh',
    interval: 8000
  }
])

// Injection for cart actions
const { addToCart, removeFromCart, cart } = inject('cartActions')

// Reactive object for filters
const filters = reactive({
  sortBy: 'tenSanPham',
  searchQuery: ''
})

// Pagination state for 'Tất cả sản phẩm' section
const currentPage = ref(0)
const totalPages = ref(0)
const itemsPerPage = 10
const allItems = ref([])
const sortedAndFilteredItems = ref([])
const isAllItemsFading = ref(false)
const autoplayInterval = ref(null)

// Khởi tạo state cho từng bộ sưu tập theo danh sách collections
const collectionItems = reactive({})
const collectionPagination = reactive({})

collections.value.forEach((collection) => {
  collectionItems[collection.title] = []
  collectionPagination[collection.title] = {
    currentPage: 0,
    itemsPerPage: 4,
    isFading: false,
    totalPages: 0
  }
})

const collectionAutoplayIntervals = reactive({})

// Reference for scrolling to the products section
const productsSection = ref(null)

// Function to convert a string to a URL-friendly slug
const slugify = (text) => {
  return text
    .toString()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()
    .replace(/\s+/g, '-')
    .replace(/[^\w-]+/g, '')
    .replace(/--+/g, '-')
}

// Handler for select element change
const onChangeSelect = (event) => {
  filters.sortBy = event.target.value
  currentPage.value = 0
  sortAndFilterAllItems()
  startAutoplay()
}

// Handler for search input change
const onChangeSearchInput = (event) => {
  filters.searchQuery = event.target.value
  currentPage.value = 0
  sortAndFilterAllItems()
  startAutoplay()
}

// Handler for adding/removing items from the cart
const onClickAddPlus = (item) => {
  if (!item.isAdded) {
    addToCart(item)
  } else {
    removeFromCart(item)
  }
}

// Handler for toggling favorite
const onToggleFavorite = (item) => {
  item.isFavorite = !item.isFavorite
  console.log(`${item.isFavorite ? 'Đã thêm' : 'Đã xóa'} ${item.title} ${item.isFavorite ? 'vào' : 'khỏi'} danh sách yêu thích`)
}

// Function to sort and filter all items based on status and user's sort preference
const sortAndFilterAllItems = () => {
  // Ẩn các sản phẩm có trạng thái 'Ngừng kinh doanh' (idTrangThai === 4)
  let filteredList = allItems.value.filter(item => item.idTrangThai !== 4 &&
    item.tenSanPham.toLowerCase().includes(filters.searchQuery.toLowerCase())
  )

  // Sắp xếp ưu tiên: 'Đang kinh doanh' (3) lên đầu, 'Hết hàng' (5) xuống cuối
  const statusOrder = { '3': 1, '5': 2 }
  filteredList.sort((a, b) => {
    const statusA = statusOrder[a.idTrangThai] || 99
    const statusB = statusOrder[b.idTrangThai] || 99

    if (statusA !== statusB) {
      return statusA - statusB
    }

    if (filters.sortBy === 'price-desc') {
      return b.giaBanThapNhat - a.giaBanThapNhat
    }
    if (filters.sortBy === 'price-asc') {
      return a.giaBanThapNhat - b.giaBanThapNhat
    }
    return a.tenSanPham.localeCompare(b.tenSanPham)
  })

  totalPages.value = Math.ceil(filteredList.length / itemsPerPage)
  sortedAndFilteredItems.value = filteredList
}

// Function to fetch all items for the main section
const fetchAllItems = async () => {
  try {
    const response = await productAPI.getProducts({ page: 0, size: 9999 })

    // Try to enrich with discount information
    try {
      const allProductDetails = await productAPI.getAllProductDetailsWithDiscount()

      const discountMap = new Map()
      allProductDetails.forEach(detail => {
        const productId = detail.idSanPham
        console.log('Processing discount detail:', {
          detailId: detail.id,
          productId: productId,
          idSanPham: detail.idSanPham,
          tenSanPham: detail.tenSanPham,
          hasDiscount: detail.hasDiscount,
          giaGiamGia: detail.giaGiamGia,
          tenDotGiamGia: detail.tenDotGiamGia
        })

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

      console.log('Discount map created:', discountMap)

      response.content.forEach(product => {
        const discountInfo = discountMap.get(product.id)
        console.log(`Checking product ${product.id} (${product.tenSanPham}):`, discountInfo)

        if (discountInfo && discountInfo.length > 0) {
          const variantsWithDiscount = discountInfo.filter(info => info.hasDiscount && info.giaGiamGia)
          console.log(`Variants with discount for product ${product.id}:`, variantsWithDiscount)

          if (variantsWithDiscount.length > 0) {
            if (!product.chiTietSanPhams) product.chiTietSanPhams = []
            variantsWithDiscount.forEach(discount => {
              product.chiTietSanPhams.push({
                giaGiamGia: discount.giaGiamGia,
                tenDotGiamGia: discount.tenDotGiamGia,
                hasDiscount: discount.hasDiscount
              })
            })
            console.log(`Added discount info to product ${product.id}:`, product.chiTietSanPhams)
          }
        }
      })

      console.log('Homepage products enriched with discount info')
    } catch (discountError) {
      console.warn('Failed to enrich homepage products with discount, using regular products:', discountError)
    }

    const statusOrder = { '3': 1, '5': 2 }
    const sortedData = response.content.filter(item => item.idTrangThai !== 4).sort((a, b) => {
      const statusA = statusOrder[a.idTrangThai] || 99
      const statusB = statusOrder[b.idTrangThai] || 99

      if (statusA !== statusB) {
        return statusA - statusB
      }
      return new Date(b.ngayTao) - new Date(a.ngayTao)
    })

    allItems.value = sortedData.map(item => {
      const mappedProduct = mapBackendToFrontend.product(item)
      return {
        ...mappedProduct,
        originalPrice: null,
        discount: null,
        isFavorite: false
      }
    })
    sortAndFilterAllItems()
  } catch (err) {
    console.error('Lỗi khi lấy dữ liệu sản phẩm từ API:', err)
    allItems.value = []
    sortAndFilterAllItems()
  }
}

// Fetch items for a specific collection by category
const fetchCollectionItems = async (collection) => {
  try {
    console.log(`Fetching products for ${collection.title} with category: "${collection.categoryName}"`)

    const response = await productAPI.getProductsByCategory(collection.categoryName, 0, 20)

    if (response?.content?.length > 0) {
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
            if (!product.chiTietSanPhams) product.chiTietSanPhams = []
            discountInfo.forEach(discount => {
              product.chiTietSanPhams.push({
                giaGiamGia: discount.giaGiamGia,
                tenDotGiamGia: discount.tenDotGiamGia,
                hasDiscount: discount.hasDiscount
              })
            })
          }
        })

        console.log(`Collection ${collection.title} enriched with discount info`)
      } catch (discountError) {
        console.warn(`Failed to enrich collection ${collection.title} with discount info:`, discountError)
      }

      const mappedProducts = response.content.map(backendProduct =>
        mapBackendToFrontend.product(backendProduct)
      )

      collectionItems[collection.title] = mappedProducts
      console.log(`Loaded ${mappedProducts.length} products for ${collection.title}`)
    } else {
      console.warn(`No products found for ${collection.title} with category "${collection.categoryName}"`)
      collectionItems[collection.title] = []
    }

    collectionPagination[collection.title].totalPages = Math.ceil(
      collectionItems[collection.title].length / collectionPagination[collection.title].itemsPerPage
    )
  } catch (err) {
    console.error(`Error fetching products for ${collection.title}:`, err)
    collectionItems[collection.title] = []
    collectionPagination[collection.title].totalPages = 0
  }
}

const loadCategoriesAndUpdateCollections = async () => {
  try {
    console.log('Using predefined category names for homepage sections')
  } catch (error) {
    console.error('Error in category setup:', error)
  }
}

const fetchAllCollections = async () => {
  const promises = collections.value.map(collection => fetchCollectionItems(collection))
  await Promise.all(promises)
}

// Autoplay logic for main product list
const startAutoplay = () => {
  if (totalPages.value <= 1) {
    if (autoplayInterval.value) {
      clearInterval(autoplayInterval.value)
    }
    return
  }

  if (autoplayInterval.value) {
    clearInterval(autoplayInterval.value)
  }
  autoplayInterval.value = setInterval(() => {
    nextPageAutoPlay()
  }, 5000)
}

// Autoplay logic for collections
const startCollectionAutoplay = (collection) => {
  if (!collection) return

  if (collectionPagination[collection.title].totalPages <= 1) {
    if (collectionAutoplayIntervals[collection.title]) {
      clearInterval(collectionAutoplayIntervals[collection.title])
    }
    return
  }

  if (collectionAutoplayIntervals[collection.title]) {
    clearInterval(collectionAutoplayIntervals[collection.title])
  }
  collectionAutoplayIntervals[collection.title] = setInterval(() => {
    nextCollectionPageAutoPlay(collection.title)
  }, collection.interval)
}

onMounted(async () => {
  await loadCategoriesAndUpdateCollections()

  await fetchAllItems()
  await fetchAllCollections()
  startAutoplay()

  collections.value.forEach(collection => {
    startCollectionAutoplay(collection)
  })
})

onUnmounted(() => {
  clearInterval(autoplayInterval.value)
  for (const key in collectionAutoplayIntervals) {
    clearInterval(collectionAutoplayIntervals[key])
  }
})

// Computed property for main section pagination
const paginatedItems = computed(() => {
  const start = currentPage.value * itemsPerPage
  const end = start + itemsPerPage
  return sortedAndFilteredItems.value.slice(start, end)
})

// Computed property for collection section pagination
const paginatedCollectionItems = (collectionTitle) => {
  const collection = collectionItems[collectionTitle]
  if (!collection) {
    return []
  }
  const start = collectionPagination[collectionTitle].currentPage * collectionPagination[collectionTitle].itemsPerPage
  const end = start + collectionPagination[collectionTitle].itemsPerPage
  return collection.slice(start, end)
}

const nextCollectionPageAutoPlay = async (collectionTitle) => {
  const currentCollection = collectionPagination[collectionTitle]
  if (currentCollection.currentPage < currentCollection.totalPages - 1) {
    currentCollection.isFading = true
    await nextTick()
    setTimeout(() => {
      currentCollection.currentPage++
      currentCollection.isFading = false
    }, 200)
  } else {
    currentCollection.isFading = true
    await nextTick()
    setTimeout(() => {
      currentCollection.currentPage = 0
      currentCollection.isFading = false
    }, 200)
  }
}

const nextPageAutoPlay = async () => {
  if (currentPage.value < totalPages.value - 1) {
    isAllItemsFading.value = true
    await nextTick()
    setTimeout(() => {
      currentPage.value++
      isAllItemsFading.value = false
    }, 200)
  } else {
    isAllItemsFading.value = true
    await nextTick()
    setTimeout(() => {
      isAllItemsFading.value = false
    }, 200)
  }
}

const nextPage = async () => {
  if (currentPage.value < totalPages.value - 1) {
    startAutoplay()
    isAllItemsFading.value = true
    await nextTick()
    setTimeout(() => {
      currentPage.value++
      isAllItemsFading.value = false
      scrollToProducts('Tất cả sản phẩm')
    }, 200)
  }
}

const prevPage = async () => {
  if (currentPage.value > 0) {
    startAutoplay()
    isAllItemsFading.value = true
    await nextTick()
    setTimeout(() => {
      currentPage.value--
      isAllItemsFading.value = false
      scrollToProducts('Tất cả sản phẩm')
    }, 200)
  }
}

const scrollToProducts = (collectionTitle) => {
  const targetId = slugify(collectionTitle)
  const targetElement = document.getElementById(targetId)
  if (targetElement) {
    targetElement.scrollIntoView({ behavior: 'smooth' })
  } else {
    if (productsSection.value) {
      productsSection.value.scrollIntoView({ behavior: 'smooth' })
    }
  }
}

const onPageClick = (page) => {
  currentPage.value = page - 1
  scrollToProducts('Tất cả sản phẩm')
  startAutoplay()
}

const nextCollectionPage = async (collectionTitle) => {
  const currentCollection = collectionPagination[collectionTitle]
  const collection = collections.value.find(c => c.title === collectionTitle)
  if (currentCollection.currentPage < currentCollection.totalPages - 1) {
    startCollectionAutoplay(collection)
    currentCollection.isFading = true
    await nextTick()
    setTimeout(() => {
      currentCollection.currentPage++
      currentCollection.isFading = false
      scrollToProducts(collectionTitle)
    }, 200)
  }
}

const prevCollectionPage = async (collectionTitle) => {
  const currentCollection = collectionPagination[collectionTitle]
  const collection = collections.value.find(c => c.title === collectionTitle)
  if (currentCollection.currentPage > 0) {
    startCollectionAutoplay(collection)
    currentCollection.isFading = true
    await nextTick()
    setTimeout(() => {
      currentCollection.currentPage--
      currentCollection.isFading = false
      scrollToProducts(collectionTitle)
    }, 200)
  }
}

watch(cart, () => {
  allItems.value = allItems.value.map((obj) => ({
    ...obj,
    isAdded: cart.value.some((cartItem) => cartItem.id === obj.id)
  }))
  collections.value.forEach(collection => {
    collectionItems[collection.title] = collectionItems[collection.title].map((obj) => ({
      ...obj,
      isAdded: cart.value.some((cartItem) => cartItem.id === obj.id)
    }))
  })
})
</script>

<template>
  <!-- Banner Section -->
  <Banner @scroll-to-products="scrollToProducts" :collections="collections" />

  <!-- Thanh chạy chữ kiểu bảng LED sân vận động -->
  <div class="st-ticker" aria-hidden="true">
    <div class="st-ticker__track">
      <span v-for="n in 2" :key="n" class="st-ticker__group">
        <span class="st-ticker__item">STRIKER</span>
        <span class="st-ticker__dot"></span>
        <span class="st-ticker__item">VẢI THOÁNG KHÍ</span>
        <span class="st-ticker__dot"></span>
        <span class="st-ticker__item">CO GIÃN 4 CHIỀU</span>
        <span class="st-ticker__dot"></span>
        <span class="st-ticker__item">THẤM HÚT MỒ HÔI</span>
        <span class="st-ticker__dot"></span>
        <span class="st-ticker__item">SIZE S ĐẾN 3XL</span>
        <span class="st-ticker__dot"></span>
        <span class="st-ticker__item">ĐỔI TRẢ 7 NGÀY</span>
        <span class="st-ticker__dot"></span>
      </span>
    </div>
  </div>

  <!-- Collections Section -->
  <div class="st-dark">
    <section
      v-for="collection in collections"
      :key="collection.title"
      :id="slugify(collection.title)"
      class="st-collection"
    >
      <!-- Số áo khổng lồ làm mốc bộ sưu tập -->
      <span class="st-jersey" aria-hidden="true">{{ collection.jersey }}</span>

      <div class="st-container">
        <!-- Section Header -->
        <header class="st-collection__head">
          <div class="st-eyebrow">
            <span class="st-eyebrow__bar"></span>
            <span>{{ collection.tagline }}</span>
          </div>
          <h2 class="st-display">{{ collection.title }}</h2>
          <p class="st-lead">
            Áo được chọn theo đúng nhu cầu vận động: chất vải, độ co giãn và form dáng phù hợp với từng buổi tập.
          </p>
        </header>

        <!-- Products Container -->
        <div class="st-panel group">
          <div :class="['st-fade', { 'is-hidden': collectionPagination[collection.title].isFading }]">
            <CardList
              :items="paginatedCollectionItems(collection.title)"
              @add-to-cart="onClickAddPlus"
              @toggle-favorite="onToggleFavorite"
              :is-loading="false"
            />
          </div>

          <!-- Navigation Arrows -->
          <button
            v-if="collectionPagination[collection.title].currentPage > 0"
            @click="prevCollectionPage(collection.title)"
            class="st-nav st-nav--prev"
            aria-label="Xem nhóm áo trước"
          >
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M15 19l-7-7 7-7" />
            </svg>
          </button>

          <button
            v-if="collectionItems[collection.title].length > (collectionPagination[collection.title].currentPage + 1) * collectionPagination[collection.title].itemsPerPage"
            @click="nextCollectionPage(collection.title)"
            class="st-nav st-nav--next"
            aria-label="Xem nhóm áo tiếp theo"
          >
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9 5l7 7-7 7" />
            </svg>
          </button>

          <!-- Progress Indicators -->
          <div class="st-dots">
            <span
              v-for="page in collectionPagination[collection.title].totalPages"
              :key="page"
              :class="['st-dot', { 'is-active': collectionPagination[collection.title].currentPage === page - 1 }]"
            ></span>
          </div>
        </div>
      </div>
    </section>
  </div>
  <!-- End Collections Section -->

  <!-- All Products Section -->
  <section ref="productsSection" class="st-all">
    <div class="st-container">
      <!-- Section Header -->
      <header class="st-all__head">
        <div class="st-eyebrow st-eyebrow--light">
          <span class="st-eyebrow__bar"></span>
          <span>Toàn bộ kho áo</span>
        </div>
        <h2 class="st-display st-display--ink">TẤT CẢ SẢN PHẨM</h2>
        <p class="st-lead st-lead--ink">
          Toàn bộ áo thể thao STRIKER, đủ môn và đủ size. Lọc theo tên hoặc sắp xếp theo giá để tìm nhanh chiếc áo bạn cần.
        </p>
      </header>

      <!-- Search and Filter Controls -->
      <div class="st-controls">
        <div class="st-field">
          <svg class="st-field__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
          </svg>
          <input
            @input="onChangeSearchInput"
            class="st-input"
            type="text"
            placeholder="Tìm áo theo tên..."
          />
        </div>

        <div class="st-field st-field--select">
          <select @change="onChangeSelect" class="st-select" aria-label="Sắp xếp sản phẩm">
            <option value="tenSanPham">Sắp xếp theo tên</option>
            <option value="price-asc">Giá: Thấp đến cao</option>
            <option value="price-desc">Giá: Cao đến thấp</option>
          </select>
          <svg class="st-field__caret" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 9l-7 7-7-7"></path>
          </svg>
        </div>

        <div class="st-count">
          <span class="st-count__num">{{ sortedAndFilteredItems.length }}</span>
          <span class="st-count__label">áo đang bán</span>
        </div>
      </div>

      <!-- Products Grid Container -->
      <div class="st-panel st-panel--light group">
        <div :class="['st-fade', { 'is-hidden': isAllItemsFading }]">
          <CardList
            :items="paginatedItems"
            @add-to-cart="onClickAddPlus"
            @toggle-favorite="onToggleFavorite"
            :is-loading="false"
          />
        </div>

        <button
          v-if="currentPage > 0"
          @click="prevPage"
          class="st-nav st-nav--prev"
          aria-label="Trang sản phẩm trước"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M15 19l-7-7 7-7" />
          </svg>
        </button>

        <button
          v-if="currentPage < totalPages - 1"
          @click="nextPage"
          class="st-nav st-nav--next"
          aria-label="Trang sản phẩm sau"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>

      <!-- Pagination -->
      <nav class="st-pager" aria-label="Phân trang sản phẩm">
        <p class="st-pager__info">
          Trang <strong>{{ currentPage + 1 }}</strong> trên <strong>{{ totalPages }}</strong>
        </p>
        <div class="st-pager__list">
          <button
            v-for="page in totalPages"
            :key="page"
            @click="onPageClick(page)"
            :class="['st-page', { 'is-active': currentPage === page - 1 }]"
          >
            {{ page }}
          </button>
        </div>
      </nav>
    </div>
  </section>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Archivo:wght@500;700&family=Barlow+Condensed:wght@600;700;800&family=Barlow:wght@400;500;600&display=swap');

/* ===== Bảng màu STRIKER ===== */
.st-ticker,
.st-dark,
.st-all {
  --ink: #10141a;
  --ink-soft: #1b212b;
  --strike: #ff3b18;
  --cobalt: #1b3bff;
  --chalk: #ffffff;
  --mist: #eef1f5;
  --steel: #8c98a8;
}

.st-container {
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 1.25rem;
}

/* ===== Ticker LED ===== */
.st-ticker {
  background: var(--strike);
  color: var(--chalk);
  overflow: hidden;
  border-top: 3px solid var(--ink);
  border-bottom: 3px solid var(--ink);
}

.st-ticker__track {
  display: flex;
  width: max-content;
  animation: st-scroll 26s linear infinite;
}

.st-ticker__group {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 0.6rem 0.75rem;
}

.st-ticker__item {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.95rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  white-space: nowrap;
}

.st-ticker__dot {
  width: 6px;
  height: 6px;
  background: var(--ink);
  transform: rotate(45deg);
  flex-shrink: 0;
}

@keyframes st-scroll {
  from { transform: translateX(0); }
  to { transform: translateX(-50%); }
}

/* ===== Khối bộ sưu tập nền tối ===== */
.st-dark {
  background: var(--ink);
  background-image:
    repeating-linear-gradient(
      115deg,
      rgba(255, 255, 255, 0.022) 0 2px,
      transparent 2px 74px
    );
  padding: 4.5rem 0 5rem;
}

.st-collection {
  position: relative;
  padding: 3.5rem 0;
}

.st-collection + .st-collection {
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

/* Số áo khổng lồ - dấu ấn riêng của STRIKER */
.st-jersey {
  position: absolute;
  top: 1.5rem;
  right: 2rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: clamp(7rem, 20vw, 17rem);
  line-height: 0.8;
  letter-spacing: -0.04em;
  color: transparent;
  -webkit-text-stroke: 2px rgba(255, 255, 255, 0.09);
  pointer-events: none;
  user-select: none;
}

.st-collection__head {
  position: relative;
  z-index: 1;
  max-width: 46rem;
  margin-bottom: 2.75rem;
}

.st-eyebrow {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
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

.st-eyebrow--light {
  color: var(--cobalt);
}

.st-eyebrow--light .st-eyebrow__bar {
  background: var(--cobalt);
}

.st-display {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: clamp(2.75rem, 7vw, 5rem);
  line-height: 0.92;
  letter-spacing: -0.015em;
  text-transform: uppercase;
  color: var(--chalk);
  margin: 0 0 1rem;
}

.st-display--ink {
  color: var(--ink);
}

.st-lead {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 1.05rem;
  line-height: 1.65;
  color: var(--steel);
  margin: 0;
}

.st-lead--ink {
  color: #4a5563;
}

/* ===== Khung sản phẩm ===== */
.st-panel {
  position: relative;
  background: rgba(255, 255, 255, 0.035);
  border: 1px solid rgba(255, 255, 255, 0.09);
  border-radius: 4px;
  padding: 2rem 1.5rem 1.5rem;
}

.st-panel--light {
  background: var(--mist);
  border: 1px solid #dde3ea;
}

.st-fade {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.st-fade.is-hidden {
  opacity: 0;
  transform: translateY(6px);
}

/* Nút điều hướng */
.st-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 3rem;
  height: 3rem;
  display: grid;
  place-items: center;
  background: var(--strike);
  color: var(--chalk);
  border: none;
  border-radius: 2px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.25s ease, background 0.25s ease;
  z-index: 20;
}

.st-nav svg {
  width: 1.5rem;
  height: 1.5rem;
}

.group:hover .st-nav,
.st-nav:focus-visible {
  opacity: 1;
}

.st-nav:hover {
  background: var(--ink);
}

.st-panel--light .st-nav:hover {
  background: var(--cobalt);
}

.st-nav--prev { left: -1.5rem; }
.st-nav--next { right: -1.5rem; }

/* Chấm chỉ trang */
.st-dots {
  display: flex;
  justify-content: center;
  gap: 0.4rem;
  margin-top: 1.75rem;
}

.st-dot {
  width: 1.5rem;
  height: 3px;
  background: rgba(255, 255, 255, 0.2);
  transition: background 0.3s ease, width 0.3s ease;
}

.st-dot.is-active {
  width: 3rem;
  background: var(--strike);
}

/* ===== Khối tất cả sản phẩm ===== */
.st-all {
  background: var(--chalk);
  padding: 5rem 0 6rem;
}

.st-all__head {
  max-width: 48rem;
  margin-bottom: 2.5rem;
}

.st-controls {
  display: flex;
  flex-wrap: wrap;
  align-items: stretch;
  gap: 0.75rem;
  padding: 1rem;
  margin-bottom: 2rem;
  background: var(--ink);
  border-radius: 4px;
}

.st-field {
  position: relative;
  display: flex;
  align-items: center;
  flex: 1 1 16rem;
}

.st-field__icon {
  position: absolute;
  left: 0.9rem;
  width: 1.15rem;
  height: 1.15rem;
  color: var(--steel);
  pointer-events: none;
}

.st-input,
.st-select {
  width: 100%;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.98rem;
  color: var(--chalk);
  background: var(--ink-soft);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 3px;
  padding: 0.85rem 1rem 0.85rem 2.75rem;
  transition: border-color 0.2s ease;
}

.st-select {
  padding-left: 1rem;
  padding-right: 2.5rem;
  appearance: none;
  cursor: pointer;
}

.st-input::placeholder {
  color: var(--steel);
}

.st-input:focus,
.st-select:focus {
  outline: none;
  border-color: var(--strike);
}

.st-field--select {
  flex: 0 1 15rem;
}

.st-field__caret {
  position: absolute;
  right: 0.9rem;
  width: 1.1rem;
  height: 1.1rem;
  color: var(--strike);
  pointer-events: none;
}

.st-count {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0 1.25rem;
  background: var(--strike);
  border-radius: 3px;
}

.st-count__num {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: 1.9rem;
  line-height: 1;
  color: var(--chalk);
}

.st-count__label {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.8rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.85);
  max-width: 5rem;
  line-height: 1.15;
}

/* ===== Phân trang ===== */
.st-pager {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.25rem;
  margin-top: 2.5rem;
}

.st-pager__info {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 0.95rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #4a5563;
  margin: 0;
}

.st-pager__info strong {
  color: var(--strike);
}

.st-pager__list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.4rem;
}

.st-page {
  min-width: 2.75rem;
  height: 2.75rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 1.05rem;
  color: var(--ink);
  background: var(--chalk);
  border: 1px solid #d5dbe3;
  border-radius: 2px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.st-page:hover {
  border-color: var(--ink);
}

.st-page.is-active {
  background: var(--ink);
  border-color: var(--ink);
  color: var(--chalk);
}

/* ===== Focus rõ ràng cho bàn phím ===== */
.st-nav:focus-visible,
.st-page:focus-visible,
.st-input:focus-visible,
.st-select:focus-visible {
  outline: 3px solid var(--cobalt);
  outline-offset: 2px;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .st-nav--prev { left: 0.25rem; }
  .st-nav--next { right: 0.25rem; }
  .st-nav { opacity: 1; }
  .st-jersey { opacity: 0.5; }
  .st-controls { padding: 0.75rem; }
  .st-count { flex: 1 1 100%; justify-content: center; padding: 0.75rem; }
  .st-count__label { max-width: none; }
}

/* ===== Tôn trọng thiết lập giảm chuyển động ===== */
@media (prefers-reduced-motion: reduce) {
  .st-ticker__track { animation: none; }
  .st-fade,
  .st-nav,
  .st-dot,
  .st-page { transition: none; }
}
</style>