package com.example.sd_24_striker_be.dto.sell.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddToCartRequest {
    
    @NotNull(message = "ID chi tiết sản phẩm không được để trống")
    private Integer idChiTietSanPham;
    
    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;
    
    /** Không bắt buộc và bị bỏ qua: server luôn tự tính giá hiện hành (giá bán + đợt giảm giá). */
    private BigDecimal gia;
}
