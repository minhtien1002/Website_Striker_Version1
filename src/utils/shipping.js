/**
 * Nguồn DUY NHẤT cho quy tắc phí vận chuyển phía khách hàng.
 *
 * Trước đây quy tắc này bị chép ra 3 nơi với 3 giá trị khác nhau:
 *   - CheckoutPage / GioHang / ChiTietSanPham : miễn phí từ 500.000đ, phí 30.000đ
 *   - VoucherStep / PaymentStep              : miễn phí từ 1.000.000đ, phí 50.000đ
 *   - Backend (BanHangClientServiceImpl)     : luôn 30.000đ, không có miễn phí
 * Hệ quả: cùng một đơn hàng hiện ba con số tổng tiền khác nhau, và số khách được
 * báo ở bước cuối lệch với số hệ thống thực sự ghi nợ.
 *
 * Các hằng số ở đây PHẢI khớp với
 * {@code BanHangClientServiceImpl#tinhPhiVanChuyen} bên backend.
 */

/** Từ mức tiền hàng này trở lên thì được miễn phí vận chuyển. */
export const FREE_SHIPPING_FROM = 500000

/** Phí vận chuyển áp dụng khi đơn chưa đạt ngưỡng miễn phí. */
export const SHIPPING_FEE = 30000

/**
 * Phí vận chuyển của một đơn, tính trên tiền hàng (chưa trừ voucher).
 * @param {number} subtotal Tổng tiền hàng.
 * @returns {number} 0 nếu được miễn phí, ngược lại là SHIPPING_FEE.
 */
export const calcShippingFee = (subtotal) => {
  const value = Number(subtotal) || 0
  return value >= FREE_SHIPPING_FROM ? 0 : SHIPPING_FEE
}

/**
 * Số tiền còn thiếu để được miễn phí vận chuyển (0 nếu đã đạt).
 */
export const amountToFreeShipping = (subtotal) => {
  const value = Number(subtotal) || 0
  return Math.max(0, FREE_SHIPPING_FROM - value)
}

/**
 * Tổng tiền cuối cùng khách phải trả = tiền hàng + phí ship - giảm giá.
 * Không bao giờ âm.
 */
export const calcFinalTotal = (subtotal, discountAmount = 0) => {
  const goods = Number(subtotal) || 0
  const discount = Number(discountAmount) || 0
  return Math.max(0, goods + calcShippingFee(goods) - discount)
}
