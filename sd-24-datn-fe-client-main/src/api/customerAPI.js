import axios from 'axios'

// Địa chỉ API Backend. Nếu dự án của bạn dùng Vite Proxy, hãy đổi thành '/api'
const API_BASE_URL = 'http://localhost:8085/api'

// Create axios instance with authentication
const createAuthenticatedAPI = () => {
  const token = localStorage.getItem('token')
  const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json'
    }
  })

  api.interceptors.response.use(
    (response) => response,
    (error) => {
      const isAccountNotFound = error.response?.status === 404 &&
        error.response?.data?.message === 'Không tìm thấy tài khoản'
      if (isAccountNotFound) {
        localStorage.removeItem('user_info')
        localStorage.removeItem('is_logged_in')
        localStorage.removeItem('remember_me')
        if (window.location.pathname.startsWith('/account')) {
          window.location.href = '/auth'
        }
      }
      return Promise.reject(error)
    }
  )

  return api
}

// Helper function to get taiKhoanId from authenticated user
const getTaiKhoanId = () => {
  const userInfo = localStorage.getItem('user_info')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      return user.id
    } catch (error) {
      console.error('Error parsing user info:', error)
    }
  }
  const taiKhoanId = localStorage.getItem('taiKhoanId')
  if (taiKhoanId) {
    return parseInt(taiKhoanId)
  }
  return 9 // Default fallback for testing
}

// Helper function to set taiKhoanId for testing
const setTaiKhoanId = (id) => {
  localStorage.setItem('taiKhoanId', id.toString())
}

export const customerAPI = {
  // Get customer profile
  getProfile: async (taiKhoanId = null) => {
    const api = createAuthenticatedAPI()
    const accountId = taiKhoanId || getTaiKhoanId()
    const response = await api.get('/khach-hang-client/profile', {
      params: { taiKhoanId: accountId }
    })
    return response.data
  },

  // Get customer overview/dashboard
  getOverview: async (taiKhoanId = null) => {
    const api = createAuthenticatedAPI()
    const accountId = taiKhoanId || getTaiKhoanId()
    const response = await api.get('/khach-hang-client/overview', {
      params: { taiKhoanId: accountId }
    })
    return response.data
  },

  // Get customer order history
  getOrderHistory: async (page = 0, size = 10, taiKhoanId = null) => {
    const api = createAuthenticatedAPI()
    const accountId = taiKhoanId || getTaiKhoanId()
    const response = await api.get('/khach-hang-client/orders', {
      params: { taiKhoanId: accountId, page, size }
    })
    return response.data
  },

  // Get customer order details
  getOrderDetails: async (orderId, taiKhoanId = null) => {
    const api = createAuthenticatedAPI()
    const accountId = taiKhoanId || getTaiKhoanId()
    const response = await api.get(`/khach-hang-client/orders/${orderId}`, {
      params: { taiKhoanId: accountId }
    })
    return response.data
  },

  // Get customer addresses
  getAddresses: async (taiKhoanId = null) => {
    const api = createAuthenticatedAPI()
    const accountId = taiKhoanId || getTaiKhoanId()
    const response = await api.get('/khach-hang-client/addresses', {
      params: { taiKhoanId: accountId }
    })
    return response.data
  },

  // Add new address
  addAddress: async (addressData, taiKhoanId = null) => {
    const api = createAuthenticatedAPI()
    const accountId = taiKhoanId || getTaiKhoanId()
    const response = await api.post('/khach-hang-client/addresses', addressData, {
      params: { taiKhoanId: accountId }
    })
    return response.data
  },

  // Update address
  updateAddress: async (addressId, addressData, taiKhoanId = null) => {
    const api = createAuthenticatedAPI()
    const accountId = taiKhoanId || getTaiKhoanId()
    const response = await api.put(`/khach-hang-client/addresses/${addressId}`, addressData, {
      params: { taiKhoanId: accountId }
    })
    return response.data
  },

  // Delete address
  deleteAddress: async (addressId, taiKhoanId = null) => {
    const api = createAuthenticatedAPI()
    const accountId = taiKhoanId || getTaiKhoanId()
    const response = await api.delete(`/khach-hang-client/addresses/${addressId}`, {
      params: { taiKhoanId: accountId }
    })
    return response.data
  },

  // Set default address
  setDefaultAddress: async (addressId, taiKhoanId = null) => {
    const api = createAuthenticatedAPI()
    const accountId = taiKhoanId || getTaiKhoanId()
    const response = await api.put(`/khach-hang-client/addresses/${addressId}/set-default`, {}, {
      params: { taiKhoanId: accountId }
    })
    return response.data
  },

  // Search order by code (for guest users)
  searchOrderByCode: async (orderCode) => {
    const api = axios.create({ baseURL: API_BASE_URL })
    const response = await api.get('/ban-hang-client/tra-cuu-don-hang', {
      params: { ma: orderCode }
    })
    return {
      success: true,
      data: response.data
    }
  },

  // =========================================================================
  // API YÊU CẦU TRẢ HÀNG (ĐÃ CẬP NHẬT BẮT LỖI CHUẨN XÁC)
  // =========================================================================
  /**
   * Gửi yêu cầu trả hàng (Hoàn hàng)
   * Endpoint chuẩn: PUT /api/hoa-don/{orderId}/yeu-cau-hoan-hang?idKhachHangActor=...&lyDo=...
   */
  requestReturnOrder: async (orderId, customerId, lyDo) => {
    const api = createAuthenticatedAPI()
    const actorId = customerId || getTaiKhoanId()

    try {
      // Gửi request với method PUT. 
      // Tham số thứ 2 là body (null vì không có body). 
      // Tham số thứ 3 là config (chứa params).
      const response = await api.put(`/hoa-don/${orderId}/yeu-cau-hoan-hang`, null, {
        params: {
          idKhachHangActor: actorId,
          lyDo: lyDo
        }
      })
      return response.data
      
    } catch (error) {
      // 1. Bắt lỗi 500 (Backend crash, NullPointerException, DataIntegrity...)
      if (error.response && error.response.status === 500) {
        // Backend đã được sửa để luôn trả về data.message, nhưng nếu có lỗi khác vẫn bắt được
        const backendMsg = error.response.data?.message;
        throw new Error(backendMsg || "Hệ thống đang gặp sự cố xử lý. Vui lòng thử lại sau ít phút!");
      }
      
      // 2. Bắt lỗi 400 (Lỗi logic nghiệp vụ: Đơn chưa giao, lý do rỗng...)
      if (error.response && error.response.data && error.response.data.message) {
        throw new Error(error.response.data.message);
      }
      
      // 3. Lỗi mất kết nối mạng hoặc lỗi không xác định khác
      throw new Error("Không thể kết nối đến máy chủ. Vui lòng kiểm tra lại đường truyền mạng!");
    }
  },

  // =========================================================================
  // API MUA LẠI ĐƠN HÀNG
  // =========================================================================
  muaLaiDonHang: async (orderId, customerId = null) => {
    const api = createAuthenticatedAPI()
    
    let idKhachHang = customerId;
    if (!idKhachHang) {
      const user = customerAPI.getCurrentUser();
      idKhachHang = user?.khachHangId || user?.id; 
    }

    if (!idKhachHang) {
      throw new Error('Không tìm thấy ID khách hàng. Vui lòng đăng nhập lại!');
    }

    try {
      const response = await api.post(`/hoa-don/${orderId}/mua-lai`, null, {
        params: { idKhachHang: idKhachHang }
      })
      return response.data
    } catch (error) {
      if (error.response && error.response.data && error.response.data.message) {
        throw new Error(error.response.data.message);
      }
      throw new Error("Không thể thực hiện mua lại đơn hàng lúc này!");
    }
  },

  // Helper methods
  setTaiKhoanId: setTaiKhoanId,
  getTaiKhoanId: getTaiKhoanId,

  isAuthenticated: () => {
    const isLoggedIn = localStorage.getItem('is_logged_in') === 'true'
    const userInfo = localStorage.getItem('user_info')
    return isLoggedIn && userInfo
  },

  getCurrentUser: () => {
    const userInfo = localStorage.getItem('user_info')
    if (userInfo) {
      try {
        return JSON.parse(userInfo)
      } catch (error) {
        console.error('Error parsing user info:', error)
        return null
      }
    }
    return null
  }
}

export default customerAPI