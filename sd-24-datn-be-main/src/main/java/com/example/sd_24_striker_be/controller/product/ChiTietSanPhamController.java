package com.example.sd_24_striker_be.controller.product;

import com.example.sd_24_striker_be.dto.product.request.ChiTietSanPhamUpdateRequest;
import com.example.sd_24_striker_be.dto.product.response.ChiTietSanPhamResponse;
import com.example.sd_24_striker_be.dto.product.response.ChiTietSanPhamWithDiscountResponse;
import com.example.sd_24_striker_be.modal.product.ChiTietSanPham;
import com.example.sd_24_striker_be.service.product.ChiTietSanPhamService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Chi tiết sản phẩm (biến thể). Không có xóa: chỉ đổi trạng thái. Chỉ có giá bán, không có giá nhập. */
@RestController
@RequestMapping("/api/chi-tiet-san-pham")
public class ChiTietSanPhamController {
    private final ChiTietSanPhamService chiTietSanPhamService;

    public ChiTietSanPhamController(ChiTietSanPhamService chiTietSanPhamService) {
        this.chiTietSanPhamService = chiTietSanPhamService;
    }

    @GetMapping("/product/{productId}")
    public Page<ChiTietSanPhamResponse> getByProductId(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer sizeId,
            @RequestParam(required = false) Integer colorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minSellingPrice,
            @RequestParam(required = false) Double maxSellingPrice) {
        return chiTietSanPhamService.getAllWithFilters(productId, search, sizeId, colorId, status,
                minSellingPrice, maxSellingPrice, PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    @GetMapping
    public Page<ChiTietSanPhamResponse> getAllWithFilters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer sizeId,
            @RequestParam(required = false) Integer colorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minSellingPrice,
            @RequestParam(required = false) Double maxSellingPrice) {
        return chiTietSanPhamService.getAllWithFilters(null, search, sizeId, colorId, status,
                minSellingPrice, maxSellingPrice, PageRequest.of(page, size, Sort.by("id").descending()));
    }

    @GetMapping("/{id}")
    public ChiTietSanPhamResponse getById(@PathVariable Integer id) {
        return chiTietSanPhamService.getResponseById(id);
    }

    @PutMapping("/{id}")
    public ChiTietSanPhamResponse update(@PathVariable Integer id, @Valid @RequestBody ChiTietSanPhamUpdateRequest request) {
        return ChiTietSanPhamService.toResponse(chiTietSanPhamService.updateChiTietSanPham(id, request));
    }

    @PutMapping("/{id}/toggle-status")
    public Map<String, Object> toggleStatus(@PathVariable Integer id) {
        ChiTietSanPham ct = chiTietSanPhamService.toggleStatus(id);
        boolean ngung = Boolean.TRUE.equals(ct.getDeleted());
        return Map.of("deleted", ngung,
                "message", "Đã chuyển biến thể " + ct.getMa() + " sang " + (ngung ? "Ngừng hoạt động" : "Hoạt động"));
    }

    // ===== Bán tại quầy =====

    @GetMapping("/sales/product/{productId}")
    public List<ChiTietSanPhamWithDiscountResponse> getActiveByProductIdForSales(@PathVariable Integer productId) {
        return chiTietSanPhamService.getActiveByProductIdWithDiscountResponses(productId);
    }

    @GetMapping("/sales")
    public Page<ChiTietSanPhamWithDiscountResponse> getActiveProductDetailsForSales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ChiTietSanPham> data = keyword != null && !keyword.trim().isEmpty()
                ? chiTietSanPhamService.getActiveProductDetailsForSalesWithKeyword(pageable, keyword.trim())
                : chiTietSanPhamService.getActiveProductDetailsForSales(pageable);
        return data.map(chiTietSanPhamService::toDiscountResponse);
    }

    @GetMapping("/sales/with-discounts")
    public List<ChiTietSanPhamWithDiscountResponse> getActiveProductDetailsWithDiscountPrices() {
        return chiTietSanPhamService.getActiveProductDetailsWithDiscountPrices();
    }

    @GetMapping("/sales/with-discounts/product/{productId}")
    public List<ChiTietSanPhamWithDiscountResponse> getActiveByProductIdWithDiscountPrices(@PathVariable Integer productId) {
        return chiTietSanPhamService.getActiveByProductIdWithDiscountResponses(productId);
    }
}
