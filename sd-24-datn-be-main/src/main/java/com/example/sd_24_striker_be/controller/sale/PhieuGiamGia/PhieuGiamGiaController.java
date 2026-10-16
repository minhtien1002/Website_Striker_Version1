package com.example.sd_24_striker_be.controller.sale.PhieuGiamGia;


import com.example.sd_24_striker_be.dto.sale.request.PhieuGiamGia.PhieuGiamGiaDTO;
import com.example.sd_24_striker_be.dto.sale.response.PhieuGiamGia.PhieuGiamGiaDetailResponse;
import com.example.sd_24_striker_be.modal.sale.PhieuGiamGia;
import com.example.sd_24_striker_be.service.sale.impl.PhieuGiamGia.PhieuGiamGiaServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/phieu-giam-gia")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000"})
public class PhieuGiamGiaController {

    private final PhieuGiamGiaServices phieuGiamGiaService;

    public PhieuGiamGiaController(PhieuGiamGiaServices phieuGiamGiaService) {
        this.phieuGiamGiaService = phieuGiamGiaService;
    }

    // ==================== GET ALL ====================
   @GetMapping
    public List<PhieuGiamGia> getall() {
       return phieuGiamGiaService.getall();
   }
    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody PhieuGiamGiaDTO dto) {
        try {
            PhieuGiamGia saved = phieuGiamGiaService.add(dto);
            return ResponseEntity.ok(saved); // ✅ trả về phiếu giảm giá vừa tạo
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // ❌ lỗi custom
        }
    }

    // Lấy phiếu giảm giá theo ID
    @GetMapping("/{id}")
    public ResponseEntity<PhieuGiamGia> getById(@PathVariable Long id) {
        try {
            PhieuGiamGia pgg = phieuGiamGiaService.getById(id);
            return ResponseEntity.ok(pgg);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Phiếu giảm giá không được sửa / xóa: chỉ đổi trạng thái.
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(phieuGiamGiaService.toggleStatus(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Phiếu dùng được cho đơn, phiếu giảm nhiều nhất đứng đầu (bán tại quầy tự chọn phiếu tốt nhất). */
    @GetMapping("/kha-dung")
    public List<java.util.Map<String, Object>> getPhieuKhaDung(@RequestParam java.math.BigDecimal tongTien,
                                                               @RequestParam(required = false) Integer khachHangId) {
        return phieuGiamGiaService.getPhieuKhaDung(tongTien, khachHangId);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<PhieuGiamGiaDetailResponse> getDetail(@PathVariable Integer id) {
        PhieuGiamGiaDetailResponse dto = phieuGiamGiaService.getDetail(id);
        return ResponseEntity.ok(dto);
    }

    // Lấy phiếu giảm giá còn hoạt động cho khách hàng
    @GetMapping("/active/customer/{customerId}")
    public ResponseEntity<List<PhieuGiamGia>> getActiveVouchersForCustomer(@PathVariable Integer customerId) {
        List<PhieuGiamGia> vouchers = phieuGiamGiaService.getActiveVouchersForCustomer(customerId);
        return ResponseEntity.ok(vouchers);
    }

    // Lấy phiếu giảm giá công khai còn hoạt động
    @GetMapping("/active/public")
    public ResponseEntity<List<PhieuGiamGia>> getActivePublicVouchers() {
        List<PhieuGiamGia> vouchers = phieuGiamGiaService.getActivePublicVouchers();
        return ResponseEntity.ok(vouchers);
    }

    // Áp dụng phiếu giảm giá cho hóa đơn
    @PostMapping("/apply/{voucherId}/invoice/{invoiceId}")
    public ResponseEntity<?> applyVoucherToInvoice(
            @PathVariable Integer voucherId,
            @PathVariable Integer invoiceId) {
        try {
            phieuGiamGiaService.applyVoucherToInvoice(invoiceId, voucherId);
            return ResponseEntity.ok("Áp dụng phiếu giảm giá thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xóa phiếu giảm giá khỏi hóa đơn
    @DeleteMapping("/remove/invoice/{invoiceId}")
    public ResponseEntity<?> removeVoucherFromInvoice(@PathVariable Integer invoiceId) {
        try {
            phieuGiamGiaService.removeVoucherFromInvoice(invoiceId);
            return ResponseEntity.ok("Xóa phiếu giảm giá thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Kiểm tra phiếu giảm giá có thể áp dụng cho khách hàng không
    @GetMapping("/can-apply/{voucherId}/customer/{customerId}")
    public ResponseEntity<Boolean> canApplyVoucherForCustomer(
            @PathVariable Integer voucherId,
            @PathVariable Integer customerId) {
        boolean canApply = phieuGiamGiaService.canApplyVoucherForCustomer(voucherId, customerId);
        return ResponseEntity.ok(canApply);
    }

    // Kiểm tra phiếu giảm giá có thể áp dụng (không cần khách hàng - cho phiếu công khai)
    @GetMapping("/can-apply/{voucherId}")
    public ResponseEntity<Boolean> canApplyVoucher(@PathVariable Integer voucherId) {
        boolean canApply = phieuGiamGiaService.canApplyVoucherForCustomer(voucherId, null);
        return ResponseEntity.ok(canApply);
    }

}
