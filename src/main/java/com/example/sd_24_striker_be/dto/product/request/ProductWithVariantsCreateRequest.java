package com.example.sd_24_striker_be.dto.product.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductWithVariantsCreateRequest {
    @Valid
    @NotNull(message = "Thông tin sản phẩm không được để trống")
    private SanPhamCreateRequest sanPham;
    
    @Valid
    @NotEmpty(message = "Danh sách biến thể không được để trống")
    private List<ChiTietSanPhamCreateRequest> chiTietSanPhams;
}
