<template>
  <div class="admin-layout d-flex">
    <Sidebar :isSidebarActive="isSidebarActive" @toggle-sidebar="toggleSidebar" @show-logout-modal="showLogoutModal = true" />

    <div :class="['main-content-wrapper', { 'full-width': !isSidebarActive }]">
      <Header
        :total-price="totalPrice"
        :cart="cart"
        :is-sidebar-active="isSidebarActive"
        @toggle-sidebar="toggleSidebar"
        @toggle-theme="toggleTheme"
      />
      <div class="content-area">
        <router-view />
      </div>
      <Footer />
    </div>

    <!-- Logout Confirmation Modal -->
    <div v-if="showLogoutModal" class="modal-overlay" @click="closeLogoutModal">
      <div class="modal-content logout-confirm-modal" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">
            <iconify-icon icon="solar:logout-2-bold-duotone" class="logout-icon"></iconify-icon>
            Xác nhận đăng xuất
          </h3>
          <button class="close-modal" @click="closeLogoutModal">
            <iconify-icon icon="solar:close-circle-bold"></iconify-icon>
          </button>
        </div>
        <div class="modal-body">
          <p class="confirm-message">Bạn có chắc chắn muốn đăng xuất khỏi hệ thống không?</p>
          <div class="warning-note">
            <iconify-icon icon="solar:info-circle-bold"></iconify-icon>
            Bạn sẽ cần đăng nhập lại để tiếp tục sử dụng.
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn secondary" @click="closeLogoutModal">Hủy bỏ</button>
          <button class="btn danger" @click="confirmLogout">
            <iconify-icon icon="solar:logout-2-bold"></iconify-icon>
            Đăng xuất
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, provide, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import authService from '@/services/authService.js'

// Components
import Sidebar from '@/components/Sidebar.vue'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

// Route tracking
const route = useRoute()
const router = useRouter()

// UI state management
const isSidebarActive = ref(true)
const isDarkTheme = ref(localStorage.getItem('theme') === 'dark')
const showLogoutModal = ref(false)

const toggleSidebar = () => {
  isSidebarActive.value = !isSidebarActive.value
}

const toggleTheme = () => {
  isDarkTheme.value = !isDarkTheme.value
  document.body.classList.toggle('dark', isDarkTheme.value)
  localStorage.setItem('theme', isDarkTheme.value ? 'dark' : 'light')
}

onMounted(() => {
  if (isDarkTheme.value) {
    document.body.classList.add('dark')
  }
})

// Cart and favorites management
const cart = ref([])
const favorites = ref([])

const formatCurrency = (amount) => {
  if (typeof amount !== 'number') return '0 VND'
  let formatted = new Intl.NumberFormat('vi-VN', {
    style: 'decimal',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount)
  formatted = formatted.replace(/\./g, ' ')
  return `${formatted} VND`
}

const totalPrice = computed(() => {
  return cart.value.reduce((total, item) => total + item.price * item.quantity, 0)
})

const addToCart = (item) => {
  const existingItemIndex = cart.value.findIndex(
    cartItem => cartItem.id === item.id && cartItem.selectedSize === item.selectedSize
  )
  if (existingItemIndex > -1) {
    cart.value[existingItemIndex].quantity = (cart.value[existingItemIndex].quantity || 1) + 1
  } else {
    cart.value.push({ ...item, isAdded: true, quantity: 1 })
  }
}

const removeFromCart = (item) => {
  cart.value = cart.value.filter(
    cartItem => !(cartItem.id === item.id && cartItem.selectedSize === item.selectedSize)
  )
}

const addToFavorite = async (item) => {
  try {
    if (!item.isFavorite) {
      const { data } = await axios.post('https://a5687b208ca7ac57.mokky.dev/favorites', item)
      favorites.value.push({ ...data, isFavorite: true })
      item.isFavorite = true
    } else {
      const favorite = favorites.value.find(fav => fav.parentId === item.id || fav.id === item.id)
      if (favorite) {
        await axios.delete(`https://a5687b208ca7ac57.mokky.dev/favorites/${favorite.id}`)
        favorites.value = favorites.value.filter(fav => fav.id !== favorite.id)
        item.isFavorite = false
      }
    }
  } catch (err) {
    console.error('Error with favorites:', err)
  }
}

const clearCart = () => {
  cart.value = []
  localStorage.removeItem('cart')
}

watch(
  cart,
  () => {
    localStorage.setItem('cart', JSON.stringify(cart.value))
  },
  { deep: true }
)

const loadCartFromLocalStorage = () => {
  const savedCart = localStorage.getItem('cart')
  if (savedCart) {
    cart.value = JSON.parse(savedCart)
    cart.value.forEach(item => {
      item.isAdded = true
    })
  }
}
loadCartFromLocalStorage()

provide('cartActions', {
  cart,
  favorites,
  addToCart,
  removeFromCart,
  addToFavorite,
  formatCurrency,
  clearCart,
  totalPrice
})

// Logout modal functions
const closeLogoutModal = () => {
  showLogoutModal.value = false
}

const confirmLogout = async () => {
  try {
    // Đóng modal
    showLogoutModal.value = false
    
    // Gọi logout từ authService
    await authService.logout()
    
    // Redirect về trang login
    router.push('/login')
    
    // Hiển thị thông báo thành công (nếu có toast)
    if (window.$toast) {
      window.$toast.success('Đăng xuất thành công!')
    }
  } catch (error) {
    console.error('Logout error:', error)
    if (window.$toast) {
      window.$toast.error('Có lỗi xảy ra khi đăng xuất')
    }
  }
}

// Responsive handling
watch(route, () => {
  if (window.innerWidth <= 768 && isSidebarActive.value) {
    isSidebarActive.value = false
  }
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.main-content-wrapper {
  flex-grow: 1;
  padding-left: v-bind("isSidebarActive ? '280px' : '0'");
  width: 100vw;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

.content-area {
  flex-grow: 1;
  padding: 20px;
  background-color: #ffffff;
  position: relative;
  z-index: 1;
}

.dark .content-area {
  background-color: #1f2937;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  backdrop-filter: blur(4px);
}

.logout-confirm-modal {
  background: white;
  border-radius: 12px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  max-width: 450px;
  width: 90%;
  max-height: 90vh;
  overflow: hidden;
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  padding: 24px 24px 16px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.logout-icon {
  font-size: 24px;
  color: #f59e0b;
}

.close-modal {
  background: none;
  border: none;
  font-size: 24px;
  color: #6b7280;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  transition: all 0.2s;
}

.close-modal:hover {
  background-color: #f3f4f6;
  color: #374151;
}

.modal-body {
  padding: 20px 24px;
}

.confirm-message {
  font-size: 16px;
  color: #374151;
  margin: 0 0 16px 0;
  line-height: 1.5;
}

.warning-note {
  background-color: #fef3c7;
  border: 1px solid #f59e0b;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #92400e;
}

.warning-note iconify-icon {
  font-size: 18px;
  color: #f59e0b;
}

.modal-footer {
  padding: 16px 24px 24px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn.secondary {
  background-color: #f3f4f6;
  color: #374151;
}

.btn.secondary:hover {
  background-color: #e5e7eb;
}

.btn.danger {
  background-color: #dc2626;
  color: white;
}

.btn.danger:hover {
  background-color: #b91c1c;
}

/* Dark theme support */
.dark .logout-confirm-modal {
  background: #1f2937;
  color: #f9fafb;
}

.dark .modal-header {
  border-bottom-color: #374151;
}

.dark .modal-title {
  color: #f9fafb;
}

.dark .confirm-message {
  color: #d1d5db;
}

.dark .close-modal {
  color: #9ca3af;
}

.dark .close-modal:hover {
  background-color: #374151;
  color: #d1d5db;
}

.dark .btn.secondary {
  background-color: #374151;
  color: #d1d5db;
}

.dark .btn.secondary:hover {
  background-color: #4b5563;
}

@media (max-width: 768px) {
  .main-content-wrapper {
    padding-left: 0 !important;
    width: 100vw !important;
  }
  
  .logout-confirm-modal {
    margin: 20px;
    width: calc(100% - 40px);
  }
  
  .modal-footer {
    flex-direction: column-reverse;
  }
  
  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
