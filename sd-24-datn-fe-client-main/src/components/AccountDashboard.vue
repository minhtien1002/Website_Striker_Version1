<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50/30 to-purple-50/30">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex flex-col lg:flex-row gap-8">
        <!-- Sidebar Navigation -->
        <div class="lg:w-80 flex-shrink-0">
          <div class="bg-white/90 backdrop-blur-sm rounded-2xl shadow-xl border border-white/20 p-6 sticky top-8">
            <!-- User Profile Section -->
            <div class="text-center mb-6 pb-6 border-b border-gray-100">
              <!-- Avatar with gradient border -->
              <div class="relative mx-auto mb-4 w-24 h-24">
                <div class="absolute inset-0 bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 rounded-full p-1">
                  <div class="w-full h-full rounded-full overflow-hidden bg-white">
                    <img 
                      v-if="currentUser.isLoggedIn && avatarUrl"
                      :src="avatarUrl" 
                      :alt="displayName"
                      class="w-full h-full object-cover"
                    >
                    <div v-else class="w-full h-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
                      <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                      </svg>
                    </div>
                  </div>
                </div>
                <!-- Online status indicator -->
                <div class="absolute bottom-1 right-1 w-6 h-6 bg-green-500 border-3 border-white rounded-full flex items-center justify-center">
                  <div class="w-2 h-2 bg-green-300 rounded-full animate-pulse"></div>
                </div>
              </div>
              
              <!-- User info -->
              <div class="space-y-1">
                <h3 class="text-lg font-bold text-gray-900 truncate">{{ displayName }}</h3>
                <p class="text-sm text-gray-600 truncate">{{ displayEmail }}</p>
                <div v-if="displayCustomerCode !== 'N/A'" class="inline-flex items-center px-2 py-1 bg-blue-50 text-blue-700 text-xs font-medium rounded-full">
                  <svg class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"></path>
                  </svg>
                  {{ displayCustomerCode }}
                </div>
              </div>
              
              <!-- Status badge -->
              <div class="mt-3 inline-flex items-center px-3 py-1 bg-green-50 text-green-700 text-xs font-semibold rounded-full border border-green-200">
                <svg class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                Đã xác thực
              </div>
            </div>
            
            <!-- Navigation Menu -->
            <nav class="space-y-2">
              <router-link 
                to="/account/overview" 
                class="flex items-center px-3 py-3 text-gray-700 rounded-xl hover:bg-gradient-to-r hover:from-blue-50 hover:to-purple-50 hover:text-blue-600 transition-all duration-300 group transform hover:scale-105"
                :class="{ 'bg-gradient-to-r from-blue-500 to-purple-600 text-white shadow-lg': $route.name === 'account-overview' }"
              >
                <div class="w-8 h-8 rounded-lg flex items-center justify-center mr-3 transition-all duration-300"
                     :class="$route.name === 'account-overview' ? 'bg-white/20' : 'bg-blue-100 group-hover:bg-blue-200'">
                  <svg class="w-4 h-4" :class="$route.name === 'account-overview' ? 'text-white' : 'text-blue-600'" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v4"></path>
                  </svg>
                </div>
                <div class="flex-1">
                  <div class="font-semibold text-sm">Tổng quan</div>
                  <div class="text-xs opacity-75">Thống kê tài khoản</div>
                </div>
                <svg v-if="$route.name === 'account-overview'" class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                </svg>
              </router-link>
              
              <router-link 
                to="/account/purchase-history" 
                class="flex items-center px-3 py-3 text-gray-700 rounded-xl hover:bg-gradient-to-r hover:from-green-50 hover:to-emerald-50 hover:text-green-600 transition-all duration-300 group transform hover:scale-105"
                :class="{ 'bg-gradient-to-r from-green-500 to-emerald-600 text-white shadow-lg': $route.name === 'account-purchase-history' }"
              >
                <div class="w-8 h-8 rounded-lg flex items-center justify-center mr-3 transition-all duration-300"
                     :class="$route.name === 'account-purchase-history' ? 'bg-white/20' : 'bg-green-100 group-hover:bg-green-200'">
                  <svg class="w-4 h-4" :class="$route.name === 'account-purchase-history' ? 'text-white' : 'text-green-600'" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
                  </svg>
                </div>
                <div class="flex-1">
                  <div class="font-semibold text-sm">Lịch sử mua hàng</div>
                  <div class="text-xs opacity-75">Đơn hàng đã đặt</div>
                </div>
                <svg v-if="$route.name === 'account-purchase-history'" class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                </svg>
              </router-link>
              
              <router-link 
                to="/account/profile" 
                class="flex items-center px-3 py-3 text-gray-700 rounded-xl hover:bg-gradient-to-r hover:from-purple-50 hover:to-pink-50 hover:text-purple-600 transition-all duration-300 group transform hover:scale-105"
                :class="{ 'bg-gradient-to-r from-purple-500 to-pink-600 text-white shadow-lg': $route.name === 'account-profile' }"
              >
                <div class="w-8 h-8 rounded-lg flex items-center justify-center mr-3 transition-all duration-300"
                     :class="$route.name === 'account-profile' ? 'bg-white/20' : 'bg-purple-100 group-hover:bg-purple-200'">
                  <svg class="w-4 h-4" :class="$route.name === 'account-profile' ? 'text-white' : 'text-purple-600'" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                  </svg>
                </div>
                <div class="flex-1">
                  <div class="font-semibold text-sm">Thông tin tài khoản</div>
                  <div class="text-xs opacity-75">Hồ sơ & địa chỉ</div>
                </div>
                <svg v-if="$route.name === 'account-profile'" class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                </svg>
              </router-link>
              
              <router-link 
                to="/account/feedback" 
                class="flex items-center px-3 py-3 text-gray-700 rounded-xl hover:bg-gradient-to-r hover:from-orange-50 hover:to-yellow-50 hover:text-orange-600 transition-all duration-300 group transform hover:scale-105"
                :class="{ 'bg-gradient-to-r from-orange-500 to-yellow-600 text-white shadow-lg': $route.name === 'account-feedback' }"
              >
                <div class="w-8 h-8 rounded-lg flex items-center justify-center mr-3 transition-all duration-300"
                     :class="$route.name === 'account-feedback' ? 'bg-white/20' : 'bg-orange-100 group-hover:bg-orange-200'">
                  <svg class="w-4 h-4" :class="$route.name === 'account-feedback' ? 'text-white' : 'text-orange-600'" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
                  </svg>
                </div>
                <div class="flex-1">
                  <div class="font-semibold text-sm">Góp ý - Phản hồi</div>
                  <div class="text-xs opacity-75">Hỗ trợ khách hàng</div>
                </div>
                <svg v-if="$route.name === 'account-feedback'" class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                </svg>
              </router-link>
            </nav>
            
            <!-- Logout Button -->
            <div class="mt-6 pt-4 border-t border-gray-100">
              <button 
                @click="handleLogout"
                class="flex items-center w-full px-3 py-3 text-gray-700 rounded-xl hover:bg-gradient-to-r hover:from-red-50 hover:to-pink-50 hover:text-red-600 transition-all duration-300 group transform hover:scale-105"
              >
                <div class="w-8 h-8 rounded-lg bg-red-100 flex items-center justify-center mr-3 group-hover:bg-red-200 transition-all duration-300">
                  <svg class="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
                  </svg>
                </div>
                <div class="flex-1 text-left">
                  <div class="font-semibold text-sm">Đăng xuất</div>
                  <div class="text-xs opacity-75">Thoát tài khoản</div>
                </div>
              </button>
            </div>
          </div>
        </div>
        
        <!-- Main Content Area -->
        <div class="flex-1 min-w-0">
          <div class="bg-white/90 backdrop-blur-sm rounded-2xl shadow-xl border border-white/20 p-8 min-h-[600px]">
            <router-view />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import authService from '@/services/authService.js'
import { useAvatar } from '@/composables/useAvatar.js'
import { customerAPI } from '@/api/customerAPI.js'

const router = useRouter()

// Avatar composable
const { avatarUrl, initializeAvatar } = useAvatar()

// User data
const currentUser = ref({
  isLoggedIn: false,
  username: '',
  email: '',
  fullName: '',
  role: ''
})

// Customer profile data
const customerProfile = ref({
  ten: '',
  email: '',
  soDienThoai: '',
  ma: ''
})

// Update user data from auth service
const updateUserData = () => {
  const user = authService.getUser()
  const isAuthenticated = authService.isAuthenticated()

  currentUser.value = {
    isLoggedIn: isAuthenticated,
    username: user?.username || '',
    email: user?.email || '',
    fullName: user?.username || user?.email || '',
    role: authService.getUserRole()
  }
}

// Load customer profile
const loadCustomerProfile = async () => {
  try {
    if (!authService.isAuthenticated()) {
      router.push('/auth')
      return
    }

    const response = await customerAPI.getProfile()
    if (response.success) {
      customerProfile.value = response.data
    }
  } catch (error) {
    console.error('Error loading customer profile:', error)
  }
}

// Display name computed
const displayName = computed(() => {
  if (customerProfile.value.ten) {
    return customerProfile.value.ten
  }
  return currentUser.value.fullName || currentUser.value.username || 'Khách hàng'
})

// Display email computed
const displayEmail = computed(() => {
  return customerProfile.value.email || currentUser.value.email || 'email@example.com'
})

// Display customer code
const displayCustomerCode = computed(() => {
  return customerProfile.value.ma || 'N/A'
})

// Avatar update handler
const handleAvatarUpdate = () => {
  // Avatar will be updated automatically through the composable
}

// Logout handler
const handleLogout = async () => {
  try {
    await authService.logout()
    router.push('/auth')
  } catch (error) {
    console.error('Logout error:', error)
    router.push('/auth')
  }
}

// Initialize on mount
onMounted(() => {
  updateUserData()
  initializeAvatar()
  loadCustomerProfile()
  
  // Listen for avatar updates
  window.addEventListener('avatarUpdated', handleAvatarUpdate)
})

// Cleanup on unmount
onUnmounted(() => {
  window.removeEventListener('avatarUpdated', handleAvatarUpdate)
})
</script>

<style scoped>
/* Custom scrollbar for sidebar */
.sidebar-scroll::-webkit-scrollbar {
  width: 4px;
}

.sidebar-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-scroll::-webkit-scrollbar-thumb {
  background: rgba(156, 163, 175, 0.3);
  border-radius: 2px;
}

.sidebar-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(156, 163, 175, 0.5);
}

/* Smooth transitions for all interactive elements */
* {
  transition-property: all;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
}

/* Enhanced backdrop blur support */
@supports (backdrop-filter: blur(12px)) {
  .backdrop-blur-sm {
    backdrop-filter: blur(12px);
  }
}

/* Mobile responsive adjustments */
@media (max-width: 1024px) {
  .lg\:w-80 {
    width: 100%;
  }
  
  .sticky {
    position: relative;
  }
}

/* Animation for menu items */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

nav > * {
  animation: slideIn 0.3s ease-out;
}

nav > *:nth-child(1) { animation-delay: 0.1s; }
nav > *:nth-child(2) { animation-delay: 0.2s; }
nav > *:nth-child(3) { animation-delay: 0.3s; }
nav > *:nth-child(4) { animation-delay: 0.4s; }

/* Gradient text effect for active items */
.gradient-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Enhanced shadow effects */
.shadow-xl {
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

/* Hover glow effect */
.hover-glow:hover {
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.3);
}
</style>
