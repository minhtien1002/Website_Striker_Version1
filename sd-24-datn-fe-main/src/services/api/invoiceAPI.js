import axios from 'axios'

// Create axios instance for invoice API
const invoiceAPI = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor to add auth token
invoiceAPI.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor for error handling
invoiceAPI.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('Invoice API Error:', error)
    return Promise.reject(error)
  }
)

export default {
  // Hoa Don (Invoice) APIs for sales counter
  createInvoice: () => invoiceAPI.post('/ban-hang/hoa-don-cho'),
  
  getInvoiceById: (id) => invoiceAPI.get(`/hoa-don/${id}/detail`),
  
  updateInvoice: (id, data) => invoiceAPI.put(`/hoa-don/${id}`, data),
  
  deleteInvoice: (id) => invoiceAPI.delete(`/ban-hang/hoa-don-cho/${id}`),
  
  getAllInvoices: (params = {}) => {
    const {
      keyword = '',
      page = 0,
      size = 10,
      sortBy = 'id',
      sortDirection = 'desc'
    } = params
    
    return invoiceAPI.get('/hoa-don', {
      params: { keyword, page, size, sortBy, sortDirection }
    })
  },

  // Get pending invoices (for sales counter)
  getPendingInvoices: () => invoiceAPI.get('/ban-hang/hoa-don-cho'),

  // Update pending invoice customer information
  updatePendingInvoiceCustomer: (invoiceId, customerData) => 
    invoiceAPI.put(`/ban-hang/hoa-don-cho/${invoiceId}/khach-hang`, customerData),

  // Hoa Don Chi Tiet (Invoice Detail) APIs
  addProductToInvoice: (invoiceId, data) => 
    invoiceAPI.post(`/hoa-don/${invoiceId}/chi-tiet`, data),
  
  updateInvoiceDetail: (invoiceId, detailId, data) => 
    invoiceAPI.put(`/hoa-don/${invoiceId}/chi-tiet/${detailId}`, data),
  
  removeProductFromInvoice: (invoiceId, detailId) => 
    invoiceAPI.delete(`/hoa-don/${invoiceId}/chi-tiet/${detailId}`),
  
  getInvoiceDetails: (invoiceId) => 
    invoiceAPI.get(`/hoa-don/${invoiceId}/chi-tiet`),

  // Payment APIs
  processPayment: (paymentData) => 
    invoiceAPI.post('/ban-hang/thanh-toan', paymentData),

  // Customer APIs for invoice
  searchCustomers: (keyword) => 
    invoiceAPI.get('/khach-hang/search', { params: { keyword } }),
  
  createQuickCustomer: (data) => 
    invoiceAPI.post('/khach-hang/quick', data),

  // Voucher APIs
  applyVoucher: (invoiceId, voucherCode) => 
    invoiceAPI.post(`/hoa-don/${invoiceId}/voucher`, { code: voucherCode }),
  
  removeVoucher: (invoiceId) => 
    invoiceAPI.delete(`/hoa-don/${invoiceId}/voucher`),

  // Invoice status updates
  updateInvoiceStatus: (invoiceId, status) => 
    invoiceAPI.put(`/hoa-don/${invoiceId}/status`, { status }),

  // Generate invoice code
  generateInvoiceCode: () => 
    invoiceAPI.get('/hoa-don/generate-code'),

  // Print invoice
  printInvoice: (invoiceId) => 
    invoiceAPI.get(`/hoa-don/${invoiceId}/print`, { responseType: 'blob' }),

  // VNPay payment APIs
  createVNPayPayment: (paymentData) => 
    invoiceAPI.post('/payment/vnpay/create', paymentData),
  
  processCombinedPayment: (paymentData) => 
    invoiceAPI.post('/payment/vnpay/process-combined', paymentData)
}
