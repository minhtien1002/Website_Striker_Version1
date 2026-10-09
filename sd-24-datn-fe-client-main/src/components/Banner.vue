<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

// Dữ liệu cho banner slider
const banners = [
  {
    // Sân vận động về đêm, khán đài kín người — đúng tinh thần "chuẩn phong độ sân cỏ".
    // Ảnh trước đây trỏ '/public/banner2.jpg': sai đường dẫn (Vite serve file tĩnh
    // từ gốc '/', không phải '/public/') nên banner bị vỡ hình từ đầu.
    image: 'https://images.pexels.com/photos/35898730/pexels-photo-35898730.jpeg?auto=compress&cs=tinysrgb&w=1600',
    title: 'Chuẩn Phong Độ Sân Cỏ',
    description:
      'Tự hào khoác lên mình những mẫu áo thể thao mang tinh thần thi đấu, kết hợp giữa thiết kế hiện đại và chất vải cao cấp.'
  },
  {
    // Vận động viên chạy bộ giờ vàng — khớp chủ đề "bền bỉ trên đường chạy".
    image: 'https://images.pexels.com/photos/29840338/pexels-photo-29840338.jpeg?auto=compress&cs=tinysrgb&w=1600',
    title: 'Bền Bỉ Trên Từng Đường Chạy',
    description:
      'Mỗi chiếc áo là một người bạn đồng hành bền bỉ, mang đến sự thoải mái và phong cách riêng cho mọi buổi tập.'
  },
  {
    // Sân bóng phủ hoàng hôn, đúng tinh thần "kết nối đam mê".
    image: 'https://images.pexels.com/photos/15207755/pexels-photo-15207755.jpeg?auto=compress&cs=tinysrgb&w=1600',
    title: 'STRIKER - Kết Nối Đam Mê',
    description:
      'Thương hiệu được sinh ra từ tình yêu thể thao. Chúng tôi mong muốn rằng những sản phẩm không chỉ để mặc, mà còn để tự hào.'
  }
]

// Trạng thái reactive cho index banner hiện tại và trạng thái chuyển động
const currentBannerIndex = ref(0)
const isTransitioning = ref(false)
let autoSlideInterval = null

// Thuộc tính tính toán để lấy đối tượng banner hiện tại
const currentBanner = computed(() => banners[currentBannerIndex.value])

// Cập nhật emit để chấp nhận tên bộ sưu tập
const emit = defineEmits(['scrollToProducts', 'scrollToCollection'])

// Chức năng chuyển đến banner tiếp theo
const nextBanner = () => {
  if (isTransitioning.value) return
  isTransitioning.value = true
  setTimeout(() => {
    currentBannerIndex.value = (currentBannerIndex.value + 1) % banners.length
    isTransitioning.value = false
  }, 500)
}

// Chức năng chuyển đến banner trước đó
const prevBanner = () => {
  if (isTransitioning.value) return
  isTransitioning.value = true
  setTimeout(() => {
    currentBannerIndex.value = (currentBannerIndex.value - 1 + banners.length) % banners.length
    isTransitioning.value = false
  }, 500)
}

// Chức năng chuyển đến một banner cụ thể theo index
const goToBanner = (index) => {
  if (isTransitioning.value || index === currentBannerIndex.value) return
  isTransitioning.value = true
  setTimeout(() => {
    currentBannerIndex.value = index
    isTransitioning.value = false
  }, 500)
}

// Chức năng bắt đầu interval tự động trượt
const startAutoSlide = () => {
  clearInterval(autoSlideInterval)
  autoSlideInterval = setInterval(() => {
    nextBanner()
  }, 20000) // 20000ms = 20 giây
}

// Lifecycle hook: onMounted để bắt đầu tự động trượt
onMounted(() => {
  startAutoSlide()
})

// Lifecycle hook: onUnmounted để xóa interval tự động trượt
onUnmounted(() => {
  clearInterval(autoSlideInterval)
})

// Cập nhật hàm xử lý click để truyền tên bộ sưu tập
const scrollToCollection = (collectionTitle) => {
  emit('scrollToProducts', collectionTitle);
};

// Hàm mới, để cuộn đến phần 'Tất cả sản phẩm'
const scrollToProducts = () => {
  emit('scrollToProducts', 'Tất cả sản phẩm');
}
</script>

<template>
  <div class="container mx-auto px-4 mt-0 mb-8">
    <section class="relative overflow-hidden rounded-xl banner-container">
      <!-- Banner Image -->
      <div class="absolute inset-0 bg-cover bg-center transition-opacity duration-500"
        :style="{ backgroundImage: `url(${currentBanner.image})`, opacity: isTransitioning ? 0 : 1 }"></div>
      <!-- Overlay -->
      <div class="absolute inset-0 bg-black opacity-50"></div>
      <!-- Banner Content -->
      <div class="relative z-10 h-full flex items-center text-white">
        <div class="w-full px-4 sm:px-6 lg:px-8">
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 lg:gap-12 items-center min-h-full">
          <!-- Left Content -->
          <div class="banner-content text-center lg:text-left order-2 lg:order-1">
            <h2 class="text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-extrabold mb-4 sm:mb-6 animate-fade-in-up leading-tight"
              v-html="currentBanner.title"></h2>
            <p class="text-sm sm:text-base md:text-lg lg:text-xl mb-6 sm:mb-8 animate-fade-in-up delay-200 leading-relaxed opacity-90">
              {{ currentBanner.description }}
            </p>
            <div class="flex flex-col sm:flex-row gap-4 justify-center lg:justify-start">
              <button @click="scrollToProducts"
                class="bg-accent-500 hover:bg-accent-600 text-white font-semibold py-3 px-8 md:py-4 md:px-10 rounded-full transition-all duration-300 transform hover:scale-105 shadow-lg text-sm sm:text-base md:text-lg">
                Mua Ngay
              </button>
              <button @click="scrollToCollection('ÁO BÓNG ĐÁ')"
                class="border-2 border-white text-white hover:bg-white hover:text-gray-900 font-semibold py-3 px-8 md:py-4 md:px-10 rounded-full transition-all duration-300 text-sm sm:text-base md:text-lg">
                Khám Phá
              </button>
            </div>
          </div>
          
          <!-- Right Content - Visual Element -->
          <div class="order-1 lg:order-2 flex justify-center lg:justify-end">
            <div class="relative">
              <!-- Decorative elements for visual balance -->
              <div class="absolute -top-4 -right-4 w-24 h-24 bg-accent-500 rounded-full opacity-20 animate-pulse"></div>
              <div class="absolute -bottom-6 -left-6 w-16 h-16 bg-white rounded-full opacity-10"></div>
              
              <!-- Brand highlight -->
              <div class="bg-white bg-opacity-10 backdrop-blur-sm rounded-2xl p-6 sm:p-8 border border-white border-opacity-20">
                <div class="text-center">
                  <div class="text-4xl sm:text-5xl md:text-6xl font-bold mb-2 text-accent-300">
                    STRIKER
                  </div>
                  <div class="text-sm sm:text-base text-white opacity-80 font-medium">
                    Chính hãng · Chuẩn phong độ
                  </div>
                  <div class="mt-4 flex justify-center space-x-2">
                    <div class="w-2 h-2 bg-accent-400 rounded-full animate-bounce"></div>
                    <div class="w-2 h-2 bg-accent-400 rounded-full animate-bounce" style="animation-delay: 0.1s"></div>
                    <div class="w-2 h-2 bg-accent-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          </div>
        </div>
      </div>
      <!-- Navigation Arrows -->
      <button @click="prevBanner"
        class="absolute left-2 sm:left-4 top-1/2 -translate-y-1/2 p-1.5 sm:p-2 bg-white bg-opacity-30 hover:bg-opacity-50 transition-colors duration-300 rounded-full z-20">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 sm:h-8 sm:w-8 text-white" fill="none" viewBox="0 0 24 24"
          stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
      </button>
      <button @click="nextBanner"
        class="absolute right-2 sm:right-4 top-1/2 -translate-y-1/2 p-1.5 sm:p-2 bg-white bg-opacity-30 hover:bg-opacity-50 transition-colors duration-300 rounded-full z-20">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 sm:h-8 sm:w-8 text-white" fill="none" viewBox="0 0 24 24"
          stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
        </svg>
      </button>
      <!-- Navigation Dots -->
      <div class="absolute bottom-4 sm:bottom-6 left-1/2 -translate-x-1/2 flex gap-2 z-20">
        <span v-for="(banner, index) in banners" :key="index" @click="goToBanner(index)" :class="[
          'h-2.5 w-2.5 sm:h-3 sm:w-3 rounded-full cursor-pointer transition-colors duration-300',
          index === currentBannerIndex
            ? 'bg-white'
            : 'bg-gray-400 bg-opacity-50 hover:bg-opacity-70'
        ]">
        </span>
      </div>
    </section>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Josefin+Sans:wght@300;400;500;600;700&display=swap');

h2 {
  font-family: 'Josefin Sans', sans-serif;
}

.animate-fade-in-up {
  animation: fadeInUp 1s ease-in-out;
}

.animate-fade-in-up.delay-200 {
  animation-delay: 0.2s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.cta-button {
  background-color: #f7f7f7;
  color: #1a1a1a;
  font-weight: 700;
  padding: 12px 32px;
  border-radius: 9999px;
  box-shadow:
    0 10px 15px -3px rgba(0, 0, 0, 0.1),
    0 4px 6px -2px rgba(0, 0, 0, 0.05);
  transition-property: background-color, transform, box-shadow;
  transition-duration: 300ms;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
}

.cta-button:hover {
  background-color: #e5e7eb;
  transform: scale(1.05);
  box-shadow:
    0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.banner-container {
  margin-left: auto;
  margin-right: auto;
  padding-left: 1rem;
  padding-right: 1rem;
  height: calc(100vh - 80px);
  max-height: 700px;
  min-height: 400px;
}

/* Responsive height adjustments */
@media (max-width: 640px) {
  .banner-container {
    height: calc(100vh - 60px);
    min-height: 350px;
    max-height: 500px;
  }
}

@media (min-width: 641px) and (max-width: 1024px) {
  .banner-container {
    height: calc(100vh - 70px);
    min-height: 400px;
    max-height: 600px;
  }
}

@media (min-width: 1025px) {
  .banner-container {
    height: calc(100vh - 80px);
    min-height: 500px;
    max-height: 700px;
  }
}
</style>
