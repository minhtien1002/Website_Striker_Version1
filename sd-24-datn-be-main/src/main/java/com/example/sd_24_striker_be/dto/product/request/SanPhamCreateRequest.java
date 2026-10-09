package com.example.sd_24_striker_be.dto.product.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamCreateRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String tenSanPham;

    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String moTaSanPham;

    @NotNull(message = "Vui lòng chọn thể loại")
    private Integer idDanhMuc;

    @NotNull(message = "Vui lòng chọn thương hiệu")
    private Integer idThuongHieu;

    @NotNull(message = "Vui lòng chọn chất liệu")
    private Integer idChatLieu;

    private String urlAnhDaiDien;
}
