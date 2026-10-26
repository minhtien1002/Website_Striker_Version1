<template>
  <div class="group relative bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-xl transition-all duration-500 transform hover:-translate-y-2">
    <!-- Product Image Container -->
    <div class="relative aspect-square overflow-hidden bg-gradient-to-br from-gray-50 to-gray-100">
      <!-- Multiple Images for Hover Effect -->
      <img
        :src="item.imageUrl"
        :alt="item.title"
        class="w-full h-full object-cover transition-opacity duration-500 group-hover:opacity-0"
        @error="handleImageError"
      />
      <img
        :src="getHoverImage(item.imageUrl)"
        :alt="item.title"
        class="absolute inset-0 w-full h-full object-cover opacity-0 transition-opacity duration-500 group-hover:opacity-100"
        @error="handleImageError"
      />
      
      <!-- Status Badge -->
      <div class="absolute top-3 left-3">
        <span
          v-if="item.productStatus === 'Hết hàng'"
          class="bg-red-500 text-white px-3 py-1 text-xs font-bold rounded-full"
        >
          Hết hàng
        </span>
        <span
          v-else-if="item.hasDiscount && item.campaignName"
          class="bg-gradient-to-r from-green-500 to-green-600 text-white px-3 py-1 text-xs font-bold rounded-full flex items-center gap-1 max-w-32"
        >
          <i class="fas fa-tag text-xs"></i>
          <span class="truncate">{{ item.campaignName }}</span>
        </span>
        <span
          v-else-if="item.discount"
          class="bg-red-500 text-white px-3 py-1 text-xs font-bold rounded-full"
        >
          -{{ item.discount }}%
        </span>
      </div>

      <!-- Quick Actions -->
      <div class="absolute top-3 right-3 flex flex-col gap-2 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
        <!-- Favorite Button -->
        <button
          @click.stop="toggleFavorite"
          class="w-10 h-10 bg-white rounded-full shadow-md flex items-center justify-center hover:bg-gray-50 transition-colors"
        >
          <svg
            :class="[
              'h-5 w-5 transition-colors',
              item.isFavorite ? 'text-red-500 fill-current' : 'text-gray-600'
            ]"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
          </svg>
        </button>

        <!-- Quick View Button -->
        <button
          @click.stop="quickView"
          class="w-10 h-10 bg-white rounded-full shadow-md flex items-center justify-center hover:bg-gray-50 transition-colors"
        >
          <svg class="h-5 w-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
          </svg>
        </button>
      </div>

      <!-- Action Buttons (appears on hover) -->
      <div class="absolute bottom-4 left-4 right-4 opacity-0 group-hover:opacity-100 transition-all duration-300 transform translate-y-4 group-hover:translate-y-0">
        <div v-if="item.productStatus === 'Hết hàng'" class="w-full">
          <button
            disabled
            class="w-full py-3 px-4 rounded-full font-semibold text-sm bg-gray-300 text-gray-500 cursor-not-allowed transition-all duration-300"
          >
            Hết hàng
          </button>
        </div>
        <div v-else class="flex gap-2">
          <!-- Add to Cart Button with Icon -->
          <button
            @click.stop="handleAddToCart"
            class="flex-1 py-3 px-4 rounded-full font-semibold text-sm bg-white text-black hover:bg-gray-100 shadow-lg hover:shadow-xl transition-all duration-300 flex items-center justify-center border border-gray-200"
            title="Thêm vào giỏ hàng"
          >
            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-1.5 6M7 13l-1.5-6m0 0h15M17 21a2 2 0 100-4 2 2 0 000 4zM9 21a2 2 0 100-4 2 2 0 000 4z"></path>
            </svg>
          </button>
          
          <!-- Buy Now Button -->
          <button
            @click.stop="handleBuyNow"
            class="flex-1 py-3 px-4 rounded-full font-semibold text-sm bg-black text-white hover:bg-gray-800 shadow-lg hover:shadow-xl transition-all duration-300"
          >
            Mua ngay
          </button>
        </div>
      </div>
    </div>

    <!-- Product Info -->
    <div class="p-5">
      <!-- Brand -->
      <p class="text-xs font-semibold text-blue-600 uppercase tracking-wider mb-1">
        {{ getBrand(item.title) }}
      </p>

      <!-- Product Name -->
      <h3 
        class="font-bold text-gray-900 mb-2 line-clamp-2 cursor-pointer hover:text-blue-600 transition-colors"
        @click="goToProduct"
      >
        {{ item.title }}
      </h3>

      <!-- Category -->
      <p class="text-sm text-gray-500 mb-3">{{ getCategory(item) }}</p>

      <!-- Price -->
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span class="text-xl font-bold text-gray-900">{{ formatPrice(item.price) }}</span>
          <span v-if="item.originalPrice" class="text-sm text-gray-500 line-through">
            {{ formatPrice(item.originalPrice) }}
          </span>
        </div>
        
        <!-- Stock Status -->
        <div class="text-right">
          <span
            :class="[
              'text-xs font-medium',
              item.totalStock > 10 ? 'text-green-600' : 
              item.totalStock > 0 ? 'text-yellow-600' : 'text-red-600'
            ]"
          >
            {{ item.totalStock > 0 ? `Còn ${item.totalStock}` : 'Hết hàng' }}
          </span>
        </div>
      </div>

      <!-- Color Options (if available) -->
      <div v-if="item.colors && item.colors.length > 0" class="mt-4">
        <div class="flex items-center justify-center gap-1">
          <div
            v-for="(color, index) in item.colors.slice(0, 5)"
            :key="index"
            :style="{ backgroundColor: color }"
            class="w-5 h-5 rounded-full border-2 border-white shadow-md hover:scale-125 transition-all duration-200 cursor-pointer relative"
            :title="getColorName(color)"
          >
            <!-- White border for light colors -->
            <div 
              v-if="color === '#FFFFFF' || color === '#FFD700'"
              class="absolute inset-0 rounded-full border border-gray-300"
            ></div>
          </div>
          <span v-if="item.colors.length > 5" class="text-xs text-gray-500 ml-1 font-medium">
            +{{ item.colors.length - 5 }}
          </span>
        </div>
      </div>
    </div>

    <!-- Hover Overlay for Better UX -->
    <div 
      class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-5 transition-all duration-300 pointer-events-none"
    ></div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps({
  item: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['add-to-cart', 'toggle-favorite', 'buy-now'])

// Methods
const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const getBrand = (title) => {
  const brands = ['Nike', 'Adidas', 'Puma', 'Kappa', 'Under Armour', 'New Balance', 'Reebok', 'Jordan', 'Fila', 'Asics']
  return brands.find(brand => title.includes(brand)) || 'STRIKER'
}

const getCategory = (product) => {
  // Use real category from backend if available
  if (product.danhMuc) {
    return product.danhMuc
  }
  if (product.categoryName) {
    return product.categoryName
  }
  // Fallback to title-based detection
  const title = product.title || product.name || ''
  if (title.includes('Chạy bộ') || title.includes('Running')) return 'Áo chạy bộ'
  if (title.includes('Bóng đá') || title.includes('Jersey')) return 'Áo bóng đá'
  if (title.includes('Gym') || title.includes('Tập luyện')) return 'Áo tập gym'
  if (title.includes('Polo')) return 'Áo polo'
  return 'Áo thể thao'
}

const getHoverImage = (originalImage) => {
  // Try to get alternative image by changing alt1 to alt2
  if (originalImage.includes('alt1')) {
    return originalImage.replace('alt1', 'alt2')
  }
  return originalImage
}

const handleImageError = (event) => {
  event.target.src = '/placeholder-product.svg'
}

const handleAddToCart = () => {
  // Chuyển hướng đến trang chi tiết sản phẩm để chọn màu sắc và kích cỡ
  router.push(`/product/${props.item.id}?action=add-to-cart`)
}

const handleBuyNow = () => {
  // Chuyển hướng đến trang chi tiết sản phẩm để chọn màu sắc và kích cỡ
  router.push(`/product/${props.item.id}?action=buy-now`)
}

const toggleFavorite = () => {
  emit('toggle-favorite', props.item)
}

const quickView = () => {
  // Open quick view modal or navigate to product detail
  goToProduct()
}

const goToProduct = () => {
  router.push(`/product/${props.item.id}`)
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
  return colorNames[color] || 'Màu khác'
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-clamp: 2; /* Standard property for compatibility */
}
</style>
