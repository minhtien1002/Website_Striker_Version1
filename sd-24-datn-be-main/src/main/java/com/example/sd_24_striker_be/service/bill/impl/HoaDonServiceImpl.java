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
import com.example.sd_24_striker_be.modal.account.NhanVien;
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
    @org.springframework.context.annotation.Lazy
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

        if (hoaDon.getDeleted()) {
            hoaDon.setDeleted(false);
        }

        boolean wasDeducted = HoaDonStatus.isStockDeducted(currentStatus);
        boolean willBeDeducted = HoaDonStatus.isStockDeducted(trangThai);

        NhanVien nhanVienThucHien = idNhanVien != null
                ? nhanVienRepository.findById(idNhanVien)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"))
                : null;

        if (currentStatus != null && currentStatus == HoaDonStatus.CHO_XAC_NHAN && nhanVienThucHien != null) {
            hoaDon.setIdNhanVien(nhanVienThucHien);
        }

        if (!wasDeducted && willBeDeducted) {
            // Kiểm tra và trừ kho
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
        lichSuHoaDon.setIdNhanVien(nhanVienThucHien);
        lichSuHoaDon.setIdHoaDon(hoaDon);
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

    @Override
    public List<HoaDon> getPendingInvoicesForSales() {
        return hoaDonRepository.findPendingInvoicesForSales();
    }

    @Override
    @Transactional
    public HoaDon createPendingInvoice(Integer idNhanVien) {
        HoaDon newInvoice = new HoaDon();

        newInvoice.setTrangThai((short) 0);
        newInvoice.setDeleted(true);
        newInvoice.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        newInvoice.setNgayTao(new Timestamp(System.currentTimeMillis()));

        newInvoice.setTienSanPham(BigDecimal.ZERO);
        newInvoice.setPhiVanChuyen(BigDecimal.ZERO);
        newInvoice.setTongTien(BigDecimal.ZERO);
        newInvoice.setTongTienSauGiam(BigDecimal.ZERO);

        newInvoice.setTenKhachHang("Khách lẻ");
        newInvoice.setDiaChiKhachHang("");
        newInvoice.setSoDienThoaiKhachHang("");
        newInvoice.setLoaiDon("Tại quầy");

        newInvoice.setIdKhachHang(getOrCreateKhachLe());

        newInvoice.setIdNhanVien(idNhanVien != null
                ? nhanVienRepository.findById(idNhanVien).orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"))
                : nhanVienRepository.findById(1).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên mặc định")));

        HoaDon savedInvoice = hoaDonRepository.save(newInvoice);
        hoaDonRepository.flush();

        HoaDon reloadedInvoice = hoaDonRepository.findById(savedInvoice.getId())
                .orElseThrow(() -> new RuntimeException("Failed to reload saved invoice"));

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

        if ("Khách lẻ".equals(request.getTenKhachHang())) {
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

        invoice.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

        HoaDon savedInvoice = hoaDonRepository.save(invoice);

        gioHangService.updateCartCustomer(id, request.getKhachHangId());

        return savedInvoice;
    }

    @Override
    @Transactional
    public void deletePendingInvoice(Integer id) {
        try {
            HoaDon invoice = hoaDonRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            if (invoice.getTrangThai() != 0) {
                throw new RuntimeException("Chỉ có thể xóa hóa đơn đang chờ");
            }

            List<HoaDonChiTiet> cartItems = hoaDonChiTietRepository.findAllByHoaDonId(id);
            if (!cartItems.isEmpty()) {
                hoaDonChiTietRepository.deleteAll(cartItems);
            }

            gioHangRepository.findByHoaDonId(id).ifPresent(gioHang -> {
                List<GioHangChiTiet> gioHangChiTietList = gioHangChiTietRepository.findByGioHangId(gioHang.getId());
                if (!gioHangChiTietList.isEmpty()) {
                    gioHangChiTietRepository.deleteAll(gioHangChiTietList);
                }
                gioHangRepository.delete(gioHang);
            });

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

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(paymentRequest.getHoaDonId())
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            if (hoaDon.getTrangThai() != 0) {
                return PaymentResponse.error("Hóa đơn đã được thanh toán hoặc không ở trạng thái chờ");
            }

            Optional<GioHang> gioHangTinhTien = gioHangRepository.findByHoaDonId(hoaDon.getId());
            if (gioHangTinhTien.isEmpty()) {
                return PaymentResponse.error("Không tìm thấy giỏ hàng cho hóa đơn này");
            }
            List<GioHangChiTiet> dongGioHang = gioHangChiTietRepository.findByGioHangId(gioHangTinhTien.get().getId());
            if (dongGioHang.isEmpty()) {
                return PaymentResponse.error("Giỏ hàng trống, không thể thanh toán");
            }

            // 1. KIỂM TRA TỒN KHO TRƯỚC KHI THANH TOÁN
            // Đảm bảo rằng tất cả sản phẩm trong giỏ đều đủ hàng
            for (GioHangChiTiet dong : dongGioHang) {
                ChiTietSanPham ctsp = dong.getIdChiTietSp();
                if (ctsp.getSoLuongTonKho() < dong.getSoLuong()) {
                    return PaymentResponse.error("Sản phẩm '" + ctsp.getIdSanPham().getTenSanPham()
                            + "' không đủ tồn kho (Chỉ còn " + ctsp.getSoLuongTonKho() + ")");
                }
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
            }
            BigDecimal tongTien = tienSanPham;
            BigDecimal tongTienSauGiam = tongTien.subtract(tienGiam);

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
            paymentRequest.setTienMat(tienMat.subtract(tienThua));
            paymentRequest.setTienChuyenKhoan(tienChuyenKhoan);

            hoaDon.setTrangThai(HoaDonStatus.DA_HOAN_THANH);
            hoaDon.setDeleted(false);
            hoaDon.setTienSanPham(tienSanPham);
            hoaDon.setPhiVanChuyen(BigDecimal.ZERO);
            hoaDon.setTongTien(tongTien);
            hoaDon.setTongTienSauGiam(tongTienSauGiam);
            java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
            hoaDon.setUpdatedAt(now);
            hoaDon.setNgayThanhToan(now);

            hoaDon.setIdNhanVien(nhanVienRepository.findById(paymentRequest.getNhanVienId())
                    .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại")));

            if (phieuGiamGia != null) {
                hoaDon.setIdPhieuGiamGia(phieuGiamGia);
            }

            // 2. LƯU CHI TIẾT HÓA ĐƠN VÀ TRỪ KHO
            for (GioHangChiTiet cartItem : dongGioHang) {
                ChiTietSanPham chiTietSanPham = cartItem.getIdChiTietSp();
                HoaDonChiTiet hoaDonChiTiet = HoaDonChiTiet.builder()
                        .idHoaDon(hoaDon)
                        .idChiTietSp(chiTietSanPham)
                        .gia(cartItem.getGia())
                        .soLuong(cartItem.getSoLuong())
                        .trangThai((short) 1)
                        .build();
                hoaDonChiTietRepository.save(hoaDonChiTiet);
            }
            hoaDonChiTietRepository.flush();

            // Gọi hàm trừ kho (đã được kiểm tra ở trên)
            deductStockForHoaDon(hoaDon);

            // 3. DỌN DẸP GIỎ HÀNG
            gioHangChiTietRepository.deleteAll(dongGioHang);
            gioHangRepository.delete(gioHangTinhTien.get());

            // 4. LƯU HÌNH THỨC THANH TOÁN
            if (paymentRequest.getTienMat().signum() > 0 || "Tiền mặt".equals(phuongThuc)) {
                luuHinhThucThanhToan(hoaDon, "Tiền mặt", paymentRequest.getTienMat(), BigDecimal.ZERO);
            }
            if (paymentRequest.getTienChuyenKhoan().signum() > 0) {
                luuHinhThucThanhToan(hoaDon, "VnPay".equalsIgnoreCase(phuongThuc) ? "VNPay" : "Chuyển khoản",
                        BigDecimal.ZERO, paymentRequest.getTienChuyenKhoan());
            }

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
            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
            sendOrderConfirmationEmailSafely(savedHoaDon);

            return PaymentResponse.success(
                    savedHoaDon.getId(),
                    savedHoaDon.getMa(),
                    tongTienSauGiam,
                    tienThua,
                    paymentRequest.getPhuongThucThanhToan()
            );

        } catch (Exception e) {
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

    private void markRollbackOnly() {
        try {
            org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus().setRollbackOnly();
        } catch (IllegalStateException ignored) {
        }
    }

    @Override
    @Transactional
    public void updateInvoiceStatusAfterPayment(Integer invoiceId, Integer status) {
        // ... (Giữ nguyên phần code này của bạn) ...
        try {
            HoaDon hoaDon = hoaDonRepository.findById(invoiceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + invoiceId));

            if (status == 1) {
                hoaDon.setTrangThai((short) 5);
                hoaDon.setDeleted(false);
                java.sql.Timestamp paidAt = new java.sql.Timestamp(System.currentTimeMillis());
                hoaDon.setUpdatedAt(paidAt);
                hoaDon.setNgayThanhToan(paidAt);

                Optional<GioHang> gioHangOpt = gioHangRepository.findByHoaDonId(hoaDon.getId());
                if (gioHangOpt.isPresent()) {
                    List<GioHangChiTiet> gioHangChiTietList = gioHangChiTietRepository.findByGioHangId(gioHangOpt.get().getId());

                    BigDecimal tongTien = BigDecimal.ZERO;

                    for (GioHangChiTiet cartItem : gioHangChiTietList) {
                        ChiTietSanPham chiTietSanPham = cartItem.getIdChiTietSp();

                        HoaDonChiTiet hoaDonChiTiet = HoaDonChiTiet.builder()
                                .idHoaDon(hoaDon)
                                .idChiTietSp(chiTietSanPham)
                                .gia(cartItem.getGia())
                                .soLuong(cartItem.getSoLuong())
                                .trangThai((short) 1)
                                .build();

                        hoaDonChiTietRepository.save(hoaDonChiTiet);

                        tongTien = tongTien.add(cartItem.getGia().multiply(BigDecimal.valueOf(cartItem.getSoLuong())));
                    }

                    hoaDon.setTienSanPham(tongTien);
                    hoaDon.setTongTien(tongTien);
                    hoaDon.setTongTienSauGiam(tongTien);

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
        // ... (Giữ nguyên phần code này của bạn, tôi đã kiểm tra nó ổn) ...
        try {
            HoaDon hoaDon = hoaDonRepository.findById(invoiceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + invoiceId));

            if (status == 1) {
                BigDecimal expectedAmount = calculatePendingInvoiceTotal(invoiceId);
                BigDecimal callbackAmount = BigDecimal.valueOf(vnpayAmount);
                if (expectedAmount.compareTo(callbackAmount) != 0) {
                    throw new RuntimeException("Số tiền VNPay không khớp tổng hóa đơn");
                }
                hoaDon.setTrangThai((short) 5);
                hoaDon.setDeleted(false);
                java.sql.Timestamp vnpayPaidAt = new java.sql.Timestamp(System.currentTimeMillis());
                hoaDon.setUpdatedAt(vnpayPaidAt);
                hoaDon.setNgayThanhToan(vnpayPaidAt);

                Optional<GioHang> gioHangOpt = gioHangRepository.findByHoaDonId(hoaDon.getId());
                if (gioHangOpt.isPresent()) {
                    List<GioHangChiTiet> gioHangChiTietList = gioHangChiTietRepository.findByGioHangId(gioHangOpt.get().getId());

                    for (GioHangChiTiet cartItem : gioHangChiTietList) {
                        ChiTietSanPham chiTietSanPham = cartItem.getIdChiTietSp();

                        HoaDonChiTiet hoaDonChiTiet = HoaDonChiTiet.builder()
                                .idHoaDon(hoaDon)
                                .idChiTietSp(chiTietSanPham)
                                .gia(cartItem.getGia())
                                .soLuong(cartItem.getSoLuong())
                                .trangThai((short) 1)
                                .build();

                        hoaDonChiTietRepository.save(hoaDonChiTiet);
                    }

                    hoaDonChiTietRepository.flush();
                    deductStockForHoaDon(hoaDon);

                    BigDecimal vnpayAmountBD = expectedAmount;
                    BigDecimal tienSanPham = gioHangChiTietList.stream()
                            .map(item -> item.getGia().multiply(BigDecimal.valueOf(item.getSoLuong())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    hoaDon.setTienSanPham(tienSanPham);
                    hoaDon.setPhiVanChuyen(BigDecimal.ZERO);
                    hoaDon.setTongTien(tienSanPham);
                    hoaDon.setTongTienSauGiam(vnpayAmountBD);

                    PhuongThucThanhToan vnpayMethod = phuongThucThanhToanRepository
                            .findByKieuThanhToan("VnPay")
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán VnPay"));

                    HinhThucThanhToan hinhThucThanhToan = HinhThucThanhToan.builder()
                            .idHoaDon(hoaDon)
                            .idPhuongThucThanhToan(vnpayMethod)
                            .tienMat(BigDecimal.ZERO)
                            .tienChuyenKhoan(vnpayAmountBD)
                            .deleted(false)
                            .build();

                    hinhThucThanhToanRepository.save(hinhThucThanhToan);

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

    @Transactional(readOnly = true)
    public BigDecimal calculatePendingInvoiceTotal(Integer invoiceId) {
        // ... (Giữ nguyên code) ...
        HoaDon hoaDon = hoaDonRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + invoiceId));
        if (hoaDon.getTrangThai() == null || hoaDon.getTrangThai() != HoaDonStatus.HOA_DON_CHO) {
            throw new RuntimeException("Hóa đơn không ở trạng thái chờ thanh toán");
        }
        GioHang gioHang = gioHangRepository.findByHoaDonId(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng cho hóa đơn này"));
        List<GioHangChiTiet> items = gioHangChiTietRepository.findByGioHangId(gioHang.getId());
        if (items.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể thanh toán");
        }
        BigDecimal subtotal = items.stream()
                .map(item -> item.getGia().multiply(BigDecimal.valueOf(item.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = BigDecimal.ZERO;
        if (hoaDon.getIdPhieuGiamGia() != null) {
            String validationError = VoucherCalculator.validate(hoaDon.getIdPhieuGiamGia(), subtotal);
            if (validationError != null) {
                throw new RuntimeException(validationError);
            }
            phieuGiamGiaServices.kiemTraQuyenSuDung(hoaDon.getIdPhieuGiamGia(), hoaDon.getIdKhachHang());
            discount = VoucherCalculator.discountAmount(hoaDon.getIdPhieuGiamGia(), subtotal);
        }
        BigDecimal total = subtotal.subtract(discount);
        if (total.signum() <= 0) {
            throw new RuntimeException("Số tiền thanh toán phải lớn hơn 0");
        }
        return total;
    }

    @Override
    @Transactional
    public void processCashPayment(Integer invoiceId, BigDecimal cashAmount) {
        // ... (Giữ nguyên code) ...
        try {
            HoaDon hoaDon = hoaDonRepository.findById(invoiceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + invoiceId));

            PhuongThucThanhToan cashPaymentMethod = phuongThucThanhToanRepository.findByKieuThanhToan("Tiền mặt")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán tiền mặt"));

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

        for (Map.Entry<Integer, Integer> entry : soLuongTheoBienThe.entrySet()) {
            ChiTietSanPham bienThe = bienTheById.get(entry.getKey());
            int tonKho = bienThe.getSoLuongTonKho() != null ? bienThe.getSoLuongTonKho() : 0;
            if (tonKho < entry.getValue()) {
                throw new RuntimeException("Không đủ tồn kho để xác nhận đơn hàng. Sản phẩm \""
                        + moTaBienThe(bienThe) + "\" cần " + entry.getValue() + " nhưng chỉ còn " + tonKho);
            }
        }

        phieuGiamGiaServices.truLuotSuDung(hoaDon.getIdPhieuGiamGia());

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

    private String moTaBienThe(ChiTietSanPham bienThe) {
        // ... (Giữ nguyên code) ...
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
        // ... (Giữ nguyên code) ...
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

    private void releaseReservedCartStock(HoaDon hoaDon) {
        // ... (Giữ nguyên code) ...
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
        // ... (Giữ nguyên code) ...
        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setGhiChu(ghiChu != null && !ghiChu.isBlank() ? ghiChu.trim() : null);
        lichSu.setMa("LSHD_" + System.currentTimeMillis());
        lichSu.setHanhDong(hanhDong);
        lichSu.setThoiGian(new java.sql.Timestamp(System.currentTimeMillis()));
        lichSu.setIdNhanVien(idNhanVien != null ? nhanVienRepository.findById(idNhanVien).orElse(null) : null);
        lichSu.setIdHoaDon(hoaDon);
        lichSu.setDeleted(hoaDon.getTrangThai());
        lichSuHoaDonRepository.save(lichSu);
    }

    private void sendOrderConfirmationEmailSafely(HoaDon hoaDon) {
        // ... (Giữ nguyên code) ...
        try {
            if (hoaDon.getEmail() != null && !hoaDon.getEmail().isBlank()) {
                emailService.sendOrderConfirmationEmail(hoaDon, hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId()));
            }
        } catch (Exception e) {
            logger.warn("Không thể gửi email xác nhận đơn hàng {}: {}", hoaDon.getMa(), e.getMessage());
        }
    }

    private void sendCancelEmailSafely(HoaDon hoaDon, String lyDo, boolean byCustomer) {
        // ... (Giữ nguyên code) ...
        try {
            if (hoaDon.getEmail() != null && !hoaDon.getEmail().isBlank()) {
                emailService.sendOrderCancelledEmail(hoaDon, lyDo, byCustomer);
            }
        } catch (Exception e) {
            logger.warn("Không thể gửi email hủy đơn {}: {}", hoaDon.getMa(), e.getMessage());
        }
    }

    private void sendReturnRequestedEmailSafely(HoaDon hoaDon, String lyDo) {
        // ... (Giữ nguyên code) ...
        try {
            if (hoaDon.getEmail() != null && !hoaDon.getEmail().isBlank()) {
                emailService.sendReturnRequestedEmail(hoaDon, lyDo);
            }
        } catch (Exception e) {
            logger.warn("Không thể gửi email yêu cầu hoàn hàng {}: {}", hoaDon.getMa(), e.getMessage());
        }
    }

    private void sendReturnConfirmedEmailSafely(HoaDon hoaDon) {
        // ... (Giữ nguyên code) ...
        try {
            if (hoaDon.getEmail() != null && !hoaDon.getEmail().isBlank()) {
                emailService.sendReturnConfirmedEmail(hoaDon);
            }
        } catch (Exception e) {
            logger.warn("Không thể gửi email xác nhận hoàn hàng {}: {}", hoaDon.getMa(), e.getMessage());
        }
    }

    private void sendReturnRejectedEmailSafely(HoaDon hoaDon, String lyDo) {
        // ... (Giữ nguyên code) ...
        try {
            if (hoaDon.getEmail() != null && !hoaDon.getEmail().isBlank()) {
                emailService.sendReturnRejectedEmail(hoaDon, lyDo);
            }
        } catch (Exception e) {
            logger.warn("Không thể gửi email từ chối hoàn hàng {}: {}", hoaDon.getMa(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public synchronized HoaDonDTOResponse cancelOrder(Integer id, Integer idNhanVien, Integer idKhachHangActor, String lyDo) {
        // ... (Giữ nguyên code) ...
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
        // ... (Giữ nguyên code) ...
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
    public synchronized void yeuCauHoanHang(Integer id, Long idKhachHangActor, String lyDo) {
        Integer actorId = (idKhachHangActor != null) ? idKhachHangActor.intValue() : null;
        requestReturn(id, actorId, lyDo);
    }

    @Override
    @Transactional
    public synchronized HoaDonDTOResponse confirmReturn(Integer id, Integer idNhanVien, String ghiChu) {
        // ... (Giữ nguyên code) ...
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại hoặc đã bị xóa"));

        if (hoaDon.getTrangThai() == null || !HoaDonStatus.RETURN_CONFIRMABLE_FROM.contains(hoaDon.getTrangThai())) {
            throw new RuntimeException("Không thể xác nhận hoàn hàng cho đơn ở trạng thái \"" + HoaDonStatus.toLabel(hoaDon.getTrangThai()) + "\"");
        }

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
        // ... (Giữ nguyên code) ...
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
        // ... (Giữ nguyên code) ...
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
        // ... (Giữ nguyên code) ...
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại hoặc đã bị xóa"));
        if (hoaDon.getTrangThai() == null || hoaDon.getTrangThai() != HoaDonStatus.GIAO_THAT_BAI) {
            throw new RuntimeException("Chỉ đơn giao thất bại mới có thể hoàn kho");
        }
        restoreStockForHoaDon(hoaDon);
        hoaDon.setTrangThai(HoaDonStatus.HOAN_KHO);
        hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        hoaDonRepository.save(hoaDon);
        logStatusChange(hoaDon, idNhanVien, "Hàng giao thất bại đã về kho - Đã cộng lại tồn kho", ghiChu);
        return hoaDonMapper.mapToDto(hoaDon);
    }

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

    @Override
    @Transactional
    public void huyDonHang(Integer id, String lyDo) {
        cancelOrder(id, null, null, lyDo);
    }

    @Override
    @Transactional
    public void muaLaiDonHang(Integer idHoaDon, Integer idKhachHang) {
        // ... (Giữ nguyên code) ...
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));

        if (hoaDon.getIdKhachHang() == null || !hoaDon.getIdKhachHang().getId().equals(idKhachHang)) {
            throw new RuntimeException("Bạn không có quyền mua lại đơn hàng này!");
        }

        List<HoaDonChiTiet> listChiTiet = hoaDonChiTietRepository.findByHoaDonId(idHoaDon);
        if (listChiTiet == null || listChiTiet.isEmpty()) {
            throw new RuntimeException("Đơn hàng không có sản phẩm để mua lại!");
        }

        Optional<GioHang> existingGioHangOpt = gioHangRepository.findByIdKhachHang(idKhachHang);
        GioHang gioHang;

        if (existingGioHangOpt.isPresent()) {
            gioHang = existingGioHangOpt.get();
        } else {
            gioHang = new GioHang();
            gioHang.setIdKhachHang(khachHangRepository.findById(idKhachHang)
                    .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại!")));
            gioHang.setTongTien(BigDecimal.ZERO);
            gioHang.setCreatedAt(java.time.LocalDateTime.now());
            gioHang.setUpdatedAt(java.time.LocalDateTime.now());
            gioHang = gioHangRepository.save(gioHang);
        }

        List<GioHangChiTiet> currentCartItems = gioHangChiTietRepository.findByGioHangId(gioHang.getId());
        int soLuongThemDuoc = 0;

        for (HoaDonChiTiet item : listChiTiet) {
            if (Boolean.TRUE.equals(item.getDeleted())) continue;

            ChiTietSanPham ctsp = item.getIdChiTietSp();

            if (ctsp == null || Boolean.TRUE.equals(ctsp.getDeleted()) || ctsp.getSoLuongTonKho() <= 0) {
                continue;
            }

            Optional<GioHangChiTiet> existingItemOpt = currentCartItems.stream()
                    .filter(ghct -> ghct.getIdChiTietSp().getId().equals(ctsp.getId()))
                    .findFirst();

            if (existingItemOpt.isPresent()) {
                GioHangChiTiet ghct = existingItemOpt.get();
                int newQty = ghct.getSoLuong() + item.getSoLuong();
                if (newQty > ctsp.getSoLuongTonKho()) {
                    newQty = ctsp.getSoLuongTonKho();
                }
                ghct.setSoLuong(newQty);
                ghct.setThanhTien(ctsp.getGiaBan().multiply(BigDecimal.valueOf(newQty)));
                gioHangChiTietRepository.save(ghct);
                soLuongThemDuoc++;
            } else {
                GioHangChiTiet newGhct = new GioHangChiTiet();
                newGhct.setIdGioHang(gioHang);
                newGhct.setIdChiTietSp(ctsp);
                newGhct.setSoLuong(Math.min(item.getSoLuong(), ctsp.getSoLuongTonKho()));
                newGhct.setGia(ctsp.getGiaBan());
                newGhct.setThanhTien(ctsp.getGiaBan().multiply(BigDecimal.valueOf(newGhct.getSoLuong())));
                newGhct.setCreatedAt(java.time.LocalDateTime.now());
                gioHangChiTietRepository.save(newGhct);
                soLuongThemDuoc++;
            }
        }

        if (soLuongThemDuoc == 0) {
            throw new RuntimeException("Tất cả sản phẩm trong đơn hàng này đã hết hàng hoặc ngừng kinh doanh, không thể mua lại!");
        }

        List<GioHangChiTiet> updatedCartItems = gioHangChiTietRepository.findByGioHangId(gioHang.getId());
        BigDecimal tongTien = updatedCartItems.stream()
                .map(GioHangChiTiet::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        gioHang.setTongTien(tongTien);
        gioHang.setUpdatedAt(java.time.LocalDateTime.now());
        gioHangRepository.save(gioHang);
    }
}