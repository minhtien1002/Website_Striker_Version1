package com.example.sd_24_striker_be.repository.product;

import com.example.sd_24_striker_be.modal.product.ChiTietSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {

    String FETCH = "LEFT JOIN FETCH ctsp.idSanPham sp " +
                   "LEFT JOIN FETCH sp.idDanhMuc " +
                   "LEFT JOIN FETCH sp.idThuongHieu " +
                   "LEFT JOIN FETCH sp.idChatLieu " +
                   "LEFT JOIN FETCH sp.idAnhSanPham " +
                   "LEFT JOIN FETCH ctsp.idMauSac " +
                   "LEFT JOIN FETCH ctsp.idKichCo " +
                   "LEFT JOIN FETCH ctsp.idAnhSanPham ";

    /** Bán được = biến thể và sản phẩm đều đang hoạt động, còn hàng. */
    String BAN_DUOC = "COALESCE(ctsp.deleted, false) = false AND COALESCE(sp.deleted, false) = false AND ctsp.soLuongTonKho > 0 ";

    /**
     * Lọc chi tiết sản phẩm (màn quản lý). Chỉ lọc theo giá bán - dự án không dùng giá nhập.
     * status: active | inactive | out_of_stock | rỗng.
     */
    @Query(value = "SELECT ctsp FROM ChiTietSanPham ctsp " + FETCH +
           "WHERE (:productId IS NULL OR sp.id = :productId) " +
           "AND (:search = '' OR LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(ctsp.ma) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:sizeId IS NULL OR ctsp.idKichCo.id = :sizeId) " +
           "AND (:colorId IS NULL OR ctsp.idMauSac.id = :colorId) " +
           "AND (:status = '' " +
           "     OR (:status = 'active' AND COALESCE(ctsp.deleted, false) = false) " +
           "     OR (:status = 'inactive' AND ctsp.deleted = true) " +
           "     OR (:status = 'out_of_stock' AND COALESCE(ctsp.deleted, false) = false AND ctsp.soLuongTonKho = 0)) " +
           "AND (:minPrice IS NULL OR ctsp.giaBan >= :minPrice) " +
           "AND (:maxPrice IS NULL OR ctsp.giaBan <= :maxPrice)",
           countQuery = "SELECT COUNT(ctsp) FROM ChiTietSanPham ctsp JOIN ctsp.idSanPham sp " +
           "WHERE (:productId IS NULL OR sp.id = :productId) " +
           "AND (:search = '' OR LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(ctsp.ma) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:sizeId IS NULL OR ctsp.idKichCo.id = :sizeId) " +
           "AND (:colorId IS NULL OR ctsp.idMauSac.id = :colorId) " +
           "AND (:status = '' " +
           "     OR (:status = 'active' AND COALESCE(ctsp.deleted, false) = false) " +
           "     OR (:status = 'inactive' AND ctsp.deleted = true) " +
           "     OR (:status = 'out_of_stock' AND COALESCE(ctsp.deleted, false) = false AND ctsp.soLuongTonKho = 0)) " +
           "AND (:minPrice IS NULL OR ctsp.giaBan >= :minPrice) " +
           "AND (:maxPrice IS NULL OR ctsp.giaBan <= :maxPrice)")
    Page<ChiTietSanPham> findAllWithFilters(@Param("productId") Integer productId,
                                           @Param("search") String search,
                                           @Param("sizeId") Integer sizeId,
                                           @Param("colorId") Integer colorId,
                                           @Param("status") String status,
                                           @Param("minPrice") Double minPrice,
                                           @Param("maxPrice") Double maxPrice,
                                           Pageable pageable);

    @Query("SELECT COUNT(ctsp) FROM ChiTietSanPham ctsp WHERE ctsp.idSanPham.id = :productId " +
           "AND ctsp.idMauSac.id = :colorId AND ctsp.idKichCo.id = :sizeId AND (:excludeId IS NULL OR ctsp.id <> :excludeId)")
    long countTrungBienThe(@Param("productId") Integer productId, @Param("colorId") Integer colorId,
                           @Param("sizeId") Integer sizeId, @Param("excludeId") Integer excludeId);

    @Query("SELECT ctsp FROM ChiTietSanPham ctsp " + FETCH + "WHERE sp.id = :productId AND " + BAN_DUOC)
    List<ChiTietSanPham> findActiveByProductIdForSales(@Param("productId") Integer productId);

    /** Mọi biến thể đang hoạt động của sản phẩm, kể cả hết hàng (để trang khách hiển thị "Hết hàng"). */
    @Query("SELECT ctsp FROM ChiTietSanPham ctsp " + FETCH + "WHERE sp.id = :productId " +
           "AND COALESCE(ctsp.deleted, false) = false AND COALESCE(sp.deleted, false) = false ORDER BY ctsp.id")
    List<ChiTietSanPham> findActiveByProductIdIncludingOutOfStock(@Param("productId") Integer productId);

    @Query(value = "SELECT ctsp FROM ChiTietSanPham ctsp " + FETCH + "WHERE " + BAN_DUOC,
           countQuery = "SELECT COUNT(ctsp) FROM ChiTietSanPham ctsp JOIN ctsp.idSanPham sp WHERE " + BAN_DUOC)
    Page<ChiTietSanPham> findActiveProductDetailsForSales(Pageable pageable);

    @Query(value = "SELECT ctsp FROM ChiTietSanPham ctsp " + FETCH + "WHERE " + BAN_DUOC +
           "AND (LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(sp.ma) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(ctsp.ma) LIKE LOWER(CONCAT('%', :keyword, '%')))",
           countQuery = "SELECT COUNT(ctsp) FROM ChiTietSanPham ctsp JOIN ctsp.idSanPham sp WHERE " + BAN_DUOC +
           "AND (LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(sp.ma) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(ctsp.ma) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ChiTietSanPham> findActiveProductDetailsForSalesWithKeyword(Pageable pageable, @Param("keyword") String keyword);

    @Query("SELECT ctsp FROM ChiTietSanPham ctsp " + FETCH + "WHERE " + BAN_DUOC + "ORDER BY ctsp.id DESC")
    List<ChiTietSanPham> findActiveProductDetailsForSales();

    @Modifying
    @Query("UPDATE ChiTietSanPham ctsp SET ctsp.deleted = :deletedStatus, ctsp.ngayCapNhat = CURRENT_TIMESTAMP WHERE ctsp.idSanPham.id = :productId")
    void updateDeletedStatusByProductId(@Param("productId") Integer productId, @Param("deletedStatus") Boolean deletedStatus);
}
