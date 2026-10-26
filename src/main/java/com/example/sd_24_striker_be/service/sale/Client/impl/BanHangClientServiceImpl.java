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

    /**
     * Ngưỡng tiền hàng được miễn phí vận chuyển và mức phí khi chưa đạt ngưỡng.
     * PHẢI khớp với {@code sd-24-datn-fe-client-main/src/utils/shipping.js} - đó là lý do
     * hai hằng số này được đặt tên rõ ràng thay vì rải số 30000 khắp nơi như trước.
     */
    private static final BigDecimal NGUONG_MIEN_PHI_VAN_CHUYEN = new BigDecimal("500000");
    private static final BigDecimal PHI_VAN_CHUYEN = new BigDecimal("30000");

    /**
     * Phí vận chuyển của một đơn online, tính trên tiền hàng (chưa trừ voucher).
     * Đây là nguồn tính phí DUY NHẤT của server: mọi luồng thanh toán client đều phải gọi
     * hàm này thay vì tự hard-code hoặc lấy theo số client gửi lên.
     */
    private BigDecimal tinhPhiVanChuyen(BigDecimal tienSanPham) {
        BigDecimal tienHang = tienSanPham != null ? tienSanPham : BigDecimal.ZERO;
        return tienHang.compareTo(NGUONG_MIEN_PHI_VAN_CHUYEN) >= 0 ? BigDecimal.ZERO : PHI_VAN_CHUYEN;
    }

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
            // Bản ghi "Khách lẻ" dùng chung (trước đây là khách ID=1 - một khách thật có tài
            // khoản, nên khách đó thấy đơn của mọi khách vãng lai trong lịch sử mua hàng).
            khachHang = khachHangRepository.findFirstByMa(KhachHangRepository.MA_KHACH_LE)
                    .orElseGet(() -> khachHangRepository.save(KhachHang.builder()
                            .ma(KhachHangRepository.MA_KHACH_LE)
                            .ten("Khách lẻ")
                            .deleted(true)
                            .createdAt(java.time.Instant.now())
                            .build()));
        }

        // Check for existing pending invoice - CHỈ áp dụng cho khách đã đăng nhập
        // thật sự (có khachHangId riêng). Với khách vãng lai (isGuest), mọi phiên
        // đều dùng chung "khách lẻ" ID=1, nên KHÔNG được tái sử dụng hóa đơn chờ
        // theo customer ID ở đây — làm vậy sẽ gộp giỏ hàng của mọi khách vãng lai
        // đang mua sắm đồng thời lại thành MỘT giỏ hàng chung. Mỗi phiên khách
        // vãng lai phải luôn được tạo hóa đơn chờ mới, và trình duyệt tự giữ
        // invoiceId riêng của mình (localStorage) để quay lại đúng giỏ đó.
        if (!isGuest) {
            List<HoaDon> existingPendingInvoices = hoaDonRepository.findByIdKhachHangAndTrangThai(khachHang.getId(), (short) 0);
            if (!existingPendingInvoices.isEmpty()) {
                return mapToHoaDonDetailResponse(existingPendingInvoices.get(0));
            }
        }

        // Get default employee (ID = 1)
        NhanVien nhanVien = nhanVienRepository.findById(1)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên mặc định không tồn tại"));

        // Create new pending invoice
        HoaDon hoaDon = HoaDon.builder()
                .idKhachHang(khachHang)
                .idNhanVien(nhanVien)
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
        // Validate invoice
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn có id: " + hoaDonId));

        if (hoaDon.getTrangThai() != 0) {
            throw new RuntimeException("Hóa đơn này không phải hóa đơn chờ! Trạng thái hiện tại: " + hoaDon.getTrangThai());
        }

        // Validate product detail
        ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(request.getIdChiTietSanPham())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết sản phẩm!"));

        if (request.getSoLuong() == null || request.getSoLuong() <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }
        if (Boolean.TRUE.equals(chiTietSanPham.getDeleted())) {
            throw new RuntimeException("Sản phẩm này đã ngừng bán");
        }

        // Check stock availability
        if (chiTietSanPham.getSoLuongTonKho() < request.getSoLuong()) {
            throw new RuntimeException("Không đủ số lượng tồn kho! Còn lại: " + chiTietSanPham.getSoLuongTonKho());
        }

        // Giá lấy từ server (giá bán + đợt giảm giá đang chạy), KHÔNG dùng request.getGia():
        // trước đây khách sửa request là mua được với giá tùy ý.
        BigDecimal gia = chiTietSanPhamService.getEffectivePrice(chiTietSanPham);

        // Get or create cart
        GioHang gioHang = gioHangRepository.findByHoaDonId(hoaDonId)
                .orElseGet(() -> {
                    GioHang newCart = GioHang.builder()
                            .idKhachHang(hoaDon.getIdKhachHang())
                            .idHoaDon(hoaDon)
                            .tongTien(BigDecimal.ZERO)
                            .build();
                    return gioHangRepository.save(newCart);
                });

        // Check if product already exists in cart
        Optional<GioHangChiTiet> existingItem = gioHangChiTietRepository
                .findByGioHangIdAndChiTietSpId(gioHang.getId(), request.getIdChiTietSanPham());

        if (existingItem.isPresent()) {
            // Update existing item
            GioHangChiTiet item = existingItem.get();
            int newQuantity = item.getSoLuong() + request.getSoLuong();

            // Check total stock availability
            if (chiTietSanPham.getSoLuongTonKho() < newQuantity) {
                throw new RuntimeException("Không đủ số lượng tồn kho! Còn lại: " + chiTietSanPham.getSoLuongTonKho());
            }

            item.setSoLuong(newQuantity);
            item.setGia(gia);
            item.setThanhTien(gia.multiply(BigDecimal.valueOf(newQuantity)));
            gioHangChiTietRepository.save(item);

            // KHÔNG trừ tồn kho ở đây - xem ghi chú tại nhánh tạo mới bên dưới.
        } else {
            // Create new cart item
            GioHangChiTiet newItem = GioHangChiTiet.builder()
                    .idGioHang(gioHang)
                    .idChiTietSp(chiTietSanPham)
                    .soLuong(request.getSoLuong())
                    .gia(gia)
                    .thanhTien(gia.multiply(BigDecimal.valueOf(request.getSoLuong())))
                    .build();
            gioHangChiTietRepository.save(newItem);

            // KHÔNG trừ tồn kho ở đây. Tồn kho chỉ bị trừ đúng một lần tại
            // HoaDonServiceImpl.deductStockForHoaDon(), chạy khi admin chuyển đơn sang
            // "Đã xác nhận". Bản cũ trừ ngay lúc thêm vào giỏ RỒI TRỪ TIẾP lần nữa lúc
            // thanh toán (xem thanhToanHoaDon bên dưới) -> mỗi đơn online bị trừ kho hai lần.
        }

        // Update cart total
        updateCartTotal(gioHang.getId());

        return mapToCartResponse(gioHang);
    }

    @Override
    public CartResponse layGioHang(Integer hoaDonId) {
        System.out.println("=== Getting cart for invoice ID: " + hoaDonId + " ===");
        
        GioHang gioHang = gioHangRepository.findByHoaDonId(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng cho hóa đơn ID: " + hoaDonId));
        
        System.out.println("Found GioHang ID: " + gioHang.getId() + " for HoaDon ID: " + hoaDonId);

        return mapToCartResponse(gioHang);
    }

    @Override
    public CartResponse capNhatSoLuongSanPham(Integer hoaDonId, Integer cartItemId, UpdateCartItemRequest request) {
        // Validate invoice
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn có id: " + hoaDonId));

        if (hoaDon.getTrangThai() != 0) {
            throw new RuntimeException("Hóa đơn này không phải hóa đơn chờ!");
        }

        // Get cart item
        GioHangChiTiet cartItem = gioHangChiTietRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng!"));

        // Sản phẩm phải thuộc giỏ của chính hóa đơn này (không cho sửa giỏ hàng của đơn khác)
        if (cartItem.getIdGioHang() == null || cartItem.getIdGioHang().getIdHoaDon() == null
                || !hoaDonId.equals(cartItem.getIdGioHang().getIdHoaDon().getId())) {
            throw new RuntimeException("Sản phẩm không thuộc giỏ hàng này");
        }
        if (request.getSoLuong() == null || request.getSoLuong() <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        // Get current and new quantities
        ChiTietSanPham chiTietSanPham = cartItem.getIdChiTietSp();
        int newQuantity = request.getSoLuong();

        // Giỏ hàng không giữ chỗ tồn kho nữa, nên phải so TỔNG số lượng mới với tồn kho
        // thực tế (bản cũ chỉ so phần chênh lệch vì kho đã bị trừ dần theo giỏ).
        if (newQuantity > 0 && chiTietSanPham.getSoLuongTonKho() < newQuantity) {
            throw new RuntimeException("Không đủ số lượng tồn kho! Còn lại: " + chiTietSanPham.getSoLuongTonKho());
        }

        // Update cart item
        cartItem.setSoLuong(newQuantity);
        cartItem.setThanhTien(cartItem.getGia().multiply(BigDecimal.valueOf(newQuantity)));
        gioHangChiTietRepository.save(cartItem);

        // Update cart total
        updateCartTotal(cartItem.getIdGioHang().getId());

        return mapToCartResponse(cartItem.getIdGioHang());
    }

    @Override
    public CartResponse xoaSanPhamKhoiGioHang(Integer hoaDonId, Integer cartItemId) {
        // Validate invoice
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn có id: " + hoaDonId));

        if (hoaDon.getTrangThai() != 0) {
            throw new RuntimeException("Hóa đơn này không phải hóa đơn chờ!");
        }

        // Get cart item
        GioHangChiTiet cartItem = gioHangChiTietRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng!"));

        GioHang gioHang = cartItem.getIdGioHang();
        
        // Không cộng trả tồn kho: giỏ hàng chưa từng trừ kho (xem themSanPhamVaoGioHang).
        gioHangChiTietRepository.delete(cartItem);

        // Update cart total
        updateCartTotal(gioHang.getId());

        return mapToCartResponse(gioHang);
    }

    @Override
    public HoaDonDetailResponse thanhToan(Integer hoaDonId, ClientPaymentRequest request) {
        // Validate invoice
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn có id: " + hoaDonId));

        if (hoaDon.getTrangThai() != 0) {
            throw new RuntimeException("Hóa đơn này không phải hóa đơn chờ!");
        }

        // Get cart
        GioHang gioHang = gioHangRepository.findByHoaDonId(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng cho hóa đơn ID: " + hoaDonId));

        // Get cart items
        List<GioHangChiTiet> cartItems = gioHangChiTietRepository.findByGioHangId(gioHang.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể thanh toán!");
        }

        // Update invoice information - only update if values are provided
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
        
        // Set voucher if provided
        if (request.getVoucherId() != null) {
            System.out.println("Setting voucher ID: " + request.getVoucherId() + " for invoice: " + hoaDonId);
            // Find voucher by ID and set it
            PhieuGiamGia voucher = phieuGiamGiaRepository.findById(request.getVoucherId()).orElse(null);
            if (voucher != null) {
                hoaDon.setIdPhieuGiamGia(voucher);
                System.out.println("Voucher set successfully: " + voucher.getMa());
            } else {
                System.err.println("Voucher not found with ID: " + request.getVoucherId());
            }
        }
        
        validateThongTinGiaoHang(hoaDon);

        // Lượt voucher KHÔNG trừ ở đây: trừ lúc admin xác nhận đơn, cùng lúc với tồn kho
        // (HoaDonServiceImpl.deductStockForHoaDon). Ở bước đặt chỉ kiểm tra voucher còn lượt.
        BigDecimal tongTienSauGiam = tinhLaiTongTien(hoaDon, cartItems);

        // Update status to confirmed (1) and payment date
        hoaDon.setTrangThai((short) 1);
        hoaDon.setNgayThanhToan(new java.sql.Timestamp(System.currentTimeMillis()));
        hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        
        hoaDonRepository.save(hoaDon);

        // Create payment method record
        createPaymentMethod(hoaDon, request.getPhuongThucThanhToan(), tongTienSauGiam);

        // Create invoice details from cart items
        for (GioHangChiTiet cartItem : cartItems) {
            HoaDonChiTiet hoaDonChiTiet = HoaDonChiTiet.builder()
                    .idHoaDon(hoaDon)
                    .idChiTietSp(cartItem.getIdChiTietSp())
                    .soLuong(cartItem.getSoLuong())
                    .gia(cartItem.getGia())
                    .trangThai((short) 1) // Active status
                    .deleted(false)
                    .build();
            hoaDonChiTietRepository.save(hoaDonChiTiet);
        }

        // Create invoice history
        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .idHoaDon(hoaDon)
                .hanhDong("Thanh toán đơn hàng")
                .thoiGian(new java.sql.Timestamp(System.currentTimeMillis()))
                .deleted((short) 0) // 0 = not deleted
                .build();
        lichSuHoaDonRepository.save(lichSu);

        // Clear cart after successful payment
        gioHangChiTietRepository.deleteAll(cartItems);
        gioHangRepository.delete(gioHang);

        System.out.println("Payment completed for invoice ID: " + hoaDonId +
                         ", Amount: " + tongTienSauGiam + ", Method: " + request.getPhuongThucThanhToan());

        sendOrderConfirmationEmailSafely(hoaDon, hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId()));

        return mapToHoaDonDetailResponse(hoaDon);
    }

    // Get cart total for VNPay payment
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

    // Create payment method record
    private void createPaymentMethod(HoaDon hoaDon, String paymentMethodType, BigDecimal amount) {
        try {
            // Find or create payment method type
            PhuongThucThanhToan phuongThuc = findOrCreatePaymentMethodType(paymentMethodType);
            
            // Create payment record
            HinhThucThanhToan hinhThuc = HinhThucThanhToan.builder()
                    .idHoaDon(hoaDon)
                    .idPhuongThucThanhToan(phuongThuc)
                    .ma(generatePaymentCode())
                    .deleted(false)
                    .build();

            // Set payment amounts based on method type
            if ("COD".equalsIgnoreCase(paymentMethodType) || "TIEN_MAT".equalsIgnoreCase(paymentMethodType)) {
                hinhThuc.setTienMat(amount);
                hinhThuc.setTienChuyenKhoan(BigDecimal.ZERO);
            } else if ("VNPAY".equalsIgnoreCase(paymentMethodType) || "CHUYEN_KHOAN".equalsIgnoreCase(paymentMethodType)) {
                hinhThuc.setTienChuyenKhoan(amount);
                hinhThuc.setTienMat(BigDecimal.ZERO);
            } else {
                // Default to cash
                hinhThuc.setTienMat(amount);
                hinhThuc.setTienChuyenKhoan(BigDecimal.ZERO);
            }

            hinhThucThanhToanRepository.save(hinhThuc);
            
            System.out.println("Created payment method: " + paymentMethodType + 
                             ", Amount: " + amount + 
                             ", Cash: " + hinhThuc.getTienMat() + 
                             ", Transfer: " + hinhThuc.getTienChuyenKhoan());
        } catch (Exception e) {
            System.err.println("Error creating payment method: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Find or create payment method type
    private PhuongThucThanhToan findOrCreatePaymentMethodType(String paymentMethodType) {
        // Try to find existing payment method
        Optional<PhuongThucThanhToan> existingMethod = phuongThucThanhToanRepository.findByKieuThanhToan(paymentMethodType);
        
        if (existingMethod.isPresent()) {
            return existingMethod.get();
        }
        
        // Create new payment method type if not found
        PhuongThucThanhToan newMethod = PhuongThucThanhToan.builder()
                .ma(generatePaymentMethodCode(paymentMethodType))
                .kieuThanhToan(paymentMethodType)
                .deleted(false)
                .build();
                
        return phuongThucThanhToanRepository.save(newMethod);
    }

    // Generate payment code
    private String generatePaymentCode() {
        return "PAY_" + System.currentTimeMillis();
    }

    // Generate payment method code
    private String generatePaymentMethodCode(String type) {
        return "PM_" + type.toUpperCase() + "_" + System.currentTimeMillis();
    }

    @Override
    public CartResponse xoaToanBoGioHang(Integer hoaDonId) {
        GioHang gioHang = gioHangRepository.findByHoaDonId(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng cho hóa đơn ID: " + hoaDonId));

        // Delete all cart items
        gioHangChiTietRepository.deleteByGioHangId(gioHang.getId());

        // Update cart total to zero
        gioHang.setTongTien(BigDecimal.ZERO);
        gioHangRepository.save(gioHang);
        
        // Return updated cart response
        return CartResponse.builder()
                .id(gioHang.getId())
                .idHoaDon(hoaDonId)
                .tongTien(BigDecimal.ZERO)
                .totalItems(0)
                .items(new ArrayList<>())
                .build();
    }

    /**
     * Tính lại toàn bộ tiền của hóa đơn online ở SERVER và ghi vào hóa đơn:
     * - chốt giá từng dòng theo giá hiện hành (giỏ cũ có thể còn giá do client gửi),
     * - phí ship theo quy tắc server,
     * - tiền giảm theo phiếu giảm giá của hóa đơn.
     * Trước đây phí ship / tiền giảm lấy thẳng từ request nên khách sửa payload là tự giảm giá.
     */
    /**
     * Thông tin giao hàng của đơn online (sau khi đã gộp dữ liệu request) phải hợp lệ -
     * trước đây backend không kiểm tra gì, khách không đăng nhập đặt được đơn với SĐT "abc".
     * Khách vãng lai bắt buộc có email (để nhận xác nhận đơn / tra cứu).
     */
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
        BigDecimal phiVanChuyen = tinhPhiVanChuyen(tienSanPham);

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
        // Validate invoice
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn có id: " + hoaDonId));

        System.out.println("Updating customer info for invoice: " + hoaDonId);
        System.out.println("Current customer info: " + hoaDon.getTenKhachHang() + ", " + hoaDon.getDiaChiKhachHang());
        
        // Update customer information if provided
        if (request.getTenKhachHang() != null && !request.getTenKhachHang().trim().isEmpty()) {
            hoaDon.setTenKhachHang(request.getTenKhachHang());
            System.out.println("Updated customer name: " + request.getTenKhachHang());
        }
        if (request.getSoDienThoai() != null && !request.getSoDienThoai().trim().isEmpty()) {
            hoaDon.setSoDienThoaiKhachHang(request.getSoDienThoai());
            System.out.println("Updated customer phone: " + request.getSoDienThoai());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            hoaDon.setEmail(request.getEmail());
            System.out.println("Updated customer email: " + request.getEmail());
        }
        if (request.getDiaChi() != null && !request.getDiaChi().trim().isEmpty()) {
            hoaDon.setDiaChiKhachHang(request.getDiaChi());
            System.out.println("Updated customer address: " + request.getDiaChi());
        }
        
        // Set voucher if provided
        if (request.getVoucherId() != null) {
            System.out.println("Setting voucher ID: " + request.getVoucherId() + " for invoice: " + hoaDonId);
            PhieuGiamGia voucher = phieuGiamGiaRepository.findById(request.getVoucherId()).orElse(null);
            if (voucher != null) {
                hoaDon.setIdPhieuGiamGia(voucher);
                System.out.println("Voucher set successfully: " + voucher.getMa());
            } else {
                System.err.println("Voucher not found with ID: " + request.getVoucherId());
            }
        }
        
        // Bước này chạy trước khi tạo link VNPay -> kiểm tra thông tin giao hàng luôn tại đây
        validateThongTinGiaoHang(hoaDon);

        // Tiền hàng / phí ship / tiền giảm do server tính lại (số tiền này được dùng để tạo
        // link VNPay), không dùng phiVanChuyen / tienGiam client gửi lên.
        gioHangRepository.findByHoaDonId(hoaDonId).ifPresent(gioHang -> {
            List<GioHangChiTiet> cartItems = gioHangChiTietRepository.findByGioHangId(gioHang.getId());
            if (!cartItems.isEmpty()) {
                tinhLaiTongTien(hoaDon, cartItems);
            }
        });
        
        hoaDon.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        hoaDonRepository.save(hoaDon);
        
        System.out.println("Customer info updated successfully for invoice: " + hoaDonId);
    }

    @Override
    public HoaDonDetailResponse thanhToan(Integer hoaDonId, PaymentRequest paymentRequest) {
        // Validate invoice
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại!"));

        if (hoaDon.getTrangThai() != 0) {
            throw new RuntimeException("Hóa đơn không ở trạng thái chờ thanh toán!");
        }

        // Get cart
        GioHang gioHang = gioHangRepository.findByHoaDonId(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng!"));

        List<GioHangChiTiet> cartItems = gioHangChiTietRepository.findByGioHangId(gioHang.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể thanh toán!");
        }

        // Validate stock availability for all items
        for (GioHangChiTiet item : cartItems) {
            ChiTietSanPham chiTietSanPham = item.getIdChiTietSp();
            if (chiTietSanPham.getSoLuongTonKho() < item.getSoLuong()) {
                throw new RuntimeException("Sản phẩm " + chiTietSanPham.getIdSanPham().getTenSanPham() +
                        " không đủ số lượng! Còn lại: " + chiTietSanPham.getSoLuongTonKho());
            }
        }

        // Calculate totals
        BigDecimal tienSanPham = cartItems.stream()
                .map(GioHangChiTiet::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal phiVanChuyen = tinhPhiVanChuyen(tienSanPham);
        BigDecimal tongTienSauGiam = tienSanPham.add(phiVanChuyen);

        // Update invoice
        hoaDon.setTienSanPham(tienSanPham);
        hoaDon.setPhiVanChuyen(phiVanChuyen);
        hoaDon.setTongTien(tongTienSauGiam);
        hoaDon.setTongTienSauGiam(tongTienSauGiam);
        hoaDon.setTrangThai((short) 1); // Confirmed status
        hoaDon.setNgayThanhToan(new java.sql.Timestamp(System.currentTimeMillis()));
        hoaDonRepository.save(hoaDon);

        // Chốt đơn: chỉ tạo dòng chi tiết hóa đơn, KHÔNG đụng tới tồn kho.
        // Đơn online sau khi thanh toán mới ở trạng thái "Chờ xác nhận" (1); tồn kho sẽ bị
        // trừ khi admin chuyển sang "Đã xác nhận" (2) qua
        // HoaDonServiceImpl.deductStockForHoaDon(). Bản cũ trừ kho ở đây trong khi kho đã bị
        // trừ sẵn lúc thêm vào giỏ -> trừ hai lần cho cùng một đơn.
        for (GioHangChiTiet cartItem : cartItems) {
            // Kiểm tra sớm để khách biết ngay nếu hàng đã hết trong lúc còn để trong giỏ.
            ChiTietSanPham chiTietSanPham = cartItem.getIdChiTietSp();
            int tonKho = chiTietSanPham.getSoLuongTonKho() != null ? chiTietSanPham.getSoLuongTonKho() : 0;
            if (tonKho < cartItem.getSoLuong()) {
                throw new RuntimeException("Sản phẩm trong giỏ đã hết hàng hoặc không đủ số lượng. Còn lại: " + tonKho);
            }

            // Create invoice detail for each quantity
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

        // Create invoice history
        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .idHoaDon(hoaDon)
                .idNhanVien(hoaDon.getIdNhanVien())
                .ma("LSHD_" + UUID.randomUUID().toString().substring(0, 8))
                .hanhDong("Thanh toán hóa đơn qua client (" + paymentRequest.getPhuongThucThanhToan() + ")")
                .thoiGian(new java.sql.Timestamp(System.currentTimeMillis()))
                .deleted((short) 0)
                .build();
        lichSuHoaDonRepository.save(lichSu);

        // Clear cart
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
        System.out.println("=== Mapping cart to response ===");
        System.out.println("Cart ID: " + cart.getId());
        
        List<GioHangChiTiet> items = gioHangChiTietRepository.findByGioHangId(cart.getId());
        System.out.println("Found " + items.size() + " items in cart");
        
        // Also check with simple query
        List<GioHangChiTiet> allItems = gioHangChiTietRepository.findAll();
        System.out.println("Total items in database: " + allItems.size());
        
        // Check items for this specific cart ID
        long itemsForThisCart = allItems.stream()
            .filter(item -> item.getIdGioHang().getId().equals(cart.getId()))
            .count();
        System.out.println("Items for cart ID " + cart.getId() + ": " + itemsForThisCart);
        
        for (GioHangChiTiet item : items) {
            System.out.println("Item: " + item.getId() + " - " + item.getIdChiTietSp().getIdSanPham().getTenSanPham() + " x" + item.getSoLuong());
        }

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
                .maSanPham(chiTietSp.getIdSanPham().getMa())
                .tenSanPham(chiTietSp.getIdSanPham().getTenSanPham())
                .tenMauSac(chiTietSp.getIdMauSac().getTenMauSac())
                .hexMauSac(chiTietSp.getIdMauSac().getHex())
                .tenKichCo(chiTietSp.getIdKichCo().getTenKichCo())
                .tenThuongHieu(chiTietSp.getIdSanPham().getIdThuongHieu().getTenThuongHieu())
                .tenDanhMuc(chiTietSp.getIdSanPham().getIdDanhMuc().getTenDanhMuc())
                // Per-variant image is optional and usually null; fall back to the product's default image.
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
        
        // Thông tin giảm giá
        if (hoaDon.getIdPhieuGiamGia() != null) {
            response.setMaGiamGia(hoaDon.getIdPhieuGiamGia().getMa());
        }
        
        // Thông tin nhân viên
        if (hoaDon.getIdNhanVien() != null) {
            response.setMaNhanVien(hoaDon.getIdNhanVien().getMa());
        }
        
        return response;
    }

    @Override
    public HoaDonDetailResponse getOrderDetails(Integer hoaDonId) {
        try {
            // Find the invoice
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng có ID: " + hoaDonId));

            // Use existing mapping method
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
            // Find the invoice by order code
            HoaDon hoaDon = hoaDonRepository.findByMa(orderCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng có mã: " + orderCode));

            // Only return orders that are confirmed (status >= 1) for security
            if (hoaDon.getTrangThai() < 1) {
                throw new ResourceNotFoundException("Đơn hàng chưa được xác nhận");
            }

            // Get basic invoice info
            HoaDonDetailResponse response = mapToHoaDonDetailResponse(hoaDon);
            
            // Get product details from HoaDonChiTiet with eager loading
            List<com.example.sd_24_striker_be.modal.bill.HoaDonChiTiet> hoaDonChiTiets = 
                hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());
            
            System.out.println("Found " + hoaDonChiTiets.size() + " order items");
            
            if (!hoaDonChiTiets.isEmpty()) {
                List<HoaDonDetailResponse.SanPhamChiTietInfo> sanPhamInfos = new java.util.ArrayList<>();
                
                for (com.example.sd_24_striker_be.modal.bill.HoaDonChiTiet hdct : hoaDonChiTiets) {
                    HoaDonDetailResponse.SanPhamChiTietInfo info = new HoaDonDetailResponse.SanPhamChiTietInfo();
                    
                    // Basic info
                    info.setHoaDonChiTietId(hdct.getId());
                    info.setIdHoaDon(hdct.getIdHoaDon().getId());
                    info.setSoLuong(hdct.getSoLuong());
                    info.setGiaBan(hdct.getGia());
                    info.setGhiChu(hdct.getGhiChu());
                    
                    // Product details from ChiTietSanPham
                    if (hdct.getIdChiTietSp() != null) {
                        com.example.sd_24_striker_be.modal.product.ChiTietSanPham ctsp = hdct.getIdChiTietSp();
                        info.setChiTietSanPhamId(ctsp.getId());
                        info.setMaChiTietSanPham(ctsp.getMa());
                        
                        // Product info
                        if (ctsp.getIdSanPham() != null) {
                            info.setIdSanPham(ctsp.getIdSanPham().getId());
                            info.setMaSanPham(ctsp.getIdSanPham().getMa());
                            info.setTenSanPham(ctsp.getIdSanPham().getTenSanPham());
                            info.setMoTaChiTiet(ctsp.getIdSanPham().getMoTaSanPham());
                            
                            // Get image from AnhSanPham relationship
                            if (ctsp.getIdSanPham().getIdAnhSanPham() != null) {
                                String imageUrl = ctsp.getIdSanPham().getIdAnhSanPham().getUrlAnh();
                                System.out.println("Product: " + ctsp.getIdSanPham().getTenSanPham() + " - Image URL: " + imageUrl);
                                info.setDuongDan(imageUrl);
                            } else {
                                System.out.println("Product: " + ctsp.getIdSanPham().getTenSanPham() + " - No image found");
                            }
                        }
                        
                        // Color info
                        if (ctsp.getIdMauSac() != null) {
                            info.setMauSac(ctsp.getIdMauSac().getTenMauSac());
                        }
                        
                        // Size info
                        if (ctsp.getIdKichCo() != null) {
                            info.setKichCo(ctsp.getIdKichCo().getTenKichCo());
                        }
                        
                        // Material info from SanPham
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
}
