import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

// Create axios instance with authentication
const createAuthenticatedAPI = () => {
  const token = localStorage.getItem('token')
  return axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json'
    }
  })
}

// Helper function to get customer ID
const getCustomerId = () => {
  const userInfo = localStorage.getItem('user_info')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      return user.customerId || user.id
    } catch (error) {
      console.error('Error parsing user info:', error)
    }
  }
  return 1 // Default guest customer ID
}

export const cartAPI = {
  // Create pending invoice for cart
  createPendingInvoice: async (customerId = null) => {
    const api = createAuthenticatedAPI()
    const customerIdToUse = customerId || getCustomerId()
    
    const response = await api.post('/ban-hang-client/hoa-don-cho', {
      idKhachHang: customerIdToUse
    })
    return response.data
  },

  // Get customer's pending invoices (carts)
  getPendingInvoices: async (customerId = null) => {
    const api = createAuthenticatedAPI()
    const customerIdToUse = customerId || getCustomerId()
    
    const response = await api.get(`/ban-hang-client/hoa-don-cho/khach-hang/${customerIdToUse}`)
    return response.data
  },

  // Add product to cart
  addToCart: async (invoiceId, productData) => {
    const api = createAuthenticatedAPI()
    
    const response = await api.post(`/ban-hang-client/gio-hang/${invoiceId}/them-san-pham`, {
      idChiTietSanPham: productData.idChiTietSanPham,
      soLuong: productData.soLuong || 1,
      giaBan: productData.giaBan
    })
    return response.data
  },

  // Get cart contents
  getCart: async (invoiceId) => {
    const api = createAuthenticatedAPI()
    
    const response = await api.get(`/ban-hang-client/gio-hang/${invoiceId}`)
    return response.data
  },

  // Update cart item quantity
  updateCartItemQuantity: async (invoiceId, itemId, quantity) => {
    const api = createAuthenticatedAPI()
    
    const response = await api.put(`/ban-hang-client/gio-hang/${invoiceId}/san-pham/${itemId}`, {
      soLuong: quantity
    })
    return response.data
  },

  // Remove item from cart
  removeFromCart: async (invoiceId, itemId) => {
    const api = createAuthenticatedAPI()
    
    const response = await api.delete(`/ban-hang-client/gio-hang/${invoiceId}/san-pham/${itemId}`)
    return response.data
  },

  // Clear entire cart
  clearCart: async (invoiceId) => {
    const api = createAuthenticatedAPI()
    
    const response = await api.delete(`/ban-hang-client/gio-hang/${invoiceId}/xoa-tat-ca`)
    return response.data
  },

  // Process payment and complete order
  processPayment: async (invoiceId, paymentData) => {
    const api = createAuthenticatedAPI()
    
    const response = await api.post(`/ban-hang-client/thanh-toan/${invoiceId}`, {
      phuongThucThanhToan: paymentData.paymentMethod,
      diaChiGiaoHang: paymentData.shippingAddress,
      ghiChu: paymentData.notes,
      maGiamGia: paymentData.voucherCode,
      thongTinKhachHang: paymentData.customerInfo
    })
    return response.data
  },

  // Get order details after completion
  getOrderDetails: async (orderId) => {
    const api = createAuthenticatedAPI()
    
    const response = await api.get(`/ban-hang-client/don-hang/${orderId}`)
    return response.data
  },

  // Validate cart before checkout
  validateCart: async (cartItems) => {
    const api = createAuthenticatedAPI()
    
    const response = await api.post('/ban-hang-client/kiem-tra-gio-hang', {
      items: cartItems.map(item => ({
        idChiTietSanPham: item.idChiTietSanPham,
        soLuong: item.soLuong
      }))
    })
    return response.data
  },

  // Calculate shipping fee
  calculateShipping: async (address, cartItems) => {
    const api = createAuthenticatedAPI()
    
    const response = await api.post('/ban-hang-client/tinh-phi-van-chuyen', {
      diaChi: address,
      sanPhams: cartItems.map(item => ({
        id: item.id,
        soLuong: item.soLuong,
        trongLuong: item.trongLuong || 0.5 // Default weight
      }))
    })
    return response.data
  },

  // Helper methods
  getCustomerId: getCustomerId,

  // Format cart data for frontend display
  formatCartData: (backendCartData) => {
    if (!backendCartData || !backendCartData.items) {
      return []
    }

    return backendCartData.items.map(item => ({
      id: item.id,
      idChiTietSanPham: item.idChiTietSanPham,
      tenSanPham: item.tenSanPham,
      hinhAnh: item.hinhAnh,
      tenMauSac: item.tenMauSac,
      tenKichCo: item.tenKichCo,
      giaBanHienTai: item.giaBan,
      soLuong: item.soLuong,
      soLuongTonKho: item.soLuongTonKho,
      thanhTien: item.giaBan * item.soLuong
    }))
  },

  // Format order data for backend submission
  formatOrderData: (orderData) => {
    return {
      idKhachHang: orderData.customerId || getCustomerId(),
      phuongThucThanhToan: orderData.paymentMethod,
      tongTien: orderData.totalPrice,
      phiVanChuyen: orderData.shippingFee,
      giamGia: orderData.discountAmount,
      maGiamGia: orderData.voucherCode,
      ghiChu: orderData.notes,
      diaChiGiaoHang: orderData.shippingAddress,
      thongTinKhachHang: {
        ten: orderData.customerInfo.ten,
        soDienThoai: orderData.customerInfo.soDienThoai,
        email: orderData.customerInfo.email
      },
      chiTietDonHang: orderData.cartItems.map(item => ({
        idChiTietSanPham: item.idChiTietSanPham,
        soLuong: item.soLuong,
        giaBan: item.giaBanHienTai
      }))
    }
  }
}

export default cartAPI
