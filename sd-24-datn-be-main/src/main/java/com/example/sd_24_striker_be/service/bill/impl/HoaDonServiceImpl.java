package com.example.sd_24_striker_be.service.bill.impl;

import com.example.sd_24_striker_be.common.sale.VoucherCalculator;
import com.example.sd_24_striker_be.common.bill.HoaDonDetailMapper;
import com.example.sd_24_striker_be.common.bill.HoaDonMapper;
import com.example.sd_24_striker_be.common.bill.HoaDonStatus;
import com.example.sd_24_striker_be.dto.bill.response.HoaDonDTOResponse;
import com.example.sd_24_striker_be.dto.bill.response.HoaDonDetailResponse;
import com.example.sd_24_striker_be.dto.bill.request.UpdateCustomerRequest;
import com.example.sd_24_striker_be.dto.sell.request.PaymentRequest;
import com.example.sd_24_striker_be.dto.sell.response.PaymentResponse;
import com.example.sd_24_striker_be.exception.ResourceNotFoundException;
import com.example.sd_24_striker_be.modal.bill.HoaDon;
import com.example.sd_24_striker_be.modal.bill.HoaDonChiTiet;
import com.example.sd_24_striker_be.modal.bill.LichSuHoaDon;
import com.example.sd_24_striker_be.modal.product.ChiTietSanPham;
import com.example.sd_24_striker_be.modal.sell.HinhThucThanhToan;
import com.example.sd_24_striker_be.modal.sell.PhuongThucThanhToan;
import com.example.sd_24_striker_be.modal.sale.PhieuGiamGia;
import com.example.sd_24_striker_be.repository.account.KhachHang.KhachHangRepository;
import com.example.sd_24_striker_be.repository.account.NhanVien.NhanVienRepository;
import com.example.sd_24_striker_be.repository.bill.HoaDonRepository;
import com.example.sd_24_striker_be.repository.bill.HoaDonChiTietRepository;
import com.example.sd_24_striker_be.repository.bill.LichSuHoaDonRepository;
import com.example.sd_24_striker_be.repository.product.ChiTietSanPhamRepository;
import com.example.sd_24_striker_be.repository.sell.HinhThucThanhToanRepository;
import com.example.sd_24_striker_be.repository.sell.PhuongThucThanhToanRepository;
import com.example.sd_24_striker_be.repository.sale.PhieuGiamGia.PhieuGiamGiaRepository;
import com.example.sd_24_striker_be.repository.sell.GioHangRepository;
import com.example.sd_24_striker_be.repository.sell.GioHangChiTietRepository;
import com.example.sd_24_striker_be.modal.sell.GioHang;
import com.example.sd_24_striker_be.modal.sell.GioHangChiTiet;
import com.example.sd_24_striker_be.service.bill.HoaDonService;
import com.example.sd_24_striker_be.service.sell.GioHangService;
import com.example.sd_24_striker_be.service.StockUpdateService;
import com.example.sd_24_striker_be.service.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HoaDonServiceImpl implements HoaDonService {

    private static final Logger logger = LoggerFactory.getLogger(HoaDonServiceImpl.class);

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private com.example.sd_24_striker_be.service.product.ChiTietSanPhamService chiTietSanPhamService;

    @Autowired
    @org.springframework.context.annotation.Lazy // PhieuGiamGiaServices cũng phụ thuộc chuỗi HoaDon -> tránh vòng phụ thuộc
    private com.example.sd_24_striker_be.service.sale.impl.PhieuGiamGia.PhieuGiamGiaServices phieuGiamGiaServices;

    @Autowired
    private HoaDonDetailMapper hoaDonDetailMapper;

    @Autowired
    private HoaDonMapper hoaDonMapper;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private HinhThucThanhToanRepository hinhThucThanhToanRepository;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired
    private GioHangRepository gioHangRepository;

    @Autowired
    private GioHangChiTietRepository gioHangChiTietRepository;

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private StockUpdateService stockUpdateService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private com.example.sd_24_striker_be.service.bill.ExcelExportService excelExportService;

    @Override
    public Page<HoaDonDTOResponse> getHoaDonAndFilters(String keyword, Long minAmount, Long maxAmount, Timestamp startDate, Timestamp endDate, Short trangThai, String loaiDon, Pageable pageable) {
        return hoaDonRepository.getAllHoaDon(keyword, minAmount, maxAmount, startDate, endDate, trangThai, false, loaiDon, pageable);
    }

    @Override
    public HoaDonDetailResponse getHoaDonDetail(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findHoaDonDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại hoặc đã bị xóa"));

        List<HoaDonDetailResponse.SanPhamChiTietInfo> sanPhamChiTietInfos = hoaDon.getChiTietHoaDon()
                .stream()
                .map(hoaDonDetailMapper::mapToSanPhamChiTietInfo)
                .collect(Collectors.toList());

        List<HoaDonDetailResponse.ThanhToanInfo> thanhToanInfos = hoaDon.getHinhThucThanhToan()
                .stream()
                .map(hoaDonDetailMapper::mapToThanhToanInfo)
                .collect(Collectors.toList());

        List<HoaDonDetailResponse.LichSuHoaDonInfo> lichSuHoaDonInfos = hoaDon.getLichSuHoaDon()
                .stream()
                .map(hoaDonDetailMapper::mapToLichSuHoaDonInfo)
                .collect(Collectors.toList());

        return new HoaDonDetailResponse.Builder()
                .withHoaDonInfo(hoaDon, hoaDon.getIdPhieuGiamGia())
                .withNhanVienInfo(hoaDon.getIdNhanVien())
                .withThanhToanInfos(thanhToanInfos)
                .withSanPhamChiTietInfos(sanPhamChiTietInfos)
                .withLichSuHoaDonInfos(lichSuHoaDonInfos)
                .build();
    }

    @Override
    public Map<String, Long> getStatusCounts() {
        List<HoaDon> allInvoices = hoaDonRepository.findAll().stream()
                .filter(hoaDon -> hoaDon.getDeleted() == null || !hoaDon.getDeleted())
                .collect(Collectors.toList());

        Map<String, Long> statusCounts = new HashMap<>();
        for (short s = HoaDonStatus.MIN; s <= HoaDonStatus.MAX; s++) {
            statusCounts.put(String.valueOf(s), 0L);
        }

        for (HoaDon hoaDon : allInvoices) {
            String statusKey = String.valueOf(hoaDon.getTrangThai());
            if (statusCounts.containsKey(statusKey)) {
                statusCounts.put(statusKey, statusCounts.get(statusKey) + 1);
            }
        }

        return statusCounts;
    }

    @Override
    public Map<String, Long> getPriceRange() {
        Long minPrice = hoaDonRepository.findMinPrice();
        Long maxPrice = hoaDonRepository.findMaxPrice();
        
        Map<String, Long> priceRange = new HashMap<>();
        priceRange.put("minPrice", minPrice != null ? minPrice : 0L);
        priceRange.put("maxPrice", maxPrice != null ? maxPrice : 0L);
        
        return priceRange;
    }

    @Override
    public BigDecimal getCompletedRevenue() {
        return hoaDonRepository.sumCompletedRevenue();
    }

    @Override
    @Transactional
    public synchronized HoaDonDTOResponse updateHoaDonStatus(Integer id, Short trangThai, Integer idNhanVien) {
        return updateHoaDonStatusWithNote(id, trangThai, idNhanVien, null);
    }

    @Override
    @Transactional
    public synchronized HoaDonDTOResponse updateHoaDonStatusWithNote(Integer id, Short trangThai, Integer idNhanVien, String ghiChu) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại hoặc đã bị xóa"));

        // Endpoint "chung" chỉ cho tiến đúng một bước theo HoaDonStatus.NEXT:
        // 1 -> 2 -> 3 -> 4 -> 5, và giao lại 9 -> 4. Hủy, giao thất bại, hoàn kho, hoàn hàng có
        // endpoint riêng vì cần lý do và xử lý tồn kho.
        // Đơn tại quầy không có bước xác nhận / vận chuyển: thanh toán xong là "Đã hoàn thành".
        if (isTaiQuay(hoaDon)) {
            throw new RuntimeException("Đơn tại quầy không có trạng thái vận chuyển, không thể cập nhật trạng thái");
        }
        Short currentStatus = hoaDon.getTrangThai();
        Short buocTiepTheo = currentStatus == null ? null : HoaDonStatus.NEXT.get(currentStatus);
        if (trangThai == null || !trangThai.equals(buocTiepTheo)) {
            throw new RuntimeException("Không thể chuyển từ \"" + HoaDonStatus.toLabel(currentStatus) + "\" sang \""
                    + HoaDonStatus.toLabel(trangThai) + "\""
                    + (buocTiepTheo != null ? ". Bước tiếp theo hợp lệ: \"" + HoaDonStatus.toLabel(buocTiepTheo) + "\"" : ""));
        }

        // If the invoice was in a pending state (deleted = true), mark it as active.
        if (hoaDon.getDeleted()) {
            hoaDon.setDeleted(false);
        }

        // Tồn kho chỉ được trừ đúng một lần, ngay khi đơn bước vào nhóm trạng thái "đã xuất
        // kho" (bắt đầu từ Đã xác nhận). Kiểm tra đủ hàng trước khi đổi trạng thái để không
        // tạo ra tồn kho âm.
        boolean wasDeducted = HoaDonStatus.isStockDeducted(currentStatus);
        boolean willBeDeducted = HoaDonStatus.isStockDeducted(trangThai);
        if (!wasDeducted && willBeDeducted) {
            deductStockForHoaDon(hoaDon);
        }

        hoaDon.setTrangThai(trangThai);
        hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

        LichSuHoaDon lichSuHoaDon = new LichSuHoaDon();
        lichSuHoaDon.setMa("LSHD_" + System.currentTimeMillis());
        
        String hanhDong = "Cập nhật trạng thái: " + mapStatusToString(trangThai);
        if (!wasDeducted && willBeDeducted) {
            hanhDong += " - Đã trừ tồn kho";
        }
        lichSuHoaDon.setHanhDong(hanhDong);
        lichSuHoaDon.setGhiChu(ghiChu != null && !ghiChu.isBlank() ? ghiChu.trim() : null);
        
        lichSuHoaDon.setThoiGian(new java.sql.Timestamp(System.currentTimeMillis()));
        lichSuHoaDon.setIdNhanVien(idNhanVien != null ?
                nhanVienRepository.findById(idNhanVien)
                        .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"))
                : null);
        lichSuHoaDon.setIdHoaDon(hoaDon);
        // Lưu trạng thái mới sau khi chuyển để timeline có thể filter chính xác
        lichSuHoaDon.setDeleted(trangThai);

        lichSuHoaDonRepository.save(lichSuHoaDon);
        hoaDonRepository.save(hoaDon);

        return hoaDonMapper.mapToDto(hoaDon);
    }

    @Override
    public HoaDonDTOResponse getHoaDonByMa(String maHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findByMa(maHoaDon)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại với mã: " + maHoaDon));
        
        return hoaDonMapper.mapToDto(hoaDon);
    }

    private String mapStatusToString(Short trangThai) {
        return HoaDonStatus.toLabel(trangThai);
    }

    // Sales counter specific methods implementation
    @Override
    public List<HoaDon> getPendingInvoicesForSales() {
        return hoaDonRepository.findPendingInvoicesForSales();
    }

    @Override
    @Transactional
    public HoaDon createPendingInvoice(Integer idNhanVien) {
        HoaDon newInvoice = new HoaDon();
        
        // Don't set ma - let database auto-generate it
        // newInvoice.setMa() is not called - database will handle auto-increment
        
        // Set default values for pending invoice
        newInvoice.setTrangThai((short) 0); // Status = 0 (pending for sales counter)
        newInvoice.setDeleted(true); // Set deleted = 1 for pending invoices
        newInvoice.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        // Trước đây dùng Date.valueOf(LocalDate.now()) - kiểu java.sql.Date luôn cắt giờ về
        // 00:00, khiến hóa đơn nào cũng "đúng ngày nhưng sai giờ" (luôn hiện 00:00/07:00 tuỳ
        // múi giờ hiển thị). Dùng Timestamp để giữ đúng giờ tạo thực tế.
        newInvoice.setNgayTao(new Timestamp(System.currentTimeMillis()));
        
        // Set default amounts
        newInvoice.setTienSanPham(BigDecimal.ZERO);
        newInvoice.setPhiVanChuyen(BigDecimal.ZERO);
        newInvoice.setTongTien(BigDecimal.ZERO);
        newInvoice.setTongTienSauGiam(BigDecimal.ZERO);
        
        // Set default customer info (guest customer)
        newInvoice.setTenKhachHang("Khách lẻ");
        newInvoice.setDiaChiKhachHang("");
        newInvoice.setSoDienThoaiKhachHang("");
        newInvoice.setLoaiDon("Tại quầy");
        
        // id_khach_hang bắt buộc trong DB -> gắn bản ghi "Khách lẻ" dùng chung. Trước đây gắn
        // khách ID=1 (một khách thật có tài khoản) nên khách đó thấy mọi đơn tại quầy của người khác.
        newInvoice.setIdKhachHang(getOrCreateKhachLe());
        
        // Nhân viên đang đăng nhập tạo hóa đơn chờ; khi thanh toán sẽ ghi lại nhân viên thanh toán.
        newInvoice.setIdNhanVien(idNhanVien != null
                ? nhanVienRepository.findById(idNhanVien).orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"))
                : nhanVienRepository.findById(1).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên mặc định")));
        
        // Save the invoice first
        HoaDon savedInvoice = hoaDonRepository.save(newInvoice);
        
        // Force flush to ensure database operations are completed
        hoaDonRepository.flush();
        
        // Reload from database to get the auto-generated ma field
        HoaDon reloadedInvoice = hoaDonRepository.findById(savedInvoice.getId())
                .orElseThrow(() -> new RuntimeException("Failed to reload saved invoice"));
        
        // If ma is still null, generate it manually based on ID
        if (reloadedInvoice.getMa() == null || reloadedInvoice.getMa().isEmpty()) {
            String generatedMa = "HD" + String.format("%06d", reloadedInvoice.getId());
            reloadedInvoice.setMa(generatedMa);
            reloadedInvoice = hoaDonRepository.save(reloadedInvoice);
        }
        
        return reloadedInvoice;
    }

    @Override
    @Transactional
    public HoaDon updatePendingInvoiceCustomer(Integer id, UpdateCustomerRequest request) {
        HoaDon invoice = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));
        
        // Update customer information
        if ("Khách lẻ".equals(request.getTenKhachHang())) {
            // "Bỏ chọn khách" ở màn hình bán hàng -> gắn về bản ghi Khách lẻ dùng chung
            // (bản FE cũ gửi khachHangId = 1, tức một khách thật).
            invoice.setIdKhachHang(getOrCreateKhachLe());
        } else if (request.getKhachHangId() != null) {
            invoice.setIdKhachHang(khachHangRepository.findById(request.getKhachHangId())
                    .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại")));
        }
        
        if (request.getTenKhachHang() != null) {
            invoice.setTenKhachHang(request.getTenKhachHang());
        }
        
        if (request.getSoDienThoaiKhachHang() != null) {
            invoice.setSoDienThoaiKhachHang(request.getSoDienThoaiKhachHang());
        }
        
        if (request.getDiaChiKhachHang() != null) {
            invoice.setDiaChiKhachHang(request.getDiaChiKhachHang());
        }
        
        if (request.getEmail() != null) {
            invoice.setEmail(request.getEmail());
        }
        
        // Update timestamp
        invoice.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        
        // Save invoice first
        HoaDon savedInvoice = hoaDonRepository.save(invoice);
        
        // Synchronize cart customer ID with invoice customer ID
        gioHangService.updateCartCustomer(id, request.getKhachHangId());
        
        return savedInvoice;
    }

    @Override
    @Transactional
    public void deletePendingInvoice(Integer id) {
        try {
            HoaDon invoice = hoaDonRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));
            
            // Only allow deletion of pending invoices (status = 0)
            if (invoice.getTrangThai() != 0) {
                throw new RuntimeException("Chỉ có thể xóa hóa đơn đang chờ");
            }
            
            // Hóa đơn nháp (trạng thái 0) chưa bao giờ bị trừ tồn kho - tồn kho chỉ bị trừ
            // tại bước xác nhận/thanh toán (xem deductStockForHoaDon). Vì vậy ở đây chỉ dọn
            // dữ liệu, tuyệt đối KHÔNG cộng trả tồn kho như bản cũ (bản cũ hợp lý khi giỏ
            // hàng còn giữ chỗ, giờ sẽ làm tồn kho phình lên mỗi lần hủy hóa đơn chờ).
            List<HoaDonChiTiet> cartItems = hoaDonChiTietRepository.findAllByHoaDonId(id);
            if (!cartItems.isEmpty()) {
                hoaDonChiTietRepository.deleteAll(cartItems);
            }

            // Delete any GioHang records that reference this invoice
            gioHangRepository.findByHoaDonId(id).ifPresent(gioHang -> {
                List<GioHangChiTiet> gioHangChiTietList = gioHangChiTietRepository.findByGioHangId(gioHang.getId());
                if (!gioHangChiTietList.isEmpty()) {
                    gioHangChiTietRepository.deleteAll(gioHangChiTietList);
                }
                gioHangRepository.delete(gioHang);
            });

            // Finally delete the invoice
            hoaDonRepository.delete(invoice);
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa hóa đơn: " + e.getMessage());
        }
    }

    private com.example.sd_24_striker_be.modal.account.KhachHang getOrCreateKhachLe() {
        return khachHangRepository.findFirstByMa(KhachHangRepository.MA_KHACH_LE)
                .orElseGet(() -> khachHangRepository.save(com.example.sd_24_striker_be.modal.account.KhachHang.builder()
                        .ma(KhachHangRepository.MA_KHACH_LE)
                        .ten("Khách lẻ")
                        .deleted(true)
                        .createdAt(java.time.Instant.now())
                        .build()));
    }

    // Bắt buộc @Transactional: phương thức này vừa tạo HoaDonChiTiet, vừa trừ tồn kho, vừa
    // xóa giỏ hàng. Nếu trừ kho thất bại (không đủ hàng) mà không rollback thì hóa đơn sẽ
    // bị chốt trong khi kho chưa trừ -> lệch số liệu vĩnh viễn.
    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        try {
            // 1. Validate and get the pending invoice
            HoaDon hoaDon = hoaDonRepository.findById(paymentRequest.getHoaDonId())
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            if (hoaDon.getTrangThai() != 0) {
                return PaymentResponse.error("Hóa đơn đã được thanh toán hoặc không ở trạng thái chờ");
            }

            // 2. Tính tiền ở SERVER từ giỏ hàng (giá hiện hành) + phiếu giảm giá. Trước đây lấy
            //    tongTien / tongTienSauGiam do client gửi lên, nên hóa đơn lưu bất kỳ số nào.
            Optional<GioHang> gioHangTinhTien = gioHangRepository.findByHoaDonId(hoaDon.getId());
            if (gioHangTinhTien.isEmpty()) {
                return PaymentResponse.error("Không tìm thấy giỏ hàng cho hóa đơn này");
            }
            List<GioHangChiTiet> dongGioHang = gioHangChiTietRepository.findByGioHangId(gioHangTinhTien.get().getId());
            if (dongGioHang.isEmpty()) {
                return PaymentResponse.error("Giỏ hàng trống, không thể thanh toán");
            }
            BigDecimal tienSanPham = BigDecimal.ZERO;
            for (GioHangChiTiet dong : dongGioHang) {
                BigDecimal gia = chiTietSanPhamService.getEffectivePrice(dong.getIdChiTietSp());
                dong.setGia(gia);
                gioHangChiTietRepository.save(dong);
                tienSanPham = tienSanPham.add(gia.multiply(BigDecimal.valueOf(dong.getSoLuong())));
            }

            PhieuGiamGia phieuGiamGia = null;
            BigDecimal tienGiam = BigDecimal.ZERO;
            if (paymentRequest.getPhieuGiamGiaId() != null) {
                phieuGiamGia = phieuGiamGiaRepository.findById(paymentRequest.getPhieuGiamGiaId())
                        .orElseThrow(() -> new RuntimeException("Phiếu giảm giá không tồn tại"));
                String loi = VoucherCalculator.validate(phieuGiamGia, tienSanPham);
                if (loi != null) {
                    return PaymentResponse.error(loi);
                }
                phieuGiamGiaServices.kiemTraQuyenSuDung(phieuGiamGia, hoaDon.getIdKhachHang());
                tienGiam = VoucherCalculator.discountAmount(phieuGiamGia, tienSanPham);
                // Lượt voucher được trừ trong deductStockForHoaDon() (cùng lúc với tồn kho) -
                // bán tại quầy thanh toán xong là chốt đơn luôn nên trừ ngay trong lần gọi đó.
            }
            BigDecimal tongTien = tienSanPham; // bán tại quầy không có phí vận chuyển
            BigDecimal tongTienSauGiam = tongTien.subtract(tienGiam);

            // Số tiền khách đưa KHÔNG bắt buộc: bỏ trống thì coi như khách trả đúng số tiền.
            BigDecimal tienMat = paymentRequest.getTienMat() != null ? paymentRequest.getTienMat() : BigDecimal.ZERO;
            BigDecimal tienChuyenKhoan = paymentRequest.getTienChuyenKhoan() != null ? paymentRequest.getTienChuyenKhoan() : BigDecimal.ZERO;
            String phuongThuc = paymentRequest.getPhuongThucThanhToan();
            BigDecimal tienThua = BigDecimal.ZERO;
            if ("Tiền mặt".equals(phuongThuc)) {
                if (tienMat.signum() == 0) {
                    tienMat = tongTienSauGiam;
                } else if (tienMat.compareTo(tongTienSauGiam) < 0) {
                    return PaymentResponse.error("Số tiền khách đưa không đủ để thanh toán");
                }
                tienThua = tienMat.subtract(tongTienSauGiam);
                tienChuyenKhoan = BigDecimal.ZERO;
            } else if ("Chuyển khoản".equals(phuongThuc) || "VnPay".equalsIgnoreCase(phuongThuc)) {
                tienMat = BigDecimal.ZERO;
                tienChuyenKhoan = tongTienSauGiam;
            } else {
                // Kết hợp: phần chuyển khoản tùy nhập, tiền mặt bù phần còn lại nếu bỏ trống
                if (tienChuyenKhoan.compareTo(tongTienSauGiam) > 0) {
                    return PaymentResponse.error("Tiền chuyển khoản vượt quá số tiền cần thanh toán");
                }
                BigDecimal conLai = tongTienSauGiam.subtract(tienChuyenKhoan);
                if (tienMat.signum() == 0) {
                    tienMat = conLai;
                } else if (tienMat.compareTo(conLai) < 0) {
                    return PaymentResponse.error("Tổng tiền khách đưa không đủ để thanh toán");
                }
                tienThua = tienMat.subtract(conLai);
            }
            // Lưu số tiền thực thu (không tính tiền thừa trả lại)
            paymentRequest.setTienMat(tienMat.subtract(tienThua));
            paymentRequest.setTienChuyenKhoan(tienChuyenKhoan);

            // 4. Update invoice information
            hoaDon.setTrangThai(HoaDonStatus.DA_HOAN_THANH); // Tại quầy: tự xác nhận + hoàn thành
            hoaDon.setDeleted(false); // Mark as active invoice
            hoaDon.setTienSanPham(tienSanPham);
            hoaDon.setPhiVanChuyen(BigDecimal.ZERO);
            hoaDon.setTongTien(tongTien); // Original total before discount
            hoaDon.setTongTienSauGiam(tongTienSauGiam); // Total after discount
            java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
            hoaDon.setUpdatedAt(now);
            hoaDon.setNgayThanhToan(now); // trước đây hóa đơn tại quầy không lưu ngày thanh toán

            // Set employee
            hoaDon.setIdNhanVien(nhanVienRepository.findById(paymentRequest.getNhanVienId())
                    .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại")));

            if (phieuGiamGia != null) {
                hoaDon.setIdPhieuGiamGia(phieuGiamGia);
            }

            // 5. Create invoice details (HoaDonChiTiet) from actual cart data
            // First, get the actual cart data to ensure consistency
            Optional<GioHang> gioHangOpt = gioHangRepository.findByHoaDonId(hoaDon.getId());
            if (gioHangOpt.isPresent()) {
                List<GioHangChiTiet> gioHangChiTietList = gioHangChiTietRepository.findByGioHangId(gioHangOpt.get().getId());
                
                for (GioHangChiTiet cartItem : gioHangChiTietList) {
                    ChiTietSanPham chiTietSanPham = cartItem.getIdChiTietSp();

                    // Create invoice detail using cart data (preserves original price and quantity)
                    HoaDonChiTiet hoaDonChiTiet = HoaDonChiTiet.builder()
                            .idHoaDon(hoaDon)
                            .idChiTietSp(chiTietSanPham)
                            .gia(cartItem.getGia()) // Use price from cart, not current product price
                            .soLuong(cartItem.getSoLuong()) // Use quantity from cart
                            .trangThai((short) 1)
                            .build();

                    hoaDonChiTietRepository.save(hoaDonChiTiet);
                }

                // Đẩy các dòng HoaDonChiTiet vừa tạo xuống DB để deductStockForHoaDon()
                // đọc lại được chúng qua repository.
                hoaDonChiTietRepository.flush();

                // Bán tại quầy đi thẳng từ "Hóa đơn chờ" (0) sang "Đã hoàn thành" (5), tức là
                // bước xác nhận và bước giao hàng gộp làm một -> trừ tồn kho ngay tại đây.
                // Giỏ hàng tại quầy KHÔNG còn giữ chỗ tồn kho nữa (xem GioHangServiceImpl),
                // nên đây là lần trừ duy nhất cho đơn này.
                deductStockForHoaDon(hoaDon);

                // Clean up cart data immediately after creating invoice details
                // This ensures all data is transferred to HoaDonChiTiet before deletion
                gioHangChiTietRepository.deleteAll(gioHangChiTietList);
                gioHangRepository.delete(gioHangOpt.get());

            } else {
                return PaymentResponse.error("Không tìm thấy giỏ hàng cho hóa đơn này");
            }

            // 6. Ghi nhận hình thức thanh toán. "Kết hợp" = một dòng tiền mặt + một dòng chuyển khoản.
            if (paymentRequest.getTienMat().signum() > 0 || "Tiền mặt".equals(phuongThuc)) {
                luuHinhThucThanhToan(hoaDon, "Tiền mặt", paymentRequest.getTienMat(), BigDecimal.ZERO);
            }
            if (paymentRequest.getTienChuyenKhoan().signum() > 0) {
                luuHinhThucThanhToan(hoaDon, "VnPay".equalsIgnoreCase(phuongThuc) ? "VNPay" : "Chuyển khoản",
                        BigDecimal.ZERO, paymentRequest.getTienChuyenKhoan());
            }

            // 7. Create payment history record (LichSuHoaDon)
            LichSuHoaDon lichSuHoaDon = new LichSuHoaDon();
            lichSuHoaDon.setMa("LSHD_" + System.currentTimeMillis());
            lichSuHoaDon.setHanhDong("Thanh toán tại quầy (" + paymentRequest.getPhuongThucThanhToan()
                    + ") - Tự động xác nhận và hoàn thành, đã trừ tồn kho");
            lichSuHoaDon.setThoiGian(new java.sql.Timestamp(System.currentTimeMillis()));
            lichSuHoaDon.setIdNhanVien(hoaDon.getIdNhanVien());
            lichSuHoaDon.setIdHoaDon(hoaDon);
            lichSuHoaDon.setDeleted(HoaDonStatus.DA_HOAN_THANH);
            if (paymentRequest.getGhiChu() != null && !paymentRequest.getGhiChu().isBlank()) {
                lichSuHoaDon.setGhiChu(paymentRequest.getGhiChu().trim());
                hoaDon.setGhiChu(paymentRequest.getGhiChu().trim());
            }

            lichSuHoaDonRepository.save(lichSuHoaDon);

            // 8. Cart cleanup already handled in step 5 after creating HoaDonChiTiet
            // This ensures data consistency and prevents issues with missing cart data

            // 9. Save the updated invoice
            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

            // 9b. Gửi email xác nhận đơn hàng cho khách (best-effort, không làm hỏng giao dịch nếu gửi lỗi)
            sendOrderConfirmationEmailSafely(savedHoaDon);

            // 10. Return success response
            return PaymentResponse.success(
                    savedHoaDon.getId(),
                    savedHoaDon.getMa(),
                    tongTienSauGiam,
                    tienThua,
                    paymentRequest.getPhuongThucThanhToan()
            );

        } catch (Exception e) {
            // Lỗi bị nuốt thành response thất bại nên Spring không tự thấy exception để
            // rollback -> phải đánh dấu rollback thủ công, nếu không hóa đơn sẽ bị commit
            // dở dang (đã tạo chi tiết nhưng chưa trừ kho).
            markRollbackOnly();
            logger.error("Lỗi xử lý thanh toán hóa đơn {}: {}", paymentRequest.getHoaDonId(), e.getMessage(), e);
            return PaymentResponse.error("Lỗi xử lý thanh toán: " + e.getMessage());
        }
    }

    private void luuHinhThucThanhToan(HoaDon hoaDon, String kieu, BigDecimal tienMat, BigDecimal tienChuyenKhoan) {
        PhuongThucThanhToan pt = phuongThucThanhToanRepository.findByKieuThanhToan(kieu)
                .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không tồn tại: " + kieu));
        hinhThucThanhToanRepository.save(HinhThucThanhToan.builder()
                .idHoaDon(hoaDon)
                .idPhuongThucThanhToan(pt)
                .tienMat(tienMat)
                .tienChuyenKhoan(tienChuyenKhoan)
                .deleted(false)
                .build());
    }

    private static boolean isTaiQuay(HoaDon hoaDon) {
        String loaiDon = hoaDon.getLoaiDon();
        return loaiDon != null && (loaiDon.equalsIgnoreCase("Tại quầy") || loaiDon.equalsIgnoreCase("TAI_QUAY"));
    }

    /** Đánh dấu giao dịch hiện tại phải rollback, an toàn cả khi không có giao dịch nào đang chạy. */
    private void markRollbackOnly() {
        try {
            org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus().setRollbackOnly();
        } catch (IllegalStateException ignored) {
            // Không nằm trong giao dịch nào - không có gì để rollback.
        }
    }

    @Override
    @Transactional
    public void updateInvoiceStatusAfterPayment(Integer invoiceId, Integer status) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(invoiceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + invoiceId));

            if (status == 1) {
                // Payment successful - process like cash payment
                // 1. Update invoice status to completed
                hoaDon.setTrangThai((short) 5); // Đã hoàn thành
                hoaDon.setDeleted(false); // Mark as active invoice
                hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

                // 2. Create invoice details (HoaDonChiTiet) from cart data
                Optional<GioHang> gioHangOpt = gioHangRepository.findByHoaDonId(hoaDon.getId());
                if (gioHangOpt.isPresent()) {
                    List<GioHangChiTiet> gioHangChiTietList = gioHangChiTietRepository.findByGioHangId(gioHangOpt.get().getId());
                    
                    BigDecimal tongTien = BigDecimal.ZERO;
                    
                    for (GioHangChiTiet cartItem : gioHangChiTietList) {
                        ChiTietSanPham chiTietSanPham = cartItem.getIdChiTietSp();
                        
                        // Create invoice detail using cart data
                        HoaDonChiTiet hoaDonChiTiet = HoaDonChiTiet.builder()
                                .idHoaDon(hoaDon)
                                .idChiTietSp(chiTietSanPham)
                                .gia(cartItem.getGia())
                                .soLuong(cartItem.getSoLuong())
                                .trangThai((short) 1)
                                .build();

                        hoaDonChiTietRepository.save(hoaDonChiTiet);
                        
                        // Calculate total
                        tongTien = tongTien.add(cartItem.getGia().multiply(BigDecimal.valueOf(cartItem.getSoLuong())));
                    }
                    
                    // Update invoice totals
                    hoaDon.setTienSanPham(tongTien);
                    hoaDon.setTongTien(tongTien);
                    hoaDon.setTongTienSauGiam(tongTien);
                    
                    // 3. Create VNPay payment record
                    PhuongThucThanhToan vnpayMethod = phuongThucThanhToanRepository
                            .findByKieuThanhToan("VnPay")
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán VnPay"));

                    HinhThucThanhToan hinhThucThanhToan = HinhThucThanhToan.builder()
                            .idHoaDon(hoaDon)
                            .idPhuongThucThanhToan(vnpayMethod)
                            .tienMat(BigDecimal.ZERO)
                            .tienChuyenKhoan(tongTien)
                            .deleted(false)
                            .build();

                    hinhThucThanhToanRepository.save(hinhThucThanhToan);
                    
                    // 4. Clean up cart data after successful payment
                    gioHangChiTietRepository.deleteAll(gioHangChiTietList);
                    gioHangRepository.delete(gioHangOpt.get());

                    logStatusChange(hoaDon, null, "Thanh toán thành công - đơn hàng đã được xác nhận");
                    hoaDonRepository.save(hoaDon);
                    sendOrderConfirmationEmailSafely(hoaDon);
                    return;
                } else {
                    throw new RuntimeException("Không tìm thấy giỏ hàng cho hóa đơn này");
                }
            } else {
                // Payment failed - hủy đơn và HOÀN LẠI tồn kho đã bị trừ lúc thêm vào giỏ
                // (trước đây chỉ đổi trạng thái, tồn kho bị trừ "kẹt" vĩnh viễn vì giỏ hàng
                // bị bỏ dở không bao giờ được dọn).
                releaseReservedCartStock(hoaDon);
                hoaDon.setTrangThai(HoaDonStatus.DA_HUY);
                hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                logStatusChange(hoaDon, null, "Thanh toán thất bại - đơn hàng tự động hủy, đã hoàn lại tồn kho");
            }

            hoaDonRepository.save(hoaDon);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật trạng thái hóa đơn: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void updateInvoiceStatusAfterVNPayPayment(Integer invoiceId, Integer status, Long vnpayAmount) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(invoiceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + invoiceId));

            if (status == 1) {
                // Payment successful - process like cash payment
                // 1. Update invoice status to completed
                hoaDon.setTrangThai((short) 5); // Đã hoàn thành
                hoaDon.setDeleted(false); // Mark as active invoice
                hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

                // 2. Create invoice details (HoaDonChiTiet) from cart data
                Optional<GioHang> gioHangOpt = gioHangRepository.findByHoaDonId(hoaDon.getId());
                if (gioHangOpt.isPresent()) {
                    List<GioHangChiTiet> gioHangChiTietList = gioHangChiTietRepository.findByGioHangId(gioHangOpt.get().getId());
                    
                    for (GioHangChiTiet cartItem : gioHangChiTietList) {
                        ChiTietSanPham chiTietSanPham = cartItem.getIdChiTietSp();
                        
                        // Create invoice detail using cart data
                        HoaDonChiTiet hoaDonChiTiet = HoaDonChiTiet.builder()
                                .idHoaDon(hoaDon)
                                .idChiTietSp(chiTietSanPham)
                                .gia(cartItem.getGia())
                                .soLuong(cartItem.getSoLuong())
                                .trangThai((short) 1)
                                .build();

                        hoaDonChiTietRepository.save(hoaDonChiTiet);
                    }

                    // Đơn VNPay tại quầy cũng nhảy thẳng sang "Đã hoàn thành" (5) nên trừ kho
                    // ngay tại đây - giống luồng tiền mặt ở processPayment().
                    hoaDonChiTietRepository.flush();
                    deductStockForHoaDon(hoaDon);

                    // Update invoice totals with VNPay amount
                    BigDecimal vnpayAmountBD = BigDecimal.valueOf(vnpayAmount);
                    hoaDon.setTienSanPham(vnpayAmountBD);
                    hoaDon.setTongTien(vnpayAmountBD);
                    hoaDon.setTongTienSauGiam(vnpayAmountBD);
                    
                    // 3. Create VNPay payment record with actual VNPay amount
                    PhuongThucThanhToan vnpayMethod = phuongThucThanhToanRepository
                            .findByKieuThanhToan("VnPay")
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán VnPay"));

                    HinhThucThanhToan hinhThucThanhToan = HinhThucThanhToan.builder()
                            .idHoaDon(hoaDon)
                            .idPhuongThucThanhToan(vnpayMethod)
                            .tienMat(BigDecimal.ZERO)
                            .tienChuyenKhoan(vnpayAmountBD) // Use actual VNPay amount
                            .deleted(false)
                            .build();

                    hinhThucThanhToanRepository.save(hinhThucThanhToan);
                    
                    // 4. Clean up cart data after successful payment
                    gioHangChiTietRepository.deleteAll(gioHangChiTietList);
                    gioHangRepository.delete(gioHangOpt.get());

                    logStatusChange(hoaDon, null, "Thanh toán VNPay thành công - đơn hàng đã được xác nhận");
                    hoaDonRepository.save(hoaDon);
                    sendOrderConfirmationEmailSafely(hoaDon);
                    return;
                } else {
                    throw new RuntimeException("Không tìm thấy giỏ hàng cho hóa đơn này");
                }
            } else {
                // Payment failed - hủy đơn và hoàn lại tồn kho đã bị trừ lúc thêm vào giỏ
                releaseReservedCartStock(hoaDon);
                hoaDon.setTrangThai(HoaDonStatus.DA_HUY);
                hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                logStatusChange(hoaDon, null, "Thanh toán VNPay thất bại - đơn hàng tự động hủy, đã hoàn lại tồn kho");
            }

            hoaDonRepository.save(hoaDon);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật trạng thái hóa đơn VNPay: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void processCashPayment(Integer invoiceId, BigDecimal cashAmount) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(invoiceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + invoiceId));

            // Find cash payment method
            PhuongThucThanhToan cashPaymentMethod = phuongThucThanhToanRepository.findByKieuThanhToan("Tiền mặt")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán tiền mặt"));

            // Create cash payment record
            HinhThucThanhToan cashPayment = new HinhThucThanhToan();
            cashPayment.setIdHoaDon(hoaDon);
            cashPayment.setIdPhuongThucThanhToan(cashPaymentMethod);
            cashPayment.setTienMat(cashAmount);
            cashPayment.setTienChuyenKhoan(BigDecimal.ZERO);
            cashPayment.setDeleted(false);

            hinhThucThanhToanRepository.save(cashPayment);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý thanh toán tiền mặt: " + e.getMessage(), e);
        }
    }

    // ==================== Helpers dùng chung cho hủy đơn / hoàn hàng / email ====================

    /**
     * Hoàn lại tồn kho cho toàn bộ dòng hàng (chưa bị xóa) của một hóa đơn đã lên đơn
     * (HoaDonChiTiet), dùng khi hủy đơn hoặc xác nhận hoàn hàng. Trước đây thao tác đổi
     * trạng thái sang "Đã hủy" hoàn toàn không đụng tới tồn kho.
     */
    /**
     * Trừ tồn kho cho toàn bộ dòng hàng của một hóa đơn. Đây là ĐIỂM TRỪ KHO DUY NHẤT
     * của hệ thống, được gọi đúng một lần khi đơn chuyển sang nhóm trạng thái "đã xuất
     * kho" (Đã xác nhận với đơn online, hoặc Đã hoàn thành với đơn bán tại quầy).
     * <p>
     * Trước đây tồn kho bị trừ ngay lúc thêm vào giỏ hàng VÀ trừ lần nữa lúc thanh toán
     * online ({@code BanHangClientServiceImpl}) -> trừ hai lần cho cùng một đơn. Gom về
     * một chỗ ở đây để không còn khả năng lệch kho.
     * <p>
     * Gộp số lượng theo từng biến thể trước khi trừ: một hóa đơn có thể có nhiều dòng
     * HoaDonChiTiet cùng trỏ vào một ChiTietSanPham (luồng client tạo mỗi đơn vị một
     * dòng riêng), nếu kiểm tra tồn từng dòng rời rạc sẽ lọt trường hợp tổng vượt tồn.
     *
     * @throws RuntimeException nếu bất kỳ biến thể nào không đủ tồn kho — ném trước khi
     *                          ghi bất cứ thay đổi nào để giao dịch rollback sạch.
     */
    private void deductStockForHoaDon(HoaDon hoaDon) {
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());

        Map<Integer, Integer> soLuongTheoBienThe = new HashMap<>();
        Map<Integer, ChiTietSanPham> bienTheById = new HashMap<>();
        for (HoaDonChiTiet chiTiet : chiTietList) {
            ChiTietSanPham bienThe = chiTiet.getIdChiTietSp();
            if (bienThe == null || chiTiet.getSoLuong() == null) continue;
            soLuongTheoBienThe.merge(bienThe.getId(), chiTiet.getSoLuong(), Integer::sum);
            bienTheById.putIfAbsent(bienThe.getId(), bienThe);
        }

        // Vòng 1: kiểm tra toàn bộ trước, để không trừ được nửa chừng rồi mới báo lỗi.
        for (Map.Entry<Integer, Integer> entry : soLuongTheoBienThe.entrySet()) {
            ChiTietSanPham bienThe = bienTheById.get(entry.getKey());
            int tonKho = bienThe.getSoLuongTonKho() != null ? bienThe.getSoLuongTonKho() : 0;
            if (tonKho < entry.getValue()) {
                throw new RuntimeException("Không đủ tồn kho để xác nhận đơn hàng. Sản phẩm \""
                        + moTaBienThe(bienThe) + "\" cần " + entry.getValue() + " nhưng chỉ còn " + tonKho);
            }
        }

        // Đơn được chốt (xác nhận / thanh toán tại quầy) -> trừ 1 lượt phiếu giảm giá của đơn,
        // giống tồn kho. Hết lượt thì không chốt được đơn. Hủy đơn sau đó KHÔNG hoàn lại lượt.
        phieuGiamGiaServices.truLuotSuDung(hoaDon.getIdPhieuGiamGia());

        // Vòng 2: trừ thật.
        for (Map.Entry<Integer, Integer> entry : soLuongTheoBienThe.entrySet()) {
            ChiTietSanPham bienThe = bienTheById.get(entry.getKey());
            int tonKho = bienThe.getSoLuongTonKho() != null ? bienThe.getSoLuongTonKho() : 0;
            int tonKhoMoi = tonKho - entry.getValue();
            bienThe.setSoLuongTonKho(tonKhoMoi);
            int daBan = bienThe.getSoLuongDaBan() != null ? bienThe.getSoLuongDaBan() : 0;
            bienThe.setSoLuongDaBan(daBan + entry.getValue());
            chiTietSanPhamRepository.save(bienThe);
            stockUpdateService.notifyStockUpdate(bienThe.getId(), tonKhoMoi);
        }
    }

    /** Mô tả ngắn gọn một biến thể để đưa vào thông báo lỗi tồn kho. */
    private String moTaBienThe(ChiTietSanPham bienThe) {
        StringBuilder sb = new StringBuilder();
        if (bienThe.getIdSanPham() != null && bienThe.getIdSanPham().getTenSanPham() != null) {
            sb.append(bienThe.getIdSanPham().getTenSanPham());
        } else {
            sb.append("ID ").append(bienThe.getId());
        }
        if (bienThe.getIdMauSac() != null && bienThe.getIdMauSac().getTenMauSac() != null) {
            sb.append(" - ").append(bienThe.getIdMauSac().getTenMauSac());
        }
        if (bienThe.getIdKichCo() != null && bienThe.getIdKichCo().getTenKichCo() != null) {
            sb.append(" - ").append(bienThe.getIdKichCo().getTenKichCo());
        }
        return sb.toString();
    }

    private void restoreStockForHoaDon(HoaDon hoaDon) {
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());
        for (HoaDonChiTiet chiTiet : chiTietList) {
            ChiTietSanPham chiTietSanPham = chiTiet.getIdChiTietSp();
            if (chiTietSanPham == null || chiTiet.getSoLuong() == null) continue;
            int currentStock = chiTietSanPham.getSoLuongTonKho() != null ? chiTietSanPham.getSoLuongTonKho() : 0;
            int newStock = currentStock + chiTiet.getSoLuong();
            chiTietSanPham.setSoLuongTonKho(newStock);
            int daBan = chiTietSanPham.getSoLuongDaBan() != null ? chiTietSanPham.getSoLuongDaBan() : 0;
            chiTietSanPham.setSoLuongDaBan(Math.max(0, daBan - chiTiet.getSoLuong()));
            chiTietSanPhamRepository.save(chiTietSanPham);
            stockUpdateService.notifyStockUpdate(chiTietSanPham.getId(), newStock);
        }
    }

    /**
     * Thanh toán online thất bại: dọn sạch giỏ hàng gắn với hóa đơn đó.
     * <p>
     * Trước đây phương thức này còn phải cộng trả tồn kho vì giỏ hàng có giữ chỗ (trừ kho
     * ngay lúc thêm sản phẩm). Từ khi tồn kho chỉ bị trừ tại bước xác nhận đơn
     * ({@link #deductStockForHoaDon}), giỏ hàng không còn chạm vào tồn kho nữa - cộng trả
     * ở đây sẽ tạo ra hàng "từ trên trời rơi xuống". Giữ lại tên phương thức để các điểm
     * gọi cũ không phải đổi.
     */
    private void releaseReservedCartStock(HoaDon hoaDon) {
        gioHangRepository.findByHoaDonId(hoaDon.getId()).ifPresent(gioHang -> {
            List<GioHangChiTiet> items = gioHangChiTietRepository.findByGioHangId(gioHang.getId());
            gioHangChiTietRepository.deleteAll(items);
            gioHangRepository.delete(gioHang);
        });
    }

    private void logStatusChange(HoaDon hoaDon, Integer idNhanVien, String hanhDong) {
        logStatusChange(hoaDon, idNhanVien, hanhDong, null);
    }

    private void logStatusChange(HoaDon hoaDon, Integer idNhanVien, String hanhDong, String ghiChu) {
        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setGhiChu(ghiChu != null && !ghiChu.isBlank() ? ghiChu.trim() : null);
        lichSu.setMa("LSHD_" + System.currentTimeMillis());
        lichSu.setHanhDong(hanhDong);
        lichSu.setThoiGian(new java.sql.Timestamp(System.currentTimeMillis()));
        lichSu.setIdNhanVien(idNhanVien != null ? nhanVienRepository.findById(idNhanVien).orElse(null) : null);
        lichSu.setIdHoaDon(hoaDon);
        // Lưu trạng thái mới sau khi chuyển để timeline có thể filter chính xác (quy ước sẵn có)
        lichSu.setDeleted(hoaDon.getTrangThai());
        lichSuHoaDonRepository.save(lichSu);
    }

    private void sendOrderConfirmationEmailSafely(HoaDon hoaDon) {
        try {
            if (hoaDon.getEmail() != null && !hoaDon.getEmail().isBlank()) {
                emailService.sendOrderConfirmationEmail(hoaDon, hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId()));
            }
        } catch (Exception e) {
            logger.warn("Không thể gửi email xác nhận đơn hàng {}: {}", hoaDon.getMa(), e.getMessage());
        }
    }

    private void sendCancelEmailSafely(HoaDon hoaDon, String lyDo, boolean byCustomer) {
        try {
            if (hoaDon.getEmail() != null && !hoaDon.getEmail().isBlank()) {
                emailService.sendOrderCancelledEmail(hoaDon, lyDo, byCustomer);
            }
        } catch (Exception e) {
            logger.warn("Không thể gửi email hủy đơn {}: {}", hoaDon.getMa(), e.getMessage());
        }
    }

    private void sendReturnRequestedEmailSafely(HoaDon hoaDon, String lyDo) {
        try {
            if (hoaDon.getEmail() != null && !hoaDon.getEmail().isBlank()) {
                emailService.sendReturnRequestedEmail(hoaDon, lyDo);
            }
        } catch (Exception e) {
            logger.warn("Không thể gửi email yêu cầu hoàn hàng {}: {}", hoaDon.getMa(), e.getMessage());
        }
    }

    private void sendReturnConfirmedEmailSafely(HoaDon hoaDon) {
        try {
            if (hoaDon.getEmail() != null && !hoaDon.getEmail().isBlank()) {
                emailService.sendReturnConfirmedEmail(hoaDon);
            }
        } catch (Exception e) {
            logger.warn("Không thể gửi email xác nhận hoàn hàng {}: {}", hoaDon.getMa(), e.getMessage());
        }
    }

    private void sendReturnRejectedEmailSafely(HoaDon hoaDon, String lyDo) {
        try {
            if (hoaDon.getEmail() != null && !hoaDon.getEmail().isBlank()) {
                emailService.sendReturnRejectedEmail(hoaDon, lyDo);
            }
        } catch (Exception e) {
            logger.warn("Không thể gửi email từ chối hoàn hàng {}: {}", hoaDon.getMa(), e.getMessage());
        }
    }

    // ==================== Hủy đơn / hoàn hàng ====================

    @Override
    @Transactional
    public synchronized HoaDonDTOResponse cancelOrder(Integer id, Integer idNhanVien, Integer idKhachHangActor, String lyDo) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại hoặc đã bị xóa"));

        boolean byCustomer = idKhachHangActor != null;
        if (byCustomer && (hoaDon.getIdKhachHang() == null || !hoaDon.getIdKhachHang().getId().equals(idKhachHangActor))) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }
        if (lyDo == null || lyDo.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập lý do hủy đơn");
        }

        Short currentStatus = hoaDon.getTrangThai();
        java.util.Set<Short> allowedFrom = byCustomer ? HoaDonStatus.CUSTOMER_CANCELLABLE_FROM : HoaDonStatus.ADMIN_CANCELLABLE_FROM;
        if (currentStatus == null || !allowedFrom.contains(currentStatus)) {
            throw new RuntimeException(byCustomer
                    ? "Đơn hàng ở trạng thái \"" + HoaDonStatus.toLabel(currentStatus) + "\" không thể tự hủy. Vui lòng liên hệ cửa hàng để được hỗ trợ."
                    : "Không thể hủy đơn hàng ở trạng thái \"" + HoaDonStatus.toLabel(currentStatus) + "\"");
        }

        // Chỉ cộng trả tồn kho nếu đơn ĐÃ từng bị trừ kho (tức đã qua bước "Đã xác nhận").
        // Đơn còn ở "Chờ xác nhận" chưa hề đụng tới tồn kho - nếu cộng trả ở đây thì tồn
        // kho sẽ bị phình lên một cách vô căn cứ.
        boolean daTruKho = HoaDonStatus.isStockDeducted(currentStatus);
        if (daTruKho) {
            restoreStockForHoaDon(hoaDon);
        }

        hoaDon.setTrangThai(HoaDonStatus.DA_HUY);
        hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        hoaDonRepository.save(hoaDon);

        String actorLabel = byCustomer ? "Khách hàng tự hủy đơn" : "Nhân viên hủy đơn";
        String moTaKho = daTruKho ? " - Đã hoàn lại tồn kho" : " - Đơn chưa xác nhận nên không phải hoàn kho";
        logStatusChange(hoaDon, idNhanVien, actorLabel + moTaKho, "Lý do: " + lyDo.trim());

        sendCancelEmailSafely(hoaDon, lyDo.trim(), byCustomer);

        return hoaDonMapper.mapToDto(hoaDon);
    }

    @Override
    @Transactional
    public synchronized HoaDonDTOResponse requestReturn(Integer id, Integer idKhachHangActor, String lyDo) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại hoặc đã bị xóa"));

        if (idKhachHangActor == null || hoaDon.getIdKhachHang() == null || !hoaDon.getIdKhachHang().getId().equals(idKhachHangActor)) {
            throw new RuntimeException("Bạn không có quyền yêu cầu hoàn hàng cho đơn này");
        }
        if (lyDo == null || lyDo.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập lý do hoàn hàng");
        }
        if (hoaDon.getTrangThai() == null || !HoaDonStatus.RETURN_REQUESTABLE_FROM.contains(hoaDon.getTrangThai())) {
            throw new RuntimeException("Chỉ có thể yêu cầu hoàn hàng khi đơn đã giao thành công");
        }

        hoaDon.setTrangThai(HoaDonStatus.YEU_CAU_HOAN_HANG);
        hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        hoaDonRepository.save(hoaDon);

        logStatusChange(hoaDon, null, "Khách hàng yêu cầu hoàn hàng - Lý do: " + lyDo.trim());

        sendReturnRequestedEmailSafely(hoaDon, lyDo.trim());

        return hoaDonMapper.mapToDto(hoaDon);
    }

    @Override
    @Transactional
    public synchronized HoaDonDTOResponse confirmReturn(Integer id, Integer idNhanVien, String ghiChu) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại hoặc đã bị xóa"));

        if (hoaDon.getTrangThai() == null || !HoaDonStatus.RETURN_CONFIRMABLE_FROM.contains(hoaDon.getTrangThai())) {
            throw new RuntimeException("Không thể xác nhận hoàn hàng cho đơn ở trạng thái \"" + HoaDonStatus.toLabel(hoaDon.getTrangThai()) + "\"");
        }

        // Hàng đã thực sự về kho -> cộng lại tồn kho ngay tại đây.
        restoreStockForHoaDon(hoaDon);

        hoaDon.setTrangThai(HoaDonStatus.DA_HOAN_HANG);
        hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        hoaDonRepository.save(hoaDon);

        String hanhDong = "Admin xác nhận đã nhận hàng hoàn về kho, đã cộng lại tồn kho";
        if (ghiChu != null && !ghiChu.trim().isEmpty()) {
            hanhDong += " - Ghi chú: " + ghiChu.trim();
        }
        logStatusChange(hoaDon, idNhanVien, hanhDong);

        sendReturnConfirmedEmailSafely(hoaDon);

        return hoaDonMapper.mapToDto(hoaDon);
    }

    @Override
    @Transactional
    public synchronized HoaDonDTOResponse rejectReturn(Integer id, Integer idNhanVien, String lyDo) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại hoặc đã bị xóa"));

        if (hoaDon.getTrangThai() == null || hoaDon.getTrangThai() != HoaDonStatus.YEU_CAU_HOAN_HANG) {
            throw new RuntimeException("Chỉ có thể từ chối yêu cầu đang ở trạng thái \"Yêu cầu hoàn hàng\"");
        }
        if (lyDo == null || lyDo.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập lý do từ chối");
        }

        hoaDon.setTrangThai(HoaDonStatus.DA_HOAN_THANH);
        hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        hoaDonRepository.save(hoaDon);

        logStatusChange(hoaDon, idNhanVien, "Admin từ chối yêu cầu hoàn hàng - Lý do: " + lyDo.trim());

        sendReturnRejectedEmailSafely(hoaDon, lyDo.trim());

        return hoaDonMapper.mapToDto(hoaDon);
    }

    @Override
    @Transactional
    public synchronized HoaDonDTOResponse markDeliveryFailed(Integer id, Integer idNhanVien, String lyDo) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại hoặc đã bị xóa"));
        if (hoaDon.getTrangThai() == null || hoaDon.getTrangThai() != HoaDonStatus.DANG_VAN_CHUYEN) {
            throw new RuntimeException("Chỉ đơn đang vận chuyển mới có thể chuyển sang Giao thất bại");
        }
        if (lyDo == null || lyDo.trim().length() < 5) {
            throw new RuntimeException("Vui lòng nhập lý do giao thất bại (ít nhất 5 ký tự)");
        }
        hoaDon.setTrangThai(HoaDonStatus.GIAO_THAT_BAI);
        hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        hoaDonRepository.save(hoaDon);
        logStatusChange(hoaDon, idNhanVien, "Giao hàng thất bại", "Lý do: " + lyDo.trim());
        return hoaDonMapper.mapToDto(hoaDon);
    }

    @Override
    @Transactional
    public synchronized HoaDonDTOResponse restockFailedDelivery(Integer id, Integer idNhanVien, String ghiChu) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại hoặc đã bị xóa"));
        if (hoaDon.getTrangThai() == null || hoaDon.getTrangThai() != HoaDonStatus.GIAO_THAT_BAI) {
            throw new RuntimeException("Chỉ đơn giao thất bại mới có thể hoàn kho");
        }
        // Hàng đã quay về kho -> cộng lại tồn kho, trừ lại số lượng đã bán.
        restoreStockForHoaDon(hoaDon);
        hoaDon.setTrangThai(HoaDonStatus.HOAN_KHO);
        hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        hoaDonRepository.save(hoaDon);
        logStatusChange(hoaDon, idNhanVien, "Hàng giao thất bại đã về kho - Đã cộng lại tồn kho", ghiChu);
        return hoaDonMapper.mapToDto(hoaDon);
    }

    /**
     * Xóa toàn bộ hóa đơn chờ tại quầy (trạng thái 0). Hóa đơn chờ chưa từng trừ kho nên chỉ
     * dọn dữ liệu. Được gọi bởi job hằng ngày (cấu hình cron trong application.properties).
     */
    @Override
    @Transactional
    public int deleteAllPendingInvoices() {
        int count = 0;
        for (HoaDon hd : hoaDonRepository.findPendingInvoicesForSales()) {
            deletePendingInvoice(hd.getId());
            count++;
        }
        return count;
    }

    @Override
    public byte[] exportHoaDonExcel(String keyword, Long minAmount, Long maxAmount, Timestamp startDate, Timestamp endDate, Short trangThai, String loaiDon) {
        return excelExportService.exportHoaDonList(keyword, minAmount, maxAmount, startDate, endDate, trangThai, loaiDon);
    }

}
