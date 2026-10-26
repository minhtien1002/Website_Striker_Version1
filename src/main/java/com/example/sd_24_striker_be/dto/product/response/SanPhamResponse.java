package com.example.sd_24_striker_be.dto.product.response;

import lombok.*;

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
    private String quocGiaSanXuat;
    private Boolean trangThai;
    private Boolean deleted;
    private Instant ngayTao;
    private Instant ngayCapNhat;
    
    // Category info
    private Integer idDanhMuc;
    private String tenDanhMuc;
    
    // Brand info  
    private Integer idThuongHieu;
    private String tenThuongHieu;
    
    // Material info
    private Integer idChatLieu;
    private String tenChatLieu;
    
    // Fabric technology info
    private Integer idCongNgheVai;
    private String tenCongNgheVai;

    // Sport info
    private Integer idMonTheThao;
    private String tenMonTheThao;
    
    // Product variants
    private List<ChiTietSanPhamResponse> chiTietSanPhams;
    
    // Additional info
    private Long totalVariants;
    private Long activeVariants;

    /**
     * Tổng tồn kho của sản phẩm = cộng so_luong_ton_kho của TẤT CẢ biến thể còn hoạt động
     * (deleted = false). Dùng cho cột "Số lượng" ở màn Quản lý sản phẩm, để người quản lý
     * thấy ngay còn bao nhiêu hàng mà không phải bấm vào từng sản phẩm xem biến thể.
     * Biến thể đã ngừng kinh doanh không được cộng vào vì hàng đó không bán được nữa.
     */
    private Integer tongSoLuongTonKho;

    /** Số biến thể còn hoạt động nhưng đã hết hàng (tồn kho = 0), để cảnh báo trên danh sách. */
    private Long soBienTheHetHang;
}
