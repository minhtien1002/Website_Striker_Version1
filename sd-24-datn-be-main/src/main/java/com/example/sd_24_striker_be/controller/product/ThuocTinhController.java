package com.example.sd_24_striker_be.controller.product;

import com.example.sd_24_striker_be.modal.product.ThuocTinh;
import com.example.sd_24_striker_be.service.product.ThuocTinhService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API chung cho các thuộc tính sản phẩm. Lớp con chỉ khai báo @RequestMapping.
 *
 * GET    ""                    phân trang: keyword, trangThai (active|inactive|rỗng), page, size
 * GET    /all                  tất cả (cho màn quản lý / bộ lọc)
 * GET    /active               chỉ đang hoạt động (cho combobox khi thêm/sửa sản phẩm)
 * GET    /{id}
 * GET    /{id}/su-dung         số sản phẩm đang dùng, để cảnh báo trước khi ngừng hoạt động
 * GET    /check-name-exists    name, excludeId
 * POST   ""                    { ten, moTa }
 * PUT    /{id}                 { ten, moTa }
 * PUT    /{id}/toggle-status
 * Không có DELETE: mọi thuộc tính chỉ quản lý bằng trạng thái.
 */
public abstract class ThuocTinhController<T extends ThuocTinh> {

    protected final ThuocTinhService<T> service;

    protected ThuocTinhController(ThuocTinhService<T> service) {
        this.service = service;
    }

    public static class ThuocTinhRequest {
        private String ten;
        private String moTa;

        public ThuocTinhRequest() {
        }

        public ThuocTinhRequest(String ten, String moTa) {
            this.ten = ten;
            this.moTa = moTa;
        }

        public String ten() {
            return ten;
        }

        public String moTa() {
            return moTa;
        }

        public String getTen() {
            return ten;
        }

        public void setTen(String ten) {
            this.ten = ten;
        }

        public String getMoTa() {
            return moTa;
        }

        public void setMoTa(String moTa) {
            this.moTa = moTa;
        }
    }

    @GetMapping
    public Page<T> search(@RequestParam(defaultValue = "") String keyword,
                          @RequestParam(defaultValue = "") String trangThai,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "1000") int size) {
        return service.search(keyword, trangThai, page, size);
    }

    @GetMapping("/all")
    public List<T> getAll() {
        return service.getAll();
    }

    @GetMapping("/active")
    public List<T> getActive() {
        return service.getActive();
    }

    @GetMapping("/{id}")
    public T getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @GetMapping("/{id}/su-dung")
    public Map<String, Object> suDung(@PathVariable Integer id) {
        return Map.of("soSanPham", service.countSanPhamSuDung(id));
    }

    @GetMapping("/check-name-exists")
    public Map<String, Object> checkNameExists(@RequestParam String name,
                                               @RequestParam(required = false) Integer excludeId) {
        return Map.of("exists", service.isTenTrung(name, excludeId));
    }

    @PostMapping
    public ResponseEntity<T> create(@RequestBody ThuocTinhRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req.ten(), req.moTa()));
    }

    @PutMapping("/{id}")
    public T update(@PathVariable Integer id, @RequestBody ThuocTinhRequest req) {
        return service.update(id, req.ten(), req.moTa());
    }

    @PutMapping("/{id}/toggle-status")
    public Map<String, Object> toggleStatus(@PathVariable Integer id) {
        T entity = service.toggleStatus(id);
        boolean ngung = Boolean.TRUE.equals(entity.getDeleted());
        return Map.of(
                "deleted", ngung,
                "message", "Đã chuyển " + service.tenLoai() + " \"" + entity.getTen() + "\" sang "
                        + (ngung ? "Ngừng hoạt động" : "Hoạt động"));
    }
}
