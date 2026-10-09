package com.example.sd_24_striker_be.dto.product.response;

import com.example.sd_24_striker_be.modal.product.ChiTietSanPham;
import com.example.sd_24_striker_be.modal.product.SanPham;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Biến thể kèm giá sau đợt giảm giá - dùng cho bán tại quầy và trang khách hàng. */
@Getter
@Setter
@NoArgsConstructor
public class ChiTietSanPhamWithDiscountResponse {
    private Integer id;
    private String ma;
    private BigDecimal giaBan;
    private Integer soLuongTonKho;

    private Integer idSanPham;
    private String maSanPham;
    private String tenSanPham;
    private String tenDanhMuc;
    private String tenThuongHieu;
    private String tenChatLieu;

    private Integer idMauSac;
    private String tenMauSac;
    private Integer idKichCo;
    private String tenKichCo;

    private String urlAnhSanPham;

    private BigDecimal giaGiamGia;
    private String tenDotGiamGia;
    private boolean hasDiscount;

    public static ChiTietSanPhamWithDiscountResponse from(ChiTietSanPham ct, BigDecimal giaGiamGia, String tenDotGiamGia) {
        ChiTietSanPhamWithDiscountResponse r = new ChiTietSanPhamWithDiscountResponse();
        r.id = ct.getId();
        r.ma = ct.getMa();
        r.giaBan = ct.getGiaBan();
        r.soLuongTonKho = ct.getSoLuongTonKho();
        SanPham sp = ct.getIdSanPham();
        if (sp != null) {
            r.idSanPham = sp.getId();
            r.maSanPham = sp.getMa();
            r.tenSanPham = sp.getTenSanPham();
            r.tenDanhMuc = sp.getIdDanhMuc() != null ? sp.getIdDanhMuc().getTenDanhMuc() : null;
            r.tenThuongHieu = sp.getIdThuongHieu() != null ? sp.getIdThuongHieu().getTenThuongHieu() : null;
            r.tenChatLieu = sp.getIdChatLieu() != null ? sp.getIdChatLieu().getTenChatLieu() : null;
        }
        if (ct.getIdMauSac() != null) {
            r.idMauSac = ct.getIdMauSac().getId();
            r.tenMauSac = ct.getIdMauSac().getTenMauSac();
        }
        if (ct.getIdKichCo() != null) {
            r.idKichCo = ct.getIdKichCo().getId();
            r.tenKichCo = ct.getIdKichCo().getTenKichCo();
        }
        // Ảnh riêng của biến thể nếu có, không thì dùng ảnh đại diện của sản phẩm
        if (ct.getIdAnhSanPham() != null) {
            r.urlAnhSanPham = ct.getIdAnhSanPham().getUrlAnh();
        } else if (sp != null && sp.getIdAnhSanPham() != null) {
            r.urlAnhSanPham = sp.getIdAnhSanPham().getUrlAnh();
        }
        r.giaGiamGia = giaGiamGia;
        r.tenDotGiamGia = tenDotGiamGia;
        r.hasDiscount = giaGiamGia != null;
        return r;
    }
}
