package com.example.sd_24_striker_be.service.sale.Client.impl;


import com.example.sd_24_striker_be.common.sale.VoucherCalculator;
import com.example.sd_24_striker_be.dto.sell.request.PaymentRequest;
import com.example.sd_24_striker_be.service.StockUpdateService;
import com.example.sd_24_striker_be.dto.bill.response.HoaDonDetailResponse;
import com.example.sd_24_striker_be.dto.sell.request.AddToCartRequest;
import com.example.sd_24_striker_be.dto.sell.request.ClientPaymentRequest;
import com.example.sd_24_striker_be.dto.sell.request.UpdateCartItemRequest;
import com.example.sd_24_striker_be.dto.sell.response.CartItemResponse;
import com.example.sd_24_striker_be.dto.sell.response.CartResponse;
import com.example.sd_24_striker_be.exception.ResourceNotFoundException;
import com.example.sd_24_striker_be.modal.account.KhachHang;
import com.example.sd_24_striker_be.modal.account.NhanVien;
import com.example.sd_24_striker_be.modal.bill.HoaDon;
import com.example.sd_24_striker_be.modal.bill.HoaDonChiTiet;
import com.example.sd_24_striker_be.modal.bill.LichSuHoaDon;
import com.example.sd_24_striker_be.modal.product.ChiTietSanPham;
import com.example.sd_24_striker_be.modal.sell.GioHang;
import com.example.sd_24_striker_be.modal.sell.GioHangChiTiet;
import com.example.sd_24_striker_be.modal.sell.HinhThucThanhToan;
import com.example.sd_24_striker_be.modal.sell.PhuongThucThanhToan;
import com.example.sd_24_striker_be.modal.sale.PhieuGiamGia;
import com.example.sd_24_striker_be.repository.account.KhachHang.KhachHangRepository;
import com.example.sd_24_striker_be.repository.account.NhanVien.NhanVienRepository;
import com.example.sd_24_striker_be.repository.bill.HoaDonChiTietRepository;
import com.example.sd_24_striker_be.repository.bill.HoaDonRepository;
import com.example.sd_24_striker_be.repository.bill.LichSuHoaDonRepository;
import com.example.sd_24_striker_be.repository.product.ChiTietSanPhamRepository;
import com.example.sd_24_striker_be.repository.sell.GioHangChiTietRepository;
import com.example.sd_24_striker_be.repository.sell.GioHangRepository;
import com.example.sd_24_striker_be.repository.sell.HinhThucThanhToanRepository;
import com.example.sd_24_striker_be.repository.sell.PhuongThucThanhToanRepository;
import com.example.sd_24_striker_be.repository.sale.PhieuGiamGia.PhieuGiamGiaRepository;
import com.example.sd_24_striker_be.service.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BanHangClientServiceImpl implements BanHangClientService{

    private static final Logger logger = LoggerFactory.getLogger(BanHangClientServiceImpl.class);

    @Autowired
    private com.example.sd_24_striker_be.service.sell.PhiVanChuyenService phiVanChuyenService;

    @Autowired
    private com.example.sd_24_striker_be.service.product.ChiTietSanPhamService chiTietSanPhamService;

    @Autowired
    private com.example.sd_24_striker_be.service.sale.impl.PhieuGiamGia.PhieuGiamGiaServices phieuGiamGiaServices;


    @Autowired
    private GioHangRepository gioHangRepository;

    @Autowired
    private GioHangChiTietRepository gioHangChiTietRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private StockUpdateService stockUpdateService;

    @Autowired
    private HinhThucThanhToanRepository hinhThucThanhToanRepository;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired
    private EmailService emailService;

    private void sendOrderConfirmationEmailSafely(HoaDon hoaDon, List<HoaDonChiTiet> items) {
        try {
            if (hoaDon.getEmail() != null && !hoaDon.getEmail().isBlank()) {
                emailService.sendOrderConfirmationEmail(hoaDon, items);
            }
        } catch (Exception e) {
            logger.warn("Không thể gửi email xác nhận đơn hàng {}: {}", hoaDon.getMa(), e.getMessage());
        }
    }

    private String generateRandomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return "HD_CLIENT_" + code;
    }

    private BigDecimal tinhPhiVanChuyen(HoaDon hoaDon, List<GioHangChiTiet> cartItems) {
        int soLuong = cartItems.stream().mapToInt(i -> i.getSoLuong() == null ? 0 : i.getSoLuong()).sum();
        return phiVanChuyenService.tinhPhi(hoaDon.getDiaChiKhachHang(), soLuong).phiVanChuyen();
    }

    @Override
    public HoaDonDetailResponse taoHoaDonCho(Integer khachHangId) {
        System.out.println("=== Creating pending invoice ===");
        System.out.println("Received khachHangId: " + khachHangId);

        // Get customer or use default guest customer
        KhachHang khachHang;
        boolean isGuest = khachHangId == null;
        if (!isGuest) {
            khachHang = khachHangRepository.findById(khachHangId)
                    .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại với ID: " + khachHangId));
            System.out.println("Found customer: " + khachHang.getTen() + " (ID: " + khachHang.getId() + ")");
        } else {
            khachHang = khachHangRepository.findFirstByMa(KhachHangRepository.MA_KHACH_LE)
                    .orElseGet(() -> khachHangRepository.save(KhachHang.builder()
                            .ma(KhachHangRepository.MA_KHACH_LE)
                            .ten("Khách lẻ")
                            .deleted(true)
                            .createdAt(java.time.Instant.now())
                            .build()));
        }

        if (!isGuest) {
            List<HoaDon> existingPendingInvoices = hoaDonRepository.findByIdKhachHangAndTrangThai(khachHang.getId(), (short) 0);
            if (!existingPendingInvoices.isEmpty()) {
                return mapToHoaDonDetailResponse(existingPendingInvoices.get(0));
            }
        }

        // Create new pending invoice
        HoaDon hoaDon = HoaDon.builder()
                .idKhachHang(khachHang)
                .idNhanVien(null)
                .ma(generateRandomCode())
                .tienSanPham(BigDecimal.ZERO)
                .loaiDon("Online")
                .phiVanChuyen(BigDecimal.ZERO)
                .tongTien(BigDecimal.ZERO)
                .tongTienSauGiam(BigDecimal.ZERO)
                .ghiChu("Hóa đơn chờ từ client")
                .tenKhachHang(khachHang.getTen() != null ? khachHang.getTen() : "Khách lẻ")
                .diaChiKhachHang("N/A")
                .soDienThoaiKhachHang(khachHang.getTaiKhoan() != null ? khachHang.getTaiKhoan().getSoDienThoai() : "N/A")
                .email(khachHang.getTaiKhoan() != null ? khachHang.getTaiKhoan().getEmail() : "N/A")
                .ngayTao(new java.sql.Timestamp(System.currentTimeMillis()))
                .trangThai((short) 0) // Pending status
                .deleted(false)
                .createdAt(new java.sql.Timestamp(System.currentTimeMillis()))
                .createdBy(1)
                .build();

        hoaDon = hoaDonRepository.save(hoaDon);

        // Create cart in database
        GioHang gioHang = GioHang.builder()
                .idKhachHang(khachHang)
                .idHoaDon(hoaDon)
                .tongTien(BigDecimal.ZERO)
                .build();
        gioHangRepository.save(gioHang);

        return mapToHoaDonDetailResponse(hoaDon);
    }

    @Override
    public Map<String, Object> tachGioHang(Integer hoaDonId, List<Integer> cartItemIdsThanhToan) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng"));
        if (hoaDon.getTrangThai() == null || hoaDon.getTrangThai() != 0) {
            throw new RuntimeException("Giỏ hàng này đã được đặt hàng");
        }
        if (cartItemIdsThanhToan == null || cartItemIdsThanhToan.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất một sản phẩm để thanh toán");
        }
        GioHang gioHang = gioHangRepository.findByHoaDonId(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng"));
        List<GioHangChiTiet> items = gioHangChiTietRepository.findByGioHangId(gioHang.getId());
        Set<Integer> chon = new HashSet<>(cartItemIdsThanhToan);
        if (items.stream().noneMatch(i -> chon.contains(i.getId()))) {
            throw new RuntimeException("Sản phẩm được chọn không thuộc giỏ hàng này");
        }
        List<GioHangChiTiet> conLai = items.stream().filter(i -> !chon.contains(i.getId())).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("hoaDonThanhToan", hoaDonId);
        result.put("hoaDonConLai", null);
        if (conLai.isEmpty()) {
            return result;
        }

        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        HoaDon moi = hoaDonRepository.save(HoaDon.builder()
                .idKhachHang(hoaDon.getIdKhachHang())
                .idNhanVien(null)
                .ma(generateRandomCode())
                .tienSanPham(BigDecimal.ZERO)
                .loaiDon("Online")
                .phiVanChuyen(BigDecimal.ZERO)
                .tongTien(BigDecimal.ZERO)
                .tongTienSauGiam(BigDecimal.ZERO)
                .ghiChu("Hóa đơn chờ từ client")
                .tenKhachHang(hoaDon.getTenKhachHang())
                .diaChiKhachHang("N/A")
                .soDienThoaiKhachHang(hoaDon.getSoDienThoaiKhachHang())
                .email(hoaDon.getEmail())
                .ngayTao(now)
                .trangThai((short) 0)
                .deleted(false)
                .createdAt(now)
                .createdBy(1)
                .build());
        GioHang gioMoi = gioHangRepository.save(GioHang.builder()
                .idKhachHang(hoaDon.getIdKhachHang())
                .idHoaDon(moi)
                .tongTien(BigDecimal.ZERO)
                .build());
        for (GioHangChiTiet item : conLai) {
            item.setIdGioHang(gioMoi);
            gioHangChiTietRepository.save(item);
        }
        gioHangChiTietRepository.flush();
        updateCartTotal(gioHang.getId());
        updateCartTotal(gioMoi.getId());
        result.put("hoaDonConLai", moi.getId());
        return result;
    }

    @Override
    public CartResponse gopGioHang(Integer tuHoaDonId, Integer vaoHoaDonId) {
        if (tuHoaDonId.equals(vaoHoaDonId)) {
            return layGioHang(vaoHoaDonId);
        }
        HoaDon tu = hoaDonRepository.findById(tuHoaDonId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng"));
        HoaDon vao = hoaDonRepository.findById(vaoHoaDonId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng"));
        if (tu.getTrangThai() != 0 || vao.getTrangThai() != 0) {
            throw new RuntimeException("Chỉ gộp được giỏ hàng chưa đặt");
        }
        GioHang gioTu = gioHangRepository.findByHoaDonId(tuHoaDonId).orElse(null);
        GioHang gioVao = gioHangRepository.findByHoaDonId(vaoHoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng"));
        if (gioTu != null) {
            for (GioHangChiTiet item : gioHangChiTietRepository.findByGioHangId(gioTu.getId())) {
                Optional<GioHangChiTiet> trung = gioHangChiTietRepository
                        .findByGioHangIdAndChiTietSpId(gioVao.getId(), item.getIdChiTietSp().getId());
                if (trung.isPresent()) {
                    GioHangChiTiet t = trung.get();
                    t.setSoLuong(t.getSoLuong() + item.getSoLuong());
                    t.setThanhTien(t.getGia().multiply(BigDecimal.valueOf(t.getSoLuong())));
                    gioHangChiTietRepository.save(t);
                    gioHangChiTietRepository.delete(item);
                } else {
                    item.setIdGioHang(gioVao);
                    gioHangChiTietRepository.save(item);
                }
            }
            gioHangChiTietRepository.flush();
            gioHangRepository.delete(gioTu);
        }
        hoaDonRepository.delete(tu);
        updateCartTotal(gioVao.getId());
        return mapToCartResponse(gioVao);
    }

    @Override
    public List<HoaDonDetailResponse> getPendingInvoicesByCustomer(Integer khachHangId) {
        KhachHang khachHang = khachHangRepository.findById(khachHangId)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại với ID: " + khachHangId));

        List<HoaDon> pendingHoaDons = hoaDonRepository.findByIdKhachHangAndTrangThai(khachHang.getId(), (short) 0);

        return pendingHoaDons.stream()
                .map(this::mapToHoaDonDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CartResponse themSanPhamVaoGioHang(Integer hoaDonId, AddToCartRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn có id: " + hoaDonId));

        if (hoaDon.getTrangThai() != 0) {
            throw new RuntimeException("Hóa đơn này không phải hóa đơn chờ! Trạng thái hiện tại: " + hoaDon.getTrangThai());
        }

        ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(request.getIdChiTietSanPham())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết sản phẩm!"));

        if (request.getSoLuong() == null || request.getSoLuong() <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }
        if (Boolean.TRUE.equals(chiTietSanPham.getDeleted())) {
            throw new RuntimeException("Sản phẩm này đã ngừng bán");
        }

        if (chiTietSanPham.getSoLuongTonKho() < request.getSoLuong()) {
            throw new RuntimeException("Không đủ số lượng tồn kho! Còn lại: " + chiTietSanPham.getSoLuongTonKho());
        }

        BigDecimal gia = chiTietSanPhamService.getEffectivePrice(chiTietSanPham);

        GioHang gioHang = gioHangRepository.findByHoaDonId(hoaDonId)
                .orElseGet(() -> {
                    GioHang newCart = GioHang.builder()
                            .idKhachHang(hoaDon.getIdKhachHang())
                            .idHoaDon(hoaDon)
                            .tongTien(BigDecimal.ZERO)
                            .build();
                    return gioHangRepository.save(newCart);
                });

        Optional<GioHangChiTiet> existingItem = gioHangChiTietRepository
                .findByGioHangIdAndChiTietSpId(gioHang.getId(), request.getIdChiTietSanPham());

        if (existingItem.isPresent()) {
            GioHangChiTiet item = existingItem.get();
            int newQuantity = item.getSoLuong() + request.getSoLuong();

            if (chiTietSanPham.getSoLuongTonKho() < newQuantity) {
                throw new RuntimeException("Không đủ số lượng tồn kho! Còn lại: " + chiTietSanPham.getSoLuongTonKho());
            }

            item.setSoLuong(newQuantity);
            item.setGia(gia);
            item.setThanhTien(gia.multiply(BigDecimal.valueOf(newQuantity)));
            gioHangChiTietRepository.save(item);
        } else {
            GioHangChiTiet newItem = GioHangChiTiet.builder()
                    .idGioHang(gioHang)
                    .idChiTietSp(chiTietSanPham)
                    .soLuong(request.getSoLuong())
                    .gia(gia)
                    .thanhTien(gia.multiply(BigDecimal.valueOf(request.getSoLuong())))
                    .build();
            gioHangChiTietRepository.save(newItem);
        }

        updateCartTotal(gioHang.getId());

        return mapToCartResponse(gioHang);
    }

    @Override
    public CartResponse layGioHang(Integer hoaDonId) {
        GioHang gioHang = gioHangRepository.findByHoaDonId(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng cho hóa đơn ID: " + hoaDonId));
        return mapToCartResponse(gioHang);
    }

    @Override
    public CartResponse capNhatSoLuongSanPham(Integer hoaDonId, Integer cartItemId, UpdateCartItemRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn có id: " + hoaDonId));

        if (hoaDon.getTrangThai() != 0) {
            throw new RuntimeException("Hóa đơn này không phải hóa đơn chờ!");
        }

        GioHangChiTiet cartItem = gioHangChiTietRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng!"));

        if (cartItem.getIdGioHang() == null || cartItem.getIdGioHang().getIdHoaDon() == null
                || !hoaDonId.equals(cartItem.getIdGioHang().getIdHoaDon().getId())) {
            throw new RuntimeException("Sản phẩm không thuộc giỏ hàng này");
        }
        if (request.getSoLuong() == null || request.getSoLuong() <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        ChiTietSanPham chiTietSanPham = cartItem.getIdChiTietSp();
        int newQuantity = request.getSoLuong();

        if (newQuantity > 0 && chiTietSanPham.getSoLuongTonKho() < newQuantity) {
            throw new RuntimeException("Không đủ số lượng tồn kho! Còn lại: " + chiTietSanPham.getSoLuongTonKho());
        }

        cartItem.setSoLuong(newQuantity);
        cartItem.setThanhTien(cartItem.getGia().multiply(BigDecimal.valueOf(newQuantity)));
        gioHangChiTietRepository.save(cartItem);

        updateCartTotal(cartItem.getIdGioHang().getId());

        return mapToCartResponse(cartItem.getIdGioHang());
    }

    @Override
    public CartResponse xoaSanPhamKhoiGioHang(Integer hoaDonId, Integer cartItemId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn có id: " + hoaDonId));

        if (hoaDon.getTrangThai() != 0) {
            throw new RuntimeException("Hóa đơn này không phải hóa đơn chờ!");
        }

        GioHangChiTiet cartItem = gioHangChiTietRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng!"));

        GioHang gioHang = cartItem.getIdGioHang();

        gioHangChiTietRepository.delete(cartItem);

        updateCartTotal(gioHang.getId());

        return mapToCartResponse(gioHang);
    }

    @Override
    public HoaDonDetailResponse thanhToan(Integer hoaDonId, ClientPaymentRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn có id: " + hoaDonId));

        if (hoaDon.getTrangThai() != 0) {
            throw new RuntimeException("Hóa đơn này không phải hóa đơn chờ!");
        }

        GioHang gioHang = gioHangRepository.findByHoaDonId(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng cho hóa đơn ID: " + hoaDonId));

        List<GioHangChiTiet> cartItems = gioHangChiTietRepository.findByGioHangId(gioHang.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể thanh toán!");
        }

        if (request.getTenKhachHang() != null && !request.getTenKhachHang().trim().isEmpty()) {
            hoaDon.setTenKhachHang(request.getTenKhachHang());
        }
        if (request.getSoDienThoai() != null && !request.getSoDienThoai().trim().isEmpty()) {
            hoaDon.setSoDienThoaiKhachHang(request.getSoDienThoai());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            hoaDon.setEmail(request.getEmail());
        }
        if (request.getDiaChi() != null && !request.getDiaChi().trim().isEmpty()) {
            hoaDon.setDiaChiKhachHang(request.getDiaChi());
        }
        if (request.getGhiChu() != null && !request.getGhiChu().trim().isEmpty()) {
            hoaDon.setGhiChu(request.getGhiChu());
        }

        if (request.getVoucherId() != null) {
            PhieuGiamGia voucher = phieuGiamGiaRepository.findById(request.getVoucherId()).orElse(null);
            if (voucher != null) {
                hoaDon.setIdPhieuGiamGia(voucher);
            }
        }

        validateThongTinGiaoHang(hoaDon);

        BigDecimal tongTienSauGiam = tinhLaiTongTien(hoaDon, cartItems);

        hoaDon.setTrangThai((short) 1);
        hoaDon.setNgayThanhToan(new java.sql.Timestamp(System.currentTimeMillis()));
        hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

        hoaDonRepository.save(hoaDon);

        createPaymentMethod(hoaDon, request.getPhuongThucThanhToan(), tongTienSauGiam);

        for (GioHangChiTiet cartItem : cartItems) {
            HoaDonChiTiet hoaDonChiTiet = HoaDonChiTiet.builder()
                    .idHoaDon(hoaDon)
                    .idChiTietSp(cartItem.getIdChiTietSp())
                    .soLuong(cartItem.getSoLuong())
                    .gia(cartItem.getGia())
                    .trangThai((short) 1)
                    .deleted(false)
                    .build();
            hoaDonChiTietRepository.save(hoaDonChiTiet);
        }

        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .idHoaDon(hoaDon)
                .ma("LSHD_" + System.currentTimeMillis())
                .hanhDong("Khách hàng đặt hàng (" + request.getPhuongThucThanhToan() + ") - Chờ xác nhận")
                .thoiGian(new java.sql.Timestamp(System.currentTimeMillis()))
                .deleted(com.example.sd_24_striker_be.common.bill.HoaDonStatus.CHO_XAC_NHAN)
                .build();
        lichSuHoaDonRepository.save(lichSu);

        gioHangChiTietRepository.deleteAll(cartItems);
        gioHangRepository.delete(gioHang);

        sendOrderConfirmationEmailSafely(hoaDon, hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId()));

        return mapToHoaDonDetailResponse(hoaDon);
    }

    public BigDecimal getCartTotal(Integer hoaDonId) {
        try {
            GioHang gioHang = gioHangRepository.findByHoaDonId(hoaDonId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng cho hóa đơn ID: " + hoaDonId));

            return gioHang.getTongTien() != null ? gioHang.getTongTien() : BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("Error getting cart total: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private void createPaymentMethod(HoaDon hoaDon, String paymentMethodType, BigDecimal amount) {
        try {
            PhuongThucThanhToan phuongThuc = findOrCreatePaymentMethodType(paymentMethodType);

            HinhThucThanhToan hinhThuc = HinhThucThanhToan.builder()
                    .idHoaDon(hoaDon)
                    .idPhuongThucThanhToan(phuongThuc)
                    .ma(generatePaymentCode())
                    .deleted(false)
                    .build();

            if ("COD".equalsIgnoreCase(paymentMethodType) || "TIEN_MAT".equalsIgnoreCase(paymentMethodType)) {
                hinhThuc.setTienMat(amount);
                hinhThuc.setTienChuyenKhoan(BigDecimal.ZERO);
            } else if ("VNPAY".equalsIgnoreCase(paymentMethodType) || "CHUYEN_KHOAN".equalsIgnoreCase(paymentMethodType)) {
                hinhThuc.setTienChuyenKhoan(amount);
                hinhThuc.setTienMat(BigDecimal.ZERO);
            } else {
                hinhThuc.setTienMat(amount);
                hinhThuc.setTienChuyenKhoan(BigDecimal.ZERO);
            }

            hinhThucThanhToanRepository.save(hinhThuc);
        } catch (Exception e) {
            throw new RuntimeException("Không thể lưu hình thức thanh toán: " + e.getMessage(), e);
        }
    }

    private PhuongThucThanhToan findOrCreatePaymentMethodType(String paymentMethodType) {
        Optional<PhuongThucThanhToan> existingMethod = phuongThucThanhToanRepository.findByKieuThanhToan(paymentMethodType);

        if (existingMethod.isPresent()) {
            return existingMethod.get();
        }

        PhuongThucThanhToan newMethod = PhuongThucThanhToan.builder()
                .ma(generatePaymentMethodCode(paymentMethodType))
                .kieuThanhToan(paymentMethodType)
                .deleted(false)
                .build();

        return phuongThucThanhToanRepository.save(newMethod);
    }

    private String generatePaymentCode() {
        return "PAY_" + System.currentTimeMillis();
    }

    private String generatePaymentMethodCode(String type) {
        return "PM_" + type.toUpperCase() + "_" + System.currentTimeMillis();
    }

    @Override
    public CartResponse xoaToanBoGioHang(Integer hoaDonId) {
        GioHang gioHang = gioHangRepository.findByHoaDonId(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng cho hóa đơn ID: " + hoaDonId));

        gioHangChiTietRepository.deleteByGioHangId(gioHang.getId());

        gioHang.setTongTien(BigDecimal.ZERO);
        gioHangRepository.save(gioHang);

        return CartResponse.builder()
                .id(gioHang.getId())
                .idHoaDon(hoaDonId)
                .tongTien(BigDecimal.ZERO)
                .totalItems(0)
                .items(new ArrayList<>())
                .build();
    }

    private void validateThongTinGiaoHang(HoaDon hoaDon) {
        boolean khachVangLai = hoaDon.getIdKhachHang() == null
                || KhachHangRepository.MA_KHACH_LE.equals(hoaDon.getIdKhachHang().getMa());
        com.example.sd_24_striker_be.common.ThongTinKhachHangValidator.throwIfInvalid(
                com.example.sd_24_striker_be.common.ThongTinKhachHangValidator.hoTen(hoaDon.getTenKhachHang()),
                com.example.sd_24_striker_be.common.ThongTinKhachHangValidator.soDienThoai(hoaDon.getSoDienThoaiKhachHang()),
                khachVangLai
                        ? com.example.sd_24_striker_be.common.ThongTinKhachHangValidator.email(hoaDon.getEmail())
                        : com.example.sd_24_striker_be.common.ThongTinKhachHangValidator.emailKhongBatBuoc(hoaDon.getEmail()),
                com.example.sd_24_striker_be.common.ThongTinKhachHangValidator.diaChi(hoaDon.getDiaChiKhachHang()));
        hoaDon.setTenKhachHang(hoaDon.getTenKhachHang().trim().replaceAll("\\s+", " "));
        hoaDon.setSoDienThoaiKhachHang(hoaDon.getSoDienThoaiKhachHang().trim());
        if (hoaDon.getEmail() != null) {
            hoaDon.setEmail(hoaDon.getEmail().trim());
        }
    }

    private BigDecimal tinhLaiTongTien(HoaDon hoaDon, List<GioHangChiTiet> cartItems) {
        for (GioHangChiTiet cartItem : cartItems) {
            BigDecimal gia = chiTietSanPhamService.getEffectivePrice(cartItem.getIdChiTietSp());
            cartItem.setGia(gia);
            cartItem.setThanhTien(gia.multiply(BigDecimal.valueOf(cartItem.getSoLuong())));
            gioHangChiTietRepository.save(cartItem);
        }

        BigDecimal tienSanPham = cartItems.stream()
                .map(GioHangChiTiet::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal phiVanChuyen = tinhPhiVanChuyen(hoaDon, cartItems);

        BigDecimal tienGiam = BigDecimal.ZERO;
        PhieuGiamGia phieuGiamGia = hoaDon.getIdPhieuGiamGia();
        if (phieuGiamGia != null) {
            String loi = VoucherCalculator.validate(phieuGiamGia, tienSanPham);
            if (loi != null) {
                throw new RuntimeException(loi);
            }
            phieuGiamGiaServices.kiemTraQuyenSuDung(phieuGiamGia, hoaDon.getIdKhachHang());
            tienGiam = VoucherCalculator.discountAmount(phieuGiamGia, tienSanPham);
        }

        BigDecimal tongTien = tienSanPham.add(phiVanChuyen);
        BigDecimal tongTienSauGiam = tongTien.subtract(tienGiam);

        hoaDon.setTienSanPham(tienSanPham);
        hoaDon.setPhiVanChuyen(phiVanChuyen);
        hoaDon.setTongTien(tongTien);
        hoaDon.setTongTienSauGiam(tongTienSauGiam);
        return tongTienSauGiam;
    }

    @Override
    public void updateCustomerInfo(Integer hoaDonId, ClientPaymentRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn có id: " + hoaDonId));

        if (request.getTenKhachHang() != null && !request.getTenKhachHang().trim().isEmpty()) {
            hoaDon.setTenKhachHang(request.getTenKhachHang());
        }
        if (request.getSoDienThoai() != null && !request.getSoDienThoai().trim().isEmpty()) {
            hoaDon.setSoDienThoaiKhachHang(request.getSoDienThoai());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            hoaDon.setEmail(request.getEmail());
        }
        if (request.getDiaChi() != null && !request.getDiaChi().trim().isEmpty()) {
            hoaDon.setDiaChiKhachHang(request.getDiaChi());
        }

        if (request.getVoucherId() != null) {
            PhieuGiamGia voucher = phieuGiamGiaRepository.findById(request.getVoucherId()).orElse(null);
            if (voucher != null) {
                hoaDon.setIdPhieuGiamGia(voucher);
            }
        }

        validateThongTinGiaoHang(hoaDon);

        gioHangRepository.findByHoaDonId(hoaDonId).ifPresent(gioHang -> {
            List<GioHangChiTiet> cartItems = gioHangChiTietRepository.findByGioHangId(gioHang.getId());
            if (!cartItems.isEmpty()) {
                tinhLaiTongTien(hoaDon, cartItems);
            }
        });

        hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        hoaDonRepository.save(hoaDon);
    }

    @Override
    public HoaDonDetailResponse thanhToan(Integer hoaDonId, PaymentRequest paymentRequest) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại!"));

        if (hoaDon.getTrangThai() != 0) {
            throw new RuntimeException("Hóa đơn không ở trạng thái chờ thanh toán!");
        }

        GioHang gioHang = gioHangRepository.findByHoaDonId(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng!"));

        List<GioHangChiTiet> cartItems = gioHangChiTietRepository.findByGioHangId(gioHang.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể thanh toán!");
        }

        for (GioHangChiTiet item : cartItems) {
            ChiTietSanPham chiTietSanPham = item.getIdChiTietSp();
            if (chiTietSanPham.getSoLuongTonKho() < item.getSoLuong()) {
                throw new RuntimeException("Sản phẩm " + chiTietSanPham.getIdSanPham().getTenSanPham() +
                        " không đủ số lượng! Còn lại: " + chiTietSanPham.getSoLuongTonKho());
            }
        }

        BigDecimal tienSanPham = cartItems.stream()
                .map(GioHangChiTiet::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal phiVanChuyen = tinhPhiVanChuyen(hoaDon, cartItems);
        BigDecimal tongTienSauGiam = tienSanPham.add(phiVanChuyen);

        hoaDon.setTienSanPham(tienSanPham);
        hoaDon.setPhiVanChuyen(phiVanChuyen);
        hoaDon.setTongTien(tongTienSauGiam);
        hoaDon.setTongTienSauGiam(tongTienSauGiam);
        hoaDon.setTrangThai((short) 1);
        hoaDon.setNgayThanhToan(new java.sql.Timestamp(System.currentTimeMillis()));
        hoaDonRepository.save(hoaDon);

        for (GioHangChiTiet cartItem : cartItems) {
            ChiTietSanPham chiTietSanPham = cartItem.getIdChiTietSp();
            int tonKho = chiTietSanPham.getSoLuongTonKho() != null ? chiTietSanPham.getSoLuongTonKho() : 0;
            if (tonKho < cartItem.getSoLuong()) {
                throw new RuntimeException("Sản phẩm trong giỏ đã hết hàng hoặc không đủ số lượng. Còn lại: " + tonKho);
            }

            for (int i = 0; i < cartItem.getSoLuong(); i++) {
                HoaDonChiTiet hoaDonChiTiet = HoaDonChiTiet.builder()
                        .idHoaDon(hoaDon)
                        .idChiTietSp(cartItem.getIdChiTietSp())
                        .gia(cartItem.getGia())
                        .soLuong(1)
                        .trangThai((short) 1)
                        .deleted(false)
                        .build();
                hoaDonChiTietRepository.save(hoaDonChiTiet);
            }
        }

        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .idHoaDon(hoaDon)
                .idNhanVien(null)
                .ma("LSHD_" + UUID.randomUUID().toString().substring(0, 8))
                .hanhDong("Thanh toán hóa đơn qua client (" + paymentRequest.getPhuongThucThanhToan() + ")")
                .thoiGian(new java.sql.Timestamp(System.currentTimeMillis()))
                .deleted((short) 0)
                .build();
        lichSuHoaDonRepository.save(lichSu);

        gioHangChiTietRepository.deleteByGioHangId(gioHang.getId());
        gioHangRepository.delete(gioHang);

        sendOrderConfirmationEmailSafely(hoaDon, hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId()));

        return mapToHoaDonDetailResponse(hoaDon);
    }

    private void updateCartTotal(Integer cartId) {
        List<GioHangChiTiet> items = gioHangChiTietRepository.findByGioHangId(cartId);
        BigDecimal total = items.stream()
                .map(GioHangChiTiet::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        GioHang cart = gioHangRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng với ID: " + cartId));

        cart.setTongTien(total);
        gioHangRepository.save(cart);
    }

    private CartResponse mapToCartResponse(GioHang cart) {
        List<GioHangChiTiet> items = gioHangChiTietRepository.findByGioHangId(cart.getId());

        return CartResponse.builder()
                .id(cart.getId())
                .idKhachHang(cart.getIdKhachHang() != null ? cart.getIdKhachHang().getId() : null)
                .tenKhachHang(cart.getIdKhachHang() != null ? cart.getIdKhachHang().getTen() : null)
                .idHoaDon(cart.getIdHoaDon().getId())
                .maHoaDon(cart.getIdHoaDon().getMa())
                .tongTien(cart.getTongTien())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .items(items.stream().map(this::mapToCartItemResponse).collect(Collectors.toList()))
                .totalItems(items.size())
                .build();
    }

    private CartItemResponse mapToCartItemResponse(GioHangChiTiet item) {
        ChiTietSanPham chiTietSp = item.getIdChiTietSp();
        return CartItemResponse.builder()
                .id(item.getId())
                .idChiTietSanPham(chiTietSp.getId())
                .maSanPham(chiTietSp.getMa())
                .tenSanPham(chiTietSp.getIdSanPham().getTenSanPham())
                .tenMauSac(chiTietSp.getIdMauSac().getTenMauSac())
                .maSanPhamGoc(chiTietSp.getIdSanPham().getMa())
                .tenKichCo(chiTietSp.getIdKichCo().getTenKichCo())
                .tenThuongHieu(chiTietSp.getIdSanPham().getIdThuongHieu().getTenThuongHieu())
                .tenDanhMuc(chiTietSp.getIdSanPham().getIdDanhMuc().getTenDanhMuc())
                .urlAnhSanPham(chiTietSp.getIdAnhSanPham() != null ? chiTietSp.getIdAnhSanPham().getUrlAnh()
                        : (chiTietSp.getIdSanPham().getIdAnhSanPham() != null
                        ? chiTietSp.getIdSanPham().getIdAnhSanPham().getUrlAnh() : null))
                .soLuong(item.getSoLuong())
                .gia(item.getGia())
                .thanhTien(item.getThanhTien())
                .soLuongTonKho(chiTietSp.getSoLuongTonKho())
                .createdAt(item.getCreatedAt())
                .build();
    }

    private HoaDonDetailResponse mapToHoaDonDetailResponse(HoaDon hoaDon) {
        HoaDonDetailResponse response = new HoaDonDetailResponse();
        response.setId(hoaDon.getId());
        response.setMaHoaDon(hoaDon.getMa());
        response.setLoaiDon(hoaDon.getLoaiDon());
        response.setTenKhachHang(hoaDon.getTenKhachHang());
        response.setSoDienThoaiKhachHang(hoaDon.getSoDienThoaiKhachHang());
        response.setDiaChiKhachHang(hoaDon.getDiaChiKhachHang());
        response.setEmail(hoaDon.getEmail());
        response.setGhiChu(hoaDon.getGhiChu());
        response.setTienSanPham(hoaDon.getTienSanPham());
        response.setTongTien(hoaDon.getTongTien());
        response.setTongTienSauGiam(hoaDon.getTongTienSauGiam());
        response.setPhiVanChuyen(hoaDon.getPhiVanChuyen());
        response.setTrangThai(hoaDon.getTrangThai());
        response.setNgayTao(hoaDon.getNgayTao());
        response.setNgayThanhToan(hoaDon.getNgayThanhToan());

        if (hoaDon.getIdPhieuGiamGia() != null) {
            response.setMaGiamGia(hoaDon.getIdPhieuGiamGia().getMa());
        }

        if (hoaDon.getIdNhanVien() != null) {
            response.setMaNhanVien(hoaDon.getIdNhanVien().getMa());
        }

        return response;
    }

    @Override
    public HoaDonDetailResponse getOrderDetails(Integer hoaDonId) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng có ID: " + hoaDonId));
            return mapToHoaDonDetailResponse(hoaDon);
        } catch (Exception e) {
            System.err.println("Error getting order details: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể lấy thông tin đơn hàng: " + e.getMessage());
        }
    }

    @Override
    public HoaDonDetailResponse searchOrderByCode(String orderCode) {
        try {
            HoaDon hoaDon = hoaDonRepository.findByMa(orderCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng có mã: " + orderCode));

            if (hoaDon.getTrangThai() < 1) {
                throw new ResourceNotFoundException("Đơn hàng chưa được xác nhận");
            }

            HoaDonDetailResponse response = mapToHoaDonDetailResponse(hoaDon);

            List<com.example.sd_24_striker_be.modal.bill.HoaDonChiTiet> hoaDonChiTiets =
                    hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());

            if (!hoaDonChiTiets.isEmpty()) {
                List<HoaDonDetailResponse.SanPhamChiTietInfo> sanPhamInfos = new java.util.ArrayList<>();

                for (com.example.sd_24_striker_be.modal.bill.HoaDonChiTiet hdct : hoaDonChiTiets) {
                    HoaDonDetailResponse.SanPhamChiTietInfo info = new HoaDonDetailResponse.SanPhamChiTietInfo();

                    info.setHoaDonChiTietId(hdct.getId());
                    info.setIdHoaDon(hdct.getIdHoaDon().getId());
                    info.setSoLuong(hdct.getSoLuong());
                    info.setGiaBan(hdct.getGia());
                    info.setGhiChu(hdct.getGhiChu());

                    if (hdct.getIdChiTietSp() != null) {
                        com.example.sd_24_striker_be.modal.product.ChiTietSanPham ctsp = hdct.getIdChiTietSp();
                        info.setChiTietSanPhamId(ctsp.getId());
                        info.setMaChiTietSanPham(ctsp.getMa());

                        if (ctsp.getIdSanPham() != null) {
                            info.setIdSanPham(ctsp.getIdSanPham().getId());
                            info.setMaSanPham(ctsp.getIdSanPham().getMa());
                            info.setTenSanPham(ctsp.getIdSanPham().getTenSanPham());
                            info.setMoTaChiTiet(ctsp.getIdSanPham().getMoTaSanPham());

                            if (ctsp.getIdSanPham().getIdAnhSanPham() != null) {
                                String imageUrl = ctsp.getIdSanPham().getIdAnhSanPham().getUrlAnh();
                                info.setDuongDan(imageUrl);
                            }
                        }

                        if (ctsp.getIdMauSac() != null) {
                            info.setMauSac(ctsp.getIdMauSac().getTenMauSac());
                        }

                        if (ctsp.getIdKichCo() != null) {
                            info.setKichCo(ctsp.getIdKichCo().getTenKichCo());
                        }

                        if (ctsp.getIdSanPham() != null && ctsp.getIdSanPham().getIdChatLieu() != null) {
                            info.setChatLieu(ctsp.getIdSanPham().getIdChatLieu().getTenChatLieu());
                        }
                    }
                    sanPhamInfos.add(info);
                }
                response.setSanPhamChiTietInfos(sanPhamInfos);
            }
            return response;
        } catch (Exception e) {
            System.err.println("Error searching order by code: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể tìm kiếm đơn hàng: " + e.getMessage());
        }
    }

    // =================================================================================
    // BỔ SUNG: HÀM XỬ LÝ HỦY ĐƠN HÀNG DÀNH CHO CLIENT
    // (Bạn cần khai báo hàm này trong Interface BanHangClientService nữa nhé)
    // =================================================================================
    public void huyDonHang(Integer hoaDonId, String lyDoHuy) {
        // 1. Tìm hóa đơn
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng có ID: " + hoaDonId));

        // 2. Lấy trạng thái hiện tại
        Short trangThaiHienTai = hoaDon.getTrangThai();

        // 3. CHẶN NGHIỆP VỤ: Không cho hủy nếu trạng thái là 3 (Chờ vận chuyển) hoặc 4 (Đang vận chuyển) hoặc 5 (Đã hoàn thành)
        // LƯU Ý: Hãy kiểm tra lại quy ước trạng thái trong DB của bạn.
        // Dựa theo mô tả: 3-Chờ vận chuyển, 4-Đang vận chuyển, 5-Đã hoàn thành, 6-Đã hủy
        if (trangThaiHienTai == 3 || trangThaiHienTai == 4 || trangThaiHienTai == 5) {
            throw new RuntimeException("Không thể hủy đơn hàng khi đơn đang ở trạng thái Chờ vận chuyển, Đang vận chuyển hoặc Đã hoàn thành!");
        }

        // 4. Nếu hợp lệ (trạng thái 0, 1 hoặc 2) -> Tiến hành hủy
        hoaDon.setTrangThai((short) 6); // 6 là trạng thái Đã hủy (theo quy ước của bạn)
        hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        hoaDonRepository.save(hoaDon);

        // 5. Ghi log lịch sử
        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .idHoaDon(hoaDon)
                .ma("LSHD_" + System.currentTimeMillis())
                .hanhDong("Khách hàng hủy đơn hàng")
                .thoiGian(new java.sql.Timestamp(System.currentTimeMillis()))
                .deleted((short) 6) // Cập nhật trạng thái mới vào log
                .ghiChu("Lý do hủy: " + (lyDoHuy != null ? lyDoHuy : "Không có lý do"))
                .build();
        lichSuHoaDonRepository.save(lichSu);

        // (Tùy chọn) Nếu đơn hàng đã bị trừ kho (ở trạng thái 2), bạn cần hoàn lại tồn kho tại đây
        // if (trangThaiHienTai == 2) {
        //     List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
        //     for (HoaDonChiTiet ct : chiTiets) {
        //         // Gọi service để cộng lại tồn kho...
        //     }
        // }
    }
}