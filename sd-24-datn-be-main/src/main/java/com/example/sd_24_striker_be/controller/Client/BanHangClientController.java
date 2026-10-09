package com.example.sd_24_striker_be.controller.Client;

import com.example.sd_24_striker_be.dto.sell.request.AddToCartRequest;
import com.example.sd_24_striker_be.dto.sell.request.UpdateCartItemRequest;
import com.example.sd_24_striker_be.dto.sell.request.ClientPaymentRequest;
import com.example.sd_24_striker_be.dto.sell.response.CartResponse;
import com.example.sd_24_striker_be.dto.bill.response.HoaDonDetailResponse;
import com.example.sd_24_striker_be.service.sale.Client.impl.BanHangClientService;
import com.example.sd_24_striker_be.modal.product.ChiTietSanPham;
import com.example.sd_24_striker_be.service.product.SanPhamService;
import com.example.sd_24_striker_be.service.product.ChiTietSanPhamService;
import com.example.sd_24_striker_be.dto.product.response.ChiTietSanPhamWithDiscountResponse;
import com.example.sd_24_striker_be.dto.product.response.SanPhamResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ban-hang-client")
@CrossOrigin(origins = "*")
public class BanHangClientController {

    @Autowired
    private BanHangClientService banHangClientService;
    
    @Autowired
    private SanPhamService sanPhamService;
    
    @Autowired
    private ChiTietSanPhamService chiTietSanPhamService;

    /** Sản phẩm đang hoạt động (trang khách hàng), có tìm kiếm. */
    @GetMapping("/san-pham")
    public Page<SanPhamResponse> getActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword) {
        return sanPhamService.searchActive(keyword, PageRequest.of(page, size, Sort.by("id").descending()));
    }

    /** Thông tin sản phẩm đang hoạt động. */
    @GetMapping("/san-pham/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable Integer productId) {
        return sanPhamService.findActiveById(productId)
                .<ResponseEntity<?>>map(sp -> ResponseEntity.ok(sanPhamService.getDetail(sp.getId())))
                .orElse(ResponseEntity.status(404).body("Sản phẩm không tồn tại hoặc đã ngừng kinh doanh"));
    }

    /** Biến thể còn bán được (còn hàng) của sản phẩm. */
    @GetMapping("/chi-tiet-san-pham/{productId}")
    public List<ChiTietSanPhamWithDiscountResponse> getActiveProductDetails(@PathVariable Integer productId) {
        return chiTietSanPhamService.getActiveByProductIdWithDiscountResponses(productId);
    }

    /**
     * Mọi biến thể đang hoạt động của sản phẩm kèm giá giảm, KỂ CẢ hết hàng - để trang chi tiết
     * hiển thị "Hết hàng" và ẩn màu không còn kích cỡ nào.
     */
    @GetMapping("/chi-tiet-san-pham-with-discount/{productId}")
    public List<ChiTietSanPhamWithDiscountResponse> getActiveProductDetailsWithDiscount(@PathVariable Integer productId) {
        return chiTietSanPhamService.getForClientProductPage(productId);
    }

    @GetMapping("/chi-tiet-san-pham")
    public Page<ChiTietSanPhamWithDiscountResponse> getAllActiveProductDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ChiTietSanPham> data = keyword != null && !keyword.trim().isEmpty()
                ? chiTietSanPhamService.getActiveProductDetailsForSalesWithKeyword(pageable, keyword.trim())
                : chiTietSanPhamService.getActiveProductDetailsForSales(pageable);
        return data.map(chiTietSanPhamService::toDiscountResponse);
    }

    /** Tách giỏ: giữ lại sản phẩm không được tích chọn trong một giỏ mới trước khi thanh toán. */
    @PostMapping("/gio-hang/{hoaDonId}/tach")
    public ResponseEntity<?> tachGioHang(@PathVariable Integer hoaDonId, @RequestBody java.util.Map<String, List<Integer>> body) {
        try {
            return ResponseEntity.ok(banHangClientService.tachGioHang(hoaDonId, body.get("cartItemIds")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Gộp lại giỏ đã tách (khi khách hủy thanh toán VNPay). */
    @PostMapping("/gio-hang/{tuHoaDonId}/gop-vao/{vaoHoaDonId}")
    public ResponseEntity<?> gopGioHang(@PathVariable Integer tuHoaDonId, @PathVariable Integer vaoHoaDonId) {
        try {
            return ResponseEntity.ok(banHangClientService.gopGioHang(tuHoaDonId, vaoHoaDonId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Phí vận chuyển theo địa chỉ giao hàng ("số nhà, phường/xã, quận/huyện, tỉnh/thành"). */
    @GetMapping("/phi-van-chuyen")
    public ResponseEntity<?> phiVanChuyen(@RequestParam String diaChi, @RequestParam(defaultValue = "1") int soLuong) {
        try {
            return ResponseEntity.ok(phiVanChuyenService.tinhPhi(diaChi, soLuong));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Autowired
    private com.example.sd_24_striker_be.service.sell.PhiVanChuyenService phiVanChuyenService;

    @GetMapping("/chi-tiet-san-pham-with-discount")
    public List<ChiTietSanPhamWithDiscountResponse> getAllActiveProductDetailsWithDiscount() {
        return chiTietSanPhamService.getActiveProductDetailsWithDiscountPrices();
    }
    /**
     * Create pending invoice for customer
     */
    @PostMapping("/hoa-don-cho")
    public ResponseEntity<?> taoHoaDonCho(@RequestParam(required = false) Integer khachHangId) {
        try {
            HoaDonDetailResponse response = banHangClientService.taoHoaDonCho(khachHangId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Get pending invoices by customer
     */
    @GetMapping("/hoa-don-cho/khach-hang/{khachHangId}")
    public ResponseEntity<?> getPendingInvoicesByCustomer(@PathVariable Integer khachHangId) {
        try {
            List<HoaDonDetailResponse> responses = banHangClientService.getPendingInvoicesByCustomer(khachHangId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Add product to cart
     */
    @PostMapping("/gio-hang/{hoaDonId}/them-san-pham")
    public ResponseEntity<?> themSanPhamVaoGioHang(
            @PathVariable Integer hoaDonId,
            @RequestBody AddToCartRequest request) {
        try {
            CartResponse response = banHangClientService.themSanPhamVaoGioHang(hoaDonId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Get cart by invoice ID
     */
    @GetMapping("/gio-hang/{hoaDonId}")
    public ResponseEntity<?> layGioHang(@PathVariable Integer hoaDonId) {
        try {
            CartResponse response = banHangClientService.layGioHang(hoaDonId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Update cart item quantity
     */
    @PutMapping("/gio-hang/{hoaDonId}/san-pham/{cartItemId}")
    public ResponseEntity<?> capNhatSoLuongSanPham(
            @PathVariable Integer hoaDonId,
            @PathVariable Integer cartItemId,
            @RequestBody UpdateCartItemRequest request) {
        try {
            CartResponse response = banHangClientService.capNhatSoLuongSanPham(hoaDonId, cartItemId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Remove product from cart
     */
    @DeleteMapping("/gio-hang/{hoaDonId}/san-pham/{cartItemId}")
    public ResponseEntity<?> xoaSanPhamKhoiGioHang(
            @PathVariable Integer hoaDonId,
            @PathVariable Integer cartItemId) {
        try {
            CartResponse response = banHangClientService.xoaSanPhamKhoiGioHang(hoaDonId, cartItemId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Clear entire cart
     */
    @DeleteMapping("/gio-hang/{hoaDonId}/xoa-tat-ca")
    public ResponseEntity<?> xoaToanBoGioHang(@PathVariable Integer hoaDonId) {
        try {
            banHangClientService.xoaToanBoGioHang(hoaDonId);
            return ResponseEntity.ok("Đã xóa toàn bộ giỏ hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Process payment and complete order
     */
    @PostMapping("/thanh-toan/{hoaDonId}")
    public ResponseEntity<?> thanhToan(
            @PathVariable Integer hoaDonId,
            @RequestBody ClientPaymentRequest request) {
        try {
            HoaDonDetailResponse result = banHangClientService.thanhToan(hoaDonId, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get order details after payment
     */
    @GetMapping("/don-hang/{hoaDonId}")
    public ResponseEntity<?> getDonHang(@PathVariable Integer hoaDonId) {
        try {
            HoaDonDetailResponse result = banHangClientService.getOrderDetails(hoaDonId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Search order by order code (for guest users)
     */
    @GetMapping("/tra-cuu-don-hang")
    public ResponseEntity<?> searchOrderByCode(@RequestParam String ma) {
        try {
            HoaDonDetailResponse result = banHangClientService.searchOrderByCode(ma);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
