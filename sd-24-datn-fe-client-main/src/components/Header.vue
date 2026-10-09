<script setup>
import { useMainStore } from '../stores/MainStore.ts'
import { useCartStore } from '../stores/cartStore.js'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import authService from '../services/authService.js'
import { useRouter } from 'vue-router'
import { useAvatar } from '@/composables/useAvatar.js'

const mainStore = useMainStore()
const { isDeviceMobile } = mainStore
const cartStore = useCartStore()
const { itemCount } = cartStore
const router = useRouter()

// Avatar composable
const { avatarUrl, initializeAvatar } = useAvatar()

// Real user data from auth service
const currentUser = ref({
  isLoggedIn: false,
  username: '',
  email: '',
  fullName: '',
  role: ''
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

// Avatar update handler
const handleAvatarUpdate = () => {
  // Avatar will be updated automatically through the composable
}

// Initialize user data on mount
onMounted(() => {
  updateUserData()
  initializeAvatar()

  // Listen for avatar updates
  window.addEventListener('avatarUpdated', handleAvatarUpdate)
})

// Cleanup on unmount
onUnmounted(() => {
  window.removeEventListener('avatarUpdated', handleAvatarUpdate)
})

// Computed property for display name
const displayName = computed(() => {
  if (!currentUser.value.isLoggedIn) {
    return 'Tài khoản'
  }
  return currentUser.value.username || currentUser.value.fullName || 'Tài khoản'
})
defineProps({
  totalPrice: Number
})

// Mobile menu state
const isMobileMenuOpen = ref(false)
// Account dropdown state
const isAccountDropdownOpen = ref(false)

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

const toggleAccountDropdown = () => {
  isAccountDropdownOpen.value = !isAccountDropdownOpen.value
}

// Close dropdown when clicking outside
const closeAccountDropdown = () => {
  isAccountDropdownOpen.value = false
}

// Handle logout
const handleLogout = async () => {
  try {
    await authService.logout()
    updateUserData()
    closeAccountDropdown()
    router.push('/auth')
  } catch (error) {
    console.error('Logout error:', error)
    // Still update UI even if logout API fails
    updateUserData()
    closeAccountDropdown()
    router.push('/auth')
  }
}
</script>

<template>
  <header class="st-header">
    <div class="st-container st-header__inner">
      <!-- Logo Section -->
      <router-link to="/home" class="st-brand">
        <img src="@/assets/logo.png" alt="STRIKER" class="st-brand__logo" />
        <span class="st-brand__text">
          <span class="st-brand__tagline">Chính hãng · Chuẩn phong độ</span>
        </span>
      </router-link>

      <!-- Navigation Menu -->
      <nav class="st-nav">
        <router-link to="/home" :class="['st-nav__link', { 'is-active': $route.name === 'home' }]">Trang chủ</router-link>
        <router-link to="/products" :class="['st-nav__link', { 'is-active': $route.name === 'products' }]">Sản phẩm</router-link>
        <router-link to="/discounts" :class="['st-nav__link', { 'is-active': $route.name === 'discounts' }]">Giảm giá</router-link>
        <router-link to="/orders" :class="['st-nav__link', { 'is-active': $route.name === 'orders' }]">Đơn hàng</router-link>
        <router-link to="/support" :class="['st-nav__link', { 'is-active': $route.name === 'support' }]">Hỗ trợ</router-link>
      </nav>

      <!-- Right Section -->
      <div class="st-actions">
        <!-- Mobile menu button -->
        <button @click="toggleMobileMenu" class="st-icon-btn st-icon-btn--mobile">
          <svg :class="{ 'is-open': isMobileMenuOpen }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              :d="isMobileMenuOpen ? 'M6 18L18 6M6 6l12 12' : 'M4 6h16M4 12h16M4 18h16'"></path>
          </svg>
        </button>

        <!-- Account Dropdown -->
        <div class="st-account">
          <button @click="toggleAccountDropdown" class="st-account__trigger">
            <span class="st-avatar">
              <img v-if="currentUser.isLoggedIn && avatarUrl" :src="avatarUrl" :alt="displayName">
              <svg v-else fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
              </svg>
              <span v-if="currentUser.isLoggedIn" class="st-avatar__dot"></span>
            </span>
            <span v-if="!isDeviceMobile" class="st-account__name">{{ displayName }}</span>
            <svg v-if="!isDeviceMobile" :class="['st-account__caret', { 'is-open': isAccountDropdownOpen }]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
            </svg>
          </button>

          <!-- Dropdown Menu -->
          <div v-if="isAccountDropdownOpen" class="st-dropdown" @click.stop>
            <div class="st-dropdown__head">
              <span class="st-avatar st-avatar--lg">
                <img v-if="currentUser.isLoggedIn && avatarUrl" :src="avatarUrl" :alt="displayName">
                <svg v-else fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
              </span>
              <div v-if="currentUser.isLoggedIn" class="st-dropdown__user">
                <p class="st-dropdown__user-name">{{ currentUser.fullName }}</p>
                <p class="st-dropdown__user-email">{{ currentUser.email }}</p>
                <span class="st-dropdown__role">{{ currentUser.role }}</span>
              </div>
              <div v-else class="st-dropdown__user">
                <p class="st-dropdown__user-name">Chưa đăng nhập</p>
                <p class="st-dropdown__user-email">Đăng nhập để trải nghiệm</p>
              </div>
            </div>

            <div class="st-dropdown__body">
              <!-- Guest User Options -->
              <router-link v-if="!currentUser.isLoggedIn" to="/auth" @click="closeAccountDropdown" class="st-dropdown__item">
                <span class="st-dropdown__item-icon">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"></path>
                  </svg>
                </span>
                <span>
                  <p class="st-dropdown__item-title">Đăng nhập / Đăng ký</p>
                  <p class="st-dropdown__item-sub">Truy cập tài khoản</p>
                </span>
              </router-link>

              <!-- Logged In User Options -->
              <template v-if="currentUser.isLoggedIn">
                <a v-if="authService.canAccessAdmin()" href="http://localhost:5173" target="_blank" @click="closeAccountDropdown" class="st-dropdown__item st-dropdown__item--border">
                  <span class="st-dropdown__item-icon st-dropdown__item-icon--work">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                    </svg>
                  </span>
                  <span class="st-dropdown__item-flex">
                    <p class="st-dropdown__item-title">Quản trị hệ thống</p>
                    <p class="st-dropdown__item-sub">Bảng điều khiển admin</p>
                  </span>
                </a>

                <router-link to="/account/overview" @click="closeAccountDropdown" class="st-dropdown__item">
                  <span class="st-dropdown__item-icon st-dropdown__item-icon--cobalt">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v4"></path>
                    </svg>
                  </span>
                  <p class="st-dropdown__item-title">Tổng quan</p>
                </router-link>

                <router-link to="/account/purchase-history" @click="closeAccountDropdown" class="st-dropdown__item">
                  <span class="st-dropdown__item-icon st-dropdown__item-icon--done">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
                    </svg>
                  </span>
                  <p class="st-dropdown__item-title">Lịch sử mua hàng</p>
                </router-link>

                <router-link to="/account/profile" @click="closeAccountDropdown" class="st-dropdown__item">
                  <span class="st-dropdown__item-icon st-dropdown__item-icon--cobalt">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                    </svg>
                  </span>
                  <p class="st-dropdown__item-title">Thông tin tài khoản</p>
                </router-link>

                <router-link to="/account/feedback" @click="closeAccountDropdown" class="st-dropdown__item">
                  <span class="st-dropdown__item-icon st-dropdown__item-icon--cobalt">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
                    </svg>
                  </span>
                  <p class="st-dropdown__item-title">Góp ý - Phản hồi</p>
                </router-link>

                <div class="st-dropdown__divider">
                  <button @click="handleLogout" class="st-dropdown__item st-dropdown__item--danger">
                    <span class="st-dropdown__item-icon st-dropdown__item-icon--danger">
                      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
                      </svg>
                    </span>
                    <p class="st-dropdown__item-title">Đăng xuất</p>
                  </button>
                </div>
              </template>
            </div>
          </div>
        </div>

        <!-- Cart Button -->
        <router-link to="/cart" class="st-cart">
          <span class="st-icon-btn">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
            </svg>
          </span>
          <span v-if="itemCount > 0" class="st-cart__badge">{{ itemCount > 9 ? '9+' : itemCount }}</span>
        </router-link>
      </div>
    </div>
  </header>

  <!-- Mobile Navigation Menu -->
  <div v-if="isMobileMenuOpen" class="st-mobile-nav">
    <div class="st-container">
      <div class="st-mobile-nav__grid">
        <router-link to="/home" @click="isMobileMenuOpen = false" :class="['st-mobile-nav__link', { 'is-active': $route.name === 'home' }]">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path></svg>
          Trang chủ
        </router-link>
        <router-link to="/products" @click="isMobileMenuOpen = false" :class="['st-mobile-nav__link', { 'is-active': $route.name === 'products' }]">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"></path></svg>
          Sản phẩm
        </router-link>
        <router-link to="/discounts" @click="isMobileMenuOpen = false" :class="['st-mobile-nav__link', { 'is-active': $route.name === 'discounts' }]">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"></path></svg>
          Giảm giá
        </router-link>
        <router-link to="/orders" @click="isMobileMenuOpen = false" :class="['st-mobile-nav__link', { 'is-active': $route.name === 'orders' }]">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>
          Đơn hàng
        </router-link>
        <router-link to="/support" @click="isMobileMenuOpen = false" :class="['st-mobile-nav__link', 'st-mobile-nav__link--wide', { 'is-active': $route.name === 'support' }]">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192L5.636 18.364M12 12h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          Hỗ trợ
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Font Nunito đã được tải chung ở index.html, không cần @import font riêng ở đây nữa. */

.st-header {
  --ink: #10141a;
  --strike: #ff3b18;
  --cobalt: #1b3bff;
  --chalk: #ffffff;
  --mist: #eef1f5;
  --line: #dde3ea;
  --steel: #8c98a8;
  --slate: #4a5563;
  --done: #0f7a3d;
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--line);
}

.st-container { width: 100%; max-width: 1280px; margin: 0 auto; padding: 0 1.25rem; }

.st-header__inner { display: flex; align-items: center; justify-content: space-between; height: 4rem; gap: 1rem; }

/* Brand */
.st-brand { display: flex; align-items: center; gap: 0.75rem; text-decoration: none; }
.st-brand__logo {
  height: 3.4rem; width: auto; flex-shrink: 0;
  transition: transform 0.2s ease;
}
.st-brand:hover .st-brand__logo { transform: scale(1.08); }
.st-brand__text { display: none; }
.st-brand__tagline { display: block; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.68rem; color: var(--steel); margin-top: 0.15rem; }

@media (min-width: 640px) {
  .st-brand__text { display: block; }
}

/* Nav */
.st-nav { display: none; align-items: center; gap: 0.25rem; }
@media (min-width: 1024px) {
  .st-nav { display: flex; }
}
.st-nav__link {
  position: relative;
  padding: 0.5rem 0.85rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--slate);
  text-decoration: none;
  border-radius: 4px;
  transition: all 0.2s ease;
}
.st-nav__link:hover { color: var(--ink); background: var(--mist); }
.st-nav__link.is-active { color: var(--strike); background: #fff1ee; }

/* Actions */
.st-actions { display: flex; align-items: center; gap: 0.4rem; }

.st-icon-btn {
  display: grid; place-items: center;
  width: 2.25rem; height: 2.25rem;
  border-radius: 50%;
  background: var(--mist);
  color: var(--slate);
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}
.st-icon-btn svg { width: 1.1rem; height: 1.1rem; transition: transform 0.2s ease; }
.st-icon-btn:hover { background: #fff1ee; color: var(--strike); }
.st-icon-btn--mobile { display: grid; }
@media (min-width: 1024px) {
  .st-icon-btn--mobile { display: none; }
}
.st-icon-btn--mobile svg.is-open { transform: rotate(90deg); }

/* Account */
.st-account { position: relative; }
.st-account__trigger { display: flex; align-items: center; gap: 0.5rem; padding: 0.35rem 0.6rem; background: transparent; border: none; border-radius: 6px; cursor: pointer; transition: background 0.2s ease; }
.st-account__trigger:hover { background: var(--mist); }
.st-avatar { position: relative; width: 2rem; height: 2rem; border-radius: 50%; overflow: hidden; border: 2px solid #fecaca; display: grid; place-items: center; background: var(--strike); color: var(--chalk); flex-shrink: 0; }
.st-avatar img { width: 100%; height: 100%; object-fit: cover; }
.st-avatar svg { width: 1rem; height: 1rem; }
.st-avatar--lg { width: 2.75rem; height: 2.75rem; }
.st-avatar__dot { position: absolute; bottom: -1px; right: -1px; width: 0.65rem; height: 0.65rem; background: var(--done); border: 2px solid var(--chalk); border-radius: 50%; }
.st-account__name { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 600; font-size: 0.85rem; color: var(--slate); max-width: 6rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.st-account__caret { width: 0.8rem; height: 0.8rem; color: var(--steel); transition: transform 0.2s ease; }
.st-account__caret.is-open { transform: rotate(180deg); }

/* Dropdown */
.st-dropdown { position: absolute; right: 0; top: calc(100% + 0.4rem); width: 15rem; background: var(--chalk); border: 1px solid var(--line); border-radius: 6px; box-shadow: 0 12px 30px rgba(16,20,26,0.15); overflow: hidden; z-index: 60; }
.st-dropdown__head { display: flex; align-items: center; gap: 0.65rem; padding: 0.9rem 0.9rem; background: var(--mist); border-bottom: 1px solid var(--line); }
.st-dropdown__user { min-width: 0; flex: 1; }
.st-dropdown__user-name { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 600; font-size: 0.85rem; color: var(--ink); margin: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.st-dropdown__user-email { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.75rem; color: var(--steel); margin: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.st-dropdown__role { display: inline-block; margin-top: 0.25rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.68rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.04em; background: var(--strike); color: var(--chalk); padding: 0.1rem 0.5rem; border-radius: 4px; }
.st-dropdown__body { padding: 0.35rem 0; }
.st-dropdown__item { display: flex; align-items: center; gap: 0.7rem; padding: 0.6rem 0.9rem; text-decoration: none; color: var(--slate); cursor: pointer; background: transparent; border: none; width: 100%; text-align: left; font: inherit; transition: background 0.2s ease; }
.st-dropdown__item:hover { background: var(--mist); }
.st-dropdown__item--border { border-bottom: 1px solid var(--line); }
.st-dropdown__item-icon { width: 1.85rem; height: 1.85rem; flex-shrink: 0; display: grid; place-items: center; background: #eef0ff; color: var(--cobalt); border-radius: 6px; }
.st-dropdown__item-icon svg { width: 1rem; height: 1rem; }
.st-dropdown__item-icon--cobalt { background: #eef0ff; color: var(--cobalt); }
.st-dropdown__item-icon--done { background: #eafbf1; color: var(--done); }
.st-dropdown__item-icon--work { background: #fff6e5; color: #b45309; }
.st-dropdown__item-icon--danger { background: #fff1ee; color: var(--strike); }
.st-dropdown__item-title { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 600; font-size: 0.85rem; color: var(--ink); margin: 0; }
.st-dropdown__item-sub { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.72rem; color: var(--steel); margin: 0; }
.st-dropdown__item-flex { flex: 1; }
.st-dropdown__item--danger { color: var(--strike); }
.st-dropdown__item--danger .st-dropdown__item-title { color: var(--strike); }
.st-dropdown__divider { border-top: 1px solid var(--line); margin-top: 0.35rem; padding-top: 0.35rem; }

/* Cart */
.st-cart { position: relative; text-decoration: none; }
.st-cart__badge { position: absolute; top: -0.2rem; right: -0.2rem; background: var(--strike); color: var(--chalk); font-size: 0.62rem; font-weight: 700; min-width: 1.1rem; height: 1.1rem; border-radius: 999px; display: grid; place-items: center; }

/* Mobile nav */
.st-mobile-nav { background: var(--chalk); border-bottom: 1px solid var(--line); }
@media (min-width: 1024px) {
  .st-mobile-nav { display: none; }
}
.st-mobile-nav__grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem; padding: 0.9rem 0; }
.st-mobile-nav__link { display: flex; align-items: center; gap: 0.5rem; padding: 0.65rem 0.75rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 600; font-size: 0.88rem; color: var(--slate); text-decoration: none; border-radius: 6px; }
.st-mobile-nav__link svg { width: 1.1rem; height: 1.1rem; }
.st-mobile-nav__link.is-active { color: var(--strike); background: #fff1ee; }
.st-mobile-nav__link--wide { grid-column: span 2; justify-content: center; }

@media (prefers-reduced-motion: reduce) {
  .st-brand__mark, .st-icon-btn, .st-icon-btn svg, .st-account__caret, .st-dropdown__item { transition: none; }
}
</style>
