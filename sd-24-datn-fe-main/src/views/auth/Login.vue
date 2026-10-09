<template>
  <div class="login-container">
    <!-- Background overlay -->
    <div class="background-overlay"></div>

    <div class="login-wrapper">
      <!-- Brand Section -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo-container">
            <img src="@/assets/images/logo-trang.png" alt="STRIKER" class="logo-img" />
          </div>
          <p class="brand-tagline">Hệ thống quản lý cửa hàng áo thể thao</p>
          <div class="brand-features">
            <div class="feature-item">
              <iconify-icon icon="solar:check-circle-bold"></iconify-icon>
              <span>Quản lý sản phẩm hiệu quả</span>
            </div>
            <div class="feature-item">
              <iconify-icon icon="solar:check-circle-bold"></iconify-icon>
              <span>Theo dõi đơn hàng real-time</span>
            </div>
            <div class="feature-item">
              <iconify-icon icon="solar:check-circle-bold"></iconify-icon>
              <span>Báo cáo thống kê chi tiết</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Login Form Section -->
      <div class="login-form-section">
        <div class="form-container">
          <div class="form-header">
            <h2 class="form-title">Chào mừng trở lại!</h2>
            <p class="form-subtitle">Đăng nhập vào hệ thống quản lý STRIKER</p>
          </div>

          <form @submit.prevent="handleLogin" class="login-form">
            <!-- Username/Email Field -->
            <div class="form-group">
              <label class="form-label">
                <iconify-icon icon="solar:user-bold-duotone"></iconify-icon>
                Tên đăng nhập
              </label>
              <input
                type="text"
                v-model="loginForm.username"
                class="form-input"
                :class="{ 'error': errors.username }"
                placeholder="Nhập tên đăng nhập của bạn"
                required
              />
              <span v-if="errors.username" class="error-message">{{ errors.username }}</span>
            </div>

            <!-- Password Field -->
            <div class="form-group">
              <label class="form-label">
                <iconify-icon icon="solar:lock-password-bold-duotone"></iconify-icon>
                Mật khẩu
              </label>
              <div class="password-input-wrapper">
                <input
                  :type="showPassword ? 'text' : 'password'"
                  v-model="loginForm.password"
                  class="form-input password-input"
                  :class="{ 'error': errors.password }"
                  placeholder="Nhập mật khẩu của bạn"
                  required
                />
                <button
                  type="button"
                  class="password-toggle"
                  @click="togglePassword"
                >
                  <iconify-icon :icon="showPassword ? 'solar:eye-closed-bold' : 'solar:eye-bold'"></iconify-icon>
                </button>
              </div>
              <span v-if="errors.password" class="error-message">{{ errors.password }}</span>
            </div>

            <!-- Remember Me & Forgot Password -->
            <div class="form-options">
              <label class="checkbox-wrapper">
                <input type="checkbox" v-model="loginForm.rememberMe" class="checkbox-input" />
                <span class="checkbox-label">Ghi nhớ đăng nhập</span>
              </label>
              <a href="#" class="forgot-password" @click.prevent="handleForgotPassword">
                Quên mật khẩu?
              </a>
            </div>

            <!-- Login Button -->
            <button
              type="submit"
              class="login-btn"
              :disabled="isLoading"
              :class="{ 'loading': isLoading }"
            >
              {{ isLoading ? 'Đang đăng nhập...' : 'Đăng nhập hệ thống' }}
            </button>

            <!-- Error Message -->
            <div v-if="loginError" class="login-error">
              <iconify-icon icon="solar:danger-circle-bold-duotone"></iconify-icon>
              {{ loginError }}
            </div>
          </form>

          <!-- Support Info -->
          <div class="support-info">
            <div class="support-item">
              <iconify-icon icon="solar:phone-bold-duotone"></iconify-icon>
              <span>Hỗ trợ: 1900-xxxx</span>
            </div>
            <div class="support-item">
              <iconify-icon icon="solar:letter-bold-duotone"></iconify-icon>
              <span>support@striker.vn</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import authService from '@/services/authService.js'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: '',
        rememberMe: false
      },
      errors: {},
      showPassword: false,
      isLoading: false,
      loginError: ''
    }
  },
  methods: {
    togglePassword() {
      this.showPassword = !this.showPassword;
    },

    validateForm() {
      this.errors = {};
      
      if (!this.loginForm.username.trim()) {
        this.errors.username = 'Vui lòng nhập tên đăng nhập hoặc email';
      }
      
      if (!this.loginForm.password.trim()) {
        this.errors.password = 'Vui lòng nhập mật khẩu';
      } else if (this.loginForm.password.length < 6) {
        this.errors.password = 'Mật khẩu phải có ít nhất 6 ký tự';
      }
      
      return Object.keys(this.errors).length === 0;
    },

    async handleLogin() {
      if (!this.validateForm()) {
        return;
      }

      this.isLoading = true;
      this.loginError = '';

      try {
        await authService.login(this.loginForm);
        
        // Determine redirect path based on user role
        let redirectPath = this.$route.query.redirect;
        
        if (!redirectPath) {
          // Default redirect based on user role
          if (authService.isAdmin()) {
            redirectPath = '/thong-ke'; // Admin goes to dashboard
          } else {
            redirectPath = '/ban-tai-quay'; // Employee goes to sales counter
          }
        }
        
        // Success - redirect to intended page
        this.$router.push(redirectPath);
        
        // Show success message (if toast is available)
        if (this.$toast) {
          this.$toast.success('Đăng nhập thành công!');
        }
        
      } catch (error) {
        this.loginError = error.message || 'Đăng nhập thất bại. Vui lòng thử lại.';
      } finally {
        this.isLoading = false;
      }
    },

    async handleForgotPassword() {
      if (!this.loginForm.username.trim()) {
        this.errors.username = 'Vui lòng nhập email để khôi phục mật khẩu';
        return;
      }

      try {
        await authService.forgotPassword(this.loginForm.username);
        if (this.$toast) {
          this.$toast.success('Link khôi phục mật khẩu đã được gửi đến email của bạn');
        }
      } catch (error) {
        this.loginError = error.message || 'Không thể gửi email khôi phục mật khẩu';
      }
    },

  },

  mounted() {
    // Check if user is already logged in
    if (authService.isAuthenticated()) {
      // Redirect based on user role
      if (authService.isAdmin()) {
        this.$router.push('/thong-ke');
      } else {
        this.$router.push('/ban-tai-quay');
      }
    }
  }
}
</script>

<style scoped>
/* Main Container */
.login-container {
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
    radial-gradient(circle at 20% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  pointer-events: none;
}


/* Main Wrapper */
.login-wrapper {
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
  background: url("data:image/svg+xml,%3Csvg width='150' height='150' viewBox='0 0 150 150' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%23ffffff' fill-opacity='0.04'%3E%3Cg transform='translate(10,20) scale(0.25)'%3E%3Cpath d='M96 288v-48c0-8.8 7.2-16 16-16h16c6.4 0 12.3 3.8 14.9 9.7l10.6 23.8c4.2 9.4 13.6 15.5 23.9 15.5h2.6c14.3 0 26-11.7 26-26v-30.7c0-6 4.9-11 11-11h6.1c2.9 0 5.7 0.9 8.1 2.5l46.4 29.7-18.3 18.3c-4.7 4.7-4.7 12.3 0 17l2.8 2.8c4.7 4.7 12.3 4.7 17 0l23.3-23.3 30.8 20.1-18.3 18.3c-4.7 4.7-4.7 12.3 0 17l2.8 2.8c4.7 4.7 12.3 4.7 17 0l25.3-25.3 52.9 10.6c23.6 4.7 40.5 25.4 40.5 49.4V320H96z'/%3E%3Cpath d='M96 336h320v8c0 13.3-10.7 24-24 24H120c-13.3 0-24-10.7-24-24v-8z'/%3E%3C/g%3E%3Cg transform='translate(80,70) scale(0.25)'%3E%3Cpath d='M96 288v-48c0-8.8 7.2-16 16-16h16c6.4 0 12.3 3.8 14.9 9.7l10.6 23.8c4.2 9.4 13.6 15.5 23.9 15.5h2.6c14.3 0 26-11.7 26-26v-30.7c0-6 4.9-11 11-11h6.1c2.9 0 5.7 0.9 8.1 2.5l46.4 29.7-18.3 18.3c-4.7 4.7-4.7 12.3 0 17l2.8 2.8c4.7 4.7 12.3 4.7 17 0l23.3-23.3 30.8 20.1-18.3 18.3c-4.7 4.7-4.7 12.3 0 17l2.8 2.8c4.7 4.7 12.3 4.7 17 0l25.3-25.3 52.9 10.6c23.6 4.7 40.5 25.4 40.5 49.4V320H96z'/%3E%3Cpath d='M96 336h320v8c0 13.3-10.7 24-24 24H120c-13.3 0-24-10.7-24-24v-8z'/%3E%3C/g%3E%3Cg transform='translate(40,120) scale(0.25)'%3E%3Cpath d='M96 288v-48c0-8.8 7.2-16 16-16h16c6.4 0 12.3 3.8 14.9 9.7l10.6 23.8c4.2 9.4 13.6 15.5 23.9 15.5h2.6c14.3 0 26-11.7 26-26v-30.7c0-6 4.9-11 11-11h6.1c2.9 0 5.7 0.9 8.1 2.5l46.4 29.7-18.3 18.3c-4.7 4.7-4.7 12.3 0 17l2.8 2.8c4.7 4.7 12.3 4.7 17 0l23.3-23.3 30.8 20.1-18.3 18.3c-4.7 4.7-4.7 12.3 0 17l2.8 2.8c4.7 4.7 12.3 4.7 17 0l25.3-25.3 52.9 10.6c23.6 4.7 40.5 25.4 40.5 49.4V320H96z'/%3E%3Cpath d='M96 336h320v8c0 13.3-10.7 24-24 24H120c-13.3 0-24-10.7-24-24v-8z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E") center/200px;
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

/* Login Form Section */
.login-form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  background: #fafbfc;
}

.form-container {
  width: 100%;
  max-width: 400px;
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

.login-form {
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
  color: #3b82f6;
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
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
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
  color: #3b82f6;
}

.error-message {
  color: #e53e3e;
  font-size: 0.875rem;
  margin-top: 4px;
  font-weight: 500;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.checkbox-input {
  width: 20px;
  height: 20px;
  accent-color: #3b82f6;
  cursor: pointer;
}

.checkbox-label {
  font-size: 0.95rem;
  color: #4a5568;
  cursor: pointer;
}

.forgot-password {
  color: #3b82f6;
  text-decoration: none;
  font-size: 0.95rem;
  font-weight: 600;
  transition: all 0.2s ease;
}

.forgot-password:hover {
  color: #2563eb;
  text-decoration: underline;
}

.login-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 18px 24px;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 12px;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.5);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.login-btn.loading {
  pointer-events: none;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.login-error {
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

.login-error iconify-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
}

.support-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.support-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #718096;
  font-size: 0.9rem;
  justify-content: center;
}

.support-item iconify-icon {
  font-size: 1.1rem;
  color: #3b82f6;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .login-wrapper {
    max-width: 900px;
  }
  
  .brand-section,
  .login-form-section {
    padding: 40px 30px;
  }
}

@media (max-width: 768px) {
  .login-container {
    padding: 20px;
  }
  
  .login-wrapper {
    flex-direction: column;
    max-width: 500px;
    min-height: auto;
  }
  
  .brand-section {
    padding: 40px 30px 30px;
  }
  
  .login-form-section {
    padding: 30px 30px 40px;
  }
  
  .brand-name {
    font-size: 2.5rem;
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
  .login-container {
    padding: 15px;
  }
  
  .login-wrapper {
    border-radius: 20px;
  }
  
  .brand-section,
  .login-form-section {
    padding: 30px 20px;
  }
  
  .form-container {
    max-width: 100%;
  }
  
  .form-options {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .brand-name {
    font-size: 2rem;
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
  
  .support-info {
    gap: 8px;
  }
  
  .support-item {
    font-size: 0.85rem;
  }
}
</style>
