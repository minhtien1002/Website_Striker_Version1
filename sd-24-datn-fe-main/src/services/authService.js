import axios from 'axios';

// Authentication Service
class AuthService {
  constructor() {
    this.user = JSON.parse(localStorage.getItem('user_info') || 'null');
    this.isLoggedIn = localStorage.getItem('is_logged_in') === 'true';
    this.baseURL = 'http://localhost:8080/api/auth';
  }

  async login(credentials) {
    try {
      const response = await axios.post(`${this.baseURL}/login`, {
        tenDangNhap: credentials.username,
        matKhau: credentials.password,
        rememberMe: credentials.rememberMe
      });

      if (response.data.success) {
        // Map backend response to frontend format
        const userInfo = {
          id: response.data.id,
          ma: response.data.ma,
          username: response.data.tenDangNhap,
          email: response.data.email,
          soDienThoai: response.data.soDienThoai,
          tenQuyen: response.data.tenQuyen,
          capQuyenHan: response.data.capQuyenHan,
          // Nhân viên gắn với tài khoản - dùng để ghi người tạo / thanh toán hóa đơn
          nhanVienId: response.data.nhanVienId,
          maNhanVien: response.data.maNhanVien,
          tenNhanVien: response.data.tenNhanVien,
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

        return { user: userInfo };
      } else {
        throw new Error(response.data.message || 'Đăng nhập thất bại');
      }
    } catch (error) {
      if (error.response && error.response.data && error.response.data.message) {
        throw new Error(error.response.data.message);
      } else {
        throw new Error('Lỗi kết nối đến server');
      }
    }
  }

  async logout() {
    try {
      // Call backend logout API with user ID if available
      const userId = this.user?.id;
      if (userId) {
        await axios.post(`${this.baseURL}/logout?userId=${userId}`);
      } else {
        await axios.post(`${this.baseURL}/logout`);
      }
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

  /** Id nhân viên của người đang đăng nhập (null nếu tài khoản chưa gắn nhân viên). */
  getNhanVienId() {
    return this.user?.nhanVienId ?? null;
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
    
    return false;
  }

  // Get user role name
  getUserRole() {
    if (this.isAdmin()) return 'Admin';
    if (this.isEmployee()) return 'Nhân viên';
    return 'Unknown';
  }

  // Simple session check - no token expiration needed
  isSessionValid() {
    return this.isAuthenticated();
  }
}

// Create and export singleton instance
const authService = new AuthService();
export default authService;

// Export individual methods for convenience
export const {
  login,
  logout,
  isAuthenticated,
  getUser,
  hasRole,
  isAdmin,
  isEmployee,
  hasPermission,
  getUserRole,
  isSessionValid
} = authService;
