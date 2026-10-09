import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'
const BACKEND_ORIGIN = 'http://localhost:8080'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

// Lỗi backend được ném ra để trang tự hiển thị trạng thái lỗi/trống (không dùng dữ liệu giả).
const request = async (label, fn) => {
  try {
    const response = await fn()
    return response.data
  } catch (error) {
    console.error(`[productAPI] ${label} failed:`, error)
    throw error
  }
}

/** Ảnh lưu đường dẫn tương đối của backend (/uploads/...) thì thêm host backend. */
export const imageUrl = (url) => {
  if (!url) return '/placeholder-product.svg'
  if (/^(https?:|data:|blob:)/.test(url)) return url
  if (url.startsWith('/uploads')) return `${BACKEND_ORIGIN}${url}`
  return url
}

export const productAPI = {
  // Sản phẩm đang hoạt động (kèm biến thể đang hoạt động)
  getProducts: (params = {}) => request('getProducts', () =>
    apiClient.get('/ban-hang-client/san-pham', {
      params: { page: params.page || 0, size: params.size || 10, keyword: params.keyword || '' }
    })),

  // Biến thể còn hàng của sản phẩm (kèm giá giảm)
  getProductDetails: (productId) => request('getProductDetails', () =>
    apiClient.get(`/ban-hang-client/chi-tiet-san-pham/${productId}`)),

  // Mọi biến thể đang hoạt động của sản phẩm, kể cả hết hàng (kèm giá giảm)
  getProductDetailsWithDiscount: (productId) => request('getProductDetailsWithDiscount', () =>
    apiClient.get(`/ban-hang-client/chi-tiet-san-pham-with-discount/${productId}`)),

  // Thông tin chung của sản phẩm (thương hiệu, thể loại, chất liệu, mô tả, ảnh)
  getProductInfo: (productId) => request('getProductInfo', () =>
    apiClient.get(`/ban-hang-client/san-pham/${productId}`)),

  getAllProductDetails: (params = {}) => request('getAllProductDetails', () =>
    apiClient.get('/ban-hang-client/chi-tiet-san-pham', {
      params: { page: params.page || 0, size: params.size || 10, keyword: params.keyword || '' }
    })),

  getAllProductDetailsWithDiscount: () => request('getAllProductDetailsWithDiscount', () =>
    apiClient.get('/ban-hang-client/chi-tiet-san-pham-with-discount')),

  getProductsByBrandIds: (brandIds, page = 0, size = 10) => request('getProductsByBrandIds', () =>
    apiClient.get('/san-pham/by-thuong-hieu', { params: { brandIds: brandIds.join(','), page, size } })),

  getProductsByCategory: (categoryName, page = 0, size = 10) => request('getProductsByCategory', () =>
    apiClient.get('/san-pham/by-danh-muc', { params: { categoryName, page, size } })),

  // Thể loại đang hoạt động
  getAllCategories: () => request('getAllCategories', () => apiClient.get('/danh-muc/active')),

  getAllProducts: (params = {}) => request('getAllProducts', () =>
    apiClient.get('/ban-hang-client/san-pham', { params: { page: params.page || 0, size: params.size || 1000 } }))
}

const vnd = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0)

export const mapBackendToFrontend = {
  /** SanPhamResponse (backend) -> dữ liệu thẻ sản phẩm. */
  product: (p) => {
    if (!p) return null
    const variants = (p.chiTietSanPhams || []).filter((d) => d.trangThai !== false && !d.deleted)
    const inStock = variants.filter((d) => (d.soLuongTonKho || 0) > 0)
    const totalStock = variants.reduce((s, d) => s + (d.soLuongTonKho || 0), 0)
    const prices = variants.map((d) => Number(d.giaBan) || 0).filter((x) => x > 0)
    const minPrice = prices.length ? Math.min(...prices) : 0
    const discounted = variants.filter((d) => d.giaGiamGia > 0)
    const minDiscountPrice = discounted.length ? Math.min(...discounted.map((d) => d.giaGiamGia)) : null
    const campaigns = [...new Set(variants.filter((d) => d.tenDotGiamGia).map((d) => d.tenDotGiamGia))]
    const image = imageUrl(p.urlAnhDaiDien || variants.find((v) => v.urlAnhSanPham)?.urlAnhSanPham)
    const status = totalStock === 0 ? 'Hết hàng' : 'Đang kinh doanh'

    return {
      id: p.id,
      ma: p.ma || '',
      tenSanPham: p.tenSanPham,
      title: p.tenSanPham,
      name: p.tenSanPham,
      brand: p.tenThuongHieu || 'Chưa xác định',
      brandId: p.idThuongHieu,
      danhMuc: p.tenDanhMuc || 'Chưa phân loại',
      categoryId: p.idDanhMuc,
      chatLieu: p.tenChatLieu,
      urlAnhDaiDien: image,
      imageUrl: image,
      giaBanThapNhat: minPrice,
      price: minPrice,
      formattedPrice: vnd(minPrice),
      giaGiamGia: minDiscountPrice,
      discountPrice: minDiscountPrice,
      formattedDiscountPrice: minDiscountPrice ? vnd(minDiscountPrice) : null,
      tenDotGiamGia: campaigns.join(' + ') || null,
      campaignName: campaigns.join(' + ') || null,
      hasDiscount: discounted.length > 0,
      soLuongTonKho: totalStock,
      stock: totalStock,
      totalStock,
      soLuongDaBan: p.tongSoLuongDaBan || 0,
      activeVariants: variants.length,
      tenTrangThai: status,
      status,
      productStatus: status,
      ngayTao: p.ngayTao,
      createdAt: p.ngayTao ? new Date(p.ngayTao) : new Date(),
      moTa: p.moTa || '',
      description: p.moTa || 'Chưa có mô tả sản phẩm',
      deleted: false,
      isAdded: false,
      isFavorite: false,
      // Chỉ liệt kê màu / size còn hàng (màu hết mọi size thì không hiện)
      colors: [...new Set(inStock.map((v) => v.tenMauSac).filter(Boolean))],
      colorDetails: [...new Map(inStock.filter((v) => v.tenMauSac).map((v) => [v.tenMauSac, { code: v.tenMauSac, name: v.tenMauSac }])).values()],
      sizes: [...new Set(inStock.map((v) => v.tenKichCo).filter(Boolean))],
      materials: p.tenChatLieu ? [p.tenChatLieu] : []
    }
  },

  /** ChiTietSanPhamWithDiscountResponse (backend) -> dữ liệu biến thể. */
  productDetail: (d) => {
    if (!d) return null
    const hasStock = (d.soLuongTonKho || 0) > 0
    const img = imageUrl(d.urlAnhSanPham)
    return {
      id: d.id,
      ma: d.ma || '',
      productId: d.idSanPham,
      maSanPham: d.maSanPham,
      tenSanPham: d.tenSanPham,
      name: d.tenSanPham,
      brand: d.tenThuongHieu || 'Chưa xác định',
      danhMuc: d.tenDanhMuc || 'Chưa phân loại',
      categoryName: d.tenDanhMuc || 'Chưa phân loại',
      chatLieu: d.tenChatLieu,
      material: d.tenChatLieu,
      giaBan: d.giaBan || 0,
      price: d.giaBan || 0,
      formattedPrice: vnd(d.giaBan),
      giaGiamGia: d.giaGiamGia || null,
      discountPrice: d.giaGiamGia || null,
      formattedDiscountPrice: d.giaGiamGia ? vnd(d.giaGiamGia) : null,
      tenDotGiamGia: d.tenDotGiamGia || null,
      campaignName: d.tenDotGiamGia || null,
      hasDiscount: !!d.hasDiscount,
      soLuongTonKho: d.soLuongTonKho || 0,
      stock: d.soLuongTonKho || 0,
      mauSac: d.tenMauSac,
      color: d.tenMauSac,
      colorId: d.idMauSac,
      kichCo: d.tenKichCo,
      size: d.tenKichCo,
      sizeId: d.idKichCo,
      anhSanPham: img,
      imageUrl: img,
      hasStock,
      isActive: true,
      status: hasStock ? 'Đang kinh doanh' : 'Hết hàng'
    }
  }
}

export default productAPI
