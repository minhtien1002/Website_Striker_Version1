package com.example.sd_24_striker_be.service.sale.impl.DotGiamGia;

import com.example.sd_24_striker_be.dto.product.response.sanpham.SanPhamDetailResponse;
import com.example.sd_24_striker_be.dto.sale.request.DotGiamGia.DotGiamGiaDTO;
import com.example.sd_24_striker_be.dto.sale.response.DotGiamGia.DotGiamGiaDetailResponse;
import com.example.sd_24_striker_be.modal.product.ChiTietSanPham;
import com.example.sd_24_striker_be.modal.sale.ChiTietDotGiamGia;
import com.example.sd_24_striker_be.modal.sale.DotGiamGia;
import com.example.sd_24_striker_be.repository.product.ChiTietSanPhamRepository;
import com.example.sd_24_striker_be.repository.sale.DotGiamGia.ChiTietDotGiamGiaRepository;
import com.example.sd_24_striker_be.repository.sale.DotGiamGia.DotGiamGiaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DotGiamGiaServices {
    private final DotGiamGiaRepository dotGiamGiaRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;

    public DotGiamGiaServices(DotGiamGiaRepository dotGiamGiaRepository, ChiTietSanPhamRepository chiTietSanPhamRepository, ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository) {
        this.dotGiamGiaRepository = dotGiamGiaRepository;
        this.chiTietSanPhamRepository = chiTietSanPhamRepository;
        this.chiTietDotGiamGiaRepository = chiTietDotGiamGiaRepository;
    }


    public List<DotGiamGia> getall() {
        return dotGiamGiaRepository.findAll();
    }

    /**
     * Kiểm tra dữ liệu đợt giảm giá (trước đây backend không kiểm tra gì: dữ liệu sai thì lỗi
     * 500 từ DB). Với loại TIEN_MAT, form admin không gửi soTienGiamToiDa nhưng cột DB bắt
     * buộc -> gán bằng chính số tiền giảm (giống dữ liệu cũ), nếu không tạo/sửa đợt sẽ lỗi 500.
     */
    private void validateAndNormalize(DotGiamGiaDTO dto) {
        if (dto.getTenDotGiamGia() == null || dto.getTenDotGiamGia().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đợt giảm giá không được để trống");
        }
        boolean phanTram = "PHAN_TRAM".equalsIgnoreCase(dto.getLoaiGiamGiaApDung());
        if (!phanTram && !"TIEN_MAT".equalsIgnoreCase(dto.getLoaiGiamGiaApDung())) {
            throw new IllegalArgumentException("Loại giảm giá không hợp lệ");
        }
        if (dto.getGiaTriGiamGia() == null || dto.getGiaTriGiamGia().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0");
        }
        if (phanTram && dto.getGiaTriGiamGia().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Phần trăm giảm không được vượt quá 100%");
        }
        if (dto.getNgayBatDau() == null || dto.getNgayKetThuc() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        if (dto.getNgayKetThuc().isBefore(dto.getNgayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        if (dto.getListSanPhamId() == null || dto.getListSanPhamId().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một sản phẩm");
        }
        if (dto.getSoTienGiamToiDa() == null) {
            if (phanTram) {
                throw new IllegalArgumentException("Số tiền giảm tối đa không được để trống");
            }
            dto.setSoTienGiamToiDa(dto.getGiaTriGiamGia());
        }
    }

    @Transactional
    public DotGiamGia addDotGiamGia(DotGiamGiaDTO dto) {
        validateAndNormalize(dto);
        // map từ DTO sang Entity
        DotGiamGia dot = DotGiamGia.builder()
                .ma(dto.getMa())
                .tenDotGiamGia(dto.getTenDotGiamGia())
                .loaiGiamGiaApDung(dto.getLoaiGiamGiaApDung())
                .giaTriGiamGia(dto.getGiaTriGiamGia())
                .soTienGiamToiDa(dto.getSoTienGiamToiDa())
                .ngayBatDau(dto.getNgayBatDau())
                .ngayKetThuc(dto.getNgayKetThuc())
                .trangThai(true)
                .deleted(false)
                .build();
        kiemTraGiaSauGiamKhongDuoiGiaNhap(dot, dto.getListSanPhamId());

        DotGiamGia savedDot = dotGiamGiaRepository.save(dot);

        // Lấy danh sách sản phẩm áp dụng
        List<ChiTietSanPham> products = chiTietSanPhamRepository.findAllById(dto.getListSanPhamId());

        for (ChiTietSanPham sp : products) {
            BigDecimal giaBanDau = sp.getGiaBan();
            BigDecimal giaSauKhiGiam = tinhGiaSauKhiGiam(savedDot, giaBanDau);

            ChiTietDotGiamGia ctgg = ChiTietDotGiamGia.builder()
                    .idDotGiamGia(savedDot)
                    .idChiTietSp(sp)
                    .ma("CTGG-" + sp.getId() + "-" + savedDot.getId())
                    .giaBanDau(giaBanDau)
                    .giaSauKhiGiam(giaSauKhiGiam)
                    .deleted(false)
                    .build();

            chiTietDotGiamGiaRepository.save(ctgg);
        }

        return savedDot;
    }

    /**
     * Giá sau giảm của từng biến thể không được thấp hơn giá nhập - nếu không, đơn có sản phẩm
     * đó sẽ không xác nhận được (xem HoaDonServiceImpl.deductStockForHoaDon).
     */
    private void kiemTraGiaSauGiamKhongDuoiGiaNhap(DotGiamGia dot, List<Integer> listChiTietSanPhamId) {
        for (ChiTietSanPham sp : chiTietSanPhamRepository.findAllById(listChiTietSanPhamId)) {
            if (sp.getGiaBan() == null || sp.getGiaNhap() == null) continue;
            BigDecimal giaSau = tinhGiaSauKhiGiam(dot, sp.getGiaBan());
            if (giaSau.compareTo(sp.getGiaNhap()) <= 0) {
                String ten = sp.getIdSanPham() != null ? sp.getIdSanPham().getTenSanPham() : ("biến thể " + sp.getMa());
                throw new IllegalArgumentException("Giá sau giảm của \"" + ten + " (" + sp.getMa() + ")\" là "
                        + giaSau.toPlainString() + "đ, không cao hơn giá nhập " + sp.getGiaNhap().toPlainString()
                        + "đ. Vui lòng giảm mức giảm giá hoặc bỏ sản phẩm này khỏi đợt.");
            }
        }
    }

    private BigDecimal tinhGiaSauKhiGiam(DotGiamGia dot, BigDecimal giaBan) {
        BigDecimal giaSau;
        if ("PHAN_TRAM".equalsIgnoreCase(dot.getLoaiGiamGiaApDung())) {
            // giảm % theo giaTriGiamGia
            giaSau = giaBan.subtract(
                    giaBan.multiply(dot.getGiaTriGiamGia()).divide(BigDecimal.valueOf(100))
            );
        } else {
            // giảm số tiền trực tiếp
            giaSau = giaBan.subtract(dot.getGiaTriGiamGia());
        }

        // check giới hạn số tiền giảm tối đa
        if (dot.getSoTienGiamToiDa() != null) {
            BigDecimal soTienGiamThucTe = giaBan.subtract(giaSau);
            if (soTienGiamThucTe.compareTo(dot.getSoTienGiamToiDa()) > 0) {
                giaSau = giaBan.subtract(dot.getSoTienGiamToiDa());
            }
        }

        // không cho < 0
        return giaSau.max(BigDecimal.ZERO);
    }

    @Transactional
    public DotGiamGiaDetailResponse getDotGiamGiaDetail(Integer id) {
        DotGiamGia dot = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá"));

        // lấy danh sách chi tiết sản phẩm thuộc đợt giảm giá
        List<ChiTietDotGiamGia> chiTietList =
                chiTietDotGiamGiaRepository.findByIdDotGiamGia_IdAndDeletedFalse(id);

        // map sang DTO sản phẩm
        List<SanPhamDetailResponse> sanPhamDtos = chiTietList.stream()
                .map(ct -> SanPhamDetailResponse.builder()
                        .idSanPham(ct.getIdChiTietSp().getId())
                        .tenSanPham(ct.getIdChiTietSp().getIdSanPham().getTenSanPham())
                        .maSanPham(ct.getIdChiTietSp().getMa())
                        .giaBanDau(ct.getGiaBanDau())
                        .giaSauKhiGiam(ct.getGiaSauKhiGiam())
                        .build())
                .toList();

        // build response
        return DotGiamGiaDetailResponse.builder()
                .id(dot.getId())
                .ma(dot.getMa())
                .tenDotGiamGia(dot.getTenDotGiamGia())
                .loaiGiamGiaApDung(dot.getLoaiGiamGiaApDung())
                .giaTriGiamGia(dot.getGiaTriGiamGia())
                .soTienGiamToiDa(dot.getSoTienGiamToiDa())
                .ngayBatDau(dot.getNgayBatDau()) // convert LocalDate -> Instant
                .ngayKetThuc(dot.getNgayKetThuc())
                .trangThai(dot.getTrangThai())
                .danhSachSanPham(sanPhamDtos)
                .build();
    }

    @Transactional
    public DotGiamGia updateDotGiamGia(Integer id, DotGiamGiaDTO dto) {
        DotGiamGia dot = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá"));
        validateAndNormalize(dto);

        // 1. Update thông tin cơ bản
        if (dto.getMa() != null) {
            dot.setMa(dto.getMa()); // form sửa không gửi mã -> giữ mã cũ thay vì ghi đè thành null
        }
        dot.setTenDotGiamGia(dto.getTenDotGiamGia());
        dot.setLoaiGiamGiaApDung(dto.getLoaiGiamGiaApDung());
        dot.setGiaTriGiamGia(dto.getGiaTriGiamGia());
        dot.setSoTienGiamToiDa(dto.getSoTienGiamToiDa());
        dot.setNgayBatDau(dto.getNgayBatDau());
        dot.setNgayKetThuc(dto.getNgayKetThuc());
        kiemTraGiaSauGiamKhongDuoiGiaNhap(dot, dto.getListSanPhamId());

        DotGiamGia savedDot = dotGiamGiaRepository.save(dot);

        // 2. Lấy chi tiết hiện tại
        List<ChiTietDotGiamGia> oldChiTietList = chiTietDotGiamGiaRepository.findByIdDotGiamGia_IdAndDeletedFalse(id);

        // Map cũ để so sánh
        Map<Integer, ChiTietDotGiamGia> oldMap = oldChiTietList.stream()
                .collect(Collectors.toMap(ct -> ct.getIdChiTietSp().getId(), ct -> ct));

        // 3. Xử lý danh sách mới
        for (ChiTietSanPham sp : chiTietSanPhamRepository.findAllById(dto.getListSanPhamId())) {
            BigDecimal giaBanDau = sp.getGiaBan();
            BigDecimal giaSauKhiGiam = tinhGiaSauKhiGiam(savedDot, giaBanDau);

            if (oldMap.containsKey(sp.getId())) {
                // Nếu sản phẩm đã tồn tại -> cập nhật giá
                ChiTietDotGiamGia ctgg = oldMap.get(sp.getId());
                ctgg.setGiaBanDau(giaBanDau);
                ctgg.setGiaSauKhiGiam(giaSauKhiGiam);
                chiTietDotGiamGiaRepository.save(ctgg);

                oldMap.remove(sp.getId()); // bỏ khỏi map để tí nữa không xóa
            } else {
                // Nếu sản phẩm mới -> thêm mới
                ChiTietDotGiamGia ctgg = ChiTietDotGiamGia.builder()
                        .idDotGiamGia(savedDot)
                        .idChiTietSp(sp)
                        .ma("CTGG-" + sp.getId() + "-" + savedDot.getId())
                        .giaBanDau(giaBanDau)
                        .giaSauKhiGiam(giaSauKhiGiam)
                        .deleted(false)
                        .build();
                chiTietDotGiamGiaRepository.save(ctgg);
            }
        }

        // 4. Xóa hẳn những sản phẩm cũ không còn trong list mới
        for (ChiTietDotGiamGia ct : oldMap.values()) {
            chiTietDotGiamGiaRepository.delete(ct);
        }

        return savedDot;
    }

    @Transactional
    public DotGiamGia toggleStatus(Integer id, Boolean newStatus) {
        DotGiamGia dot = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá"));
        
        dot.setTrangThai(newStatus);
        return dotGiamGiaRepository.save(dot);
    }

}
