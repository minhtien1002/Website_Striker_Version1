import { AuthAPI } from './authAPI.js';

// Authentication Service for Client
class AuthService {
  constructor() {
    this.user = JSON.parse(localStorage.getItem('user_info') || 'null');
    this.isLoggedIn = localStorage.getItem('is_logged_in') === 'true';
  }

  async login(credentials) {
    try {
      // Use customer login endpoint by default (auth-client)
      const response = await AuthAPI.login({
        username: credentials.username, // Use username directly
        password: credentials.password,
        rememberMe: credentials.rememberMe
      });

      if (response.success) {
        // Map backend response to frontend format
        const userInfo = {
          id: response.id,
          ma: response.ma,
          username: response.tenDangNhap,
          email: response.email,
          soDienThoai: response.soDienThoai,
          tenQuyen: response.tenQuyen,
          capQuyenHan: response.capQuyenHan,
          customerId: response.customerId, // ID khách hàng
          tenKhachHang: response.tenKhachHang, // Tên khách hàng
          loginTime: new Date().toISOString()
        };

        // Store user info and login status
        localStorage.setItem('user_info', JSON.stringify(userInfo));
        localStorage.setItem('is_logged_in', 'true');
        
        if (credentials.rememberMe) {
          localStorage.setItem('remember_me', 'true');
        }

        this.user = userInfo;
        this.isLoggedIn = true;

        return { success: true, user: userInfo };
      } else {
        throw new Error(response.message || 'Đăng nhập thất bại');
      }
    } catch (error) {
      if (error.response && error.response.data && error.response.data.message) {
        throw new Error(error.response.data.message);
      } else {
        throw new Error(error.message || 'Lỗi kết nối đến server');
      }
    }
  }

  // Admin login method for admin panel access
  async adminLogin(credentials) {
    try {
      const response = await AuthAPI.adminLogin({
        username: credentials.username,
        password: credentials.password,
        rememberMe: credentials.rememberMe
      });

      if (response.success) {
        // Map backend response to frontend format
        const userInfo = {
          id: response.id,
          ma: response.ma,
          username: response.tenDangNhap,
          email: response.email,
          soDienThoai: response.soDienThoai,
          tenQuyen: response.tenQuyen,
          capQuyenHan: response.capQuyenHan,
          loginTime: new Date().toISOString()
        };

        // Store user info and login status
        localStorage.setItem('user_info', JSON.stringify(userInfo));
        localStorage.setItem('is_logged_in', 'true');
        
        if (credentials.rememberMe) {
          localStorage.setItem('remember_me', 'true');
        }

        this.user = userInfo;
        this.isLoggedIn = true;

        return { success: true, user: userInfo };
      } else {
        throw new Error(response.message || 'Đăng nhập thất bại');
      }
    } catch (error) {
      if (error.response && error.response.data && error.response.data.message) {
        throw new Error(error.response.data.message);
      } else {
        throw new Error(error.message || 'Lỗi kết nối đến server');
      }
    }
  }

  async logout() {
    try {
      // Call backend logout API with user ID if available
      const userId = this.user?.id;
      await AuthAPI.logout(userId);
    } catch (error) {
      console.error('Logout API error:', error);
      // Continue with local logout even if API fails
    }

    // Clear local storage
    localStorage.removeItem('user_info');
    localStorage.removeItem('is_logged_in');
    localStorage.removeItem('remember_me');
    
    this.user = null;
    this.isLoggedIn = false;
  }

  // Utility methods
  isAuthenticated() {
    return this.isLoggedIn && !!this.user;
  }

  getUser() {
    return this.user;
  }

  hasRole(role) {
    return this.user?.role === role;
  }

  // Check if user is admin (capQuyenHan = 1)
  isAdmin() {
    return this.user?.capQuyenHan === 1;
  }

  // Check if user is employee (capQuyenHan = 2)
  isEmployee() {
    return this.user?.capQuyenHan === 2;
  }

  // Check if user is customer (capQuyenHan = 3 or no capQuyenHan for regular customers)
  isCustomer() {
    return this.user?.capQuyenHan === 3 || (!this.user?.capQuyenHan && this.isAuthenticated());
  }

  // Check if user has access to specific feature
  hasPermission(permission) {
    if (this.isAdmin()) {
      return true; // Admin has full access
    }
    
    if (this.isEmployee()) {
      // Define employee permissions
      const employeePermissions = [
        'ban-tai-quay', // Sales counter
        'hoa-don', // Invoices
        'khach-hang', // Customers
        'phieu-giam-gia' // Vouchers
      ];
      return employeePermissions.includes(permission);
    }

    if (this.isCustomer()) {
      // Define customer permissions
      const customerPermissions = [
        'shopping', // Shopping
        'cart', // Shopping cart
        'orders', // View own orders
        'profile' // Edit own profile
      ];
      return customerPermissions.includes(permission);
    }
    
    return false;
  }

  // Get user role name
  getUserRole() {
    if (this.isAdmin()) return 'Admin';
    if (this.isEmployee()) return 'Nhân viên';
    if (this.isCustomer()) return 'Khách hàng';
    return 'Unknown';
  }

  // Simple session check - no token expiration needed
  isSessionValid() {
    return this.isAuthenticated();
  }

  // Check if user can access admin panel
  canAccessAdmin() {
    return this.isAdmin() || this.isEmployee();
  }

  // Get redirect path after login based on user role
  getRedirectPath() {
    // Always redirect to home page after login
    return '/';
  }

  // Get redirect path for guest users after checkout
  getGuestRedirectPath() {
    // Guest users go to home page after successful checkout
    return '/';
  }

  async register(registerData) {
    try {
      const response = await AuthAPI.register({
        name: registerData.name,
        email: registerData.email,
        phoneNumber: registerData.phoneNumber,
        password: registerData.password
      });

      if (response.success) {
        // Trả kèm tên đăng nhập do hệ thống tạo ra (từ họ và tên) để FE báo cho khách biết
        // dùng gì để đăng nhập lần sau.
        return { success: true, message: response.message, tenDangNhap: response.tenDangNhap };
      } else {
        throw new Error(response.message || 'Đăng ký thất bại');
      }
    } catch (error) {
      if (error.response && error.response.data && error.response.data.message) {
        throw new Error(error.response.data.message);
      } else {
        throw new Error(error.message || 'Lỗi kết nối đến server');
      }
    }
  }

  async forgotPassword(email) {
    try {
      const response = await AuthAPI.forgotPassword(email);
      if (response.success) {
        return { success: true, message: response.message, debugOtpCode: response.debugOtpCode };
      }
      throw new Error(response.message || 'Không thể gửi mã xác nhận');
    } catch (error) {
      if (error.response && error.response.data && error.response.data.message) {
        throw new Error(error.response.data.message);
      } else {
        throw new Error(error.message || 'Lỗi kết nối đến server');
      }
    }
  }

  async resetPassword({ email, otpCode, newPassword }) {
    try {
      const response = await AuthAPI.resetPassword({ email, otpCode, newPassword });
      if (response.success) {
        return { success: true, message: response.message };
      }
      throw new Error(response.message || 'Đặt lại mật khẩu thất bại');
    } catch (error) {
      if (error.response && error.response.data && error.response.data.message) {
        throw new Error(error.response.data.message);
      } else {
        throw new Error(error.message || 'Lỗi kết nối đến server');
      }
    }
  }
}

// Create and export singleton instance
const authService = new AuthService();
export default authService;

// Export individual methods for convenience
export const {
  login,
  adminLogin,
  logout,
  isAuthenticated,
  getUser,
  hasRole,
  isAdmin,
  isEmployee,
  isCustomer,
  hasPermission,
  getUserRole,
  isSessionValid,
  canAccessAdmin,
  getRedirectPath
} = authService;
