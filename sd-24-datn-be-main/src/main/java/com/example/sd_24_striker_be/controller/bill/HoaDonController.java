package com.example.sd_24_striker_be.controller.bill;

import java.math.BigDecimal;
import com.example.sd_24_striker_be.dto.bill.response.HoaDonDTOResponse;
import com.example.sd_24_striker_be.dto.bill.response.HoaDonDetailResponse;
import com.example.sd_24_striker_be.service.bill.HoaDonService;
import com.example.sd_24_striker_be.service.bill.InHoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/hoa-don")
public class HoaDonController {
    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private InHoaDonService inHoaDonService;

    @GetMapping("/home")
    public ResponseEntity<?> getAllHoaDon(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long minAmount,
            @RequestParam(required = false) Long maxAmount,
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate,
            @RequestParam(required = false) Short trangThai,
            @RequestParam(required = false) String loaiDon,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        if (loaiDon != null) {
            if (loaiDon.equalsIgnoreCase("Tại quầy") || loaiDon.equalsIgnoreCase("tai quay") || loaiDon.equalsIgnoreCase("TAI_QUAY")) {
                loaiDon = "Tại quầy";
            } else if (loaiDon.equalsIgnoreCase("Online")) {
                loaiDon = "Online";
            } else {
                loaiDon = null;
            }
        }
        if (minAmount != null && maxAmount != null && minAmount > maxAmount) {
            return ResponseEntity.badRequest().body("minAmount phải nhỏ hơn hoặc bằng maxAmount");
        }

        Timestamp startTimestamp = startDate != null ? new Timestamp(startDate) : null;
        Timestamp endTimestamp = endDate != null ? new Timestamp(endDate) : null;

        if (startTimestamp != null && endTimestamp != null && startTimestamp.after(endTimestamp)) {
            return ResponseEntity.badRequest().body("startDate phải trước hoặc bằng endDate");
        }

        String validSortBy = mapSortField(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), validSortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<HoaDonDTOResponse> response = hoaDonService.getHoaDonAndFilters(keyword, minAmount, maxAmount, startTimestamp, endTimestamp, trangThai, loaiDon, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<HoaDonDetailResponse> getHoaDonDetail(@PathVariable Integer id) {
        HoaDonDetailResponse response = hoaDonService.getHoaDonDetail(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status-counts")
    public ResponseEntity<Map<String, Long>> getStatusCounts() {
        try {
            Map<String, Long> statusCounts = hoaDonService.getStatusCounts();
            return ResponseEntity.ok(statusCounts);
        } catch (Exception e) {
            Map<String, Long> error = new HashMap<>();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/completed-revenue")
    public ResponseEntity<BigDecimal> getCompletedRevenue() {
        return ResponseEntity.ok(hoaDonService.getCompletedRevenue());
    }

    @GetMapping("/price-range")
    public ResponseEntity<Map<String, Long>> getPriceRange() {
        try {
            Map<String, Long> priceRange = hoaDonService.getPriceRange();
            return ResponseEntity.ok(priceRange);
        } catch (Exception e) {
            Map<String, Long> error = new HashMap<>();
            error.put("minPrice", 0L);
            error.put("maxPrice", 0L);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}/update-status")
    public ResponseEntity<?> updateHoaDonStatus(
            @PathVariable Integer id,
            @RequestParam Short trangThai,
            @RequestParam(required = false) Integer idNhanVien,
            @RequestParam(required = false) String ghiChu) {
        try {
            HoaDonDTOResponse response = hoaDonService.updateHoaDonStatusWithNote(id, trangThai, idNhanVien, ghiChu);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/huy")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer idNhanVien,
            @RequestParam String lyDo) {
        try {
            HoaDonDTOResponse response = hoaDonService.cancelOrder(id, idNhanVien, null, lyDo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/giao-that-bai")
    public ResponseEntity<?> markDeliveryFailed(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer idNhanVien,
            @RequestParam String lyDo) {
        try {
            return ResponseEntity.ok(hoaDonService.markDeliveryFailed(id, idNhanVien, lyDo));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/hoan-kho")
    public ResponseEntity<?> restockFailedDelivery(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer idNhanVien,
            @RequestParam(required = false) String ghiChu) {
        try {
            return ResponseEntity.ok(hoaDonService.restockFailedDelivery(id, idNhanVien, ghiChu));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/xac-nhan-hoan-hang")
    public ResponseEntity<?> confirmReturn(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer idNhanVien,
            @RequestParam(required = false) String ghiChu) {
        try {
            HoaDonDTOResponse response = hoaDonService.confirmReturn(id, idNhanVien, ghiChu);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/tu-choi-hoan-hang")
    public ResponseEntity<?> rejectReturn(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer idNhanVien,
            @RequestParam String lyDo) {
        try {
            HoaDonDTOResponse response = hoaDonService.rejectReturn(id, idNhanVien, lyDo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/mua-lai")
    public ResponseEntity<?> muaLaiDonHang(
            @PathVariable Integer id,
            @RequestParam Integer idKhachHang) {
        try {
            hoaDonService.muaLaiDonHang(id, idKhachHang);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã thêm các sản phẩm vào giỏ hàng thành công!");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long minAmount,
            @RequestParam(required = false) Long maxAmount,
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate,
            @RequestParam(required = false) Short trangThai,
            @RequestParam(required = false) String loaiDon) {
        if (loaiDon != null) {
            if (loaiDon.equalsIgnoreCase("Tại quầy") || loaiDon.equalsIgnoreCase("tai quay") || loaiDon.equalsIgnoreCase("TAI_QUAY")) {
                loaiDon = "Tại quầy";
            } else if (loaiDon.equalsIgnoreCase("Online")) {
                loaiDon = "Online";
            } else {
                loaiDon = null;
            }
        }
        Timestamp startTimestamp = startDate != null ? new Timestamp(startDate) : null;
        Timestamp endTimestamp = endDate != null ? new Timestamp(endDate) : null;

        byte[] excelBytes = hoaDonService.exportHoaDonExcel(keyword, minAmount, maxAmount, startTimestamp, endTimestamp, trangThai, loaiDon);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        String fileName = "danh-sach-don-hang-" + new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date()) + ".xlsx";
        headers.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/QR-by-ma/{ma}")
    public ResponseEntity<HoaDonDTOResponse> getHoaDonByMa(@PathVariable("ma") String maHD) {
        try {
            return ResponseEntity.ok(hoaDonService.getHoaDonByMa(maHD));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> inHoaDon(@PathVariable Integer id) throws Exception {
        HoaDonDetailResponse hoaDon = hoaDonService.getHoaDonDetail(id);
        byte[] pdfBytes = inHoaDonService.generateHoaDonPdf(hoaDon);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "hoa_don_" + hoaDon.getMaHoaDon() + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // =========================================================================
    // API KHÁCH HÀNG GỬI YÊU CẦU TRẢ HÀNG
    // =========================================================================
    @PutMapping("/{id}/yeu-cau-hoan-hang")
    public ResponseEntity<?> yeuCauHoanHang(
            @PathVariable("id") Integer id,
            @RequestParam("idKhachHangActor") Long idKhachHangActor,
            @RequestParam("lyDo") String lyDo) {
        try {
            // Gọi hàm xử lý logic từ Service
            hoaDonService.yeuCauHoanHang(id, idKhachHangActor, lyDo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Gửi yêu cầu hoàn hàng thành công");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Bắt các lỗi logic từ Service (VD: Đơn hàng chưa giao thành công)
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            // Bắt TẤT CẢ các lỗi hệ thống còn lại (NullPointerException, DataIntegrityViolationException...)
            // IN RA CONSOLE CỦA INTELLIJ ĐỂ DEBUG
            System.err.println("=== LỖI 500 KHI GỬI YÊU CẦU HOÀN HÀNG ===");
            e.printStackTrace(); // In chi tiết lỗi ra màn hình đen của IntelliJ

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            // Trả về thông báo lỗi cho Frontend thay vì để nó hiện "No message available"
            errorResponse.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private String mapSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "id";
        }

        switch (sortBy.toLowerCase()) {
            case "tongtiensaugiam":
            case "tong_tien_sau_giam":
            case "tongtienthanhtoan":
            case "tong_tien_thanh_toan":
                return "tongTienSauGiam";
            case "tongtien":
            case "tong_tien":
                return "tongTien";
            case "tiensanpham":
            case "tien_san_pham":
                return "tienSanPham";
            case "ngaytao":
            case "ngay_tao":
                return "ngayTao";
            case "ngaythanhtoan":
            case "ngay_thanh_toan":
                return "ngayThanhToan";
            case "trangthai":
            case "trang_thai":
                return "trangThai";
            case "ma":
                return "ma";
            case "tenkhachhang":
            case "ten_khach_hang":
                return "tenKhachHang";
            case "sodienthoaikhachhang":
            case "so_dien_thoai_khach_hang":
                return "soDienThoaiKhachHang";
            case "loaidon":
            case "loai_don":
                return "loaiDon";
            case "phivanchuyen":
            case "phi_van_chuyen":
                return "phiVanChuyen";
            case "createdat":
            case "created_at":
                return "createdAt";
            case "updatedat":
            case "updated_at":
                return "updatedAt";
            case "id":
                return "id";
            default:
                return "id";
        }
    }
}