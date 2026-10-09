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
        if (dto.getNgayBatDau().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Ngày bắt đầu không được trước ngày hôm nay");
        }
        if (dto.getTenDotGiamGia().trim().length() > 255) {
            throw new IllegalArgumentException("Tên đợt giảm giá tối đa 255 ký tự");
        }
        dto.setTenDotGiamGia(dto.getTenDotGiamGia().trim().replaceAll("\\s+", " "));
        if (!phanTram && dto.getGiaTriGiamGia().compareTo(BigDecimal.valueOf(1000)) < 0) {
            throw new IllegalArgumentException("Số tiền giảm tối thiểu 1.000đ");
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

        DotGiamGia savedDot = dotGiamGiaRepository.saveAndFlush(dot);
        if (savedDot.getMa() == null || savedDot.getMa().isBlank()) {
            savedDot.setMa(String.format("DGG%03d", savedDot.getId()));
            savedDot = dotGiamGiaRepository.save(savedDot);
        }

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
                        .maSanPham(ct.getIdChiTietSp().getIdSanPham().getMa())
                        .maChiTiet(ct.getIdChiTietSp().getMa())
                        .mauSac(ct.getIdChiTietSp().getIdMauSac() != null ? ct.getIdChiTietSp().getIdMauSac().getTenMauSac() : null)
                        .kichCo(ct.getIdChiTietSp().getIdKichCo() != null ? ct.getIdChiTietSp().getIdKichCo().getTenKichCo() : null)
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
    public DotGiamGia toggleStatus(Integer id, Boolean newStatus) {
        DotGiamGia dot = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá"));
        
        dot.setTrangThai(newStatus);
        return dotGiamGiaRepository.save(dot);
    }

}
