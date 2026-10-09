package com.example.sd_24_striker_be.modal.product;

import java.time.Instant;

/**
 * Hợp đồng chung của các thuộc tính sản phẩm (thương hiệu, thể loại, chất liệu, kích cỡ, màu sắc)
 * để dùng chung một bộ service/controller thay vì 5 bản sao gần giống nhau.
 * Mỗi entity giữ tên cột riêng, chỉ ánh xạ sang các accessor chung này.
 *
 * Trạng thái: deleted = false là "Hoạt động", deleted = true là "Ngừng hoạt động". Không xóa cứng.
 */
public interface ThuocTinh {
    Integer getId();

    String getTen();

    void setTen(String ten);

    String getMa();

    void setMa(String ma);

    String getMoTa();

    void setMoTa(String moTa);

    Boolean getDeleted();

    void setDeleted(Boolean deleted);

    Instant getNgayTao();

    void setNgayTao(Instant ngayTao);

    void setNgayCapNhat(Instant ngayCapNhat);
}
