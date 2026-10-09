package com.example.sd_24_striker_be.controller.product;

import com.example.sd_24_striker_be.dto.product.request.ChiTietSanPhamCreateRequest;
import com.example.sd_24_striker_be.dto.product.request.ProductWithVariantsCreateRequest;
import com.example.sd_24_striker_be.dto.product.request.SanPhamCreateRequest;
import com.example.sd_24_striker_be.dto.product.request.SanPhamUpdateRequest;
import com.example.sd_24_striker_be.dto.product.response.ChiTietSanPhamResponse;
import com.example.sd_24_striker_be.dto.product.response.SanPhamResponse;
import com.example.sd_24_striker_be.modal.product.SanPham;
import com.example.sd_24_striker_be.service.product.ChiTietSanPhamService;
import com.example.sd_24_striker_be.service.product.SanPhamService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Sản phẩm. Không có xóa: chỉ đổi trạng thái Hoạt động / Ngừng hoạt động. */
@RestController
@RequestMapping("/api/san-pham")
public class SanPhamController {
    private final SanPhamService sanPhamService;
    private final ChiTietSanPhamService chiTietSanPhamService;

    public SanPhamController(SanPhamService sanPhamService, ChiTietSanPhamService chiTietSanPhamService) {
        this.sanPhamService = sanPhamService;
        this.chiTietSanPhamService = chiTietSanPhamService;
    }

    /** Danh sách quản lý, có lọc. */
    @GetMapping
    public Page<SanPhamResponse> search(@RequestParam(defaultValue = "") String keyword,
                                        @RequestParam(required = false) Integer idThuongHieu,
                                        @RequestParam(required = false) Integer idDanhMuc,
                                        @RequestParam(required = false) Integer idChatLieu,
                                        @RequestParam(defaultValue = "") String trangThai,
                                        @RequestParam(required = false) java.math.BigDecimal minGia,
                                        @RequestParam(required = false) java.math.BigDecimal maxGia,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return sanPhamService.search(keyword, idThuongHieu, idDanhMuc, idChatLieu, trangThai, minGia, maxGia,
                PageRequest.of(page, size, Sort.by("id").descending()));
    }

    /** Sản phẩm đang hoạt động (trang khách hàng). */
    @GetMapping("/active")
    public Page<SanPhamResponse> active(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return sanPhamService.getActivePaged(PageRequest.of(page, size, Sort.by("id").descending()));
    }

    @GetMapping("/{id}")
    public SanPhamResponse getById(@PathVariable Integer id) {
        return sanPhamService.getDetail(id);
    }

    @GetMapping("/check-name-exists")
    public Map<String, Object> checkNameExists(@RequestParam String name, @RequestParam(required = false) Integer excludeId) {
        return Map.of("exists", sanPhamService.isTenTrung(name, excludeId));
    }

    @PostMapping
    public SanPhamResponse create(@RequestBody @Valid SanPhamCreateRequest request) {
        return sanPhamService.getDetail(sanPhamService.createSanPham(request).getId());
    }

    @PostMapping("/with-variants")
    public SanPhamResponse createWithVariants(@RequestBody @Valid ProductWithVariantsCreateRequest request) {
        return sanPhamService.getDetail(sanPhamService.createProductWithVariants(request).getId());
    }

    @PutMapping("/{id}")
    public SanPhamResponse update(@PathVariable Integer id, @RequestBody @Valid SanPhamUpdateRequest request) {
        return sanPhamService.getDetail(sanPhamService.updateSanPham(id, request).getId());
    }

    @PostMapping("/{productId}/variants")
    public List<ChiTietSanPhamResponse> addVariants(@PathVariable Integer productId,
                                                    @RequestBody @Valid List<ChiTietSanPhamCreateRequest> variants) {
        SanPham product = sanPhamService.getEntity(productId);
        return chiTietSanPhamService.createProductVariants(product, variants).stream()
                .map(ChiTietSanPhamService::toResponse).toList();
    }

    @PutMapping("/{id}/toggle-status")
    public Map<String, Object> toggleStatus(@PathVariable Integer id) {
        SanPham sp = sanPhamService.toggleProductStatus(id);
        boolean ngung = Boolean.TRUE.equals(sp.getDeleted());
        return Map.of("deleted", ngung,
                "message", "Đã chuyển sản phẩm \"" + sp.getTenSanPham() + "\" sang " + (ngung ? "Ngừng hoạt động" : "Hoạt động"));
    }

    @GetMapping("/by-thuong-hieu")
    public Page<SanPhamResponse> getProductsByBrandIds(@RequestParam List<Integer> brandIds,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return sanPhamService.getProductsByBrandIds(brandIds, PageRequest.of(page, size));
    }

    @GetMapping("/by-danh-muc")
    public Page<SanPhamResponse> getProductsByCategoryName(@RequestParam String categoryName,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return sanPhamService.getProductsByCategoryName(categoryName, PageRequest.of(page, size));
    }

    @GetMapping("/available-for-discount")
    public Page<SanPhamResponse> getProductsAvailableForDiscount(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        return sanPhamService.getProductsAvailableForDiscount(PageRequest.of(page, size, Sort.by("id").descending()));
    }
}
