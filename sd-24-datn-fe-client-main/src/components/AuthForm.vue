<template>
  <div class="auth-container">
    <!-- Background with overlay -->
    <!-- <div class="background-overlay"></div> -->
    
    <div class="auth-wrapper" :class="{ 'register-mode': isRegisterMode }">
      <!-- Brand Section -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo-container">
            <img src="@/assets/logo-trang.png" alt="STRIKER" class="logo-img" />
          </div>
          <p class="brand-tagline">Hệ thống mua sắm áo thể thao chính hãng</p>
          <div class="brand-features">
            <div class="feature-item">
              <iconify-icon icon="solar:check-circle-bold"></iconify-icon>
              <span>Sản phẩm chính hãng 100%</span>
            </div>
            <div class="feature-item">
              <iconify-icon icon="solar:check-circle-bold"></iconify-icon>
              <span>Giao hàng nhanh chóng</span>
            </div>
            <div class="feature-item">
              <iconify-icon icon="solar:check-circle-bold"></iconify-icon>
              <span>Hỗ trợ 24/7</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Form Section -->
      <div class="form-section">
        <!-- Toggle Switch -->
        <div class="form-toggle-switch">
          <div class="toggle-container">
            <button 
              class="toggle-btn" 
              :class="{ 'active': !isRegisterMode }"
              @click="showLogin"
            >
              Đăng nhập
            </button>
            <button 
              class="toggle-btn" 
              :class="{ 'active': isRegisterMode }"
              @click="showRegister"
            >
              Đăng ký
            </button>
            <div class="toggle-slider" :class="{ 'register': isRegisterMode }"></div>
          </div>
        </div>

        <!-- Login Form -->
        <div class="form-container login-form" :class="{ 'hidden': isRegisterMode || forgotPasswordMode }">
          <div class="form-header">
            <h2 class="form-title">Chào mừng trở lại!</h2>
            <p class="form-subtitle">Đăng nhập vào tài khoản STRIKER của bạn</p>
          </div>

          <form @submit.prevent="handleSignIn" class="auth-form">
            <!-- Username Field -->
            <div class="form-group">
              <label class="form-label">
                <iconify-icon icon="solar:user-bold-duotone"></iconify-icon>
                Tên đăng nhập
              </label>
              <input
                type="text"
                v-model="signInForm.username"
                class="form-input"
                :class="{ 'error': errors.loginUsername }"
                placeholder="Nhập tên đăng nhập của bạn"
                required
              />
              <span v-if="errors.loginUsername" class="error-message">{{ errors.loginUsername }}</span>
            </div>

            <!-- Password Field -->
            <div class="form-group">
              <label class="form-label">
                <iconify-icon icon="solar:lock-password-bold-duotone"></iconify-icon>
                Mật khẩu
              </label>
              <div class="password-input-wrapper">
                <input
                  :type="showLoginPassword ? 'text' : 'password'"
                  v-model="signInForm.password"
                  class="form-input password-input"
                  :class="{ 'error': errors.loginPassword }"
                  placeholder="Nhập mật khẩu của bạn"
                  required
                />
                <button
                  type="button"
                  class="password-toggle"
                  @click="toggleLoginPassword"
                >
                  <iconify-icon :icon="showLoginPassword ? 'solar:eye-closed-bold' : 'solar:eye-bold'"></iconify-icon>
                </button>
              </div>
              <span v-if="errors.loginPassword" class="error-message">{{ errors.loginPassword }}</span>
            </div>

            <!-- Remember Me & Forgot Password -->
            <div class="form-options">
              <label class="checkbox-wrapper" for="rememberMe">
                <input 
                  type="checkbox" 
                  v-model="signInForm.rememberMe" 
                  class="checkbox-input"
                  id="rememberMe"
                />
                <span class="checkbox-label">
                  <iconify-icon icon="solar:shield-check-bold-duotone" class="checkbox-icon"></iconify-icon>
                  Ghi nhớ đăng nhập
                </span>
              </label>
              <a href="#" class="forgot-password" @click.prevent="handleForgotPassword">
                Quên mật khẩu?
              </a>
            </div>

            <!-- Login Button -->
            <button
              type="submit"
              class="auth-btn"
              :disabled="isLoading"
              :class="{ 'loading': isLoading }"
            >
              {{ isLoading ? 'Đang đăng nhập...' : 'Đăng nhập' }}
            </button>

            <!-- Shop Now Button -->
            <button
              type="button"
              @click="goToHomePage"
              class="auth-btn-secondary"
              :disabled="isLoading"
            >
              <iconify-icon icon="solar:bag-smile-bold-duotone"></iconify-icon>
              Mua ngay không cần đăng nhập
            </button>

            <!-- Error Message -->
            <div v-if="loginError" class="auth-error">
              <iconify-icon icon="solar:danger-circle-bold-duotone"></iconify-icon>
              {{ loginError }}
            </div>
          </form>
        </div>

        <!-- Forgot Password Panel -->
        <div class="form-container forgot-form" :class="{ 'hidden': !forgotPasswordMode }">
          <div class="form-header">
            <h2 class="form-title">Quên mật khẩu?</h2>
            <p class="form-subtitle">
              {{ forgotStep === 'request' ? 'Nhập email đã đăng ký để nhận mã xác nhận' : 'Nhập mã xác nhận và mật khẩu mới' }}
            </p>
          </div>

          <form v-if="forgotStep === 'request'" @submit.prevent="handleRequestOtp" class="auth-form">
            <div class="form-group">
              <label class="form-label">
                <iconify-icon icon="solar:letter-bold-duotone"></iconify-icon>
                Email
              </label>
              <input
                type="email"
                v-model="forgotForm.email"
                class="form-input"
                :class="{ 'error': errors.forgotEmail }"
                placeholder="Nhập email đã đăng ký"
                required
              />
              <span v-if="errors.forgotEmail" class="error-message">{{ errors.forgotEmail }}</span>
            </div>

            <button type="submit" class="auth-btn" :disabled="isLoading" :class="{ 'loading': isLoading }">
              {{ isLoading ? 'Đang gửi...' : 'Gửi mã xác nhận' }}
            </button>

            <button type="button" class="auth-btn-secondary" @click="exitForgotPassword">
              Quay lại đăng nhập
            </button>

            <div v-if="forgotError" class="auth-error">
              <iconify-icon icon="solar:danger-circle-bold-duotone"></iconify-icon>
              {{ forgotError }}
            </div>
          </form>

          <form v-else @submit.prevent="handleResetPassword" class="auth-form">
            <div class="form-group">
              <label class="form-label">
                <iconify-icon icon="solar:shield-keyhole-bold-duotone"></iconify-icon>
                Mã xác nhận
              </label>
              <input
                type="text"
                v-model="forgotForm.otpCode"
                class="form-input"
                :class="{ 'error': errors.otpCode }"
                placeholder="Nhập mã 6 số đã gửi tới email"
                maxlength="6"
                required
              />
              <span class="field-hint">Đã gửi tới {{ forgotForm.email }}. Mã có hiệu lực trong 15 phút.</span>
              <span v-if="errors.otpCode" class="error-message">{{ errors.otpCode }}</span>
            </div>

            <div class="form-group">
              <label class="form-label">
                <iconify-icon icon="solar:lock-password-bold-duotone"></iconify-icon>
                Mật khẩu mới
              </label>
              <input
                :type="showRegPassword ? 'text' : 'password'"
                v-model="forgotForm.newPassword"
                class="form-input"
                :class="{ 'error': errors.newPassword }"
                placeholder="Nhập mật khẩu mới"
                required
              />
              <span class="field-hint">Ít nhất 6 ký tự, bắt đầu bằng chữ hoa và có ký tự đặc biệt (VD: Abc@123)</span>
              <span v-if="errors.newPassword" class="error-message">{{ errors.newPassword }}</span>
            </div>

            <button type="submit" class="auth-btn" :disabled="isLoading" :class="{ 'loading': isLoading }">
              {{ isLoading ? 'Đang xử lý...' : 'Đặt lại mật khẩu' }}
            </button>

            <button type="button" class="auth-btn-secondary" @click="forgotStep = 'request'">
              Nhập lại email
            </button>

            <div v-if="forgotError" class="auth-error">
              <iconify-icon icon="solar:danger-circle-bold-duotone"></iconify-icon>
              {{ forgotError }}
            </div>
          </form>
        </div>

        <!-- Register Form -->
        <div class="form-container register-form" :class="{ 'hidden': !isRegisterMode }">
          <div class="form-header">
            <h2 class="form-title">Tạo tài khoản mới</h2>
            <p class="form-subtitle">Đăng ký để trở thành thành viên STRIKER</p>
          </div>

          <form @submit.prevent="handleSignUp" class="auth-form">
            <!-- Name Field -->
            <div class="form-group">
              <label class="form-label">
                <iconify-icon icon="solar:user-bold-duotone"></iconify-icon>
                Họ và tên
              </label>
              <input
                type="text"
                v-model="signUpForm.name"
                class="form-input"
                :class="{ 'error': errors.name }"
                placeholder="Nhập họ và tên của bạn"
                required
              />
              <span v-if="errors.name" class="error-message">{{ errors.name }}</span>
            </div>

            <!-- Email Field -->
            <div class="form-group">
              <label class="form-label">
                <iconify-icon icon="solar:letter-bold-duotone"></iconify-icon>
                Email
              </label>
              <input
                type="email"
                v-model="signUpForm.email"
                class="form-input"
                :class="{ 'error': errors.email }"
                placeholder="Nhập email của bạn"
                required
              />
              <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
            </div>

            <!-- Phone Field -->
            <div class="form-group">
              <label class="form-label">
                <iconify-icon icon="solar:phone-bold-duotone"></iconify-icon>
                Số điện thoại
              </label>
              <input
                type="tel"
                v-model="signUpForm.phoneNumber"
                class="form-input"
                :class="{ 'error': errors.phoneNumber }"
                placeholder="Nhập số điện thoại"
                required
              />
              <span v-if="errors.phoneNumber" class="error-message">{{ errors.phoneNumber }}</span>
            </div>

            <!-- Password Field -->
            <div class="form-group">
              <label class="form-label">
                <iconify-icon icon="solar:lock-password-bold-duotone"></iconify-icon>
                Mật khẩu
              </label>
              <div class="password-input-wrapper">
                <input
                  :type="showRegPassword ? 'text' : 'password'"
                  v-model="signUpForm.password"
                  class="form-input password-input"
                  :class="{ 'error': errors.password }"
                  placeholder="Nhập mật khẩu"
                  required
                />
                <button
                  type="button"
                  class="password-toggle"
                  @click="toggleRegPassword"
                >
                  <iconify-icon :icon="showRegPassword ? 'solar:eye-closed-bold' : 'solar:eye-bold'"></iconify-icon>
                </button>
              </div>
              <span class="field-hint">Ít nhất 6 ký tự, bắt đầu bằng chữ hoa và có ký tự đặc biệt (VD: Abc@123)</span>
              <span v-if="errors.password" class="error-message">{{ errors.password }}</span>
            </div>

            <!-- Confirm Password Field -->
            <div class="form-group">
              <label class="form-label">
                <iconify-icon icon="solar:lock-password-bold-duotone"></iconify-icon>
                Xác nhận mật khẩu
              </label>
              <div class="password-input-wrapper">
                <input
                  :type="showConfirmPassword ? 'text' : 'password'"
                  v-model="signUpForm.confirmPassword"
                  class="form-input password-input"
                  :class="{ 'error': errors.confirmPassword }"
                  placeholder="Nhập lại mật khẩu"
                  required
                />
                <button
                  type="button"
                  class="password-toggle"
                  @click="toggleConfirmPassword"
                >
                  <iconify-icon :icon="showConfirmPassword ? 'solar:eye-closed-bold' : 'solar:eye-bold'"></iconify-icon>
                </button>
              </div>
              <span v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</span>
            </div>

            <!-- Register Button -->
            <button
              type="submit"
              class="auth-btn"
              :disabled="isLoading"
              :class="{ 'loading': isLoading }"
            >
              {{ isLoading ? 'Đang đăng ký...' : 'Đăng ký tài khoản' }}
            </button>

            <!-- Shop Now Button -->
            <button
              type="button"
              @click="goToHomePage"
              class="auth-btn-secondary"
              :disabled="isLoading"
            >
              <iconify-icon icon="solar:bag-smile-bold-duotone"></iconify-icon>
              Mua ngay không cần đăng ký
            </button>

            <!-- Error Message -->
            <div v-if="registerError" class="auth-error">
              <iconify-icon icon="solar:danger-circle-bold-duotone"></iconify-icon>
              {{ registerError }}
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import authService from '../services/authService.js';
import { useNotification } from '../composables/useNotification.js';
import { validateEmail, validateSoDienThoai } from '../utils/validators.js';

const router = useRouter();
const { success } = useNotification();

// State management
const isRegisterMode = ref(false);
const isLoading = ref(false);
const errors = ref({});
const loginError = ref('');
const registerError = ref('');

// Password visibility states
const showLoginPassword = ref(false);
const showRegPassword = ref(false);
const showConfirmPassword = ref(false);

// Form data for Sign Up
const signUpForm = ref({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
  phoneNumber: '',
});

// Form data for Sign In
const signInForm = ref({
  username: '',
  password: '',
  rememberMe: false
});

// Forgot password state
const forgotPasswordMode = ref(false);
const forgotStep = ref('request'); // 'request' | 'confirm'
const forgotError = ref('');
const forgotForm = ref({
  email: '',
  otpCode: '',
  newPassword: ''
});

// Toggle functions
const showLogin = () => {
  isRegisterMode.value = false;
  forgotPasswordMode.value = false;
  clearErrors();
};

const showRegister = () => {
  isRegisterMode.value = true;
  forgotPasswordMode.value = false;
  clearErrors();
};

// Password toggle functions
const toggleLoginPassword = () => {
  showLoginPassword.value = !showLoginPassword.value;
};

const toggleRegPassword = () => {
  showRegPassword.value = !showRegPassword.value;
};

const toggleConfirmPassword = () => {
  showConfirmPassword.value = !showConfirmPassword.value;
};

// Clear errors
const clearErrors = () => {
  errors.value = {};
  loginError.value = '';
  registerError.value = '';
};

// Validation functions
const validateSignUp = () => {
  errors.value = {};
  
  const trimmedName = signUpForm.value.name.trim().replace(/\s+/g, ' ');
  if (!trimmedName) {
    errors.value.name = 'Vui lòng nhập họ và tên';
  } else if (!/^[\p{L} ]{2,50}$/u.test(trimmedName)) {
    errors.value.name = 'Họ và tên chỉ được chứa chữ cái và khoảng trắng, từ 2 đến 50 ký tự';
  }

  // Cùng quy tắc với backend: SĐT đúng 10 số bắt đầu bằng 0 (trước đây nhận 10-11 số)
  const emailError = validateEmail(signUpForm.value.email);
  if (emailError) errors.value.email = emailError;

  const phoneError = validateSoDienThoai(signUpForm.value.phoneNumber.replace(/\s/g, ''));
  if (phoneError) errors.value.phoneNumber = phoneError;
  
  const pwd = signUpForm.value.password;
  if (!pwd.trim()) {
    errors.value.password = 'Vui lòng nhập mật khẩu';
  } else if (pwd.length < 6) {
    errors.value.password = 'Mật khẩu phải có ít nhất 6 ký tự';
  } else if (!/^[A-ZÀ-Ỹ]/.test(pwd)) {
    errors.value.password = 'Mật khẩu phải bắt đầu bằng một chữ cái viết hoa';
  } else if (!/[^a-zA-Z0-9]/.test(pwd)) {
    errors.value.password = 'Mật khẩu phải chứa ít nhất một ký tự đặc biệt (VD: !@#$%...)';
  }
  
  if (!signUpForm.value.confirmPassword.trim()) {
    errors.value.confirmPassword = 'Vui lòng xác nhận mật khẩu';
  } else if (signUpForm.value.password !== signUpForm.value.confirmPassword) {
    errors.value.confirmPassword = 'Mật khẩu xác nhận không khớp';
  }
  
  return Object.keys(errors.value).length === 0;
};

const validateSignIn = () => {
  errors.value = {};
  
  if (!signInForm.value.username.trim()) {
    errors.value.loginUsername = 'Vui lòng nhập tên đăng nhập';
  }
  
  if (!signInForm.value.password.trim()) {
    errors.value.loginPassword = 'Vui lòng nhập mật khẩu';
  }
  
  return Object.keys(errors.value).length === 0;
};

// Handle Sign Up
const handleSignUp = async () => {
  if (!validateSignUp()) {
    return;
  }

  isLoading.value = true;
  registerError.value = '';

  try {
    const result = await authService.register({
      name: signUpForm.value.name,
      email: signUpForm.value.email.trim(),
      password: signUpForm.value.password,
      phoneNumber: signUpForm.value.phoneNumber.replace(/\s/g, ''),
    });
    
    console.log('Sign Up Success:', result);
    success(
      result.tenDangNhap
        ? `Đăng ký thành công! Tên đăng nhập của bạn là "${result.tenDangNhap}".`
        : 'Đăng ký thành công! Bạn có thể đăng nhập ngay bây giờ.'
    );

    // Switch to login form after successful registration, prefill the generated username
    showLogin();
    if (result.tenDangNhap) {
      signInForm.value.username = result.tenDangNhap;
    }

    // Clear registration form
    signUpForm.value = {
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      phoneNumber: '',
    };
    
  } catch (error) {
    console.error('Sign Up Error:', error);
    registerError.value = error.message || 'Đăng ký thất bại! Vui lòng thử lại sau';
  } finally {
    isLoading.value = false;
  }
};

// Handle Sign In
const handleSignIn = async () => {
  if (!validateSignIn()) {
    return;
  }

  isLoading.value = true;
  loginError.value = '';

  try {
    const result = await authService.login({
      username: signInForm.value.username,
      password: signInForm.value.password,
      rememberMe: signInForm.value.rememberMe
    });
    
    console.log('Sign In Success:', result);
    
    success('Đăng nhập thành công!');
    
    // Redirect to home page after successful login
    setTimeout(() => {
      router.push(authService.getRedirectPath());
    }, 1500);
    
  } catch (error) {
    console.error('Sign In Error:', error);
    loginError.value = error.message || 'Đăng nhập thất bại! Vui lòng thử lại sau';
  } finally {
    isLoading.value = false;
  }
};

// Handle Forgot Password - opens the dedicated forgot-password panel
const handleForgotPassword = () => {
  forgotError.value = '';
  forgotStep.value = 'request';
  forgotForm.value = { email: '', otpCode: '', newPassword: '' };
  forgotPasswordMode.value = true;
};

const exitForgotPassword = () => {
  forgotPasswordMode.value = false;
  forgotError.value = '';
};

// Step 1: request an OTP code sent to the registered email
const handleRequestOtp = async () => {
  errors.value.forgotEmail = '';
  forgotError.value = '';

  if (!forgotForm.value.email.trim()) {
    errors.value.forgotEmail = 'Vui lòng nhập email';
    return;
  }

  isLoading.value = true;
  try {
    const result = await authService.forgotPassword(forgotForm.value.email.trim());
    success(result.message || 'Mã xác nhận đã được gửi tới email của bạn');
    // Chế độ dev (chưa cấu hình SMTP thật): backend trả kèm mã để test được toàn bộ luồng.
    if (result.debugOtpCode) {
      forgotForm.value.otpCode = result.debugOtpCode;
    }
    forgotStep.value = 'confirm';
  } catch (error) {
    forgotError.value = error.message || 'Không thể gửi mã xác nhận';
  } finally {
    isLoading.value = false;
  }
};

// Step 2: confirm the OTP code and set a new password
const handleResetPassword = async () => {
  errors.value.otpCode = '';
  errors.value.newPassword = '';
  forgotError.value = '';

  const pwd = forgotForm.value.newPassword;
  if (!forgotForm.value.otpCode.trim()) {
    errors.value.otpCode = 'Vui lòng nhập mã xác nhận';
    return;
  }
  if (!pwd.trim()) {
    errors.value.newPassword = 'Vui lòng nhập mật khẩu mới';
    return;
  } else if (pwd.length < 6) {
    errors.value.newPassword = 'Mật khẩu phải có ít nhất 6 ký tự';
    return;
  } else if (!/^[A-ZÀ-Ỹ]/.test(pwd)) {
    errors.value.newPassword = 'Mật khẩu phải bắt đầu bằng một chữ cái viết hoa';
    return;
  } else if (!/[^a-zA-Z0-9]/.test(pwd)) {
    errors.value.newPassword = 'Mật khẩu phải chứa ít nhất một ký tự đặc biệt (VD: !@#$%...)';
    return;
  }

  isLoading.value = true;
  try {
    const result = await authService.resetPassword({
      email: forgotForm.value.email.trim(),
      otpCode: forgotForm.value.otpCode.trim(),
      newPassword: pwd
    });
    success(result.message || 'Đặt lại mật khẩu thành công');
    signInForm.value.username = '';
    exitForgotPassword();
  } catch (error) {
    forgotError.value = error.message || 'Đặt lại mật khẩu thất bại';
  } finally {
    isLoading.value = false;
  }
};

// Go to home page without login (for guest shopping)
const goToHomePage = () => {
  success('Chuyển đến trang chủ để mua sắm!');
  setTimeout(() => {
    router.push('/');
  }, 1000);
};

// Debug: Log remember me state changes
watch(() => signInForm.value.rememberMe, (newValue) => {
  console.log('Remember me changed:', newValue);
});

// Component mounted
onMounted(() => {
  // Icons are now handled by @iconify/vue component
});

</script>

<style scoped>
/* Main Container */
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  font-family: 'Nunito', 'Be Vietnam Pro', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* Background Elements */
.background-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(0, 0, 0, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(0, 0, 0, 0.3) 0%, transparent 50%),
    rgba(0, 0, 0, 0.4);
  pointer-events: none;
}

/* Main Wrapper */
.auth-wrapper {
  display: flex;
  width: 100%;
  max-width: 1200px;
  min-height: 600px;
  background: white;
  border-radius: 24px;
  box-shadow: 
    0 25px 50px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* Brand Section */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #2d3748 0%, #1a202c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  position: relative;
}

.brand-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: repeating-linear-gradient(115deg, rgba(255, 255, 255, 0.035) 0 2px, transparent 2px 74px);
  pointer-events: none;
}

.brand-content {
  text-align: center;
  color: white;
  position: relative;
  z-index: 1;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-bottom: 24px;
}

.logo-img {
  height: 96px;
  width: auto;
}

.brand-tagline {
  font-size: 1.25rem;
  margin-bottom: 40px;
  opacity: 0.9;
  font-weight: 300;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1rem;
  opacity: 0.9;
}

.feature-item iconify-icon {
  font-size: 1.5rem;
  color: #48bb78;
  flex-shrink: 0;
}

/* Form Section */
.form-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  background: #fafbfc;
  position: relative;
}

/* Toggle Switch */
.form-toggle-switch {
  margin-bottom: 40px;
  width: 100%;
  max-width: 400px;
}

.toggle-container {
  display: flex;
  background: #e2e8f0;
  border-radius: 50px;
  padding: 4px;
  position: relative;
  width: 100%;
}

.toggle-btn {
  flex: 1;
  padding: 12px 24px;
  border: none;
  background: transparent;
  color: #718096;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  border-radius: 46px;
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
}

.toggle-btn.active {
  color: white;
}

.toggle-slider {
  position: absolute;
  top: 4px;
  left: 4px;
  width: calc(50% - 4px);
  height: calc(100% - 8px);
  background: linear-gradient(135deg, #ff3b18, #c92c0f);
  border-radius: 46px;
  transition: transform 0.3s ease;
  box-shadow: 0 2px 8px rgba(255, 59, 24, 0.3);
}

.toggle-slider.register {
  transform: translateX(100%);
}

.form-container {
  width: 100%;
  max-width: 400px;
  transition: all 0.6s ease;
}

.form-container.hidden {
  opacity: 0;
  visibility: hidden;
  position: absolute;
  pointer-events: none;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-title {
  font-size: 2.25rem;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 8px;
}

.form-subtitle {
  color: #718096;
  font-size: 1.1rem;
  margin-bottom: 0;
  font-weight: 400;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #2d3748;
  font-size: 0.95rem;
}

.form-label iconify-icon {
  font-size: 1.2rem;
  color: #ff3b18;
}

.form-input {
  padding: 16px 20px;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.form-input:focus {
  outline: none;
  border-color: #ff3b18;
  box-shadow: 0 0 0 4px rgba(255, 59, 24, 0.1);
  transform: translateY(-1px);
}

.form-input.error {
  border-color: #e53e3e;
  box-shadow: 0 0 0 4px rgba(229, 62, 62, 0.1);
}

.password-input-wrapper {
  position: relative;
  width: 100%;
}

.password-input {
  width: 100%;
  padding-right: 50px;
}

.password-toggle {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #718096;
  cursor: pointer;
  font-size: 1.2rem;
  transition: color 0.2s ease;
  padding: 8px;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
}

.password-toggle:hover {
  color: #ff3b18;
}

.error-message {
  color: #e53e3e;
  font-size: 0.875rem;
  margin-top: 4px;
  font-weight: 500;
}

.field-hint {
  color: #718096;
  font-size: 0.85rem;
  margin-top: 2px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 16px 0;
  min-height: 40px; /* Ensure minimum height */
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 0; /* Add some padding */
  transition: all 0.2s ease;
}

.checkbox-wrapper:hover {
  opacity: 0.8;
}

.checkbox-input {
  width: 20px;
  height: 20px;
  accent-color: #ff3b18;
  cursor: pointer;
  flex-shrink: 0; /* Prevent shrinking */
  margin: 0; /* Remove default margin */
  appearance: auto; /* Ensure native appearance */
  -webkit-appearance: checkbox; /* Safari compatibility */
  -moz-appearance: checkbox; /* Firefox compatibility */
}

.checkbox-label {
  font-size: 0.95rem;
  color: #4a5568;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
}

.checkbox-icon {
  font-size: 1.1rem;
  color: #ff3b18;
}

.forgot-password {
  color: #ff3b18;
  text-decoration: none;
  font-size: 0.95rem;
  font-weight: 600;
  transition: all 0.2s ease;
}

.forgot-password:hover {
  color: #d92e12;
  text-decoration: underline;
}

.auth-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 18px 24px;
  background: linear-gradient(135deg, #ff3b18, #c92c0f);
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 12px;
  box-shadow: 0 4px 15px rgba(255, 59, 24, 0.4);
}

.auth-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 59, 24, 0.5);
}

.auth-btn:active:not(:disabled) {
  transform: translateY(0);
}

.auth-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.auth-btn.loading {
  pointer-events: none;
}

.auth-btn-secondary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 18px 24px;
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.4);
}

.auth-btn-secondary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(16, 185, 129, 0.5);
}

.auth-btn-secondary:active:not(:disabled) {
  transform: translateY(0);
}

.auth-btn-secondary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.auth-btn-secondary iconify-icon {
  font-size: 1.3rem;
}

.auth-error {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  background: #fed7d7;
  border: 1px solid #feb2b2;
  border-radius: 12px;
  color: #c53030;
  font-size: 0.95rem;
  margin-top: 12px;
  font-weight: 500;
}

.auth-error iconify-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .auth-wrapper {
    max-width: 900px;
  }
  
  .brand-section,
  .form-section {
    padding: 40px 30px;
  }
}

@media (max-width: 768px) {
  .auth-container {
    padding: 20px;
  }
  
  .auth-wrapper {
    flex-direction: column;
    max-width: 500px;
    min-height: auto;
  }
  
  .brand-section {
    padding: 40px 30px 30px;
  }
  
  .form-section {
    padding: 30px 30px 40px;
  }
  
  .form-title {
    font-size: 2rem;
  }
  
  .brand-features {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 16px;
    justify-content: center;
  }
  
  .feature-item {
    font-size: 0.9rem;
  }
}

@media (max-width: 480px) {
  .auth-container {
    padding: 15px;
  }
  
  .auth-wrapper {
    border-radius: 20px;
  }
  
  .brand-section,
  .form-section {
    padding: 30px 20px;
  }
  
  .form-container {
    max-width: 100%;
  }
  
  .form-options {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
    min-height: auto;
  }
  
  .checkbox-wrapper {
    width: 100%;
    justify-content: flex-start;
  }
  
  .form-title {
    font-size: 1.75rem;
  }
  
  .logo-container {
    flex-direction: column;
    gap: 12px;
  }
  
  .brand-features {
    flex-direction: column;
    text-align: center;
  }
}
</style>