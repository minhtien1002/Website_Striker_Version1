import { brandApi, categoryApi, materialApi, sizeApi, colorApi } from '@/services/api/productAPI.js'

/**
 * Cấu hình 5 thuộc tính sản phẩm. Danh mục đã đổi tên hiển thị thành "Thể loại";
 * môn thể thao đã bỏ; công nghệ vải đã gộp vào chất liệu. Màu sắc chỉ nhập tên, không nhập mã màu.
 */
export const THUOC_TINH = {
  thuongHieu: { label: 'thương hiệu', route: '/thuoc-tinh/thuong-hieu', api: brandApi, tenField: 'tenThuongHieu', maField: 'maThuongHieu', example: 'adidas = Adidas' },
  theLoai: { label: 'thể loại', route: '/thuoc-tinh/danh-muc', api: categoryApi, tenField: 'tenDanhMuc', maField: 'maDanhMuc', example: 'áo bóng đá = Áo Bóng Đá' },
  chatLieu: { label: 'chất liệu', route: '/thuoc-tinh/chat-lieu', api: materialApi, tenField: 'tenChatLieu', maField: 'maChatLieu', example: 'cotton 100% = Cotton 100%' },
  kichCo: { label: 'kích cỡ', route: '/thuoc-tinh/kich-co', api: sizeApi, tenField: 'tenKichCo', maField: 'maKichCo', example: 'm = M', maxLength: 50 },
  mauSac: { label: 'màu sắc', route: '/thuoc-tinh/mau-sac', api: colorApi, tenField: 'tenMauSac', maField: 'maMauSac', example: 'đỏ = Đỏ' }
}
