package com.example.sd_24_striker_be.dto.product.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietSanPhamUpdateRequest {
    @NotNull(message = "Vui lòng chọn màu sắc")
    private Integer idMauSac;

    @NotNull(message = "Vui lòng chọn kích cỡ")
    private Integer idKichCo;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    @Max(value = 100000, message = "Số lượng tối đa 100.000")
    private Integer soLuongTonKho;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "1000", message = "Giá bán tối thiểu 1.000đ")
    @DecimalMax(value = "100000000", message = "Giá bán tối đa 100.000.000đ")
    private BigDecimal giaBan;

    @Size(max = 500, message = "Mô tả chi tiết tối đa 500 ký tự")
    private String moTaChiTiet;

    private String urlAnhSanPham;
}
