<template>
  <div class="st-page">
    <!-- Header -->
    <header class="st-head">
      <span class="st-head__jersey" aria-hidden="true">07</span>
      <div class="st-container st-head__inner">
        <div class="st-eyebrow">
          <span class="st-eyebrow__bar"></span>
          <span>Tài khoản STRIKER</span>
        </div>
        <h1 class="st-display">THÔNG TIN TÀI KHOẢN</h1>
        <p class="st-lead">Quản lý thông tin cá nhân và địa chỉ giao hàng của bạn một cách dễ dàng và an toàn.</p>
      </div>
    </header>

    <div class="st-container st-body">
      <!-- Authentication Required Message -->
      <div v-if="!customerAPI.isAuthenticated()" class="st-empty">
        <svg class="st-empty__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
        </svg>
        <h3 class="st-empty__title">Cần đăng nhập</h3>
        <p class="st-empty__hint">Bạn cần đăng nhập để xem thông tin tài khoản và quản lý địa chỉ.</p>
        <router-link to="/auth" class="st-btn st-btn--primary">Đăng nhập ngay</router-link>
      </div>

      <!-- Error Message -->
      <div v-if="error && customerAPI.isAuthenticated()" class="st-alert" role="alert">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
        <p>{{ error }}</p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="st-loading">
        <span class="st-spinner" aria-hidden="true"></span>
        <p>Đang tải thông tin...</p>
      </div>

      <!-- Tab Navigation -->
      <div v-else-if="customerAPI.isAuthenticated()" class="st-tabbar">
        <button
          @click="activeTab = 'personal'"
          :class="['st-tab', { 'is-active': activeTab === 'personal' }]"
        >
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
          </svg>
          Thông tin cá nhân
        </button>
        <button
          @click="activeTab = 'addresses'"
          :class="['st-tab', { 'is-active': activeTab === 'addresses' }]"
        >
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
          </svg>
          Sổ địa chỉ
        </button>
      </div>

      <!-- Personal Information Tab -->
      <div v-if="activeTab === 'personal' && customerAPI.isAuthenticated()" class="st-panel">
        <form @submit.prevent="updatePersonalInfo" class="st-form-stack">
          <!-- Avatar Section -->
          <div class="st-avatar-block">
            <div class="st-avatar-block__row">
              <div class="st-avatar-wrap">
                <div class="st-avatar-ring">
                  <img :src="currentAvatarUrl" alt="Avatar" class="st-avatar-img">
                </div>
                <button type="button" @click="showAvatarSelector = true" class="st-avatar-edit">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"></path>
                  </svg>
                </button>
              </div>
              <div class="st-avatar-block__info">
                <h3 class="st-block__title">Ảnh đại diện</h3>
                <p class="st-block__desc">Chọn một avatar phù hợp để cá nhân hóa tài khoản của bạn</p>
                <div class="st-tags">
                  <span class="st-tag">DiceBear</span>
                  <span class="st-tag st-tag--cobalt">SVG</span>
                  <span class="st-tag st-tag--done">Đa dạng</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Avatar Selector Modal -->
          <div v-if="showAvatarSelector" class="st-modal-overlay" @click.self="showAvatarSelector = false">
            <div class="st-modal st-modal--wide">
              <div class="st-modal__head">
                <div>
                  <h3 class="st-modal__title">Chọn Avatar</h3>
                  <p class="st-modal__sub">Chọn một avatar phù hợp với phong cách của bạn</p>
                </div>
                <button @click="showAvatarSelector = false" class="st-modal__close">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                  </svg>
                </button>
              </div>

              <!-- Avatar Styles -->
              <div class="st-modal__section">
                <h4 class="st-block__title">Chọn phong cách</h4>
                <div class="st-style-grid">
                  <button
                    v-for="style in avatarStyles"
                    :key="style.name"
                    @click="selectedStyle = style.name"
                    :class="['st-style-card', { 'is-active': selectedStyle === style.name }]"
                  >
                    <img :src="getAvatarUrl(style.name, 'preview')" :alt="style.label">
                    <p>{{ style.label }}</p>
                  </button>
                </div>
              </div>

              <!-- Avatar Grid -->
              <div class="st-modal__section">
                <h4 class="st-block__title">Chọn avatar</h4>
                <div class="st-avatar-grid">
                  <button
                    v-for="seed in avatarSeeds"
                    :key="seed"
                    @click="selectAvatar(selectedStyle, seed)"
                    :class="['st-avatar-option', { 'is-active': personalInfo.avatar === getAvatarUrl(selectedStyle, seed) }]"
                  >
                    <img :src="getAvatarUrl(selectedStyle, seed)" :alt="seed">
                  </button>
                </div>
              </div>

              <div class="st-modal__actions">
                <button @click="showAvatarSelector = false" class="st-btn">Hủy bỏ</button>
                <button @click="confirmAvatarSelection" class="st-btn st-btn--primary">Xác nhận</button>
              </div>
            </div>
          </div>

          <!-- Personal Info Form -->
          <div class="st-form-stack">
            <div class="st-block">
              <h4 class="st-block__title">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
                Thông tin cơ bản
              </h4>
              <div class="st-form__grid">
                <div class="st-form__col">
                  <div class="st-form__field">
                    <label class="st-label">Họ và tên *</label>
                    <input v-model="personalInfo.fullName" type="text" required class="st-input" placeholder="Nhập họ và tên đầy đủ">
                  </div>
                  <div class="st-form__field">
                    <label class="st-label">Email *</label>
                    <input v-model="personalInfo.email" type="email" required class="st-input" placeholder="example@email.com">
                  </div>
                  <div class="st-form__field">
                    <label class="st-label">Số điện thoại *</label>
                    <input v-model="personalInfo.phoneNumber" type="tel" required pattern="[0-9]{10,11}" class="st-input" placeholder="0901234567">
                  </div>
                </div>

                <div class="st-form__col">
                  <div class="st-form__field">
                    <label class="st-label">Ngày sinh</label>
                    <input v-model="personalInfo.birthDate" type="date" class="st-input">
                  </div>
                  <div class="st-form__field">
                    <label class="st-label">Giới tính</label>
                    <select v-model="personalInfo.gender" class="st-select">
                      <option value="">Chọn giới tính</option>
                      <option value="male">Nam</option>
                      <option value="female">Nữ</option>
                      <option value="other">Khác</option>
                    </select>
                  </div>
                  <div class="st-form__field">
                    <label class="st-label">Số CCCD/CMND</label>
                    <input v-model="personalInfo.cccd" type="text" maxlength="12" pattern="[0-9]*" @input="validateCCCD" class="st-input" placeholder="Nhập số CCCD/CMND">
                    <p class="st-form__hint">Nhập 9-12 số của CCCD hoặc CMND</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Trạng thái tài khoản -->
            <div class="st-block st-block--status">
              <h4 class="st-block__title">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                Trạng thái tài khoản
              </h4>
              <div class="st-status-row">
                <div class="st-status-row__left">
                  <div class="st-status-icon">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                    </svg>
                  </div>
                  <div>
                    <h5 class="st-status-row__title">Tài khoản đã xác thực</h5>
                    <p class="st-status-row__desc">Tài khoản của bạn đã được xác minh và hoạt động bình thường</p>
                  </div>
                </div>
                <span class="st-badge st-badge--done">Hoạt động</span>
              </div>
            </div>
          </div>

          <!-- Submit Button -->
          <div class="st-form__actions">
            <button type="button" class="st-btn">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
              </svg>
              Khôi phục
            </button>
            <button type="submit" :disabled="loading" class="st-btn st-btn--primary">
              <span v-if="loading">Đang cập nhật...</span>
              <span v-else>Cập nhật thông tin</span>
            </button>
          </div>
        </form>
      </div>

      <!-- Address Book Tab -->
      <div v-if="activeTab === 'addresses' && customerAPI.isAuthenticated()" class="st-panel">
        <div class="st-panel__head">
          <div>
            <h3 class="st-panel__title">Sổ địa chỉ</h3>
            <p class="st-panel__sub">Quản lý địa chỉ giao hàng của bạn</p>
          </div>
          <button @click="showAddAddressModal = true" class="st-btn st-btn--primary">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
            </svg>
            Thêm địa chỉ mới
          </button>
        </div>

        <!-- Address Carousel -->
        <div v-if="addresses.length > 0" class="st-carousel">
          <div class="st-carousel__viewport">
            <div class="st-carousel__track" :style="{ transform: `translateX(-${currentAddressIndex * 100}%)` }">
              <div v-for="(address, index) in addresses" :key="address.id" class="st-carousel__slide">
                <div class="st-address-card">
                  <div class="st-address-card__head">
                    <div class="st-address-icon">
                      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
                      </svg>
                    </div>
                    <div>
                      <h4 class="st-address-card__name">{{ address.name }}</h4>
                      <div class="st-address-card__meta">
                        <span v-if="address.isDefault" class="st-badge st-badge--done">Mặc định</span>
                        <span class="st-address-card__count">Địa chỉ {{ index + 1 }} / {{ addresses.length }}</span>
                      </div>
                    </div>
                  </div>

                  <div class="st-address-card__body">
                    <p class="st-address-card__line">
                      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path>
                      </svg>
                      {{ address.phoneNumber }}
                    </p>
                    <p class="st-address-card__line">
                      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
                      </svg>
                      <span>{{ address.fullAddress }}</span>
                    </p>
                  </div>

                  <div class="st-address-card__actions">
                    <button @click="editAddress(address)" class="st-chip st-chip--cobalt">Sửa</button>
                    <button v-if="!address.isDefault" @click="setDefaultAddress(address.id)" class="st-chip st-chip--done">Đặt mặc định</button>
                    <button v-if="!address.isDefault" @click="deleteAddress(address.id)" class="st-chip st-chip--danger">Xóa</button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Carousel Navigation -->
          <div class="st-carousel__nav">
            <button @click="previousAddress" :disabled="currentAddressIndex === 0" class="st-nav-btn">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
              </svg>
            </button>

            <div class="st-dots">
              <button
                v-for="(address, index) in addresses"
                :key="index"
                @click="currentAddressIndex = index"
                :class="['st-dot', { 'is-active': index === currentAddressIndex }]"
              ></button>
            </div>

            <button @click="nextAddress" :disabled="currentAddressIndex === addresses.length - 1" class="st-nav-btn">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
              </svg>
            </button>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else class="st-empty">
          <svg class="st-empty__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
          </svg>
          <h3 class="st-empty__title">Chưa có địa chỉ nào</h3>
          <p class="st-empty__hint">Thêm địa chỉ để việc đặt hàng trở nên thuận tiện hơn</p>
          <button @click="showAddAddressModal = true" class="st-btn st-btn--primary">Thêm địa chỉ đầu tiên</button>
        </div>
      </div>

      <!-- Add/Edit Address Modal -->
      <div v-if="showAddAddressModal || editingAddress" class="st-modal-overlay" @click.self="closeAddressModal">
        <div class="st-modal">
          <div class="st-modal__head">
            <div>
              <h3 class="st-modal__title">{{ editingAddress ? 'Sửa địa chỉ' : 'Thêm địa chỉ mới' }}</h3>
              <p class="st-modal__sub">Vui lòng điền đầy đủ thông tin để giao hàng chính xác</p>
            </div>
            <button @click="closeAddressModal" class="st-modal__close">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>

          <form @submit.prevent="saveAddress" class="st-form-stack">
            <div class="st-block">
              <h4 class="st-block__title">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
                Thông tin người nhận
              </h4>
              <div class="st-form__grid">
                <div class="st-form__field">
                  <label class="st-label">Họ và tên *</label>
                  <input v-model="addressForm.tenNguoiNhan" type="text" required class="st-input" placeholder="Nhập họ và tên người nhận">
                </div>
                <div class="st-form__field">
                  <label class="st-label">Số điện thoại *</label>
                  <input v-model="addressForm.soDienThoai" type="tel" required pattern="[0-9]{10,11}" class="st-input" placeholder="Nhập số điện thoại (10-11 số)">
                </div>
              </div>
            </div>

            <div class="st-block">
              <h4 class="st-block__title">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
                </svg>
                Địa chỉ giao hàng
              </h4>
              <div class="st-form__grid st-form__grid--3">
                <div class="st-form__field">
                  <label class="st-label">Tỉnh/Thành phố *</label>
                  <input v-model="addressForm.thanhPho" type="text" required class="st-input" placeholder="VD: Hồ Chí Minh">
                </div>
                <div class="st-form__field">
                  <label class="st-label">Quận/Huyện *</label>
                  <input v-model="addressForm.quan" type="text" required class="st-input" placeholder="VD: Quận 1">
                </div>
                <div class="st-form__field">
                  <label class="st-label">Phường/Xã *</label>
                  <input v-model="addressForm.phuong" type="text" required class="st-input" placeholder="VD: Phường Bến Nghé">
                </div>
              </div>
              <div class="st-form__field">
                <label class="st-label">Địa chỉ cụ thể *</label>
                <textarea v-model="addressForm.diaChiCuThe" required rows="3" class="st-input st-textarea" placeholder="Số nhà, tên đường, hẻm... (VD: 123 Nguyễn Huệ, Hẻm 45)"></textarea>
              </div>
            </div>

            <div class="st-block">
              <div class="st-checkbox-row">
                <div class="st-checkbox-row__left">
                  <input v-model="isDefaultChecked" type="checkbox" id="defaultAddress" class="st-checkbox">
                  <div>
                    <label for="defaultAddress" class="st-checkbox-row__label">Đặt làm địa chỉ mặc định</label>
                    <span class="st-checkbox-row__hint">
                      {{ isDefaultChecked ? 'Địa chỉ này sẽ được đặt làm mặc định' : 'Địa chỉ mặc định sẽ được chọn tự động khi đặt hàng' }}
                    </span>
                  </div>
                </div>
                <span v-if="isDefaultChecked" class="st-badge st-badge--done">Mặc định</span>
                <span v-else class="st-badge">Thường</span>
              </div>
            </div>

            <div class="st-modal__actions">
              <button type="button" @click="closeAddressModal" class="st-btn">Hủy bỏ</button>
              <button type="submit" :disabled="loading" class="st-btn st-btn--primary">
                <span v-if="loading">Đang lưu...</span>
                <span v-else>{{ editingAddress ? 'Cập nhật địa chỉ' : 'Thêm địa chỉ' }}</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>

  <!-- Toast Notification -->
  <NotificationToast
    v-if="showToast"
    :message="toastMessage"
    :type="toastType"
    :title="toastType === 'success' ? 'Thành công' : 'Thông báo'"
    @close="closeToast"
  />
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { customerAPI } from '@/api/customerAPI'
import NotificationToast from '@/components/NotificationToast.vue'
import { useAvatar } from '@/composables/useAvatar.js'

const activeTab = ref('personal')
const showAddAddressModal = ref(false)
const editingAddress = ref(null)
const currentAddressIndex = ref(0)
const loading = ref(false)
const error = ref('')

// Toast notification
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

// Show notification function
const showNotification = (message, type = 'success') => {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true

  // Auto hide after 3 seconds
  setTimeout(() => {
    showToast.value = false
  }, 3000)
}

// Avatar Selection using composable
const {
  getAvatarUrl,
  getCurrentAvatarUrl,
  saveAvatar,
  avatarUrl,
  avatarStyles,
  avatarSeeds
} = useAvatar()

const showAvatarSelector = ref(false)
const selectedStyle = ref('avataaars')
const selectedAvatar = ref('')

// Personal Information
const personalInfo = reactive({
  fullName: '',
  email: '',
  phoneNumber: '',
  birthDate: '',
  gender: '',
  avatar: '',
  cccd: '',
  customerCode: ''
})

// Addresses
const addresses = ref([])

// Address Form
const addressForm = reactive({
  tenNguoiNhan: '',
  soDienThoai: '',
  thanhPho: '',
  quan: '',
  phuong: '',
  diaChiCuThe: '',
  macDinh: false
})

// Debug computed property for checkbox
const isDefaultChecked = computed({
  get() {
    return addressForm.macDinh
  },
  set(value) {
    addressForm.macDinh = value
    console.log('Checkbox changed:', value)
  }
})

// Computed property for current avatar URL
const currentAvatarUrl = computed(() => {
  return personalInfo.avatar || avatarUrl.value || getAvatarUrl('avataaars', 'default')
})

const selectAvatar = (style, seed) => {
  selectedAvatar.value = getAvatarUrl(style, seed)
}

const confirmAvatarSelection = () => {
  if (selectedAvatar.value) {
    personalInfo.avatar = selectedAvatar.value
    // Use composable to save avatar
    saveAvatar(selectedAvatar.value)

    // Show success toast
    toastMessage.value = 'Avatar đã được cập nhật thành công!'
    toastType.value = 'success'
    showToast.value = true
  }
  showAvatarSelector.value = false
}

// Toast close handler
const closeToast = () => {
  showToast.value = false
}

// Load customer profile data
const loadCustomerProfile = async () => {
  try {
    loading.value = true
    const response = await customerAPI.getProfile()

    if (response.success) {
      const profile = response.data
      personalInfo.fullName = profile.ten || ''
      personalInfo.email = profile.email || ''
      personalInfo.phoneNumber = profile.soDienThoai || ''
      personalInfo.birthDate = profile.ngaySinh || ''
      personalInfo.gender = profile.gioiTinh === 1 ? 'male' : profile.gioiTinh === 0 ? 'female' : ''
      personalInfo.cccd = profile.cccd || ''
      personalInfo.customerCode = profile.ma || ''

      // Load avatar from composable
      personalInfo.avatar = getCurrentAvatarUrl()
    } else {
      error.value = response.message || 'Không thể tải thông tin khách hàng'
    }
  } catch (err) {
    error.value = 'Không thể tải thông tin khách hàng: ' + err.message
    console.error('Error loading customer profile:', err)
  } finally {
    loading.value = false
  }
}

// Load customer addresses
const loadCustomerAddresses = async () => {
  try {
    const response = await customerAPI.getAddresses()

    if (response.success) {
      addresses.value = response.data.map(addr => ({
        id: addr.id,
        name: addr.tenKhachHang,
        phoneNumber: addr.soDienThoai,
        fullAddress: `${addr.diaChiCuThe}, ${addr.phuong}, ${addr.quan}, ${addr.thanhPho}`,
        isDefault: addr.macDinh,
        thanhPho: addr.thanhPho,
        quan: addr.quan,
        phuong: addr.phuong,
        diaChiCuThe: addr.diaChiCuThe
      }))
    } else {
      error.value = response.message || 'Không thể tải danh sách địa chỉ'
    }
  } catch (err) {
    error.value = 'Không thể tải danh sách địa chỉ: ' + err.message
    console.error('Error loading addresses:', err)
  }
}


// Initialize data
onMounted(async () => {
  try {
    error.value = '' // Clear any previous errors

    // Check if user is authenticated
    if (!customerAPI.isAuthenticated()) {
      error.value = 'Bạn cần đăng nhập để xem thông tin tài khoản'
      return
    }

    // Load customer data
    await Promise.all([
      loadCustomerProfile(),
      loadCustomerAddresses()
    ])
  } catch (err) {
    console.error('Error initializing customer data:', err)
    error.value = 'Có lỗi xảy ra khi tải dữ liệu: ' + err.message
  }
})

// Carousel Navigation Functions
const nextAddress = () => {
  if (currentAddressIndex.value < addresses.value.length - 1) {
    currentAddressIndex.value++
  }
}

const previousAddress = () => {
  if (currentAddressIndex.value > 0) {
    currentAddressIndex.value--
  }
}

// Validate CCCD input - only allow numbers
const validateCCCD = (event) => {
  const value = event.target.value
  // Remove any non-numeric characters
  const numericValue = value.replace(/[^0-9]/g, '')
  // Update the model with cleaned value
  personalInfo.cccd = numericValue
}

const updatePersonalInfo = async () => {
  try {
    loading.value = true

    // Validate CCCD length if provided
    if (personalInfo.cccd && (personalInfo.cccd.length < 9 || personalInfo.cccd.length > 12)) {
      showNotification('Số CCCD/CMND phải có từ 9-12 chữ số', 'error')
      return
    }

    // TODO: Implement profile update API call
    console.log('Updating personal info:', personalInfo)

    // Show success notification
    showNotification('Thông tin cá nhân đã được cập nhật thành công!', 'success')
  } catch (err) {
    showNotification('Không thể cập nhật thông tin: ' + err.message, 'error')
    console.error('Error updating profile:', err)
  } finally {
    loading.value = false
  }
}

const editAddress = (address) => {
  editingAddress.value = address
  addressForm.tenNguoiNhan = address.name
  addressForm.soDienThoai = address.phoneNumber
  addressForm.thanhPho = address.thanhPho
  addressForm.quan = address.quan
  addressForm.phuong = address.phuong
  addressForm.diaChiCuThe = address.diaChiCuThe
  addressForm.macDinh = address.isDefault
}

const closeAddressModal = () => {
  showAddAddressModal.value = false
  editingAddress.value = null
  // Reset form completely
  Object.assign(addressForm, {
    tenNguoiNhan: '',
    soDienThoai: '',
    thanhPho: '',
    quan: '',
    phuong: '',
    diaChiCuThe: '',
    macDinh: false
  })
}

// Toggle default address checkbox
const saveAddress = async () => {
  try {
    loading.value = true

    // Debug log form data before saving
    console.log('Saving address form:', addressForm)

    if (editingAddress.value) {
      // Update existing address
      const response = await customerAPI.updateAddress(editingAddress.value.id, addressForm)
      if (response.success) {
        await loadCustomerAddresses()
        closeAddressModal()
        showNotification('Địa chỉ đã được cập nhật thành công!', 'success')
      } else {
        showNotification('Không thể cập nhật địa chỉ. Vui lòng thử lại!', 'error')
      }
    } else {
      // Add new address
      const response = await customerAPI.addAddress(addressForm)
      if (response.success) {
        await loadCustomerAddresses()
        closeAddressModal()
        showNotification('Địa chỉ đã được thêm thành công!', 'success')
      } else {
        showNotification('Không thể thêm địa chỉ. Vui lòng thử lại!', 'error')
      }
    }
  } catch (err) {
    error.value = 'Không thể lưu địa chỉ: ' + err.message
    showNotification('Có lỗi xảy ra khi lưu địa chỉ: ' + err.message, 'error')
    console.error('Error saving address:', err)
  } finally {
    loading.value = false
  }
}

const setDefaultAddress = async (addressId) => {
  try {
    const response = await customerAPI.setDefaultAddress(addressId)
    if (response.success) {
      await loadCustomerAddresses()
      showNotification('Đã đặt làm địa chỉ mặc định!', 'success')
    } else {
      showNotification('Không thể đặt địa chỉ mặc định. Vui lòng thử lại!', 'error')
    }
  } catch (err) {
    error.value = 'Không thể đặt địa chỉ mặc định: ' + err.message
    showNotification('Có lỗi xảy ra khi đặt địa chỉ mặc định: ' + err.message, 'error')
    console.error('Error setting default address:', err)
  }
}

const deleteAddress = async (addressId) => {
  if (confirm('Bạn có chắc chắn muốn xóa địa chỉ này?')) {
    try {
      const response = await customerAPI.deleteAddress(addressId)
      if (response.success) {
        await loadCustomerAddresses()
        showNotification('Địa chỉ đã được xóa thành công!', 'success')

        // Adjust carousel index if needed
        if (currentAddressIndex.value >= addresses.value.length && addresses.value.length > 0) {
          currentAddressIndex.value = addresses.value.length - 1
        }
      } else {
        showNotification('Không thể xóa địa chỉ. Vui lòng thử lại!', 'error')
      }
    } catch (err) {
      error.value = 'Không thể xóa địa chỉ: ' + err.message
      showNotification('Có lỗi xảy ra khi xóa địa chỉ: ' + err.message, 'error')
      console.error('Error deleting address:', err)
    }
  }
}
</script>

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
  background: var(--mist);
}

.st-container {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 1.25rem;
}

/* ===== Header ===== */
.st-head {
  position: relative;
  overflow: hidden;
  background: var(--ink);
  background-image: repeating-linear-gradient(115deg, rgba(255, 255, 255, 0.022) 0 2px, transparent 2px 74px);
  padding: 3rem 0 2.5rem;
}

.st-head__jersey {
  position: absolute;
  top: 50%;
  right: 3rem;
  transform: translateY(-50%);
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: clamp(7rem, 16vw, 13rem);
  line-height: 0.8;
  color: transparent;
  -webkit-text-stroke: 2px rgba(255, 255, 255, 0.08);
  pointer-events: none;
  user-select: none;
}

.st-head__inner { position: relative; z-index: 1; }

.st-eyebrow {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.8rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.85rem;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: var(--strike);
}

.st-eyebrow__bar { width: 2.5rem; height: 3px; background: var(--strike); }

.st-display {
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 800;
  font-size: clamp(2.25rem, 5vw, 3.5rem);
  line-height: 0.95;
  text-transform: uppercase;
  color: var(--chalk);
  margin: 0 0 0.6rem;
}

.st-lead { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 1rem; color: var(--steel); margin: 0; max-width: 34rem; }

/* ===== Body ===== */
.st-body { padding: 2.5rem 1.25rem 4rem; display: flex; flex-direction: column; gap: 1.5rem; }

/* Alerts */
.st-alert {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: #fff1ee;
  border-left: 4px solid var(--strike);
  padding: 0.9rem 1.1rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.92rem;
  color: #9a2410;
}
.st-alert svg { width: 1.25rem; height: 1.25rem; flex-shrink: 0; }
.st-alert p { margin: 0; }

.st-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 3.5rem 1rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  color: var(--slate);
}
.st-spinner {
  width: 2.5rem; height: 2.5rem;
  border: 3px solid var(--line);
  border-top-color: var(--strike);
  border-radius: 50%;
  animation: st-spin 0.8s linear infinite;
}
@keyframes st-spin { to { transform: rotate(360deg); } }

/* Tabs */
.st-tabbar { display: flex; background: var(--chalk); border: 1px solid var(--line); }
.st-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.6rem;
  padding: 1.1rem 1.5rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.95rem;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--slate);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}
.st-tab svg { width: 1.3rem; height: 1.3rem; }
.st-tab:hover { color: var(--ink); background: var(--mist); }
.st-tab.is-active { background: var(--ink); color: var(--chalk); }

/* Panel */
.st-panel { background: var(--chalk); border: 1px solid var(--line); padding: 2rem 1.75rem; }
.st-panel__head { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 1rem; margin-bottom: 1.75rem; }
.st-panel__title { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.5rem; text-transform: uppercase; color: var(--ink); margin: 0 0 0.2rem; }
.st-panel__sub { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; color: var(--slate); margin: 0; font-size: 0.9rem; }

.st-form-stack { display: flex; flex-direction: column; gap: 1.75rem; }

/* Avatar block */
.st-avatar-block { background: var(--mist); padding: 1.75rem; }
.st-avatar-block__row { display: flex; flex-wrap: wrap; align-items: center; gap: 1.75rem; }
.st-avatar-wrap { position: relative; flex-shrink: 0; }
.st-avatar-ring { width: 7rem; height: 7rem; border-radius: 50%; padding: 3px; background: var(--ink); }
.st-avatar-img { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; background: var(--chalk); }
.st-avatar-edit {
  position: absolute;
  bottom: -0.2rem; right: -0.2rem;
  width: 2.4rem; height: 2.4rem;
  display: grid; place-items: center;
  background: var(--strike);
  color: var(--chalk);
  border: 3px solid var(--chalk);
  border-radius: 50%;
  cursor: pointer;
}
.st-avatar-edit svg { width: 1.1rem; height: 1.1rem; }
.st-avatar-block__info { flex: 1; min-width: 15rem; }
.st-block__title {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 1.15rem;
  letter-spacing: 0.02em;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 0.6rem;
}
.st-block__title svg { width: 1.3rem; height: 1.3rem; color: var(--strike); flex-shrink: 0; }
.st-block__desc { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; color: var(--slate); margin: 0 0 0.9rem; }
.st-tags { display: flex; flex-wrap: wrap; gap: 0.5rem; }
.st-tag { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.8rem; font-weight: 600; padding: 0.35rem 0.85rem; background: var(--chalk); border: 1px solid var(--line); color: var(--slate); border-radius: 999px; }
.st-tag--cobalt { color: var(--cobalt); border-color: var(--cobalt); }
.st-tag--done { color: var(--done); border-color: var(--done); }

/* Block (generic section) */
.st-block { background: var(--mist); padding: 1.75rem; }
.st-block--status { background: #eafbf1; }

.st-form__grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(15rem, 1fr)); gap: 1.5rem; margin-bottom: 1.25rem; }
.st-form__grid--3 { grid-template-columns: repeat(auto-fit, minmax(11rem, 1fr)); }
.st-form__grid:last-child { margin-bottom: 0; }
.st-form__col { display: flex; flex-direction: column; gap: 1.25rem; }
.st-form__field { display: flex; flex-direction: column; gap: 0.5rem; }

.st-label { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.8rem; letter-spacing: 0.06em; text-transform: uppercase; color: var(--slate); }

.st-input, .st-select {
  width: 100%;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-size: 0.92rem;
  color: var(--ink);
  background: var(--chalk);
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.75rem 0.9rem;
}
.st-textarea { resize: vertical; }
.st-input:focus, .st-select:focus { outline: none; border-color: var(--strike); }
.st-form__hint { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.78rem; color: var(--steel); margin: 0.35rem 0 0; }
.st-form__actions { display: flex; justify-content: flex-end; gap: 0.75rem; }

/* Status row */
.st-status-row { display: flex; align-items: center; justify-content: space-between; gap: 1rem; background: var(--chalk); border: 1px solid rgba(15,122,61,0.25); padding: 1.25rem 1.5rem; }
.st-status-row__left { display: flex; align-items: center; gap: 1rem; }
.st-status-icon { width: 2.75rem; height: 2.75rem; display: grid; place-items: center; background: var(--done); color: var(--chalk); border-radius: 50%; }
.st-status-icon svg { width: 1.3rem; height: 1.3rem; }
.st-status-row__title { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; color: var(--ink); margin: 0 0 0.15rem; }
.st-status-row__desc { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.85rem; color: var(--slate); margin: 0; }

.st-badge {
  display: inline-flex;
  align-items: center;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.75rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  padding: 0.35rem 0.85rem;
  background: var(--mist);
  color: var(--slate);
  border-radius: 999px;
}
.st-badge--done { background: #eafbf1; color: var(--done); }

/* Buttons */
.st-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-family: 'Nunito', 'Be Vietnam Pro', sans-serif;
  font-weight: 700;
  font-size: 0.9rem;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  border: 1px solid var(--line);
  border-radius: 2px;
  padding: 0.75rem 1.5rem;
  cursor: pointer;
  text-decoration: none;
  background: var(--chalk);
  color: var(--ink);
  transition: all 0.2s ease;
}
.st-btn svg { width: 1.1rem; height: 1.1rem; }
.st-btn:hover { border-color: var(--ink); }
.st-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.st-btn--primary { background: var(--ink); border-color: var(--ink); color: var(--chalk); }
.st-btn--primary:hover { background: var(--strike); border-color: var(--strike); }

/* Empty state */
.st-empty { text-align: center; background: var(--chalk); border: 1px dashed var(--line); padding: 4rem 1.5rem; }
.st-empty__icon { width: 3.5rem; height: 3.5rem; margin: 0 auto 1.25rem; color: var(--steel); }
.st-empty__title { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.4rem; text-transform: uppercase; color: var(--ink); margin: 0 0 0.5rem; }
.st-empty__hint { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; color: var(--slate); margin: 0 0 1.5rem; }

/* Modal */
.st-modal-overlay {
  position: fixed; inset: 0;
  background: rgba(16, 20, 26, 0.65);
  display: flex; align-items: center; justify-content: center;
  z-index: 50; padding: 1rem;
}
.st-modal { background: var(--chalk); padding: 2rem; width: 100%; max-width: 34rem; max-height: 90vh; overflow-y: auto; }
.st-modal--wide { max-width: 52rem; }
.st-modal__head { display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; margin-bottom: 1.75rem; }
.st-modal__title { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 800; font-size: 1.6rem; text-transform: uppercase; color: var(--ink); margin: 0 0 0.3rem; }
.st-modal__sub { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; color: var(--slate); margin: 0; font-size: 0.9rem; }
.st-modal__close { background: transparent; border: none; color: var(--steel); cursor: pointer; padding: 0.5rem; }
.st-modal__close svg { width: 1.4rem; height: 1.4rem; }
.st-modal__section { margin-bottom: 1.75rem; }
.st-modal__actions { display: flex; justify-content: flex-end; gap: 0.75rem; padding-top: 1.25rem; border-top: 1px solid var(--line); }

/* Avatar style / grid pickers */
.st-style-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(6rem, 1fr)); gap: 0.9rem; }
.st-style-card { background: var(--chalk); border: 1px solid var(--line); padding: 0.9rem; text-align: center; cursor: pointer; transition: border-color 0.2s ease; }
.st-style-card img { width: 3.5rem; height: 3.5rem; border-radius: 50%; margin: 0 auto 0.5rem; }
.st-style-card p { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.82rem; font-weight: 600; color: var(--slate); margin: 0; }
.st-style-card.is-active { border-color: var(--strike); background: #fff1ee; }

.st-avatar-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(4rem, 1fr)); gap: 0.75rem; }
.st-avatar-option { padding: 0.4rem; border: 1px solid var(--line); background: var(--chalk); cursor: pointer; }
.st-avatar-option img { width: 100%; aspect-ratio: 1; border-radius: 4px; }
.st-avatar-option.is-active { border-color: var(--strike); }

/* Address carousel */
.st-carousel__viewport { overflow: hidden; }
.st-carousel__track { display: flex; transition: transform 0.4s ease; }
.st-carousel__slide { width: 100%; flex-shrink: 0; padding: 0 0.25rem; }

.st-address-card { background: var(--mist); border: 1px solid var(--line); padding: 1.5rem; }
.st-address-card__head { display: flex; align-items: flex-start; gap: 0.9rem; margin-bottom: 1.1rem; }
.st-address-icon { width: 2.75rem; height: 2.75rem; flex-shrink: 0; display: grid; place-items: center; background: var(--ink); color: var(--chalk); border-radius: 50%; }
.st-address-icon svg { width: 1.25rem; height: 1.25rem; }
.st-address-card__name { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 1.1rem; color: var(--ink); margin: 0 0 0.3rem; }
.st-address-card__meta { display: flex; align-items: center; gap: 0.6rem; flex-wrap: wrap; }
.st-address-card__count { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.8rem; color: var(--steel); }
.st-address-card__body { display: flex; flex-direction: column; gap: 0.6rem; margin-bottom: 1.25rem; }
.st-address-card__line { display: flex; align-items: flex-start; gap: 0.6rem; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.9rem; color: var(--slate); margin: 0; }
.st-address-card__line svg { width: 1.15rem; height: 1.15rem; color: var(--steel); flex-shrink: 0; margin-top: 0.1rem; }
.st-address-card__actions { display: flex; flex-wrap: wrap; gap: 0.6rem; }

.st-chip { font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 700; font-size: 0.82rem; letter-spacing: 0.04em; text-transform: uppercase; padding: 0.5rem 1rem; border: none; border-radius: 2px; cursor: pointer; background: var(--chalk); color: var(--ink); }
.st-chip--cobalt { color: var(--cobalt); background: #eef0ff; }
.st-chip--done { color: var(--done); background: #eafbf1; }
.st-chip--danger { color: var(--strike); background: #fff1ee; }

.st-carousel__nav { display: flex; align-items: center; justify-content: space-between; margin-top: 1.5rem; }
.st-nav-btn { width: 2.75rem; height: 2.75rem; display: grid; place-items: center; background: var(--chalk); border: 1px solid var(--line); border-radius: 50%; cursor: pointer; color: var(--slate); }
.st-nav-btn svg { width: 1.15rem; height: 1.15rem; }
.st-nav-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.st-dots { display: flex; gap: 0.5rem; }
.st-dot { width: 0.6rem; height: 0.6rem; border-radius: 50%; background: var(--line); border: none; cursor: pointer; padding: 0; }
.st-dot.is-active { background: var(--strike); }

/* Checkbox row */
.st-checkbox-row { display: flex; align-items: center; justify-content: space-between; gap: 1rem; flex-wrap: wrap; }
.st-checkbox-row__left { display: flex; align-items: flex-start; gap: 0.9rem; }
.st-checkbox { width: 1.2rem; height: 1.2rem; accent-color: var(--strike); margin-top: 0.15rem; }
.st-checkbox-row__label { display: block; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-weight: 600; color: var(--ink); cursor: pointer; }
.st-checkbox-row__hint { display: block; font-family: 'Nunito', 'Be Vietnam Pro', sans-serif; font-size: 0.78rem; color: var(--steel); margin-top: 0.15rem; }

/* Focus */
.st-btn:focus-visible, .st-input:focus-visible, .st-select:focus-visible, .st-tab:focus-visible, .st-chip:focus-visible, .st-nav-btn:focus-visible {
  outline: 3px solid var(--cobalt);
  outline-offset: 2px;
}

/* Responsive */
@media (max-width: 640px) {
  .st-head__jersey { opacity: 0.5; }
  .st-avatar-block__row { justify-content: center; text-align: center; }
  .st-status-row { flex-direction: column; align-items: flex-start; }
}

@media (prefers-reduced-motion: reduce) {
  .st-btn, .st-tab, .st-style-card { transition: none; }
  .st-spinner { animation: none; }
  .st-carousel__track { transition: none; }
}
</style>
