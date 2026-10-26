import axios from 'axios'
import authService from './authService.js'

const API_BASE_URL = 'http://localhost:8080/api'

// Create axios instance with default config
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
})

// Cart API endpoints
export const cartAPI = {
  // 1. Tạo hóa đơn chờ (Create pending invoice)
  createPendingInvoice: async (customerId = null) => {
    try {
      const url = '/ban-hang-client/hoa-don-cho'
      const params = customerId ? { khachHangId: customerId } : {}
      
      const response = await apiClient.post(url, null, { params })
      return response.data
    } catch (error) {
      console.error('Error creating pending invoice:', error)
      throw new Error(error.response?.data || 'Không thể tạo hóa đơn chờ')
    }
  },

  // 2. Lấy hóa đơn chờ của khách hàng (Get pending invoices by customer)
  getPendingInvoicesByCustomer: async (customerId) => {
    try {
      const response = await apiClient.get(`/ban-hang-client/hoa-don-cho/khach-hang/${customerId}`)
      return response.data
    } catch (error) {
      console.error('Error fetching pending invoices:', error)
      throw new Error(error.response?.data || 'Không thể lấy danh sách hóa đơn chờ')
    }
  },

  // 3. Thêm sản phẩm vào giỏ hàng (Add product to cart)
  addToCart: async (invoiceId, productData) => {
    try {
      const requestBody = {
        idChiTietSanPham: productData.idChiTietSanPham,
        soLuong: productData.soLuong,
        gia: productData.gia
      }

      const response = await apiClient.post(
        `/ban-hang-client/gio-hang/${invoiceId}/them-san-pham`,
        requestBody
      )
      return response.data
    } catch (error) {
      console.error('Error adding to cart:', error)
      throw new Error(error.response?.data || 'Không thể thêm sản phẩm vào giỏ hàng')
    }
  },

  // 4. Lấy giỏ hàng (Get cart)
  getCart: async (invoiceId) => {
    try {
      console.log('Getting cart for invoiceId:', invoiceId)
      const response = await apiClient.get(`/ban-hang-client/gio-hang/${invoiceId}`)
      console.log('Cart API full response:', response)
      console.log('Cart API response data:', response.data)
      console.log('Cart API response structure:', {
        id: response.data?.id,
        items: response.data?.items,
        itemsLength: response.data?.items?.length,
        totalItems: response.data?.totalItems,
        tongTien: response.data?.tongTien
      })
      return response.data
    } catch (error) {
      if (error.response?.status === 404) {
        console.log('Cart not found (404) - this is normal for empty cart')
        return null // Giỏ hàng trống - không phải lỗi
      }
      console.error('Error fetching cart:', error)
      throw new Error(error.response?.data || 'Lỗi kết nối khi tải giỏ hàng')
    }
  },

  // 5. Cập nhật số lượng sản phẩm trong giỏ (Update cart item quantity)
  updateCartItem: async (invoiceId, cartItemId, quantity) => {
    try {
      const requestBody = {
        soLuong: quantity
      }

      const response = await apiClient.put(
        `/ban-hang-client/gio-hang/${invoiceId}/san-pham/${cartItemId}`,
        requestBody
      )
      return response.data
    } catch (error) {
      console.error('Error updating cart item:', error)
      throw new Error(error.response?.data || 'Không thể cập nhật số lượng sản phẩm')
    }
  },

  // 6. Xóa sản phẩm khỏi giỏ hàng (Remove item from cart)
  removeFromCart: async (invoiceId, cartItemId) => {
    try {
      const response = await apiClient.delete(
        `/ban-hang-client/gio-hang/${invoiceId}/san-pham/${cartItemId}`
      )
      return response.data
    } catch (error) {
      console.error('Error removing from cart:', error)
      throw new Error(error.response?.data || 'Không thể xóa sản phẩm khỏi giỏ hàng')
    }
  },

  // 7. Xóa toàn bộ giỏ hàng (Clear entire cart)
  clearCart: async (invoiceId) => {
    try {
      const response = await apiClient.delete(`/ban-hang-client/gio-hang/${invoiceId}/xoa-tat-ca`)
      return response.data
    } catch (error) {
      console.error('Error clearing cart:', error)
      throw new Error(error.response?.data || 'Không thể xóa toàn bộ giỏ hàng')
    }
  },

  // 8. Thanh toán (Process payment)
  processPayment: async (invoiceId, paymentData) => {
    try {
      const requestBody = {
        phuongThucThanhToan: paymentData.phuongThucThanhToan || 'Tiền mặt',
        tienKhachDua: paymentData.tienKhachDua || 0,
        ghiChu: paymentData.ghiChu || ''
      }

      const response = await apiClient.post(
        `/ban-hang-client/thanh-toan/${invoiceId}`,
        requestBody
      )
      return response.data
    } catch (error) {
      console.error('Error processing payment:', error)
      throw new Error(error.response?.data || 'Không thể xử lý thanh toán')
    }
  },

  // 9. Lấy chi tiết đơn hàng (Get order details)
  getOrderDetails: async (invoiceId) => {
    try {
      const response = await apiClient.get(`/ban-hang-client/don-hang/${invoiceId}`)
      return response.data
    } catch (error) {
      console.error('Error getting order details:', error)
      // Fallback: try to get invoice details from admin API
      try {
        const fallbackResponse = await apiClient.get(`/hoa-don/${invoiceId}`)
        return fallbackResponse.data
      } catch (fallbackError) {
        console.error('Fallback API also failed:', fallbackError)
        throw new Error('Không thể tải thông tin đơn hàng')
      }
    }
  }
}

// Cart Service - Business logic layer
export class CartService {
  constructor() {
    // Don't cache these values, always get from localStorage
  }
  
  get currentInvoiceId() {
    return localStorage.getItem('currentInvoiceId')
  }
  
  get customerId() {
    // Khách đã đăng nhập với vai trò khách hàng: dùng đúng customerId của họ.
    // KHÔNG fallback về 1 (khách lẻ) khi đã đăng nhập — làm vậy sẽ gộp nhầm
    // giỏ hàng của họ vào giỏ hàng dùng chung của "khách lẻ" mặc định.
    if (authService.isAuthenticated() && authService.isCustomer()) {
      const user = authService.getUser()
      if (user?.customerId) {
        return user.customerId
      }
      console.warn('Tài khoản đã đăng nhập nhưng thiếu customerId — kiểm tra lại liên kết tài khoản/khách hàng.')
      return null
    }

    // Khách chưa đăng nhập (guest): trả về null để backend tự tạo một hóa đơn
    // chờ RIÊNG cho phiên này, thay vì gộp chung mọi khách vãng lai vào 1 giỏ.
    return null
  }

  // Lấy hoặc tạo hóa đơn chờ
  async getOrCreatePendingInvoice() {
    try {
      if (!this.currentInvoiceId) {
        console.log('Creating pending invoice with customerId:', this.customerId)
        console.log('User authentication status:', {
          isAuthenticated: authService.isAuthenticated(),
          isCustomer: authService.isCustomer(),
          user: authService.getUser()
        })
        
        const invoiceData = await cartAPI.createPendingInvoice(this.customerId)
        localStorage.setItem('currentInvoiceId', invoiceData.id)
        return invoiceData
      }

      // Kiểm tra hóa đơn hiện tại có tồn tại không
      try {
        const cart = await cartAPI.getCart(this.currentInvoiceId)
        if (cart) {
          return { id: this.currentInvoiceId }
        }
      } catch (error) {
        // Nếu hóa đơn không tồn tại (404 hoặc 400), clear localStorage và tạo mới
        console.log('Invoice not found, clearing localStorage and creating new one...')
        localStorage.removeItem('currentInvoiceId')
        const invoiceData = await cartAPI.createPendingInvoice(this.customerId)
        localStorage.setItem('currentInvoiceId', invoiceData.id)
        return invoiceData
      }

      return { id: this.currentInvoiceId }
    } catch (error) {
      console.error('Error getting or creating pending invoice:', error)
      throw error
    }
  }

  // Thêm sản phẩm vào giỏ hàng
  async addToCart(productDetailId, quantity, price) {
    try {
      // Đảm bảo có hóa đơn chờ
      await this.getOrCreatePendingInvoice()

      const productData = {
        idChiTietSanPham: productDetailId,
        soLuong: quantity,
        gia: price
      }

      const cartData = await cartAPI.addToCart(this.currentInvoiceId, productData)
      return cartData
    } catch (error) {
      console.error('Error adding to cart:', error)
      
      // Nếu lỗi 400/404 (invoice không tồn tại), thử tạo lại và add lần nữa
      if (error.response?.status === 400 || error.response?.status === 404) {
        console.log('Invoice may be invalid, recreating and retrying...')
        localStorage.removeItem('currentInvoiceId')
        await this.getOrCreatePendingInvoice()
        
        const productData = {
          idChiTietSanPham: productDetailId,
          soLuong: quantity,
          gia: price
        }
        
        const cartData = await cartAPI.addToCart(this.currentInvoiceId, productData)
        return cartData
      }
      
      throw error
    }
  }

  // Lấy giỏ hàng hiện tại
  async getCart() {
    try {
      if (!this.currentInvoiceId) {
        console.log('No currentInvoiceId found - cart is empty')
        return null
      }

      console.log('CartService getting cart with invoiceId:', this.currentInvoiceId)
      console.log('CartService calling API endpoint:', `/ban-hang-client/gio-hang/${this.currentInvoiceId}`)
      const cart = await cartAPI.getCart(this.currentInvoiceId)
      console.log('CartService received cart data:', cart)
      console.log('CartService cart.items:', cart?.items)
      console.log('CartService cart.totalItems:', cart?.totalItems)
      
      // If cart is null, it means empty cart (not an error)
      if (!cart) {
        console.log('Cart is empty (API returned null)')
      }
      
      return cart
    } catch (error) {
      // Only log as error if it's a real API error, not empty cart
      if (error.message && !error.message.includes('404')) {
        console.error('Real error getting cart:', error)
        throw error
      }
      console.log('Cart not found (likely empty):', error.message)
      return null
    }
  }

  // Cập nhật số lượng sản phẩm
  async updateQuantity(cartItemId, quantity) {
    try {
      if (!this.currentInvoiceId) {
        throw new Error('Giỏ hàng trống. Vui lòng thêm sản phẩm trước khi cập nhật.')
      }

      console.log('Updating quantity:', { invoiceId: this.currentInvoiceId, cartItemId, quantity })
      const cartData = await cartAPI.updateCartItem(this.currentInvoiceId, cartItemId, quantity)
      console.log('Update quantity result:', cartData)
      return cartData
    } catch (error) {
      console.error('Error updating quantity:', error)
      throw error
    }
  }

  // Xóa sản phẩm khỏi giỏ hàng
  async removeItem(cartItemId) {
    try {
      if (!this.currentInvoiceId) {
        throw new Error('Giỏ hàng trống. Không có sản phẩm để xóa.')
      }

      console.log('Removing item:', { invoiceId: this.currentInvoiceId, cartItemId })
      const cartData = await cartAPI.removeFromCart(this.currentInvoiceId, cartItemId)
      console.log('Remove item result:', cartData)
      return cartData
    } catch (error) {
      console.error('Error removing item:', error)
      throw error
    }
  }

  // Xóa toàn bộ giỏ hàng
  async clearCart() {
    try {
      if (!this.currentInvoiceId) {
        return
      }

      await cartAPI.clearCart(this.currentInvoiceId)
      
      // Reset local storage
      localStorage.removeItem('currentInvoiceId')
      this.currentInvoiceId = null
    } catch (error) {
      console.error('Error clearing cart:', error)
      throw error
    }
  }

  // Thanh toán
  async processPayment(paymentData) {
    try {
      if (!this.currentInvoiceId) {
        throw new Error('Giỏ hàng trống. Vui lòng thêm sản phẩm trước khi thanh toán.')
      }

      const result = await cartAPI.processPayment(this.currentInvoiceId, paymentData)
      
      // Reset giỏ hàng sau khi thanh toán thành công
      localStorage.removeItem('currentInvoiceId')
      this.currentInvoiceId = null
      
      return result
    } catch (error) {
      console.error('Error processing payment:', error)
      throw error
    }
  }

  // Lấy số lượng items trong giỏ hàng
  async getCartItemCount() {
    try {
      const cart = await this.getCart()
      return cart?.totalItems || 0
    } catch (error) {
      console.error('Error getting cart item count:', error)
      return 0
    }
  }

  // Set customer ID khi user đăng nhập
  setCustomerId(customerId) {
    this.customerId = customerId
    localStorage.setItem('customerId', customerId)
  }

  // Clear customer ID khi user đăng xuất
  clearCustomerId() {
    this.customerId = null
    localStorage.removeItem('customerId')
  }
}

// Export singleton instance
export const cartService = new CartService()

export default cartAPI
