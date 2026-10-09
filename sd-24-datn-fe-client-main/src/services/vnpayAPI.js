import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

// Create axios instance for VNPay
const vnpayClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
})

// VNPay API endpoints for client
export const vnpayAPI = {
  // Create VNPay payment URL
  createPayment: async (invoiceId, paymentData) => {
    try {
      console.log('Creating VNPay payment for invoice:', invoiceId)
      console.log('Payment data:', paymentData)
      
      const response = await vnpayClient.post(`/client-payment/vnpay/create/${invoiceId}`, {
        tenKhachHang: paymentData.customerName,
        soDienThoai: paymentData.phone,
        email: paymentData.email,
        diaChi: paymentData.address,
        phuongThucThanhToan: 'VNPAY',
        phiVanChuyen: paymentData.shippingFee || 0,
        tienGiam: paymentData.discount || 0,
        ghiChu: paymentData.notes || '',
        voucherId: paymentData.voucherId || null
      })
      
      console.log('VNPay payment URL created:', response.data)
      return response.data
    } catch (error) {
      console.error('Error creating VNPay payment:', error)
      throw new Error(error.response?.data || 'Không thể tạo thanh toán VNPay')
    }
  },

  // Get payment status
  getPaymentStatus: async (invoiceId) => {
    try {
      const response = await vnpayClient.get(`/client-payment/vnpay/status/${invoiceId}`)
      return response.data
    } catch (error) {
      console.error('Error getting payment status:', error)
      throw new Error(error.response?.data || 'Không thể lấy trạng thái thanh toán')
    }
  }
}

// VNPay Service - Business logic layer
export class VNPayService {
  constructor() {
    // Don't cache currentInvoiceId, always get fresh from localStorage
  }

  // Get current invoice ID
  get currentInvoiceId() {
    return localStorage.getItem('currentInvoiceId')
  }

  // Create VNPay payment and redirect
  async createVNPayPayment(paymentData) {
    try {
      const invoiceId = this.currentInvoiceId
      if (!invoiceId) {
        console.error('No currentInvoiceId found in localStorage')
        throw new Error('Không tìm thấy giỏ hàng')
      }

      console.log('VNPay using invoice ID:', invoiceId)

      console.log('Creating VNPay payment with data:', paymentData)
      console.log('VoucherId from paymentData:', paymentData.voucherId, 'Type:', typeof paymentData.voucherId)

      const vnpayPayload = {
        customerName: paymentData.customerInfo.name,
        phone: paymentData.customerInfo.phone,
        email: paymentData.customerInfo.email,
        address: paymentData.customerInfo.address,
        shippingFee: paymentData.shippingFee,
        discount: paymentData.discount,
        notes: paymentData.notes,
        voucherId: paymentData.voucherId
      }
      
      console.log('VNPay payload being sent:', vnpayPayload)

      // Create VNPay payment URL
      const result = await vnpayAPI.createPayment(invoiceId, vnpayPayload)

      if (result.paymentUrl) {
        console.log('Redirecting to VNPay:', result.paymentUrl)
        // Redirect to VNPay payment page
        window.location.href = result.paymentUrl
        return result
      } else {
        throw new Error('Không nhận được URL thanh toán từ VNPay')
      }
    } catch (error) {
      console.error('Error creating VNPay payment:', error)
      throw error
    }
  }

  // Check payment status
  async checkPaymentStatus(invoiceId) {
    try {
      const result = await vnpayAPI.getPaymentStatus(invoiceId)
      return result
    } catch (error) {
      console.error('Error checking payment status:', error)
      throw error
    }
  }

  // Get current invoice ID
  getCurrentInvoiceId() {
    return this.currentInvoiceId
  }

  // Handle VNPay return (called from URL parameters)
  handleVNPayReturn(urlParams) {
    const status = urlParams.get('status')
    const invoiceId = urlParams.get('invoiceId')
    const payment = urlParams.get('payment')

    console.log('VNPay return:', { status, invoiceId, payment })

    if (payment === 'vnpay') {
      if (status === 'success') {
        // Clear invoice ID after successful payment
        localStorage.removeItem('currentInvoiceId')
        
        return {
          success: true,
          message: 'Thanh toán VNPay thành công!',
          invoiceId: invoiceId
        }
      } else if (status === 'failed') {
        return {
          success: false,
          message: 'Thanh toán VNPay thất bại. Vui lòng thử lại.',
          invoiceId: invoiceId
        }
      } else if (status === 'error') {
        return {
          success: false,
          message: 'Có lỗi xảy ra trong quá trình thanh toán VNPay.',
          invoiceId: invoiceId
        }
      }
    }

    return null
  }

  // Validate VNPay payment data
  validateVNPayData(paymentData) {
    const errors = []

    // Validate customer info
    if (!paymentData.customerInfo?.name?.trim()) {
      errors.push('Tên khách hàng không được để trống')
    }

    if (!paymentData.customerInfo?.phone?.trim()) {
      errors.push('Số điện thoại không được để trống')
    }

    if (!paymentData.customerInfo?.address?.trim()) {
      errors.push('Địa chỉ không được để trống')
    }

    return {
      isValid: errors.length === 0,
      errors
    }
  }
}

// Create singleton instance
export const vnpayService = new VNPayService()

export default vnpayAPI
