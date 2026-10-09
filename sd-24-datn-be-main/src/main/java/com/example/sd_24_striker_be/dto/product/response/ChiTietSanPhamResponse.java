package com.example.sd_24_striker_be.dto.product.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietSanPhamResponse {
    private Integer id;
    private String ma;
    private BigDecimal giaBan;
    private Integer soLuongTonKho;
    /** Số lượng đã bán, để đối chiếu với tồn kho. */
    private Integer soLuongDaBan;
    private String moTaChiTiet;
    /** true = Hoạt động. */
    private Boolean trangThai;
    private Boolean deleted;
    private Instant ngayTao;
    private Instant ngayCapNhat;

    private Integer idSanPham;
    private String maSanPham;
    private String tenSanPham;

    private Integer idMauSac;
    private String tenMauSac;

    private Integer idKichCo;
    private String tenKichCo;

    private String urlAnhSanPham;
}
