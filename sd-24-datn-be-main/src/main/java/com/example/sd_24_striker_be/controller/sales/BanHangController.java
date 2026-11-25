package com.example.sd_24_striker_be.controller.sales;

import com.example.sd_24_striker_be.dto.account.request.KhachHang.KhachHangQuickCreateRequest;
import com.example.sd_24_striker_be.dto.account.response.KhachHang.KhachHangDTOResponse;
import com.example.sd_24_striker_be.dto.bill.request.UpdateCustomerRequest;
import com.example.sd_24_striker_be.dto.sell.request.PaymentRequest;
import com.example.sd_24_striker_be.dto.sell.response.PaymentResponse;
import com.example.sd_24_striker_be.modal.bill.HoaDon;
import com.example.sd_24_striker_be.dto.product.response.ChiTietSanPhamWithDiscountResponse;
import com.example.sd_24_striker_be.modal.product.ChiTietSanPham;
import com.example.sd_24_striker_be.service.account.Client.impl.KhachHang.KhachHangServices;
import com.example.sd_24_striker_be.service.bill.HoaDonService;
import com.example.sd_24_striker_be.service.product.ChiTietSanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ban-hang")
@CrossOrigin(origins = "*")
public class BanHangController {

    private final ChiTietSanPhamService chiTietSanPhamService;
    private final KhachHangServices khachHangServices;
    private final HoaDonService hoaDonService;

    // Đã xóa SanPhamService vì không sử dụng trong controller này để tránh warning
    @Autowired
    public BanHangController(ChiTietSanPhamService chiTietSanPhamService,
                             KhachHangServices khachHangServices,
                             HoaDonService hoaDonService) {
        this.chiTietSanPhamService = chiTietSanPhamService;
        this.khachHangServices = khachHangServices;
        this.hoaDonService = hoaDonService;
    }

    /** Biến thể còn bán được (đang hoạt động, còn hàng) kèm giá sau đợt giảm giá. */
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

    @GetMapping("/chi-tiet-san-pham/{productId}")
    public List<ChiTietSanPhamWithDiscountResponse> getActiveProductDetails(@PathVariable Integer productId) {
        return chiTietSanPhamService.getActiveByProductIdWithDiscountResponses(productId);
    }

    /**
     * Get active customers for sales counter with phone numbers from account table
     */
    @GetMapping("/khach-hang")
    public ResponseEntity<Page<KhachHangDTOResponse>> getActiveCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "") String keyword) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<KhachHangDTOResponse> activeCustomers = khachHangServices.getActiveCustomersForSales(pageable, keyword.trim());
        return ResponseEntity.ok(activeCustomers);
    }

    /**
     * Quick create customer with only name and phone number for sales counter
     */
    @PostMapping("/khach-hang/quick-create")
    public ResponseEntity<?> quickCreateCustomer(@RequestBody KhachHangQuickCreateRequest request) {
        try {
            return ResponseEntity.ok(khachHangServices.quickCreateCustomer(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get all pending invoices for sales counter (status = 1)
     */
    @GetMapping("/hoa-don-cho")
    public ResponseEntity<List<HoaDon>> getPendingInvoices() {
        List<HoaDon> pendingInvoices = hoaDonService.getPendingInvoicesForSales();
        return ResponseEntity.ok(pendingInvoices);
    }

    /**
     * Create new pending invoice for sales counter
     */
    /** Tạo hóa đơn chờ mới - không giới hạn số lượng; mọi hóa đơn chờ được tự xóa lúc 0h. */
    @PostMapping("/hoa-don-cho")
    public ResponseEntity<HoaDon> createPendingInvoice(@RequestParam(required = false) Integer idNhanVien) {
        HoaDon newInvoice = hoaDonService.createPendingInvoice(idNhanVien);
        return ResponseEntity.ok(newInvoice);
    }

    /**
     * Update pending invoice customer information
     */
    @PutMapping("/hoa-don-cho/{id}/khach-hang")
    public ResponseEntity<HoaDon> updatePendingInvoiceCustomer(
            @PathVariable Integer id,
            @RequestBody UpdateCustomerRequest request) {
        HoaDon updatedInvoice = hoaDonService.updatePendingInvoiceCustomer(id, request);
        return ResponseEntity.ok(updatedInvoice);
    }

    /**
     * Delete pending invoice (soft delete)
     */
    @DeleteMapping("/hoa-don-cho/{id}")
    public ResponseEntity<Void> deletePendingInvoice(@PathVariable Integer id) {
        hoaDonService.deletePendingInvoice(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Process payment for cash transactions
     */
    @PostMapping("/thanh-toan")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest paymentRequest) {
        PaymentResponse response = hoaDonService.processPayment(paymentRequest);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    // =========================================================================
    // API HỦY ĐƠN HÀNG (Dành cho nhân viên / quản trị viên)
    // =========================================================================
    /**
     * Hủy đơn hàng. Áp dụng nghiệp vụ: Không cho phép hủy nếu đơn đang ở trạng thái
     * Chờ vận chuyển (3) hoặc Đang vận chuyển (4).
     */
    @PutMapping("/huy-don/{id}")
    public ResponseEntity<?> cancelOrder(
            @PathVariable("id") Integer id,
            @RequestParam(value = "lyDo", required = false) String lyDo) {
        try {
            // Gọi hàm xử lý ở Service
            hoaDonService.huyDonHang(id, lyDo);
            return ResponseEntity.ok("Hủy đơn hàng thành công");
        } catch (RuntimeException e) {
            // Trả về lỗi 400 Bad Request kèm thông báo (VD: "Không thể hủy đơn hàng khi đang ở trạng thái Chờ vận chuyển...")
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}