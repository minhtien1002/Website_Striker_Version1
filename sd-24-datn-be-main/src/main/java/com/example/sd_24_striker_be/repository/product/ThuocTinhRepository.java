package com.example.sd_24_striker_be.repository.product;

import com.example.sd_24_striker_be.modal.product.ThuocTinh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * Các truy vấn mà mọi repository thuộc tính phải cung cấp (mỗi repository tự viết @Query
 * theo tên cột của mình).
 */
@NoRepositoryBean
public interface ThuocTinhRepository<T extends ThuocTinh> extends JpaRepository<T, Integer> {

    /** keyword rỗng = bỏ qua; deleted null = mọi trạng thái. */
    Page<T> search(String keyword, Boolean deleted, Pageable pageable);

    /** So sánh tên đã trim, không phân biệt hoa thường ("adidas" trùng "Adidas"). */
    long countTrungTen(String ten, Integer excludeId);

    /** Số sản phẩm đang dùng thuộc tính (đối với kích cỡ / màu sắc: số sản phẩm có biến thể dùng). */
    long countSanPhamSuDung(Integer id);

    List<T> findByDeletedFalseOrderByIdDesc();
}
