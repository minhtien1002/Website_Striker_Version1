package com.example.sd_24_striker_be.service.product;

import com.example.sd_24_striker_be.dto.product.request.ProductWithVariantsCreateRequest;
import com.example.sd_24_striker_be.dto.product.request.SanPhamCreateRequest;
import com.example.sd_24_striker_be.dto.product.request.SanPhamUpdateRequest;
import com.example.sd_24_striker_be.dto.product.response.ChiTietSanPhamResponse;
import com.example.sd_24_striker_be.dto.product.response.SanPhamResponse;
import com.example.sd_24_striker_be.exception.BusinessException;
import com.example.sd_24_striker_be.exception.ResourceNotFoundException;
import com.example.sd_24_striker_be.modal.product.*;
import com.example.sd_24_striker_be.repository.product.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SanPhamService {
    private final SanPhamRepository sanPhamRepository;
    private final AnhSanPhamRepository anhSanPhamRepository;
    private final ChiTietSanPhamService chiTietSanPhamService;
    private final DanhMucRepository danhMucRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final ChatLieuRepository chatLieuRepository;

    public SanPhamService(SanPhamRepository sanPhamRepository, AnhSanPhamRepository anhSanPhamRepository,
                          ChiTietSanPhamService chiTietSanPhamService, DanhMucRepository danhMucRepository,
                          ThuongHieuRepository thuongHieuRepository, ChatLieuRepository chatLieuRepository) {
        this.sanPhamRepository = sanPhamRepository;
        this.anhSanPhamRepository = anhSanPhamRepository;
        this.chiTietSanPhamService = chiTietSanPhamService;
        this.danhMucRepository = danhMucRepository;
        this.thuongHieuRepository = thuongHieuRepository;
        this.chatLieuRepository = chatLieuRepository;
    }

    // ===================== Đọc =====================

    @Transactional(readOnly = true)
    public Page<SanPhamResponse> search(String keyword, Integer idThuongHieu, Integer idDanhMuc, Integer idChatLieu,
                                        String trangThai, BigDecimal minGia, BigDecimal maxGia, Pageable pageable) {
        Boolean deleted = "active".equals(trangThai) ? Boolean.FALSE : "inactive".equals(trangThai) ? Boolean.TRUE : null;
        if ((minGia != null && minGia.signum() < 0) || (maxGia != null && maxGia.signum() < 0)) {
            throw new BusinessException("Giá không được âm");
        }
        if (minGia != null && maxGia != null && minGia.compareTo(maxGia) > 0) {
            throw new BusinessException("Giá từ phải nhỏ hơn hoặc bằng giá đến");
        }
        return sanPhamRepository.search(keyword == null ? "" : keyword.trim(), idThuongHieu, idDanhMuc, idChatLieu, deleted, minGia, maxGia, pageable)
                .map(this::toResponse);
    }

    /** Sản phẩm đang hoạt động (trang khách hàng). */
    @Transactional(readOnly = true)
    public Page<SanPhamResponse> getActivePaged(Pageable pageable) {
        return sanPhamRepository.findActivePaged(pageable).map(this::toResponseHoatDong);
    }

    /** Sản phẩm đang hoạt động theo từ khóa, chỉ kèm biến thể đang hoạt động (bán hàng / trang khách). */
    @Transactional(readOnly = true)
    public Page<SanPhamResponse> searchActive(String keyword, Pageable pageable) {
        return sanPhamRepository.search(keyword == null ? "" : keyword.trim(), null, null, null, false, null, null, pageable)
                .map(this::toResponseHoatDong);
    }

    private SanPhamResponse toResponseHoatDong(SanPham sp) {
        SanPhamResponse res = toResponse(sp);
        res.setChiTietSanPhams(res.getChiTietSanPhams().stream().filter(ct -> Boolean.TRUE.equals(ct.getTrangThai())).toList());
        return res;
    }

    public SanPham getEntity(Integer id) {
        return sanPhamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
    }

    /** Chi tiết đầy đủ, mọi trạng thái (màn quản lý). */
    @Transactional(readOnly = true)
    public SanPhamResponse getDetail(Integer id) {
        return toResponse(getEntity(id));
    }

    public Optional<SanPham> findActiveById(Integer id) {
        return sanPhamRepository.findById(id).filter(sp -> !Boolean.TRUE.equals(sp.getDeleted()));
    }

    public SanPhamResponse toResponse(SanPham sp) {
        SanPhamResponse.SanPhamResponseBuilder b = SanPhamResponse.builder()
                .id(sp.getId())
                .ma(sp.getMa())
                .tenSanPham(sp.getTenSanPham())
                .moTa(sp.getMoTaSanPham())
                .trangThai(!Boolean.TRUE.equals(sp.getDeleted()))
                .deleted(Boolean.TRUE.equals(sp.getDeleted()))
                .ngayTao(sp.getNgayTao())
                .ngayCapNhat(sp.getNgayCapNhat())
                .urlAnhDaiDien(sp.getIdAnhSanPham() != null ? sp.getIdAnhSanPham().getUrlAnh() : null);
        if (sp.getIdDanhMuc() != null) {
            b.idDanhMuc(sp.getIdDanhMuc().getId()).tenDanhMuc(sp.getIdDanhMuc().getTenDanhMuc());
        }
        if (sp.getIdThuongHieu() != null) {
            b.idThuongHieu(sp.getIdThuongHieu().getId()).tenThuongHieu(sp.getIdThuongHieu().getTenThuongHieu());
        }
        if (sp.getIdChatLieu() != null) {
            b.idChatLieu(sp.getIdChatLieu().getId()).tenChatLieu(sp.getIdChatLieu().getTenChatLieu());
        }

        List<ChiTietSanPhamResponse> bienThe = sp.getChiTietSanPhams() == null ? List.of()
                : sp.getChiTietSanPhams().stream()
                    .map(ChiTietSanPhamService::toResponse)
                    .sorted(Comparator.comparing(ChiTietSanPhamResponse::getId))
                    .toList();
        List<ChiTietSanPhamResponse> hoatDong = bienThe.stream().filter(ct -> Boolean.TRUE.equals(ct.getTrangThai())).toList();

        b.chiTietSanPhams(bienThe)
         .totalVariants((long) bienThe.size())
         .activeVariants((long) hoatDong.size())
         .tongSoLuongTonKho(hoatDong.stream().mapToInt(ct -> ct.getSoLuongTonKho() == null ? 0 : ct.getSoLuongTonKho()).sum())
         .tongSoLuongDaBan(bienThe.stream().mapToInt(ct -> ct.getSoLuongDaBan() == null ? 0 : ct.getSoLuongDaBan()).sum())
         .soBienTheHetHang(hoatDong.stream().filter(ct -> ct.getSoLuongTonKho() == null || ct.getSoLuongTonKho() <= 0).count())
         .giaBanThapNhat(hoatDong.stream().map(ChiTietSanPhamResponse::getGiaBan).filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(null))
         .giaBanCaoNhat(hoatDong.stream().map(ChiTietSanPhamResponse::getGiaBan).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(null))
         .danhSachMauSac(bienThe.stream().map(ChiTietSanPhamResponse::getTenMauSac).filter(Objects::nonNull).distinct().toList())
         .danhSachKichCo(bienThe.stream().map(ChiTietSanPhamResponse::getTenKichCo).filter(Objects::nonNull).distinct().toList());
        return b.build();
    }

    // ===================== Ghi =====================

    @Transactional
    public SanPham createSanPham(SanPhamCreateRequest r) {
        SanPham sp = new SanPham();
        sp.setTenSanPham(validateTen(r.getTenSanPham(), null));
        sp.setMoTaSanPham(trimToNull(r.getMoTaSanPham()));
        ganThuocTinh(sp, r.getIdDanhMuc(), r.getIdThuongHieu(), r.getIdChatLieu());
        if (r.getUrlAnhDaiDien() != null && !r.getUrlAnhDaiDien().isBlank()) {
            sp.setIdAnhSanPham(luuAnhDaiDien(null, r.getUrlAnhDaiDien()));
        }
        Instant now = Instant.now();
        sp.setNgayTao(now);
        sp.setNgayCapNhat(now);
        sp.setDeleted(false);
        sp = sanPhamRepository.saveAndFlush(sp);
        sp.setMa(String.format("SP%03d", sp.getId()));
        return sanPhamRepository.save(sp);
    }

    @Transactional
    public SanPham createProductWithVariants(ProductWithVariantsCreateRequest request) {
        SanPham sp = createSanPham(request.getSanPham());
        List<ChiTietSanPham> variants = chiTietSanPhamService.createProductVariants(sp, request.getChiTietSanPhams());
        sp.setChiTietSanPhams(new java.util.LinkedHashSet<>(variants));
        return sp;
    }

    @Transactional
    public SanPham updateSanPham(Integer id, SanPhamUpdateRequest r) {
        SanPham sp = getEntity(id);
        sp.setTenSanPham(validateTen(r.getTenSanPham(), id));
        sp.setMoTaSanPham(trimToNull(r.getMoTaSanPham()));
        ganThuocTinh(sp, r.getIdDanhMuc(), r.getIdThuongHieu(), r.getIdChatLieu());
        if (r.getUrlAnhDaiDien() != null && !r.getUrlAnhDaiDien().isBlank()) {
            sp.setIdAnhSanPham(luuAnhDaiDien(sp.getIdAnhSanPham(), r.getUrlAnhDaiDien()));
        }
        sp.setNgayCapNhat(Instant.now());
        return sanPhamRepository.save(sp);
    }

    /** Đổi trạng thái sản phẩm và áp dụng cùng trạng thái cho mọi biến thể. */
    @Transactional
    public SanPham toggleProductStatus(Integer id) {
        SanPham sp = getEntity(id);
        boolean ngung = !Boolean.TRUE.equals(sp.getDeleted());
        sp.setDeleted(ngung);
        sp.setNgayCapNhat(Instant.now());
        sp = sanPhamRepository.save(sp);
        chiTietSanPhamService.updateDeletedStatusByProductId(id, ngung);
        return sp;
    }

    public boolean isTenTrung(String ten, Integer excludeId) {
        if (ten == null || ten.isBlank()) return false;
        return sanPhamRepository.countTrungTen(ten.trim().replaceAll("\\s+", " "), excludeId) > 0;
    }

    private String validateTen(String ten, Integer excludeId) {
        if (ten == null || ten.isBlank()) {
            throw new BusinessException("Tên sản phẩm không được để trống");
        }
        String s = ten.trim().replaceAll("\\s+", " ");
        if (s.length() < 3) {
            throw new BusinessException("Tên sản phẩm phải có ít nhất 3 ký tự");
        }
        if (sanPhamRepository.countTrungTen(s, excludeId) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "Tên sản phẩm \"" + s + "\" đã tồn tại (không phân biệt chữ hoa, chữ thường)");
        }
        return s;
    }

    /**
     * Gán thể loại / thương hiệu / chất liệu. Thuộc tính đang ngừng hoạt động chỉ được giữ nguyên
     * nếu sản phẩm vốn đã dùng nó, không được chọn mới.
     */
    private void ganThuocTinh(SanPham sp, Integer idDanhMuc, Integer idThuongHieu, Integer idChatLieu) {
        sp.setIdDanhMuc(layHoatDong(danhMucRepository, idDanhMuc, sp.getIdDanhMuc(), "Thể loại"));
        sp.setIdThuongHieu(layHoatDong(thuongHieuRepository, idThuongHieu, sp.getIdThuongHieu(), "Thương hiệu"));
        sp.setIdChatLieu(layHoatDong(chatLieuRepository, idChatLieu, sp.getIdChatLieu(), "Chất liệu"));
    }

    private <T extends ThuocTinh> T layHoatDong(ThuocTinhRepository<T> repo, Integer id, T hienTai, String nhan) {
        T tt = repo.findById(id).orElseThrow(() -> new BusinessException(nhan + " không tồn tại"));
        boolean giuNguyen = hienTai != null && Objects.equals(hienTai.getId(), id);
        if (Boolean.TRUE.equals(tt.getDeleted()) && !giuNguyen) {
            throw new BusinessException(nhan + " \"" + tt.getTen() + "\" đang ngừng hoạt động");
        }
        return tt;
    }

    private AnhSanPham luuAnhDaiDien(AnhSanPham anh, String url) {
        if (anh == null) {
            anh = new AnhSanPham();
            anh.setLaAnhDaiDien(true);
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

    // ===================== Trang khách hàng / đợt giảm giá =====================

    @Transactional(readOnly = true)
    public Page<SanPhamResponse> getProductsByBrandIds(List<Integer> brandIds, Pageable pageable) {
        return sanPhamRepository.findProductsByBrandIds(brandIds, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SanPhamResponse> getProductsByCategoryName(String categoryName, Pageable pageable) {
        return sanPhamRepository.findProductsByCategoryName(categoryName, pageable).map(this::toResponse);
    }

    /** Sản phẩm còn hàng để chọn vào đợt giảm giá; chỉ kèm biến thể đang hoạt động và còn hàng. */
    @Transactional(readOnly = true)
    public Page<SanPhamResponse> getProductsAvailableForDiscount(Pageable pageable) {
        return sanPhamRepository.findProductsAvailableForDiscount(pageable).map(sp -> {
            SanPhamResponse res = toResponse(sp);
            List<ChiTietSanPhamResponse> conHang = res.getChiTietSanPhams().stream()
                    .filter(ct -> Boolean.TRUE.equals(ct.getTrangThai()) && ct.getSoLuongTonKho() != null && ct.getSoLuongTonKho() > 0)
                    .toList();
            res.setChiTietSanPhams(conHang);
            res.setTotalVariants((long) conHang.size());
            res.setActiveVariants((long) conHang.size());
            return res;
        });
    }
}
