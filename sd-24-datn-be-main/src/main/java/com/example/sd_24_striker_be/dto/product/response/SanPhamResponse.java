package com.example.sd_24_striker_be.dto.product.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamResponse {
    private Integer id;
    private String ma;
    private String tenSanPham;
    private String moTa;
    /** true = Hoạt động. */
    private Boolean trangThai;
    private Boolean deleted;
    private Instant ngayTao;
    private Instant ngayCapNhat;
    private String urlAnhDaiDien;

    private Integer idDanhMuc;
    private String tenDanhMuc;

    private Integer idThuongHieu;
    private String tenThuongHieu;

    private Integer idChatLieu;
    private String tenChatLieu;

    private List<ChiTietSanPhamResponse> chiTietSanPhams;

    private Long totalVariants;
    private Long activeVariants;

    /** Tổng tồn kho của các biến thể đang hoạt động. */
    private Integer tongSoLuongTonKho;
    /** Tổng đã bán của mọi biến thể. */
    private Integer tongSoLuongDaBan;
    /** Số biến thể đang hoạt động nhưng hết hàng. */
    private Long soBienTheHetHang;

    private BigDecimal giaBanThapNhat;
    private BigDecimal giaBanCaoNhat;
    private List<String> danhSachMauSac;
    private List<String> danhSachKichCo;
}
