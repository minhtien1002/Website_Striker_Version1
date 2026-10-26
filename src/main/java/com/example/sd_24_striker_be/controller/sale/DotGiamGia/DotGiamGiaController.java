package com.example.sd_24_striker_be.controller.sale.DotGiamGia;


import com.example.sd_24_striker_be.dto.sale.request.DotGiamGia.DotGiamGiaDTO;
import com.example.sd_24_striker_be.dto.sale.response.DotGiamGia.DotGiamGiaDetailResponse;
import com.example.sd_24_striker_be.modal.sale.DotGiamGia;
import com.example.sd_24_striker_be.service.sale.impl.DotGiamGia.DotGiamGiaServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dot-giam-gia")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class DotGiamGiaController {
    private final DotGiamGiaServices dotGiamGiaServices;


    public DotGiamGiaController(DotGiamGiaServices dotGiamGiaServices) {
        this.dotGiamGiaServices = dotGiamGiaServices;
    }

    @GetMapping
    public List<DotGiamGia> getall() {
        return dotGiamGiaServices.getall();
    }

    @PostMapping("/add")
    public ResponseEntity<?> addDotGiamGia(@RequestBody DotGiamGiaDTO dto) {
        try {
            return ResponseEntity.ok(dotGiamGiaServices.addDotGiamGia(dto));
        } catch (RuntimeException e) {
            return toErrorResponse(e);
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(dotGiamGiaServices.getDotGiamGiaDetail(id));
        } catch (RuntimeException e) {
            return toErrorResponse(e);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody DotGiamGiaDTO dto) {
        try {
            return ResponseEntity.ok(dotGiamGiaServices.updateDotGiamGia(id, dto));
        } catch (RuntimeException e) {
            return toErrorResponse(e);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id, @RequestBody Map<String, Boolean> request) {
        try {
            Boolean newStatus = request.get("trangThai");
            return ResponseEntity.ok(dotGiamGiaServices.toggleStatus(id, newStatus));
        } catch (RuntimeException e) {
            return toErrorResponse(e);
        }
    }

    // Trước đây mọi lỗi (dữ liệu sai, không tìm thấy) đều thành 500 không kèm lý do
    private ResponseEntity<?> toErrorResponse(RuntimeException e) {
        if (e instanceof IllegalArgumentException) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        if (e.getMessage() != null && e.getMessage().startsWith("Không tìm thấy")) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        throw e;
    }
}
