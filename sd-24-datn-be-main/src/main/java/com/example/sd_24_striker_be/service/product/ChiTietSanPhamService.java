package com.example.sd_24_striker_be.service.product;

import com.example.sd_24_striker_be.dto.product.request.ChiTietSanPhamCreateRequest;
import com.example.sd_24_striker_be.dto.product.request.ChiTietSanPhamUpdateRequest;
import com.example.sd_24_striker_be.dto.product.response.ChiTietSanPhamResponse;
import com.example.sd_24_striker_be.dto.product.response.ChiTietSanPhamWithDiscountResponse;
import com.example.sd_24_striker_be.exception.BusinessException;
import com.example.sd_24_striker_be.exception.ResourceNotFoundException;
import com.example.sd_24_striker_be.modal.product.*;
import com.example.sd_24_striker_be.modal.sale.ChiTietDotGiamGia;
import com.example.sd_24_striker_be.modal.sale.DotGiamGia;
import com.example.sd_24_striker_be.repository.product.AnhSanPhamRepository;
import com.example.sd_24_striker_be.repository.product.ChiTietSanPhamRepository;
import com.example.sd_24_striker_be.repository.product.KichCoRepository;
import com.example.sd_24_striker_be.repository.product.MauSacRepository;
import com.example.sd_24_striker_be.repository.sale.DotGiamGia.ChiTietDotGiamGiaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class ChiTietSanPhamService {
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final AnhSanPhamRepository anhSanPhamRepository;
    private final ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;
    private final MauSacRepository mauSacRepository;
    private final KichCoRepository kichCoRepository;

    public ChiTietSanPhamService(ChiTietSanPhamRepository chiTietSanPhamRepository,
                                 AnhSanPhamRepository anhSanPhamRepository,
                                 ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository,
                                 MauSacRepository mauSacRepository,
                                 KichCoRepository kichCoRepository) {
        this.chiTietSanPhamRepository = chiTietSanPhamRepository;
        this.anhSanPhamRepository = anhSanPhamRepository;
        this.chiTietDotGiamGiaRepository = chiTietDotGiamGiaRepository;
        this.mauSacRepository = mauSacRepository;
        this.kichCoRepository = kichCoRepository;
    }

    // ===================== Quản lý =====================

    @Transactional(readOnly = true)
    public Page<ChiTietSanPhamResponse> getAllWithFilters(Integer productId, String search, Integer sizeId, Integer colorId,
                                                          String status, Double minPrice, Double maxPrice, Pageable pageable) {
        return chiTietSanPhamRepository.findAllWithFilters(productId,
                        search == null ? "" : search.trim(), sizeId, colorId,
                        status == null ? "" : status.trim(), minPrice, maxPrice, pageable)
                .map(ChiTietSanPhamService::toResponse);
    }

    public static ChiTietSanPhamResponse toResponse(ChiTietSanPham ct) {
        ChiTietSanPhamResponse.ChiTietSanPhamResponseBuilder b = ChiTietSanPhamResponse.builder()
                .id(ct.getId())
                .ma(ct.getMa())
                .giaBan(ct.getGiaBan())
                .soLuongTonKho(ct.getSoLuongTonKho())
                .soLuongDaBan(ct.getSoLuongDaBan() == null ? 0 : ct.getSoLuongDaBan())
                .moTaChiTiet(ct.getMoTaChiTiet())
                .trangThai(!Boolean.TRUE.equals(ct.getDeleted()))
                .deleted(Boolean.TRUE.equals(ct.getDeleted()))
                .ngayTao(ct.getNgayTao())
                .ngayCapNhat(ct.getNgayCapNhat());
        SanPham sp = ct.getIdSanPham();
        if (sp != null) {
            b.idSanPham(sp.getId()).maSanPham(sp.getMa()).tenSanPham(sp.getTenSanPham());
        }
        if (ct.getIdMauSac() != null) {
            b.idMauSac(ct.getIdMauSac().getId()).tenMauSac(ct.getIdMauSac().getTenMauSac());
        }
        if (ct.getIdKichCo() != null) {
            b.idKichCo(ct.getIdKichCo().getId()).tenKichCo(ct.getIdKichCo().getTenKichCo());
        }
        if (ct.getIdAnhSanPham() != null) {
            b.urlAnhSanPham(ct.getIdAnhSanPham().getUrlAnh());
        } else if (sp != null && sp.getIdAnhSanPham() != null) {
            b.urlAnhSanPham(sp.getIdAnhSanPham().getUrlAnh());
        }
        return b.build();
    }

    @Transactional(readOnly = true)
    public ChiTietSanPhamResponse getResponseById(Integer id) {
        return toResponse(getById(id));
    }

    public ChiTietSanPham getById(Integer id) {
        return chiTietSanPhamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết sản phẩm"));
    }

    public Optional<ChiTietSanPham> findById(Integer id) {
        return chiTietSanPhamRepository.findById(id);
    }

    /**
     * Tạo các biến thể cho sản phẩm. Kiểm tra toàn bộ trước khi lưu: không trùng cặp
     * (màu, kích cỡ) trong cùng request lẫn với biến thể đã có, màu/kích cỡ phải đang hoạt động.
     */
    @Transactional
    public List<ChiTietSanPham> createProductVariants(SanPham sanPham, List<ChiTietSanPhamCreateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException("Vui lòng thêm ít nhất một biến thể (màu sắc, kích cỡ)");
        }
        Set<String> trongRequest = new HashSet<>();
        for (ChiTietSanPhamCreateRequest r : requests) {
            if (r.getIdMauSac() == null || r.getIdKichCo() == null) {
                throw new BusinessException("Vui lòng chọn màu sắc và kích cỡ cho mọi biến thể");
            }
            kiemTraGiaVaSoLuong(r.getGiaBan(), r.getSoLuongTonKho());
            MauSac mau = layMauSacHoatDong(r.getIdMauSac(), null);
            KichCo co = layKichCoHoatDong(r.getIdKichCo(), null);
            if (!trongRequest.add(r.getIdMauSac() + "-" + r.getIdKichCo())) {
                throw new BusinessException("Biến thể " + mau.getTenMauSac() + " / " + co.getTenKichCo() + " bị lặp");
            }
            if (chiTietSanPhamRepository.countTrungBienThe(sanPham.getId(), r.getIdMauSac(), r.getIdKichCo(), null) > 0) {
                throw new BusinessException("Sản phẩm đã có biến thể " + mau.getTenMauSac() + " / " + co.getTenKichCo());
            }
        }

        List<ChiTietSanPham> created = new ArrayList<>();
        for (ChiTietSanPhamCreateRequest r : requests) {
            ChiTietSanPham ct = new ChiTietSanPham();
            ct.setIdSanPham(sanPham);
            ct.setIdMauSac(mauSacRepository.getReferenceById(r.getIdMauSac()));
            ct.setIdKichCo(kichCoRepository.getReferenceById(r.getIdKichCo()));
            ct.setSoLuongTonKho(r.getSoLuongTonKho());
            ct.setSoLuongDaBan(0);
            ct.setGiaBan(r.getGiaBan());
            ct.setMoTaChiTiet(trimToNull(r.getMoTaChiTiet()));
            if (r.getUrlAnhSanPham() != null && !r.getUrlAnhSanPham().isBlank()) {
                ct.setIdAnhSanPham(luuAnh(null, r.getUrlAnhSanPham(), false));
            }
            Instant now = Instant.now();
            ct.setNgayNhap(now);
            ct.setNgayTao(now);
            ct.setNgayCapNhat(now);
            ct.setDeleted(false);
            ct = chiTietSanPhamRepository.saveAndFlush(ct);
            ct.setMa(String.format("CTSP%03d", ct.getId()));
            created.add(chiTietSanPhamRepository.save(ct));
        }
        return created;
    }

    @Transactional
    public ChiTietSanPham updateChiTietSanPham(Integer id, ChiTietSanPhamUpdateRequest r) {
        ChiTietSanPham ct = getById(id);
        Integer mauCu = ct.getIdMauSac() != null ? ct.getIdMauSac().getId() : null;
        Integer coCu = ct.getIdKichCo() != null ? ct.getIdKichCo().getId() : null;
        // Cho phép giữ nguyên màu/kích cỡ đã ngừng hoạt động, chỉ chặn khi đổi sang cái đang ngừng
        MauSac mau = layMauSacHoatDong(r.getIdMauSac(), mauCu);
        KichCo co = layKichCoHoatDong(r.getIdKichCo(), coCu);
        if (chiTietSanPhamRepository.countTrungBienThe(ct.getIdSanPham().getId(), r.getIdMauSac(), r.getIdKichCo(), id) > 0) {
            throw new BusinessException("Sản phẩm đã có biến thể " + mau.getTenMauSac() + " / " + co.getTenKichCo());
        }
        ct.setIdMauSac(mau);
        ct.setIdKichCo(co);
        ct.setSoLuongTonKho(r.getSoLuongTonKho());
        ct.setGiaBan(r.getGiaBan());
        ct.setMoTaChiTiet(trimToNull(r.getMoTaChiTiet()));
        if (r.getUrlAnhSanPham() != null && !r.getUrlAnhSanPham().isBlank()) {
            ct.setIdAnhSanPham(luuAnh(ct.getIdAnhSanPham(), r.getUrlAnhSanPham(), false));
        }
        ct.setNgayCapNhat(Instant.now());
        return chiTietSanPhamRepository.save(ct);
    }

    @Transactional
    public ChiTietSanPham toggleStatus(Integer id) {
        ChiTietSanPham ct = getById(id);
        boolean ngung = !Boolean.TRUE.equals(ct.getDeleted());
        if (!ngung && Boolean.TRUE.equals(ct.getIdSanPham().getDeleted())) {
            throw new BusinessException("Sản phẩm đang ngừng hoạt động, hãy bật hoạt động cho sản phẩm trước");
        }
        ct.setDeleted(ngung);
        ct.setNgayCapNhat(Instant.now());
        return chiTietSanPhamRepository.save(ct);
    }

    public void updateDeletedStatusByProductId(Integer productId, Boolean deletedStatus) {
        chiTietSanPhamRepository.updateDeletedStatusByProductId(productId, deletedStatus);
    }

    private static void kiemTraGiaVaSoLuong(BigDecimal giaBan, Integer soLuong) {
        if (giaBan == null || giaBan.compareTo(BigDecimal.valueOf(1000)) < 0 || giaBan.compareTo(BigDecimal.valueOf(100_000_000)) > 0) {
            throw new BusinessException("Giá bán phải từ 1.000đ đến 100.000.000đ");
        }
        if (soLuong == null || soLuong < 0 || soLuong > 100_000) {
            throw new BusinessException("Số lượng phải từ 0 đến 100.000");
        }
    }

    private MauSac layMauSacHoatDong(Integer id, Integer idHienTai) {
        MauSac m = mauSacRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Màu sắc không tồn tại"));
        if (Boolean.TRUE.equals(m.getDeleted()) && !Objects.equals(id, idHienTai)) {
            throw new BusinessException("Màu sắc \"" + m.getTenMauSac() + "\" đang ngừng hoạt động");
        }
        return m;
    }

    private KichCo layKichCoHoatDong(Integer id, Integer idHienTai) {
        KichCo k = kichCoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Kích cỡ không tồn tại"));
        if (Boolean.TRUE.equals(k.getDeleted()) && !Objects.equals(id, idHienTai)) {
            throw new BusinessException("Kích cỡ \"" + k.getTenKichCo() + "\" đang ngừng hoạt động");
        }
        return k;
    }

    private AnhSanPham luuAnh(AnhSanPham anh, String url, boolean daiDien) {
        if (anh == null) {
            anh = new AnhSanPham();
            anh.setLaAnhDaiDien(daiDien);
            anh.setNgayTao(Instant.now());
            anh.setDeleted(false);
        }
        anh.setUrlAnh(url.trim());
        anh.setNgayCapNhat(Instant.now());
        return anhSanPhamRepository.save(anh);
    }

    private static String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    // ===================== Bán hàng =====================

    public List<ChiTietSanPham> getActiveByProductIdForSales(Integer productId) {
        return chiTietSanPhamRepository.findActiveByProductIdForSales(productId);
    }

    public Page<ChiTietSanPham> getActiveProductDetailsForSales(Pageable pageable) {
        return chiTietSanPhamRepository.findActiveProductDetailsForSales(pageable);
    }

    public Page<ChiTietSanPham> getActiveProductDetailsForSalesWithKeyword(Pageable pageable, String keyword) {
        return chiTietSanPhamRepository.findActiveProductDetailsForSalesWithKeyword(pageable, keyword);
    }

    @Transactional(readOnly = true)
    public List<ChiTietSanPhamWithDiscountResponse> getActiveProductDetailsWithDiscountPrices() {
        List<ChiTietSanPhamWithDiscountResponse> result = new ArrayList<>();
        for (ChiTietSanPham ct : chiTietSanPhamRepository.findActiveProductDetailsForSales()) {
            result.add(toDiscountResponse(ct));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ChiTietSanPhamWithDiscountResponse> getActiveByProductIdWithDiscountResponses(Integer productId) {
        List<ChiTietSanPhamWithDiscountResponse> result = new ArrayList<>();
        for (ChiTietSanPham ct : chiTietSanPhamRepository.findActiveByProductIdForSales(productId)) {
            result.add(toDiscountResponse(ct));
        }
        return result;
    }

    /** Trang chi tiết sản phẩm phía khách: mọi biến thể đang hoạt động, kể cả hết hàng. */
    @Transactional(readOnly = true)
    public List<ChiTietSanPhamWithDiscountResponse> getForClientProductPage(Integer productId) {
        List<ChiTietSanPhamWithDiscountResponse> result = new ArrayList<>();
        for (ChiTietSanPham ct : chiTietSanPhamRepository.findActiveByProductIdIncludingOutOfStock(productId)) {
            result.add(toDiscountResponse(ct));
        }
        return result;
    }

    public ChiTietSanPhamWithDiscountResponse toDiscountResponse(ChiTietSanPham ct) {
        DiscountInfo info = getDiscountInfo(ct);
        return ChiTietSanPhamWithDiscountResponse.from(ct,
                info != null ? info.getDiscountedPrice() : null,
                info != null ? info.getCampaignName() : null);
    }

    /**
     * Giá bán hiện hành của biến thể (đã áp đợt giảm giá nếu có). Giỏ hàng / hóa đơn PHẢI dùng
     * giá này thay vì giá client gửi lên.
     */
    public BigDecimal getEffectivePrice(ChiTietSanPham chiTietSanPham) {
        DiscountInfo info = getDiscountInfo(chiTietSanPham);
        return info != null ? info.getDiscountedPrice() : chiTietSanPham.getGiaBan();
    }

    /**
     * Tính giá sau các đợt giảm giá đang chạy: các đợt phần trăm lấy trung bình phần trăm (giới hạn
     * bởi tổng mức giảm tối đa), các đợt giảm tiền cộng dồn. Trả null nếu không có giảm giá.
     */
    public DiscountInfo getDiscountInfo(ChiTietSanPham chiTietSanPham) {
        List<ChiTietDotGiamGia> activeDiscounts = chiTietDotGiamGiaRepository.findActiveDiscountsByProductDetail(chiTietSanPham.getId());
        if (activeDiscounts.isEmpty()) {
            return null;
        }

        BigDecimal originalPrice = chiTietSanPham.getGiaBan();
        LocalDate today = LocalDate.now();
        List<DotGiamGia> percentageDiscounts = new ArrayList<>();
        List<DotGiamGia> fixedAmountDiscounts = new ArrayList<>();
        List<String> campaignNames = new ArrayList<>();

        for (ChiTietDotGiamGia chiTietDiscount : activeDiscounts) {
            DotGiamGia discount = chiTietDiscount.getIdDotGiamGia();
            if (Boolean.TRUE.equals(discount.getTrangThai())
                    && discount.getNgayBatDau() != null && discount.getNgayKetThuc() != null
                    && !discount.getNgayBatDau().isAfter(today)
                    && !discount.getNgayKetThuc().isBefore(today)) {
                campaignNames.add(discount.getTenDotGiamGia());
                if ("PHAN_TRAM".equals(discount.getLoaiGiamGiaApDung())) {
                    percentageDiscounts.add(discount);
                } else {
                    fixedAmountDiscounts.add(discount);
                }
            }
        }

        if (percentageDiscounts.isEmpty() && fixedAmountDiscounts.isEmpty()) {
            return null;
        }

        BigDecimal finalPrice = originalPrice;
        if (!percentageDiscounts.isEmpty()) {
            BigDecimal totalPercentage = BigDecimal.ZERO;
            BigDecimal totalMaxDiscount = BigDecimal.ZERO;
            for (DotGiamGia discount : percentageDiscounts) {
                totalPercentage = totalPercentage.add(discount.getGiaTriGiamGia());
                if (discount.getSoTienGiamToiDa() != null) {
                    totalMaxDiscount = totalMaxDiscount.add(discount.getSoTienGiamToiDa());
                }
            }
            BigDecimal averagePercentage = totalPercentage.divide(BigDecimal.valueOf(percentageDiscounts.size()), 2, RoundingMode.HALF_UP);
            BigDecimal discountAmount = originalPrice.multiply(averagePercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            if (totalMaxDiscount.compareTo(BigDecimal.ZERO) > 0 && discountAmount.compareTo(totalMaxDiscount) > 0) {
                discountAmount = totalMaxDiscount;
            }
            finalPrice = finalPrice.subtract(discountAmount);
        }
        for (DotGiamGia discount : fixedAmountDiscounts) {
            finalPrice = finalPrice.subtract(discount.getGiaTriGiamGia());
        }
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }
        finalPrice = finalPrice.setScale(0, RoundingMode.HALF_UP);
        if (finalPrice.compareTo(originalPrice) == 0) {
            return null;
        }
        return new DiscountInfo(originalPrice, finalPrice, String.join(" + ", campaignNames));
    }

    public static class DiscountInfo {
        private final BigDecimal originalPrice;
        private final BigDecimal discountedPrice;
        private final String campaignName;

        public DiscountInfo(BigDecimal originalPrice, BigDecimal discountedPrice, String campaignName) {
            this.originalPrice = originalPrice;
            this.discountedPrice = discountedPrice;
            this.campaignName = campaignName;
        }

        public BigDecimal getOriginalPrice() { return originalPrice; }
        public BigDecimal getDiscountedPrice() { return discountedPrice; }
        public String getCampaignName() { return campaignName; }
    }
}
