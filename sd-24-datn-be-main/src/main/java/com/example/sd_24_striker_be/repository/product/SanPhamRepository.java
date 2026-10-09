package com.example.sd_24_striker_be.repository.product;

import com.example.sd_24_striker_be.modal.product.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {

    /**
     * Danh sách quản lý sản phẩm có lọc. Không fetch join collection để phân trang đúng ở DB;
     * dữ liệu liên quan được nạp lười trong transaction của service.
     */
    @Query("SELECT sp FROM SanPham sp WHERE " +
           "(:keyword = '' OR LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(sp.ma) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:idThuongHieu IS NULL OR sp.idThuongHieu.id = :idThuongHieu) " +
           "AND (:idDanhMuc IS NULL OR sp.idDanhMuc.id = :idDanhMuc) " +
           "AND (:idChatLieu IS NULL OR sp.idChatLieu.id = :idChatLieu) " +
           "AND (:deleted IS NULL OR COALESCE(sp.deleted, false) = :deleted) " +
           // Khoảng giá: sản phẩm có ít nhất một biến thể đang bán với giá bán nằm trong khoảng
           "AND (:minGia IS NULL AND :maxGia IS NULL OR EXISTS (SELECT 1 FROM ChiTietSanPham ct WHERE ct.idSanPham = sp " +
           "     AND COALESCE(ct.deleted, false) = false " +
           "     AND (:minGia IS NULL OR ct.giaBan >= :minGia) AND (:maxGia IS NULL OR ct.giaBan <= :maxGia)))")
    Page<SanPham> search(@Param("keyword") String keyword,
                         @Param("idThuongHieu") Integer idThuongHieu,
                         @Param("idDanhMuc") Integer idDanhMuc,
                         @Param("idChatLieu") Integer idChatLieu,
                         @Param("deleted") Boolean deleted,
                         @Param("minGia") java.math.BigDecimal minGia,
                         @Param("maxGia") java.math.BigDecimal maxGia,
                         Pageable pageable);

    @Query("SELECT COUNT(sp) FROM SanPham sp WHERE LOWER(TRIM(sp.tenSanPham)) = LOWER(:ten) AND (:excludeId IS NULL OR sp.id <> :excludeId)")
    long countTrungTen(@Param("ten") String ten, @Param("excludeId") Integer excludeId);

    @Query("SELECT sp FROM SanPham sp WHERE COALESCE(sp.deleted, false) = false")
    Page<SanPham> findActivePaged(Pageable pageable);

    @Query("SELECT sp FROM SanPham sp WHERE COALESCE(sp.deleted, false) = false AND sp.idThuongHieu.id IN :brandIds")
    Page<SanPham> findProductsByBrandIds(@Param("brandIds") List<Integer> brandIds, Pageable pageable);

    @Query("SELECT sp FROM SanPham sp WHERE COALESCE(sp.deleted, false) = false AND sp.idDanhMuc.tenDanhMuc = :categoryName")
    Page<SanPham> findProductsByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

    /** Sản phẩm còn ít nhất một biến thể đang hoạt động và còn hàng - dùng cho đợt giảm giá. */
    @Query("SELECT sp FROM SanPham sp WHERE COALESCE(sp.deleted, false) = false " +
           "AND EXISTS (SELECT 1 FROM ChiTietSanPham ct WHERE ct.idSanPham = sp " +
           "            AND COALESCE(ct.deleted, false) = false AND ct.soLuongTonKho > 0)")
    Page<SanPham> findProductsAvailableForDiscount(Pageable pageable);

    @Query("SELECT COUNT(sp) FROM SanPham sp WHERE COALESCE(sp.deleted, false) = false")
    Long getTotalActiveProducts();
}
