package com.example.sd_24_striker_be.controller.product;

import com.example.sd_24_striker_be.modal.product.CongNgheVai;
import com.example.sd_24_striker_be.service.product.CongNgheVaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cong-nghe-vai")
@CrossOrigin(origins = "*")
public class CongNgheVaiController {

    @Autowired
    private CongNgheVaiService congNgheVaiService;

    @GetMapping
    public ResponseEntity<Page<CongNgheVai>> getAllWithPagination(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Page<CongNgheVai> result = congNgheVaiService.getAllWithPagination(keyword, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CongNgheVai>> getAll() {
        return ResponseEntity.ok(congNgheVaiService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CongNgheVai> getById(@PathVariable Integer id) {
        Optional<CongNgheVai> congNgheVai = congNgheVaiService.findById(id);
        return congNgheVai.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CongNgheVai congNgheVai) {
        try {
            if (congNgheVai.getTenCongNghe() == null || congNgheVai.getTenCongNghe().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên công nghệ vải không được để trống");
            }

            // Chặn trùng tên ở backend (trước đây chỉ frontend kiểm tra, gọi API trực tiếp vẫn tạo trùng được)
            if (congNgheVaiService.checkNameExists(congNgheVai.getTenCongNghe().trim(), null)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên đã tồn tại, vui lòng chọn tên khác");
            }

            CongNgheVai saved = congNgheVaiService.save(congNgheVai);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi tạo công nghệ vải: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody CongNgheVai congNgheVai) {
        try {
            if (congNgheVai.getTenCongNghe() == null || congNgheVai.getTenCongNghe().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên công nghệ vải không được để trống");
            }

            if (congNgheVaiService.checkNameExists(congNgheVai.getTenCongNghe().trim(), id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên đã tồn tại, vui lòng chọn tên khác");
            }

            CongNgheVai updated = congNgheVaiService.update(id, congNgheVai);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi cập nhật công nghệ vải: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            CongNgheVai updated = congNgheVaiService.toggleStatus(id);
            if (updated != null) {
                String statusText = updated.getDeleted() ? "vô hiệu hóa" : "kích hoạt";
                return ResponseEntity.ok().body("Đã " + statusText + " công nghệ vải thành công");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi cập nhật trạng thái công nghệ vải: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            boolean deleted = congNgheVaiService.delete(id);
            if (deleted) {
                return ResponseEntity.ok().body("Đã xóa công nghệ vải thành công");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi xóa công nghệ vải: " + e.getMessage());
        }
    }

    @GetMapping("/check-name-exists")
    public ResponseEntity<?> checkNameExists(
            @RequestParam String name,
            @RequestParam(required = false) Integer excludeId) {
        try {
            boolean exists = congNgheVaiService.checkNameExists(name.trim(), excludeId);
            return ResponseEntity.ok().body(new CheckExistsResponse(exists));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi kiểm tra tên công nghệ vải: " + e.getMessage());
        }
    }

    public static class CheckExistsResponse {
        private boolean exists;

        public CheckExistsResponse(boolean exists) {
            this.exists = exists;
        }

        public boolean isExists() {
            return exists;
        }

        public void setExists(boolean exists) {
            this.exists = exists;
        }
    }
}
