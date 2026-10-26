package com.example.sd_24_striker_be.controller.product;

import com.example.sd_24_striker_be.modal.product.MonTheThao;
import com.example.sd_24_striker_be.service.product.MonTheThaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mon-the-thao")
@CrossOrigin(origins = "*")
public class MonTheThaoController {

    @Autowired
    private MonTheThaoService monTheThaoService;

    @GetMapping
    public ResponseEntity<Page<MonTheThao>> getAllWithPagination(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Page<MonTheThao> result = monTheThaoService.getAllWithPagination(keyword, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<List<MonTheThao>> getAll() {
        return ResponseEntity.ok(monTheThaoService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonTheThao> getById(@PathVariable Integer id) {
        Optional<MonTheThao> monTheThao = monTheThaoService.findById(id);
        return monTheThao.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody MonTheThao monTheThao) {
        try {
            if (monTheThao.getTenMonTheThao() == null || monTheThao.getTenMonTheThao().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên môn thể thao không được để trống");
            }

            // Chặn trùng tên ở backend (trước đây chỉ frontend kiểm tra, gọi API trực tiếp vẫn tạo trùng được)
            if (monTheThaoService.checkNameExists(monTheThao.getTenMonTheThao().trim(), null)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên đã tồn tại, vui lòng chọn tên khác");
            }

            MonTheThao saved = monTheThaoService.save(monTheThao);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi tạo môn thể thao: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody MonTheThao monTheThao) {
        try {
            if (monTheThao.getTenMonTheThao() == null || monTheThao.getTenMonTheThao().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên môn thể thao không được để trống");
            }

            if (monTheThaoService.checkNameExists(monTheThao.getTenMonTheThao().trim(), id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên đã tồn tại, vui lòng chọn tên khác");
            }

            MonTheThao updated = monTheThaoService.update(id, monTheThao);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi cập nhật môn thể thao: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            MonTheThao updated = monTheThaoService.toggleStatus(id);
            if (updated != null) {
                String statusText = updated.getDeleted() ? "vô hiệu hóa" : "kích hoạt";
                return ResponseEntity.ok().body("Đã " + statusText + " môn thể thao thành công");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi cập nhật trạng thái môn thể thao: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            boolean deleted = monTheThaoService.delete(id);
            if (deleted) {
                return ResponseEntity.ok().body("Đã xóa môn thể thao thành công");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi xóa môn thể thao: " + e.getMessage());
        }
    }

    @GetMapping("/check-name-exists")
    public ResponseEntity<?> checkNameExists(
            @RequestParam String name,
            @RequestParam(required = false) Integer excludeId) {
        try {
            boolean exists = monTheThaoService.checkNameExists(name.trim(), excludeId);
            return ResponseEntity.ok().body(new CheckExistsResponse(exists));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi kiểm tra tên môn thể thao: " + e.getMessage());
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
