import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

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

  // Nếu tài khoản đang lưu ở trình duyệt không còn tồn tại/hợp lệ ở backend
  // (vd: đã bị xoá, hoặc dữ liệu localStorage cũ còn sót lại sau khi DB được
  // reseed), các API /khach-hang-client/* trả 404 với message cụ thể
  // "Không tìm thấy tài khoản". Chỉ dọn phiên đăng nhập khi ĐÚNG message này —
  // tuyệt đối không dọn cho MỌI lỗi 404 (vd "không tìm thấy đơn hàng",
  // "chưa có địa chỉ nào"...), vì đó là 404 hợp lệ của một tài khoản còn tồn
  // tại, không phải dấu hiệu session hỏng. Bug cũ từng đăng xuất nhầm khách
  // đang hoạt động bình thường chỉ vì 1 API con trả 404 không liên quan.
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
  // Get from authenticated user info
  const userInfo = localStorage.getItem('user_info')

  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      return user.id // This is the taiKhoan ID from authentication
    } catch (error) {
      console.error('Error parsing user info:', error)
    }
  }

  // Fallback: try to get from localStorage (for testing)
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

  // Helper methods
  setTaiKhoanId: setTaiKhoanId,
  getTaiKhoanId: getTaiKhoanId,

  // Check if user is authenticated
  isAuthenticated: () => {
    const isLoggedIn = localStorage.getItem('is_logged_in') === 'true'
    const userInfo = localStorage.getItem('user_info')
    return isLoggedIn && userInfo
  },

  // Get current user info
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
