package com.example.sd_24_striker_be.service.sale.Client.impl;

import com.example.sd_24_striker_be.dto.sell.request.AddToCartRequest;
import com.example.sd_24_striker_be.dto.sell.request.ClientPaymentRequest;
import com.example.sd_24_striker_be.dto.sell.request.UpdateCartItemRequest;
import com.example.sd_24_striker_be.dto.sell.request.PaymentRequest;
import com.example.sd_24_striker_be.dto.sell.response.CartResponse;
import com.example.sd_24_striker_be.dto.bill.response.HoaDonDetailResponse;

import java.util.List;

public interface BanHangClientService {

    /**
     * Create a pending invoice for customer
     */
    HoaDonDetailResponse taoHoaDonCho(Integer khachHangId);

    /**
     * Get pending invoices by customer
     */
    List<HoaDonDetailResponse> getPendingInvoicesByCustomer(Integer khachHangId);

    /**
     * Add product to cart (database-based)
     */
    CartResponse themSanPhamVaoGioHang(Integer hoaDonId, AddToCartRequest request);

    /**
     * Get cart by invoice ID
     */
    CartResponse layGioHang(Integer hoaDonId);

    /**
     * Update cart item quantity
     */
    CartResponse capNhatSoLuongSanPham(Integer hoaDonId, Integer cartItemId, UpdateCartItemRequest request);

    /**
     * Remove product from cart
     */
    CartResponse xoaSanPhamKhoiGioHang(Integer hoaDonId, Integer cartItemId);

    HoaDonDetailResponse thanhToan(Integer hoaDonId, ClientPaymentRequest request);

    /**
     * Khách tích chọn một phần giỏ hàng để thanh toán: các sản phẩm KHÔNG chọn được chuyển sang
     * một hóa đơn chờ (giỏ hàng) mới để giữ lại. Trả về id hóa đơn sẽ thanh toán và id giỏ còn lại (có thể null).
     */
    java.util.Map<String, Object> tachGioHang(Integer hoaDonId, List<Integer> cartItemIdsThanhToan);

    /** Gộp giỏ {@code tuHoaDonId} vào giỏ {@code vaoHoaDonId} (dùng khi thanh toán VNPay bị hủy sau khi đã tách giỏ). */
    CartResponse gopGioHang(Integer tuHoaDonId, Integer vaoHoaDonId);

    /**
     * Update customer information in invoice (for pre-saving before VNPay)
     */
    void updateCustomerInfo(Integer hoaDonId, ClientPaymentRequest request);

    /**
     * Clear entire cart
     */
    CartResponse xoaToanBoGioHang(Integer hoaDonId);

    /**
     * Process payment and complete order
     */
    HoaDonDetailResponse thanhToan(Integer hoaDonId, PaymentRequest paymentRequest);

    /**
     * Get order details after payment
     */
    HoaDonDetailResponse getOrderDetails(Integer hoaDonId);

    /**
     * Search order by order code (for guest users)
     */
    HoaDonDetailResponse searchOrderByCode(String orderCode);

    /**
     * Khách hàng tự hủy đơn hàng.
     * Chỉ cho phép hủy khi đơn hàng đang ở trạng thái Chờ xác nhận (1) hoặc Đã xác nhận (2).
     * Chặn hủy nếu đơn đang ở trạng thái Chờ vận chuyển (3), Đang vận chuyển (4) hoặc Đã hoàn thành (5).
     */
    void huyDonHang(Integer hoaDonId, String lyDoHuy);
}