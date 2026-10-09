import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const productAPI = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

productAPI.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

/** Lấy câu thông báo lỗi từ response của backend (chuỗi hoặc {message}/{error}). */
export const errorMessage = (error, fallback = 'Có lỗi xảy ra, vui lòng thử lại') => {
  const data = error?.response?.data
  if (typeof data === 'string' && data.trim()) return data
  if (data?.message) return data.message
  if (data?.error) return data.error
  return fallback
}

/**
 * API chung cho các thuộc tính sản phẩm (thương hiệu, thể loại, chất liệu, kích cỡ, màu sắc).
 * Không có xóa - chỉ đổi trạng thái.
 */
export const thuocTinhApi = (path) => ({
  search: (params = {}) => productAPI.get(`/${path}`, { params: { size: 1000, ...params } }),
  all: () => productAPI.get(`/${path}/all`),
  active: () => productAPI.get(`/${path}/active`),
  getById: (id) => productAPI.get(`/${path}/${id}`),
  usage: (id) => productAPI.get(`/${path}/${id}/su-dung`),
  checkName: (name, excludeId = null) =>
    productAPI.get(`/${path}/check-name-exists`, { params: { name, ...(excludeId ? { excludeId } : {}) } }),
  create: (data) => productAPI.post(`/${path}`, data),
  update: (id, data) => productAPI.put(`/${path}/${id}`, data),
  toggleStatus: (id) => productAPI.put(`/${path}/${id}/toggle-status`)
})

export const brandApi = thuocTinhApi('thuong-hieu')
export const categoryApi = thuocTinhApi('danh-muc')
export const materialApi = thuocTinhApi('chat-lieu')
export const sizeApi = thuocTinhApi('kich-co')
export const colorApi = thuocTinhApi('mau-sac')

const clean = (obj) => Object.fromEntries(Object.entries(obj).filter(([, v]) => v !== '' && v !== null && v !== undefined))

export const productService = {
  // ----- Sản phẩm -----
  searchProducts: (params = {}) => productAPI.get('/san-pham', { params: clean(params) }),
  getProductById: (id) => productAPI.get(`/san-pham/${id}`),
  checkProductName: (name, excludeId = null) =>
    productAPI.get('/san-pham/check-name-exists', { params: { name, ...(excludeId ? { excludeId } : {}) } }),
  createProductWithVariants: (data) => productAPI.post('/san-pham/with-variants', data),
  updateProduct: (id, data) => productAPI.put(`/san-pham/${id}`, data),
  toggleProductStatus: (id) => productAPI.put(`/san-pham/${id}/toggle-status`),
  addVariantsToProduct: (productId, variants) => productAPI.post(`/san-pham/${productId}/variants`, variants),
  getProductsAvailableForDiscount: (params = {}) => productAPI.get('/san-pham/available-for-discount', { params }),

  // ----- Chi tiết sản phẩm (biến thể) -----
  getProductDetailsPaged: (productId, params = {}) =>
    productAPI.get(`/chi-tiet-san-pham/product/${productId}`, { params: clean(params) }),
  getAllProductDetails: (params = {}) => productAPI.get('/chi-tiet-san-pham', { params: clean(params) }),
  getProductDetailById: (id) => productAPI.get(`/chi-tiet-san-pham/${id}`),
  updateProductDetail: (id, data) => productAPI.put(`/chi-tiet-san-pham/${id}`, data),
  toggleProductDetailStatus: (id) => productAPI.put(`/chi-tiet-san-pham/${id}/toggle-status`),

  // ----- Ảnh -----
  // Không tự set 'Content-Type: multipart/form-data' (thiếu boundary) - để trình duyệt tự sinh.
  uploadImage: async (file) => {
    const formData = new FormData()
    formData.append('file', file)
    try {
      const response = await productAPI.post('/upload/image', formData, { headers: { 'Content-Type': undefined } })
      return response.data.url
    } catch (error) {
      const response = await productAPI.post('/simple-upload/image', formData, { headers: { 'Content-Type': undefined } })
      return response.data.url
    }
  },

  // ----- Bán tại quầy -----
  getSalesProductDetails: (params = {}) => productAPI.get('/chi-tiet-san-pham/sales', { params: clean(params) }),
  getActiveCustomersForSales: (params = {}) =>
    productAPI.get('/ban-hang/khach-hang', { params: { page: 0, size: 10, sortBy: 'id', sortDir: 'desc', keyword: '', ...params } }),
  quickCreateCustomer: (data) => productAPI.post('/ban-hang/khach-hang/quick-create', data),
  getPendingInvoices: () => productAPI.get('/ban-hang/hoa-don-cho'),
  createPendingInvoice: (idNhanVien) =>
    productAPI.post('/ban-hang/hoa-don-cho', null, { params: idNhanVien ? { idNhanVien } : {} }),
  deletePendingInvoice: (invoiceId) => productAPI.delete(`/ban-hang/hoa-don-cho/${invoiceId}`),
  updatePendingInvoiceCustomer: (invoiceId, data) => productAPI.put(`/ban-hang/hoa-don-cho/${invoiceId}/khach-hang`, data),
  processPayment: (data) => productAPI.post('/ban-hang/thanh-toan', data),
  getAvailableVouchers: (tongTien, khachHangId) =>
    productAPI.get('/phieu-giam-gia/kha-dung', { params: clean({ tongTien, khachHangId }) })
}

/** URL ảnh hiển thị được: ảnh tương đối của backend thì thêm host. */
export const imageUrl = (url) => {
  if (!url) return ''
  if (/^(https?:|data:|blob:)/.test(url)) return url
  return `http://localhost:8080${url.startsWith('/') ? '' : '/'}${url}`
}

export const formatMoney = (v) =>
  v === null || v === undefined || v === '' ? '—' : `${Number(v).toLocaleString('vi-VN')} đ`

export const formatDate = (v, withTime = false) => {
  if (!v) return '—'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return '—'
  return withTime
    ? d.toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
    : d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

export default productService
