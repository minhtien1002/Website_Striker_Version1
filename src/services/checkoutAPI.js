import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

// Create axios instance with default config
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
})

// Checkout API endpoints
export const checkoutAPI = {
  // Process payment and complete order
  processPayment: async (invoiceId, paymentData) => {
    try {
      console.log('Processing payment for invoice:', invoiceId)
      console.log('Payment data:', paymentData)
      
      const response = await apiClient.post(`/ban-hang-client/thanh-toan/${invoiceId}`, {
        tenKhachHang: paymentData.customerName,
        soDienThoai: paymentData.phone,
        email: paymentData.email,
        diaChi: paymentData.address,
        phuongThucThanhToan: paymentData.paymentMethod,
        phiVanChuyen: paymentData.shippingFee || 0,
        tienGiam: paymentData.discount || 0,
        ghiChu: paymentData.notes || '',
        voucherId: paymentData.voucherId || null
      })
      
      console.log('Payment processed successfully:', response.data)
      return response.data
    } catch (error) {
      console.error('Error processing payment:', error)
      throw new Error(error.response?.data || 'Không thể xử lý thanh toán')
    }
  },

  // Get order details after payment
  getOrderDetails: async (orderId) => {
    try {
      const response = await apiClient.get(`/hoa-don/${orderId}`)
      return response.data
    } catch (error) {
      console.error('Error fetching order details:', error)
      throw new Error(error.response?.data || 'Không thể lấy thông tin đơn hàng')
    }
  }
}

// Checkout Service - Business logic layer
export class CheckoutService {
  constructor() {
    // Don't cache currentInvoiceId, always get fresh from localStorage
  }

  // Get current invoice ID
  getCurrentInvoiceId() {
    return localStorage.getItem('currentInvoiceId')
  }

  // Process checkout and payment
  async processCheckout(checkoutData) {
    try {
      const currentInvoiceId = this.getCurrentInvoiceId()
      if (!currentInvoiceId) {
        throw new Error('Không tìm thấy giỏ hàng')
      }

      console.log('Processing checkout with data:', checkoutData)

      // Process payment
      const orderResult = await checkoutAPI.processPayment(currentInvoiceId, {
        customerName: checkoutData.customerInfo.name,
        phone: checkoutData.customerInfo.phone,
        email: checkoutData.customerInfo.email,
        address: checkoutData.customerInfo.address,
        paymentMethod: checkoutData.paymentMethod,
        shippingFee: checkoutData.shippingFee,
        discount: checkoutData.discount,
        notes: checkoutData.notes,
        voucherId: checkoutData.voucherId
      })

      // Clear cart after successful checkout
      localStorage.removeItem('currentInvoiceId')

      return orderResult
    } catch (error) {
      console.error('Error processing checkout:', error)
      throw error
    }
  }


  // Validate checkout data
  validateCheckoutData(checkoutData) {
    const errors = []

    // Validate customer info
    if (!checkoutData.customerInfo?.name?.trim()) {
      errors.push('Tên khách hàng không được để trống')
    }

    if (!checkoutData.customerInfo?.phone?.trim()) {
      errors.push('Số điện thoại không được để trống')
    }

    if (!checkoutData.customerInfo?.address?.trim()) {
      errors.push('Địa chỉ không được để trống')
    }

    // Validate payment method
    if (!checkoutData.paymentMethod) {
      errors.push('Phương thức thanh toán không được để trống')
    }

    return {
      isValid: errors.length === 0,
      errors
    }
  }
}

// Create singleton instance
export const checkoutService = new CheckoutService()

export default checkoutAPI
